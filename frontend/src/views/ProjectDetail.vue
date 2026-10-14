<template>
  <div class="project-detail-page">
    <!-- 加载动画 -->
    <div v-if="loading" class="pd-loading">
      <div class="pd-loading-spinner"></div>
      <span>正在加载项目详情...</span>
    </div>

    <template v-else-if="project">
      <!-- 顶部导航 -->
      <div class="pd-topbar">
        <button class="pd-back" @click="goBack">
          <span class="pd-back-icon">←</span>
          返回项目管理
        </button>
        <div class="pd-breadcrumb">
          <span class="pd-breadcrumb-link" @click="goBack">项目管理</span>
          <span class="pd-breadcrumb-sep">/</span>
          <span class="pd-breadcrumb-current">{{ project.title }}</span>
        </div>
      </div>

      <!-- 头部卡片 -->
      <div class="pd-header card">
        <div class="pd-header-top">
          <span class="pd-status" :class="'status-' + project.status">{{ project.statusText }}</span>
          <span class="pd-role-badge" :class="roleClass">{{ roleText }}</span>
        </div>
        <h1 class="pd-title">{{ project.title }}</h1>
        <p class="pd-subtitle">{{ project.subtitle }}</p>

        <div class="pd-meta">
          <div class="pd-meta-item">
            <span class="pd-meta-label">项目类别</span>
            <span class="pd-meta-value">{{ project.categoryName }}</span>
          </div>
          <div class="pd-meta-item">
            <span class="pd-meta-label">发布者</span>
            <span class="pd-meta-value">{{ project.publisher }}</span>
          </div>
          <div class="pd-meta-item">
            <span class="pd-meta-label">发布时间</span>
            <span class="pd-meta-value">{{ project.publishedAt }}</span>
          </div>
          <div class="pd-meta-item">
            <span class="pd-meta-label">项目负责人</span>
            <span class="pd-meta-value">{{ project.leader }}</span>
          </div>
          <div class="pd-meta-item">
            <span class="pd-meta-label">项目周期</span>
            <span class="pd-meta-value">{{ project.startDate }} ~ {{ project.endDate }}</span>
          </div>
          <div class="pd-meta-item">
            <span class="pd-meta-label">团队成员</span>
            <span class="pd-meta-value">{{ project.members.length }} 人</span>
          </div>
        </div>

        <!-- 项目进度 -->
        <div class="pd-progress-block">
          <div class="pd-progress-head">
            <span class="pd-progress-title">项目进度</span>
            <span class="pd-progress-num">
              <template v-if="isLeader">
                <input
                  type="number"
                  class="pd-progress-input"
                  v-model.number="draftProgress"
                  min="0"
                  max="100"
                />
                <span>%</span>
              </template>
              <template v-else>{{ project.progress }}%</template>
            </span>
          </div>
          <div class="pd-progress-bar">
            <div class="pd-progress-fill" :style="{ width: displayProgress + '%' }"></div>
          </div>

          <!-- 负责人可编辑进度 -->
          <div v-if="isLeader" class="pd-progress-edit">
            <input
              type="range"
              class="pd-range"
              min="0"
              max="100"
              step="1"
              v-model.number="draftProgress"
            />
            <button class="btn-primary" @click="saveProgress">保存进度</button>
            <button class="btn-secondary" @click="cancelProgress">取消</button>
            <span class="pd-hint">✏️ 仅项目负责人可手动修改项目进度</span>
          </div>
          <div v-else class="pd-progress-lock">
            <span>🔒 项目进度由负责人维护，成员仅可查看</span>
          </div>
        </div>

        <!-- 技术标签 -->
        <div class="pd-tags">
          <span v-for="t in project.tags" :key="t" class="pd-tag">{{ t }}</span>
        </div>
      </div>

      <!-- 主体两栏 -->
      <div class="pd-body">
        <!-- 左侧主内容 -->
        <div class="pd-main">
          <!-- 项目介绍 -->
          <section class="card pd-card">
            <h3 class="pd-card-title">
              <span class="pd-card-icon">📖</span>项目介绍
            </h3>
            <div class="pd-desc-section">
              <h4>项目概述</h4>
              <p class="pd-desc-text">{{ project.description.overview }}</p>
            </div>
            <div class="pd-desc-section">
              <h4>核心功能</h4>
              <ul class="pd-feature-list">
                <li v-for="(f, i) in project.description.features" :key="i">{{ f }}</li>
              </ul>
            </div>
            <div class="pd-desc-section">
              <h4>技术架构</h4>
              <p class="pd-desc-text">{{ project.description.architecture }}</p>
            </div>
            <div class="pd-desc-section">
              <h4>应用场景</h4>
              <p class="pd-desc-text">{{ project.description.scenarios }}</p>
            </div>
            <div class="pd-tech-list">
              <span v-for="t in project.techStack" :key="t" class="pd-tech-tag">{{ t }}</span>
            </div>
          </section>

          <!-- 项目里程碑 -->
          <section class="card pd-card">
            <div class="pd-card-head">
              <h3 class="pd-card-title">
                <span class="pd-card-icon">🎯</span>项目里程碑
                <span class="pd-count">{{ project.milestones.length }}</span>
              </h3>
              <button v-if="isLeader" class="btn-upload" @click="openMilestoneForm()">＋ 新增里程碑</button>
            </div>

            <!-- 新增 / 编辑里程碑表单（仅负责人） -->
            <div v-if="isLeader && milestoneForm.visible" class="pd-milestone-form">
              <input
                v-model="milestoneForm.title"
                class="pd-milestone-input"
                placeholder="里程碑名称，如：需求分析与调研"
              />
              <input
                v-model="milestoneForm.date"
                class="pd-milestone-input"
                placeholder="日期范围，如：2024-01-01 ~ 2024-02-28"
              />
              <select v-model="milestoneForm.status" class="pd-milestone-select">
                <option value="pending">待开始</option>
                <option value="current">进行中</option>
                <option value="completed">已完成</option>
              </select>
              <div class="pd-milestone-form-actions">
                <button class="btn-primary" @click="saveMilestone">保存</button>
                <button class="btn-secondary" @click="cancelMilestoneForm">取消</button>
              </div>
            </div>

            <div class="pd-milestones">
              <div
                v-for="(m, i) in project.milestones"
                :key="i"
                class="pd-milestone"
                :class="'ms-' + m.status"
              >
                <div class="pd-milestone-dot"></div>
                <div class="pd-milestone-info">
                  <div class="pd-milestone-title">{{ m.title }}</div>
                  <div class="pd-milestone-date">{{ m.date }}</div>
                </div>
                <span
                  class="pd-milestone-status"
                  :class="{ 'is-clickable': isLeader }"
                  :title="isLeader ? '点击切换状态（待开始 → 进行中 → 已完成）' : ''"
                  @click="cycleMilestoneStatus(m)"
                >{{ milestoneText(m.status) }}</span>
                <div v-if="isLeader" class="pd-milestone-ops">
                  <button class="pd-milestone-op" title="编辑里程碑" @click="openMilestoneForm(m, i)">✏️</button>
                  <button class="pd-milestone-op" title="删除里程碑" @click="removeMilestone(i)">🗑</button>
                </div>
              </div>
              <div v-if="!project.milestones.length" class="pd-empty">暂无里程碑，由项目负责人维护</div>
            </div>
          </section>

          <!-- 团队成员 -->
          <section class="card pd-card">
            <h3 class="pd-card-title">
              <span class="pd-card-icon">👥</span>团队成员
              <span class="pd-count">{{ project.members.length }}</span>
            </h3>
            <div class="pd-members">
              <div
                v-for="m in project.members"
                :key="m.id || m.name"
                class="pd-member"
                :class="{ 'is-leader': m.username === project.leaderUsername }"
              >
                <div class="pd-member-avatar" :class="{ 'leader-avatar': m.username === project.leaderUsername }">
                  {{ (m.name || '?').charAt(0) }}
                </div>
                <div class="pd-member-info">
                  <div class="pd-member-name">
                    {{ m.name }}
                    <span v-if="m.username === project.leaderUsername" class="pd-member-badge leader">负责人</span>
                    <span v-else-if="isCurrentUser(m)" class="pd-member-badge me">我</span>
                  </div>
                  <div class="pd-member-role">{{ m.role }}</div>
                </div>
              </div>
            </div>
          </section>

          <!-- 项目动态（进程时间线） -->
          <section class="card pd-card">
            <h3 class="pd-card-title">
              <span class="pd-card-icon">📝</span>项目动态
              <span class="pd-count">{{ project.activities.length }}</span>
            </h3>

            <!-- 发布动态 -->
            <div v-if="isParticipant" class="pd-post-box">
              <textarea
                v-model="newActivityText"
                class="pd-post-textarea"
                placeholder="更新项目进程，分享最新进展..."
                rows="2"
              ></textarea>
              <div class="pd-post-actions">
                <span class="pd-post-hint">发布后将按时间倒序展示在动态列表最上方</span>
                <button class="btn-primary" :disabled="!newActivityText.trim()" @click="postActivity">
                  发布动态
                </button>
              </div>
            </div>
            <div v-else class="pd-post-disabled">
              <span>🔒 仅项目负责人与成员可更新项目进程</span>
            </div>

            <!-- 时间线（越靠上越新） -->
            <div class="pd-timeline">
              <div v-for="(act, i) in sortedActivities" :key="i" class="pd-timeline-item">
                <div class="pd-timeline-avatar" :style="{ background: activityColor(act.type) }">
                  {{ (act.author || '?').charAt(0) }}
                </div>
                <div class="pd-timeline-content">
                  <div class="pd-timeline-head">
                    <span class="pd-timeline-author">{{ act.author }}</span>
                    <span class="pd-timeline-role">{{ act.role }}</span>
                    <span class="pd-timeline-type" :style="{ color: activityColor(act.type) }">
                      {{ activityTypeText(act.type) }}
                    </span>
                    <span class="pd-timeline-time">🕐 {{ act.time }}</span>
                  </div>
                  <div class="pd-timeline-text">{{ act.content }}</div>
                  <!-- 文件类动态：可直接下载关联文件 -->
                  <div v-if="act.type === 'file' && act.file" class="pd-timeline-file">
                    <span class="pd-timeline-file-icon">{{ fileIcon(act.file.type) }}</span>
                    <span class="pd-timeline-file-name" :title="act.file.name">{{ act.file.name }}</span>
                    <span class="pd-timeline-file-meta">{{ act.file.size }}</span>
                    <button class="pd-file-download" @click="downloadFile(act.file)">下载</button>
                  </div>
                </div>
              </div>
              <div v-if="!sortedActivities.length" class="pd-empty">暂无动态</div>
            </div>
          </section>
        </div>

        <!-- 右侧侧栏 -->
        <div class="pd-side">
          <!-- 项目文件 -->
          <section class="card pd-card">
            <div class="pd-side-head">
              <h3 class="pd-card-title">
                <span class="pd-card-icon">📁</span>项目文件
                <span class="pd-count">{{ project.files.length }}</span>
              </h3>
              <button v-if="isParticipant" class="btn-upload" @click="triggerFileInput">＋ 上传文件</button>
              <input
                type="file"
                ref="fileInput"
                class="pd-file-input"
                @change="handleFileSelected"
              />
            </div>
            <div class="pd-file-list">
              <div v-for="(f, i) in project.files" :key="i" class="pd-file-item">
                <div class="pd-file-icon">{{ fileIcon(f.type) }}</div>
                <div class="pd-file-info">
                  <div class="pd-file-name" :title="f.name">{{ f.name }}</div>
                  <div class="pd-file-meta">{{ f.size }} · {{ f.uploader }} · {{ f.uploadedAt }}</div>
                </div>
                <button class="pd-file-download" title="下载文件" @click="downloadFile(f)">下载</button>
              </div>
              <div v-if="!project.files.length" class="pd-empty">暂无文件</div>
            </div>
            <div v-if="!isParticipant" class="pd-post-disabled">
              <span>🔒 仅项目负责人与成员可上传文件</span>
            </div>
          </section>

          <!-- 项目信息 -->
          <section class="card pd-card">
            <h3 class="pd-card-title">
              <span class="pd-card-icon">ℹ️</span>项目信息
            </h3>
            <div class="pd-info-rows">
              <div class="pd-info-row"><span>项目编号</span><b>{{ project.id }}</b></div>
              <div class="pd-info-row"><span>项目类别</span><b>{{ project.categoryName }}</b></div>
              <div class="pd-info-row"><span>发布者</span><b>{{ project.publisher }}</b></div>
              <div class="pd-info-row"><span>项目负责人</span><b>{{ project.leader }}</b></div>
              <div class="pd-info-row"><span>项目状态</span><b class="pd-info-status">{{ project.statusText }}</b></div>
              <div class="pd-info-row"><span>开始日期</span><b>{{ project.startDate }}</b></div>
              <div class="pd-info-row"><span>结束日期</span><b>{{ project.endDate }}</b></div>
            </div>
          </section>

          <!-- 项目统计 -->
          <section class="card pd-card">
            <h3 class="pd-card-title">
              <span class="pd-card-icon">📊</span>项目统计
            </h3>
            <div class="pd-stats-grid">
              <div class="pd-stat">
                <b>{{ project.progress }}%</b>
                <span>项目进度</span>
              </div>
              <div class="pd-stat">
                <b>{{ project.files.length }}</b>
                <span>项目文件</span>
              </div>
              <div class="pd-stat">
                <b>{{ project.activities.length }}</b>
                <span>项目动态</span>
              </div>
              <div class="pd-stat">
                <b>{{ project.members.length }}</b>
                <span>团队成员</span>
              </div>
            </div>
            <div class="pd-milestone-progress">
              <div class="pd-milestone-progress-head">
                <span>里程碑进度</span>
                <span>{{ milestoneDone }}/{{ project.milestones.length }}</span>
              </div>
              <div class="pd-progress-bar">
                <div class="pd-progress-fill" :style="{ width: milestonePercent + '%' }"></div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </template>

    <div v-else class="pd-notfound">
      <div class="pd-notfound-icon">😕</div>
      <p>未找到该项目</p>
      <button class="btn-primary" @click="goBack">返回项目管理</button>
    </div>

    <!-- Toast 提示 -->
    <transition name="toast-fade">
      <div v-if="toast" class="pd-toast" :class="'toast-' + toast.type">
        {{ toast.message }}
      </div>
    </transition>
  </div>
