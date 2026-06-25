#!/usr/bin/env python3
"""
R2 warn 로그 일일 분석기.

사용법:
  # 어제치(KST) 자동 분석 - R2에서 읽고 리포트를 R2에 저장
  python analyze.py

  # 특정 날짜
  python analyze.py --date 2026-06-24

  # 로컬 파일로 파싱 검증 (R2 접속 없이)
  python analyze.py --local /path/to/logfile.json

환경변수 (R2 모드에서 필수):
  R2_ACCOUNT_ID, R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY
선택:
  R2_BUCKET        (기본: gugutalk)
  LOG_PREFIX       (기본: logs/warn)
  REPORT_PREFIX    (기본: reports/warn)  - 비우면 R2 저장 안 함
  DISCORD_WEBHOOK  (설정 시 요약을 디스코드로 전송)
  TZ_OFFSET_HOURS  (기본: 9 = KST, "어제" 계산 기준)
"""
import os, sys, json, re, gzip, io, argparse, datetime
from collections import Counter, defaultdict

# message 필드에서 METHOD/URI/IP/메시지 추출.
# IP는 IPv6(콜론 포함), 메시지는 콤마 포함 가능 -> 라벨명에 의존하지 않게 처리.
MSG_RE = re.compile(
    r"METHOD = (?P<method>\w+), URI = (?P<uri>[^,]+), IP = (?P<ip>[^,]+), \S+ = (?P<msg>.*)",
    re.S,
)

# APNs 푸시 전송 거절 로그. 토큰은 디바이스마다 달라 버리고 '사유'로 묶는다.
APNS_RE = re.compile(r"APNs .*?사유 = (?P<reason>.+)", re.S)


def normalize_uri(uri: str) -> str:
    """숫자 path segment를 {id}로 치환해 같은 엔드포인트끼리 묶는다."""
    return re.sub(r"/\d+", "/{id}", uri.strip())


def normalize_msg(msg: str) -> str:
    """장황한 Spring validation 에러를 'field: 사용자메시지' 형태로 축약."""
    msg = msg.strip()
    if msg.startswith("Validation failed"):
        field = re.search(r"on field '([^']+)'", msg)
        defaults = re.findall(r"default message \[([^\]]+)\]", msg)
        human = defaults[-1] if defaults else "?"
        f = field.group(1) if field else "?"
        return f"[Validation] {f}: {human}"
    return msg


def parse_lines(text: str, stats: dict):
    """NDJSON 텍스트 한 덩어리를 파싱해 stats 누적."""
    for line in text.splitlines():
        line = line.strip()
        if not line:
            continue
        stats["total"] += 1
        try:
            log = json.loads(line)
        except json.JSONDecodeError:
            stats["unparsed"] += 1
            continue
        raw = log.get("message", "")
        m = MSG_RE.search(raw)
        if not m:
            # HTTP 형식이 아니면 APNs 거절 로그인지 확인
            a = APNS_RE.search(raw)
            if a:
                reason = a["reason"].strip()
                stats["by_msg"][f"[APNs 거절] {reason}"] += 1
                stats["by_hour"][(log.get("date", "") + "T")[11:13]] += 1
                continue
            stats["no_match"] += 1
            stats["other_msgs"][raw[:120]] += 1
            continue
        uri = normalize_uri(m["uri"])
        msg = normalize_msg(m["msg"])
        hour = (log.get("date", "") + "T")[11:13]  # "17" 같은 시(hour)
        stats["by_endpoint"][(m["method"], uri)] += 1
        stats["by_msg"][msg] += 1
        stats["by_member"][str(log.get("memberId", "?"))] += 1
        stats["by_hour"][hour] += 1


def new_stats():
    return {
        "total": 0,
        "no_match": 0,
        "unparsed": 0,
        "by_endpoint": Counter(),
        "by_msg": Counter(),
        "by_member": Counter(),
        "by_hour": Counter(),
        "other_msgs": Counter(),
    }


def build_report(stats: dict, date_str: str) -> dict:
    return {
        "date": date_str,
        "total_warns": stats["total"],
        "unmatched": stats["no_match"] + stats["unparsed"],
        "top_endpoints": [
            {"method": k[0], "uri": k[1], "count": v}
            for k, v in stats["by_endpoint"].most_common(20)
        ],
        "top_messages": [
            {"message": k, "count": v} for k, v in stats["by_msg"].most_common(20)
        ],
        "top_members": [
            {"memberId": k, "count": v} for k, v in stats["by_member"].most_common(15)
        ],
        "by_hour": {h: stats["by_hour"][h] for h in sorted(stats["by_hour"])},
        "other_messages": [
            {"message": k, "count": v} for k, v in stats["other_msgs"].most_common(10)
        ],
    }


