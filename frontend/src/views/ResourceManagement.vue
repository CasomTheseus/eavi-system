<template>
  <div class="resource-management">


    <!-- 主要内容 -->
    <div class="main-container">
      <div class="book-spine">资源管理</div>
      
      <!-- 侧边栏 -->
      <div class="sidebar">
        <h2 class="sidebar-title">
          <span>📦</span>
          资源分类
        </h2>
        <ul class="category-list">
          <li 
            v-for="category in categories" 
            :key="category.id"
            class="category-item"
            :class="{ active: selectedCategory === category.id }"
            @click="filterResources(category.id)"
          >
            <div class="category-content">
              <span class="category-icon">{{ category.icon }}</span>
              <span>{{ category.name }}</span>
            </div>
            <span class="category-count">{{ category.count }}</span>
          </li>
        </ul>
      </div>

      <!-- 主内容区域 -->
      <div class="content-area">
        <div class="content-header">
          <h1 class="content-title">
            <span>🎯</span>
            团队资源管理
          </h1>
          <div class="search-container">
            <span class="search-icon">🔍</span>
            <input 
              type="text" 
              class="search-input" 
              placeholder="搜索资源..." 
              v-model="searchQuery"
              @input="searchResources"
            >
          </div>
          <button class="add-resource-btn" @click="addResource">
            <span>➕</span>
            申请资源
          </button>
        </div>

        <!-- 资源网格 -->
        <div class="resources-grid">
          <div 
            v-for="resource in filteredResources" 
            :key="resource.id"
            class="resource-card"
            @click="viewResourceDetail(resource)"
          >
            <div class="resource-header">
              <div class="resource-name">{{ resource.name }}</div>
              <div 
                class="resource-status"
                :class="`status-${resource.status}`"
              >
                {{ getStatusText(resource.status) }}
              </div>
            </div>
            <div class="resource-info">
              {{ resource.description }}
            </div>
            <div class="resource-specs">
              <span 
                v-for="spec in resource.specs" 
                :key="spec"
                class="spec-tag"
              >
                {{ spec }}
              </span>
            </div>
            <div class="resource-meta">
              <span>负责人: {{ resource.manager }}</span>
              <span>位置: {{ resource.location }}</span>
            </div>
            <div class="resource-actions">
              <button 
                class="btn btn-primary" 
                @click.stop="viewResourceDetail(resource)"
              >
                查看详情
              </button>
              <button 
                class="btn"
                :class="resource.status === 'available' ? 'btn-primary' : 'btn-secondary'"
                @click.stop="applyResource(resource)"
              >
                {{ resource.status === 'available' ? '立即申请' : '申请使用' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ResourceManagement',
  data() {
    return {
      searchQuery: '',
      selectedCategory: 'all',
      
      categories: [
        { id: 'all', name: '全部资源', icon: '📊', count: 8 },
        { id: 'server', name: '服务器设备', icon: '🖥️', count: 4 },
        { id: 'display', name: '显示器设备', icon: '🖥️', count: 4 }
      ],
      
      resources: [
        {
          id: 'server-01',
          name: '服务器-01 (Dell R740)',
          category: 'server',
          status: 'available',
          description: '高性能计算服务器，适用于深度学习训练和大数据处理',
          specs: ['Intel Xeon', '64GB RAM', 'RTX 4090', '2TB SSD'],
          manager: '张三',
          location: '机房A-01'
        },
        {
          id: 'server-02',
          name: '服务器-02 (HP DL380)',
          category: 'server',
          status: 'in-use',
          description: '通用计算服务器，适用于Web服务和数据库应用',
          specs: ['Intel Xeon', '32GB RAM', '1TB SSD', 'RAID 5'],
          manager: '王五',
          location: '机房A-02'
        },
        {
          id: 'server-03',
          name: 'GPU服务器 (自建)',
          category: 'server',
          status: 'available',
          description: '专用GPU计算服务器，配置多张显卡用于并行计算',
          specs: ['AMD Ryzen', '128GB RAM', '4x RTX 3080', '4TB NVMe'],
          manager: '赵六',
          location: '机房B-01'
        },
        {
          id: 'server-04',
          name: '存储服务器 (NAS)',
          category: 'server',
          status: 'in-use',
          description: '网络存储服务器，提供大容量数据存储和备份服务',
          specs: ['Intel Core i7', '16GB RAM', '24TB HDD', 'RAID 6'],
          manager: '孙七',
          location: '机房A-03'
        },
        {
          id: 'display-01',
          name: '4K显示器 27寸',
          category: 'display',
          status: 'available',
          description: '高分辨率显示器，适用于图像处理和设计工作',
          specs: ['3840x2160', 'IPS面板', '99% sRGB', 'USB-C'],
          manager: '张三',
          location: '办公室A-101'
        },
        {
          id: 'display-02',
          name: '超宽显示器 34寸',
          category: 'display',
          status: 'in-use',
          description: '超宽屏显示器，提供更大的工作空间',
          specs: ['3440x1440', '曲面VA', '144Hz', 'HDR400'],
          manager: '李四',
          location: '办公室A-102'
        },
        {
          id: 'display-03',
          name: '专业显示器 32寸',
          category: 'display',
          status: 'available',
          description: '专业级显示器，适用于色彩敏感的设计工作',
          specs: ['4096x2160', '99% sRGB', '硬件校色', 'Thunderbolt'],
          manager: '王五',
          location: '设计室B-201'
        },
        {
          id: 'display-04',
          name: '便携显示器 15寸',
          category: 'display',
          status: 'in-use',
          description: '便携式显示器，适用于移动办公和演示',
          specs: ['1920x1080', 'USB-C供电', '轻薄设计', '触控支持'],
          manager: '赵六',
          location: '移动设备'
        }
      ]
    }
  },
  
  computed: {
    filteredResources() {
      let filtered = this.resources;
      
      // 按分类筛选
      if (this.selectedCategory !== 'all') {
        filtered = filtered.filter(resource => resource.category === this.selectedCategory);
      }
      
      // 搜索筛选
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        filtered = filtered.filter(resource => 
          resource.name.toLowerCase().includes(query) ||
          resource.description.toLowerCase().includes(query) ||
          resource.specs.some(spec => spec.toLowerCase().includes(query)) ||
          resource.manager.toLowerCase().includes(query) ||
          resource.location.toLowerCase().includes(query)
        );
      }
      
      return filtered;
    }
  },
  
  methods: {
    filterResources(categoryId) {
      this.selectedCategory = categoryId;
    },
    
    searchResources() {
      // 搜索逻辑已在computed中处理
    },
    
    addResource() {
      // 跳转到资源申请页面
      this.$router.push('/resource-application');
    },
    
    viewResourceDetail(resource) {
      // 跳转到资源详情页面
      this.$router.push({
        name: 'ResourceDetails',
        query: {
          id: resource.id,
          name: resource.name,
          type: resource.category,
          status: resource.status
        }
      });
    },
    
    applyResource(resource) {
      const message = resource.status === 'available' 
        ? `确定要申请使用 ${resource.name} 吗？`
        : `${resource.name} 正在使用中，确定要申请排队使用吗？`;
        
      if (confirm(message)) {
        alert('申请已提交，请等待审批！');
      }
    },
    
    getStatusText(status) {
      const statusMap = {
        'available': '可用',
        'in-use': '使用中',
        'maintenance': '维护中',
        'reserved': '已预约'
      };
      return statusMap[status] || status;
    }
  },
  
  mounted() {
    // 页面加载时应用保存的主题
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.body.setAttribute('data-theme', savedTheme);
    
    // 更新主题切换按钮
    const themeToggle = document.querySelector('.theme-toggle');
    if (savedTheme === 'dark') {
      themeToggle.textContent = '☀️';
    } else {
      themeToggle.textContent = '🌙';
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
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  background-attachment: fixed;
  min-height: 100vh;
  color: #334155;
  line-height: 1.6;
  position: relative;
  overflow-x: hidden;
}

body::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(30, 58, 138, 0.4) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(59, 130, 246, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(147, 197, 253, 0.2) 0%, transparent 50%);
  animation: backgroundMove 20s ease-in-out infinite;
}

@keyframes backgroundMove {
  0%, 100% { transform: translateX(0) translateY(0); }
  25% { transform: translateX(-20px) translateY(-10px); }
  50% { transform: translateX(20px) translateY(10px); }
  75% { transform: translateX(-10px) translateY(20px); }
}



/* 主要内容容器 */
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
  min-height: calc(100vh - 140px);
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
  width: 280px;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.category-list {
  list-style: none;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  margin-bottom: 0.5rem;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
  font-weight: 500;
}

.category-item:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.category-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.category-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
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

/* 主内容区域 */
.content-area {
  flex: 1;
  padding: 2rem;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
}

.content-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.search-container {
  position: relative;
  flex: 1;
  max-width: 400px;
  margin: 0 2rem;
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

.add-resource-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0.75rem 1.5rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.add-resource-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

/* 资源网格 */
.resources-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
}

.resource-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
  cursor: pointer;
}