</template>

<script>
import { getProjectById } from '../data/projects.js'

export default {
  name: 'ProjectDetail',
  data() {
    return {
      loading: true,
      project: null,
      draftProgress: 0,
      newActivityText: '',
      toast: null,
      toastTimer: null,
      // 里程碑新增/编辑表单（仅负责人）
      milestoneForm: {
        visible: false,
        editIndex: -1,
        title: '',
        date: '',
        status: 'pending'
      }
    }
  },
  computed: {
    currentUser() {
      try {
        return JSON.parse(localStorage.getItem('user'))
      } catch (e) {
        return null
      }
    },
    // 当前用户是否为项目负责人（仅负责人可修改项目进度）
    isLeader() {
      const u = this.currentUser
      if (!u || !this.project) return false
      return (
        this.project.leaderUsername === u.username ||
        this.project.leader === u.name ||
        this.project.leaderId === u.id
      )
    },
    // 当前用户是否为项目成员
    isMember() {
      const u = this.currentUser
      if (!u || !this.project) return false
      return this.project.members.some(
        m => m.username === u.username || m.name === u.name || m.id === u.id
      )
    },
    // 负责人或成员可更新进程、上传文件
    isParticipant() {
      return this.isLeader || this.isMember
    },
    roleClass() {
      if (this.isLeader) return 'role-leader'
      if (this.isMember) return 'role-member'
      return 'role-guest'
    },
    roleText() {
      if (this.isLeader) return '当前角色：项目负责人'
      if (this.isMember) return '当前角色：项目成员'
      return '当前角色：访客（只读）'
    },
    displayProgress() {
      if (this.isLeader) return this.draftProgress
      return this.project.progress
    },
    // 动态时间线：按时间倒序，越靠上越新
    sortedActivities() {
      return [...this.project.activities].sort((a, b) => b.time.localeCompare(a.time))
    },
    milestoneDone() {
      return this.project.milestones.filter(m => m.status === 'completed').length
    },
    milestonePercent() {
      return this.project.milestones.length
        ? Math.round((this.milestoneDone / this.project.milestones.length) * 100)
        : 0
    }
  },
  watch: {
    '$route.params.id'() {
      this.loadProjectData()
    }
  },
  mounted() {
    this.loadProjectData()
  },
  beforeUnmount() {
    clearTimeout(this.toastTimer)
  },
  methods: {
    loadProjectData() {
      const id = this.$route.params.id || this.$route.query.id
      if (!id) {
        this.project = null
        this.loading = false
        return
      }
      this.loading = true
      // 模拟异步加载
      setTimeout(() => {
        const base = getProjectById(id)
        if (!base) {
          this.project = null
          this.loading = false
          return
        }
        let data = JSON.parse(JSON.stringify(base))
        // 读取本地缓存（用户上传的文件、发布的动态、修改的进度持久化保存）
        const cacheKey = 'project_data_' + data.id
        try {
          const cached = localStorage.getItem(cacheKey)
          if (cached) data = JSON.parse(cached)
        } catch (e) {
          // 忽略缓存解析错误
        }
        // 为"文件上传"类动态自动关联对应文件，支持在动态中直接下载
        this.attachActivityFiles(data)
        this.project = data
        this.draftProgress = data.progress
        this.loading = false
      }, 300)
    },

    // 文件类动态自动关联对应文件对象，便于在动态中直接下载
    attachActivityFiles(data) {
      if (!Array.isArray(data.activities) || !Array.isArray(data.files)) return
      data.activities.forEach(act => {
        if (act.type === 'file' && !act.file) {
          const m = act.content && act.content.match(/上传了文件[：:]\s*(.+)/)
          const fname = m && m[1] ? m[1].trim() : ''
          act.file = fname ? (data.files.find(f => f.name === fname) || null) : null
        }
      })
    },

    saveProject() {
      try {
        localStorage.setItem('project_data_' + this.project.id, JSON.stringify(this.project))
      } catch (e) {
        // 存储失败时忽略
      }
      // 同步自定义项目列表，保证列表页展示的进度/状态等字段一致
      try {
        const list = JSON.parse(localStorage.getItem('custom_projects') || '[]')
        const idx = list.findIndex(p => p.id === this.project.id)
        if (idx >= 0) {
          list[idx] = this.project
          localStorage.setItem('custom_projects', JSON.stringify(list))
        }
      } catch (e) {
        // 存储失败时忽略
      }
    },

    // ===== 进度修改（仅负责人）=====
    saveProgress() {
      if (!this.isLeader) {
        this.showToast('仅项目负责人可修改项目进度', 'warning')
        return
      }
      const old = this.project.progress
      const v = Math.max(0, Math.min(100, Math.round(Number(this.draftProgress) || 0)))
      if (v === old) {
        this.showToast('进度未发生变化', 'info')
        return
      }
      this.project.progress = v
      this.draftProgress = v
      this.addActivity('progress', `将项目进度由 ${old}% 更新至 ${v}%`)
      this.showToast('项目进度已更新', 'success')
    },

    cancelProgress() {
      this.draftProgress = this.project.progress
      this.showToast('已取消修改', 'info')
    },

    // ===== 发布动态（负责人与成员）=====
    postActivity() {
      const text = this.newActivityText.trim()
      if (!text) return
      this.addActivity('update', text)
      this.newActivityText = ''
      this.showToast('动态已发布', 'success')
    },

    addActivity(type, content, file = null) {
      const now = new Date()
      const time = this.formatTime(now)
      const u = this.currentUser || { name: '未知用户' }
      const role = this.isLeader ? '项目负责人' : this.isMember ? '项目成员' : '访客'
      const act = { type, author: u.name || u.username, role, content, time }
      if (file) act.file = file
      this.project.activities.unshift(act)
      this.project.activities.sort((a, b) => b.time.localeCompare(a.time))
      this.saveProject()
    },

    // ===== 文件上传（负责人与成员）=====
    triggerFileInput() {
      this.$refs.fileInput.click()
    },

    handleFileSelected(e) {
      const file = e.target.files && e.target.files[0]
      if (!file) return
      const u = this.currentUser || { name: '未知用户' }
      const time = this.formatTime(new Date())
      const newFile = {
        name: file.name,
        size: this.formatSize(file.size),
        uploader: u.name || u.username,
        uploadedAt: time,
        type: this.fileTypeFromName(file.name)
      }
      // 新文件放在最上方
      this.project.files.unshift(newFile)
      this.addActivity('file', `上传了文件：${file.name}`, newFile)
      this.showToast('文件上传成功', 'success')
      e.target.value = ''
    },

    // ===== 文件下载 =====
    downloadFile(f) {
      const content =
        `文件名称：${f.name}\n` +
        `文件大小：${f.size}\n` +
        `上传者：${f.uploader}\n` +
        `上传时间：${f.uploadedAt}\n\n` +
        `这是由 EAVI 项目管理系统生成的模拟下载文件，\n用于演示项目文件的下载功能。\n`
      const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = f.name
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
      this.showToast(`正在下载：${f.name}`, 'success')
    },

    // ===== 工具方法 =====
    isCurrentUser(m) {
      const u = this.currentUser
      if (!u) return false
      return m.username === u.username || m.name === u.name || m.id === u.id
    },

    formatTime(d) {
      const pad = n => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    },

    formatSize(bytes) {
      if (bytes < 1024) return bytes + ' B'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
      return (bytes / 1024 / 1024).toFixed(1) + ' MB'
    },

    fileTypeFromName(name) {
      const ext = (name.split('.').pop() || '').toLowerCase()
      const map = {
        pdf: 'pdf', doc: 'doc', docx: 'doc', ppt: 'ppt', pptx: 'ppt',
        xls: 'xls', xlsx: 'xls', md: 'md', txt: 'txt', zip: 'zip',
        rar: 'zip', png: 'img', jpg: 'img', jpeg: 'img', gif: 'img',
        webp: 'img', svg: 'img', js: 'code', ts: 'code', java: 'code',
        py: 'code', vue: 'code', css: 'code', html: 'code'
      }
      return map[ext] || 'file'
    },

    fileIcon(type) {
      const map = {
        pdf: '📕', doc: '📘', ppt: '📙', xls: '📗', md: '📄',
        txt: '📄', zip: '📦', img: '🖼️', code: '📜', file: '📎'
      }
      return map[type] || '📎'
    },

    activityColor(type) {
      const map = {
        progress: '#7c5cff',
        file: '#ff9d4d',
        update: '#2fa8ff',
        member: '#22c38e',
        milestone: '#f15bb5'
      }
      return map[type] || '#8a94a6'
    },

    activityTypeText(type) {
      const map = {
        progress: '进度变更',
        file: '文件上传',
        update: '进程更新',
        member: '成员加入',
        milestone: '里程碑'
      }
      return map[type] || '项目动态'
    },

    milestoneText(status) {
      const map = { completed: '已完成', current: '进行中', pending: '待开始' }
      return map[status] || status
    },

    // ===== 里程碑管理（仅负责人）=====
    openMilestoneForm(m = null, index = -1) {
      this.milestoneForm.visible = true
      this.milestoneForm.editIndex = index
      this.milestoneForm.title = m ? m.title : ''
      this.milestoneForm.date = m ? m.date : ''
      this.milestoneForm.status = m ? m.status : 'pending'
    },

    cancelMilestoneForm() {
      this.milestoneForm.visible = false
      this.milestoneForm.editIndex = -1
      this.milestoneForm.title = ''
      this.milestoneForm.date = ''
      this.milestoneForm.status = 'pending'
    },

    saveMilestone() {
      if (!this.isLeader) return
      const title = this.milestoneForm.title.trim()
      if (!title) {
        this.showToast('请填写里程碑名称', 'warning')
        return
      }
      const date = this.milestoneForm.date.trim() || '日期待定'
      const status = this.milestoneForm.status
      const isEdit = this.milestoneForm.editIndex >= 0

      if (isEdit) {
        const m = this.project.milestones[this.milestoneForm.editIndex]
        if (!m) return
        const statusChanged = m.status !== status
        m.title = title
        m.date = date
        m.status = status
        if (statusChanged) {
          this.addActivity('milestone', `里程碑「${title}」状态更新为「${this.milestoneText(status)}」`)
        }
        this.showToast('里程碑已更新', 'success')
      } else {
        this.project.milestones.push({ title, date, status })
        this.addActivity('milestone', `新增里程碑「${title}」（${this.milestoneText(status)}）`)
        this.showToast('里程碑已新增', 'success')
      }
      this.cancelMilestoneForm()
    },

    // 点击状态徽章循环切换：待开始 → 进行中 → 已完成
    cycleMilestoneStatus(m) {
      if (!this.isLeader || !m) return
      const order = { pending: 'current', current: 'completed', completed: 'pending' }
      const oldText = this.milestoneText(m.status)
      m.status = order[m.status] || 'pending'
      this.addActivity('milestone', `里程碑「${m.title}」状态由「${oldText}」更新为「${this.milestoneText(m.status)}」`)
      this.showToast('里程碑状态已更新', 'success')
    },

    removeMilestone(index) {
      if (!this.isLeader) return
      const m = this.project.milestones[index]
      if (!m) return
      if (!window.confirm(`确认删除里程碑「${m.title}」？此操作不可撤销。`)) return
      this.project.milestones.splice(index, 1)
      this.addActivity('milestone', `删除里程碑「${m.title}」`)
      this.showToast('里程碑已删除', 'success')
    },

    showToast(message, type = 'info') {
      this.toast = { message, type }
      clearTimeout(this.toastTimer)
      this.toastTimer = setTimeout(() => (this.toast = null), 2500)
    },

    goBack() {
      if (window.history.length > 1) {
        this.$router.back()
      } else {
        this.$router.push('/projects')
      }
    }
  }
}
</script>

