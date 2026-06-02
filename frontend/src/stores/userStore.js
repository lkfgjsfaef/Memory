import { reactive } from 'vue'

const state = reactive({
  token: localStorage.getItem('token') || '',
  userId: Number(localStorage.getItem('userId')) || null,
  username: localStorage.getItem('username') || '',
  nickname: localStorage.getItem('nickname') || '',
  avatarUrl: localStorage.getItem('avatarUrl') || ''
})

export function useUserStore() {
  const isLoggedIn = () => !!state.token

  function login(userData) {
    state.token = userData.token
    state.userId = userData.userId
    state.username = userData.username
    state.nickname = userData.nickname
    state.avatarUrl = userData.avatarUrl || ''
    localStorage.setItem('token', userData.token)
    localStorage.setItem('userId', userData.userId)
    localStorage.setItem('username', userData.username)
    localStorage.setItem('nickname', userData.nickname)
    localStorage.setItem('avatarUrl', userData.avatarUrl || '')
  }

  function logout() {
    state.token = ''
    state.userId = null
    state.username = ''
    state.nickname = ''
    state.avatarUrl = ''
    localStorage.clear()
  }

  function setAvatar(url) {
    state.avatarUrl = url
    localStorage.setItem('avatarUrl', url)
  }

  return { state, isLoggedIn, login, logout, setAvatar }
}
