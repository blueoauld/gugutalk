import Link from "next/link";

export default function Footer() {
  return (
    <footer className="mx-auto w-full max-w-7xl">
      <div className="flex flex-col items-center gap-3 px-3 py-6">
        <div className="flex items-center gap-4 text-sm font-medium text-zinc-600 dark:text-zinc-400">
          <Link
            href="/service"
            className="transition-colors hover:text-zinc-900 dark:hover:text-zinc-100"
          >
            서비스 이용약관
          </Link>
          <span className="h-3 w-px bg-zinc-300 dark:bg-zinc-700" />
          <Link
            href="/privacy"
            className="transition-colors hover:text-zinc-900 dark:hover:text-zinc-100"
          >
            개인정보 취급방침
          </Link>
          <span className="h-3 w-px bg-zinc-300 dark:bg-zinc-700" />
          <a
            href="mailto:cs@pidulgi.com"
            className="transition-colors hover:text-zinc-900 dark:hover:text-zinc-100"
          >
            문의하기
          </a>
        </div>
        <p className="text-xs text-zinc-400 dark:text-zinc-500">
          © {new Date().getFullYear()} pidulgi. All rights reserved.
        </p>
      </div>
    </footer>
  );
}
