import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // Load env vars based on mode
  const env = loadEnv(mode, process.cwd())

  const port = parseInt(env.VITE_APP_PORT) || 3000
  const apiUrl = env.VITE_APP_APIURL
  const backendUrl = env.VITE_BACKEND_URL

  console.log('PORT from env:', port)
  console.log('API URL from env:', apiUrl)
  console.log('Backend URL from env:', backendUrl)

  return {
    plugins: [
      vue(),
      vueDevTools(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
    server: {
      port,
      proxy: {
        [apiUrl]: {
          target: backendUrl,
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp(`^${apiUrl}`), '')
        }
      }
    }
  }
})
