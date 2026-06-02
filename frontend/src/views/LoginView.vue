<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-hearts">💕</div>
        <h1>Memory</h1>
        <p>珍藏每一个闪亮的瞬间</p>
      </div>
      <div class="login-form">
        <div class="form-group">
          <label>账号</label>
          <div class="user-select">
            <button
              v-for="user in users"
              :key="user.username"
              :class="['user-option', { active: form.username === user.username }]"
              @click="form.username = user.username"
            >
              <span class="user-avatar">{{ user.avatar }}</span>
              <span>{{ user.nickname }}</span>
            </button>
          </div>
        </div>
        <div class="form-group">
          <label>密码</label>
          <input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          />
        </div>
        <button class="btn-login" :disabled="loading" @click="handleLogin">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
        <p v-if="error" class="error-msg">{{ error }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../data/api.js'
import { useUserStore } from '../stores/userStore.js'

const router = useRouter()
const userStore = useUserStore()

const users = [
  { username: 'jiangjiang', nickname: '酱酱', avatar: '👦' },
  { username: 'feifei', nickname: '菲菲', avatar: '👧' }
]

const form = reactive({ username: 'jiangjiang', password: '' })
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  if (!form.username || !form.password) {
    error.value = '请选择账号并输入密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const data = await login(form.username, form.password)
    userStore.login(data)
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.message || '登录失败，请检查密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFF5D9 0%, #F9E7F1 50%, #D4F0FF 100%);
}

.login-card {
  background: white;
  border-radius: 24px;
  padding: 48px 40px;
  width: 400px;
  max-width: 90vw;
  box-shadow: 0 8px 40px rgba(0,0,0,0.06);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-hearts {
  font-size: 48px;
  margin-bottom: 8px;
}

.login-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0 0 8px 0;
  font-weight: 700;
}

.login-header p {
  color: #999;
  font-size: 14px;
  margin: 0;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 13px;
  color: #888;
  margin-bottom: 8px;
}

.user-select {
  display: flex;
  gap: 12px;
}

.user-option {
  flex: 1;
  padding: 14px 16px;
  border: 2px solid #e8e0d8;
  border-radius: 16px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #555;
}

.user-option:hover {
  border-color: #FFB6C1;
  background: #FFF5F7;
}

.user-option.active {
  border-color: #FFA500;
  background: #FFF8F0;
  color: #8B4513;
  font-weight: 600;
}

.user-avatar {
  font-size: 24px;
}

.form-group input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e8e0d8;
  border-radius: 16px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.3s;
  box-sizing: border-box;
  color: #333;
}

.form-group input:focus {
  border-color: #FFB6C1;
}

.btn-login {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #FFA500, #FFB6C1);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
}

.btn-login:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(255,165,0,0.3);
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-msg {
  color: #E53935;
  font-size: 13px;
  text-align: center;
  margin-top: 12px;
}
</style>
