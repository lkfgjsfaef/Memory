<template>
  <div class="daily-view">
    <div class="page-header">
      <div>
        <h2 class="page-title">📔 日常记录</h2>
        <p class="page-subtitle">记录我们的甜蜜日常</p>
      </div>
      <button class="btn-primary" @click="openCreate">+ 添加记录</button>
    </div>

    <div class="stats-row">
      <StatsCard :number="stats.monthCount" label="本月记录" bgColor="#D4F0FF" />
      <StatsCard :number="stats.totalCount" label="总记录数" bgColor="#F9E7F1" />
      <StatsCard :number="stats.loveDays" label="相恋天数" bgColor="#FFF5D9" />
    </div>

    <div class="filter-bar">
      <select v-model="selectedYear" class="filter-select">
        <option value="all">全部年份</option>
        <option v-for="y in years" :key="y" :value="y">{{ y }}年</option>
      </select>
      <select v-model="selectedMonth" class="filter-select">
        <option value="all">全部月份</option>
        <option v-for="m in 12" :key="m" :value="m">{{ m }}月</option>
      </select>
    </div>

    <div v-if="loading" class="loading-spinner">加载中...</div>
    <div v-else class="timeline animate-stagger">
      <div v-for="record in filteredRecords" :key="record.id" class="timeline-item">
        <div class="timeline-marker">
          <div class="marker-dot">{{ new Date(record.recordDate).getDate() }}</div>
          <div class="marker-line"></div>
        </div>
        <div class="timeline-content">
          <div class="record-date-label">
            {{ new Date(record.recordDate).getFullYear() }}年{{ String(new Date(record.recordDate).getMonth() + 1).padStart(2, '0') }}月
          </div>
          <div class="record-card card">
            <div class="record-header">
              <div class="record-author">
                <div class="author-avatar">{{ record.userId === 2 ? '👧' : '👦' }}</div>
                <div>
                  <span class="author-name">{{ record.author }}</span>
                  <span class="record-title">{{ record.title }}</span>
                </div>
              </div>
              <div class="record-actions">
                <button class="action-btn" @click="openEdit(record)">✏️</button>
                <button class="action-btn" @click="handleDelete(record.id)">🗑️</button>
              </div>
            </div>
            <div class="record-meta">
              <span class="mood-badge" v-if="record.moodIcon">
                <span>{{ record.moodIcon }}</span>
                <span>{{ record.mood }}</span>
              </span>
              <span class="record-location" v-if="record.location">📍 {{ record.location }}</span>
            </div>
            <div class="record-body">
              <p v-for="(para, pi) in (record.content || '').split('\n\n')" :key="pi">{{ para }}</p>
            </div>
            <div v-if="record.imageUrls" class="record-images">
              <img
                v-for="(url, i) in record.imageUrls.split(',')"
                :key="i"
                :src="url"
                class="record-image-thumb"
                @click="previewImage = url"
              />
            </div>
          </div>
        </div>
      </div>
      <div v-if="!loading && filteredRecords.length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <p>暂无记录，快来写点什么吧！</p>
      </div>
    </div>

    <!-- Image Preview -->
    <div v-if="previewImage" class="image-preview-overlay" @click="previewImage = null">
      <img :src="previewImage" class="preview-img" />
    </div>

    <!-- Create/Edit Modal -->
    <Modal :visible="showModal" :title="modalTitle" @close="showModal = false" @confirm="handleSave">
      <label>标题</label><input v-model="form.title" placeholder="记录标题" />
      <label>内容</label><textarea v-model="form.content" placeholder="今天发生了什么..." rows="4"></textarea>
      <div class="form-row">
        <div><label>作者</label><input :value="form.author" disabled class="readonly-input" /></div>
        <div><label>日期</label><input type="date" v-model="form.recordDate" /></div>
      </div>
      <div class="form-row">
        <div><label>地点</label><input v-model="form.location" placeholder="地点" /></div>
        <div><label>心情</label><select v-model="form.mood"><option v-for="m in moods" :key="m" :value="m">{{ m }}</option></select></div>
        <div><label>图标</label><select v-model="form.moodIcon"><option v-for="e in moodIcons" :key="e" :value="e">{{ e }}</option></select></div>
      </div>
      <label>配图（可上传多张）</label>
      <div class="upload-area">
        <div v-if="form.imageUrls" class="upload-photos">
          <div v-for="(url, i) in form.imageUrls.split(',')" :key="i" class="photo-item">
            <img :src="url" class="preview-thumb" />
            <button class="remove-btn" @click="removeImage(i)">✕</button>
          </div>
        </div>
        <label class="upload-btn">
          <input type="file" accept="image/*" style="display:none" @change="onImageUpload" />
          <span>📷 点击上传图片</span>
        </label>
        <span v-if="uploading" class="upload-progress">上传中 {{ progress }}%</span>
      </div>
    </Modal>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import StatsCard from '../components/StatsCard.vue'
