import axios from 'axios'

const api = axios.create({
  baseURL: 'http://47.95.120.193:8081/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      if (window.location.pathname !== '/login') {
        localStorage.clear()
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// Auth
export const login = (username, password) => api.post('/auth/login', { username, password }).then(r => r.data.data)
export const getCurrentUser = () => api.get('/auth/me').then(r => r.data.data)
export const getUserById = (id) => api.get(`/auth/user/${id}`).then(r => r.data.data)
export const updateAvatar = (avatarUrl) => api.put('/auth/avatar', { avatarUrl }).then(r => r.data.data)
export const getQiniuToken = () => api.get('/qiniu/upload-token').then(r => r.data.data)

// 首页
export const getCouple = () => api.get('/couple').then(r => r.data.data)
export const updateCouple = (data) => api.put('/couple', data).then(r => r.data)
export const getImportantDates = () => api.get('/important-dates').then(r => r.data.data)
export const createImportantDate = (data) => api.post('/important-dates', data).then(r => r.data)
export const updateImportantDate = (id, data) => api.put(`/important-dates/${id}`, data).then(r => r.data)
export const deleteImportantDate = (id) => api.delete(`/important-dates/${id}`).then(r => r.data)

// 日常记录
export const getDailyRecords = (year, month) => {
  const params = {}
  if (year && year !== 'all') params.year = year
  if (month && month !== 'all') params.month = month
  return api.get('/daily-records', { params }).then(r => r.data.data)
}
export const getDailyStats = () => api.get('/daily-records/stats').then(r => r.data.data)
export const createDailyRecord = (data) => api.post('/daily-records', data).then(r => r.data)
export const updateDailyRecord = (id, data) => api.put(`/daily-records/${id}`, data).then(r => r.data)
export const deleteDailyRecord = (id) => api.delete(`/daily-records/${id}`).then(r => r.data)

// 日历
export const getCalendarNotes = (year, month) =>
  api.get('/calendar/notes', { params: { year, month } }).then(r => r.data.data)
export const createCalendarNote = (data) => api.post('/calendar/notes', data).then(r => r.data)
export const updateCalendarNote = (id, data) => api.put(`/calendar/notes/${id}`, data).then(r => r.data)
export const deleteCalendarNote = (id) => api.delete(`/calendar/notes/${id}`).then(r => r.data)
export const getCalendarMoods = (year, month) =>
  api.get('/calendar/moods', { params: { year, month } }).then(r => r.data.data)
export const upsertCalendarMood = (data) => api.post('/calendar/moods', data).then(r => r.data.data)

// 心愿
export const getWishes = (status, category) => {
  const params = {}
  if (status && status !== 'all') params.status = status
  if (category && category !== 'all') params.category = category
  return api.get('/wishes', { params }).then(r => r.data.data)
}
export const getWishStats = () => api.get('/wishes/stats').then(r => r.data.data)
export const createWish = (data) => api.post('/wishes', data).then(r => r.data)
export const updateWish = (id, data) => api.put(`/wishes/${id}`, data).then(r => r.data)
export const deleteWish = (id) => api.delete(`/wishes/${id}`).then(r => r.data)

// 回忆 - 相册
export const getAlbums = () => api.get('/albums').then(r => r.data.data)
export const getAlbum = (id) => api.get(`/albums/${id}`).then(r => r.data.data)
export const createAlbum = (data) => api.post('/albums', data).then(r => r.data)
export const updateAlbum = (id, data) => api.put(`/albums/${id}`, data).then(r => r.data)
export const deleteAlbum = (id) => api.delete(`/albums/${id}`).then(r => r.data)

// 回忆 - 记忆长河
export const getMoments = () => api.get('/moments').then(r => r.data.data)
export const getMoment = (id) => api.get(`/moments/${id}`).then(r => r.data.data)
export const createMoment = (data) => api.post('/moments', data).then(r => r.data)
export const updateMoment = (id, data) => api.put(`/moments/${id}`, data).then(r => r.data)
export const deleteMoment = (id) => api.delete(`/moments/${id}`).then(r => r.data)

// 音乐搜索
export const searchMusic = (keyword) => api.get('/music/search', { params: { keyword } }).then(r => r.data.data)

// 音乐歌单（后端持久化）
export const getPlaylist = () => api.get('/music/playlist').then(r => r.data.data)
export const savePlaylist = (songs) => api.put('/music/playlist', songs).then(r => r.data)

// 回忆 - 足迹
export const getLocations = () => api.get('/locations').then(r => r.data.data)
export const createLocation = (data) => api.post('/locations', data).then(r => r.data)
export const deleteLocation = (id) => api.delete(`/locations/${id}`).then(r => r.data)
