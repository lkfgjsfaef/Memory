<template>
  <div class="memories-view">
    <div class="page-header">
      <div>
        <h2 class="page-title">💕 回忆库</h2>
        <p class="page-subtitle">珍藏我们每一个闪闪发光的瞬间</p>
      </div>
    </div>

    <div class="sub-tabs">
      <button v-for="tab in subTabs" :key="tab.value" class="sub-tab" :class="{ active: activeTab === tab.value }" @click="activeTab = tab.value">
        <span>{{ tab.icon }}</span>{{ tab.label }}
      </button>
    </div>

    <!-- Footprint Map — always rendered for fast init -->
    <div v-if="activeTab === 'map'" class="tab-content">
      <div class="map-section">
        <div class="map-container card">
          <div v-if="loading" class="map-loading">地图加载中...</div>
          <div ref="leafletMap" class="leaflet-map" :class="{ 'map-ready': !loading }"></div>
        </div>
        <div class="map-footer">
          <span class="footer-icon">👣</span>
          <span>我们的足迹 — 目前去过了 <strong>{{ visitedLocations.length }}</strong> 个地方</span>
          <span class="map-hint">点击地图任意位置添加足迹</span>
        </div>
      </div>
    </div>

    <div v-if="loading && activeTab !== 'map'" class="loading-spinner">加载中...</div>
    <template v-if="!loading || activeTab === 'map'">
      <!-- Memory River -->

      <!-- Memory River -->
      <div v-if="activeTab === 'river'" class="tab-content">
        <div v-if="timelineMemories.length === 0" class="empty-state">
          <div class="empty-icon">📖</div><p>暂无记忆瞬间</p>
        </div>
        <div v-for="group in timelineMemories" :key="group.label" class="timeline-group">
          <div class="group-header">
            <span class="group-star">⭐</span><span class="group-label">{{ group.label }}</span>
            <span class="group-badge">{{ group.moments.length }} 个瞬间</span>
          </div>
          <div class="group-line"></div>
          <div v-for="moment in group.moments" :key="moment.id" class="moment-card card" @click="goMomentDetail(moment.id)">
            <div class="moment-collage">
              <div v-if="moment.photoUrls" class="moment-photos">
                <img
                  v-for="(url, i) in moment.photoUrls.split(',').slice(0, 4)"
                  :key="i"
                  :src="url"
                  class="moment-photo"
                />
              </div>
              <div v-else class="collage-placeholder"><span class="collage-emoji">{{ moment.emoji }}</span></div>
            </div>
            <div class="moment-title">{{ moment.title }}</div>
            <div class="moment-meta">
              <span>📅 {{ moment.momentDate || moment.date }}</span>
              <span v-if="moment.location">📍 {{ moment.location }}</span>
            </div>
            <div class="moment-actions" @click.stop>
              <button class="btn-outline-sm" @click="openMomentEdit(moment)">编辑</button>
              <button class="btn-outline-sm btn-danger" @click="handleDeleteMoment(moment.id)">删除</button>
            </div>
          </div>
        </div>
        <button class="btn-primary add-btn" @click="openMomentCreate">+ 添加记忆瞬间</button>
      </div>

      <!-- Time Album -->
      <div v-if="activeTab === 'album'" class="tab-content">
        <div class="album-grid">
          <div v-for="album in memoryAlbums" :key="album.id" class="album-card card" @click="goAlbumDetail(album.id)">
            <div class="album-tape"></div>
            <div class="album-cover">
              <img v-if="album.coverUrl" :src="album.coverUrl" class="cover-img" />
              <span v-else class="photo-emoji">{{ album.emoji }}</span>
              <span v-if="album.isPrivate" class="album-private">私密</span>
            </div>
            <div class="album-info">
              <div class="album-location">{{ album.location }}</div>
              <div class="album-date">{{ album.albumDate || album.date }}</div>
            </div>
            <div class="album-actions" @click.stop>
              <button class="btn-outline-sm" @click="openAlbumEdit(album)">编辑</button>
              <button class="btn-outline-sm btn-danger" @click="handleDeleteAlbum(album.id)">删除</button>
            </div>
          </div>
          <div class="album-card create-card" @click="openAlbumCreate">
            <div class="create-icon">+</div><div class="create-text">创建新相册</div>
          </div>
        </div>
      </div>
    </template>

    <!-- Image Preview -->
    <div v-if="previewImage" class="image-preview-overlay" @click="previewImage = null">
      <img :src="previewImage" class="preview-img" />
    </div>

    <!-- Location Modal -->
    <Modal :visible="showLocationModal" title="添加足迹" @close="showLocationModal = false" @confirm="handleSaveLocation">
      <div class="loc-select-status" :class="{ selected: locationForm.lat != null }">
        <span v-if="locationForm.lat != null">📍 已选择位置 ({{ locationForm.lat.toFixed(4) }}, {{ locationForm.lng.toFixed(4) }})</span>
        <span v-else>👆 请关闭弹窗，在地图上点击位置</span>
        <button v-if="locationForm.lat != null" class="btn-outline-sm" type="button" @click="clearLocation">重新选择</button>
      </div>
      <label>城市名</label><input v-model="locationForm.name" placeholder="城市名" />
      <label>标题描述</label><input v-model="locationForm.title" placeholder="例如：第一次一起旅行" />
      <div class="form-row">
        <div><label>省份</label><input v-model="locationForm.province" placeholder="省份" /></div>
        <div><label>到访日期</label><input type="date" v-model="locationForm.visitDate" /></div>
      </div>
      <label>配图</label>
      <div class="upload-area">
        <div v-if="locationForm.imageUrl" class="upload-preview">
          <img :src="locationForm.imageUrl" class="preview-thumb" />
          <button class="remove-btn" @click="locationForm.imageUrl = ''">✕</button>
        </div>
        <label v-else class="upload-btn">
          <input type="file" accept="image/*" style="display:none" @change="onLocImageUpload" />
          <span>📷 点击上传照片</span>
        </label>
        <span v-if="uploading" class="upload-progress">{{ progress }}%</span>
      </div>
    </Modal>

    <!-- Moment Modal -->
    <Modal :visible="showMomentModal" :title="momentModalTitle" @close="showMomentModal = false" @confirm="handleSaveMoment">
      <label>标题</label><input v-model="momentForm.title" placeholder="瞬间标题" />
      <div class="form-row">
        <div><label>日期</label><input type="date" v-model="momentForm.momentDate" /></div>
        <div><label>地点</label><input v-model="momentForm.location" placeholder="地点" /></div>
      </div>
      <div><label>图标</label><select v-model="momentForm.emoji"><option v-for="e in momentEmojis" :key="e" :value="e">{{ e }}</option></select></div>
      <label>照片（可上传多张）</label>
      <div class="upload-area">
        <div v-if="momentForm.photoUrls" class="upload-photos">
          <div v-for="(url, i) in momentForm.photoUrls.split(',')" :key="i" class="photo-item">
            <img :src="url" class="preview-thumb" />
            <button class="remove-btn" @click="removeMomentPhoto(i)">✕</button>
          </div>
        </div>
        <label class="upload-btn">
          <input type="file" accept="image/*" style="display:none" @change="onMomentImageUpload" />
          <span>📷 上传照片</span>
        </label>
        <span v-if="uploading" class="upload-progress">{{ progress }}%</span>
      </div>
    </Modal>

    <!-- Album Modal -->
    <Modal :visible="showAlbumModal" :title="albumModalTitle" @close="showAlbumModal = false" @confirm="handleSaveAlbum">
      <label>地点/主题</label><input v-model="albumForm.location" placeholder="相册主题" />
      <div class="form-row">
        <div><label>日期</label><input type="date" v-model="albumForm.albumDate" /></div>
        <div><label>图标</label><select v-model="albumForm.emoji"><option v-for="e in albumEmojis" :key="e" :value="e">{{ e }}</option></select></div>
      </div>
      <label>封面图片</label>
      <div class="upload-area">
        <div v-if="albumForm.coverUrl" class="upload-preview">
          <img :src="albumForm.coverUrl" class="preview-thumb" />
          <button class="remove-btn" @click="albumForm.coverUrl = ''">✕</button>
        </div>
        <label v-else class="upload-btn">
          <input type="file" accept="image/*" style="display:none" @change="onAlbumCoverUpload" />
          <span>📷 点击上传封面</span>
        </label>
        <span v-if="uploading" class="upload-progress">{{ progress }}%</span>
      </div>
      <label>相册图片（可上传多张）</label>
      <div class="upload-area">
        <div v-if="albumForm.photoUrls" class="upload-photos">
          <div v-for="(url, i) in albumForm.photoUrls.split(',')" :key="i" class="photo-item">
            <img :src="url" class="preview-thumb" />
            <button class="remove-btn" @click="removeAlbumPhoto(i)">✕</button>
          </div>
        </div>
        <label class="upload-btn">
          <input type="file" accept="image/*" style="display:none" @change="onAlbumPhotoUpload" />
          <span>📷 上传照片</span>
        </label>
      </div>
    </Modal>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Fix Leaflet default marker icons for Vite bundling
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
})
import Modal from '../components/Modal.vue'
import { useToast } from '../composables/useToast.js'
import { useUpload } from '../composables/useUpload.js'
import { getAlbums, createAlbum, updateAlbum, deleteAlbum, getMoments, createMoment, updateMoment, deleteMoment, getLocations, createLocation, deleteLocation } from '../data/api.js'