import Modal from '../components/Modal.vue'
import { useToast } from '../composables/useToast.js'
import { useUpload } from '../composables/useUpload.js'
import { useUserStore } from '../stores/userStore.js'
import { getDailyRecords, getDailyStats, createDailyRecord, updateDailyRecord, deleteDailyRecord } from '../data/api.js'

const { toast } = useToast()
const userStore = useUserStore()
const { uploading, progress, uploadImage } = useUpload()
const selectedYear = ref('all'); const selectedMonth = ref('all')
const years = [2026, 2025, 2024]
const stats = ref({ monthCount: 0, totalCount: 0, loveDays: 0 })
const filteredRecords = ref([])
const loading = ref(true)
const showModal = ref(false); const modalTitle = ref('添加记录')
const editingId = ref(null); const saving = ref(false)
const previewImage = ref(null)
const moods = ['开心','难过','兴奋','感动','平静','幸福','想他','撒娇']
const moodIcons = ['😊','😢','🎉','🥰','😌','💕','🥺','😋']

const myName = userStore.state.nickname || '用户A'
const emptyForm = () => ({ title: '', content: '', author: myName, location: '', mood: '开心', moodIcon: '😊', recordDate: new Date().toISOString().slice(0, 10), imageUrls: '' })
const form = ref(emptyForm())

function openCreate() { editingId.value = null; form.value = emptyForm(); modalTitle.value = '添加记录'; showModal.value = true }
function openEdit(r) {
  editingId.value = r.id
  form.value = { title: r.title, content: r.content || '', author: r.author, location: r.location || '', mood: r.mood || '', moodIcon: r.moodIcon || '', recordDate: r.recordDate || '', imageUrls: r.imageUrls || '' }
  modalTitle.value = '编辑记录'; showModal.value = true
}

async function onImageUpload(e) {
  const file = e.target.files[0]
  if (!file) return
  try {
    const url = await uploadImage(file)
    form.value.imageUrls = form.value.imageUrls ? form.value.imageUrls + ',' + url : url
  } catch (e) { toast.error('上传失败') }
}

function removeImage(index) {
  const urls = form.value.imageUrls.split(',').filter((_, i) => i !== index)
  form.value.imageUrls = urls.join(',')
}

async function handleSave() {
  if (saving.value) return; saving.value = true
  try {
    if (editingId.value) await updateDailyRecord(editingId.value, form.value)
    else await createDailyRecord(form.value)
    toast.success(editingId.value ? '修改成功' : '添加成功')
    showModal.value = false; loadData()
  } catch (e) { toast.error('保存失败') } finally { saving.value = false }
}

async function handleDelete(id) {
  if (!window.confirm('确定删除？')) return
  try { await deleteDailyRecord(id); toast.success('删除成功'); loadData() }
  catch (e) { toast.error('删除失败') }
}

async function loadData() {
  loading.value = true
  try {
    const [records, statsData] = await Promise.all([getDailyRecords(selectedYear.value, selectedMonth.value), getDailyStats()])
    filteredRecords.value = records || []; stats.value = statsData || { monthCount: 0, totalCount: 0, loveDays: 0 }
  } catch (e) { filteredRecords.value = [] } finally { loading.value = false }
}

watch([selectedYear, selectedMonth], loadData)
onMounted(() => { loadData() })
</script>