.resource-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: #667eea;
}

.resource-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.resource-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 1.1rem;
}

.resource-status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-available {
  background: #dcfce7;
  color: #166534;
}

.status-in-use {
  background: #fef3c7;
  color: #92400e;
}

.status-maintenance {
  background: #fee2e2;
  color: #991b1b;
}

.status-reserved {
  background: #dbeafe;
  color: #1e40af;
}

.resource-info {
  color: #64748b;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.resource-specs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.spec-tag {
  background: #f1f5f9;
  color: #64748b;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
}

.resource-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #94a3b8;
  margin-bottom: 1rem;
}

.resource-actions {
  display: flex;
  gap: 0.5rem;
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

.btn-secondary {
  background: #f1f5f9;
  color: #64748b;
}

.btn-secondary:hover {
  background: #e2e8f0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-container {
    flex-direction: column;
    gap: 0;
  }
  
  .sidebar {
    width: 100%;
    padding: 2rem;
    border-radius: 0;
  }
  
  .category-list {
    display: flex;
    gap: 1rem;
    overflow-x: auto;
    padding-bottom: 1rem;
  }
  
  .category-item {
    white-space: nowrap;
    margin-bottom: 0;
  }
}

@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }
  
  .main-container {
    margin: 1rem;
    padding: 0;
  }
  
  .content-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .search-container {
    margin: 0;
    max-width: none;
  }
  
  .resources-grid {
    grid-template-columns: 1fr;
  }
}

