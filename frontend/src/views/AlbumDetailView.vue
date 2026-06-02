<template>
  <div class="album-detail">
    <button class="back-btn" @click="$router.push('/memories')">← 返回回忆库</button>

    <div v-if="loading" class="loading-spinner">加载中...</div>
    <template v-else-if="album">
      <div class="album-header card">
        <div class="cover-section">
          <img v-if="album.coverUrl" :src="album.coverUrl" class="cover-img" />
          <span v-else class="cover-emoji">{{ album.emoji }}</span>
        </div>
        <div class="album-meta">
          <h2>{{ album.emoji }} {{ album.location }}</h2>
          <p class="album-date">📅 {{ album.albumDate || album.date }}</p>
          <span v-if="album.isPrivate" class="private-tag">🔒 私密相册</span>
        </div>
        <div class="header-actions">
          <button class="btn-outline" @click="openEdit">✏️ 编辑</button>
        </div>
      </div>

      <div class="photos-section">
        <div class="section-header">
          <h3>📸 相册照片</h3>
          <label class="btn-outline add-photo-btn">
            <input type="file" accept="image/*" style="display:none" @change="onPhotoUpload" />
            + 添加照片
          </label>
          <span v-if="uploading" class="upload-progress">上传中 {{ progress }}%</span>
        </div>

        <div v-if="photos.length === 0" class="empty-state">
          <div class="empty-icon">📷</div>
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
    <Modal :visible="showEditModal" title="编辑相册" @close="showEditModal = false" @confirm="handleSave">
      <label>地点/主题</label><input v-model="editForm.location" placeholder="相册主题" />
      <div class="form-row">
        <div><label>日期</label><input type="date" v-model="editForm.albumDate" /></div>
        <div><label>图标</label><select v-model="editForm.emoji"><option v-for="e in albumEmojis" :key="e" :value="e">{{ e }}</option></select></div>
      </div>
      <label>封面图片</label>
      <div class="upload-area">
        <div v-if="editForm.coverUrl" class="upload-preview">
          <img :src="editForm.coverUrl" class="preview-thumb" />
          <button class="remove-btn" @click="editForm.coverUrl = ''">✕</button>
        </div>
        <label v-else class="upload-btn">
          <input type="file" accept="image/*" style="display:none" @change="onCoverUpload" />
          <span>📷 更换封面</span>
        </label>
      </div>
    </Modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Modal from '../components/Modal.vue'
import { useToast } from '../composables/useToast.js'
import { useUpload } from '../composables/useUpload.js'
import { getAlbum, updateAlbum } from '../data/api.js'

const route = useRoute()
const router = useRouter()
const { toast } = useToast()
const { uploading, progress, uploadImage } = useUpload()

const album = ref(null)
const loading = ref(true)
const previewImage = ref(null)
const showEditModal = ref(false)
const saving = ref(false)
const albumEmojis = ['🏛️','🌊','🗼','🧸','🍲','🌹','🍦','🌸','🦋']

const photos = computed(() => {
  if (!album.value || !album.value.photoUrls) return []
  return album.value.photoUrls.split(',').filter(u => u)
})

const editForm = ref({})

function openEdit() {
  editForm.value = {
    location: album.value.location || '',
    albumDate: album.value.albumDate || album.value.date || '',
    emoji: album.value.emoji || '🏛️',
    coverUrl: album.value.coverUrl || ''
  }
  showEditModal.value = true
}

async function onCoverUpload(e) {
  const file = e.target.files[0]; if (!file) return
  try { editForm.value.coverUrl = await uploadImage(file) } catch (e) { toast.error('上传失败') }
}

async function onPhotoUpload(e) {
  const file = e.target.files[0]; if (!file) return
  try {
    const url = await uploadImage(file)
    const newUrls = album.value.photoUrls ? album.value.photoUrls + ',' + url : url
    await updateAlbum(album.value.id, { ...album.value, photoUrls: newUrls })
    album.value.photoUrls = newUrls
    toast.success('照片已添加')
  } catch (e) { toast.error('上传失败') }
}

