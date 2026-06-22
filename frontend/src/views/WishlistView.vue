<template>
  <div class="wishlist-view">
    <div class="page-header">
      <div>
        <h2 class="page-title">⭐ 心愿清单</h2>
        <p class="page-subtitle">好多心愿想一起实现</p>
      </div>
      <button class="btn-primary" @click="openCreate">+ 添加心愿</button>
    </div>

    <div class="stats-row">
      <StatsCard :number="totalWishes" label="全部心愿" bgColor="#FFF5D9" />
      <StatsCard :number="pendingWishes" label="待实现" bgColor="#F9E7F1" />
      <StatsCard :number="completedWishes" label="已完成" bgColor="#E8F5E9" />
    </div>

    <div class="filter-bar">
      <div class="filter-buttons">
        <button v-for="f in filters" :key="f.value" class="btn-outline" :class="{ active: currentFilter === f.value }" @click="currentFilter = f.value">{{ f.label }}</button>
      </div>
      <div class="filter-right">
        <div class="owner-toggle">
          <button :class="['owner-btn', { active: ownerFilter === 'all' }]" @click="ownerFilter = 'all'">全部</button>
          <button :class="['owner-btn', { active: ownerFilter === 'mine' }]" @click="ownerFilter = 'mine'">我的</button>
          <button :class="['owner-btn', { active: ownerFilter === 'partner' }]" @click="ownerFilter = 'partner'">TA的</button>
        </div>
        <select v-model="selectedTag" class="tag-select">
          <option value="all">全部标签</option>
          <option v-for="tag in tags" :key="tag" :value="tag">{{ tag }}</option>
        </select>
      </div>
    </div>

    <div v-if="loading" class="loading-spinner">加载中...</div>
    <div v-else class="wish-list animate-stagger">
      <div v-for="wish in displayWishes" :key="wish.id" class="wish-card card" :class="{ completed: wish.status === 'completed' }">
        <div class="wish-header">
          <div class="wish-avatar">{{ wish.userId === 2 ? '👧' : '👦' }}</div>
          <div class="wish-meta">
            <span class="wish-category" :class="getCategoryClass(wish.category)">{{ wish.category }}</span>
            <span class="wish-author-tag">{{ wish.author }}</span>
          </div>
          <div class="wish-actions">
            <button class="action-btn" @click="openEdit(wish)">✏️</button>
            <button class="action-btn" @click="handleDelete(wish.id)">🗑️</button>
          </div>
        </div>
        <h3 class="wish-title" :class="{ 'is-completed': wish.status === 'completed' }">{{ wish.title }}</h3>
        <p class="wish-description">{{ wish.description }}</p>
        <div v-if="wish.imageUrls" class="wish-images">
          <img
            v-for="(url, i) in wish.imageUrls.split(',')"
            :key="i"
            :src="url"
            class="wish-image-thumb"
          />
        </div>
        <div class="wish-footer">
          <span class="wish-date">发起于: {{ wish.startDate }}</span>
          <span v-if="wish.status === 'completed'" class="completed-badge">已完成 ✨</span>
          <span v-else class="pending-badge" @click="markCompleted(wish)">点我标记完成~</span>
        </div>
      </div>
      <div v-if="displayWishes.length === 0" class="empty-state">
        <div class="empty-icon">✨</div><p>暂无符合条件的愿望~</p>
      </div>
    </div>

    <div class="bottom-quote card">
      <div class="quote-icon">✨</div>
      <div class="quote-main">一起实现每一个小小的梦想</div>
      <div class="quote-sub">每一个心愿都是我们共同的期待</div>
    </div>

    <Modal :visible="showModal" :title="modalTitle" @close="showModal = false" @confirm="handleSave">
      <label>心愿标题</label><input v-model="form.title" placeholder="心愿标题" />
      <label>描述</label><textarea v-model="form.description" placeholder="描述一下这个心愿..." rows="3"></textarea>
      <div class="form-row">
        <div><label>分类</label><select v-model="form.category"><option v-for="t in tags" :key="t" :value="t">{{ t }}</option></select></div>
        <div><label>状态</label><select v-model="form.status"><option value="pending">待实现</option><option value="completed">已完成</option></select></div>
      </div>
      <div class="form-row">
        <div><label>发起人</label><select v-model="form.author"><option value="用户A">用户A</option><option value="用户B">用户B</option></select></div>
        <div><label>发起日期</label><input type="date" v-model="form.startDate" /></div>
      </div>
      <label>配图（可上传多张）</label>
      <div class="upload-area">
        <div v-if="form.imageUrls" class="upload-photos">
          <div v-for="(url, i) in form.imageUrls.split(',')" :key="i" class="photo-item">
            <img :src="url" class="preview-thumb" />
            <button class="remove-btn" @click="removeWishImage(i)">✕</button>
          </div>
        </div>
        <label class="upload-btn">
          <input type="file" accept="image/*" style="display:none" @change="onImageUpload" />
          <span>📷 点击上传配图</span>
        </label>
        <span v-if="uploading" class="upload-progress">上传中 {{ progress }}%</span>
      </div>
    </Modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import StatsCard from '../components/StatsCard.vue'
