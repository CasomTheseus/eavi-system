<template>
  <div class="dataset-page">
    <!-- 浮动装饰元素 -->
    <div class="floating-elements">
      <div class="floating-book">📖</div>
      <div class="floating-book">📚</div>
      <div class="floating-book">📓</div>
      <div class="floating-book">📔</div>
    </div>

    <!-- 主要内容 -->
    <div class="main-container">
      <!-- 书脊 -->
      <div class="book-spine">数据集</div>
      
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <div class="sidebar-section">
          <div class="sidebar-title">筛选</div>
          <div 
            v-for="filter in filters" 
            :key="filter.key"
            class="sidebar-item" 
            :class="{ active: activeFilter === filter.key }"
            @click="setActiveFilter(filter.key)"
          >
            <div class="sidebar-item-content">
              <span class="sidebar-icon">{{ filter.icon }}</span>
              <span>{{ filter.label }}</span>
            </div>
            <span class="sidebar-count">{{ filter.count }}</span>
          </div>
        </div>
        
        <div class="sidebar-section">
          <div class="sidebar-title">状态</div>
          <div 
            v-for="status in statusFilters" 
            :key="status.key"
            class="sidebar-item" 
            :class="{ active: activeStatus === status.key }"
            @click="setActiveStatus(status.key)"
          >
            <div class="sidebar-item-content">
              <span class="sidebar-icon">{{ status.icon }}</span>
              <span>{{ status.label }}</span>
            </div>
            <span class="sidebar-count">{{ status.count }}</span>
          </div>
        </div>
      </aside>

      <!-- 主内容区 -->
      <main class="content-area">
        <div class="content-header">
          <h1 class="content-title">数据集管理</h1>
          <p class="content-subtitle">浏览、搜索和管理您的AI训练数据集</p>
        </div>
        
        <div class="content-body">
          <!-- 新建数据集按钮 -->
          <div class="action-bar">
            <button class="btn btn-primary new-dataset-btn" @click="goToUpload">
              <span>➕</span>
              <span>新建数据集</span>
            </button>
          </div>
          
          <!-- 搜索和筛选栏 -->
          <div class="search-filter-bar">
            <div class="search-container">
              <span class="search-icon">🔍</span>
              <input 
                type="text" 
                class="search-input" 
                placeholder="搜索数据集、标签或描述..." 
                v-model="searchTerm"
                @input="filterDatasets"
              >
            </div>
            <select class="filter-select" v-model="sortBy" @change="sortDatasets">
              <option value="name">按名称排序</option>
              <option value="date">按日期排序</option>
              <option value="size">按大小排序</option>
              <option value="author">按作者排序</option>
            </select>
            <div class="view-toggle">
              <button 
                class="view-btn" 
                :class="{ active: viewMode === 'grid' }"
                @click="setViewMode('grid')"
              >🔲</button>
              <button 
                class="view-btn" 
                :class="{ active: viewMode === 'list' }"
                @click="setViewMode('list')"
              >📋</button>
            </div>
          </div>
          
          <!-- 标签筛选 -->
          <div class="category-filter-bar">
          <div class="category-items">
            <div 
              v-for="tag in tagFilters" 
              :key="tag.key"
              class="category-item" 
              :class="{ active: activeTag === tag.key }"
              @click="setActiveTag(tag.key)"
            >
              <span class="category-icon">{{ tag.icon }}</span>
              <span>{{ tag.label }}</span>
              <span class="category-count">{{ tag.count }}</span>
            </div>
          </div>
          </div>
          
          
          
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-container">
            <div class="loading-spinner">⏳</div>
            <div class="loading-text">正在加载数据集...</div>
          </div>
          
          <!-- 错误状态 -->
          <div v-else-if="error" class="error-container">
            <div class="error-icon">❌</div>
            <div class="error-text">{{ error }}</div>
            <button class="btn btn-primary" @click="loadDatasets">重试</button>
          </div>
          
          <!-- 数据集网格视图 -->
          <div v-else-if="viewMode === 'grid'" class="dataset-grid">
            <div 
              v-for="dataset in filteredDatasets" 
              :key="dataset.id"
              class="dataset-card" 
              @click="goToDetail(dataset)"
            >
              <div class="dataset-header">
                <div class="dataset-name">{{ dataset.name }}</div>
                <div class="dataset-status" :class="`status-${dataset.status}`">
                  {{ getStatusText(dataset.status) }}
                </div>
              </div>
              <div class="dataset-info">{{ dataset.description }}</div>
              <div class="dataset-tags">
                <span v-if="!dataset.tags || dataset.tags.length === 0" class="no-tags">无标签</span>
                <span v-else v-for="tag in dataset.tags" :key="tag" class="tag">{{ tag }}</span>
              </div>
              <div class="dataset-meta">
                <span>📁 {{ dataset.sizeFormatted || dataset.size }}</span>
                <span>📅 {{ formatDate(dataset.createdTime || dataset.date) }}</span>
                <span>👤 {{ dataset.author }}</span>
              </div>
            </div>
          </div>
          
          <!-- 数据集列表视图 -->
          <div v-else-if="viewMode === 'list'" class="dataset-list active">
            <div 
              v-for="dataset in filteredDatasets" 
              :key="dataset.id"
              class="dataset-list-item"
              @click="goToDetail(dataset)"
            >
              <div class="dataset-list-info">
                <div class="dataset-list-name">{{ dataset.name }}</div>
                <div class="dataset-list-tags">
                  <span v-if="!dataset.tags || dataset.tags.length === 0" class="no-tags">无标签</span>
                  <span v-else v-for="tag in dataset.tags" :key="tag" class="tag">{{ tag }}</span>
                </div>
                <div class="dataset-list-size">{{ dataset.sizeFormatted || dataset.size }}</div>
                <div class="dataset-list-date">{{ formatDate(dataset.createdTime || dataset.date) }}</div>
                <div class="dataset-list-author">{{ dataset.author }}</div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DatasetList',
  data() {
    return {
      searchTerm: '',
      sortBy: 'name',
      sortOrder: 'asc',
      viewMode: 'grid',
      activeFilter: 'all',
      activeStatus: '',
      activeTag: '',
      loading: false,
      error: null,
      pagination: {
        page: 1,
        pageSize: 20,
        total: 0,
        totalPages: 0
      },
      filters: [
        { key: 'all', label: '所有数据集', icon: '📊', count: 0 },
        { key: 'recent', label: '最近上传', icon: '📤', count: 0 }
      ],
      statusFilters: [
        { key: 'active', label: '已完成', icon: '✅', count: 0 },
        { key: 'processing', label: '处理中', icon: '⏳', count: 0 },
        { key: 'error', label: '错误', icon: '❌', count: 0 }
      ],
      tagFilters: [
        { key: '', label: '全部标签', icon: '📊', count: 0 }
      ],
      datasets: [],
      filteredDatasets: [],
      searchTimeout: null
    }
  },
  async mounted() {
    await this.loadDatasets();
    await this.loadTasks();
  },
  methods: {
    // API调用方法
    async loadDatasets() {
      this.loading = true;
      this.error = null;
      
      try {
        const params = new URLSearchParams({
          page: this.pagination.page,
          pageSize: this.pagination.pageSize,
          sortBy: this.sortBy,
          sortOrder: this.sortOrder
        });
        
        if (this.searchTerm) {
          params.append('search', this.searchTerm);
        }
        
        if (this.activeTag) {
          params.append('taskId', this.activeTag);
        }
        
        if (this.activeStatus) {
          params.append('status', this.activeStatus);
        }
        
        const response = await fetch(`http://localhost:8080/datasets?${params}`, {
          headers: {
            'Authorization': `Bearer ${this.getToken()}`,
            'Content-Type': 'application/json'
          }
        });
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const result = await response.json();
        
        if (result.code === 200) {
          // 适配新的响应格式
          const pageInfo = result.data.pageInfo;
          
          // 处理数据集数据，适配字段名变化
          this.datasets = pageInfo.datasets.map(dataset => ({
            id: dataset.dataset_id,
            name: dataset.dataset_name,
            description: dataset.description,
            size: dataset.size,
            sizeFormatted: dataset.size_formatted,
            fileCount: dataset.file_count,
            tags: dataset.task_names ? dataset.task_names.split(',').map(tag => tag.trim()) : [],
            author: dataset.name, // 作者名称在name字段中
            createdTime: dataset.created_time,
            updatedTime: dataset.updated_time,
            downloadCount: dataset.downloadCount,
            date: dataset.created_time // 兼容性字段
          }));
          
          this.filteredDatasets = [...this.datasets];
          this.pagination.total = pageInfo.total;
          this.pagination.totalPages = pageInfo.totalPages;
          this.pagination.page = pageInfo.page;
          this.pagination.pageSize = pageInfo.pageSize;
          
          // 更新筛选器计数
          this.updateFilterCounts();
        } else {
          throw new Error(result.message || '获取数据集列表失败');
        }
      } catch (error) {
        console.error('加载数据集失败:', error);
        this.error = error.message;
      } finally {
        this.loading = false;
      }
    },
    
    // async loadStats() {
    //   try {
    //     const response = await fetch('/api/v1/datasets/stats', {
    //       headers: {
    //         'Authorization': `Bearer ${this.getToken()}`,
    //         'Content-Type': 'application/json'
    //       }
    //     });
        
    //     if (!response.ok) {
    //       throw new Error(`HTTP error! status: ${response.status}`);
    //     }
        
    //     const result = await response.json();
        
    //     if (result.code === 200) {
    //       this.stats = {
    //         total: result.data.totalDatasets,
    //         totalSize: result.data.totalSizeFormatted,
    //         processing: result.data.processingCount,
    //         downloads: result.data.monthlyDownloads
    //       };
          
    //       // 更新状态筛选器计数
    //       if (result.data.statusStats) {
    //         this.statusFilters.forEach(filter => {
    //           if (filter.key && result.data.statusStats[filter.key]) {
    //             filter.count = result.data.statusStats[filter.key];
    //           }
    //         });
    //       }
    //     }
    //   } catch (error) {
    //     console.error('加载统计信息失败:', error);
    //   }
    // },
    
    getToken() {
      // 从localStorage或其他地方获取token
      return localStorage.getItem('token') || '';
    },
    
    updateFilterCounts() {
      // 更新各种筛选器的计数
      this.filters[0].count = this.datasets.length; // 所有数据集
      
      // 计算最近上传的数据集数量（最近7天）
      const recentDate = new Date();
      recentDate.setDate(recentDate.getDate() - 7);
      const recentCount = this.datasets.filter(dataset => {
        const createdDate = new Date(dataset.createdTime || dataset.date);
        return createdDate >= recentDate;
      }).length;
      this.filters[1].count = recentCount; // 最近上传
      
      // 更新标签筛选器计数（基于任务类别）
      const tagCounts = {};
      this.datasets.forEach(dataset => {
        dataset.tags.forEach(tag => {
          tagCounts[tag] = (tagCounts[tag] || 0) + 1;
        });
      });
      this.tagFilters.forEach(filter => {
        if (filter.key && tagCounts[filter.label]) {
          filter.count = tagCounts[filter.label];
        }
        if (filter.key === '') {
          filter.count = this.datasets.length;
        }
      });
    },
    
    // 筛选和排序方法
    setActiveFilter(filter) {
      this.activeFilter = filter;
      this.pagination.page = 1;
      this.loadDatasets();
    },
    
    setActiveStatus(status) {
      this.activeStatus = status;
      this.pagination.page = 1;
      this.loadDatasets();
    },
    
    setActiveTag(tag) {
      this.activeTag = tag;
      this.pagination.page = 1;
      this.loadDatasets();
    },
    
    setViewMode(mode) {
      this.viewMode = mode;
    },
    
    async filterDatasets() {
      this.pagination.page = 1;
      await this.loadDatasets();
    },
    
    async sortDatasets() {
      this.pagination.page = 1;
      await this.loadDatasets();
    },
    
    getStatusText(status) {
      const statusMap = {
        active: '已完成',
        processing: '处理中',
        error: '处理失败'
      };
      return statusMap[status] || status;
    },
    
    getTagText(tags) {
      if (!tags || tags.length === 0) return '无标签';
      return tags.join(', ');
    },

    async loadTasks() {
      try {
        const response = await fetch('http://localhost:8080/tasks', {
          headers: {
            'Authorization': `Bearer ${this.getToken()}`,
            'Content-Type': 'application/json'
          }
        });
        if (!response.ok) return;
        const result = await response.json();
        if (result.code === 200 && Array.isArray(result.data)) {
          const tasks = result.data.map(t => ({ key: String(t.id), label: t.name, icon: '🏷️', count: 0 }));
          this.tagFilters = [{ key: '', label: '全部标签', icon: '📊', count: this.datasets.length }].concat(tasks);
          this.updateFilterCounts();
        }
      } catch (e) {
      }
    },
    
    formatDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN');
    },
    
    goToUpload() {
      this.$router.push('/datasets/upload');
    },
    
    goToDetail(dataset) {
      this.$router.push({
        path: `/datasets/${encodeURIComponent(dataset.name)}`,
        query: {
          id: dataset.id,
          status: dataset.status
        }
      });
    }
  },
  watch: {
    sortBy() {
      this.sortDatasets();
    },
    searchTerm() {
      // 防抖处理
      clearTimeout(this.searchTimeout);
      this.searchTimeout = setTimeout(() => {
        this.filterDatasets();
      }, 500);
    }
  }
}
</script>

