<template>
  <div class="project-create-page">
    <!-- 顶部导航 -->
    <div class="nc-topbar">
      <button class="nc-back" @click="goBack">
        <span class="nc-back-icon">←</span>
        返回项目管理
      </button>
      <div class="nc-breadcrumb">
        <span class="nc-breadcrumb-link" @click="goBack">项目管理</span>
        <span class="nc-breadcrumb-sep">/</span>
        <span class="nc-breadcrumb-current">新建项目</span>
      </div>
    </div>

    <div class="nc-wrap">
      <div class="nc-head">
        <div class="nc-head-icon">🚀</div>
        <div>
          <h1 class="nc-head-title">新建项目</h1>
          <p class="nc-head-sub">填写项目信息，创建后自动生成项目详情页，创建者默认为项目负责人</p>
        </div>
      </div>

      <!-- 基本信息 -->
      <section class="card nc-card">
        <h3 class="nc-card-title"><span class="nc-card-icon">📋</span>基本信息</h3>
        <div class="nc-form-grid">
          <div class="nc-field nc-field-full">
            <label class="nc-label">项目名称 <i class="nc-required">*</i></label>
            <input v-model.trim="form.name" class="nc-input" placeholder="请输入项目名称，如：智能数据分析平台" maxlength="50" />
          </div>
          <div class="nc-field nc-field-full">
            <label class="nc-label">项目简介</label>
            <input v-model.trim="form.subtitle" class="nc-input" placeholder="一句话介绍项目亮点，将显示在详情页标题下方" maxlength="100" />
          </div>
          <div class="nc-field">
            <label class="nc-label">项目类别</label>
            <select v-model="form.category" class="nc-input nc-select">
              <option value="ai">🤖 AI项目</option>
              <option value="web">🌐 Web开发</option>
              <option value="mobile">📱 移动应用</option>
              <option value="data">📊 数据分析</option>
            </select>
          </div>
          <div class="nc-field">
            <label class="nc-label">项目状态</label>
            <select v-model="form.status" class="nc-input nc-select">
              <option value="planning">🟡 规划中</option>
              <option value="active">🟢 进行中</option>
              <option value="paused">⏸ 已暂停</option>
              <option value="completed">✅ 已完成</option>
            </select>
          </div>
          <div class="nc-field">
            <label class="nc-label">开始日期</label>
            <input v-model="form.startDate" type="date" class="nc-input" />
          </div>
          <div class="nc-field">
            <label class="nc-label">结束日期</label>
            <input v-model="form.endDate" type="date" class="nc-input" />
          </div>
          <div class="nc-field">
            <label class="nc-label">发布者</label>
            <input v-model.trim="form.publisher" class="nc-input" placeholder="如：EAVI 团队数据管理委员会" />
          </div>
          <div class="nc-field">
            <label class="nc-label">项目负责人</label>
            <div class="nc-leader">
              <span class="nc-leader-avatar">{{ currentUserInitial }}</span>
              <span class="nc-leader-name">{{ currentUserName }}</span>
              <span class="nc-leader-tag">默认负责人</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 项目介绍 -->
      <section class="card nc-card">
        <h3 class="nc-card-title"><span class="nc-card-icon">📖</span>项目介绍</h3>
        <div class="nc-form-grid">
          <div class="nc-field nc-field-full">
            <label class="nc-label">项目概述</label>
            <textarea v-model.trim="form.overview" class="nc-input nc-textarea" rows="3" placeholder="详细介绍项目背景、目标与价值"></textarea>
          </div>
          <div class="nc-field nc-field-full">
            <label class="nc-label">核心功能</label>
            <textarea v-model.trim="form.featuresText" class="nc-input nc-textarea" rows="3" placeholder="每行填写一项核心功能，如：&#10;支持多维度数据可视化&#10;实时数据流处理"></textarea>
          </div>
          <div class="nc-field nc-field-full">
            <label class="nc-label">技术架构</label>
            <textarea v-model.trim="form.architecture" class="nc-input nc-textarea" rows="3" placeholder="描述项目整体技术架构与设计方案"></textarea>
          </div>
          <div class="nc-field nc-field-full">
            <label class="nc-label">应用场景</label>
            <textarea v-model.trim="form.scenarios" class="nc-input nc-textarea" rows="3" placeholder="描述项目的典型应用场景与目标用户"></textarea>
          </div>
          <div class="nc-field nc-field-full">
            <label class="nc-label">技术栈标签</label>
            <input v-model.trim="form.techStackText" class="nc-input" placeholder="多个标签用逗号分隔，如：Vue3, Node.js, 机器学习" />
          </div>
        </div>
      </section>

      <!-- 团队成员 -->
      <section class="card nc-card">
        <h3 class="nc-card-title">
          <span class="nc-card-icon">👥</span>团队成员
          <span class="nc-count">{{ selectedMembers.length + 1 }} 人</span>
        </h3>
        <div class="nc-member-grid">
          <div class="nc-member nc-member-leader" @click="toggleMember(currentUser.username)">
            <span class="nc-member-avatar">{{ currentUserInitial }}</span>
            <div class="nc-member-info">
              <span class="nc-member-name">{{ currentUserName }} <i class="nc-member-badge">负责人</i></span>
              <span class="nc-member-role">项目负责人</span>
            </div>
            <span class="nc-member-check">✓</span>
          </div>
          <div
            v-for="u in memberCandidates"
            :key="u.username"
            class="nc-member"
            :class="{ 'is-selected': selectedMembers.includes(u.username) }"
            @click="toggleMember(u.username)"
          >
            <span class="nc-member-avatar">{{ u.name.charAt(0) }}</span>
            <div class="nc-member-info">
              <span class="nc-member-name">{{ u.name }}</span>
              <span class="nc-member-role">项目成员</span>
            </div>
            <span v-if="selectedMembers.includes(u.username)" class="nc-member-check">✓</span>
          </div>
        </div>
      </section>

      <!-- 项目里程碑 -->
      <section class="card nc-card">
        <h3 class="nc-card-title"><span class="nc-card-icon">🎯</span>项目里程碑</h3>
        <div v-for="(m, i) in form.milestones" :key="i" class="nc-milestone-row">
          <input v-model.trim="m.title" class="nc-input nc-milestone-title" placeholder="里程碑名称，如：需求分析" />
          <input v-model.trim="m.date" class="nc-input nc-milestone-date" placeholder="日期范围，如：2024-01-01 ~ 2024-02-28" />
          <select v-model="m.status" class="nc-input nc-select nc-milestone-status">
            <option value="pending">待开始</option>
            <option value="current">进行中</option>
            <option value="completed">已完成</option>
          </select>
          <button class="nc-icon-btn" title="删除该里程碑" @click="removeMilestone(i)">🗑</button>
        </div>
        <button class="nc-add-btn" @click="addMilestone">＋ 添加里程碑</button>
      </section>

      <!-- 项目文件 -->
      <section class="card nc-card">
        <h3 class="nc-card-title"><span class="nc-card-icon">📁</span>项目文件</h3>
        <div class="nc-upload-area" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleDrop">
          <span class="nc-upload-icon">⬆️</span>
          <span>点击上传或拖拽文件到此处</span>
          <span class="nc-upload-hint">可上传多个文件</span>
          <input ref="fileInput" type="file" multiple class="nc-file-input-hidden" @change="handleFileSelected" />
        </div>
        <div v-if="form.files.length" class="nc-file-list">
          <div v-for="(f, i) in form.files" :key="i" class="nc-file-item">
            <span class="nc-file-icon">{{ fileIcon(f.type) }}</span>
            <span class="nc-file-name" :title="f.name">{{ f.name }}</span>
            <span class="nc-file-meta">{{ f.size }}</span>
            <button class="nc-icon-btn" title="移除文件" @click="removeFile(i)">✕</button>
          </div>
        </div>
        <div v-else class="nc-empty">暂无文件</div>
      </section>

      <!-- 操作栏 -->
      <div class="nc-actions">
        <button class="nc-btn-primary" :disabled="submitting" @click="submit">
          {{ submitting ? '正在创建...' : '🚀 创建项目' }}
        </button>
        <button class="nc-btn-ghost" @click="goBack">取消</button>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuth } from '../composables/useAuth.js'
