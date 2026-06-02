import { ref } from 'vue'
import { useToast } from './useToast.js'

export function useApi(apiFunc) {
  const loading = ref(false)
  const error = ref(null)
  const data = ref(null)
  const { toast } = useToast()

  async function execute(...args) {
    loading.value = true
    error.value = null
    try {
      const result = await apiFunc(...args)
      data.value = result
      return result
    } catch (e) {
      const msg = e.response?.data?.message || e.message || '请求失败'
      error.value = msg
      toast.error(msg)
      throw e
    } finally {
      loading.value = false
    }
  }

  return { loading, error, data, execute }
}

export function useConfirmDelete(deleteFn, { toast }) {
  async function handleDelete(id, message = '确定要删除吗？') {
    if (!window.confirm(message)) return
    try {
      await deleteFn(id)
      toast.success('删除成功')
      return true
    } catch (e) {
      toast.error('删除失败: ' + (e.response?.data?.message || e.message))
      return false
    }
  }
  return { handleDelete }
}

export function useSave(saveFn, { toast }) {
  const saving = ref(false)

  async function handleSave(form, isEdit, loadFn) {
    saving.value = true
    try {
      await saveFn(form)
      toast.success(isEdit ? '修改成功' : '添加成功')
      loadFn?.()
      return true
    } catch (e) {
      toast.error('保存失败: ' + (e.response?.data?.message || e.message))
      return false
    } finally {
      saving.value = false
    }
  }
  return { saving, handleSave }
}
