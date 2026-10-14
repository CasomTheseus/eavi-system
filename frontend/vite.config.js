import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    open: true,
    // 配置静态文件服务，允许访问上级目录的文件
    fs: {
      allow: ['..']
    },
    // 配置API代理
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets'
  },
  // 配置别名，方便访问上级目录的文件
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@data': path.resolve(__dirname, '../')
    }
  }
})