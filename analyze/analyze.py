#!/usr/bin/env python3
"""
R2 일별 로그 분석기 (레벨별).

레벨(info/warn/error)마다 R2의 logs/<level>/YYYY/MM/DD/ 를 읽어 요약 리포트를 만들고,
해당 레벨 전용 디스코드 채널 웹훅으로 임베드를 전송 + R2 reports/<level> 에 저장한다.
레벨마다 성격이 달라 리포트 구성도 다르다:
  - info  : 트래픽 대시보드(요청수 / 에러율 / 상태코드 분포 / 응답시간 / 엔드포인트 TOP)
  - warn  : 예외 경고(엔드포인트 / 메시지 / 멤버 / 시간대 TOP)
  - error : 에러(예외 클래스 / 엔드포인트 / 멤버 / 시간대 TOP)

사용법:
  LEVEL=warn  python analyze.py                  # 어제치(KST)
  LEVEL=info  python analyze.py --date 2026-06-24

환경변수:
  LEVEL            info | warn | error   (기본: warn)
  R2_ACCOUNT_ID, R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY   (필수)
선택:
  R2_BUCKET        (기본: gugutalk)
  LOG_PREFIX       (기본: logs/<level>)
  REPORT_PREFIX    (기본: reports/<level>)  - 비우면 R2 저장 안 함
  DISCORD_WEBHOOK  (설정 시 요약 임베드를 해당 레벨 채널로 전송)
  TZ_OFFSET_HOURS  (기본: 9 = KST, "어제" 계산 기준)
"""
from __future__ import annotations

import os, sys, json, gzip, re, argparse, datetime
from collections import Counter, defaultdict

LEVELS = ("info", "warn", "error")

# 레벨별 임베드 색상.
LEVEL_COLOR = {"info": 0x3498DB, "warn": 0xF1C40F, "error": 0xE74C3C}

# method/uri/ip/status/ms 는 message 문자열이 아니라 MDC/구조화필드 -> JSON 최상위 필드로 들어온다.
# message 에는 "메세지 = ..." 라벨만 남으므로 라벨을 떼고 본문만 취한다.
MSG_LABEL_RE = re.compile(r"^메세지 = ")

# APNs 푸시 전송 거절 로그(warn). 토큰은 디바이스마다 달라 버리고 '사유'로 묶는다.
APNS_RE = re.compile(r"APNs .*?사유 = (?P<reason>.+)", re.S)

# ---------- 공통 정규화 ----------
def normalize_uri(uri: str) -> str:
    """숫자 path segment를 {id}로 치환해 같은 엔드포인트끼리 묶는다."""
    return re.sub(r"/\d+", "/{id}", (uri or "").strip())

def normalize_msg(msg: str) -> str:
    """장황한 Spring validation 에러를 '[Validation] field: 사용자메시지' 형태로 축약."""
    msg = (msg or "").strip()
    if msg.startswith("Validation failed"):
        field = re.search(r"on field '([^']+)'", msg)
        defaults = re.findall(r"default message \[([^\]]+)\]", msg)
        human = defaults[-1] if defaults else "?"
        f = field.group(1) if field else "?"
        return f"[Validation] {f}: {human}"
    return msg or "(빈 메시지)"

def hour_of(log: dict) -> str:
    """로그 타임스탬프에서 '17' 같은 시(hour) 추출."""
    return (str(log.get("date", "")) + "T")[11:13]

def exception_summary(log: dict) -> str | None:
    """스택트레이스 첫 줄(예외 클래스 + 메시지)로 같은 예외끼리 묶는다."""
    st = log.get("stack_trace") or ""
    if not st:
        return None
    first = st.splitlines()[0].strip() if st.splitlines() else ""
    return first[:160] or None

# ---------- 레벨별 파싱 ----------
def parse_info(log: dict, raw: str, hour: str, stats: dict):
    method, uri, status = log.get("method"), log.get("uri"), log.get("status")
    if method and uri and status is not None:
        stats["req_total"] += 1
        code = int(status)
        stats["status_code"][code] += 1
        stats["status_class"][f"{code // 100}xx"] += 1
        ep = (method, normalize_uri(uri))
        stats["by_endpoint"][ep] += 1
        stats["by_hour"][hour] += 1
        ms = log.get("ms")
        if ms is not None:
            ms = int(ms)
            stats["ep_ms_sum"][ep] += ms
            stats["ep_ms_cnt"][ep] += 1
            stats["ep_ms_max"][ep] = max(stats["ep_ms_max"][ep], ms)
        return
    # 요청 요약이 아닌 일반 info 로그
    stats["no_match"] += 1

def parse_warn(log: dict, raw: str, hour: str, stats: dict):
    a = APNS_RE.search(raw)
    if a:
        stats["by_msg"][f"[APNs 거절] {a['reason'].strip()}"] += 1
        stats["by_hour"][hour] += 1
        return
    method, uri = log.get("method"), log.get("uri")
    if method and uri:
        stats["req_total"] += 1
        stats["by_endpoint"][(method, normalize_uri(uri))] += 1
        stats["by_msg"][normalize_msg(MSG_LABEL_RE.sub("", raw))] += 1
        stats["by_member"][str(log.get("memberId", "?"))] += 1
        stats["by_hour"][hour] += 1
        return
    stats["no_match"] += 1

