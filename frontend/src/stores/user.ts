// /frontend/src/stores/user.ts
// 职责描述：用户状态管理——Token、用户名、登录/登出/刷新

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { logout as logoutApi } from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('accessToken') || '')
  const refreshTokenVal = ref(localStorage.getItem('refreshToken') || '')
  const username = ref(localStorage.getItem('username') || '')

  const isLoggedIn = computed(() => !!token.value)

  function setToken(accessToken: string, refreshToken: string) {
    token.value = accessToken
    refreshTokenVal.value = refreshToken
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
  }

  function setUsername(name: string) {
    username.value = name
    localStorage.setItem('username', name)
  }

  async function logout() {
    try { await logoutApi() } catch { /* ignore */ }
    token.value = ''
    refreshTokenVal.value = ''
    username.value = ''
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('username')
  }

  return { token, refreshTokenVal, username, isLoggedIn, setToken, setUsername, logout }
})