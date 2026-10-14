<template>
  <div class="resource-detail-page">


    <!-- 主要内容 -->
    <div class="main-container">
      <div class="book-spine">资源详情</div>
      
      <!-- 页面头部 -->
      <div class="page-header">
        <h1 class="page-title">
          <span>{{ resourceIcon }}</span>
          <span>{{ resourceData.name }}</span>
        </h1>
        <p class="page-subtitle">查看资源的详细信息、使用状态和历史记录</p>
      </div>

      <!-- 资源详情卡片 -->
      <div class="resource-detail-card">
        <div class="resource-header">
          <div class="resource-title">
            <div class="resource-icon">{{ resourceIcon }}</div>
            <div>
              <div class="resource-name">{{ resourceData.name }}</div>
              <div style="color: #64748b; font-size: 1rem; margin-top: 0.25rem;">
                {{ resourceData.category }}
              </div>
            </div>
          </div>
          <div :class="['resource-status', `status-${resourceData.status}`]">
            {{ getStatusText(resourceData.status) }}
          </div>
        </div>

        <!-- 详情网格 -->
        <div class="details-grid">
          <!-- 基本信息 -->
          <div class="detail-section">
            <h3 class="section-title">
              <span>📋</span>
              基本信息
            </h3>
            <div class="detail-item">
              <span class="detail-label">资源名称</span>
              <span class="detail-value">{{ resourceData.name }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">资源类型</span>
              <span class="detail-value">{{ resourceData.type }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">位置</span>
              <span class="detail-value">{{ resourceData.location }}</span>
            </div>
          </div>

          <!-- 负责人信息 -->
          <div class="detail-section">
            <h3 class="section-title">
              <span>👤</span>
              负责人信息
            </h3>
            <div class="detail-item">
              <span class="detail-label">负责人</span>
              <span class="detail-value">{{ resourceData.owner }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">联系邮箱</span>
              <span class="detail-value">{{ resourceData.ownerEmail }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">联系电话</span>
              <span class="detail-value">{{ resourceData.ownerPhone }}</span>
            </div>
          </div>

          <!-- 技术规格 -->
          <div class="detail-section">
            <h3 class="section-title">
              <span>⚙️</span>
              技术规格
            </h3>
            <div class="detail-item" v-for="spec in resourceData.specs" :key="spec.label">
              <span class="detail-label">{{ spec.label }}</span>
              <span class="detail-value">{{ spec.value }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 使用历史 -->
      <div class="usage-history">
        <h3 class="section-title">
          <span>📊</span>
          使用历史
        </h3>
        <div class="history-item" v-for="item in usageHistory" :key="item.id">
          <span class="history-date">{{ item.date }}</span>
          <span class="history-action">{{ item.action }}</span>
          <span class="history-user">{{ item.user }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button class="action-btn" @click="requestResource">
          <span>📝</span>
          申请使用
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ResourceDetail',
  data() {
    return {
      themeIcon: '🌙',
      resourceData: {
        id: '',
        name: '',
        type: '',
        category: '',
        status: '',
        location: '',
        owner: '',
        ownerEmail: '',
        ownerPhone: '',
        specs: []
      },
      usageHistory: [
        {
          id: 1,
          date: '2024-12-01',
          action: '开始使用 - 深度学习模型训练项目',
          user: '李四'
        },
        {
          id: 2,
          date: '2024-11-28',
          action: '归还设备',
          user: '王五'
        },
        {
          id: 3,
          date: '2024-11-15',
          action: '开始使用 - 图像处理算法开发',
          user: '王五'
        },
        {
          id: 4,
          date: '2024-11-10',
          action: '系统维护完成',
          user: '张三'
        },
        {
          id: 5,
          date: '2024-11-08',
          action: '进入维护状态',
          user: '张三'
        }
      ]
    }
  },
  computed: {
    resourceIcon() {
      return this.resourceData.type === 'server' ? '🖥️' : '📺';
    }
  },
  mounted() {
    this.restoreTheme();
    this.loadResourceDetails();
  },
  methods: {
    loadResourceDetails() {
      // 从路由参数获取资源ID
      const resourceId = this.$route.params.id;
      
      // 模拟从API获取资源详情
      // 这里可以根据resourceId从后端获取实际数据
      const mockData = {
        'server-01': {
          id: 'server-01',
          name: '服务器-01',
          type: 'server',
          category: '服务器',
          status: 'in-use',
          location: '实验室A-101',
          owner: '张三',
          ownerEmail: 'zhangsan@eavi.com',
          ownerPhone: '138-0000-0000',
          specs: [
            { label: '处理器', value: 'Intel i9-12900H' },
            { label: '显卡', value: 'NVIDIA RTX 4090' },
            { label: '内存', value: '64GB DDR5' },
            { label: '存储', value: '2TB NVMe SSD' }
          ]
        },
        'display-01': {
          id: 'display-01',
          name: '显示器-01',
          type: 'display',
          category: '显示器',
          status: 'available',
          location: '办公室A-101',
          owner: '张三',
          ownerEmail: 'zhangsan@eavi.com',
          ownerPhone: '138-0000-0000',
          specs: [
            { label: '分辨率', value: '3840x2160' },
            { label: '面板类型', value: 'IPS' },
            { label: '色域', value: '99% sRGB' },
            { label: '接口', value: 'HDMI, USB-C' }
          ]
        }
      };

      // 获取资源数据，如果没有找到则使用默认数据
      this.resourceData = mockData[resourceId] || mockData['server-01'];
    },
    getStatusText(status) {
      const statusMap = {
        'available': '可用',
        'in-use': '使用中',
        'maintenance': '维护中'
      };
      return statusMap[status] || '未知';
    },
    requestResource() {
      this.$router.push('/resource-application');
    },
    restoreTheme() {
      const savedTheme = localStorage.getItem('theme');
      if (savedTheme === 'dark') {
        document.body.classList.add('dark-theme');
        this.themeIcon = '☀️';
      }
    }
  }
}
</script>

<style scoped>
/* 全局样式 */
.resource-detail-page {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  background-attachment: fixed;
  min-height: 100vh;
  color: #334155;
  line-height: 1.6;
  position: relative;
  overflow-x: hidden;
}

.resource-detail-page::before {
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



/* 主要内容 */
.main-container {
  width: 80%;
  max-width: none;
  margin: 2rem auto;
  padding: 0 2rem;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.1),
    0 0 0 1px rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
  z-index: 10;
}

.book-spine {
  position: absolute;
  left: -10px;
  top: 0;
  bottom: 0;
  width: 20px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 0 10px 10px 0;
  box-shadow: 
    inset -2px 0 4px rgba(0, 0, 0, 0.2),
    2px 0 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  color: white;
  font-weight: 600;
  font-size: 1.1rem;
  letter-spacing: 2px;
}

/* 页面头部 */
.page-header {
  padding: 3rem 2rem 2rem 3rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  margin: 0 -2rem 2rem -2rem;
  border-radius: 20px 20px 0 0;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.page-subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
  margin-bottom: 2rem;
}

/* 资源详情卡片 */
.resource-detail-card {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(226, 232, 240, 0.5);
}

.resource-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f1f5f9;
}

.resource-title {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.resource-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.resource-name {
  font-size: 1.8rem;
  font-weight: 600;
  color: #1e293b;
}

.resource-status {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
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

/* 详情网格 */
.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.detail-section {
  background: #f8fafc;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
}

.section-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #e2e8f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  font-weight: 500;
  color: #64748b;
}

.detail-value {
  font-weight: 600;
  color: #1e293b;
}

/* 使用历史 */
.usage-history {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(226, 232, 240, 0.5);
}

.history-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.history-date {
  font-size: 0.875rem;
  color: #64748b;
  min-width: 100px;
}

.history-action {
  font-weight: 500;
  color: #1e293b;
}

.history-user {
  font-size: 0.875rem;
  color: #667eea;
  margin-left: auto;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
  flex-wrap: wrap;
}

.action-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 1rem 1.5rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.action-btn.secondary {
  background: white;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.action-btn.secondary:hover {
  background: #f1f5f9;
  transform: translateY(-2px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }
  
  .main-container {
    margin: 1rem;
    padding: 0 1rem;
  }
  
  .page-header {
    padding: 2rem 1rem 1rem 2rem;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .details-grid {
    grid-template-columns: 1fr;
  }
  
  .resource-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}

/* 暗色主题 */
:global(.dark-theme) .navbar,
:global(.dark-theme) .main-container,
:global(.dark-theme) .resource-detail-card,
:global(.dark-theme) .usage-history,
:global(.dark-theme) .detail-section {
  background: #1e293b;
  border-color: #334155;
  color: #e2e8f0;
}

:global(.dark-theme) .history-item {
  background: #334155;
  border-color: #475569;
}
</style>