const router = useRouter()
const { toast } = useToast()
const { uploading, progress, uploadImage } = useUpload()

const activeTab = ref('map')
const memoryAlbums = ref([])
const timelineMemories = ref([])
const visitedLocations = ref([])
const loading = ref(true)
const previewImage = ref(null)

const subTabs = [
  { value: 'map', label: '足迹地图', icon: '📍' },
  { value: 'river', label: '记忆长河', icon: '📖' },
  { value: 'album', label: '时光相册', icon: '📷' }
]

const saving = ref(false)

// Location
const showLocationModal = ref(false)
const emptyLocationForm = () => ({ name: '', province: '', visitDate: '', title: '', imageUrl: '', lat: null, lng: null })
const locationForm = ref(emptyLocationForm())
const leafletMap = ref(null)
let mapInstance = null
let clickMarker = null

// Moment
const showMomentModal = ref(false); const momentModalTitle = ref('添加记忆瞬间')
const editingMomentId = ref(null)
const momentEmojis = ['🏞️','🗼','🌊','💕','🎉','🍲','✈️','🏔️','🌅']
const emptyMomentForm = () => ({ title: '', momentDate: '', location: '', emoji: '🏞️', photoUrls: '' })
const momentForm = ref(emptyMomentForm())

// Album
const showAlbumModal = ref(false); const albumModalTitle = ref('创建新相册')
const editingAlbumId = ref(null)
const albumEmojis = ['🏛️','🌊','🗼','🧸','🍲','🌹','🍦','🌸','🦋']
const emptyAlbumForm = () => ({ location: '', albumDate: '', emoji: '🏛️', coverUrl: '', photoUrls: '' })
const albumForm = ref(emptyAlbumForm())

