<template>
  <div class="toast-container">
    <TransitionGroup name="toast">
      <div
        v-for="t in toasts"
        :key="t.id"
        class="toast"
        :class="'toast-' + t.type"
        @click="toasts = toasts.filter(x => x.id !== t.id)"
      >
        <span class="toast-icon">{{ icons[t.type] }}</span>
        <span class="toast-msg">{{ t.message }}</span>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { useToast } from '../composables/useToast.js'

const { toasts } = useToast()
const icons = { success: '✓', error: '✕', info: 'ℹ', warning: '⚠' }
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  gap: 8px;
  pointer-events: none;
}
.toast {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  min-width: 200px;
  max-width: 360px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
}
.toast-success { background: #E8F5E9; color: #2E7D32; }
.toast-error   { background: #FFEBEE; color: #C62828; }
.toast-info    { background: #E3F2FD; color: #1565C0; }
.toast-warning { background: #FFF3E0; color: #E65100; }
.toast-icon { font-size: 16px; font-weight: 700; }
.toast-msg { flex: 1; }
.toast-enter-active { transition: all 0.3s ease; }
.toast-leave-active { transition: all 0.2s ease; }
.toast-enter-from { opacity: 0; transform: translateX(40px); }
.toast-leave-to   { opacity: 0; transform: translateX(40px); }
</style>
