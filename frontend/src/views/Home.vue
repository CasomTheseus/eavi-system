<template>
  <div class="home-page">
    <!-- 主要内容 -->
    <div class="main-container">
      <!-- 欢迎部分 -->
      <div class="welcome-section">
        <h1 class="welcome-title">欢迎来到EAVI团队数据管理系统</h1>
        <p class="welcome-subtitle">高效管理团队研究项目，实时跟踪成员进度，促进学术合作与交流</p>
        
        <div class="team-intro">
          <div class="team-description">
            <h3 class="team-title">👥 关于EAVI团队</h3>
            <div class="team-content">
              EAVI（Enhanced Artificial Vision Intelligence）团队是一支专注于计算机视觉与人工智能研究的创新团队。我们致力于推动视觉智能技术的前沿发展，在图像识别、深度学习、机器视觉等领域取得了丰硕的研究成果。
              <br><br>
              团队由8名充满活力的研究人员组成，包括教授、博士生和硕士生。我们秉承"创新、协作、卓越"的理念，在学术研究的同时，注重团队文化建设和成员间的交流合作。通过定期的学术讨论、团队建设活动和技术分享，我们不断提升团队凝聚力和研究水平。
            </div>
          </div>
          
          <div class="team-photos">
            <div class="photo-card">
              <div class="photo-placeholder">📸</div>
              <div class="photo-caption">团队年度学术会议合影</div>
            </div>
            <div class="photo-card">
              <div class="photo-placeholder">🎉</div>
              <div class="photo-caption">项目成果发布庆祝活动</div>
            </div>
            <div class="photo-card">
              <div class="photo-placeholder">🏆</div>
              <div class="photo-caption">获得最佳论文奖颁奖典礼</div>
            </div>
            <div class="photo-card">
              <div class="photo-placeholder">🌟</div>
              <div class="photo-caption">团队户外拓展训练活动</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 世界会议期刊投稿时间 -->
      <div class="conference-section">
        <div class="conference-card">
          <div class="conference-header">
            <h3 class="conference-title">🌍 世界会议期刊投稿时间</h3>
            <div class="conference-controls">
              <div class="year-selector">
                <button class="year-btn" @click="previousYear">‹</button>
                <span class="current-year">{{ currentDisplayYear }}</span>
                <button class="year-btn" @click="nextYear">›</button>
              </div>
              <div class="filter-tabs">
                <button 
                  v-for="filter in filters" 
                  :key="filter.value"
                  class="filter-tab" 
                  :class="{ active: currentFilter === filter.value }"
                  @click="filterSubmissions(filter.value)"
                >
                  {{ filter.label }}
                </button>
              </div>
            </div>
          </div>
          
          <div class="search-box">
            <div class="search-icon">🔍</div>
            <input 
              type="text" 
              class="search-input" 
              placeholder="搜索会议或期刊名称..." 
              v-model="searchQuery"
              @input="searchSubmissions"
            >
          </div>
          
          <div class="stats-overview">
            <div class="stat-item">
              <div class="stat-number">{{ stats.conferences }}</div>
              <div class="stat-label">顶级会议</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ stats.journals }}</div>
              <div class="stat-label">SCI期刊</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ stats.workshops }}</div>
              <div class="stat-label">研讨会</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ stats.urgentDeadlines }}</div>
              <div class="stat-label">本月截止</div>
            </div>
          </div>
          
          <div class="timeline-container">
            <div class="timeline-months">
              <div 
                v-for="(month, index) in filteredTimeline" 
                :key="index"
                class="month-card"
                :style="{ display: month.visible ? 'block' : 'none' }"
              >
                <div class="month-header">
                  <div class="month-name">{{ months[index] }}</div>
                  <div class="month-count">{{ month.submissions.length }}</div>
                </div>
                
                <div class="submissions-list">
                  <div v-if="month.submissions.length === 0" class="empty-month">
                    本月暂无投稿
                  </div>
                  <div 
                    v-else
                    v-for="submission in month.submissions" 
                    :key="submission.name"
                    class="submission-item"
                    :style="{ display: submission.visible ? 'flex' : 'none' }"
                  >
                    <div :class="`submission-icon ${submission.type}`">
                      {{ getSubmissionIcon(submission.type) }}
                    </div>
                    <div class="submission-content">
                      <div class="submission-name">
                        {{ submission.name }}
                        <span :class="`submission-rank ${submission.rank}`">
                          {{ getRankText(submission.rank) }}
                        </span>
                      </div>
                      <div class="submission-details">{{ submission.description }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 功能卡片 -->
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">🗂️</div>
          <h3 class="feature-title">数据集管理</h3>
          <p class="feature-description">管理和组织研究数据集，支持数据上传、分类、标注和共享，提供便捷的数据检索功能。</p>
          <button class="feature-btn" @click="$router.push('/datasets')">数据集管理</button>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">📄</div>
          <h3 class="feature-title">文献发表</h3>
          <p class="feature-description">管理论文投稿流程，跟踪审稿状态，记录发表成果和影响因子，维护文献数据库。</p>
          <button class="feature-btn" @click="$router.push('/literature')">文献管理</button>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">📋</div>
          <h3 class="feature-title">项目管理</h3>
          <p class="feature-description">创建和管理研究项目，设置里程碑，分配任务给团队成员，跟踪项目整体进展。</p>
          <button class="feature-btn" @click="$router.push('/projects')">管理项目</button>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">🖥️</div>
          <h3 class="feature-title">资源管理</h3>
          <p class="feature-description">管理计算资源、设备资源和软件资源，优化资源分配，提高团队工作效率。</p>
          <button class="feature-btn" @click="$router.push('/resources')">资源管理</button>
        </div>
      </div>
      
      <!-- 最近活动 -->
      <div class="recent-activity">
        <h2 class="section-title">
          <div class="section-icon">🕒</div>
          最近活动
        </h2>
        
        <div class="activity-list">
          <div 
            v-for="activity in recentActivities" 
            :key="activity.id"
            class="activity-item"
          >
            <div :class="`activity-icon ${activity.type}`">{{ activity.icon }}</div>
            <div class="activity-content">
              <div class="activity-title">{{ activity.title }}</div>
              <div class="activity-time">{{ activity.time }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      currentDisplayYear: 2025,
      currentFilter: 'all',
      searchQuery: '',
      months: [
        '1月', '2月', '3月', '4月', '5月', '6月',
        '7月', '8月', '9月', '10月', '11月', '12月'
      ],
      filters: [
        { value: 'all', label: '全部' },
        { value: 'conference', label: '会议' },
        { value: 'journal', label: '期刊' },
        { value: 'workshop', label: '研讨会' }
      ],
      submissionsData: {
        2025: {
          1: [
            { name: 'AAAI 2026', type: 'conference', rank: 'ccf-a', description: '人工智能顶级会议' },
            { name: 'IEEE TPAMI', type: 'journal', rank: 'sci-q1', description: '模式分析与机器智能期刊' },
            { name: 'WACV 2026', type: 'conference', rank: 'ccf-b', description: '计算机视觉应用会议' }
          ],
          2: [
            { name: 'ICML 2025', type: 'conference', rank: 'ccf-a', description: '机器学习国际会议' },
            { name: 'Nature Machine Intelligence', type: 'journal', rank: 'sci-q1', description: '自然机器智能期刊' },
            { name: 'ICLR 2026', type: 'conference', rank: 'ccf-a', description: '学习表征国际会议' },
            { name: 'Computer Vision Workshop', type: 'workshop', rank: 'ccf-c', description: '计算机视觉研讨会' }
          ],
          3: [
            { name: 'CVPR 2025', type: 'conference', rank: 'ccf-a', description: '计算机视觉与模式识别' },
            { name: 'IEEE TIP', type: 'journal', rank: 'sci-q1', description: '图像处理期刊' },
            { name: 'ECCV 2025', type: 'conference', rank: 'ccf-a', description: '欧洲计算机视觉会议' }
          ],
          4: [
            { name: 'NeurIPS 2025', type: 'conference', rank: 'ccf-a', description: '神经信息处理系统' },
            { name: 'IEEE TNNLS', type: 'journal', rank: 'sci-q1', description: '神经网络与学习系统' },
            { name: 'IJCAI 2026', type: 'conference', rank: 'ccf-a', description: '国际人工智能联合会议' }
          ],
          5: [
            { name: 'ICCV 2025', type: 'conference', rank: 'ccf-a', description: '国际计算机视觉会议' },
            { name: 'Pattern Recognition', type: 'journal', rank: 'sci-q1', description: '模式识别期刊' },
            { name: 'BMVC 2025', type: 'conference', rank: 'ccf-c', description: '英国机器视觉会议' }
          ],
          6: [
            { name: 'ICIP 2025', type: 'conference', rank: 'ccf-c', description: '图像处理国际会议' },
            { name: 'IEEE TMM', type: 'journal', rank: 'sci-q1', description: '多媒体期刊' },
            { name: 'SIGGRAPH 2026', type: 'conference', rank: 'ccf-a', description: '计算机图形学会议' }
          ],
          7: [
            { name: 'MICCAI 2025', type: 'conference', rank: 'ccf-b', description: '医学图像计算会议' },
            { name: 'Medical Image Analysis', type: 'journal', rank: 'sci-q1', description: '医学图像分析期刊' }
          ],
          8: [
            { name: 'ICDM 2025', type: 'conference', rank: 'ccf-b', description: '数据挖掘国际会议' },
            { name: 'IEEE TKDE', type: 'journal', rank: 'sci-q1', description: '知识与数据工程期刊' },
            { name: 'KDD 2026', type: 'conference', rank: 'ccf-a', description: '知识发现与数据挖掘' }
          ],
          9: [
            { name: 'ACMMM 2025', type: 'conference', rank: 'ccf-a', description: 'ACM多媒体会议' },
            { name: 'IEEE TCSVT', type: 'journal', rank: 'sci-q1', description: '电路系统与视频技术' }
          ],
          10: [
            { name: 'NIPS Workshop', type: 'workshop', rank: 'ccf-c', description: 'NIPS研讨会' },
            { name: 'Neurocomputing', type: 'journal', rank: 'sci-q2', description: '神经计算期刊' },
            { name: 'WACV 2026', type: 'conference', rank: 'ccf-b', description: '计算机视觉应用会议' }
          ],
          11: [
            { name: 'AAAI 2026', type: 'conference', rank: 'ccf-a', description: '人工智能协会会议' },
            { name: 'Artificial Intelligence', type: 'journal', rank: 'sci-q1', description: '人工智能期刊' }
          ],
          12: [
            { name: 'ICPR 2026', type: 'conference', rank: 'ccf-c', description: '模式识别国际会议' },
            { name: 'Computer Vision and Image Understanding', type: 'journal', rank: 'sci-q2', description: '计算机视觉与图像理解' }
          ]
        }
      },
      recentActivities: [
        {
          id: 1,
          type: 'research',
          icon: '🔬',
          title: '张三完成了机器学习算法优化项目的第二阶段',
          time: '2小时前'
        },
        {
          id: 2,
          type: 'paper',
          icon: '📝',
          title: '李四提交了深度学习模型研究论文初稿',
          time: '5小时前'
        },
        {
          id: 3,
          type: 'meeting',
          icon: '👥',
          title: '团队周会：讨论下周研究计划和进度安排',
          time: '1天前'
        },
        {
          id: 4,
          type: 'research',
          icon: '📊',
          title: '王五更新了数据分析项目的实验结果',
          time: '2天前'
        }
      ]
    }
  },
  computed: {
    filteredTimeline() {
      const yearData = this.submissionsData[this.currentDisplayYear] || {}
      const timeline = []
      
      for (let month = 1; month <= 12; month++) {
        const monthData = yearData[month] || []
        const filteredData = this.filterByType(monthData, this.currentFilter)
        
        // 添加搜索过滤
        filteredData.forEach(submission => {
          submission.visible = this.isSubmissionVisible(submission)
        })
        
        const visibleSubmissions = filteredData.filter(s => s.visible)
        
        timeline.push({
          submissions: filteredData,
          visible: this.searchQuery ? visibleSubmissions.length > 0 : true
        })
      }
      
      return timeline
    },
    stats() {
      const yearData = this.submissionsData[this.currentDisplayYear] || {}
      let conferences = 0, journals = 0, workshops = 0
      
      Object.values(yearData).forEach(monthData => {
        monthData.forEach(item => {
          if (item.type === 'conference') conferences++
          else if (item.type === 'journal') journals++
          else if (item.type === 'workshop') workshops++
        })
      })
      
      // 计算本月投稿数量作为紧急提醒
      const currentMonth = new Date().getMonth() + 1
      const currentMonthData = yearData[currentMonth] || []
      
      return {
        conferences,
        journals,
        workshops,
        urgentDeadlines: currentMonthData.length
      }
    }
  },
  methods: {
    previousYear() {
      this.currentDisplayYear--
    },
    nextYear() {
      this.currentDisplayYear++
    },
    filterSubmissions(type) {
      this.currentFilter = type
    },
    filterByType(data, type) {
      if (type === 'all') return [...data]
      return data.filter(item => item.type === type)
    },
    isSubmissionVisible(submission) {
      if (!this.searchQuery) return true
      
      const query = this.searchQuery.toLowerCase()
      const name = submission.name.toLowerCase()
      const description = submission.description.toLowerCase()
      
      return name.includes(query) || description.includes(query)
    },
    searchSubmissions() {
      // 触发计算属性重新计算
    },
    getSubmissionIcon(type) {
      switch(type) {
        case 'conference': return '🏛️'
        case 'journal': return '📄'
        case 'workshop': return '🔬'
        default: return '📋'
      }
    },
    getRankText(rank) {
      switch(rank) {
        case 'ccf-a': return 'CCF-A'
        case 'ccf-b': return 'CCF-B'
        case 'ccf-c': return 'CCF-C'
        case 'sci-q1': return 'SCI Q1'
        case 'sci-q2': return 'SCI Q2'
        default: return ''
      }
    }
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  position: relative;
}

