<template>
  <div class="main-container">
    <!-- 返回按钮 -->
    <div class="back-button-container">
      <button class="back-btn" @click="goBack">
        <span class="back-icon">←</span>
        <span>返回数据集列表</span>
      </button>
    </div>
    
    <!-- 数据集头部信息 -->
    <div class="dataset-header">
      <h1 class="dataset-title">{{ datasetInfo.title }}</h1>
      <p class="dataset-subtitle">{{ datasetInfo.subtitle }}</p>
      
      <div class="dataset-meta-info">
        <div class="meta-item">
          <span class="meta-icon">📁</span>
          <div class="meta-content">
            <span class="meta-label">数据大小</span>
            <span class="meta-value">{{ datasetInfo.size }}</span>
          </div>
        </div>
        
        <div class="meta-item">
          <span class="meta-icon">📅</span>
          <div class="meta-content">
            <span class="meta-label">发布日期</span>
            <span class="meta-value">{{ datasetInfo.date }}</span>
          </div>
        </div>
        
        <div class="meta-item">
          <span class="meta-icon">👤</span>
          <div class="meta-content">
            <span class="meta-label">发布者</span>
            <span class="meta-value">{{ datasetInfo.author }}</span>
          </div>
        </div>
        
        <div class="meta-item">
          <span class="meta-icon">🏷️</span>
          <div class="meta-content">
            <span class="meta-label">状态</span>
            <div class="status-badge status-active">
              <span>✅</span>
              <span>{{ datasetInfo.status }}</span>
            </div>
          </div>
        </div>
        
        <div class="meta-item">
          <span class="meta-icon">📊</span>
          <div class="meta-content">
            <span class="meta-label">文件数量</span>
            <span class="meta-value">{{ datasetInfo.fileCount }}</span>
          </div>
        </div>
        
        <div class="meta-item">
          <span class="meta-icon">⬇️</span>
          <div class="meta-content">
            <span class="meta-label">下载次数</span>
            <span class="meta-value">{{ datasetInfo.downloadCount }}</span>
          </div>
        </div>
        
        <div class="meta-item">
          <span class="meta-icon">🔄</span>
          <div class="meta-content">
            <span class="meta-label">更新时间</span>
            <span class="meta-value">{{ datasetInfo.updatedTime }}</span>
          </div>
        </div>
        
        <div class="meta-item meta-tags" v-if="datasetInfo.taskNames">
          <span class="meta-icon">🏷️</span>
          <div class="meta-content">
            <span class="meta-label">类别</span>
            <div class="tags-container">
              <span v-for="tag in datasetInfo.taskNames.split(',')" :key="tag.trim()" class="tag-item">{{ tag.trim() }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容网格 -->
    <div class="content-grid">
      <!-- 左侧主要内容 -->
      <div class="main-content">
        <div class="content-card">
          <h2 class="card-title">
            <span class="card-icon">📖</span>
            数据集详情
          </h2>
          
          <div class="markdown-container">
            <div v-if="loading" class="loading-state">
              <div class="loading-spinner"></div>
              <p>正在加载数据集文档...</p>
            </div>
            
            <div v-else-if="error" class="error-state">
              <div class="error-icon">⚠️</div>
              <p>{{ error }}</p>
              <button @click="loadDatasetContent" class="retry-btn">重试</button>
            </div>
            
            <div v-else class="markdown-content" v-html="htmlContent"></div>
          </div>
        </div>
      </div>

      <!-- 右侧边栏 -->
      <div class="sidebar">
        <!-- 下载链接 -->
        <div class="content-card">
          <h2 class="card-title">
            <span class="card-icon">⬇️</span>
            下载数据集
          </h2>
          <div class="download-address" title="真实地址仅用于指引，不是下载链接">
            <div class="address-label">数据集真实地址</div>
            <div class="address-value">{{ datasetInfo.trueAddress || '暂无地址' }}</div>
          </div>
        </div>


      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'

export default {
  name: 'DatasetDetail',
  setup() {
    const route = useRoute()
    const loading = ref(true)
    const error = ref('')
    const htmlContent = ref('')
    
    const datasetInfo = ref({
      title: '',
      subtitle: '',
      size: '',
      date: '',
      author: '',
      status: '',
      fileCount: '',
      downloadCount: '',
      taskNames: '',
      updatedTime: '',
      trueAddress: ''
    })

    const downloadLinks = ref([])

    // 图片URL处理函数
    const getImageUrl = (datasetId, imagePath) => {
      // 如果已经是完整的API路径，直接返回
      if (imagePath.startsWith('datasets/') || imagePath.startsWith('http')) {
        return imagePath
      }
      
      // 构建API访问路径
      return `datasets/${datasetId}/images/${imagePath}`
    }

    // 处理HTML内容中的图片路径
    const processImagePaths = (htmlContent, datasetId) => {
      if (!htmlContent || !datasetId) return htmlContent
      
      // 使用正则表达式找到所有img标签的src属性
      return htmlContent.replace(/<img([^>]*?)src=["']([^"']*?)["']([^>]*?)>/gi, (match, before, src, after) => {
        // 如果src已经是API路径或完整URL，不做处理
        if (src.startsWith('datasets/') || src.startsWith('http')) {
          return match
        }
        
        // 生成新的API访问路径
        const newSrc = getImageUrl(datasetId, src)
        return `<img${before}src="${newSrc}"${after}>`
      })
    }

    // API调用函数 - 获取数据集HTML内容
    const getDatasetHtmlContent = async (datasetId) => {
      const token = localStorage.getItem('token')
      const url = `http://localhost:8080/datasets/${datasetId}/markdown-content`
      console.log('调用HTML内容API:', url)
      console.log('使用的token:', token ? '已设置' : '未设置')
      
      const response = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      console.log('API响应状态:', response.status)
      const result = await response.json()
      console.log('API响应结果:', result)
      
      if (result.code === 200) {
        return result.data
      } else {
        throw new Error(result.message)
      }
    }

    // API调用函数 - 获取数据集基本信息
    const getDatasetInfo = async (datasetId) => {
      const token = localStorage.getItem('token')
      const response = await fetch(`http://localhost:8080/datasets/${datasetId}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        console.log(result)
        return result.data.data
      } else {
        throw new Error(result.message)
      }
    }

    // API调用函数 - 获取数据集下载链接
    const getDatasetDownloadLinks = async (datasetId) => {
      const token = localStorage.getItem('token')
      const response = await fetch(`https://api.eavi.xidian.edu.cn/v1/datasets/${datasetId}/download-links`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      const result = await response.json()
      if (result.code === 200) {
        return result.data
      } else {
        throw new Error(result.message)
      }
    }

    // 加载数据集HTML内容和信息
    const loadDatasetContent = async () => {
      try {
        loading.value = true
        error.value = ''
        
        const datasetId = route.query.id || route.params.name || '1001' // 优先从query获取ID，其次从params获取，默认为1001
        console.log('正在加载数据集内容，ID:', datasetId)
        
        // 并行获取数据集基本信息和HTML内容
        const [basicInfo, htmlData] = await Promise.all([
          getDatasetInfo(datasetId),
          // getDatasetDownloadLinks(datasetId),
          getDatasetHtmlContent(datasetId),
          //getDatasetDownloadLinks(datasetId)
        ])
        
        console.log('获取到的基本信息:', basicInfo)
        console.log('获取到的HTML数据:', htmlData)
        
        // 更新数据集基本信息
        datasetInfo.value = {
          title: basicInfo.datasetName,
          subtitle: basicInfo.description,
          size: basicInfo.sizeFormatted,
          date: basicInfo.createdTime ? new Date(basicInfo.createdTime).toLocaleDateString() : '',
          author: basicInfo.name,
          status: basicInfo.status,
          fileCount: basicInfo.fileCount ? basicInfo.fileCount.toLocaleString() : '',
          downloadCount: basicInfo.downloadcount ? basicInfo.downloadcount.toLocaleString() : '',
          taskNames: basicInfo.taskNames || '',
          updatedTime: basicInfo.updatedTime ? new Date(basicInfo.updatedTime).toLocaleDateString() : '',
          trueAddress: basicInfo.trueAddress || ''
        }
        
        // 处理HTML内容中的图片路径并设置
        htmlContent.value = processImagePaths(htmlData.htmlContent, datasetId)
        
        // 设置下载链接为空数组（暂时）
        downloadLinks.value = []
        
      } catch (err) {
        console.error('加载数据集内容失败:', err)
        error.value = '无法加载数据集内容，请稍后重试'
      } finally {
        loading.value = false
      }
    }



    // 下载数据集
    const downloadDataset = (downloadUrl, title) => {
      if (downloadUrl) {
        // 使用API返回的下载链接
        window.open(downloadUrl, '_blank')
        console.log(`开始下载: ${title}`)
      } else {
        alert('下载链接不可用')
      }
    }

    // 图片错误处理函数
    const handleImageError = (event) => {
      const img = event.target
      img.alt = '图片加载失败'
      img.style.display = 'flex'
      img.style.alignItems = 'center'
      img.style.justifyContent = 'center'
      img.style.background = '#f8f9fa'
      img.style.border = '2px dashed #dee2e6'
      img.style.minHeight = '200px'
      img.style.color = '#6c757d'
      img.style.fontSize = '14px'
      console.warn('图片加载失败:', img.src)
    }

    // 为HTML内容中的图片添加错误处理
    const addImageErrorHandlers = () => {
      nextTick(() => {
        const images = document.querySelectorAll('.markdown-content img')
        images.forEach(img => {
          img.addEventListener('error', handleImageError)
          img.addEventListener('load', () => {
            console.log('图片加载成功:', img.src)
          })
        })
      })
    }

    // 返回上一页
    const goBack = () => {
      window.history.back()
    }

    onMounted(async () => {
      try {
        // 加载数据集内容和信息
        await loadDatasetContent()
        // 添加图片错误处理
        addImageErrorHandlers()
      } catch (err) {
        console.error('初始化页面失败:', err)
      }
    })

    return {
      loading,
      error,
      htmlContent,
      datasetInfo,
      downloadLinks,
      downloadDataset,
      loadDatasetContent,
      goBack,
      getImageUrl,
      processImagePaths,
      handleImageError,
      addImageErrorHandlers
    }
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.6;
  color: #333;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

/* 主容器 */
.main-container {
  max-width: 1800px;
  margin: 0 auto;
  padding: 30px 40px;
  min-height: 100vh;
}

/* 返回按钮容器 */
.back-button-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(102, 126, 234, 0.3);
  color: #667eea;
  padding: 12px 20px;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.1);
}

.back-btn:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.back-icon {
  font-size: 16px;
  font-weight: bold;
}

/* 数据集头部 */
.dataset-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px 50px;
  margin-bottom: 30px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  text-align: center;
  width: 102%;
}

.dataset-title {
  color: #2c3e50;
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.dataset-subtitle {
  color: #7f8c8d;
  font-size: 1.2rem;
  margin-bottom: 30px;
}

.dataset-meta-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 30px;
  margin-top: 30px;
  justify-content: center;
  align-items: start;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 15px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  text-align: left;
  justify-content: flex-start;
}

.meta-icon {
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 10px;
}

.meta-content {
  flex: 1;
}

.meta-label {
  display: block;
  font-size: 0.9rem;
  color: #7f8c8d;
  margin-bottom: 5px;
}

.meta-value {
  font-weight: 600;
  color: #2c3e50;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
}

.status-active {
  background: rgba(46, 204, 113, 0.1);
  color: #27ae60;
  border: 1px solid rgba(46, 204, 113, 0.3);
}

/* 标签样式 */
.meta-tags {
  grid-column: 1 / -1; /* 让标签占据整行 */
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
}

.tag-item {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 500;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
}

.tag-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 内容网格 */
.content-grid {
  display: grid;
  grid-template-columns: 3.5fr 1fr;
  gap: 25px;
}

/* 内容卡片 */
.content-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 10px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 30px;
  width: 110%;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 30px;
  padding-bottom: 18px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.card-icon {
  font-size: 1.3rem;
}

.download-address {
  padding: 16px 18px;
  border: 2px dashed rgba(102, 126, 234, 0.3);
  border-radius: 12px;
  background: rgba(102, 126, 234, 0.05);
}

.address-label {
  font-size: 0.95rem;
  color: #7f8c8d;
  margin-bottom: 8px;
}

.address-value {
  font-size: 1.05rem;
  font-weight: 600;
  color: #2c3e50;
}

/* Markdown容器 */
.markdown-container {
  min-height: 600px;
}

.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(102, 126, 234, 0.1);
  border-left: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 15px;
}

