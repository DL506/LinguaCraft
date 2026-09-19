/// <reference types="vite/client" />
// Vite 构建配置:插件、路径别名、PWA(依据任务书第 1/3/13.1 章)
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'
import VueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig({
  plugins: [
    vue(),
    // Vue DevTools 调试插件:仅在开发服务器(dev)中注入悬浮调试面板,生产构建不引入,不影响打包产物
    VueDevTools(),
    // PWA 插件:与 MSW 的 Service Worker 互斥(任务书 13.1)。
    // devOptions.enabled 保持默认 false → 开发环境只有 MSW 生效;
    // VITE_USE_MOCK 仅存在于 .env.development,生产构建中它的值为 undefined,MSW 注册代码自动跳过,由 PWA SW 工作。
    VitePWA({
      registerType: 'autoUpdate',
      // 附加预缓存资源:图标与 PWA 清单(hljs 双主题样式已随 css glob 进入预缓存)
      includeAssets: ['favicon.ico', 'pwa-192.png', 'pwa-512.png', 'manifest.webmanifest'],
      manifest: false, // 使用 public/manifest.webmanifest 静态文件(含 192/512 图标与 maskable)
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
    // 联调代理:后端运行在 8080(任务契约 Base URL = /api)。
    // 联调时在 .env.development.local 中设置 VITE_USE_MOCK=false 关闭 MSW,请求经此代理转发到后端;
    // 演示开发保持 .env.development 的 VITE_USE_MOCK=true 时,请求由 MSW 拦截,不会到达此代理。
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})