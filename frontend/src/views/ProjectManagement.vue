<template>
  <div class="project-management">


    <!-- 浮动装饰元素 -->
    <div class="floating-books">
      <div class="floating-book book1">📊</div>
      <div class="floating-book book2">📈</div>
      <div class="floating-book book3">📋</div>
      <div class="floating-book book4">🎯</div>
      <div class="floating-book book5">⚡</div>
      <div class="floating-book book6">🔧</div>
    </div>

    <div class="main-container">
      <!-- 侧边栏 -->
      <div class="sidebar book-spine">
        <div class="sidebar-header">
          <h3>项目分类</h3>
        </div>
        <div class="sidebar-content">
          <div class="sidebar-section">
            <h4>项目类型</h4>
            <div 
              v-for="category in projectCategories" 
              :key="category.id"
              class="sidebar-item"
              :class="{ active: selectedCategory === category.id }"
              @click="selectCategory(category.id)"
            >
              <span class="sidebar-icon">{{ category.icon }}</span>
              <span class="sidebar-text">{{ category.name }}</span>
              <span class="sidebar-count">{{ category.count }}</span>
            </div>
          </div>
          
          <div class="sidebar-section">
            <h4>项目状态</h4>
            <div 
              v-for="status in projectStatuses" 
              :key="status.id"
              class="sidebar-item"
              :class="{ active: selectedStatus === status.id }"
              @click="selectStatus(status.id)"
            >
              <span class="sidebar-icon">{{ status.icon }}</span>
              <span class="sidebar-text">{{ status.name }}</span>
              <span class="sidebar-count">{{ status.count }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="main-content">
        <div class="content-header">
          <h1>项目管理</h1>
          <button class="new-project-btn" @click="createNewProject">
            <span class="btn-icon">+</span>
            新建项目
          </button>
        </div>

        <!-- 搜索和筛选栏 -->
        <div class="search-filter-bar">
          <div class="search-box">
            <input 
              type="text" 
              class="search-input" 
              placeholder="搜索项目名称、负责人或描述..."
              v-model="searchTerm"
              @input="handleSearch"
            >
            <span class="search-icon">🔍</span>
          </div>
          <div class="filter-controls">
            <select class="filter-select" v-model="sortBy" @change="handleSort">
              <option value="name">按名称排序</option>
              <option value="date">按创建时间</option>
              <option value="progress">按进度排序</option>
              <option value="leader">按负责人</option>
            </select>
            <div class="view-toggle">
              <button 
                class="view-btn"
                :class="{ active: viewMode === 'grid' }"
                @click="setViewMode('grid')"
              >
                ⊞
              </button>
              <button 
                class="view-btn"
                :class="{ active: viewMode === 'list' }"
                @click="setViewMode('list')"
              >
                ☰
              </button>
            </div>
          </div>
        </div>

        <!-- 分类筛选 -->
        <div class="category-filter">
          <div 
            v-for="category in allCategories" 
            :key="category.id"
            class="category-item"
            :class="{ active: activeCategory === category.id }"
            @click="setActiveCategory(category.id)"
          >
            {{ category.name }}
          </div>
        </div>

        <!-- 项目统计 -->
        <div class="project-stats">
          <div class="stat-item">
            <span class="stat-number">{{ totalProjects }}</span>
            <span class="stat-label">总项目数</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ activeProjects }}</span>
            <span class="stat-label">进行中</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ completedProjects }}</span>
            <span class="stat-label">已完成</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ averageProgress }}%</span>
            <span class="stat-label">平均进度</span>
          </div>
        </div>

        <!-- 项目网格 -->
        <div class="projects-grid" :class="{ 'list-view': viewMode === 'list' }">
          <div 
            v-for="project in filteredProjects" 
            :key="project.id"
            class="project-card"
            @click="openProjectDetails(project)"
          >
            <div class="project-header">
              <div class="project-name">{{ project.name }}</div>
              <div class="project-status" :class="`status-${project.status}`">
                {{ getStatusText(project.status) }}
              </div>
            </div>
            <div class="project-info">
              {{ project.description }}
            </div>
            <div class="project-leader">
              <div class="leader-avatar">{{ project.leader.charAt(0) }}</div>
              <span class="leader-name">项目负责人：{{ project.leader }}</span>
            </div>
            <div class="project-team">
              <span style="font-size: 0.75rem; color: #64748b;">团队成员：</span>
              <div class="team-avatars">
                <div 
                  v-for="member in project.team.slice(0, 4)" 
                  :key="member"
                  class="team-avatar"
                >
                  {{ member.charAt(0) }}
                </div>
              </div>
              <span v-if="project.team.length > 4" class="team-count">
                +{{ project.team.length - 4 }}
              </span>
            </div>
            <div class="project-tags">
              <span 
                v-for="tag in project.tags" 
                :key="tag"
                class="tag"
              >
                {{ tag }}
              </span>
            </div>
            <div class="project-progress">
              <div class="progress-label">
                <span>项目进度</span>
                <span>{{ project.progress }}%</span>
              </div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: project.progress + '%' }"></div>
              </div>
            </div>
            <div class="project-meta">
              <span>开始时间：{{ project.startDate }}</span>
              <span>预计完成：{{ project.endDate }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getAllProjects } from '../data/projects.js'