/* 主要内容区域 */
.main-container {
  width: 80%;
  max-width: none;
  margin: 2rem auto;
  padding: 0 2rem;
  position: relative;
  z-index: 10;
}

.welcome-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 3rem 2.5rem;
  margin-bottom: 2rem;
  box-shadow: 
    0 25px 80px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-align: center;
}

.welcome-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 1rem;
}

.welcome-subtitle {
  font-size: 1.2rem;
  color: #64748b;
  margin-bottom: 2rem;
}

/* 团队介绍样式 */
.team-intro {
  margin-top: 2rem;
}

.team-description {
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 1px solid rgba(226, 232, 240, 0.5);
  text-align: left;
}

.team-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.team-content {
  color: #64748b;
  line-height: 1.8;
  font-size: 1rem;
}

.team-photos {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-top: 1.5rem;
}

.photo-card {
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  border-radius: 16px;
  padding: 1rem;
  border: 1px solid rgba(226, 232, 240, 0.5);
  transition: all 0.3s ease;
  overflow: hidden;
}

.photo-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(59, 130, 246, 0.15);
}

.photo-placeholder {
  width: 100%;
  height: 180px;
  background: linear-gradient(135deg, #e2e8f0 0%, #f1f5f9 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  color: #94a3b8;
  margin-bottom: 1rem;
  position: relative;
  overflow: hidden;
}

.photo-placeholder::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}

