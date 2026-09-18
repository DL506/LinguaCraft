/// <reference types="vite/client" />

// 自定义环境变量的类型声明(任务书 13.1:VITE_USE_MOCK 控制 MSW 启停)
// 与 vite/client 内置的 ImportMetaEnv 接口合并,获得 import.meta.env.VITE_USE_MOCK 的类型提示
interface ImportMetaEnv {
  readonly VITE_USE_MOCK?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}