<template>
  <div class="calendar-view">
    <div class="page-header">
      <div>
        <h2 class="page-title">📅 恋爱日历</h2>
        <p class="page-subtitle">左键点击今天记录心情，右键任意日期添加标记</p>
      </div>
    </div>

    <div class="calendar-main card">
      <div class="cal-header">
        <button class="cal-nav" @click="prevMonth">◀</button>
        <span class="cal-month">{{ currentYear }}年 {{ currentMonth }}月</span>
        <button class="cal-nav" @click="nextMonth">▶</button>
        <button class="cal-today" @click="goToday">今天</button>
      </div>
      <div class="cal-weekdays">
        <span v-for="(d, i) in ['日','一','二','三','四','五','六']" :key="d" :class="{ weekend: i === 0 || i === 6 }">{{ d }}</span>
      </div>
      <div class="cal-grid">
        <div
          v-for="day in calendarDays" :key="day.key"
          class="cal-cell"
          :class="{
            'other-month': !day.current,
            'has-note': day.hasNote,
            'has-mood': day.hasMood,
            'is-today': day.isToday,
            'weekend': day.weekend
          }"
          @click="day.current ? onCellClick(day) : null"
          @contextmenu.prevent="day.current ? onCellRightClick(day) : null"
        >
          <span class="cell-day">{{ day.day }}</span>
          <span v-if="day.moods.length > 0" class="cell-moods">
            <span v-for="m in day.moods" :key="m.userId" class="cell-mood">{{ m.moodIcon }}</span>
          </span>
        </div>
      </div>
      <div class="cal-legend">
        <span class="legend-item"><span class="legend-mood">😊</span> 今日心情（左键）</span>
        <span class="legend-item"><span class="legend-note"></span> 标记（右键）</span>
      </div>
    </div>

    <!-- Mood Picker -->
    <div v-if="showMoodPicker" class="mood-picker-overlay" @click="showMoodPicker = false">
      <div class="mood-picker card" @click.stop>
        <div class="mood-picker-title">{{ moodPickerDate }} 今天心情如何？</div>
        <div class="mood-options">
          <button
            v-for="m in moodOptions" :key="m.mood"
            :class="['mood-option', { selected: selectedMood === m.mood }]"
            @click="setMood(m)"
          >
            <span class="mood-emoji">{{ m.icon }}</span>
            <span class="mood-label">{{ m.mood }}</span>
          </button>
        </div>
        <button class="mood-close" @click="showMoodPicker = false">关闭</button>
      </div>
    </div>

    <div class="cal-sections">
      <div class="section-card card">
        <div class="section-title-row">
          <h3 class="section-title">⭐ 重要日子</h3>
          <button class="btn-outline-sm" @click="$router.push('/')">+ 管理</button>
        </div>
        <div class="important-scroll">
          <div v-for="item in importantDates" :key="item.id" class="important-item">
            <span class="important-icon">{{ item.icon }}</span>
            <div class="important-info">
              <div class="important-label">{{ item.title }}</div>
              <div class="important-date">{{ item.date || item.eventDate }}</div>
            </div>
            <div class="important-countdown">
              <span v-if="item.daysLeft > 0">还有 {{ item.daysLeft }} 天</span>
              <span v-else-if="item.daysLeft === 0" class="today-tag">就是今天!</span>
              <span v-else class="passed-tag">已过</span>
            </div>
          </div>
        </div>
      </div>

      <div class="section-card card">
        <h3 class="section-title">
          ✏️ 本月标记
        </h3>
        <div v-if="notesLoading" class="loading-mini">加载中...</div>
        <div v-else class="notes-list">
          <div v-for="note in monthNotes" :key="note.id" class="note-item">
            <span class="note-badge">{{ formatNoteDate(note.noteDate) }}</span>
            <span class="note-icon">{{ note.icon }}</span>
            <span class="note-text">{{ note.text }}</span>
            <button class="note-delete" @click.stop="handleDeleteNote(note.id)">×</button>
          </div>
          <p v-if="monthNotes.length === 0" class="no-notes">右键点击日历格子添加标记~</p>
        </div>
      </div>
    </div>

    <Modal :visible="showNoteModal" :title="noteModalTitle" @close="showNoteModal = false" @confirm="handleSaveNote">
      <label>标记内容</label><input v-model="noteForm.text" placeholder="标记内容" />
      <div class="form-row">
        <div><label>日期</label><input type="date" v-model="noteForm.noteDate" /></div>
        <div><label>图标</label><select v-model="noteForm.icon"><option v-for="e in noteIcons" :key="e" :value="e">{{ e }}</option></select></div>
      </div>
    </Modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import Modal from '../components/Modal.vue'
