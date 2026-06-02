import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: { guest: true } },
  { path: '/', name: 'home', component: () => import('../views/HomeView.vue'), meta: { requiresAuth: true } },
  { path: '/daily', name: 'daily', component: () => import('../views/DailyView.vue'), meta: { requiresAuth: true } },
  { path: '/calendar', name: 'calendar', component: () => import('../views/CalendarView.vue'), meta: { requiresAuth: true } },
  { path: '/wishlist', name: 'wishlist', component: () => import('../views/WishlistView.vue'), meta: { requiresAuth: true } },
  { path: '/memories', name: 'memories', component: () => import('../views/MemoriesView.vue'), meta: { requiresAuth: true } },
  { path: '/memories/album/:id', name: 'album-detail', component: () => import('../views/AlbumDetailView.vue'), meta: { requiresAuth: true } },
  { path: '/memories/moment/:id', name: 'moment-detail', component: () => import('../views/MomentDetailView.vue'), meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.guest && token) {
    next('/')
  } else {
    next()
  }
})

export default router