<style scoped>
/* ===== 页面容器 ===== */
.project-detail-page {
  min-height: 100vh;
  padding: 2rem;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
}

.pd-topbar {
  max-width: 1200px;
  margin: 0 auto 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.pd-back {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  color: #475569;
  padding: 0.55rem 1.1rem;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.08);
}

.pd-back:hover {
  border-color: #667eea;
  color: #667eea;
  transform: translateX(-2px);
}

.pd-back-icon {
  font-size: 1rem;
}

.pd-breadcrumb {
  font-size: 0.85rem;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.pd-breadcrumb-link {
  cursor: pointer;
  color: #667eea;
  transition: color 0.2s;
}

.pd-breadcrumb-link:hover {
  color: #764ba2;
}

.pd-breadcrumb-sep {
  color: #cbd5e1;
}

.pd-breadcrumb-current {
  color: #475569;
  font-weight: 500;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== 通用卡片 ===== */
.card {
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.08);
  border: 1px solid #eef0f6;
}

.pd-header {
  max-width: 1200px;
  margin: 0 auto 1.5rem;
  padding: 1.8rem 2rem;
}

.pd-header-top {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.8rem;
}

.pd-status {
  padding: 0.3rem 0.9rem;
  border-radius: 20px;
  font-size: 0.78rem;
  font-weight: 600;
}

.pd-status.status-active {
  background: #dcfce7;
  color: #166534;
}

.pd-status.status-planning {
  background: #fef3c7;
  color: #92400e;
}

.pd-status.status-completed {
  background: #dbeafe;
  color: #1e40af;
}

.pd-status.status-paused {
  background: #fecaca;
  color: #991b1b;
}

.pd-role-badge {
  padding: 0.3rem 0.9rem;
  border-radius: 20px;
  font-size: 0.78rem;
  font-weight: 600;
}

.pd-role-badge.role-leader {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.35);
}

