import axios from 'axios'

export const TOKEN_KEY = 'hkt_auth_token'
export const BHAKT_KEY = 'hkt_auth_bhakt'
export const ROLE_KEY  = 'hkt_auth_role'

const axiosClient = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

axiosClient.interceptors.request.use(config => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

axiosClient.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(BHAKT_KEY)
      localStorage.removeItem(ROLE_KEY)
      // Only force a redirect for admin routes — public pages stay where they are.
      if (window.location.pathname.startsWith('/admin')) {
        window.location.href = '/admin-login'
      }
    }
    return Promise.reject(err)
  }
)

export default axiosClient
