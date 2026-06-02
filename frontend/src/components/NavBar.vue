<template>
  <nav class="navbar">
    <div class="navbar-inner">
      <router-link
        v-for="tab in tabs"
        :key="tab.name"
        :to="tab.path"
        class="nav-tab"
        :class="{ active: currentRoute === tab.name }"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span class="tab-label">{{ tab.label }}</span>
      </router-link>
      <div class="nav-right">
        <div class="user-badge" @click="toggleMenu" v-click-outside="closeMenu">
          <img v-if="userStore.state.avatarUrl" :src="userStore.state.avatarUrl" class="user-avatar-img" />
          <span v-else class="user-avatar-emoji">🐻</span>
          <span class="user-name">{{ userStore.state.nickname || '未登录' }}</span>
        </div>
        <div v-if="menuOpen" class="user-menu">
          <div class="menu-item" @click="handleLogout">退出登录</div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/userStore.js'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const currentRoute = computed(() => route.name)
const menuOpen = ref(false)

const tabs = [
  { name: 'home', label: '首页', icon: '🏠', path: '/' },
  { name: 'daily', label: '日常', icon: '📔', path: '/daily' },
  { name: 'calendar', label: '日历', icon: '📅', path: '/calendar' },
  { name: 'wishlist', label: '心愿', icon: '⭐', path: '/wishlist' },
  { name: 'memories', label: '回忆', icon: '💕', path: '/memories' }
]

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function closeMenu() {
  menuOpen.value = false
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

const vClickOutside = {
  mounted(el, binding) {
    el._clickOutside = (event) => {
      if (!el.contains(event.target)) {
        binding.value()
      }
    }
    document.addEventListener('click', el._clickOutside)
  },
  unmounted(el) {
    document.removeEventListener('click', el._clickOutside)
  }
}
</script>

<style scoped>
.navbar {
  background: var(--bg-primary);
  padding: 12px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #f0e8d8;
}

.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 20px;
  position: relative;
}

.nav-tab {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 20px;
  border-radius: 20px;
  background: white;
  border: 1.5px solid #e8e0d8;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-dark);
  transition: all 0.3s ease;
  text-decoration: none;
}

.nav-tab:hover {
  border-color: var(--color-orange-light);
  background: #FFF8F0;
}

.nav-tab.active {
  background: var(--color-orange-light);
  border-color: var(--color-orange);
  color: #8B4513;
  font-weight: 600;
}

.tab-icon {
  font-size: 15px;
}

.nav-right {
  position: absolute;
  right: 20px;
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 20px;
  background: white;
  border: 1.5px solid #e8e0d8;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-badge:hover {
  border-color: #FFB6C1;
  background: #FFF5F7;
}

.user-avatar-img {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.user-avatar-emoji {
  font-size: 20px;
}

.user-name {
  font-size: 13px;
  color: #555;
  font-weight: 500;
}

.user-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  overflow: hidden;
  min-width: 120px;
}

.menu-item {
  padding: 10px 20px;
  font-size: 14px;
  color: #E53935;
  cursor: pointer;
  transition: background 0.2s;
}

.menu-item:hover {
  background: #FFF5F5;
}
</style>