export default {
  name: 'ProjectManagement',
  data() {
    return {
      searchTerm: '',
      sortBy: 'name',
      viewMode: 'grid',
      selectedCategory: 'all',
      selectedStatus: 'all',
      activeCategory: 'all',
      
      allCategories: [
        { id: 'all', name: '全部' },
        { id: 'ai', name: 'AI项目' },
        { id: 'web', name: 'Web开发' },
        { id: 'mobile', name: '移动应用' },
        { id: 'data', name: '数据分析' }
      ],
      
      // 内置项目 + 用户自定义项目（localStorage 持久化）
      projects: getAllProjects()
    }
  },
  
  computed: {
    projectCategories() {
      const base = [
        { id: 'all', name: '全部项目', icon: '📁' },
        { id: 'ai', name: 'AI项目', icon: '🤖' },
        { id: 'web', name: 'Web开发', icon: '🌐' },
        { id: 'mobile', name: '移动应用', icon: '📱' },
        { id: 'data', name: '数据分析', icon: '📊' }
      ]
      return base.map(c => ({
        ...c,
        count: c.id === 'all' ? this.projects.length : this.projects.filter(p => p.category === c.id).length
      }))
    },
    
    projectStatuses() {
      const base = [
        { id: 'all', name: '全部状态', icon: '📊' },
        { id: 'active', name: '进行中', icon: '🟢' },
        { id: 'planning', name: '规划中', icon: '🟡' },
        { id: 'completed', name: '已完成', icon: '✅' },
        { id: 'paused', name: '已暂停', icon: '⏸' }
      ]
      return base.map(s => ({
        ...s,
        count: s.id === 'all' ? this.projects.length : this.projects.filter(p => p.status === s.id).length
      }))
    },
    filteredProjects() {
      let filtered = this.projects;
      
      // 按分类筛选
      if (this.activeCategory !== 'all') {
        filtered = filtered.filter(project => project.category === this.activeCategory);
      }
      
      // 按状态筛选
      if (this.selectedStatus !== 'all') {
        filtered = filtered.filter(project => project.status === this.selectedStatus);
      }
      
      // 搜索筛选
      if (this.searchTerm) {
        const term = this.searchTerm.toLowerCase();
        filtered = filtered.filter(project => 
          project.name.toLowerCase().includes(term) ||
          project.description.toLowerCase().includes(term) ||
          project.leader.toLowerCase().includes(term)
        );
      }
      
      // 排序
      filtered.sort((a, b) => {
        switch (this.sortBy) {
          case 'name':
            return a.name.localeCompare(b.name);
          case 'date':
            return new Date(b.startDate) - new Date(a.startDate);
          case 'progress':
            return b.progress - a.progress;
          case 'leader':
            return a.leader.localeCompare(b.leader);
          default:
            return 0;
        }
      });
      
      return filtered;
    },
    
    totalProjects() {
      return this.projects.length;
    },
    
    activeProjects() {
      return this.projects.filter(p => p.status === 'active').length;
    },
    
    completedProjects() {
      return this.projects.filter(p => p.status === 'completed').length;
    },
    
    averageProgress() {
      const total = this.projects.reduce((sum, p) => sum + p.progress, 0);
      return Math.round(total / this.projects.length);
    }
  },
  
  methods: {
    selectCategory(categoryId) {
      this.selectedCategory = categoryId;
    },
    
    selectStatus(statusId) {
      this.selectedStatus = statusId;
    },
    
    setActiveCategory(categoryId) {
      this.activeCategory = categoryId;
    },
    
    setViewMode(mode) {
      this.viewMode = mode;
    },
    
    handleSearch() {
      // 搜索逻辑已在computed中处理
    },
    
    handleSort() {
      // 排序逻辑已在computed中处理
    },
    
    createNewProject() {
      // 跳转到新建项目页面
      this.$router.push({
        name: 'ProjectCreate'
      });
    },
    
    openProjectDetails(project) {
      // 跳转到项目详情页面
      this.$router.push({
        name: 'ProjectDetail',
        params: {
          id: project.id
        }
      });
    },
    
    getStatusText(status) {
      const statusMap = {
        'active': '进行中',
        'planning': '规划中',
        'completed': '已完成',
        'paused': '已暂停'
      };
      return statusMap[status] || status;
    }
  }
}
</script>

