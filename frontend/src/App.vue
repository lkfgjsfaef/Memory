<template>
  <div id="app">
    <NavBar v-if="route.name !== 'login'" />
    <main class="main-content" :class="{ 'no-nav': route.name === 'login' }">
      <router-view v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" />
        </Transition>
      </router-view>
    </main>
    <Toast />
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import NavBar from './components/NavBar.vue'
import Toast from './components/Toast.vue'

const route = useRoute()
</script>

<style>
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.main-content.no-nav {
  padding: 0;
}

.page-enter-active,
.page-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
