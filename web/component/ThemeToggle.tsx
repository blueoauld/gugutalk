"use client";

export default function ThemeToggle() {
  const toggle = () => {
    const next = !document.documentElement.classList.contains("dark");
    document.documentElement.classList.toggle("dark", next);
    localStorage.setItem("theme", next ? "dark" : "light");
  };

  return (
    <button
      type="button"
      onClick={toggle}
      aria-label="다크 모드 전환"
      className="flex h-10 w-10 items-center justify-center rounded-xl text-zinc-700 outline-none transition-colors hover:bg-zinc-200/60 focus:outline-none dark:text-zinc-200 dark:hover:bg-zinc-800/60"
    >
      {/* 라이트 모드: 달 아이콘 (클릭 시 다크로) */}
      <svg
        viewBox="0 0 24 24"
        className="h-5 w-5 fill-current dark:hidden"
        aria-hidden="true"
      >
        <path d="M21.752 15.002A9.718 9.718 0 0 1 18 15.75c-5.385 0-9.75-4.365-9.75-9.75 0-1.33.266-2.597.748-3.752A9.753 9.753 0 0 0 3 11.25C3 16.635 7.365 21 12.75 21a9.753 9.753 0 0 0 9.002-5.998Z" />
      </svg>

      {/* 다크 모드: 해 아이콘 (클릭 시 라이트로) */}
      <svg
        viewBox="0 0 24 24"
        className="hidden h-5 w-5 fill-current dark:block"
        aria-hidden="true"
      >
        <path d="M12 2.25a.75.75 0 0 1 .75.75v1.5a.75.75 0 0 1-1.5 0V3a.75.75 0 0 1 .75-.75Zm0 14.25a4.5 4.5 0 1 0 0-9 4.5 4.5 0 0 0 0 9Zm0 1.5a.75.75 0 0 1 .75.75v1.5a.75.75 0 0 1-1.5 0v-1.5a.75.75 0 0 1 .75-.75ZM21.75 12a.75.75 0 0 1-.75.75h-1.5a.75.75 0 0 1 0-1.5H21a.75.75 0 0 1 .75.75ZM4.5 12a.75.75 0 0 1-.75.75h-1.5a.75.75 0 0 1 0-1.5h1.5A.75.75 0 0 1 4.5 12Zm14.197-6.697a.75.75 0 0 1 0 1.06l-1.06 1.061a.75.75 0 1 1-1.061-1.06l1.06-1.061a.75.75 0 0 1 1.061 0ZM7.424 16.576a.75.75 0 0 1 0 1.06l-1.06 1.061a.75.75 0 0 1-1.061-1.06l1.06-1.061a.75.75 0 0 1 1.061 0Zm11.273 2.121a.75.75 0 0 1-1.06 0l-1.061-1.06a.75.75 0 1 1 1.06-1.061l1.061 1.06a.75.75 0 0 1 0 1.061ZM7.424 7.424a.75.75 0 0 1-1.06 0L5.302 6.363a.75.75 0 0 1 1.06-1.06l1.061 1.06a.75.75 0 0 1 0 1.06Z" />
      </svg>
    </button>
  );
}
