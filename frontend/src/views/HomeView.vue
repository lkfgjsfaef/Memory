<template>
  <div class="home-view">
    <!-- Hero Section -->
    <div class="hero-section card">
      <div class="hero-label">修炼爱情已经</div>
      <div class="hero-days">
        <span class="days-number">{{ loveDays }}</span>
        <span class="days-unit">天</span>
      </div>
      <div class="hero-since">从 {{ coupleInfo?.couple?.loveStartDate || '2026-03-16' }} 起…</div>
      <div class="hero-avatars">
        <div class="avatar-wrapper" :class="{ 'is-self': userStore.state.userId === 1 }">
          <div class="avatar avatar-him" @click="handleAvatarClick(1)">
            <img v-if="hisAvatar" :src="hisAvatar" class="avatar-img" />
            <span v-else>👦</span>
            <div class="avatar-overlay" v-if="userStore.state.userId === 1">📷</div>
          </div>
          <div class="avatar-label">{{ coupleInfo?.couple?.hisName || '他' }}</div>
        </div>
        <div class="heartbeat-line">
          <svg viewBox="0 0 200 40" class="heartbeat-svg">
            <polyline
              points="0,20 30,20 40,5 50,35 60,15 70,25 80,20 100,20 110,5 120,35 130,15 140,25 150,20 200,20"
              fill="none" stroke="#FF4D4D" stroke-width="2"
            />
          </svg>
        </div>
        <div class="avatar-wrapper" :class="{ 'is-self': userStore.state.userId === 2 }">
          <div class="avatar avatar-her" @click="handleAvatarClick(2)">
            <img v-if="herAvatar" :src="herAvatar" class="avatar-img" />
            <span v-else>👧</span>
            <div class="avatar-overlay" v-if="userStore.state.userId === 2">📷</div>
          </div>
          <div class="avatar-label">{{ coupleInfo?.couple?.herName || '她' }}</div>
        </div>
      </div>
    </div>

    <div class="home-grid">
      <!-- Quick Access -->
      <div class="section-card card">
        <h3 class="section-title">⭐ 快速入口</h3>
        <div class="quick-grid">
          <router-link v-for="item in quickAccessItems" :key="item.label" :to="item.path" class="quick-item">
            <div class="quick-icon">{{ item.icon }}</div>
            <div class="quick-label">{{ item.label }}</div>
            <div class="quick-subtitle">{{ item.subtitle }}</div>
          </router-link>
        </div>
      </div>

      <!-- Important Dates -->
      <div class="section-card card">
        <div class="section-title-row">
          <h3 class="section-title">📅 重要日子</h3>
          <button class="btn-outline-sm" @click="openDateCreate">+ 添加</button>
        </div>
        <div v-if="loading" class="loading-spinner">加载中...</div>
        <div v-else class="dates-list">
          <div v-for="date in importantDates" :key="date.id || date.title" class="date-item">
            <div class="date-icon">{{ date.icon }}</div>
            <div class="date-info">
              <div class="date-title">{{ date.title }}</div>
              <div class="date-date">{{ date.date }}</div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: Math.min(100, Math.max(0, (1 - (date.daysLeft || 0) / 200)) * 100) + '%' }"></div>
              </div>
            </div>
            <div class="date-countdown">
              <span v-if="date.daysLeft > 0">还有 {{ date.daysLeft }} 天</span>
              <span v-else-if="date.daysLeft === 0" class="today">就是今天!</span>
              <span v-else class="passed">已过</span>
            </div>
            <div class="date-actions">
              <button class="action-btn" @click="openDateEdit(date)">✏️</button>
              <button class="action-btn" @click="handleDeleteDate(date.id)">🗑️</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Dynamic Calendar -->
      <div class="section-card card">
        <h3 class="section-title">🗓️ 恋爱日历</h3>
        <div class="mini-calendar">
          <div class="cal-header">
            <button class="cal-nav" @click="prevCalMonth">◀</button>
            <span class="cal-month">{{ calYear }}年 {{ calMonth }}月</span>
            <button class="cal-nav" @click="nextCalMonth">▶</button>
          </div>
          <div class="cal-weekdays">
            <span v-for="d in ['日','一','二','三','四','五','六']" :key="d">{{ d }}</span>
          </div>
          <div class="cal-days">
            <span
              v-for="day in homeCalendarDays"
              :key="day.key"
              class="cal-day"
              :class="{
                'other-month': !day.current,
                'has-event': day.hasEvent,
                'is-today': day.isToday,
                'selected': selectedCalDate === day.dateStr
              }"
              @click="handleCalDayClick(day)"
            >{{ day.day }}</span>
          </div>
        </div>
        <!-- Quick Date Note Popup -->
        <div v-if="calPopupVisible" class="cal-popup">
          <div class="popup-date">{{ selectedCalDate }}</div>
          <div v-if="calEvents.length" class="popup-events">
            <div v-for="ev in calEvents" :key="ev.id" class="popup-event">
              <span>{{ ev.icon }}</span>
              <span>{{ ev.title || ev.text }}</span>
            </div>
          </div>
          <div class="popup-actions">
            <button class="btn-outline-sm" @click="openCalNoteAdd">+ 添加标记</button>
            <button class="btn-outline-sm" @click="calPopupVisible = false">关闭</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom cards -->
    <div class="bottom-cards">
      <div class="bottom-card card card-blue animate-fade-in">
        <div class="bottom-icon">🏔️</div>
        <div class="bottom-text">和你一起看过的风景，都是最美的画面</div>
      </div>
      <div class="bottom-card card card-pink animate-fade-in">
        <div class="bottom-icon">🧧</div>
        <div class="bottom-text">每一个心愿都是我们共同的期待</div>
      </div>
    </div>

    <!-- Important Date Modal -->
    <Modal :visible="showDateModal" :title="dateModalTitle" @close="showDateModal = false" @confirm="handleSaveDate">
      <label>标题</label>
      <input v-model="dateForm.title" placeholder="日子标题" />
      <div class="form-row">
        <div><label>图标</label>
          <select v-model="dateForm.icon">
            <option v-for="e in emojis" :key="e" :value="e">{{ e }}</option>
          </select>
        </div>
        <div><label>日期</label><input type="date" v-model="dateForm.eventDate" /></div>
      </div>
      <div class="form-row">
        <div><label>农历日期</label><input v-model="dateForm.lunarDate" placeholder="如: 农历五月初五" /></div>
        <div><label>备注</label><input v-model="dateForm.note" placeholder="备注" /></div>
      </div>
      <label class="checkbox-label">
        <input type="checkbox" v-model="dateForm.recurring" :true-value="1" :false-value="0" /> 每年重复
      </label>
    </Modal>

    <!-- Calendar Note Modal -->
    <Modal :visible="showCalNoteModal" title="添加日历标记" @close="showCalNoteModal = false" @confirm="handleSaveCalNote">
      <label>日期</label>
      <input :value="selectedCalDate" disabled />
      <label>内容</label>
      <input v-model="calNoteForm.text" placeholder="标记内容" />
      <label>图标</label>
      <select v-model="calNoteForm.icon">
        <option v-for="e in calEmojis" :key="e" :value="e">{{ e }}</option>
      </select>
    </Modal>

    <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="onAvatarFileChange" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import Modal from '../components/Modal.vue'
