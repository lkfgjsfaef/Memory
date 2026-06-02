/**
 * Compress an image file using Canvas — reduces upload size by 80-90%.
 * Modern phone photos are 5-10MB; this brings them to ~200-500KB.
 */
export function compressImage(file, maxWidth = 1920, maxHeight = 1920, quality = 0.8) {
  return new Promise((resolve, reject) => {
    // Skip non-image files or small files (< 500KB)
    if (!file.type.startsWith('image/')) return resolve(file)
    if (file.size < 500 * 1024) return resolve(file)

    const img = new Image()
    const url = URL.createObjectURL(file)

    img.onload = () => {
      URL.revokeObjectURL(url)

      let { width, height } = img
      // Only resize if image exceeds max dimensions
      if (width <= maxWidth && height <= maxHeight && file.size < 1024 * 1024) {
        return resolve(file)
      }

      if (width > maxWidth) {
        height = Math.round(height * (maxWidth / width))
        width = maxWidth
      }
      if (height > maxHeight) {
        width = Math.round(width * (maxHeight / height))
        height = maxHeight
      }

      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, width, height)

      canvas.toBlob(
        (blob) => {
          if (blob) {
            const compressed = new File([blob], file.name, { type: 'image/jpeg', lastModified: Date.now() })
            resolve(compressed)
          } else {
            resolve(file) // fallback to original
          }
        },
        'image/jpeg',
        quality
      )
    }

    img.onerror = () => {
      URL.revokeObjectURL(url)
      resolve(file) // fallback to original
    }

    img.src = url
  })
}
