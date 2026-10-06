/// <reference types="vitest/config" />
import path from 'node:path'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// In dev, /api is proxied to the backend so the browser sees a single origin
// (same shape as the production reverse proxy: / -> frontend, /api -> backend).
const backend = process.env.DEV_API_PROXY_TARGET ?? 'http://localhost:8080'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: { alias: { '@': path.resolve(import.meta.dirname, 'src') } },
  server: { port: 5173, proxy: { '/api': { target: backend, changeOrigin: true } } },
  test: { environment: 'jsdom', globals: true, setupFiles: ['src/test/setup.ts'] },
})