.retry-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 600;
  margin-top: 15px;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  background: #5a67d8;
  transform: translateY(-2px);
}

/* Markdown样式 - 针对HTML内容优化 */
.markdown-content {
  line-height: 1.8;
  color: #2c3e50;
  font-size: 16px;
}

.markdown-content :deep(h1) {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 30px 0 20px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  border-bottom: 3px solid rgba(102, 126, 234, 0.2);
  padding-bottom: 10px;
  line-height: 1.3;
}

.markdown-content :deep(h1:first-child) {
  margin-top: 0;
}

.markdown-content :deep(h2) {
  font-size: 2rem;
  font-weight: 600;
  margin: 25px 0 15px 0;
  color: #667eea;
  border-left: 4px solid #667eea;
  padding-left: 15px;
  line-height: 1.3;
}

.markdown-content :deep(h2:first-child) {
  margin-top: 0;
}

.markdown-content :deep(h3) {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 20px 0 10px 0;
  color: #764ba2;
  line-height: 1.3;
}

.markdown-content :deep(h3:first-child) {
  margin-top: 0;
}

.markdown-content :deep(h4) {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 18px 0 8px 0;
  color: #667eea;
  line-height: 1.3;
}

.markdown-content :deep(p) {
  margin: 15px 0;
  text-align: justify;
}

