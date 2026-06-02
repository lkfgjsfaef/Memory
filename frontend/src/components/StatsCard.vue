<template>
  <div class="stats-card" :style="{ backgroundColor: bgColor }">
    <div class="stats-number">{{ number }}</div>
    <div class="stats-label">{{ label }}</div>
  </div>
</template>

<script setup>
defineProps({
  number: { type: [String, Number], required: true },
  label: { type: String, required: true },
  bgColor: { type: String, default: '#FFF5D9' }
})
</script>

<style scoped>
.stats-card {
  border-radius: var(--radius-md);
  padding: 20px 28px;
  text-align: center;
  box-shadow: var(--shadow-card);
  min-width: 140px;
  flex: 1;
}

.stats-number {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-dark);
  line-height: 1.2;
}

.stats-label {
  font-size: 13px;
  color: var(--text-gray);
  margin-top: 4px;
}
</style>