.photo-caption {
  font-size: 0.9rem;
  color: #64748b;
  text-align: center;
  font-weight: 500;
}

/* 会议期刊组件样式 */
.conference-section {
  margin-bottom: 2rem;
}

.conference-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 
    0 25px 80px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.conference-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.conference-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.conference-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.year-selector {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.year-btn {
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  color: white;
  border: none;
  border-radius: 8px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.2s ease;
}

.year-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.current-year {
  font-weight: 600;
  color: #1e293b;
  min-width: 60px;
  text-align: center;
  font-size: 1.1rem;
}

.filter-tabs {
  display: flex;
  background: #f1f5f9;
  border-radius: 12px;
  padding: 4px;
  gap: 4px;
}

.filter-tab {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #64748b;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.875rem;
}

.filter-tab.active {
  background: white;
  color: #1e293b;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filter-tab:hover:not(.active) {
  color: #1e293b;
}

.timeline-container {
  margin-bottom: 2rem;
}

.timeline-months {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.month-card {
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  border-radius: 16px;
  padding: 1.5rem;
  border: 1px solid rgba(226, 232, 240, 0.5);
  transition: all 0.3s ease;
}

.month-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(59, 130, 246, 0.15);
}

.month-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.month-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e293b;
}

.month-count {
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.submissions-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.submission-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.75rem;
  background: white;
  border-radius: 8px;
  border: 1px solid rgba(226, 232, 240, 0.5);
  transition: all 0.2s ease;
}