<style scoped>
/* 全局样式 */
.dataset-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow-x: hidden;
}

/* 主容器 */
.main-container {
  width: 80%;
  max-width: none;
  margin: 2rem auto;
  padding: 0;
  display: flex;
  gap: 2rem;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.1),
    0 0 0 1px rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
  min-height: calc(100vh - 70px);
}

.main-container::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 40px;
  background: linear-gradient(180deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  border-radius: 20px 0 0 20px;
  box-shadow: inset -2px 0 4px rgba(0, 0, 0, 0.1);
}

/* 书脊设计 */
.book-spine {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 40px;
  background: linear-gradient(180deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  border-radius: 20px 0 0 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  color: white;
  font-weight: bold;
  font-size: 0.9rem;
  letter-spacing: 2px;
  z-index: 2;
}

/* 侧边栏 */
.sidebar {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 252, 0.8) 100%);
  border-radius: 0 15px 15px 0;
  padding: 2rem 1.5rem 1.5rem 3rem;
  height: 100%;
  width: 280px;
  overflow-y: auto;
  margin-left: 40px;
  border: 1px solid #e2e8f0;
}

.sidebar-section {
  margin-bottom: 2rem;
}

.sidebar-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 1rem;
}

.sidebar-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 0.75rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 0.5rem;
}

