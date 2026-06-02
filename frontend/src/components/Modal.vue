<template>
  <div v-if="visible" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-container">
      <div class="modal-header">
        <h3 class="modal-title">{{ title }}</h3>
        <button class="modal-close" @click="$emit('close')">&times;</button>
      </div>
      <div class="modal-body">
        <slot />
      </div>
      <div class="modal-footer">
        <button class="btn-cancel" @click="$emit('close')">取消</button>
        <button class="btn-primary" @click="$emit('confirm')">{{ confirmText }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  visible: Boolean,
  title: String,
  confirmText: { type: String, default: '确定' }
})
defineEmits(['close', 'confirm'])
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-container {
  background: #FFFDF7;
  border-radius: 16px;
  padding: 24px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.modal-title {
  font-size: 18px;
  font-weight: 700;
}
.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--text-light);
  padding: 0;
  line-height: 1;
}
.modal-body {
  margin-bottom: 20px;
}
.modal-body :deep(input),
.modal-body :deep(textarea),
.modal-body :deep(select) {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e0d8c8;
  border-radius: 8px;
  font-size: 14px;
  font-family: var(--font-family);
  background: white;
  margin-bottom: 12px;
  box-sizing: border-box;
}
.modal-body :deep(textarea) {
  resize: vertical;
  min-height: 80px;
}
.modal-body :deep(label) {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-gray);
  margin-bottom: 4px;
}
.modal-body :deep(.form-row) {
  display: flex;
  gap: 12px;
}
.modal-body :deep(.form-row > *) {
  flex: 1;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.btn-cancel {
  padding: 10px 20px;
  border-radius: 8px;
  border: 1px solid #e0d8c8;
  background: white;
  font-size: 14px;
  cursor: pointer;
  font-family: var(--font-family);
}
.btn-cancel:hover {
  background: #f5f0e8;
}
</style>
