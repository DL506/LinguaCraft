/// <reference types="vite/client" />
// Vite 构建配置:插件、路径别名、PWA(依据任务书第 1/3/13.1 章)
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    vue(),
    // PWA 插件:与 MSW 的 Service Worker 互斥(任务书 13.1)。
    // devOptions.enabled 保持默认 false → 开发环境只有 MSW 生效;
    // VITE_USE_MOCK 仅存在于 .env.development,生产构建中它的值为 undefined,MSW 注册代码自动跳过,由 PWA SW 工作。
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico'],
      manifest: false, // P13 阶段补充 manifest 图标与预缓存配置
      devOptions: { enabled: false },
    }),
  ],
  resolve: {
    alias: {
      // '@' 指向 src 目录(任务书第 3 章所有 import 均基于该别名)
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
  },
})