.pd-role-badge.role-member {
  background: #ecfdf5;
  color: #047857;
  border: 1px solid #a7f3d0;
}

.pd-role-badge.role-guest {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.pd-title {
  font-size: 1.7rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.pd-subtitle {
  color: #64748b;
  font-size: 0.95rem;
  line-height: 1.6;
  margin-bottom: 1.3rem;
}

.pd-meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 0.9rem;
  padding: 1.1rem 1.2rem;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #eef0f6;
  margin-bottom: 1.3rem;
}

.pd-meta-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.pd-meta-label {
  font-size: 0.75rem;
  color: #94a3b8;
}

.pd-meta-value {
  font-size: 0.88rem;
  color: #334155;
  font-weight: 500;
}

/* ===== 进度 ===== */
.pd-progress-block {
  margin-bottom: 1.2rem;
}

.pd-progress-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.pd-progress-title {
  font-size: 0.9rem;
  color: #475569;
  font-weight: 600;
}

.pd-progress-num {
  font-size: 1.1rem;
  font-weight: 700;
  color: #667eea;
  display: flex;
  align-items: center;
  gap: 0.2rem;
}

.pd-progress-input {
  width: 64px;
  padding: 0.25rem 0.4rem;
  border: 2px solid #667eea;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 700;
  color: #667eea;
  text-align: right;
  outline: none;
}

.pd-progress-input:focus {
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.pd-progress-bar {
  width: 100%;
  height: 8px;
  background: #eef0f6;
  border-radius: 4px;
  overflow: hidden;
}

.pd-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.pd-progress-edit {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  margin-top: 0.7rem;
  flex-wrap: wrap;
}

.pd-range {
  flex: 1;
  min-width: 200px;
  height: 6px;
  -webkit-appearance: none;
  appearance: none;
  background: #eef0f6;
  border-radius: 3px;
  outline: none;
}

.pd-range::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.pd-range::-moz-range-thumb {
  width: 18px;
  height: 18px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  cursor: pointer;
}

.pd-hint {
  font-size: 0.78rem;
  color: #94a3b8;
}

.pd-progress-lock {
  margin-top: 0.6rem;
  font-size: 0.8rem;
  color: #94a3b8;
}

/* ===== 标签 ===== */
.pd-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.pd-tag {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  padding: 0.3rem 0.8rem;
  border-radius: 12px;
  font-size: 0.78rem;
  font-weight: 500;
}

/* ===== 主体布局 ===== */
.pd-body {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 1.5rem;
  align-items: start;
}

.pd-main,
.pd-side {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.pd-card {
  padding: 1.5rem;
}

.pd-card-title {
  font-size: 1.05rem;
  color: #1e293b;
  margin-bottom: 1.1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.pd-card-icon {
  font-size: 1.1rem;
}

.pd-count {
  background: rgba(102, 126, 234, 0.12);
  color: #667eea;
  font-size: 0.75rem;
  padding: 0.15rem 0.55rem;
  border-radius: 12px;
  font-weight: 600;
}

/* ===== 项目介绍 ===== */
.pd-desc-section {
  margin-bottom: 1.1rem;
}

.pd-desc-section h4 {
  font-size: 0.9rem;
  color: #475569;
  margin-bottom: 0.4rem;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.pd-desc-section h4::before {
  content: '';
  width: 4px;
  height: 14px;
  background: linear-gradient(180deg, #667eea, #764ba2);
  border-radius: 2px;
}

.pd-desc-text {
  color: #64748b;
  font-size: 0.88rem;
  line-height: 1.8;
}

.pd-feature-list {
  list-style: none;
  padding: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.4rem 1rem;
}

.pd-feature-list li {
  color: #64748b;
  font-size: 0.85rem;
  line-height: 1.6;
  display: flex;
  align-items: flex-start;
  gap: 0.4rem;
}

.pd-feature-list li::before {
  content: '✓';
  color: #667eea;
  font-weight: 700;
  flex-shrink: 0;
}

.pd-tech-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.pd-tech-tag {
  background: #f1f5f9;
  color: #475569;
  padding: 0.3rem 0.8rem;
  border-radius: 8px;
  font-size: 0.78rem;
  font-weight: 500;
  border: 1px solid #e2e8f0;
}

/* ===== 里程碑 ===== */
.pd-milestones {
  display: flex;
  flex-direction: column;
}

.pd-milestone {
  display: flex;
  align-items: flex-start;
  gap: 0.9rem;
  padding: 0.7rem 0;
  position: relative;
}

.pd-milestone:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 8px;
  top: 34px;
  bottom: -4px;
  width: 2px;
  background: #e2e8f0;
}

.pd-milestone-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #e2e8f0;
  border: 3px solid #fff;
  box-shadow: 0 0 0 2px #e2e8f0;
  flex-shrink: 0;
  margin-top: 2px;
  z-index: 1;
}

.pd-milestone.ms-completed .pd-milestone-dot {
  background: linear-gradient(135deg, #22c38e, #10b981);
  box-shadow: 0 0 0 2px #a7f3d0;
}

.pd-milestone.ms-current .pd-milestone-dot {
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.35);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.35); }
  50% { box-shadow: 0 0 0 5px rgba(102, 126, 234, 0.15); }
}

.pd-milestone-info {
  flex: 1;
}

.pd-milestone-title {
  font-size: 0.9rem;
  color: #334155;
  font-weight: 500;
}

.pd-milestone-date {
  font-size: 0.78rem;
  color: #94a3b8;
  margin-top: 0.15rem;
}

.pd-milestone-status {
  font-size: 0.75rem;
  padding: 0.2rem 0.6rem;
  border-radius: 10px;
  font-weight: 500;
  flex-shrink: 0;
}

.pd-milestone.ms-completed .pd-milestone-status {
  background: #ecfdf5;
  color: #047857;
}

.pd-milestone.ms-current .pd-milestone-status {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.pd-milestone.ms-pending .pd-milestone-status {
  background: #f8fafc;
  color: #94a3b8;
}

.pd-milestone-status.is-clickable {
  cursor: pointer;
  transition: all 0.2s ease;
}

.pd-milestone-status.is-clickable:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
}

/* 里程碑卡片头（标题 + 管理按钮） */
.pd-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.8rem;
  margin-bottom: 1rem;
}

.pd-card-head .pd-card-title {
  margin-bottom: 0;
}

/* 里程碑表单 */
.pd-milestone-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  align-items: center;
  padding: 0.9rem;
  margin-bottom: 0.9rem;
  background: #f8f9ff;
  border: 1px dashed #c7d2fe;
  border-radius: 12px;
}

