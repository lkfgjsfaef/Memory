// Mock data for the Memory application

export const loveStartDate = '2018-10-19'

export const quickAccessItems = [
  { icon: '📷', label: '日常记录', subtitle: '吃、喝、玩、睡', path: '/daily' },
  { icon: '💕', label: '恋爱日历', subtitle: '重要的日子都帮你记下来', path: '/calendar' },
  { icon: '✈️', label: '心愿清单', subtitle: '你的梦想，我陪你实现', path: '/wishlist' },
  { icon: '🖼️', label: '珍藏回忆', subtitle: '好好珍藏那些独一无二的瞬间', path: '/memories' }
]

export const importantDates = [
  { icon: '💚', title: '端午节', date: '2026-06-19', note: '农历五月初五', daysLeft: 30 },
  { icon: '🎂', title: '他的生日', date: '2026-07-06', note: '', daysLeft: 47 },
  { icon: '🎋', title: '七夕节', date: '2026-08-19', note: '', daysLeft: 91 },
  { icon: '🌕', title: '中秋节', date: '2026-09-24', note: '', daysLeft: 128 },
  { icon: '🇨🇳', title: '国庆节', date: '2026-10-01', note: '', daysLeft: 134 },
  { icon: '💕', title: '相恋纪念日', date: '2026-10-19', note: '', daysLeft: 152 }
]

export const dailyRecords = [
  {
    id: 1,
    date: '2026-05-20',
    year: 2026,
    month: 5,
    author: '酱酱',
    location: '深圳',
    title: '网站亮相日！',
    content: '等着向田猪猪展示网站成果，结果她可恶的一批，一直说没空、没空。\n\n哎，一片真心照明月，奈何明月照沟渠。',
    mood: '难过',
    moodIcon: '😢'
  },
  {
    id: 2,
    date: '2026-05-15',
    year: 2026,
    month: 5,
    author: '菲菲',
    location: '北京',
    title: '一起去天坛',
    content: '今天天气超好，和酱酱一起去了天坛公园，阳光暖暖的，拍了超多好看的照片！',
    mood: '开心',
    moodIcon: '😊'
  },
  {
    id: 3,
    date: '2026-05-01',
    year: 2026,
    month: 5,
    author: '酱酱',
    location: '北京',
    title: '五一快乐',
    content: '劳动节放假啦！终于可以好好休息一下，计划明天去逛街买礼物。',
    mood: '兴奋',
    moodIcon: '🎉'
  }
]

export const wishes = [
  {
    id: 1,
    category: '未来规划',
    title: '结束异地',
    description: '期待朝朝暮暮，暮暮朝朝',
    startDate: '2026-05-18',
    status: 'pending',
    author: '酱酱'
  },
  {
    id: 2,
    category: '旅行计划',
    title: '一起去大理',
    description: '苍山洱海，风花雪月',
    startDate: '2026-04-10',
    status: 'completed',
    author: '菲菲'
  }
]

export const memoryAlbums = [
  { id: 1, location: '北京', date: '2026-05-19', emoji: '🏛️', isPrivate: true },
  { id: 2, location: '日常生活', date: '2026-05-18', emoji: '🧸', isPrivate: true },
  { id: 3, location: '南昌', date: '2025-10-01', emoji: '🗼', isPrivate: true },
  { id: 4, location: '厦门', date: '2025-05-11', emoji: '🌊', isPrivate: true },
  { id: 5, location: '香港', date: '2025-05-01', emoji: '🍦', isPrivate: true },
  { id: 6, location: '保定', date: '2025-04-13', emoji: '🍲', isPrivate: true },
  { id: 7, location: '杭州', date: '2024-05-22', emoji: '🌹', isPrivate: true }
]

export const timelineMemories = [
  {
    year: 2026,
    month: 5,
    label: '2026年5月',
    moments: [
      {
        id: 1,
        title: '滔滔江水～悠悠大运河旁～',
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
        title: '古塔前的约定',
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
  { name: '香港', province: '广东', date: '2025-05-01', x: 68, y: 65 },
  { name: '保定', province: '河北', date: '2025-04-13', x: 66, y: 30 },
  { name: '杭州', province: '浙江', date: '2024-05-22', x: 73, y: 48 }
]

export const calendarImportantDays = [
  { icon: '💕', title: '相恋纪念日', date: '每年10月19日' },
  { icon: '🎂', title: '他的生日', date: '农历五月廿二' },
  { icon: '🎂', title: '她的生日', date: '农历腊月十九' },
  { icon: '📜', title: '传统法定节假日', date: '' },
  { icon: '⭐', title: '点击日历格子可添加自定义标记', date: '（点击标记可修改或删除）' }
]

export const monthNotes = [
  { date: '05-20', text: '网站就要亮相啦', icon: '🌟' }
]