<style scoped>
/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

body::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(120, 219, 255, 0.2) 0%, transparent 50%);
  z-index: -1;
  animation: backgroundShift 20s ease-in-out infinite;
}

@keyframes backgroundShift {
  0%, 100% { transform: translateX(0) translateY(0); }
  25% { transform: translateX(-20px) translateY(-10px); }
  50% { transform: translateX(20px) translateY(10px); }
  75% { transform: translateX(-10px) translateY(20px); }
}



/* 浮动装饰元素 */
.floating-books {
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
  animation: float 20s infinite linear;
}

.book1 { top: 10%; left: 10%; animation-delay: 0s; }
.book2 { top: 20%; right: 10%; animation-delay: -5s; }
.book3 { top: 60%; left: 5%; animation-delay: -10s; }
.book4 { top: 70%; right: 20%; animation-delay: -15s; }
.book5 { top: 40%; left: 80%; animation-delay: -8s; }
.book6 { top: 80%; left: 70%; animation-delay: -12s; }

@keyframes float {
  0% { transform: translateY(0px) rotate(0deg); }
  25% { transform: translateY(-20px) rotate(90deg); }
  50% { transform: translateY(0px) rotate(180deg); }
  75% { transform: translateY(-10px) rotate(270deg); }
  100% { transform: translateY(0px) rotate(360deg); }
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

/* 侧边栏样式 */
.sidebar {
  width: 280px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 2rem 0;
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.1);
  position: relative;
}

.book-spine {
  background: linear-gradient(180deg, 
    rgba(102, 126, 234, 0.1) 0%,
    rgba(118, 75, 162, 0.1) 100%);
  border-right: 4px solid rgba(102, 126, 234, 0.3);
}

.book-spine::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 2px;
  height: 100%;
  background: linear-gradient(180deg, 
    rgba(102, 126, 234, 0.5) 0%,
    rgba(118, 75, 162, 0.5) 100%);
}

.sidebar-header {
  padding: 0 2rem 1rem;
  border-bottom: 1px solid rgba(102, 126, 234, 0.2);
  margin-bottom: 1rem;
}

.sidebar-header h3 {
  color: #334155;
  font-size: 1.1rem;
  font-weight: 600;
}

.sidebar-content {
  padding: 0 1rem;
}

.sidebar-section {
  margin-bottom: 2rem;
}

.sidebar-section h4 {
  color: #64748b;
  font-size: 0.9rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  padding: 0 1rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  margin: 0.25rem 0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.sidebar-item:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateX(4px);
}

.sidebar-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2), rgba(118, 75, 162, 0.2));
  color: #667eea;
  font-weight: 600;
}

.sidebar-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 0 2px 2px 0;
}

.sidebar-icon {
  margin-right: 0.75rem;
  font-size: 1.1rem;
}

.sidebar-text {
  flex: 1;
  font-size: 0.9rem;
}

.sidebar-count {
  background: rgba(102, 126, 234, 0.2);
  color: #667eea;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  min-width: 20px;
  text-align: center;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 2rem;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  margin: 1rem;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.content-header h1 {
  color: #1e293b;
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.new-project-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.new-project-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-icon {
  font-size: 1.2rem;
  font-weight: bold;
}

/* 搜索和筛选栏 */
.search-filter-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  align-items: center;
}

.search-box {
  flex: 1;
  position: relative;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  font-size: 0.9rem;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
  font-size: 1rem;
}

.filter-controls {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.filter-select {
  padding: 0.75rem 1rem;
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #667eea;
}

.view-toggle {
  display: flex;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  padding: 0.25rem;
  border: 2px solid rgba(102, 126, 234, 0.2);
}

.view-btn {
  padding: 0.5rem 0.75rem;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1rem;
  color: #64748b;
}

.view-btn.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

/* 分类筛选 */
.category-filter {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.category-item {
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.8);
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  font-weight: 500;
  color: #64748b;
}

.category-item:hover {
  border-color: #667eea;
  transform: translateY(-1px);
}

.category-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

/* 项目统计 */
.project-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-item {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  padding: 1.5rem;
  border-radius: 12px;
  text-align: center;
  border: 2px solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.15);
}

.stat-number {
  display: block;
  font-size: 2rem;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 0.5rem;
}

.stat-label {
  color: #64748b;
  font-size: 0.9rem;
  font-weight: 500;
}

/* 项目网格 */
.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
}

.project-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
  cursor: pointer;
}

.project-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: #667eea;
}

.project-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.project-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 1.1rem;
}

.project-status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-active {
  background: #dcfce7;
  color: #166534;
}