.pd-milestone-input {
  flex: 1 1 200px;
  min-width: 0;
  padding: 0.5rem 0.8rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.82rem;
  outline: none;
  transition: border-color 0.2s ease;
}

.pd-milestone-input:focus {
  border-color: #667eea;
}

.pd-milestone-select {
  padding: 0.5rem 0.8rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.82rem;
  outline: none;
  background: #fff;
  color: #334155;
  cursor: pointer;
}

.pd-milestone-form-actions {
  display: flex;
  gap: 0.5rem;
  margin-left: auto;
}

/* 里程碑行内操作 */
.pd-milestone-ops {
  display: flex;
  gap: 0.3rem;
  flex-shrink: 0;
}

.pd-milestone-op {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 7px;
  background: #f1f5f9;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pd-milestone-op:hover {
  background: rgba(102, 126, 234, 0.15);
  transform: scale(1.08);
}

/* ===== 团队成员 ===== */
.pd-members {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 0.9rem;
}

.pd-member {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.9rem 1rem;
  border: 1px solid #eef0f6;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: #fafbfe;
}

.pd-member:hover {
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.pd-member.is-leader {
  border-color: rgba(102, 126, 234, 0.5);
  background: rgba(102, 126, 234, 0.06);
}

.pd-member-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #94a3b8, #64748b);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 1rem;
  flex-shrink: 0;
}

