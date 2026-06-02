<template>
  <div class="moment-detail">
    <button class="back-btn" @click="$router.push('/memories')">← 返回回忆库</button>

    <div v-if="loading" class="loading-spinner">加载中...</div>
    <template v-else-if="moment">
      <div class="moment-header card">
        <div class="moment-emoji">{{ moment.emoji }}</div>
        <h2>{{ moment.title }}</h2>
        <div class="moment-info">
          <span>📅 {{ moment.momentDate || moment.date }}</span>
          <span v-if="moment.location">📍 {{ moment.location }}</span>
        </div>
        <div class="header-actions">
          <button class="btn-outline" @click="openEdit">✏️ 编辑</button>
        </div>
      </div>

      <div class="photos-section">
        <div class="section-header">
          <h3>📸 记忆照片</h3>
          <label class="btn-outline add-photo-btn">
            <input type="file" accept="image/*" style="display:none" @change="onPhotoUpload" />
            + 添加照片
          </label>
          <span v-if="uploading" class="upload-progress">上传中 {{ progress }}%</span>
        </div>

        <div v-if="photos.length === 0" class="empty-state">
          <div class="empty-icon">🖼️</div>
          <p>还没有照片，快来添加吧！</p>
        </div>
        <div v-else class="photos-grid">
          <div v-for="(url, i) in photos" :key="i" class="photo-card">
            <img :src="url" class="photo-img" @click="previewImage = url" />
            <button class="photo-remove" @click="removePhoto(i)">✕</button>
          </div>
        </div>
      </div>
    </template>

    <!-- Image Preview -->
    <div v-if="previewImage" class="image-preview-overlay" @click="previewImage = null">
      <img :src="previewImage" class="preview-img" />
    </div>

    <!-- Edit Modal -->
    <Modal :visible="showEditModal" title="编辑记忆瞬间" @close="showEditModal = false" @confirm="handleSave">
      <label>标题</label><input v-model="editForm.title" placeholder="瞬间标题" />
      <div class="form-row">
        <div><label>日期</label><input type="date" v-model="editForm.momentDate" /></div>
        <div><label>地点</label><input v-model="editForm.location" placeholder="地点" /></div>
      </div>
      <div><label>图标</label><select v-model="editForm.emoji"><option v-for="e in momentEmojis" :key="e" :value="e">{{ e }}</option></select></div>
    </Modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Modal from '../components/Modal.vue'
import { useToast } from '../composables/useToast.js'
import { useUpload } from '../composables/useUpload.js'
import { getMoment, updateMoment } from '../data/api.js'

const route = useRoute()
const router = useRouter()
const { toast } = useToast()
const { uploading, progress, uploadImage } = useUpload()

const moment = ref(null)
const loading = ref(true)
const previewImage = ref(null)
const showEditModal = ref(false)
const saving = ref(false)
const momentEmojis = ['🏞️','🗼','🌊','💕','🎉','🍲','✈️','🏔️','🌅']

const photos = computed(() => {
  if (!moment.value || !moment.value.photoUrls) return []
  return moment.value.photoUrls.split(',').filter(u => u)
})

const editForm = ref({})

function openEdit() {
  editForm.value = {
    title: moment.value.title || '',
    momentDate: moment.value.momentDate || moment.value.date || '',
    location: moment.value.location || '',
    emoji: moment.value.emoji || '🏞️'
  }
  showEditModal.value = true
}

async function onPhotoUpload(e) {
  const file = e.target.files[0]; if (!file) return
  try {
    const url = await uploadImage(file)
    const newUrls = moment.value.photoUrls ? moment.value.photoUrls + ',' + url : url
    await updateMoment(moment.value.id, { ...moment.value, photoUrls: newUrls })
    moment.value.photoUrls = newUrls
    toast.success('照片已添加')
  } catch (e) { toast.error('上传失败') }
}

async function removePhoto(index) {
  if (!window.confirm('确定删除这张照片？')) return
  const urls = moment.value.photoUrls.split(',').filter((_, i) => i !== index)
  try {
    await updateMoment(moment.value.id, { ...moment.value, photoUrls: urls.join(',') })
    moment.value.photoUrls = urls.join(',')
    toast.success('已删除')
  } catch (e) { toast.error('删除失败') }
}

async function handleSave() {
  if (saving.value) return; saving.value = true
  try {
    await updateMoment(moment.value.id, { ...moment.value, ...editForm.value })
    moment.value = { ...moment.value, ...editForm.value }
    toast.success('修改成功')
    showEditModal.value = false
  } catch (e) { toast.error('保存失败') } finally { saving.value = false }
}

onMounted(async () => {
  try {
    moment.value = await getMoment(route.params.id)
  } catch (e) {
    toast.error('加载失败')
    router.push('/memories')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.moment-detail { max-width: 900px; margin: 0 auto; }
.back-btn { background: none; border: none; font-size: 14px; color: #c9a87c; cursor: pointer; padding: 0; margin-bottom: 20px; }
.back-btn:hover { color: #FFB6C1; }
.loading-spinner { text-align: center; padding: 60px; color: #c9a87c; }

.moment-header { text-align: center; padding: 32px 24px; border-radius: 20px; margin-bottom: 24px; background: linear-gradient(135deg, #FFFEFA, #FFFDF8); }
.moment-emoji { font-size: 64px; margin-bottom: 12px; }
.moment-header h2 { font-size: 24px; color: #5B4B3E; margin: 0 0 12px 0; }
.moment-info { display: flex; justify-content: center; gap: 16px; font-size: 14px; color: #c0a880; margin-bottom: 12px; }
.header-actions { display: flex; justify-content: center; }
.btn-outline { padding: 8px 18px; border-radius: 16px; border: 1.5px solid #e0d5c5; background: white; font-size: 13px; cursor: pointer; color: #8B7355; transition: all 0.2s; }
.btn-outline:hover { border-color: #FFB6C1; background: #FFF5F7; }

.photos-section {  }
.section-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.section-header h3 { font-size: 17px; color: #5B4B3E; margin: 0; flex: 1; }
.add-photo-btn { cursor: pointer; display: inline-block; }
.upload-progress { font-size: 12px; color: #E88D2E; }

.photos-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; }
.photo-card { position: relative; border-radius: 14px; overflow: hidden; aspect-ratio: 1; }
.photo-img { width: 100%; height: 100%; object-fit: cover; cursor: pointer; transition: transform 0.2s; }
.photo-img:hover { transform: scale(1.03); }
.photo-remove { position: absolute; top: 6px; right: 6px; width: 24px; height: 24px; border-radius: 50%; background: rgba(229,57,53,0.85); color: white; border: none; cursor: pointer; font-size: 12px; display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.2s; }
.photo-card:hover .photo-remove { opacity: 1; }

.empty-state { text-align: center; padding: 60px 20px; color: #c9a87c; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }

.image-preview-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.85); z-index: 2000; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.preview-img { max-width: 90vw; max-height: 90vh; border-radius: 12px; }

@media (max-width: 600px) { .photos-grid { grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); } }
</style>