.markdown-content :deep(strong) {
  font-weight: 600;
  color: #2c3e50;
}

.markdown-content :deep(em) {
  font-style: italic;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 15px 0;
  padding-left: 30px;
}

.markdown-content :deep(li) {
  margin: 8px 0;
}

.markdown-content :deep(ul li) {
  list-style-type: disc;
}

.markdown-content :deep(ol li) {
  list-style-type: decimal;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #667eea;
  background: rgba(102, 126, 234, 0.05);
  padding: 15px 20px;
  margin: 20px 0;
  border-radius: 0 10px 10px 0;
  position: relative;
  font-style: italic;
}

.markdown-content :deep(blockquote::before) {
  content: '"';
  font-size: 4rem;
  color: rgba(102, 126, 234, 0.3);
  position: absolute;
  top: -10px;
  left: 10px;
  font-family: serif;
}

.markdown-content :deep(code) {
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', 'Monaco', 'Menlo', monospace;
  color: #667eea;
  font-weight: 600;
  font-size: 0.875em;
}

.markdown-content :deep(pre) {
  background: #2c3e50;
  color: #ecf0f1;
  padding: 20px;
  border-radius: 10px;
  overflow-x: auto;
  margin: 20px 0;
  position: relative;
  font-family: 'Courier New', 'Monaco', 'Menlo', monospace;
  font-size: 0.875em;
  line-height: 1.5;
}