.status-planning {
  background: #fef3c7;
  color: #92400e;
}

.status-completed {
  background: #dbeafe;
  color: #1e40af;
}

.status-paused {
  background: #fecaca;
  color: #991b1b;
}

.project-info {
  color: #64748b;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.project-leader {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.leader-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.75rem;
  font-weight: 600;
}

.leader-name {
  font-size: 0.875rem;
  color: #64748b;
}

.project-team {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.team-avatars {
  display: flex;
  gap: -0.25rem;
}

.team-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.7rem;
  font-weight: 600;
  border: 2px solid white;
  margin-left: -0.25rem;
}

.team-count {
  font-size: 0.75rem;
  color: #64748b;
  margin-left: 0.5rem;
}

.project-tags {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.tag {
  background: #f1f5f9;
  color: #64748b;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
}

.project-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #94a3b8;
  margin-bottom: 1rem;
}

.project-progress {
  margin-bottom: 1rem;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: #64748b;
  margin-bottom: 0.25rem;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.project-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

.btn {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-secondary {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-secondary:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.leader-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.9rem;
  margin-right: 0.75rem;
}

.leader-name {
  font-size: 0.85rem;
  color: #475569;
  font-weight: 500;
}

.project-team {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.team-avatars {
  display: flex;
  margin-left: 0.5rem;
}

.team-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #94a3b8, #64748b);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.7rem;
  margin-left: -4px;
  border: 2px solid white;
}

.team-count {
  font-size: 0.75rem;
  color: #64748b;
  margin-left: 0.5rem;
  font-weight: 500;
}

.project-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tag {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.project-progress {
  margin-bottom: 1rem;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  color: #475569;
  font-weight: 500;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.project-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: #64748b;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    order: 2;
  }
  
  .main-content {
    order: 1;
    margin: 1rem 0;
  }
  
  .projects-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
}

@media (max-width: 768px) {
  .navbar {
    padding: 1rem;
    flex-wrap: wrap;
  }
  
  .nav-links {
    order: 3;
    width: 100%;
    margin-top: 1rem;
    justify-content: center;
  }
  
  .search-filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-controls {
    justify-content: space-between;
  }
  
  .projects-grid {
    grid-template-columns: 1fr;
  }
  
  .project-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 1rem;
    margin: 0.5rem;
  }
  
  .content-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .project-stats {
    grid-template-columns: 1fr;
  }
  
  .category-filter {
    justify-content: center;
  }
}

/* 暗色主题 */
.dark-theme {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.dark-theme .navbar {
  background: rgba(30, 41, 59, 0.95);
}

.dark-theme .nav-link {
  color: #94a3b8;
}

.dark-theme .nav-link:hover,
.dark-theme .nav-link.active {
  color: #667eea;
}

.dark-theme .username {
  color: #94a3b8;
}

.dark-theme .sidebar {
  background: rgba(30, 41, 59, 0.95);
}

.dark-theme .sidebar-header h3 {
  color: #e2e8f0;
}

.dark-theme .sidebar-section h4 {
  color: #94a3b8;
}

.dark-theme .sidebar-item {
  color: #cbd5e1;
}

.dark-theme .sidebar-item:hover {
  background: rgba(102, 126, 234, 0.2);
}

.dark-theme .main-content {
  background: rgba(30, 41, 59, 0.9);
}

.dark-theme .content-header h1 {
  color: #e2e8f0;
}

.dark-theme .search-input,
.dark-theme .filter-select {
  background: rgba(51, 65, 85, 0.8);
  border-color: rgba(102, 126, 234, 0.3);
  color: #e2e8f0;
}

.dark-theme .view-toggle {
  background: rgba(51, 65, 85, 0.8);
  border-color: rgba(102, 126, 234, 0.3);
}

.dark-theme .view-btn {
  color: #94a3b8;
}

.dark-theme .category-item {
  background: rgba(51, 65, 85, 0.8);
  border-color: rgba(102, 126, 234, 0.3);
  color: #cbd5e1;
}

.dark-theme .stat-item {
  background: rgba(51, 65, 85, 0.8);
  border-color: rgba(102, 126, 234, 0.2);
}

.dark-theme .stat-label {
  color: #94a3b8;
}

.dark-theme .project-card {
  background: rgba(51, 65, 85, 0.9);
  border-color: rgba(102, 126, 234, 0.2);
}

.dark-theme .project-name {
  color: #e2e8f0;
}

.dark-theme .project-info {
  color: #94a3b8;
}

.dark-theme .leader-name {
  color: #cbd5e1;
}

.dark-theme .progress-label {
  color: #cbd5e1;
}

.dark-theme .project-meta {
  color: #94a3b8;
}
</style>