import { useToast } from '../composables/useToast.js'
import { useUpload } from '../composables/useUpload.js'
import { useUserStore } from '../stores/userStore.js'
import { quickAccessItems } from '../data/mock.js'
import { getCouple, getImportantDates, createImportantDate, updateImportantDate, deleteImportantDate } from '../data/api.js'
import { getCalendarNotes, createCalendarNote } from '../data/api.js'
import { getCurrentUser, getUserById, updateAvatar } from '../data/api.js'

const { toast } = useToast()
const { uploading, uploadImage } = useUpload()
const userStore = useUserStore()

const loading = ref(true)
const loveDays = ref(0)
const importantDates = ref([])
const coupleInfo = ref(null)
const hisAvatar = ref('')
const herAvatar = ref('')

// Date modal
const showDateModal = ref(false)
const dateModalTitle = ref('添加重要日子')
const editingDateId = ref(null)
const saving = ref(false)
const emojis = ['💚','🎂','🎋','🌕','🇨🇳','💕','🎉','📅','🌟','🎁','💍','🎊']
const calEmojis = ['📌','🌟','💕','🎉','🍰','🎂','💐','🌸','🎵','📝']

const emptyDateForm = () => ({ title: '', icon: '📅', eventDate: '', lunarDate: '', note: '', recurring: 1 })
const dateForm = ref(emptyDateForm())

// Calendar
const today = new Date()
const calYear = ref(today.getFullYear())
const calMonth = ref(today.getMonth() + 1)
const calNotes = ref([])
const selectedCalDate = ref('')
const calPopupVisible = ref(false)

// Calendar note modal
const showCalNoteModal = ref(false)
const calNoteForm = ref({ text: '', icon: '📌' })

// Avatar input
const avatarInput = ref(null)
const avatarTargetUserId = ref(null)

