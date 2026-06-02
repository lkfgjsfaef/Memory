<template>
  <div class="music-player" :class="{ collapsed: !showPanel }">
    <button class="toggle-btn" @click="togglePanel" :title="showPanel ? '收起' : '展开播放器'">
      {{ isPlaying ? '🎶' : '🎵' }}
    </button>

    <div v-show="showPanel" class="player-panel">
      <!-- Search -->
      <div class="search-bar">
        <input
          v-model="searchKeyword"
          placeholder="搜索歌曲..."
          class="search-input"
          @keyup.enter="doSearch"
        />
        <button class="search-btn" @click="doSearch" :disabled="searching">🔍</button>
      </div>

      <!-- Search Results -->
      <div v-if="searchResults.length > 0" class="search-results">
        <div class="results-header">
          <span>搜索结果</span>
          <button class="clear-btn" @click="searchResults = []; searchKeyword = ''">✕</button>
        </div>
        <div v-for="song in searchResults" :key="song.id" class="result-item">
          <div class="result-info">
            <div class="result-name">{{ song.name }}</div>
            <div class="result-artist">{{ song.artist }}</div>
          </div>
          <button class="add-btn" @click="addToPlaylist(song)" :disabled="isInPlaylist(song.id)">+</button>
        </div>
      </div>

      <!-- Searching -->
      <div v-if="searching" class="searching-text">搜索中...</div>

      <!-- My Playlist -->
      <div class="playlist-section">
        <div class="playlist-header">
          <span>我的歌单 ({{ playlist.length }})</span>
          <button class="mode-toggle" @click="togglePlayMode" :title="playMode === 'sequential' ? '顺序播放' : '随机播放'">
            {{ playMode === 'sequential' ? '🔁' : '🔀' }}
          </button>
        </div>
        <div v-if="playlist.length === 0" class="empty-hint">搜索歌曲并添加到歌单</div>
        <div
          v-for="(song, i) in playlist"
          :key="song.id"
          draggable="true"
          :class="['playlist-item', { active: currentIndex === i, dragging: dragIndex === i }]"
          @click="playSong(i)"
          @dragstart="onDragStart(i, $event)"
          @dragover.prevent="onDragOver(i, $event)"
          @drop="onDrop(i)"
          @dragend="onDragEnd"
        >
          <span class="drag-handle" @mousedown.stop>⠿</span>
          <img v-if="song.picUrl" :src="song.picUrl" class="song-thumb" />
          <span v-else class="song-thumb-placeholder">🎶</span>
          <div class="song-info">
            <div class="song-name">{{ song.name }}</div>
            <div class="song-artist">{{ song.artist }}</div>
          </div>
          <button class="remove-song-btn" @click.stop="removeSong(i)">✕</button>
        </div>
      </div>

      <!-- Mini Player -->
      <div v-if="currentSong" class="mini-player">
        <div class="now-playing">
          <span class="np-icon" :class="{ playing: isPlaying }">🎧</span>
          <span>{{ currentSong.name }} - {{ currentSong.artist }}</span>
        </div>
        <div class="player-controls">
          <button class="ctrl-btn" @click="playPrev" title="上一首">⏮</button>
          <button class="ctrl-btn ctrl-play" @click="togglePlay" :title="isPlaying ? '暂停' : '播放'">
            {{ isPlaying ? '⏸' : '▶' }}
          </button>
          <button class="ctrl-btn" @click="playNext" title="下一首">⏭</button>
          <button class="mode-toggle-inline" @click="togglePlayMode" :title="playMode === 'sequential' ? '顺序播放' : '随机播放'">
            {{ playMode === 'sequential' ? '🔁' : '🔀' }}
          </button>
          <span class="time-display">{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span>
        </div>
        <div class="progress-wrap" @click="seek">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
          </div>
        </div>
        <audio
          ref="audioRef"
          :src="audioSrc"
          @timeupdate="onTimeUpdate"
          @ended="onAudioEnded"
          @loadedmetadata="onLoaded"
          @play="isPlaying = true"
          @pause="isPlaying = false"
          @error="onAudioError"
        ></audio>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { searchMusic, getPlaylist, savePlaylist } from '../data/api.js'

const showPanel = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const searching = ref(false)
const currentIndex = ref(-1)
const isPlaying = ref(false)
const playMode = ref('sequential')
const playlist = ref([])
const dragIndex = ref(-1)
const dragOverIndex = ref(-1)
const playedStack = ref([])

const audioRef = ref(null)
const currentTime = ref(0)
const duration = ref(0)
const audioSrc = ref('')

const currentSong = computed(() => {
  if (currentIndex.value >= 0 && currentIndex.value < playlist.value.length) {
    return playlist.value[currentIndex.value]
  }
  return null
})

const progressPercent = computed(() => {
  if (duration.value <= 0) return 0
  return Math.min(100, (currentTime.value / duration.value) * 100)
})

function getAudioUrl(song) {
  if (!song) return ''
  return `https://music.163.com/song/media/outer/url?id=${song.id}.mp3`
}

async function loadPlaylist() {
  try {
    const data = await getPlaylist()
    if (data && data.length > 0) return data
  } catch (e) { /* fallback to localStorage */ }
  try {
    const saved = localStorage.getItem('music_playlist')
    return saved ? JSON.parse(saved) : []
  } catch { return [] }
}

async function syncToBackend() {
  try {
    await savePlaylist(playlist.value)
    localStorage.removeItem('music_playlist')
  } catch {
    localStorage.setItem('music_playlist', JSON.stringify(playlist.value))
  }
}

function isInPlaylist(id) {
  return playlist.value.some(s => s.id === id)
}

async function doSearch() {
  const kw = searchKeyword.value.trim()
  if (!kw || searching.value) return
  searching.value = true
  searchResults.value = []
  try {
    const list = await searchMusic(kw)
    searchResults.value = list || []
  } catch {
    searchResults.value = []
  } finally {
    searching.value = false
  }
}

function playAudio() {
  nextTick(() => {
    const a = audioRef.value
    if (a) {
      a.play().catch(() => {})
    }
  })
}

function setSong(index) {
  currentIndex.value = index
  currentTime.value = 0
  duration.value = (currentSong.value?.duration || 0) / 1000
  audioSrc.value = getAudioUrl(currentSong.value)
  playAudio()
}

function addToPlaylist(song) {
  if (!isInPlaylist(song.id)) {
    playlist.value.push({ ...song })
    syncToBackend()
    if (currentIndex.value === -1) {
      setSong(0)
    }
  }
}

function removeSong(index) {
  const wasCurrent = currentIndex.value === index
  playlist.value.splice(index, 1)
  syncToBackend()
  playedStack.value = playedStack.value.filter(i => i !== index).map(i => i > index ? i - 1 : i)
  if (playlist.value.length === 0) {
    currentIndex.value = -1
    audioSrc.value = ''
    isPlaying.value = false
    return
  }
  if (currentIndex.value >= playlist.value.length) {
    currentIndex.value = playlist.value.length - 1
  }
  if (wasCurrent) {
    if (index <= currentIndex.value && currentIndex.value > 0) currentIndex.value--
    if (currentIndex.value < 0) currentIndex.value = 0
    setSong(currentIndex.value)
  } else if (currentIndex.value > index) {
    currentIndex.value--
  }
}

function playSong(index) {
  if (currentIndex.value === index) {
    // Restart current song
    const a = audioRef.value
    if (a) { a.currentTime = 0; a.play().catch(() => {}) }
    return
  }
  setSong(index)
}

function togglePanel() {
  showPanel.value = !showPanel.value
  if (showPanel.value && playlist.value.length > 0 && currentIndex.value < 0) {
    currentIndex.value = 0
  }
}

function togglePlay() {
  const a = audioRef.value
  if (!a) {
    if (currentSong.value) setSong(currentIndex.value)
    return
  }
  if (isPlaying.value) {
    a.pause()
  } else {
    if (!a.src || a.src === window.location.href) {
      setSong(currentIndex.value)
      return
    }
    a.play().catch(() => {})
  }
}

function togglePlayMode() {
  playMode.value = playMode.value === 'sequential' ? 'random' : 'sequential'
}

function playNext() {
  if (playlist.value.length === 0) return
  if (playMode.value === 'random') {
    setSong(getRandomNextIndex())
  } else {
    setSong((currentIndex.value + 1) % playlist.value.length)
  }
}

function getRandomNextIndex() {
  if (playlist.value.length === 1) return 0
  const candidates = []
  for (let i = 0; i < playlist.value.length; i++) {
    if (!playedStack.value.includes(i)) candidates.push(i)
  }
  if (candidates.length === 0) {
    playedStack.value = []
    for (let i = 0; i < playlist.value.length; i++) {
      if (i !== currentIndex.value) candidates.push(i)
    }
  }
  const pick = candidates[Math.floor(Math.random() * candidates.length)]
  playedStack.value.push(pick)
  if (playedStack.value.length > playlist.value.length) playedStack.value.shift()
  return pick
}

function playPrev() {
  if (playlist.value.length === 0) return
  if (playMode.value === 'random') {
    setSong(getRandomNextIndex())
  } else {
    setSong(currentIndex.value <= 0 ? playlist.value.length - 1 : currentIndex.value - 1)
  }
}

function onTimeUpdate() {
  if (audioRef.value) {
    currentTime.value = audioRef.value.currentTime
    if (audioRef.value.duration && isFinite(audioRef.value.duration)) {
      duration.value = audioRef.value.duration
    }
  }
}

function onLoaded() {
  if (audioRef.value && audioRef.value.duration && isFinite(audioRef.value.duration)) {
    duration.value = audioRef.value.duration
  }
}

function onAudioEnded() {
  playNext()
}

function onAudioError() {
  // Some songs may be unavailable due to copyright
  isPlaying.value = false
}

function formatTime(secs) {
  if (!secs || !isFinite(secs)) return '0:00'
  const m = Math.floor(secs / 60)
  const s = Math.floor(secs % 60)
  return `${m}:${String(s).padStart(2, '0')}`
}

function seek(e) {
  if (!audioRef.value || duration.value <= 0) return
  const rect = e.currentTarget.getBoundingClientRect()
  const pct = (e.clientX - rect.left) / rect.width
  audioRef.value.currentTime = pct * duration.value
}

// Drag and drop
function onDragStart(i, e) {
  dragIndex.value = i
  e.dataTransfer.effectAllowed = 'move'
  e.dataTransfer.setData('text/plain', String(i))
}
function onDragOver(i, e) {
  e.dataTransfer.dropEffect = 'move'
  dragOverIndex.value = i
}
function onDrop(targetIndex) {
  if (dragIndex.value < 0 || dragIndex.value === targetIndex) return
  const item = playlist.value.splice(dragIndex.value, 1)[0]
  playlist.value.splice(targetIndex, 0, item)
  syncToBackend()
  if (currentIndex.value === dragIndex.value) {
    currentIndex.value = targetIndex
  } else if (dragIndex.value < currentIndex.value && targetIndex >= currentIndex.value) {
    currentIndex.value--
  } else if (dragIndex.value > currentIndex.value && targetIndex <= currentIndex.value) {
    currentIndex.value++
  }
  dragIndex.value = -1
  dragOverIndex.value = -1
}
function onDragEnd() {
  dragIndex.value = -1
  dragOverIndex.value = -1
}

onMounted(() => {
  // Load from localStorage immediately (no API call, no risk)
  try {
    const saved = localStorage.getItem('music_playlist')
    if (saved) playlist.value = JSON.parse(saved)
  } catch {}
  if (playlist.value.length > 0 && currentIndex.value < 0) {
    currentIndex.value = 0
  }
  // Override with backend data (global shared playlist, no auth needed for reads)
  loadPlaylist().then(data => {
    if (data && data.length > 0) playlist.value = data
  }).catch(() => {})
})

onUnmounted(() => {
  if (audioRef.value) {
    audioRef.value.pause()
    audioRef.value.src = ''
  }
})
</script>

<style scoped>
.music-player {
  position: fixed;
  top: 90px;
  left: 16px;
  z-index: 999;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}

.toggle-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFA500, #FFB6C1);
  color: white;
  border: none;
  font-size: 18px;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(255, 165, 0, 0.3);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 18px rgba(255, 165, 0, 0.4);
}