import { createProject, categoryMap, statusMap } from '../data/projects.js'

export default {
  name: 'ProjectCreate',
  data() {
    return {
      submitting: false,
      // 可选成员候选池（不含当前登录用户）
      memberCandidates: [
        { id: 2, username: 'lisi', name: '李四' },
        { id: 3, username: 'wangwu', name: '王五' },
        { id: 4, username: 'zhaoliu', name: '赵六' },
        { id: 7, username: 'chenqi', name: '陈七' }
      ],
      selectedMembers: [],
      form: {
        name: '',
        subtitle: '',
        category: 'ai',
        status: 'planning',
        startDate: '',
        endDate: '',
        publisher: 'EAVI 团队数据管理委员会',
        overview: '',
        featuresText: '',
        architecture: '',
        scenarios: '',
        techStackText: '',
        milestones: [],
        files: []
      }
    }
  },
  computed: {
    currentUser() {
      const { user } = useAuth()
      return user.value
    },
    currentUserName() {
      const u = this.currentUser
      return (u && (u.name || u.username)) || '未知用户'
    },
    currentUserInitial() {
      return (this.currentUserName || '?').charAt(0)
    }
  },
  methods: {
    goBack() {
      if (window.history.length > 1) {
        this.$router.back()
      } else {
        this.$router.push({ name: 'ProjectManagement' })
      }
    },
    toggleMember(username) {
      const idx = this.selectedMembers.indexOf(username)
      if (idx >= 0) this.selectedMembers.splice(idx, 1)
      else this.selectedMembers.push(username)
    },
    addMilestone() {
      this.form.milestones.push({ title: '', date: '', status: 'pending' })
    },
    removeMilestone(i) {
      this.form.milestones.splice(i, 1)
    },
    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    handleDrop(e) {
      const files = e.dataTransfer && e.dataTransfer.files
      if (files && files.length) this.appendFiles(files)
    },
    handleFileSelected(e) {
      if (e.target.files && e.target.files.length) this.appendFiles(e.target.files)
      e.target.value = ''
    },
    appendFiles(fileList) {
      const u = this.currentUser || { name: '未知用户' }
      const now = new Date()
      const time = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
      Array.from(fileList).forEach(file => {
        this.form.files.push({
          name: file.name,
          size: this.formatSize(file.size),
          uploader: u.name || u.username,
          uploadedAt: time,
          type: this.fileTypeFromName(file.name)
        })
      })
    },
    removeFile(i) {
      this.form.files.splice(i, 1)
    },
    formatSize(bytes) {
      if (!bytes && bytes !== 0) return '未知大小'
      if (bytes < 1024) return bytes + ' B'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
      return (bytes / 1024 / 1024).toFixed(1) + ' MB'
    },
    fileTypeFromName(name) {
      const ext = (name.split('.').pop() || '').toLowerCase()
      if (['png', 'jpg', 'jpeg', 'gif', 'webp', 'svg'].includes(ext)) return 'image'
      if (['pdf'].includes(ext)) return 'pdf'
      if (['doc', 'docx'].includes(ext)) return 'doc'
      if (['xls', 'xlsx', 'csv'].includes(ext)) return 'sheet'
      if (['ppt', 'pptx'].includes(ext)) return 'ppt'
      if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) return 'zip'
      if (['mp4', 'avi', 'mov', 'mkv'].includes(ext)) return 'video'
      if (['mp3', 'wav', 'flac'].includes(ext)) return 'audio'
      if (['js', 'ts', 'vue', 'java', 'py', 'go', 'c', 'cpp', 'html', 'css', 'json', 'sql'].includes(ext)) return 'code'
      return 'file'
    },
    fileIcon(type) {
      const map = {
        image: '🖼️', pdf: '📄', doc: '📝', sheet: '📊', ppt: '📽️',
        zip: '🗜️', video: '🎬', audio: '🎵', code: '💻', file: '📁'
      }
      return map[type] || '📁'
    },
    submit() {
      if (this.submitting) return
      if (!this.form.name) {
        this.showToast('请填写项目名称', 'warning')
        return
      }
      this.submitting = true

      const u = this.currentUser || { id: '', username: '', name: '未知用户' }
      const now = new Date()
      const time = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`

      // 构建成员列表：负责人 + 选中的成员
      const members = [
        {
          id: u.id || 0,
          username: u.username || '',
          name: u.name || u.username || '未知用户',
          role: '项目负责人'
        },
        ...this.memberCandidates
          .filter(c => this.selectedMembers.includes(c.username))
          .map(c => ({ id: c.id, username: c.username, name: c.name, role: '项目成员' }))
      ]

      const features = this.form.featuresText.split('\n').map(s => s.trim()).filter(Boolean)
      const techStack = this.form.techStackText.split(/[,，;；]/).map(s => s.trim()).filter(Boolean)

      // 初始动态
      const activities = [
        {
          type: 'update',
          author: u.name || u.username || '未知用户',
          role: '项目负责人',
          content: `创建项目「${this.form.name}」，项目正式立项`,
          time
        }
      ]
      const otherMembers = members.filter(m => m.role !== '项目负责人')
      if (otherMembers.length) {
        activities.push({
          type: 'member',
          author: u.name || u.username || '未知用户',
          role: '项目负责人',
          content: `${otherMembers.map(m => m.name).join('、')} 加入项目团队`,
          time
        })
      }
      this.form.files.forEach(f => {
        activities.push({
          type: 'file',
          author: u.name || u.username || '未知用户',
          role: '项目负责人',
          content: `上传了文件：${f.name}`,
          time,
          file: f
        })
      })
      activities.sort((a, b) => b.time.localeCompare(a.time))

      const project = createProject({
        name: this.form.name,
        subtitle: this.form.subtitle || this.form.overview || '暂无简介',
        category: this.form.category,
        status: this.form.status,
        description: {
          overview: this.form.overview || '暂无项目概述',
          features: features.length ? features : ['暂无核心功能信息'],
          architecture: this.form.architecture || '暂无技术架构说明',
          scenarios: this.form.scenarios || '暂无应用场景说明'
        },
        publisher: this.form.publisher || 'EAVI 团队数据管理委员会',
        startDate: this.form.startDate,
        endDate: this.form.endDate,
        leader: u.name || u.username || '未知用户',
        leaderId: u.id || 0,
        leaderUsername: u.username || '',
        team: members.map(m => m.name),
        members,
        tags: techStack.length ? techStack : ['新项目'],
        techStack,
        milestones: this.form.milestones.filter(m => m.title.trim()),
        files: this.form.files,
        activities
      })

      this.showToast('项目创建成功', 'success')
      // 跳转到新项目详情页
      setTimeout(() => {
        this.$router.push({ name: 'ProjectDetail', params: { id: project.id } })
      }, 600)
    },
    showToast(message, type = 'info') {
      // 简单提示（无 toast 组件时用浏览器 alert 风格气泡，此处轻量处理）
      console.log(`[${type}] ${message}`)
      const t = document.createElement('div')
      t.className = 'nc-toast nc-toast-' + type
      t.textContent = message
      document.body.appendChild(t)
      setTimeout(() => {
        t.classList.add('nc-toast-hide')
        setTimeout(() => t.remove(), 300)
      }, 2200)
    }
  }
}
</script>

<style scoped>
.project-create-page {
  min-height: 100vh;
  padding: 1.4rem 2rem 3rem;
  max-width: 980px;
  margin: 0 auto;
  background: linear-gradient(135deg, #f5f7ff 0%, #f0f4ff 50%, #f7f8ff 100%);
}

/* 顶部导航 */
.nc-topbar {
  display: flex;
  align-items: center;
  gap: 1.2rem;
  margin-bottom: 1.4rem;
}

.nc-back {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 10px;
  background: #fff;
  color: #334155;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.12);
  transition: all 0.2s ease;
}

.nc-back:hover {
  transform: translateX(-2px);
  color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.nc-back-icon {
  font-size: 1rem;
}

.nc-breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: #94a3b8;
}

.nc-breadcrumb-link {
  cursor: pointer;
  transition: color 0.2s ease;
}

.nc-breadcrumb-link:hover {
  color: #667eea;
}

.nc-breadcrumb-sep {
  color: #cbd5e1;
}

.nc-breadcrumb-current {
  color: #334155;
  font-weight: 600;
}

/* 页头 */
.nc-head {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.4rem 1.6rem;
  margin-bottom: 1.2rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 18px;
  color: #fff;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.nc-head-icon {
  font-size: 2.2rem;
}

.nc-head-title {
  font-size: 1.4rem;
  font-weight: 700;
  margin: 0 0 0.25rem;
}

.nc-head-sub {
  font-size: 0.85rem;
  opacity: 0.85;
  margin: 0;
}

/* 卡片 */
.nc-card {
  background: #fff;
  border-radius: 16px;
  padding: 1.4rem 1.6rem;
  margin-bottom: 1.2rem;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.08);
}

.nc-card-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.05rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 1.2rem;
}

.nc-card-icon {
  font-size: 1.15rem;
}

.nc-count {
  margin-left: auto;
  font-size: 0.78rem;
  font-weight: 600;
  color: #667eea;
  background: #eef2ff;
  padding: 0.2rem 0.7rem;
  border-radius: 20px;
}

/* 表单 */
.nc-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem 1.2rem;
}

.nc-field-full {
  grid-column: 1 / -1;
}

.nc-label {
  display: block;
  font-size: 0.82rem;
  font-weight: 600;
  color: #475569;
  margin-bottom: 0.45rem;
}

.nc-required {
  color: #ef4444;
  font-style: normal;
}

.nc-input {
  width: 100%;
  padding: 0.62rem 0.85rem;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.86rem;
  color: #334155;
  outline: none;
  background: #fafbff;
  box-sizing: border-box;
  transition: all 0.2s ease;
}

.nc-input:focus {
  border-color: #667eea;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12);
}

.nc-select {
  cursor: pointer;
  appearance: auto;
}

.nc-textarea {
  resize: vertical;
  min-height: 80px;
  line-height: 1.6;
}

/* 负责人 */
.nc-leader {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.5rem 0.85rem;
  background: #f8f9ff;
  border: 1.5px dashed #c7d2fe;
  border-radius: 10px;
}

.nc-leader-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 700;
}

.nc-leader-name {
  font-size: 0.88rem;
  font-weight: 600;
  color: #334155;
}

.nc-leader-tag {
  margin-left: auto;
  font-size: 0.72rem;
  color: #667eea;
  background: #eef2ff;
  padding: 0.15rem 0.6rem;
  border-radius: 20px;
  font-weight: 600;
}

/* 团队成员 */
.nc-member-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 0.8rem;
}

.nc-member {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.75rem 0.9rem;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nc-member:hover {
  border-color: #c7d2fe;
  background: #fafbff;
}

.nc-member.is-selected {
  border-color: #667eea;
  background: #eef2ff;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.nc-member-leader {
  cursor: default;
  border-color: #a78bfa;
  background: #f5f3ff;
}

.nc-member-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.95rem;
  font-weight: 700;
  flex-shrink: 0;
}

.nc-member-info {
  min-width: 0;
  flex: 1;
}

.nc-member-name {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.9rem;
  font-weight: 600;
  color: #334155;
}

.nc-member-badge {
  font-style: normal;
  font-size: 0.68rem;
  color: #7c3aed;
  background: #ede9fe;
  padding: 0.1rem 0.45rem;
  border-radius: 20px;
  font-weight: 700;
}

.nc-member-role {
  display: block;
  font-size: 0.75rem;
  color: #94a3b8;
  margin-top: 0.15rem;
}

.nc-member-check {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #667eea;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  flex-shrink: 0;
}

/* 里程碑 */
.nc-milestone-row {
  display: grid;
  grid-template-columns: 1.2fr 1.4fr 0.6fr auto;
  gap: 0.6rem;
  margin-bottom: 0.6rem;
}

.nc-milestone-title {
  font-weight: 600;
}

.nc-icon-btn {
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 9px;
  background: #f1f5f9;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nc-icon-btn:hover {
  background: #fee2e2;
  transform: scale(1.06);
}

.nc-add-btn {
  width: 100%;
  padding: 0.6rem;
  border: 1.5px dashed #c7d2fe;
  border-radius: 10px;
  background: #fafbff;
  color: #667eea;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nc-add-btn:hover {
  background: #eef2ff;
  border-color: #667eea;
}

/* 文件上传 */
.nc-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
  padding: 1.6rem;
  border: 1.5px dashed #c7d2fe;
  border-radius: 12px;
  background: #fafbff;
  cursor: pointer;
  color: #64748b;
  font-size: 0.88rem;
  transition: all 0.2s ease;
}

.nc-upload-area:hover {
  background: #eef2ff;
  border-color: #667eea;
}

.nc-upload-icon {
  font-size: 1.6rem;
}

.nc-upload-hint {
  font-size: 0.75rem;
  color: #94a3b8;
}

.nc-file-input-hidden {
  display: none;
}

.nc-file-list {
  margin-top: 0.8rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.nc-file-item {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.6rem 0.8rem;
  background: #f8f9ff;
  border: 1px solid #eef0f6;
  border-radius: 10px;
}

.nc-file-icon {
  font-size: 1rem;
}

.nc-file-name {
  flex: 1;
  font-size: 0.84rem;
  font-weight: 600;
  color: #334155;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nc-file-meta {
  font-size: 0.75rem;
  color: #94a3b8;
  flex-shrink: 0;
}

.nc-empty {
  margin-top: 0.8rem;
  text-align: center;
  font-size: 0.8rem;
  color: #94a3b8;
  padding: 0.8rem;
  background: #fafbff;
  border-radius: 10px;
}

/* 操作栏 */
.nc-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-top: 1.6rem;
}

.nc-btn-primary {
  padding: 0.75rem 2.2rem;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(102, 126, 234, 0.3);
  transition: all 0.2s ease;
}

.nc-btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(102, 126, 234, 0.4);
}

.nc-btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.nc-btn-ghost {
  padding: 0.75rem 2rem;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  background: #fff;
  color: #64748b;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nc-btn-ghost:hover {
  border-color: #cbd5e1;
  color: #334155;
}

/* Toast */
.nc-toast {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  padding: 0.7rem 1.4rem;
  border-radius: 12px;
  color: #fff;
  font-size: 0.88rem;
  font-weight: 600;
  z-index: 9999;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.18);
  animation: nc-toast-in 0.3s ease;
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.nc-toast-success {
  background: #10b981;
}

.nc-toast-warning {
  background: #f59e0b;
}

.nc-toast-info {
  background: #3b82f6;
}

.nc-toast-hide {
  opacity: 0;
  transform: translateX(-50%) translateY(-10px);
}

@keyframes nc-toast-in {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-16px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

@media (max-width: 768px) {
  .project-create-page {
    padding: 1rem;
  }
  .nc-form-grid {
    grid-template-columns: 1fr;
  }
  .nc-milestone-row {
    grid-template-columns: 1fr 1fr;
  }
  .nc-milestone-status {
    grid-column: 1 / -1;
  }
}
</style>
