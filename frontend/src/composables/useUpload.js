import { ref } from 'vue'
import * as qiniu from 'qiniu-js'
import { getQiniuToken } from '../data/api.js'
import { compressImage } from './useImageCompress.js'

export function useUpload() {
  const uploading = ref(false)
  const progress = ref(0)

  async function uploadImage(file) {
    uploading.value = true
    progress.value = 0
    try {
      const compressed = await compressImage(file)
      const { token, domain } = await getQiniuToken()
      const ext = compressed.name.split('.').pop() || 'jpg'
      const key = `memory/${Date.now()}_${Math.random().toString(36).slice(2, 8)}.${ext}`
      const config = { useCdnDomain: true, region: qiniu.region.as0 }
      const observable = qiniu.upload(compressed, key, token, {}, config)
      return new Promise((resolve, reject) => {
        observable.subscribe({
          next: (res) => { progress.value = Math.round(res.total.percent) },
          error: (err) => {
            console.error('Qiniu upload error:', err)
            uploading.value = false
            reject(err)
          },
          complete: (res) => {
            uploading.value = false
            resolve(domain + '/' + res.key)
          }
        })
      })
    } catch (e) {
      uploading.value = false
      throw e
    }
  }

  return { uploading, progress, uploadImage }
}