.pd-member-avatar.leader-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.4);
}

.pd-member-info {
  min-width: 0;
}

.pd-member-name {
  font-size: 0.9rem;
  color: #334155;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  flex-wrap: wrap;
}

.pd-member-role {
  font-size: 0.78rem;
  color: #94a3b8;
  margin-top: 0.15rem;
}

.pd-member-badge {
  font-size: 0.7rem;
  padding: 0.1rem 0.45rem;
  border-radius: 8px;
  font-weight: 500;
}

.pd-member-badge.leader {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}

.pd-member-badge.me {
  background: #fef3c7;
  color: #92400e;
}

/* ===== 动态发布 ===== */
.pd-post-box {
  background: #f8fafc;
  border: 1px solid #eef0f6;
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 1.2rem;
}

.pd-post-textarea {
  width: 100%;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 0.7rem 0.9rem;
  font-size: 0.88rem;
  font-family: inherit;
  color: #334155;
  resize: vertical;
  min-height: 56px;
  outline: none;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.pd-post-textarea:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.pd-post-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.7rem;
  gap: 0.8rem;
  flex-wrap: wrap;
}

.pd-post-hint {
  font-size: 0.75rem;
  color: #94a3b8;
}

.pd-post-disabled {
  background: #f8fafc;
  border: 1px dashed #e2e8f0;
  border-radius: 10px;
  padding: 0.8rem 1rem;
  font-size: 0.82rem;
  color: #94a3b8;
  text-align: center;
  margin-bottom: 1.2rem;
}