async function loadData() {
  loading.value = true
  try {
    const [coupleData, datesData] = await Promise.all([getCouple(), getImportantDates()])
    if (coupleData) {
      coupleInfo.value = coupleData
      loveDays.value = coupleData.loveDays || 0
    }
    if (datesData) importantDates.value = datesData
    await Promise.all([loadUserAvatars(), loadCalNotes()])
  } catch (e) {
    loveDays.value = 2770
    importantDates.value = []
  } finally { loading.value = false }
}

async function loadUserAvatars() {
  try {
    const me = await getCurrentUser()
    if (me) {
      if (me.id === 1) hisAvatar.value = me.avatarUrl || ''
      else herAvatar.value = me.avatarUrl || ''
    }
    const partnerId = userStore.state.userId === 1 ? 2 : 1
    const partner = await getUserById(partnerId).catch(() => null)
    if (partner) {
      if (partnerId === 1) hisAvatar.value = partner.avatarUrl || ''
      else herAvatar.value = partner.avatarUrl || ''
    }
  } catch (e) { /* ignore */ }
}

async function loadCalNotes() {
  try {
    const notes = await getCalendarNotes(calYear.value, calMonth.value) || []
    calNotes.value = notes
  } catch (e) { calNotes.value = [] }
}

const homeCalendarDays = computed(() => {
  const firstDay = new Date(calYear.value, calMonth.value - 1, 1).getDay()
  const daysInMonth = new Date(calYear.value, calMonth.value, 0).getDate()
  const prevMonthDays = new Date(calYear.value, calMonth.value - 1, 0).getDate()
  const days = []
  const todayStr = `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-${String(today.getDate()).padStart(2,'0')}`

  for (let i = firstDay - 1; i >= 0; i--) {
    days.push({ key: 'prev-' + i, day: prevMonthDays - i, current: false, hasEvent: false, isToday: false, dateStr: '' })
  }
  for (let i = 1; i <= daysInMonth; i++) {
    const dateStr = `${calYear.value}-${String(calMonth.value).padStart(2,'0')}-${String(i).padStart(2,'0')}`
    const hasNote = calNotes.value.some(n => {
      const d = n.noteDate || n.date
      return d === dateStr
    })
    const hasImportant = importantDates.value.some(d => {
      const ed = d.eventDate || d.date
      return ed === dateStr
    })
    days.push({
      key: 'cur-' + i, day: i, current: true,
      hasEvent: hasNote || hasImportant,
      isToday: dateStr === todayStr,
      dateStr
    })
  }
  while (days.length < 42) {
    const i = days.length - 35
    days.push({ key: 'next-' + i, day: i, current: false, hasEvent: false, isToday: false, dateStr: '' })
  }
  return days
})

const calEvents = computed(() => {
  const notes = calNotes.value.filter(n => (n.noteDate || n.date) === selectedCalDate.value)
  const important = importantDates.value.filter(d => (d.eventDate || d.date) === selectedCalDate.value)
  return [...important.map(d => ({ id: d.id, icon: d.icon, title: d.title })), ...notes.map(n => ({ id: n.id, icon: n.icon, title: n.text }))]
})

function prevCalMonth() {
  if (calMonth.value === 1) { calMonth.value = 12; calYear.value-- }
  else calMonth.value--
  selectedCalDate.value = ''
  calPopupVisible.value = false
  loadCalNotes()
}

function nextCalMonth() {
  if (calMonth.value === 12) { calMonth.value = 1; calYear.value++ }
  else calMonth.value++
  selectedCalDate.value = ''
  calPopupVisible.value = false
  loadCalNotes()
}

function handleCalDayClick(day) {
  if (!day.current) return
  selectedCalDate.value = day.dateStr
  calPopupVisible.value = true
}

function openCalNoteAdd() {
  showCalNoteModal.value = true
  calNoteForm.value = { text: '', icon: '📌' }
}

async function handleSaveCalNote() {
  if (saving.value) return; saving.value = true
  try {
    await createCalendarNote({
      noteDate: selectedCalDate.value,
      text: calNoteForm.value.text,
      icon: calNoteForm.value.icon,
      year: calYear.value,
      month: calMonth.value
    })
    toast.success('标记成功')
    showCalNoteModal.value = false
    await loadCalNotes()
  } catch (e) { toast.error('保存失败') } finally { saving.value = false }
}

function openDateCreate() {
  editingDateId.value = null; dateForm.value = emptyDateForm()
  dateModalTitle.value = '添加重要日子'; showDateModal.value = true
}