// Map initialization
function initMap() {
  if (!leafletMap.value || mapInstance) return
  mapInstance = L.map(leafletMap.value, { attributionControl: false, zoomControl: true }).setView([32.5, 110], 5)

  // Use tiles from multiple possible CDNs
  // Try GeoQ first (known to work in China), fall back to Amap
  const tiles = L.tileLayer('https://map.geoq.cn/ArcGIS/rest/services/ChinaOnlineCommunity/MapServer/tile/{z}/{y}/{x}', {
    maxZoom: 18,
    attribution: 'GeoQ'
  }).addTo(mapInstance)

  tiles.on('tileerror', function() {
    if (tiles._url.indexOf('geoq') !== -1) {
      tiles.setUrl('https://webrd01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}')
    }
  })

  // Click to add location
  mapInstance.on('click', function(e) {
    if (clickMarker) mapInstance.removeLayer(clickMarker)
    clickMarker = L.marker(e.latlng).addTo(mapInstance)
      .bindPopup('点击这里添加足迹', { closeButton: false })
      .openPopup()
    locationForm.value.lat = parseFloat(e.latlng.lat.toFixed(7))
    locationForm.value.lng = parseFloat(e.latlng.lng.toFixed(7))
    if (!showLocationModal.value) showLocationModal.value = true
  })

  addMapMarkers()
}

