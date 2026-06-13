import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  // react-router y react-router-dom deben resolver a una sola instancia para
  // compartir el contexto del Router (los componentes usan ambos imports).
  resolve: {
    dedupe: ['react', 'react-dom', 'react-router', 'react-router-dom'],
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.js',
    // Inline para que en el entorno de test ambos paquetes carguen el mismo build (ESM)
    // y compartan el contexto del Router (si no, useNavigate/Link ven contextos distintos).
    server: {
      deps: {
        inline: ['react-router', 'react-router-dom'],
      },
    },
  }
})