import { useToast } from '../composables/useToast.js'
import { useUserStore } from '../stores/userStore.js'
import { getCalendarNotes, createCalendarNote, deleteCalendarNote, getCalendarMoods, upsertCalendarMood } from '../data/api.js'
import { getImportantDates } from '../data/api.js'

const { toast } = useToast()
const userStore = useUserStore()
const today = new Date()
const currentYear = ref(today.getFullYear())
const currentMonth = ref(today.getMonth() + 1)
const monthNotes = ref([])
const monthMoods = ref([])
const notesLoading = ref(false)
const importantDates = ref([])
const showNoteModal = ref(false)
const noteModalTitle = ref('添加标记')
const editingNoteId = ref(null)
const saving = ref(false)
const noteIcons = ['📌','🌟','💕','🎂','🎉','✈️','📅','🌸','🍰']

const moodOptions = [
  { mood: '开心', icon: '😊' },
  { mood: '伤心', icon: '😢' },
  { mood: '普通', icon: '😐' },
  { mood: '生气', icon: '😠' }
]

const showMoodPicker = ref(false)
const moodPickerDate = ref('')
const selectedMood = ref('')

const emptyNoteForm = () => ({ text: '', icon: '📌', noteDate: '' })
const noteForm = ref(emptyNoteForm())

async function loadAll() {
  notesLoading.value = true
  try {
    const [notes, dates, moods] = await Promise.all([
      getCalendarNotes(currentYear.value, currentMonth.value),
      getImportantDates(),
      getCalendarMoods(currentYear.value, currentMonth.value)
    ])
    monthNotes.value = notes || []
    importantDates.value = dates || []
    monthMoods.value = moods || []
  } catch (e) { monthNotes.value = []; importantDates.value = []; monthMoods.value = [] }
  finally { notesLoading.value = false }
}

function isToday(dateStr) {
  const t = new Date()
  return dateStr === `${t.getFullYear()}-${String(t.getMonth()+1).padStart(2,'0')}-${String(t.getDate()).padStart(2,'0')}`
}

function onCellClick(day) {
  if (!day.dateStr || !isToday(day.dateStr)) return
  const myMood = day.moods.find(m => m.userId === userStore.state.userId)
  moodPickerDate.value = `${currentYear.value}年${currentMonth.value}月${day.day}日`
  selectedMood.value = myMood ? myMood.mood : ''
  showMoodPicker.value = true
}

function onCellRightClick(day) {
  editingNoteId.value = null
  noteForm.value = {
    ...emptyNoteForm(),
    noteDate: day.dateStr || `${currentYear.value}-${String(currentMonth.value).padStart(2,'0')}-${String(day.day).padStart(2,'0')}`
  }
  noteModalTitle.value = '添加标记'; showNoteModal.value = true
}

async function setMood(m) {
  const dateStr = `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-${String(today.getDate()).padStart(2,'0')}`
  try {
    await upsertCalendarMood({ moodDate: dateStr, mood: m.mood, moodIcon: m.icon })
    selectedMood.value = m.mood
    showMoodPicker.value = false
    toast.success('心情已记录')
    loadAll()
  } catch (e) { toast.error('保存心情失败') }
}

async function handleSaveNote() {
  if (saving.value) return; saving.value = true
  try {
    await createCalendarNote({ ...noteForm.value, year: currentYear.value, month: currentMonth.value })
    toast.success('添加成功')
    showNoteModal.value = false; loadAll()
  } catch (e) { toast.error('保存失败') } finally { saving.value = false }
}

async function handleDeleteNote(id) {
  if (!window.confirm('确定删除这条标记？')) return
  try { await deleteCalendarNote(id); toast.success('删除成功'); loadAll() }
  catch (e) { toast.error('删除失败') }
}