<style scoped>
.daily-view { max-width: 800px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.page-title { font-size: 22px; font-weight: 700; background: linear-gradient(135deg, #FF8C42, #FFB6C1); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.page-subtitle { font-size: 14px; color: #c9a87c; margin-top: 4px; }
.stats-row { display: flex; gap: 12px; margin-bottom: 20px; }
.filter-bar { display: flex; gap: 10px; margin-bottom: 28px; }
.filter-select { padding: 8px 16px; border-radius: 20px; border: 1.5px solid #e8e0d8; background: white; font-size: 13px; color: #6B5B4E; cursor: pointer; outline: none; transition: border-color 0.2s; }
.filter-select:focus { border-color: #FFB6C1; }
.loading-spinner { text-align: center; padding: 40px; color: #c9a87c; font-size: 14px; }

/* Timeline */
.timeline { position: relative; }
.timeline-item { display: flex; gap: 16px; margin-bottom: 20px; }
.timeline-marker { display: flex; flex-direction: column; align-items: center; flex-shrink: 0; }
.marker-dot { width: 38px; height: 38px; border-radius: 50%; background: linear-gradient(135deg, #FFA500, #FF8C42); color: white; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px; flex-shrink: 0; box-shadow: 0 3px 10px rgba(255,165,0,0.2); }
.marker-line { width: 2px; flex: 1; background: linear-gradient(to bottom, #FFD6C0, #f0e6d8); margin-top: 4px; }
.record-date-label { font-size: 12px; color: #c9a87c; margin-bottom: 8px; }
.record-card { padding: 20px; border-radius: 18px; background: linear-gradient(135deg, #FFFEFA, #FFFDF8); }
.record-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px; }
.record-author { display: flex; align-items: center; gap: 10px; }
.record-actions { display: flex; gap: 4px; }
.author-avatar { width: 38px; height: 38px; border-radius: 50%; background: linear-gradient(135deg, #FFF5D9, #FFE8C0); display: flex; align-items: center; justify-content: center; font-size: 20px; }
.author-name { font-weight: 600; font-size: 14px; color: #8B7355; margin-right: 8px; }
.record-title { font-weight: 700; font-size: 15px; color: #5B4B3E; }
.record-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.mood-badge { display: flex; align-items: center; gap: 4px; background: #FFF5EC; padding: 4px 10px; border-radius: 12px; font-size: 12px; color: #8B7355; }
.record-location { font-size: 12px; color: #c0a880; }
.record-body { font-size: 14px; line-height: 1.8; color: #5B4B3E; }
.record-body p { margin-bottom: 8px; }
.record-images { margin-top: 12px; display: flex; flex-wrap: wrap; gap: 8px; }
.record-image-thumb { width: 100px; height: 100px; object-fit: cover; border-radius: 10px; cursor: pointer; transition: transform 0.2s; border: 2px solid #f0e6d8; }
.record-image-thumb:hover { transform: scale(1.05); }
.action-btn { background: none; border: none; font-size: 15px; cursor: pointer; padding: 4px; border-radius: 6px; transition: background 0.2s; }
.action-btn:hover { background: #f5f0e8; }
.empty-state { text-align: center; padding: 60px 20px; color: #c9a87c; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }

/* Upload */
.upload-area { margin-top: 4px; }
.upload-preview { position: relative; display: inline-block; }
.upload-photos { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
.photo-item { position: relative; display: inline-block; }
.preview-thumb { width: 80px; height: 80px; object-fit: cover; border-radius: 10px; border: 2px solid #f0e6d8; }
.remove-btn { position: absolute; top: -6px; right: -6px; width: 20px; height: 20px; border-radius: 50%; background: #E53935; color: white; border: none; cursor: pointer; font-size: 11px; display: flex; align-items: center; justify-content: center; }
.upload-btn { display: inline-block; padding: 10px 20px; border-radius: 14px; border: 2px dashed #e0d5c5; cursor: pointer; font-size: 13px; color: #c9a87c; transition: all 0.2s; }
.upload-btn:hover { border-color: #FFB6C1; color: #FFB6C1; background: #FFF5F7; }
.readonly-input { background: #faf8f5; cursor: default; }
.upload-progress { margin-left: 10px; font-size: 12px; color: #E88D2E; }

/* Image preview overlay */
.image-preview-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.8); z-index: 2000; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.preview-img { max-width: 90vw; max-height: 90vh; border-radius: 12px; }
</style>