function addMapMarkers() {
  if (!mapInstance) return
  mapInstance.eachLayer(layer => { if (layer instanceof L.Marker && layer !== clickMarker) mapInstance.removeLayer(layer) })
  visitedLocations.value.forEach(loc => {
    const lat = parseFloat(loc.lat)
    const lng = parseFloat(loc.lng)
    if (lat && lng) {
      const popupHtml = `
        <div style="text-align:center;font-family:sans-serif">
          <strong>${loc.title || loc.name}</strong><br/>
          <small>${loc.province || ''} ${loc.visitDate || ''}</small><br/>
          ${loc.imageUrl ? `<img src="${loc.imageUrl}" style="max-width:120px;max-height:80px;border-radius:8px;margin-top:4px"/>` : ''}
        </div>`
      const marker = L.marker([lat, lng]).addTo(mapInstance)
        .bindPopup(popupHtml, { closeButton: false })
      marker.on('mouseover', () => marker.openPopup())
      marker.on('mouseout', () => marker.closePopup())
    }
  })
}

watch(visitedLocations, () => {
  nextTick(() => addMapMarkers())
})

// Detail page navigation
function goAlbumDetail(id) { router.push(`/memories/album/${id}`) }
function goMomentDetail(id) { router.push(`/memories/moment/${id}`) }

// Location handlers
function clearLocation() {
  locationForm.value.lat = null
  locationForm.value.lng = null
  if (clickMarker) { mapInstance.removeLayer(clickMarker); clickMarker = null }
}

function openLocationCreate() { locationForm.value = emptyLocationForm(); clearLocation(); showLocationModal.value = true }

async function onLocImageUpload(e) {
  const file = e.target.files[0]; if (!file) return
  try { locationForm.value.imageUrl = await uploadImage(file) } catch (e) { toast.error('上传失败') }
}

async function handleSaveLocation() {
  if (saving.value) return
  if (!locationForm.value.name) { toast.error('请输入城市名'); return }
  saving.value = true
  try {
    const data = { ...locationForm.value }
    if (data.lat == null) { data.lat = 32.5; data.lng = 110 }
    await createLocation(data); toast.success('添加成功')
    showLocationModal.value = false
    if (clickMarker) { mapInstance.removeLayer(clickMarker); clickMarker = null }
    locationForm.value = emptyLocationForm()
    loadAll()
  } catch (e) { toast.error('保存失败') } finally { saving.value = false }
}

// Moment handlers
function openMomentCreate() { editingMomentId.value = null; momentForm.value = emptyMomentForm(); momentModalTitle.value = '添加记忆瞬间'; showMomentModal.value = true }
function openMomentEdit(m) {
  editingMomentId.value = m.id
  momentForm.value = { title: m.title, momentDate: m.momentDate || m.date || '', location: m.location || '', emoji: m.emoji || '🏞️', photoUrls: m.photoUrls || '' }
  momentModalTitle.value = '编辑记忆瞬间'; showMomentModal.value = true
}