.sidebar-item:hover, .sidebar-item.active {
  background: #f1f5f9;
  color: #667eea;
}

.sidebar-item-content {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.sidebar-icon {
  font-size: 1.2rem;
  width: 20px;
  text-align: center;
}

.sidebar-count {
  background: #e2e8f0;
  color: #64748b;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-weight: 500;
}

.sidebar-item.active .sidebar-count {
  background: #667eea;
  color: white;
}

/* 主内容区 */
.content-area {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 252, 0.8) 100%);
  border-radius: 0 15px 15px 0;
  padding: 2rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.content-header {
  padding: 2rem;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.content-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.content-subtitle {
  opacity: 0.9;
  font-size: 1.1rem;
}

.content-body {
  padding: 2rem;
}

/* 搜索和筛选栏 */
.search-filter-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

/* 分类筛选栏 */
.category-filter-bar {
  margin-bottom: 2rem;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.category-items {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  justify-content: center;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.875rem;
}

.category-item:hover {
  border-color: #667eea;
  background: #f1f5f9;
}

.category-item.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.category-icon {
  font-size: 1rem;
}

.category-count {
  background: #e2e8f0;
  color: #64748b;
  font-size: 0.75rem;
  padding: 0.125rem 0.375rem;
  border-radius: 10px;
  font-weight: 500;
  min-width: 20px;
  text-align: center;
}

.category-item.active .category-count {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.search-container {
  position: relative;
  flex: 1;
  min-width: 300px;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 3rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  background: #ffffff;
  transition: all 0.2s ease;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  font-size: 1.2rem;
}

.filter-select {
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  background: white;
  cursor: pointer;
  min-width: 150px;
}

.view-toggle {
  display: flex;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.view-btn {
  padding: 0.75rem 1rem;
  border: none;
  background: white;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.2s ease;
}

.view-btn.active {
  background: #667eea;
  color: white;
}

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e2e8f0;
}

.new-dataset-btn {
  font-size: 0.9rem;
  padding: 0.6rem 1.2rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 2px 4px rgba(102, 126, 234, 0.2);
}

.new-dataset-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}

/* 数据集统计 */
 

/* 数据集网格视图 */
.dataset-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
}

.dataset-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
  cursor: pointer;
}