/* ===== 时间线 ===== */
.pd-timeline {
  display: flex;
  flex-direction: column;
}

.pd-timeline-item {
  display: flex;
  gap: 0.9rem;
  padding: 0.75rem 0;
  position: relative;
}

.pd-timeline-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 17px;
  top: 44px;
  bottom: -4px;
  width: 2px;
  background: #eef0f6;
}

.pd-timeline-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 0.9rem;
  flex-shrink: 0;
  z-index: 1;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.pd-timeline-content {
  flex: 1;
  background: #fafbfe;
  border: 1px solid #eef0f6;
  border-radius: 10px;
  padding: 0.75rem 1rem;
  min-width: 0;
}

.pd-timeline-head {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  flex-wrap: wrap;
  margin-bottom: 0.3rem;
}

.pd-timeline-author {
  font-size: 0.88rem;
  font-weight: 600;
  color: #334155;
}

.pd-timeline-role {
  font-size: 0.72rem;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 0.1rem 0.45rem;
  border-radius: 8px;
}

.pd-timeline-type {
  font-size: 0.72rem;
  font-weight: 600;
}

.pd-timeline-time {
  font-size: 0.72rem;
  color: #94a3b8;
  margin-left: auto;
}

.pd-timeline-text {
  font-size: 0.85rem;
  color: #64748b;
  line-height: 1.6;
}