async function onMomentImageUpload(e) {
  const file = e.target.files[0]; if (!file) return
  try {
    const url = await uploadImage(file)
    momentForm.value.photoUrls = momentForm.value.photoUrls ? momentForm.value.photoUrls + ',' + url : url
  } catch (e) { toast.error('上传失败') }
}

function removeMomentPhoto(index) {
  const urls = momentForm.value.photoUrls.split(',').filter((_, i) => i !== index)
  momentForm.value.photoUrls = urls.join(',')
}

async function handleSaveMoment() {
  if (saving.value) return; saving.value = true
  try {
    if (editingMomentId.value) await updateMoment(editingMomentId.value, momentForm.value)
    else await createMoment(momentForm.value)
    toast.success(editingMomentId.value ? '修改成功' : '添加成功')
    showMomentModal.value = false; loadAll()
  } catch (e) { toast.error('保存失败') } finally { saving.value = false }
}

async function handleDeleteMoment(id) {
  if (!window.confirm('确定删除？')) return
  try { await deleteMoment(id); toast.success('删除成功'); loadAll() } catch (e) { toast.error('删除失败') }
}

// Album handlers
function openAlbumCreate() { editingAlbumId.value = null; albumForm.value = emptyAlbumForm(); albumModalTitle.value = '创建新相册'; showAlbumModal.value = true }
function openAlbumEdit(a) {
  editingAlbumId.value = a.id
  albumForm.value = { location: a.location || '', albumDate: a.albumDate || a.date || '', emoji: a.emoji || '🏛️', coverUrl: a.coverUrl || '', photoUrls: a.photoUrls || '' }
  albumModalTitle.value = '编辑相册'; showAlbumModal.value = true
}

async function onAlbumCoverUpload(e) {
  const file = e.target.files[0]; if (!file) return
  try { albumForm.value.coverUrl = await uploadImage(file) } catch (e) { toast.error('上传失败') }
}

async function onAlbumPhotoUpload(e) {
  const file = e.target.files[0]; if (!file) return
  try {
    const url = await uploadImage(file)
    albumForm.value.photoUrls = albumForm.value.photoUrls ? albumForm.value.photoUrls + ',' + url : url
  } catch (e) { toast.error('上传失败') }
}

function removeAlbumPhoto(index) {
  const urls = albumForm.value.photoUrls.split(',').filter((_, i) => i !== index)
  albumForm.value.photoUrls = urls.join(',')
}

async function handleSaveAlbum() {
  if (saving.value) return; saving.value = true
  try {
    if (editingAlbumId.value) await updateAlbum(editingAlbumId.value, albumForm.value)
    else await createAlbum(albumForm.value)
    toast.success(editingAlbumId.value ? '修改成功' : '添加成功')
    showAlbumModal.value = false; loadAll()
  } catch (e) { toast.error('保存失败') } finally { saving.value = false }
}

async function handleDeleteAlbum(id) {
  if (!window.confirm('确定删除？')) return
  try { await deleteAlbum(id); toast.success('删除成功'); loadAll() } catch (e) { toast.error('删除失败') }
}

async function loadAll() {
  loading.value = true
  // Init map immediately — don't wait for data
  if (activeTab.value === 'map') {
    await nextTick()
    initMap()
  }
  try {
    const results = await Promise.allSettled([getAlbums(), getMoments(), getLocations()])
    memoryAlbums.value = results[0].status === 'fulfilled' ? (results[0].value || []) : []
    timelineMemories.value = results[1].status === 'fulfilled' ? (results[1].value || []) : []
    visitedLocations.value = results[2].status === 'fulfilled' ? (results[2].value || []) : []
  } catch (e) {} finally { loading.value = false }
  await nextTick()
  addMapMarkers()
}

onMounted(() => { loadAll() })

watch(activeTab, async (tab) => {
  if (tab === 'map') {
    await nextTick()
    initMap()
    if (!loading.value) addMapMarkers()
  }
})
</script>