import Modal from '../components/Modal.vue'
import { useToast } from '../composables/useToast.js'
import { useUpload } from '../composables/useUpload.js'
import { useUserStore } from '../stores/userStore.js'
import { getWishes, getWishStats, createWish, updateWish, deleteWish } from '../data/api.js'

const { toast } = useToast()
const { uploading, progress, uploadImage } = useUpload()
const userStore = useUserStore()

const currentFilter = ref('all')
const selectedTag = ref('all')
const ownerFilter = ref('all')
const tags = ['未来规划', '旅行计划', '生活目标']
const filters = [{ label: '全部', value: 'all' },{ label: '待实现', value: 'pending' },{ label: '已完成', value: 'completed' }]
const statsData = ref({ total: 0, pending: 0, completed: 0 })
const filteredWishes = ref([])
const loading = ref(true)
const totalWishes = computed(() => statsData.value.total)
const pendingWishes = computed(() => statsData.value.pending)
const completedWishes = computed(() => statsData.value.completed)

const displayWishes = computed(() => {
  if (ownerFilter.value === 'all') return filteredWishes.value
  const uid = ownerFilter.value === 'mine' ? userStore.state.userId : (userStore.state.userId === 1 ? 2 : 1)
  return filteredWishes.value.filter(w => w.userId === uid)
})

const showModal = ref(false); const modalTitle = ref('添加心愿')
const editingId = ref(null); const saving = ref(false)
const emptyForm = () => ({ title: '', description: '', category: '未来规划', status: 'pending', author: userStore.state.nickname || '用户A', startDate: new Date().toISOString().slice(0, 10), imageUrls: '' })
const form = ref(emptyForm())

function openCreate() { editingId.value = null; form.value = emptyForm(); modalTitle.value = '添加心愿'; showModal.value = true }
function openEdit(w) {
  editingId.value = w.id
  form.value = { title: w.title, description: w.description || '', category: w.category || '', status: w.status || 'pending', author: w.author || '', startDate: w.startDate || '', imageUrls: w.imageUrls || '' }
  modalTitle.value = '编辑心愿'; showModal.value = true
}

async function onImageUpload(e) {
  const file = e.target.files[0]
  if (!file) return
  try {
    const url = await uploadImage(file)
    form.value.imageUrls = form.value.imageUrls ? form.value.imageUrls + ',' + url : url
  } catch (e) { toast.error('上传失败') }
}

function removeWishImage(index) {
  const urls = form.value.imageUrls.split(',').filter((_, i) => i !== index)
  form.value.imageUrls = urls.join(',')
}

async function handleSave() {
  if (saving.value) return; saving.value = true
  try {
    if (editingId.value) await updateWish(editingId.value, form.value)
    else await createWish(form.value)
    toast.success(editingId.value ? '修改成功' : '添加成功')
    showModal.value = false; loadData()
  } catch (e) { toast.error('保存失败') } finally { saving.value = false }
}

async function handleDelete(id) {
  if (!window.confirm('确定删除？')) return
  try { await deleteWish(id); toast.success('删除成功'); loadData() }
  catch (e) { toast.error('删除失败') }
}

async function markCompleted(wish) {
  try { await updateWish(wish.id, { ...wish, status: 'completed' }); toast.success('心愿已完成！'); loadData() }
  catch (e) { toast.error('操作失败') }
}

async function loadData() {
  loading.value = true
  try {
    const [wishes, stats] = await Promise.all([getWishes(currentFilter.value, selectedTag.value), getWishStats()])
    filteredWishes.value = wishes || []; statsData.value = stats || { total: 0, pending: 0, completed: 0 }
  } catch (e) { filteredWishes.value = [] } finally { loading.value = false }
}

function getCategoryClass(c) { const m = { '未来规划':'cat-purple','旅行计划':'cat-blue','生活目标':'cat-green' }; return m[c] || 'cat-purple' }

watch([currentFilter, selectedTag], loadData)
onMounted(() => { loadData() })
</script>

