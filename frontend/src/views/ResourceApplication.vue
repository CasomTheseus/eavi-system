<template>
  <div class="resource-application-page">


    <!-- 主要内容 -->
    <div class="main-container">
      <div class="book-spine">资源申请</div>
      
      <div class="content-area">
        <div class="page-header">
          <h1 class="page-title">
            <span>📝</span>
            资源申请
          </h1>
          <p class="page-subtitle">申请团队硬件设备和基础设施资源</p>
        </div>

        <form class="application-form" @submit.prevent="submitApplication">
          <!-- 申请人信息 -->
          <div class="form-section">
            <h3 class="section-title">
              <span>👤</span>
              申请人信息
            </h3>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">申请人姓名</label>
                <input type="text" class="form-input" v-model="formData.applicantName" required>
              </div>
              <div class="form-group">
                <label class="form-label">联系方式</label>
                <input type="text" class="form-input" v-model="formData.contact" required>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">部门/团队</label>
                <input type="text" class="form-input" v-model="formData.department" required>
              </div>
              <div class="form-group">
                <label class="form-label">职位</label>
                <input type="text" class="form-input" v-model="formData.position" required>
              </div>
            </div>
          </div>

          <!-- 资源类型选择 -->
          <div class="form-section">
            <h3 class="section-title">
              <span>🎯</span>
              资源类型
            </h3>
            <div class="resource-types">
              <div 
                v-for="type in resourceTypes" 
                :key="type.id"
                :class="['resource-type-card', { selected: selectedResourceType === type.id }]"
                @click="selectResourceType(type.id)"
              >
                <div class="resource-type-icon">{{ type.icon }}</div>
                <div class="resource-type-name">{{ type.name }}</div>
                <div class="resource-type-desc">{{ type.description }}</div>
              </div>
            </div>

            <!-- 服务器规格选择 -->
            <div v-if="selectedResourceType === 'server'" class="resource-specs active">
              <h4 class="form-label">选择服务器配置</h4>
              <div class="spec-options">
                <div 
                  v-for="spec in serverSpecs" 
                  :key="spec.id"
                  :class="['spec-option', { selected: selectedSpec === spec.id }]"
                  @click="selectSpec(spec.id)"
                >
                  <div class="spec-name">{{ spec.name }}</div>
                  <div class="spec-details">{{ spec.details }}</div>
                </div>
              </div>
            </div>

            <!-- 显示器规格选择 -->
            <div v-if="selectedResourceType === 'display'" class="resource-specs active">
              <h4 class="form-label">选择显示器类型</h4>
              <div class="spec-options">
                <div 
                  v-for="spec in displaySpecs" 
                  :key="spec.id"
                  :class="['spec-option', { selected: selectedSpec === spec.id }]"
                  @click="selectSpec(spec.id)"
                >
                  <div class="spec-name">{{ spec.name }}</div>
                  <div class="spec-details">{{ spec.details }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 申请详情 -->
          <div class="form-section">
            <h3 class="section-title">
              <span>📋</span>
              申请详情
            </h3>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">使用期限</label>
                <select class="form-select" v-model="formData.duration" required>
                  <option value="">请选择使用期限</option>
                  <option value="1week">1周</option>
                  <option value="1month">1个月</option>
                  <option value="3months">3个月</option>
                  <option value="6months">6个月</option>
                  <option value="1year">1年</option>
                  <option value="permanent">长期使用</option>
                </select>
              </div>
              <div class="form-group">
                <label class="form-label">优先级</label>
                <select class="form-select" v-model="formData.priority" required>
                  <option value="">请选择优先级</option>
                  <option value="low">低</option>
                  <option value="medium">中</option>
                  <option value="high">高</option>
                  <option value="urgent">紧急</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">使用目的</label>
              <textarea 
                class="form-textarea" 
                v-model="formData.purpose" 
                placeholder="请详细描述资源的使用目的和用途..." 
                required
              ></textarea>
            </div>
            <div class="form-group">
              <label class="form-label">技术要求</label>
              <textarea 
                class="form-textarea" 
                v-model="formData.requirements" 
                placeholder="请描述对资源的具体技术要求或特殊需求..."
              ></textarea>
            </div>
          </div>

          <!-- 表单操作 -->
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="goBack">
              <span>←</span>
              返回
            </button>
            <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="loading"></span>
              <span v-else>📤</span>
              {{ isSubmitting ? '提交中...' : '提交申请' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ResourceApplication',
  data() {
    return {
      themeIcon: '🌙',
      isSubmitting: false,
      selectedResourceType: '',
      selectedSpec: '',
      formData: {
        applicantName: '',
        contact: '',
        department: '',
        position: '',
        duration: '',
        priority: '',
        purpose: '',
        requirements: ''
      },
      resourceTypes: [
        {
          id: 'server',
          name: '服务器设备',
          description: '高性能计算服务器、GPU服务器、存储服务器等',
          icon: '🖥️'
        },
        {
          id: 'display',
          name: '显示器设备',
          description: '4K显示器、超宽显示器、专业显示器等',
          icon: '🖥️'
        }
      ],
      serverSpecs: [
        {
          id: 'high-performance',
          name: '高性能计算服务器',
          details: 'Intel Xeon, 64GB RAM, RTX 4090, 2TB SSD'
        },
        {
          id: 'general',
          name: '通用计算服务器',
          details: 'Intel Xeon, 32GB RAM, 1TB SSD, RAID 5'
        },
        {
          id: 'gpu',
          name: 'GPU计算服务器',
          details: 'AMD Ryzen, 128GB RAM, 4x RTX 3080, 4TB NVMe'
        },
        {
          id: 'storage',
          name: '存储服务器',
          details: 'Intel Core i7, 16GB RAM, 24TB HDD, RAID 6'
        }
      ],
      displaySpecs: [
        {
          id: '4k-27',
          name: '4K显示器 27寸',
          details: '27寸, 4K分辨率, IPS面板, USB-C'
        },
        {
          id: 'ultrawide-34',
          name: '超宽显示器 34寸',
          details: '34寸, 3440x1440, 曲面屏, 144Hz'
        },
        {
          id: 'professional-32',
          name: '专业显示器 32寸',
          details: '32寸, 4K分辨率, 99% sRGB, 硬件校色'
        },
        {
          id: 'portable-15',
          name: '便携显示器 15寸',
          details: '15.6寸, 1080p, IPS面板, USB-C供电'
        }
      ]
    }
  },
  mounted() {
    this.restoreTheme();
  },
  methods: {
    selectResourceType(type) {
      this.selectedResourceType = type;
      this.selectedSpec = ''; // 重置规格选择
    },
    selectSpec(spec) {
      this.selectedSpec = spec;
    },
    async submitApplication() {
      // 验证必填字段
      if (!this.selectedResourceType) {
        alert('请选择资源类型！');
        return;
      }

      if (!this.selectedSpec) {
        alert('请选择具体的资源规格！');
        return;
      }

      this.isSubmitting = true;

      try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 2000));

        // 获取表单数据
        const applicationData = {
          ...this.formData,
          resourceType: this.selectedResourceType,
          resourceSpec: this.selectedSpec,
          submitTime: new Date().toISOString(),
          applicationId: 'APP-' + Date.now()
        };

        console.log('申请数据:', applicationData);
        
        // 显示成功消息
        alert(`申请已成功提交！\n\n申请编号: ${applicationData.applicationId}\n状态: 待审批\n\n您可以在资源管理页面查看申请状态。`);
        
        // 重定向到资源管理页面
        this.$router.push('/resources');
      } catch (error) {
        console.error('提交申请失败:', error);
        alert('提交申请失败，请稍后重试！');
      } finally {
        this.isSubmitting = false;
      }
    },
    goBack() {
      this.$router.go(-1);
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
.resource-application-page {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  background-attachment: fixed;
  min-height: 100vh;
  color: #334155;
  line-height: 1.6;
  position: relative;
  overflow-x: hidden;
}

.resource-application-page::before {
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
  padding: 0 2rem;
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

/* 内容区域 */
.content-area {
  padding: 3rem 2rem 2rem 4rem;
}

.page-header {
  text-align: center;
  margin-bottom: 3rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
}

.page-subtitle {
  font-size: 1.2rem;
  color: #64748b;
  margin-bottom: 2rem;
}

/* 申请表单 */
.application-form {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(226, 232, 240, 0.5);
}

.form-section {
  margin-bottom: 2rem;
}

.section-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.form-input, .form-select, .form-textarea {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  background: #ffffff;
  transition: all 0.2s ease;
}

.form-input:focus, .form-select:focus, .form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

/* 资源类型选择 */
.resource-types {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.resource-type-card {
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
}

.resource-type-card:hover {
  border-color: #667eea;
  background: #f1f5f9;
}

.resource-type-card.selected {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.resource-type-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.resource-type-name {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.resource-type-desc {
  font-size: 0.9rem;
  color: #64748b;
}

/* 资源规格选择 */
.resource-specs {
  margin-top: 1rem;
}

.spec-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.spec-option {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.spec-option:hover {
  border-color: #667eea;
}

.spec-option.selected {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.spec-name {
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.spec-details {
  font-size: 0.85rem;
  color: #64748b;
}

/* 按钮样式 */
.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.btn-secondary {
  background: #f1f5f9;
  color: #64748b;
}

.btn-secondary:hover {
  background: #e2e8f0;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid #e2e8f0;
}

/* 加载动画 */
.loading {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }
  
  .main-container {
    margin: 1rem;
    padding: 0;
  }
  
  .content-area {
    padding: 2rem 1rem 1rem 2rem;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .resource-types {
    grid-template-columns: 1fr;
  }
  
  .spec-options {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
}

/* 暗色主题 */
:global(.dark-theme) .navbar,
:global(.dark-theme) .main-container,
:global(.dark-theme) .application-form,
:global(.dark-theme) .resource-type-card,
:global(.dark-theme) .spec-option {
  background: #1e293b;
  border-color: #334155;
  color: #e2e8f0;
}

:global(.dark-theme) .form-input,
:global(.dark-theme) .form-select,
:global(.dark-theme) .form-textarea {
  background: #334155;
  border-color: #475569;
  color: #e2e8f0;
}

:global(.dark-theme) .resource-type-card:hover,
:global(.dark-theme) .spec-option:hover {
  background: #334155;
}
</style>