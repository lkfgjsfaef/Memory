import { ref } from 'vue'

const toasts = ref([])
let nextId = 0

export function useToast() {
  function show(message, type = 'info', duration = 3000) {
    const id = nextId++
    toasts.value.push({ id, message, type })
    if (duration > 0) {
      setTimeout(() => remove(id), duration)
    }
  }

  function remove(id) {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }

  return {
    toasts,
    toast: {
      success: (msg) => show(msg, 'success'),
      error: (msg) => show(msg, 'error', 5000),
      info: (msg) => show(msg, 'info'),
      warning: (msg) => show(msg, 'warning', 4000)
    }
  }
}