async function removePhoto(index) {
  if (!window.confirm('确定删除这张照片？')) return
  const urls = album.value.photoUrls.split(',').filter((_, i) => i !== index)
  try {
    await updateAlbum(album.value.id, { ...album.value, photoUrls: urls.join(',') })
    album.value.photoUrls = urls.join(',')
    toast.success('已删除')
  } catch (e) { toast.error('删除失败') }
}

async function handleSave() {
  if (saving.value) return; saving.value = true
  try {
    await updateAlbum(album.value.id, { ...album.value, ...editForm.value })
    album.value = { ...album.value, ...editForm.value }
    toast.success('修改成功')
    showEditModal.value = false
  } catch (e) { toast.error('保存失败') } finally { saving.value = false }
}

onMounted(async () => {
  try {
    album.value = await getAlbum(route.params.id)
  } catch (e) {
    toast.error('加载失败')
    router.push('/memories')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.album-detail { max-width: 900px; margin: 0 auto; }
.back-btn { background: none; border: none; font-size: 14px; color: #c9a87c; cursor: pointer; padding: 0; margin-bottom: 20px; }
.back-btn:hover { color: #FFB6C1; }
.loading-spinner { text-align: center; padding: 60px; color: #c9a87c; }

.album-header { display: flex; gap: 20px; padding: 24px; border-radius: 20px; margin-bottom: 24px; background: linear-gradient(135deg, #FFFEFA, #FFFDF8); align-items: center; }
.cover-section { width: 140px; height: 140px; border-radius: 16px; overflow: hidden; background: linear-gradient(135deg, #FFF8E1, #FFECB3); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.cover-img { width: 100%; height: 100%; object-fit: cover; }
.cover-emoji { font-size: 56px; }
.album-meta { flex: 1; }
.album-meta h2 { font-size: 22px; color: #5B4B3E; margin: 0 0 8px 0; }
.album-date { font-size: 14px; color: #c0a880; margin: 0 0 6px 0; }
.private-tag { font-size: 12px; color: #B06080; background: #F9E7F1; padding: 3px 10px; border-radius: 10px; }
.header-actions { flex-shrink: 0; }
.btn-outline { padding: 8px 18px; border-radius: 16px; border: 1.5px solid #e0d5c5; background: white; font-size: 13px; cursor: pointer; color: #8B7355; transition: all 0.2s; }
.btn-outline:hover { border-color: #FFB6C1; background: #FFF5F7; }

.photos-section {  }
.section-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.section-header h3 { font-size: 17px; color: #5B4B3E; margin: 0; flex: 1; }
.add-photo-btn { cursor: pointer; display: inline-block; }
.upload-progress { font-size: 12px; color: #E88D2E; }

.photos-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 12px; }
.photo-card { position: relative; border-radius: 14px; overflow: hidden; aspect-ratio: 1; }
.photo-img { width: 100%; height: 100%; object-fit: cover; cursor: pointer; transition: transform 0.2s; }
.photo-img:hover { transform: scale(1.03); }
.photo-remove { position: absolute; top: 6px; right: 6px; width: 24px; height: 24px; border-radius: 50%; background: rgba(229,57,53,0.85); color: white; border: none; cursor: pointer; font-size: 12px; display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.2s; }
.photo-card:hover .photo-remove { opacity: 1; }

.empty-state { text-align: center; padding: 60px 20px; color: #c9a87c; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }

.image-preview-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.85); z-index: 2000; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.preview-img { max-width: 90vw; max-height: 90vh; border-radius: 12px; }

.upload-area { margin-top: 4px; }
.upload-preview { position: relative; display: inline-block; }
.preview-thumb { width: 80px; height: 80px; object-fit: cover; border-radius: 10px; border: 2px solid #f0e6d8; }
.remove-btn { position: absolute; top: -6px; right: -6px; width: 20px; height: 20px; border-radius: 50%; background: #E53935; color: white; border: none; cursor: pointer; font-size: 11px; }
.upload-btn { display: inline-block; padding: 8px 16px; border-radius: 12px; border: 2px dashed #e0d5c5; cursor: pointer; font-size: 12px; color: #c9a87c; transition: all 0.2s; }
.upload-btn:hover { border-color: #FFB6C1; color: #FFB6C1; background: #FFF5F7; }

@media (max-width: 600px) { .album-header { flex-direction: column; text-align: center; } .cover-section { width: 100%; height: 200px; } }
</style>