.markdown-content :deep(pre::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 10px 10px 0 0;
}

/* 复制按钮样式 */
.markdown-content :deep(.copy-btn) {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(102, 126, 234, 0.9);
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
  z-index: 10;
}

.markdown-content :deep(.copy-btn:hover) {
  background: #667eea;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}

.markdown-content :deep(pre code) {
  background: none;
  padding: 0;
  color: inherit;
  font-weight: normal;
}

.markdown-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  padding: 15px 20px;
  text-align: left;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.markdown-content :deep(th) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-weight: 600;
}

.markdown-content :deep(tr:hover) {
  background: rgba(102, 126, 234, 0.05);
}

.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  margin: 20px 0;
  transition: transform 0.3s ease;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
}

.markdown-content :deep(img:hover) {
  transform: scale(1.02);
}

/* 图片加载状态 */
.markdown-content :deep(img[src=""]) {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.markdown-content :deep(img[src=""]):before {
  content: "加载中...";
  color: #666;
  font-size: 14px;
}

/* 图片加载失败处理 */
.markdown-content :deep(img[alt*="加载失败"]) {
  background: #f8f9fa;
  border: 2px dashed #dee2e6;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6c757d;
  font-size: 14px;
}

.markdown-content :deep(img[alt*="加载失败"]):before {
  content: "图片加载失败";
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.markdown-content :deep(a) {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
}

.markdown-content :deep(a:hover) {
  border-bottom-color: #667eea;
}

/* 下载链接 */
.download-links {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.download-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  background: rgba(102, 126, 234, 0.05);
  border: 2px solid rgba(102, 126, 234, 0.1);
  border-radius: 15px;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
}

.download-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  border-color: rgba(102, 126, 234, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.2);
}

.download-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.download-icon {
  font-size: 1.5rem;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 12px;
}

.download-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 5px;
}