.submission-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.submission-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  flex-shrink: 0;
}

.submission-icon.conference {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  color: white;
}

.submission-icon.journal {
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  color: white;
}

.submission-icon.workshop {
  background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
  color: white;
}

.submission-content {
  flex: 1;
  min-width: 0;
}

.submission-name {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.25rem;
  font-size: 0.875rem;
  line-height: 1.3;
}

.submission-details {
  font-size: 0.75rem;
  color: #64748b;
  line-height: 1.4;
}

.submission-rank {
  background: #fef3c7;
  color: #92400e;
  padding: 0.125rem 0.375rem;
  border-radius: 4px;
  font-size: 0.625rem;
  font-weight: 600;
  margin-left: 0.5rem;
}

.submission-rank.ccf-a {
  background: #dcfce7;
  color: #166534;
}

.submission-rank.ccf-b {
  background: #dbeafe;
  color: #1e40af;
}

.submission-rank.ccf-c {
  background: #fef3c7;
  color: #92400e;
}

.submission-rank.sci-q1 {
  background: #f3e8ff;
  color: #7c3aed;
}

.submission-rank.sci-q2 {
  background: #ecfdf5;
  color: #059669;
}

.empty-month {
  text-align: center;
  color: #94a3b8;
  font-style: italic;
  padding: 2rem;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.5);
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 500;
}

