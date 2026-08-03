import { defineConfig } from 'vite'
import react, { reactCompilerPreset } from '@vitejs/plugin-react'
import babel from '@rolldown/plugin-babel'

// https://vite.dev/config/
export default defineConfig(({ mode }) => ({
  plugins: [
    react(),
    babel({ presets: [reactCompilerPreset()] })
  ],
  base: '/',
    server: {
      port: 3003,
      strictPort: true,
      open: false,
  },
  // react-draggable(react-grid-layout 내부 의존성)이 process.env.NODE_ENV를 직접 참조하는데,
  // Vite는 Webpack과 달리 Node의 process 전역을 자동으로 폴리필해주지 않아 "process is not defined"로 죽는다.
  define: {
    'process.env.NODE_ENV': JSON.stringify(mode),
  },
  resolve: {
    alias: {
      '@': '/src',
    },
  },
}))
