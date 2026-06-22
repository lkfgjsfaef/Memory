// Mock data for development/demo purposes
// All data is fictional — replace with real API calls in production

export const loveStartDate = '2025-01-01'

export const quickAccessItems = [
  { icon: '📷', label: '日常记录', subtitle: '记录每一天的点滴', path: '/daily' },
  { icon: '💕', label: '恋爱日历', subtitle: '重要的日子都帮你记下来', path: '/calendar' },
  { icon: '✈️', label: '心愿清单', subtitle: '一起实现每一个梦想', path: '/wishlist' },
  { icon: '🖼️', label: '珍藏回忆', subtitle: '珍藏那些独一无二的瞬间', path: '/memories' }
]

export const importantDates = [
  { icon: '💚', title: '端午节', date: '2026-06-19', note: '农历五月初五', daysLeft: 30 },
  { icon: '🎂', title: '他的生日', date: '2026-07-06', note: '', daysLeft: 47 },
  { icon: '🎋', title: '七夕节', date: '2026-08-19', note: '', daysLeft: 91 },
  { icon: '🌕', title: '中秋节', date: '2026-09-24', note: '', daysLeft: 128 },
  { icon: '🇨🇳', title: '国庆节', date: '2026-10-01', note: '', daysLeft: 134 },
  { icon: '💕', title: '相恋纪念日', date: '2026-01-01', note: '', daysLeft: 193 }
]

export const dailyRecords = [
  {
    id: 1,
    date: '2026-05-20',
    year: 2026,
    month: 5,
    author: '用户A',
    location: '深圳',
    title: '阳光正好的一天',
    content: '今天天气特别好，一起出门散步，吃了好多好吃的，拍了很多照片。',
    mood: '开心',
    moodIcon: '😊'
  },
  {
    id: 2,
    date: '2026-05-15',
    year: 2026,
    month: 5,
    author: '用户B',
    location: '北京',
    title: '周末小旅行',
    content: '一起去了公园，阳光暖暖的，拍了很多好看的照片，真是个完美的周末！',
    mood: '开心',
    moodIcon: '😊'
  },
  {
    id: 3,
    date: '2026-05-01',
    year: 2026,
    month: 5,
    author: '用户A',
    location: '北京',
    title: '劳动节快乐',
    content: '放假啦！终于可以好好休息一下，计划明天去逛街买礼物。',
    mood: '兴奋',
    moodIcon: '🎉'
  }
]

export const wishes = [
  {
    id: 1,
    category: '未来规划',
    title: '一起去看海',
    description: '吹海风，看日落，捡贝壳',
    startDate: '2026-05-18',
    status: 'pending',
    author: '用户A'
  },
  {
    id: 2,
    category: '旅行计划',
    title: '一起去大理',
    description: '苍山洱海，风花雪月',
    startDate: '2026-04-10',
    status: 'completed',
    author: '用户B'
  }
]

export const memoryAlbums = [
  { id: 1, location: '北京', date: '2026-05-19', emoji: '🏛️', isPrivate: true },
  { id: 2, location: '日常生活', date: '2026-05-18', emoji: '🧸', isPrivate: true },
  { id: 3, location: '南昌', date: '2025-10-01', emoji: '🗼', isPrivate: true },
  { id: 4, location: '厦门', date: '2025-05-11', emoji: '🌊', isPrivate: true },
  { id: 5, location: '香港', date: '2025-05-01', emoji: '🍦', isPrivate: true },
  { id: 6, location: '杭州', date: '2025-04-13', emoji: '🍲', isPrivate: true }
]

export const timelineMemories = [
  {
    year: 2026,
    month: 5,
    label: '2026年5月',
    moments: [
      {
        id: 1,
        title: '初夏的午后',
        date: '2026-05-01',
        location: '北京市',
        emoji: '🏞️'
      }
    ]
  },
  {
    year: 2025,
    month: 10,
    label: '2025年10月',
    moments: [
      {
        id: 2,
        title: '秋天的约定',
        date: '2025-10-01',
        location: '南昌市',
        emoji: '🗼'
      }
    ]
  }
]

export const visitedLocations = [
  { name: '北京', province: '北京市', date: '2026-05-19', x: 68, y: 28 },
  { name: '南昌', province: '江西', date: '2025-10-01', x: 66, y: 55 },
  { name: '厦门', province: '福建', date: '2025-05-11', x: 72, y: 60 },
  { name: '杭州', province: '浙江', date: '2024-05-22', x: 73, y: 48 }
]

export const calendarImportantDays = [
  { icon: '💕', title: '相恋纪念日', date: '每年1月1日' },
  { icon: '🎂', title: '他的生日', date: '农历五月廿二' },
  { icon: '🎂', title: '她的生日', date: '农历腊月十九' },
  { icon: '📜', title: '传统法定节假日', date: '' },
  { icon: '⭐', title: '点击日历格子可添加自定义标记', date: '（点击标记可修改或删除）' }
]

export const monthNotes = [
  { date: '05-20', text: '美好的一天', icon: '🌟' }
]
