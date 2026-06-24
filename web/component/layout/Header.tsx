import ThemeToggle from "@/component/ThemeToggle";
import logo from "@/public/logo.png";
import localFont from "next/font/local";
import Image from "next/image";
import Link from "next/link";

const paperlogy = localFont({
  src: "../../app/fonts/Paperlogy-7Bold.woff2",
  display: "swap",
  weight: "700",
});

const APP_STORE_URL =
  "https://apps.apple.com/us/app/%EA%B5%AC%EA%B5%AC%ED%86%A1/id6778419443";

export default function Header() {
  return (
    <header className="sticky top-0 z-50">
      <div className="mx-auto max-w-7xl flex items-center p-3 gap-2 bg-white/70 backdrop-blur-xs dark:bg-zinc-900/70">
        <Link href="/" className="flex items-center gap-2">
          <Image
            src={logo}
            alt="구구톡 로고"
            className="h-9 w-9 rounded-md"
            priority
          />
          <span className={`${paperlogy.className} text-2xl`}>구구톡</span>
        </Link>

        <div className="ml-auto flex items-center gap-2">
          <ThemeToggle />

          <Link
            href={APP_STORE_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 rounded-xl bg-black px-4 py-2 text-white outline-none transition-opacity hover:opacity-80 focus:outline-none dark:bg-white dark:text-black"
          >
          <svg
            viewBox="0 0 384 512"
            className="h-7 w-7 fill-current"
            aria-hidden="true"
          >
            <path d="M318.7 268.7c-.2-36.7 16.4-64.4 50-84.8-18.8-26.9-47.2-41.7-84.7-44.6-35.5-2.8-74.3 20.7-88.5 20.7-15 0-49.4-19.7-76.4-19.7C63.3 141.2 4 184.8 4 273.5q0 39.3 14.4 81.2c12.8 36.7 59 126.7 107.2 125.2 25.2-.6 43-17.9 75.8-17.9 31.8 0 48.3 17.9 76.4 17.9 48.6-.7 90.4-82.5 102.6-119.3-65.2-30.7-61.7-90-61.7-91.9zm-56.6-164.2c27.3-32.4 24.8-61.9 24-72.5-24.1 1.4-52 16.4-67.9 34.9-17.5 19.8-27.8 44.3-25.6 71.9 26.1 2 49.9-11.4 69.5-34.3z" />
          </svg>
            <span className="flex flex-col items-start leading-none">
              <span className="text-[10px]">Download on the</span>
              <span className="text-lg font-semibold leading-tight">
                App Store
              </span>
            </span>
          </Link>
        </div>
      </div>
    </header>
  );
}