.dataset-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: #667eea;
}

.dataset-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.dataset-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 1.1rem;
}

.dataset-status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-active {
  background: #dcfce7;
  color: #166534;
}

.status-processing {
  background: #fef3c7;
  color: #92400e;
}

.status-error {
  background: #fecaca;
  color: #991b1b;
}

.dataset-info {
  color: #64748b;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.dataset-tags {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  align-items: center;
}

.tag {
  background: #f1f5f9;
  color: #475569;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 500;
  display: inline-block;
  transition: all 0.2s ease;
  border: 1px solid #e2e8f0;
}

.tag:hover {
  background: #e2e8f0;
  border-color: #cbd5e1;
}

.no-tags {
  color: #94a3b8;
  font-style: italic;
  font-size: 0.75rem;
}

.dataset-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #94a3b8;
  margin-bottom: 1rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5a67d8;
}

/* 数据集列表视图 */
.dataset-list {
  display: none;
}

.dataset-list.active {
  display: block;
}

.dataset-list-item {
  display: flex;
  align-items: center;
  padding: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  margin-bottom: 0.5rem;
  background: white;
  transition: all 0.2s ease;
  cursor: pointer;
}

.dataset-list-item:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.dataset-list-info {
  flex: 1;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr;
  gap: 1rem;
  align-items: center;
}