<style scoped>
.wishlist-view { max-width: 800px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.page-title { font-size: 22px; font-weight: 700; background: linear-gradient(135deg, #FF8C42, #FFB6C1); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.page-subtitle { font-size: 14px; color: #c9a87c; margin-top: 4px; }
.stats-row { display: flex; gap: 12px; margin-bottom: 20px; }
.filter-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
.filter-buttons { display: flex; gap: 6px; }
.filter-right { display: flex; align-items: center; gap: 12px; }
.owner-toggle { display: flex; gap: 3px; background: #f5f0e8; border-radius: 14px; padding: 3px; }
.owner-btn { padding: 5px 12px; border: none; border-radius: 12px; font-size: 12px; cursor: pointer; background: transparent; color: #999; transition: all 0.2s; }
.owner-btn.active { background: white; color: #E88D2E; font-weight: 600; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.tag-select { padding: 7px 14px; border-radius: 14px; border: 1.5px solid #e0d5c5; background: white; font-size: 13px; color: #6B5B4E; cursor: pointer; outline: none; }

.loading-spinner { text-align: center; padding: 40px; color: #c9a87c; font-size: 14px; }
.wish-list { display: flex; flex-direction: column; gap: 14px; margin-bottom: 28px; }
.wish-card { padding: 20px; border-radius: 20px; background: linear-gradient(135deg, #FFFEFA, #FFFCF8); transition: all 0.3s; }
.wish-card:hover { box-shadow: 0 8px 28px rgba(255,182,193,0.12); transform: translateY(-2px); }
.wish-card.completed { opacity: 0.7; background: linear-gradient(135deg, #f8f8f8, #f5f5f3); }
.wish-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.wish-avatar { width: 38px; height: 38px; border-radius: 50%; background: linear-gradient(135deg, #FFF5D9, #FFE8C0); display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0; }
.wish-meta { flex: 1; display: flex; align-items: center; gap: 8px; }
.wish-category { padding: 4px 10px; border-radius: 10px; font-size: 11px; font-weight: 500; color: white; }
.cat-purple { background: linear-gradient(135deg, #B388FF, #CE93D8); }
.cat-blue { background: linear-gradient(135deg, #64B5F6, #81D4FA); }
.cat-green { background: linear-gradient(135deg, #81C784, #A5D6A7); }
.wish-author-tag { font-size: 11px; color: #c0a880; background: #faf5ed; padding: 3px 8px; border-radius: 8px; }
.wish-actions { display: flex; gap: 4px; }
.action-btn { background: none; border: none; font-size: 15px; cursor: pointer; padding: 4px; border-radius: 6px; transition: background 0.2s; }
.action-btn:hover { background: #f5f0e8; }
.wish-title { font-size: 17px; font-weight: 700; color: #5B4B3E; margin-bottom: 6px; }
.wish-title.is-completed { color: #ccc; text-decoration: line-through; }
.wish-description { font-size: 13px; color: #8B7B8B; margin-bottom: 10px; }
.wish-images { margin-bottom: 10px; display: flex; flex-wrap: wrap; gap: 8px; }
.wish-image-thumb { width: 90px; height: 90px; object-fit: cover; border-radius: 10px; border: 2px solid #f0e6d8; }
.wish-footer { display: flex; justify-content: space-between; align-items: center; }
.wish-date { font-size: 12px; color: #c9a87c; }
.completed-badge { font-size: 12px; color: #81C784; font-weight: 500; }
.pending-badge { font-size: 12px; color: #FFA500; cursor: pointer; padding: 4px 10px; border-radius: 10px; background: #FFF5EC; transition: all 0.2s; }
.pending-badge:hover { background: #FFE0C0; color: #E88D2E; }
.empty-state { text-align: center; padding: 60px 20px; color: #c9a87c; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.bottom-quote { text-align: center; padding: 28px 20px; border-radius: 20px; background: linear-gradient(135deg, #FFFDF9, #FFF8F5); }
.quote-icon { font-size: 32px; margin-bottom: 10px; }
.quote-main { font-size: 17px; font-weight: 700; color: #8B7355; margin-bottom: 6px; }
.quote-sub { font-size: 13px; color: #c0a880; }

/* Upload */
.upload-area { margin-top: 4px; }
.upload-preview { position: relative; display: inline-block; }
.upload-photos { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
.photo-item { position: relative; display: inline-block; }
.preview-thumb { width: 80px; height: 80px; object-fit: cover; border-radius: 10px; border: 2px solid #f0e6d8; }
.remove-btn { position: absolute; top: -6px; right: -6px; width: 20px; height: 20px; border-radius: 50%; background: #E53935; color: white; border: none; cursor: pointer; font-size: 11px; display: flex; align-items: center; justify-content: center; }
.upload-btn { display: inline-block; padding: 10px 20px; border-radius: 14px; border: 2px dashed #e0d5c5; cursor: pointer; font-size: 13px; color: #c9a87c; transition: all 0.2s; }
.upload-btn:hover { border-color: #FFB6C1; color: #FFB6C1; background: #FFF5F7; }
.upload-progress { margin-left: 10px; font-size: 12px; color: #E88D2E; }
</style>