/* 暗色主题 */
body[data-theme="dark"] {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%);
}

body[data-theme="dark"] .navbar {
  background: rgba(15, 23, 42, 0.95);
}

body[data-theme="dark"] .nav-link {
  color: #94a3b8;
}

body[data-theme="dark"] .nav-link:hover,
body[data-theme="dark"] .nav-link.active {
  color: #667eea;
  background: rgba(51, 65, 85, 0.5);
}

body[data-theme="dark"] .theme-toggle:hover {
  background: rgba(51, 65, 85, 0.5);
}

body[data-theme="dark"] .main-container {
  background: rgba(15, 23, 42, 0.95);
}

body[data-theme="dark"] .sidebar {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.9) 0%, rgba(51, 65, 85, 0.8) 100%);
}

body[data-theme="dark"] .sidebar-title {
  color: #e2e8f0;
}

body[data-theme="dark"] .category-item {
  color: #94a3b8;
}

body[data-theme="dark"] .category-item:hover {
  background: rgba(102, 126, 234, 0.2);
  color: #667eea;
}

body[data-theme="dark"] .content-title {
  color: #e2e8f0;
}

body[data-theme="dark"] .search-input {
  background: rgba(51, 65, 85, 0.8);
  border-color: rgba(102, 126, 234, 0.3);
  color: #e2e8f0;
}

body[data-theme="dark"] .resource-card {
  background: rgba(30, 41, 59, 0.9);
  border-color: rgba(102, 126, 234, 0.2);
}

body[data-theme="dark"] .resource-name {
  color: #e2e8f0;
}

body[data-theme="dark"] .resource-info {
  color: #94a3b8;
}

body[data-theme="dark"] .spec-tag {
  background: rgba(51, 65, 85, 0.8);
  color: #cbd5e1;
}

body[data-theme="dark"] .resource-meta {
  color: #64748b;
}

body[data-theme="dark"] .btn-secondary {
  background: rgba(51, 65, 85, 0.8);
  color: #cbd5e1;
}

body[data-theme="dark"] .btn-secondary:hover {
  background: rgba(71, 85, 105, 0.8);
}
</style>