function openDateEdit(date) {
  editingDateId.value = date.id
  dateForm.value = {
    title: date.title || '', icon: date.icon || '📅',
    eventDate: date.date || date.eventDate || '', lunarDate: date.lunarDate || '',
    note: date.note || '', recurring: date.recurring || 1
  }
  dateModalTitle.value = '编辑重要日子'; showDateModal.value = true
}

async function handleSaveDate() {
  if (saving.value) return; saving.value = true
  try {
    if (editingDateId.value) await updateImportantDate(editingDateId.value, dateForm.value)
    else await createImportantDate(dateForm.value)
    toast.success(editingDateId.value ? '修改成功' : '添加成功')
    showDateModal.value = false; loadData()
  } catch (e) { toast.error('保存失败') } finally { saving.value = false }
}

async function handleDeleteDate(id) {
  if (!window.confirm('确定删除？')) return
  try { await deleteImportantDate(id); toast.success('删除成功'); loadData() }
  catch (e) { toast.error('删除失败') }
}

// Avatar upload
function handleAvatarClick(userId) {
  if (userStore.state.userId !== userId) {
    toast.info('只能更换自己的头像哦~')
    return
  }
  avatarTargetUserId.value = userId
  avatarInput.value.click()
}

async function onAvatarFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  try {
    const url = await uploadImage(file)
    await updateAvatar(url)
    userStore.setAvatar(url)
    if (avatarTargetUserId.value === 1) hisAvatar.value = url
    else herAvatar.value = url
    toast.success('头像更新成功')
  } catch (e) { toast.error('上传失败') }
  avatarInput.value.value = ''
}

onMounted(() => { loadData() })
</script>

<style scoped>
.home-view { max-width: 1100px; margin: 0 auto; }