def parse_error(log: dict, raw: str, hour: str, stats: dict):
    exc = exception_summary(log)
    if exc:
        stats["by_exception"][exc] += 1
    method, uri = log.get("method"), log.get("uri")
    if method and uri:
        stats["req_total"] += 1
        stats["by_endpoint"][(method, normalize_uri(uri))] += 1
        stats["by_member"][str(log.get("memberId", "?"))] += 1
        stats["by_hour"][hour] += 1
        if not exc:
            stats["by_msg"][normalize_msg(MSG_LABEL_RE.sub("", raw))] += 1
        return
    if not exc:
        stats["no_match"] += 1


PARSERS = {"info": parse_info, "warn": parse_warn, "error": parse_error}

def parse_lines(text: str, stats: dict, level: str):
    """NDJSON 텍스트 한 덩어리를 파싱해 stats 누적."""
    parser = PARSERS[level]
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
        raw = log.get("message", "") or ""
        parser(log, raw, hour_of(log), stats)

def new_stats():
    return {
        "total": 0,
        "unparsed": 0,
        "no_match": 0,
        "req_total": 0,
        "by_endpoint": Counter(),
        "by_msg": Counter(),
        "by_member": Counter(),
        "by_hour": Counter(),
        # info 전용
        "status_code": Counter(),
        "status_class": Counter(),
        "ep_ms_sum": defaultdict(int),
        "ep_ms_cnt": defaultdict(int),
        "ep_ms_max": defaultdict(int),
        # error 전용
        "by_exception": Counter(),
    }

# ---------- 레벨별 리포트 ----------
def build_report(stats: dict, date_str: str, level: str) -> dict:
    base = {
        "level": level,
        "date": date_str,
        "total": stats["total"],
        "total_requests": stats["req_total"],
        "unmatched": stats["no_match"] + stats["unparsed"],
        "by_hour": {h: stats["by_hour"][h] for h in sorted(stats["by_hour"])},
    }
    if level == "info":
        req = stats["req_total"]
        bad = sum(v for c, v in stats["status_code"].items() if c >= 400)
        slow = []
        for ep, cnt in stats["ep_ms_cnt"].items():
            slow.append((ep, stats["ep_ms_sum"][ep] / cnt, stats["ep_ms_max"][ep], cnt))
        slow.sort(key=lambda x: x[1], reverse=True)
        base.update(
            error_rate=round(bad / req * 100, 2) if req else 0.0,
            status_class={k: stats["status_class"][k] for k in sorted(stats["status_class"])},
            top_status=[{"code": c, "count": v} for c, v in stats["status_code"].most_common(8)],
            top_endpoints=[
                {"method": k[0], "uri": k[1], "count": v}
                for k, v in stats["by_endpoint"].most_common(20)
            ],
            slowest_endpoints=[
                {"method": ep[0], "uri": ep[1], "avg_ms": round(avg), "max_ms": mx, "count": cnt}
                for ep, avg, mx, cnt in slow[:15]
            ],
        )
    elif level == "error":
        base.update(
            top_exceptions=[
                {"exception": k, "count": v} for k, v in stats["by_exception"].most_common(20)
            ],
            top_endpoints=[
                {"method": k[0], "uri": k[1], "count": v}
                for k, v in stats["by_endpoint"].most_common(15)
            ],
            top_members=[
                {"memberId": k, "count": v} for k, v in stats["by_member"].most_common(15)
            ],
        )
    else:  # warn
        base.update(
            top_endpoints=[
                {"method": k[0], "uri": k[1], "count": v}
                for k, v in stats["by_endpoint"].most_common(20)
            ],
            top_messages=[
                {"message": k, "count": v} for k, v in stats["by_msg"].most_common(20)
            ],
            top_members=[
                {"memberId": k, "count": v} for k, v in stats["by_member"].most_common(15)
            ],
        )
    return base


# ---------- 디스코드 임베드 ----------
# 디스코드 임베드 field value는 최대 1024자. 넘으면 잘라서 '…' 표시.
def _embed_value(s: str) -> str:
    s = (s or "").strip() or "-"
    return s if len(s) <= 1024 else s[:1020] + "\n…"


def _footer_date(date_str: str) -> str:
    """리포트 날짜(YYYY-MM-DD)를 푸터용 DD/MM/YYYY 로 변환. 파싱 실패 시 원본."""
    try:
        d = datetime.date.fromisoformat(date_str)
        return d.strftime("%d/%m/%Y")
    except ValueError:
        return date_str


def _fmt_endpoints(eps: list) -> str:
    return "\n".join(f"{e['count']}건, {e['method']} - {e['uri']}" for e in eps[:5])


def _fmt_hours(report: dict) -> str:
    return "\n".join(f"{h}시, {c}건" for h, c in report["by_hour"].items())