.download-description {
  font-size: 0.9rem;
  color: #7f8c8d;
}

.download-size {
  font-weight: 600;
  color: #667eea;
  font-size: 1.1rem;
}



/* 暗色主题 */
.dark-theme {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
}

.dark-theme .back-btn {
  background: rgba(52, 73, 94, 0.95);
  border-color: rgba(236, 240, 241, 0.3);
  color: #ecf0f1;
}

.dark-theme .back-btn:hover {
  background: rgba(236, 240, 241, 0.9);
  color: #2c3e50;
}

.dark-theme .content-card {
  background: rgba(52, 73, 94, 0.95);
  color: #ecf0f1;
}

.dark-theme .card-title {
  color: #ecf0f1;
}

.dark-theme .dataset-header {
  background: rgba(52, 73, 94, 0.95);
  color: #ecf0f1;
}

.dark-theme .meta-item {
  background: rgba(44, 62, 80, 0.7);
}

.dark-theme .meta-value {
  color: #ecf0f1;
}

.dark-theme .download-btn {
  background: rgba(52, 73, 94, 0.5);
  border-color: rgba(236, 240, 241, 0.2);
  color: #ecf0f1;
}

.dark-theme .download-btn:hover {
  background: rgba(52, 73, 94, 0.8);
  border-color: rgba(236, 240, 241, 0.4);
}

.dark-theme .download-title {
  color: #ecf0f1;
}

.dark-theme .tag-item {
  background: linear-gradient(135deg, #34495e, #2c3e50);
  color: #ecf0f1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.dark-theme .tag-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
}



.dark-theme .markdown-content {
  color: #ecf0f1;
}

.dark-theme .markdown-content :deep(h1),
.dark-theme .markdown-content :deep(h2),
.dark-theme .markdown-content :deep(h3) {
  color: #ecf0f1;
}

.dark-theme .markdown-content :deep(.copy-btn) {
  background: rgba(236, 240, 241, 0.9);
  color: #2c3e50;
}

.dark-theme .markdown-content :deep(.copy-btn:hover) {
  background: #ecf0f1;
  color: #2c3e50;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .main-container {
    max-width: 1400px;
    padding: 30px 30px;
  }
  
  .dataset-header {
    padding: 40px 40px;
  }
  
  .content-grid {
    grid-template-columns: 3.2fr 1fr;
    gap: 25px;
  }
}

@media (max-width: 1200px) {
  .main-container {
    max-width: 1200px;
    padding: 25px 25px;
  }
  
  .dataset-header {
    padding: 35px 35px;
  }
  
  .content-grid {
    grid-template-columns: 3fr 1fr;
    gap: 20px;
  }
  
  .dataset-meta-info {
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 25px;
  }
}

@media (max-width: 1024px) {
  .main-container {
    max-width: 1000px;
    padding: 25px 20px;
  }
  
  .dataset-header {
    padding: 30px 30px;
  }
  
  .content-grid {
    grid-template-columns: 2fr 1fr;
    gap: 25px;
  }
  
  .content-card {
    padding: 30px;
  }
}

@media (max-width: 768px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .dataset-meta-info {
    grid-template-columns: 1fr;
  }
  
  .dataset-title {
    font-size: 2rem;
  }
  
  .main-container {
    padding: 20px 15px;
  }
  
  .content-card {
    padding: 20px;
  }
  
  .dataset-header {
    padding: 25px 20px;
  }
  
  .markdown-content :deep(h1) {
    font-size: 2rem;
  }
  
  .markdown-content :deep(h2) {
    font-size: 1.5rem;
  }
  
  .markdown-content :deep(table) {
    font-size: 0.9rem;
  }
  
  .markdown-content :deep(th),
  .markdown-content :deep(td) {
    padding: 10px 15px;
  }
}
</style>