/* Hero */
.hero-section { text-align: center; padding: 36px 20px 32px; margin-bottom: 20px; background: linear-gradient(135deg, #FFFDF7 0%, #FFF5F0 50%, #FFF5F9 100%); border-radius: 24px; }
.hero-label { font-size: 14px; color: #c9a87c; margin-bottom: 8px; letter-spacing: 2px; }
.hero-days { margin-bottom: 6px; }
.days-number { font-size: 80px; font-weight: 800; background: linear-gradient(135deg, #FF8C42, #FF6B6B); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; line-height: 1; }
.days-unit { font-size: 24px; color: #c9a87c; margin-left: 6px; }
.hero-since { font-size: 13px; color: #c0a880; margin-bottom: 28px; }
.hero-avatars { display: flex; align-items: center; justify-content: center; gap: 20px; }
.avatar-wrapper { text-align: center; }
.avatar { width: 76px; height: 76px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 38px; margin-bottom: 6px; position: relative; cursor: pointer; transition: transform 0.3s; overflow: hidden; }
.avatar:hover { transform: scale(1.05); }
.avatar-him { background: linear-gradient(135deg, #FFF5D9, #FFE8B0); border: 3px solid #FFD700; box-shadow: 0 4px 16px rgba(255,215,0,0.2); }
.avatar-her { background: linear-gradient(135deg, #F9E7F1, #FFD6E8); border: 3px solid #FFB6C1; box-shadow: 0 4px 16px rgba(255,182,193,0.2); }
.avatar-img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; }
.avatar-overlay { position: absolute; bottom: 0; width: 100%; background: rgba(0,0,0,0.3); color: white; font-size: 12px; text-align: center; padding: 3px 0; opacity: 0; transition: opacity 0.3s; }
.is-self .avatar:hover .avatar-overlay { opacity: 1; }
.avatar-label { font-size: 14px; color: #8B7355; font-weight: 500; }
.heartbeat-line { width: 80px; margin-bottom: 20px; }
.heartbeat-svg { width: 100%; height: 30px; }

/* Grid */
.home-grid { display: grid; grid-template-columns: 1fr 1.5fr 1fr; gap: 16px; margin-bottom: 20px; }
.section-card { padding: 20px; border-radius: 20px; }
.section-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.btn-outline-sm { padding: 5px 12px; border-radius: 14px; border: 1.5px solid #e0d5c5; background: white; font-size: 12px; cursor: pointer; color: #8B7355; transition: all 0.2s; }
.btn-outline-sm:hover { background: #FFF5EC; border-color: #FFB6C1; }
.loading-spinner { text-align: center; padding: 24px; color: #c9a87c; font-size: 14px; }

/* Quick Access */
.quick-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.quick-item { display: flex; flex-direction: column; align-items: center; gap: 6px; padding: 16px 10px; border-radius: 16px; background: linear-gradient(135deg, #FFF9F0, #FFF5EC); border: 1.5px solid #f0e6d8; transition: all 0.2s; text-decoration: none; color: inherit; }
.quick-item:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(255,182,193,0.15); border-color: #FFD6C0; }
.quick-icon { font-size: 28px; }
.quick-label { font-size: 13px; font-weight: 600; color: #6B5B4E; }
.quick-subtitle { font-size: 11px; color: #c0a880; text-align: center; }

/* Important Dates */
.dates-list { display: flex; flex-direction: column; gap: 12px; max-height: 380px; overflow-y: auto; }
.date-item { display: flex; align-items: center; gap: 12px; padding: 8px 10px; border-radius: 14px; transition: background 0.2s; }
.date-item:hover { background: #FFFAF5; }
.date-icon { font-size: 22px; width: 38px; height: 38px; border-radius: 50%; background: linear-gradient(135deg, #FFF5E6, #FFE8D0); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.date-info { flex: 1; min-width: 0; }
.date-title { font-size: 13px; font-weight: 600; color: #5B4B3E; }
.date-date { font-size: 11px; color: #c0a880; margin-bottom: 4px; }
.progress-bar { height: 5px; background: #f0e6d8; border-radius: 3px; overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #FFB6C1, #FFA500); border-radius: 3px; transition: width 0.5s ease; }
.date-countdown { font-size: 12px; color: #E88D2E; white-space: nowrap; flex-shrink: 0; }
.date-countdown .today { color: #4CAF50; font-weight: 700; }
.date-countdown .passed { color: #ccc; }
.date-actions { display: flex; gap: 2px; flex-shrink: 0; }
.action-btn { background: none; border: none; font-size: 14px; cursor: pointer; padding: 3px; border-radius: 6px; transition: background 0.2s; }
.action-btn:hover { background: #f5f0e8; }

/* Calendar */
.mini-calendar { padding: 0; }
.cal-header { display: flex; align-items: center; justify-content: center; gap: 16px; margin-bottom: 12px; }
.cal-nav { background: none; border: 1.5px solid #e8e0d8; border-radius: 50%; width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; cursor: pointer; font-size: 11px; color: #999; transition: all 0.2s; }
.cal-nav:hover { border-color: #FFB6C1; color: #FFB6C1; background: #FFF5F7; }
.cal-month { font-size: 15px; font-weight: 600; color: #E88D2E; }
.cal-weekdays { display: grid; grid-template-columns: repeat(7, 1fr); text-align: center; font-size: 11px; color: #ccc; margin-bottom: 4px; }
.cal-days { display: grid; grid-template-columns: repeat(7, 1fr); text-align: center; gap: 2px; }
.cal-day { padding: 5px 0; font-size: 12px; border-radius: 8px; cursor: pointer; transition: all 0.2s; color: #7B6B5E; }
.cal-day.other-month { color: #e8e0d8; cursor: default; }
.cal-day.has-event { font-weight: 700; color: #E88D2E; position: relative; }
.cal-day.has-event::after { content: ''; position: absolute; bottom: 2px; left: 50%; transform: translateX(-50%); width: 4px; height: 4px; border-radius: 50%; background: #FFB6C1; }
.cal-day.is-today { background: #FFF5D9; color: #E88D2E; font-weight: 800; box-shadow: 0 0 0 2px #FFE0A0; }
.cal-day:not(.other-month):hover { background: #FFF5EC; }
.cal-day.selected { background: #FFE8D0; color: #E88D2E; }
.cal-popup { margin-top: 12px; padding: 12px; background: #FFFDF9; border-radius: 14px; border: 1.5px solid #f0e6d8; }
.popup-date { font-size: 13px; font-weight: 600; color: #8B7355; margin-bottom: 8px; }
.popup-events { display: flex; flex-direction: column; gap: 6px; margin-bottom: 10px; }
.popup-event { font-size: 13px; color: #6B5B4E; display: flex; align-items: center; gap: 6px; }
.popup-actions { display: flex; gap: 8px; }

/* Bottom */
.bottom-cards { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.bottom-card { text-align: center; padding: 28px 20px; border-radius: 20px; }
.card-blue { background: linear-gradient(135deg, #E8F4FD, #D4ECFF); }
.card-pink { background: linear-gradient(135deg, #FDE8F0, #FFD6E8); }
.bottom-icon { font-size: 36px; margin-bottom: 10px; }
.bottom-text { font-size: 14px; color: #8B7B8B; }

.checkbox-label { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #999; cursor: pointer; }
.checkbox-label input { width: auto !important; margin-bottom: 0 !important; }

@media (max-width: 900px) { .home-grid { grid-template-columns: 1fr; } .bottom-cards { grid-template-columns: 1fr; } }
</style>
