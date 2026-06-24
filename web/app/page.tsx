import Reveal from "@/component/Reveal";
import mock_1 from "@/public/mockup/mockup-1.png";
import mock_2 from "@/public/mockup/mockup-2.png";
import mock_3 from "@/public/mockup/mockup-3.png";
import mock_4 from "@/public/mockup/mockup-4.png";
import mock_5 from "@/public/mockup/mockup-5.png";
import mock_6 from "@/public/mockup/mockup-6.png";
import Image from "next/image";

const features = [
  {
    image: mock_1,
    title: "마음이 오가는 익명 피드",
    description:
      "최근 올라온 글과 우리 지역 글을 한눈에 만나보세요. 마음에 드는 사용자에게 좋아요와 리뷰를 남기고, 모르는 누군가와의 인연을 시작할 수 있어요.",
  },
  {
    image: mock_2,
    title: "쌓여가는 대화 목록",
    description:
      "건넨 쪽지 한 통이 대화가 되는 순간. 전체와 안 읽은 메시지를 구분해 보여주니, 놓친 메세지 없이 이야기를 이어갈 수 있어요.",
  },
  {
    image: mock_3,
    title: "1:1 실시간 채팅",
    description:
      "익명으로 시작한 대화도 마음이 통하면 깊어집니다. 메시지와 사진, 영상을 주고받으며 진짜 대화를 나눠보세요.",
  },
  {
    image: mock_4,
    title: "오늘의 인기 멤버 랭킹",
    description:
      "좋아요와 싫어요, 그리고 리뷰로 매겨지는 랭킹. 가장 많은 사랑을 받는 사람들을 만나보고 나의 순위도 확인해보세요.",
  },
  {
    image: mock_5,
    title: "내 활동을 한곳에서",
    description:
      "프로필부터 좋아요, 차단 목록, 비밀 사진까지 깔끔하게 관리하세요. 출석 체크와 광고 보상으로 포인트도 차곡차곡 쌓을 수 있어요.",
  },
  {
    image: mock_6,
    title: "눈이 편한 다크 모드",
    description:
      "밤늦은 대화도 부담 없이. 라이트와 다크, 취향에 맞는 화면으로 언제든 편안하게 구구톡을 즐겨보세요.",
  },
];

export default function Home() {
  return (
    <div className="mx-auto w-full max-w-5xl py-12">
      <div className="flex flex-col gap-24">
        {features.map((feature, index) => (
          <Reveal key={index}>
            <section
              className={`flex flex-col items-center gap-10 md:gap-16 ${
                index % 2 === 1 ? "md:flex-row-reverse" : "md:flex-row"
              }`}
            >
              {/* 목업 이미지 */}
              <div className="shrink-0">
                <Image
                  src={feature.image}
                  alt={feature.title}
                  className="h-auto w-56 rounded-3xl md:w-64"
                  priority={index === 0}
                />
              </div>

              {/* 설명 영역 */}
              <div className="flex flex-col gap-4 text-center md:text-left">
                <h2 className="text-3xl font-bold">{feature.title}</h2>
                <p className="text-lg text-gray-600 dark:text-zinc-400">
                  {feature.description}
                </p>
              </div>
            </section>
          </Reveal>
        ))}
      </div>
    </div>
  );
}