.dataset-list-name {
  font-weight: 600;
  color: #1e293b;
}

.dataset-list-tags {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
  align-items: center;
}

.dataset-list-category,
.dataset-list-size,
.dataset-list-date,
.dataset-list-author {
  color: #64748b;
  font-size: 0.875rem;
}

/* 加载和错误状态 */
.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
}

.loading-spinner, .error-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.loading-text, .error-text {
  font-size: 1.1rem;
  color: #64748b;
  margin-bottom: 1rem;
}

.error-container .btn {
  margin-top: 1rem;
}

/* 浮动装饰元素 */
.floating-elements {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.floating-book {
  position: absolute;
  font-size: 2rem;
  opacity: 0.1;
  animation: float 15s ease-in-out infinite;
}

.floating-book:nth-child(1) {
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.floating-book:nth-child(2) {
  top: 20%;
  right: 15%;
  animation-delay: 3s;
}

.floating-book:nth-child(3) {
  bottom: 20%;
  left: 20%;
  animation-delay: 6s;
}

.floating-book:nth-child(4) {
  bottom: 30%;
  right: 10%;
  animation-delay: 9s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  25% {
    transform: translateY(-20px) rotate(5deg);
  }
  50% {
    transform: translateY(-10px) rotate(-3deg);
  }
  75% {
    transform: translateY(-15px) rotate(2deg);
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-container {
    grid-template-columns: 1fr;
    gap: 1rem;
    padding: 1rem;
  }
  
  .sidebar {
    order: 2;
  }
  
  .content-area {
    order: 1;
  }
}

  @media (max-width: 768px) {
    .dataset-grid {
      grid-template-columns: 1fr;
    }
    
    .search-filter-bar {
      flex-direction: column;
    }
    
    .dataset-list-info {
      grid-template-columns: 1fr;
      gap: 0.5rem;
    }
  }
</style>