.search-box {
  position: relative;
  margin-bottom: 1rem;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  border: 1px solid rgba(226, 232, 240, 0.5);
  border-radius: 12px;
  background: white;
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  font-size: 1rem;
}

/* 功能卡片网格 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.feature-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 
    0 25px 80px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 
    0 35px 100px rgba(0, 0, 0, 0.2),
    0 0 0 1px rgba(255, 255, 255, 0.1);
}

.feature-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 
    0 8px 32px rgba(59, 130, 246, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.feature-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.75rem;
}

.feature-description {
  color: #64748b;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.feature-btn {
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 0.75rem 1.5rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 
    0 8px 32px rgba(59, 130, 246, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.feature-btn:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 12px 40px rgba(59, 130, 246, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

/* 最近活动部分 */
.recent-activity {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 
    0 25px 80px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.section-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1rem;
}

.activity-list {
  space-y: 1rem;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-radius: 12px;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  border: 1px solid rgba(226, 232, 240, 0.5);
  margin-bottom: 1rem;
  transition: all 0.2s ease;
}

.activity-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  flex-shrink: 0;
}

.activity-icon.research {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  color: white;
}

.activity-icon.paper {
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  color: white;
}

.activity-icon.meeting {
  background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
  color: white;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.25rem;
}

.activity-time {
  font-size: 0.875rem;
  color: #64748b;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-container {
    padding: 0 1rem;
    margin: 1rem auto;
  }
  
  .welcome-section {
    padding: 2rem 1.5rem;
  }
  
  .welcome-title {
    font-size: 2rem;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .team-photos {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .team-photos {
    grid-template-columns: 1fr;
  }
  
  .welcome-title {
    font-size: 1.75rem;
  }
  
  .feature-card {
    padding: 1.5rem;
  }
}
</style>