<style scoped>
.memories-view { max-width: 1100px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.page-title { font-size: 22px; font-weight: 700; background: linear-gradient(135deg, #FF8C42, #FFB6C1); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.page-subtitle { font-size: 14px; color: #c9a87c; margin-top: 4px; }
.loading-spinner { text-align: center; padding: 60px; color: #c9a87c; font-size: 14px; }

.sub-tabs { display: flex; justify-content: center; gap: 6px; margin-bottom: 24px; }
.sub-tab { display: flex; align-items: center; gap: 5px; padding: 9px 22px; border-radius: 22px; background: white; border: 1.5px solid #e8e0d8; font-size: 13px; font-weight: 500; color: #8B7355; cursor: pointer; transition: all 0.3s; }
.sub-tab:hover { border-color: #FFB6C1; background: #FFF5F7; }
.sub-tab.active { background: linear-gradient(135deg, #FFF0E0, #FFE0D0); border-color: #FFA500; color: #8B4513; font-weight: 600; }

.btn-outline-sm { padding: 5px 14px; border-radius: 14px; border: 1.5px solid #e0d5c5; background: white; font-size: 12px; cursor: pointer; color: #8B7355; transition: all 0.2s; }
.btn-outline-sm:hover { border-color: #FFB6C1; background: #FFF5F7; }
.btn-outline-sm.btn-danger { color: #E53935; border-color: #f0c8c8; }
.btn-outline-sm.btn-danger:hover { background: #FFF5F5; }
.add-btn { display: block; margin: 20px auto 0; }
.btn-primary { padding: 10px 24px; border-radius: 20px; border: none; background: linear-gradient(135deg, #FFA500, #FFB6C1); color: white; font-weight: 600; cursor: pointer; font-size: 14px; transition: all 0.3s; }
.btn-primary:hover { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(255,165,0,0.3); }

/* Map */
.map-section {  }
.map-container { padding: 4px; border-radius: 20px; overflow: hidden; position: relative; }
.map-loading { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: #faf8f5; z-index: 10; border-radius: 18px; font-size: 14px; color: #c9a87c; }
.leaflet-map { width: 100%; height: 480px; border-radius: 18px; cursor: crosshair; }
.map-footer { text-align: center; padding: 16px; font-size: 14px; color: #8B7355; display: flex; align-items: center; justify-content: center; gap: 10px; flex-wrap: wrap; }
.map-hint { font-size: 12px; color: #c9a87c; background: #FFFDF5; padding: 4px 12px; border-radius: 12px; border: 1px dashed #e0d5c5; }

.loc-select-status { padding: 10px 14px; border-radius: 12px; margin-bottom: 12px; font-size: 13px; display: flex; align-items: center; gap: 8px; justify-content: space-between; }
.loc-select-status.selected { background: #E8F5E9; color: #388E3C; }
.loc-select-status:not(.selected) { background: #FFF8E1; color: #F57F17; }

.empty-state { text-align: center; padding: 60px 20px; color: #c9a87c; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }

/* Memory River */
.timeline-group { margin-bottom: 28px; }
.group-header { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; padding-left: 10px; }
.group-star { font-size: 18px; }
.group-label { font-size: 16px; font-weight: 700; color: #5B4B3E; }
.group-badge { background: #F9E7F1; color: #B06080; padding: 3px 12px; border-radius: 12px; font-size: 11px; }
.group-line { width: 2px; height: 16px; background: linear-gradient(to bottom, #FFD6C0, transparent); margin-left: 20px; margin-bottom: 10px; }
.moment-card { margin-left: 36px; padding: 18px; max-width: 600px; border-radius: 18px; cursor: pointer; transition: all 0.2s; }
.moment-card:hover { box-shadow: 0 6px 22px rgba(255,182,193,0.15); transform: translateY(-2px); }
.moment-collage { margin-bottom: 12px; }
.moment-photos { display: grid; grid-template-columns: repeat(auto-fill, minmax(120px, 1fr)); gap: 6px; }
.moment-photo { width: 100%; height: 100px; object-fit: cover; border-radius: 10px; transition: transform 0.2s; }
.moment-photo:hover { transform: scale(1.05); }
.collage-placeholder { width: 100%; height: 140px; background: linear-gradient(135deg, #E3F2FD, #FFE0F0); border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.collage-emoji { font-size: 48px; }
.moment-title { font-size: 15px; font-weight: 600; text-align: center; margin-bottom: 8px; color: #5B4B3E; }
.moment-meta { display: flex; justify-content: center; gap: 12px; font-size: 12px; color: #c0a880; margin-bottom: 8px; }
.moment-actions { display: flex; justify-content: center; gap: 6px; }

/* Album */
.album-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.album-card { position: relative; padding: 0; overflow: hidden; border-radius: 16px; background: white; cursor: pointer; transition: all 0.2s; }
.album-card:hover { box-shadow: 0 8px 28px rgba(255,182,193,0.15); transform: translateY(-2px); }
.album-tape { width: 36px; height: 10px; background: #e8ddd0; border-radius: 2px; position: absolute; top: -5px; left: 50%; transform: translateX(-50%); z-index: 2; }
.album-cover { width: 100%; aspect-ratio: 1; display: flex; align-items: center; justify-content: center; position: relative; background: linear-gradient(135deg, #FFF8E1, #FFECB3); }
.cover-img { width: 100%; height: 100%; object-fit: cover; }
.photo-emoji { font-size: 42px; }
.album-private { position: absolute; top: 6px; right: 6px; background: rgba(255,182,193,0.85); color: #666; padding: 2px 8px; border-radius: 8px; font-size: 11px; }
.album-info { padding: 10px 10px 2px; text-align: center; }
.album-location { font-size: 13px; font-weight: 600; color: #5B4B3E; }
.album-date { font-size: 11px; color: #c0a880; margin-top: 2px; }
.album-actions { display: flex; justify-content: center; gap: 6px; padding: 6px 10px 10px; }
.create-card { border: 2px dashed #e0d5c5; background: linear-gradient(135deg, #FFFDF9, #FFF8F5); display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 180px; transition: all 0.2s; }
.create-card:hover { border-color: #FFB6C1; background: #FFF5F9; transform: translateY(-2px); }
.create-icon { width: 44px; height: 44px; border-radius: 50%; border: 2px solid #d0c8b8; display: flex; align-items: center; justify-content: center; font-size: 22px; color: #A0926B; margin-bottom: 8px; }
.create-text { font-size: 13px; color: #8B7E66; font-weight: 500; }

/* Upload */
.upload-area { margin-top: 4px; }
.upload-preview { position: relative; display: inline-block; }
.upload-photos { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
.photo-item { position: relative; }
.preview-thumb { width: 80px; height: 80px; object-fit: cover; border-radius: 10px; border: 2px solid #f0e6d8; }
.remove-btn { position: absolute; top: -6px; right: -6px; width: 20px; height: 20px; border-radius: 50%; background: #E53935; color: white; border: none; cursor: pointer; font-size: 11px; display: flex; align-items: center; justify-content: center; }
.upload-btn { display: inline-block; padding: 8px 16px; border-radius: 12px; border: 2px dashed #e0d5c5; cursor: pointer; font-size: 12px; color: #c9a87c; transition: all 0.2s; }
.upload-btn:hover { border-color: #FFB6C1; color: #FFB6C1; background: #FFF5F7; }
.upload-progress { margin-left: 8px; font-size: 12px; color: #E88D2E; }

/* Image preview overlay */
.image-preview-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.85); z-index: 2000; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.preview-img { max-width: 90vw; max-height: 90vh; border-radius: 12px; }

@media (max-width: 900px) { .album-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .album-grid { grid-template-columns: 1fr; } }
</style>