.player-panel {
  width: 300px;
  max-height: calc(100vh - 140px);
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.search-bar {
  display: flex;
  padding: 10px;
  gap: 6px;
  border-bottom: 1px solid #f0e6d8;
}

.search-input {
  flex: 1;
  padding: 7px 12px;
  border-radius: 12px;
  border: 1.5px solid #e8e0d8;
  font-size: 12px;
  outline: none;
  color: #5B4B3E;
}

.search-input:focus { border-color: #FFB6C1; }

.search-btn {
  background: #FFE8D0;
  border: none;
  border-radius: 12px;
  padding: 6px 10px;
  cursor: pointer;
  font-size: 14px;
}

.search-btn:hover { background: #FFD0A0; }

.search-results {
  max-height: 180px;
  overflow-y: auto;
  border-bottom: 1px solid #f0e6d8;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 12px;
  font-size: 11px;
  color: #c0a880;
  background: #FFFDF9;
}

.clear-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 12px;
  color: #ccc;
}

.clear-btn:hover { color: #E53935; }

.result-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  gap: 8px;
  transition: background 0.15s;
}

.result-item:hover { background: #FFFDF9; }

.result-info { flex: 1; min-width: 0; }

.result-name {
  font-size: 12px;
  font-weight: 600;
  color: #5B4B3E;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-artist {
  font-size: 11px;
  color: #c0a880;
}

.add-btn {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #FFE8D0;
  color: #E88D2E;
  border: none;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.add-btn:hover { background: #FFD0A0; }
.add-btn:disabled { background: #f0f0f0; color: #ccc; cursor: default; }

.searching-text {
  text-align: center;
  padding: 12px;
  font-size: 12px;
  color: #c0a880;
}

.playlist-section {
  max-height: 140px;
  overflow-y: auto;
}

.playlist-header {
  padding: 8px 12px;
  font-size: 12px;
  font-weight: 600;
  color: #E88D2E;
  background: #FFFDF9;
  border-bottom: 1px solid #f0e6d8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mode-toggle {
  background: none;
  border: none;
  font-size: 14px;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 8px;
  transition: background 0.2s;
}

.mode-toggle:hover { background: #FFE8D0; }

.empty-hint {
  padding: 16px;
  text-align: center;
  font-size: 12px;
  color: #c9a87c;
}

.playlist-item {
  display: flex;
  align-items: center;
  padding: 7px 12px;
  gap: 8px;
  cursor: pointer;
  transition: background 0.15s;
}

.playlist-item:hover { background: #FFFDF9; }
.playlist-item.active { background: #FFF5EC; }
.playlist-item.active .song-name { color: #E88D2E; }
.playlist-item.dragging { opacity: 0.4; }

.drag-handle {
  cursor: grab;
  font-size: 12px;
  color: #ccc;
  flex-shrink: 0;
  user-select: none;
}

.drag-handle:active { cursor: grabbing; }

.song-thumb {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}

.song-thumb-placeholder {
  font-size: 18px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.song-info { flex: 1; min-width: 0; }

.song-name {
  font-size: 12px;
  font-weight: 500;
  color: #5B4B3E;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-artist {
  font-size: 11px;
  color: #c0a880;
}

.remove-song-btn {
  background: none;
  border: none;
  font-size: 12px;
  color: #ddd;
  cursor: pointer;
  flex-shrink: 0;
}

.remove-song-btn:hover { color: #E53935; }

.mini-player {
  padding: 8px;
  border-top: 1px solid #f0e6d8;
  background: #FFFDF9;
}

.now-playing {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #8B7355;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.np-icon { font-size: 14px; transition: color 0.3s; }
.np-icon.playing { color: #E88D2E; }

.player-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.ctrl-btn {
  background: none;
  border: 1.5px solid #e8e0d8;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  color: #8B7355;
  flex-shrink: 0;
}

.ctrl-btn:hover { border-color: #FFB6C1; background: #FFF5F7; }

.ctrl-play {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #FFA500, #FFB6C1);
  border: none;
  color: white;
  font-size: 14px;
}

.ctrl-play:hover { background: linear-gradient(135deg, #FFB680, #FFD0D8); }

.mode-toggle-inline {
  background: none;
  border: none;
  font-size: 14px;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 8px;
  transition: background 0.2s;
  flex-shrink: 0;
}

.mode-toggle-inline:hover { background: #FFE8D0; }

.time-display {
  font-size: 10px;
  color: #c0a880;
  margin-left: auto;
  white-space: nowrap;
}

.progress-wrap {
  padding: 0 2px;
  cursor: pointer;
  height: 14px;
  display: flex;
  align-items: center;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background: #f0e6d8;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #FFB6C1, #FFA500);
  border-radius: 2px;
  transition: width 0.15s linear;
}

audio {
  display: none;
}

@media (max-width: 600px) {
  .player-panel { width: 260px; }
}
</style>