def render_text(report: dict) -> str:
    L = []
    L.append(f"📊 WARN 로그 일일 리포트  ({report['date']})")
    L.append(f"총 {report['total_warns']}건  (미매칭 {report['unmatched']}건)")
    L.append("")
    L.append("■ 엔드포인트 TOP")
    for e in report["top_endpoints"]:
        L.append(f"  {e['count']:>5}  {e['method']:5} {e['uri']}")
    L.append("")
    L.append("■ 메시지 TOP")
    for m in report["top_messages"]:
        L.append(f"  {m['count']:>5}  {m['message']}")
    L.append("")
    L.append("■ 멤버별 TOP (특정 유저가 폭주하는지 확인)")
    for m in report["top_members"]:
        L.append(f"  {m['count']:>5}  memberId={m['memberId']}")
    L.append("")
    L.append("■ 시간대 분포(시:건수)")
    L.append("  " + "  ".join(f"{h}:{c}" for h, c in report["by_hour"].items()))
    if report["other_messages"]:
        L.append("")
        L.append("■ 형식 미매칭 메시지(파서 점검용)")
        for m in report["other_messages"]:
            L.append(f"  {m['count']:>5}  {m['message']}")
    return "\n".join(L)


# ---------- R2 입출력 ----------
def r2_client():
    import boto3

    return boto3.client(
        "s3",
        endpoint_url=f"https://{os.environ['R2_ACCOUNT_ID']}.r2.cloudflarestorage.com",
        aws_access_key_id=os.environ["R2_ACCESS_KEY_ID"],
        aws_secret_access_key=os.environ["R2_SECRET_ACCESS_KEY"],
        region_name="auto",
    )


def analyze_r2(date: datetime.date) -> dict:
    s3 = r2_client()
    bucket = os.environ.get("R2_BUCKET", "gugutalk")
    log_prefix = os.environ.get("LOG_PREFIX", "logs/warn")
    prefix = f"{log_prefix}/{date.year}/{date.month:02d}/{date.day:02d}/"
    stats = new_stats()
    paginator = s3.get_paginator("list_objects_v2")
    nfiles = 0
    for page in paginator.paginate(Bucket=bucket, Prefix=prefix):
        for obj in page.get("Contents", []):
            nfiles += 1
            body = s3.get_object(Bucket=bucket, Key=obj["Key"])["Body"].read()
            if obj["Key"].endswith(".gz"):
                body = gzip.decompress(body)
            parse_lines(body.decode("utf-8", "replace"), stats)
    print(f"[r2] {prefix}  files={nfiles}  warns={stats['total']}", file=sys.stderr)
    return stats


def save_report_r2(report: dict, text: str, date: datetime.date):
    rp = os.environ.get("REPORT_PREFIX", "reports/warn")
    if not rp:
        return
    s3 = r2_client()
    bucket = os.environ.get("R2_BUCKET", "gugutalk")
    base = f"{rp}/{date.year}/{date.month:02d}/{date.day:02d}"
    s3.put_object(
        Bucket=bucket, Key=f"{base}.json",
        Body=json.dumps(report, ensure_ascii=False, indent=2).encode(),
        ContentType="application/json",
    )
    s3.put_object(
        Bucket=bucket, Key=f"{base}.txt",
        Body=text.encode(), ContentType="text/plain; charset=utf-8",
    )
    print(f"[r2] report saved -> {base}.json / .txt", file=sys.stderr)


def notify_discord(text: str):
    hook = os.environ.get("DISCORD_WEBHOOK")
    if not hook:
        return
    import urllib.request
    # 디스코드 content는 최대 2000자. ```...``` 래핑(6자) 여유까지 고려해 1900자에서 자른다.
    inner = text if len(text) <= 1900 else text[:1900] + "\n…(생략)"
    req = urllib.request.Request(
        hook, data=json.dumps({"content": f"```\n{inner}\n```"}).encode(),
        headers={
            "Content-Type": "application/json",
            # 디스코드는 Cloudflare 뒤에 있어 기본 urllib UA를 403으로 막는다. UA를 명시.
            "User-Agent": "gugutalk-log-analyzer/1.0 (+https://github.com)",
        },
    )
    try:
        urllib.request.urlopen(req, timeout=10)
        print("[discord] sent", file=sys.stderr)
    except Exception as e:
        # 알림 실패가 분석 잡 전체를 죽이지 않도록 경고만 남긴다.
        print(f"[discord] FAILED: {e}", file=sys.stderr)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--date", help="YYYY-MM-DD (기본: 어제)")
    ap.add_argument("--local", help="R2 대신 로컬 NDJSON 파일 분석(.gz도 가능)")
    args = ap.parse_args()

    if args.local:
        stats = new_stats()
        raw = open(args.local, "rb").read()
        if args.local.endswith(".gz"):
            raw = gzip.decompress(raw)
        parse_lines(raw.decode("utf-8", "replace"), stats)
        date_str = args.date or "local"
        report = build_report(stats, date_str)
        print(render_text(report))
        return

    off = int(os.environ.get("TZ_OFFSET_HOURS", "9"))
    if args.date:
        date = datetime.date.fromisoformat(args.date)
    else:
        now = datetime.datetime.utcnow() + datetime.timedelta(hours=off)
        date = now.date() - datetime.timedelta(days=1)

    stats = analyze_r2(date)
    report = build_report(stats, date.isoformat())
    text = render_text(report)
    print(text)
    save_report_r2(report, text, date)
    notify_discord(text)


if __name__ == "__main__":
    main()