def build_info_embed(report: dict) -> dict:
    status = "\n".join(f"{s['code']}: {s['count']}" for s in report["top_status"])
    slowest = "\n".join(
        f"{e['avg_ms']}ms, {e['max_ms']}ms, {e['count']}건, {e['method']} - {e['uri']}"
        for e in report["slowest_endpoints"][:5]
    )
    return {
        "title": "INFO 일일 리포트",
        "color": LEVEL_COLOR["info"],
        "fields": [
            {"name": "총 요청", "value": _embed_value(f"{report['total_requests']}건")},
            {"name": "에러율", "value": _embed_value(f"{report['error_rate']}%")},
            {"name": "상태코드 분포", "value": _embed_value(status)},
            {"name": "엔드포인트 TOP 5 (요청량)", "value": _embed_value(_fmt_endpoints(report["top_endpoints"]))},
            {"name": "느린 엔드포인트 TOP 5 (평균, 최대, 요청량)", "value": _embed_value(slowest)},
            {"name": "시간대 분포 (시간, 요청량)", "value": _embed_value(_fmt_hours(report))},
        ],
        "footer": {"text": _footer_date(report["date"])},
    }


def build_warn_embed(report: dict) -> dict:
    messages = "\n".join(f"{m['count']}건, {m['message']}" for m in report["top_messages"][:5])
    members = "\n".join(f"{m['count']}건, ID = {m['memberId']}" for m in report["top_members"][:5])
    return {
        "title": "WARN 일일 리포트",
        "color": LEVEL_COLOR["warn"],
        "fields": [
            {"name": "총 요청", "value": _embed_value(f"{report['total_requests']}건")},
            {"name": "엔드포인트 TOP 5 (요청량)", "value": _embed_value(_fmt_endpoints(report["top_endpoints"]))},
            {"name": "메세지 TOP 5 (요청량)", "value": _embed_value(messages)},
            {"name": "멤버별 TOP 5 (요청량)", "value": _embed_value(members)},
            {"name": "시간대 분포 (시간, 요청량)", "value": _embed_value(_fmt_hours(report))},
        ],
        "footer": {"text": _footer_date(report["date"])},
    }


def build_error_embed(report: dict) -> dict:
    exceptions = "\n".join(f"{x['count']}건, {x['exception']}" for x in report["top_exceptions"][:5])
    members = "\n".join(f"{m['count']}건, ID = {m['memberId']}" for m in report["top_members"][:5])
    return {
        "title": "ERROR 일일 리포트",
        "color": LEVEL_COLOR["error"],
        "fields": [
            {"name": "총 요청", "value": _embed_value(f"{report['total_requests']}건")},
            {"name": "예외 TOP 5 (요청량)", "value": _embed_value(exceptions)},
            {"name": "엔드포인트 TOP 5 (요청량)", "value": _embed_value(_fmt_endpoints(report["top_endpoints"]))},
            {"name": "멤버별 TOP 5 (요청량)", "value": _embed_value(members)},
            {"name": "시간대 분포 (시간, 요청량)", "value": _embed_value(_fmt_hours(report))},
        ],
        "footer": {"text": _footer_date(report["date"])},
    }


EMBED_BUILDERS = {"info": build_info_embed, "warn": build_warn_embed, "error": build_error_embed}


def build_embed(report: dict) -> dict:
    return EMBED_BUILDERS[report["level"]](report)


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


def analyze_r2(date: datetime.date, level: str) -> dict:
    s3 = r2_client()
    bucket = os.environ.get("R2_BUCKET", "gugutalk")
    log_prefix = os.environ.get("LOG_PREFIX", f"logs/{level}")
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
            parse_lines(body.decode("utf-8", "replace"), stats, level)
    print(f"[r2] {prefix}  files={nfiles}  lines={stats['total']}", file=sys.stderr)
    return stats


def save_report_r2(report: dict, date: datetime.date, level: str):
    rp = os.environ.get("REPORT_PREFIX", f"reports/{level}")
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
    print(f"[r2] report saved -> {base}.json", file=sys.stderr)


def notify_discord(embed: dict):
    hook = os.environ.get("DISCORD_WEBHOOK")
    if not hook:
        return
    import urllib.request

    payload = {"embeds": [embed]}
    req = urllib.request.Request(
        hook, data=json.dumps(payload).encode(),
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
    ap.add_argument("--level", help="info|warn|error (기본: env LEVEL 또는 warn)")
    args = ap.parse_args()

    level = (args.level or os.environ.get("LEVEL", "warn")).lower()
    if level not in LEVELS:
        sys.exit(f"LEVEL must be one of {LEVELS}, got '{level}'")

    off = int(os.environ.get("TZ_OFFSET_HOURS", "9"))
    if args.date:
        date = datetime.date.fromisoformat(args.date)
    else:
        now = datetime.datetime.utcnow() + datetime.timedelta(hours=off)
        date = now.date() - datetime.timedelta(days=1)

    stats = analyze_r2(date, level)
    report = build_report(stats, date.isoformat(), level)
    save_report_r2(report, date, level)
    notify_discord(build_embed(report))


if __name__ == "__main__":
    main()