function formatNoteDate(d) { if (!d) return ''; return d.length === 5 ? d : d.substring(5) }

function prevMonth() {
  if (currentMonth.value === 1) { currentMonth.value = 12; currentYear.value-- }
  else currentMonth.value--
}
function nextMonth() {
  if (currentMonth.value === 12) { currentMonth.value = 1; currentYear.value++ }
  else currentMonth.value++
}
function goToday() {
  currentYear.value = today.getFullYear()
  currentMonth.value = today.getMonth() + 1
}

const calendarDays = computed(() => {
  const y = currentYear.value; const m = currentMonth.value
  const firstDay = new Date(y, m - 1, 1).getDay()
  const daysInMonth = new Date(y, m, 0).getDate()
  const prevDays = new Date(y, m - 1, 0).getDate()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-${String(today.getDate()).padStart(2,'0')}`
  const days = []

  for (let i = firstDay - 1; i >= 0; i--) {
    days.push({ key: 'prev-'+i, day: prevDays-i, current: false, hasNote: false, hasMood: false, moods: [], isToday: false, weekend: false })
  }

  for (let i = 1; i <= daysInMonth; i++) {
    const dateStr = `${y}-${String(m).padStart(2,'0')}-${String(i).padStart(2,'0')}`
    const dow = new Date(y, m - 1, i).getDay()
    const hasNote = monthNotes.value.some(n => (n.noteDate || n.date) === dateStr)
    const moods = monthMoods.value.filter(m => m.moodDate === dateStr)
    days.push({
      key: 'cur-'+i, day: i, current: true,
      hasNote,
      hasMood: moods.length > 0,
      moods: moods,
      moodType: moods.length > 0 ? moods[0].mood : '',
      isToday: dateStr === todayStr,
      weekend: dow === 0 || dow === 6,
      dateStr
    })
  }

  while (days.length < 42) {
    const i = days.length - 35
    days.push({ key: 'next-'+i, day: i, current: false, hasNote: false, hasMood: false, moods: [], isToday: false, weekend: false })
  }
  return days
})

watch([currentMonth, currentYear], loadAll)
onMounted(() => { loadAll() })
</script>

<style scoped>
.calendar-view { max-width: 800px; margin: 0 auto; }
.page-header { margin-bottom: 20px; }
.page-title { font-size: 22px; font-weight: 700; background: linear-gradient(135deg, #FF8C42, #FFB6C1); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.page-subtitle { font-size: 14px; color: #c9a87c; margin-top: 4px; }

.calendar-main { padding: 20px; border-radius: 20px; margin-bottom: 20px; }
.cal-header { display: flex; align-items: center; justify-content: center; gap: 16px; margin-bottom: 14px; }
.cal-month { font-size: 18px; font-weight: 700; color: #E88D2E; }
.cal-nav { width: 30px; height: 30px; border-radius: 50%; background: #faf5ed; border: 1.5px solid #e8e0d8; font-size: 12px; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s; color: #999; }
.cal-nav:hover { border-color: #FFB6C1; color: #FFB6C1; background: #FFF5F7; }
.cal-today { padding: 5px 14px; border-radius: 14px; border: 1.5px solid #e0d5c5; background: white; font-size: 12px; cursor: pointer; color: #8B7355; transition: all 0.2s; }
.cal-today:hover { border-color: #FFB6C1; background: #FFF5F7; }

.cal-weekdays { display: grid; grid-template-columns: repeat(7, 1fr); text-align: center; margin-bottom: 6px; }
.cal-weekdays span { font-size: 12px; color: #c0a880; padding: 4px 0; }
.cal-weekdays span.weekend { color: #e8c0c0; }
.cal-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 3px; }
.cal-cell { aspect-ratio: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; border-radius: 10px; cursor: pointer; transition: all 0.2s; position: relative; }
.cal-cell:hover:not(.other-month) { background: #FFF5EC; }
.cal-cell.other-month { opacity: 0.3; cursor: default; }
.cal-cell.weekend:not(.other-month) { background: #FFFDFA; }
.cal-cell.is-today { background: #FFF5D9; box-shadow: 0 0 0 2px #FFE0A0; }
.cal-cell.is-today .cell-day { color: #E88D2E; font-weight: 800; }
.cell-day { font-size: 14px; font-weight: 600; color: #6B5B4E; }

/* Mood emoji on cell */
.cell-moods { display: flex; gap: 1px; margin-top: 1px; }
.cell-mood { font-size: 13px; line-height: 1; }

/* Note cells — orange background */
.cal-cell.has-note { background: #FFF0E0; }
.cal-cell.has-note.is-today { background: #FFF5D9; box-shadow: 0 0 0 3px #FFA500; }

/* Legend */
.cal-legend { display: flex; gap: 20px; justify-content: center; margin-top: 14px; font-size: 12px; color: #c0a880; }
.legend-item { display: flex; align-items: center; gap: 4px; }
.legend-note { width: 14px; height: 14px; border-radius: 4px; background: #FFF0E0; border: 1px solid #FFD0A0; display: inline-block; }

/* Mood Picker */
.mood-picker-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); z-index: 1500; display: flex; align-items: center; justify-content: center; }
.mood-picker { padding: 24px; border-radius: 20px; text-align: center; z-index: 1501; max-width: 340px; width: 90%; }
.mood-picker-title { font-size: 16px; font-weight: 600; color: #5B4B3E; margin-bottom: 16px; }
.mood-options { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 16px; }
.mood-option { display: flex; flex-direction: column; align-items: center; gap: 4px; padding: 14px; border-radius: 14px; border: 2px solid #e8e0d8; background: white; cursor: pointer; transition: all 0.2s; }
.mood-option:hover { border-color: #FFB6C1; background: #FFF5F7; }
.mood-option.selected { border-color: #FFA500; background: #FFF5EC; }
.mood-emoji { font-size: 32px; }
.mood-label { font-size: 13px; color: #8B7355; font-weight: 500; }
.mood-close { padding: 6px 20px; border-radius: 14px; border: 1.5px solid #e0d5c5; background: white; cursor: pointer; font-size: 13px; color: #8B7355; }

/* Sections below calendar */
.cal-sections { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.section-card { padding: 20px; border-radius: 20px; }
.section-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.section-title { font-size: 16px; font-weight: 700; color: #E88D2E; margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center; }
.btn-outline-sm { padding: 4px 12px; border-radius: 14px; border: 1.5px solid #e0d5c5; background: white; font-size: 12px; cursor: pointer; color: #8B7355; transition: all 0.2s; }
.btn-outline-sm:hover { border-color: #FFB6C1; background: #FFF5F7; }

.important-scroll { display: flex; flex-direction: column; gap: 10px; max-height: 280px; overflow-y: auto; }
.important-item { display: flex; align-items: center; gap: 10px; padding: 8px; border-radius: 12px; transition: background 0.2s; }
.important-item:hover { background: #FFFAF5; }
.important-icon { font-size: 20px; width: 36px; height: 36px; border-radius: 50%; background: #FFF5EC; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.important-info { flex: 1; }
.important-label { font-size: 13px; font-weight: 600; color: #5B4B3E; }
.important-date { font-size: 12px; color: #c0a880; }
.important-countdown { font-size: 12px; color: #E88D2E; white-space: nowrap; }
.today-tag { color: #4CAF50; font-weight: 700; }
.passed-tag { color: #ccc; }

.loading-mini { text-align: center; padding: 12px; color: #c9a87c; font-size: 13px; }
.no-notes { font-size: 13px; color: #c9a87c; text-align: center; padding: 16px; }
.notes-list { display: flex; flex-direction: column; gap: 8px; max-height: 280px; overflow-y: auto; }
.note-item { display: flex; align-items: center; gap: 8px; padding: 6px 8px; border-radius: 10px; transition: background 0.2s; }
.note-item:hover { background: #FFFDF9; }
.note-badge { background: #FFE8D0; color: #E88D2E; padding: 3px 10px; border-radius: 12px; font-size: 11px; font-weight: 600; white-space: nowrap; }
.note-icon { font-size: 16px; }
.note-text { font-size: 13px; flex: 1; color: #6B5B4E; }
.note-delete { background: none; border: none; color: #ddd; font-size: 16px; cursor: pointer; padding: 0 4px; transition: color 0.2s; }
.note-delete:hover { color: #E53935; }

@media (max-width: 768px) { .cal-sections { grid-template-columns: 1fr; } }
</style>