/* 动态中的文件附件（可直接下载） */
.pd-timeline-file {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
  padding: 0.55rem 0.7rem;
  background: #f8f9ff;
  border: 1px solid #eef0f6;
  border-radius: 10px;
  max-width: 100%;
}

.pd-timeline-file-icon {
  font-size: 1.05rem;
  flex-shrink: 0;
}

.pd-timeline-file-name {
  font-size: 0.82rem;
  color: #334155;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.pd-timeline-file-meta {
  font-size: 0.75rem;
  color: #94a3b8;
  flex-shrink: 0;
}

/* ===== 侧栏：文件 ===== */
.pd-side-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.8rem;
  margin-bottom: 1rem;
}

.pd-side-head .pd-card-title {
  margin-bottom: 0;
}

.btn-upload {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 10px;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.3);
  white-space: nowrap;
}

.btn-upload:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.pd-file-input {
  display: none;
}

.pd-file-list {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.pd-file-item {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.65rem 0.8rem;
  border: 1px solid #eef0f6;
  border-radius: 10px;
  transition: all 0.3s ease;
  background: #fafbfe;
}

.pd-file-item:hover {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.04);
}

.pd-file-icon {
  font-size: 1.4rem;
  flex-shrink: 0;
}

.pd-file-info {
  flex: 1;
  min-width: 0;
}

.pd-file-name {
  font-size: 0.85rem;
  color: #334155;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pd-file-meta {
  font-size: 0.72rem;
  color: #94a3b8;
  margin-top: 0.15rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pd-file-download {
  flex-shrink: 0;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border: none;
  padding: 0.35rem 0.8rem;
  border-radius: 8px;
  font-size: 0.78rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pd-file-download:hover {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}

/* ===== 侧栏：项目信息 ===== */
.pd-info-rows {
  display: flex;
  flex-direction: column;
}

.pd-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.55rem 0;
  border-bottom: 1px dashed #eef0f6;
  gap: 0.8rem;
}

.pd-info-row:last-child {
  border-bottom: none;
}

.pd-info-row span {
  font-size: 0.82rem;
  color: #94a3b8;
  flex-shrink: 0;
}

.pd-info-row b {
  font-size: 0.82rem;
  color: #334155;
  font-weight: 500;
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pd-info-status {
  color: #667eea !important;
}

/* ===== 侧栏：统计 ===== */
.pd-stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.8rem;
  margin-bottom: 1.2rem;
}

.pd-stat {
  background: #f8fafc;
  border: 1px solid #eef0f6;
  border-radius: 10px;
  padding: 0.9rem;
  text-align: center;
}

.pd-stat b {
  display: block;
  font-size: 1.4rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.pd-stat span {
  font-size: 0.75rem;
  color: #94a3b8;
}

.pd-milestone-progress-head {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #64748b;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

/* ===== 按钮 ===== */
.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border: none;
  padding: 0.55rem 1.3rem;
  border-radius: 10px;
  font-size: 0.88rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
  padding: 0.55rem 1.3rem;
  border-radius: 10px;
  font-size: 0.88rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

/* ===== 加载 / 空状态 ===== */
.pd-loading {
  min-height: 70vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  color: #667eea;
  font-size: 0.95rem;
}

.pd-loading-spinner {
  width: 44px;
  height: 44px;
  border: 4px solid rgba(102, 126, 234, 0.15);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.pd-notfound {
  min-height: 60vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
}

.pd-notfound-icon {
  font-size: 3rem;
}

.pd-notfound p {
  color: #64748b;
  font-size: 1rem;
}

.pd-empty {
  text-align: center;
  color: #94a3b8;
  font-size: 0.85rem;
  padding: 1rem 0;
}

/* ===== Toast ===== */
.pd-toast {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  padding: 0.7rem 1.4rem;
  border-radius: 12px;
  font-size: 0.88rem;
  font-weight: 500;
  z-index: 9999;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.15);
  color: #fff;
  max-width: 80vw;
}

.pd-toast.toast-success {
  background: linear-gradient(135deg, #22c38e, #10b981);
}

.pd-toast.toast-warning {
  background: linear-gradient(135deg, #f59e0b, #f97316);
}

.pd-toast.toast-info {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.3s ease;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-10px);
}

/* ===== 响应式 ===== */
@media (max-width: 1024px) {
  .pd-body {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .project-detail-page {
    padding: 1rem;
  }

  .pd-header {
    padding: 1.2rem;
  }

  .pd-title {
    font-size: 1.4rem;
  }

  .pd-meta {
    grid-template-columns: 1fr 1fr;
  }

  .pd-feature-list {
    grid-template-columns: 1fr;
  }

  .pd-topbar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
