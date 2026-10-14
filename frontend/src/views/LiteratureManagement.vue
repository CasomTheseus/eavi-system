<template>
  <div class="literature-management">
    <!-- 浮动装饰元素 -->
    <div class="floating-elements">
      <div class="floating-book">📖</div>
      <div class="floating-book">📚</div>
      <div class="floating-book">📓</div>
      <div class="floating-book">📔</div>
    </div>

    <!-- 主要内容 -->
    <div class="main-container">
      <div class="book-spine">文献管理</div>
      
      <!-- 侧边栏 -->
      <div class="sidebar">
        <div class="sidebar-section">
          <div class="sidebar-title">文献目录</div>
          <div class="folder-actions">
            <button class="btn btn-primary" @click="createFolderRoot">新建目录</button>
          </div>
          <div class="folder-tree">
            <div v-for="node in folderTree" :key="node.id" class="folder-node">
              <div class="folder-item" :class="{ active: selectedFolderId === node.id }">
                <span v-if="hasChildren(node)" class="caret" @click.stop="toggleFolderExpand(node.id)">{{ isExpanded(node.id) ? '▾' : '▸' }}</span>
                <span v-else class="caret-placeholder"></span>
                <span class="folder-name" @click="selectFolder(node)">{{ node.name }}</span>
                <span class="folder-count">{{ countFolderPapers(node) }}</span>
                <button class="folder-add" @click.stop="createSubfolder(node.id)">＋</button>
                <button 
                  v-if="node.name !== '我的文库'" 
                  class="folder-delete" 
                  @click.stop="deleteFolder(node.id)"
                >🗑</button>
              </div>
              <div v-if="hasChildren(node) && isExpanded(node.id)" class="folder-children">
                <div v-for="child in node.children" :key="child.id" class="folder-node">
                  <div class="folder-item" :class="{ active: selectedFolderId === child.id }">
                    <span v-if="hasChildren(child)" class="caret" @click.stop="toggleFolderExpand(child.id)">{{ isExpanded(child.id) ? '▾' : '▸' }}</span>
                    <span v-else class="caret-placeholder"></span>
                    <span class="folder-name" @click="selectFolder(child)">{{ child.name }}</span>
                    <span class="folder-count">{{ countFolderPapers(child) }}</span>
                    <button class="folder-add" @click.stop="createSubfolder(child.id)">＋</button>
                    <button 
                      v-if="child.name !== '我的文库'" 
                      class="folder-delete" 
                      @click.stop="deleteFolder(child.id)"
                    >🗑</button>
                  </div>
                  <div v-if="hasChildren(child) && isExpanded(child.id)" class="folder-children">
                    <div v-for="g in child.children" :key="g.id" class="folder-node">
                      <div class="folder-item" :class="{ active: selectedFolderId === g.id }">
                        <span v-if="hasChildren(g)" class="caret" @click.stop="toggleFolderExpand(g.id)">{{ isExpanded(g.id) ? '▾' : '▸' }}</span>
                        <span v-else class="caret-placeholder"></span>
                        <span class="folder-name" @click="selectFolder(g)">{{ g.name }}</span>
                        <span class="folder-count">{{ countFolderPapers(g) }}</span>
                        <button class="folder-add" @click.stop="createSubfolder(g.id)">＋</button>
                        <button 
                          v-if="g.name !== '我的文库'" 
                          class="folder-delete" 
                          @click.stop="deleteFolder(g.id)"
                        >🗑</button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 主内容区 -->
      <div class="content-area">
        <div class="content-header">
          <h1 class="content-title">📚 文献管理</h1>
          <p class="content-subtitle">管理团队研究文献，跟踪论文发表进度</p>
        </div>
        
        <div class="content-body">
          <!-- 添加文献按钮 -->
          <div class="action-bar">
            <button class="btn btn-primary new-literature-btn" @click="addNewLiterature">
              <span>➕</span>
              <span>添加文献</span>
            </button>
          </div>
          
          <!-- 搜索和筛选栏 -->
          <div class="search-filter-bar">
            <div class="search-container">
              <span class="search-icon">🔍</span>
              <input 
                type="text" 
                class="search-input" 
                placeholder="搜索文献标题、作者、关键词..."
                v-model="searchQuery"
                @input="onSearchInput"
              >
            </div>
            <select class="filter-select" v-model="sortBy" @change="onSortChange">
              <option value="year">按年份排序</option>
              <option value="citations">按引用数排序</option>
              <option value="date">按发表时间排序</option>
            </select>
            <button class="view-btn" @click="onSearchClick">🔎 搜索</button>
          </div>
          
          
          
          <!-- 文献统计 -->
          <div class="stats-bar">
            <div class="stat-item">
              <div class="stat-number">{{ totalLiterature }}</div>
              <div class="stat-label">总文献数</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ readCount }}</div>
              <div class="stat-label">已阅读</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ readingCount }}</div>
              <div class="stat-label">正在阅读</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ unreadCount }}</div>
              <div class="stat-label">未阅读</div>
            </div>
          </div>
          
          <!-- 文献网格视图 -->
          <div class="literature-grid" :class="{ 'list-view': viewMode === 'list' }">
            <div 
              v-for="literature in displayedLiterature" 
              :key="literature.id"
              class="literature-card"
              @click="viewLiteratureDetail(literature)"
            >
              <div class="literature-content">
                <div class="literature-header">
                  <div 
                    class="literature-status"
                    :class="`status-${literature.status}`"
                  >{{ getStatusText(literature.status) }}</div>
                </div>
                <div class="literature-title">{{ literature.title }}</div>
                <div class="literature-authors">{{ literature.authors }}</div>
                <div class="literature-journal">{{ literature.journal }}</div>
                <div class="literature-abstract">{{ literature.abstract }}</div>
                <div class="literature-bottom">
                  <div class="literature-tags">
                    <span 
                      v-for="tag in literature.tags" 
                      :key="tag"
                      class="tag"
                    >{{ tag }}</span>
                  </div>
                </div>
              </div>
              <div class="literature-sidebar">
              <div class="literature-meta">
                <span>📅 上传时间: {{ literature.uploadDate }}</span>
                <span>📊 引用: {{ literature.citations }}</span>
                <span>⭐ {{ literature.impact }}</span>
                <span>📂 所属目录: {{ getPaperFolders(literature.id).map(f=>f.name).join('、') || '未归档' }}</span>
              </div>
              <div class="literature-actions">
                <button class="btn btn-accent" @click.stop="openAddToFolder(literature)">📂 添加到目录</button>
                <button class="btn btn-primary" @click.stop="openPaper(literature)">📄 论文</button>
                <button class="btn btn-secondary" @click.stop="openCode(literature)">💻 代码</button>
              </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showFolderPicker" class="modal-mask" @click.self="closeFolderPicker">
      <div class="modal-card">
        <div class="modal-header">添加到目录</div>
        <div class="modal-body">
          <select v-model="selectedFolderForAdd" style="width:100%; padding:0.5rem; border:1px solid #e2e8f0; border-radius:8px;">
            <option v-for="opt in folderOptions" :key="opt.id" :value="opt.id">{{ opt.label }}</option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="closeFolderPicker">取消</button>
          <button class="btn btn-primary" @click="confirmAddToFolder">确认添加</button>
        </div>
      </div>
    </div>
    <div v-if="isGlobalBlocking" class="blocker-mask">
      <div class="blocking-banner">{{ blockingText || '正在处理...' }}</div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  name: 'LiteratureManagement',
  data() {
    return {
      isLoading: false,
      page: 1,
      pageSize: 20,
      sortOrder: 'desc',
      searchQuery: '',
      sortBy: 'year',
      viewMode: 'grid',
      activeCategory: 'all',
      activeStatus: null,
      
      literatureCategories: [
        { id: 'all', name: '全部文献', icon: '📚', count: 45 },
        { id: 'journal', name: '期刊论文', icon: '🔬', count: 28 },
        { id: 'conference', name: '会议论文', icon: '🎓', count: 12 },
        { id: 'book', name: '书籍章节', icon: '📖', count: 3 },
        { id: 'report', name: '技术报告', icon: '📄', count: 2 }
      ],
      
      statusFilters: [
        { id: 'published', name: '已发表', icon: '✅', count: 32 },
        { id: 'draft', name: '草稿', icon: '📝', count: 8 },
        { id: 'review', name: '审稿中', icon: '🔍', count: 5 }
      ],
      literatureList: [
        {
          id: 1,
          title: 'Deep Learning for Computer Vision: A Comprehensive Survey',
          authors: '张三, 李四, 王五',
          journal: 'IEEE Transactions on Pattern Analysis and Machine Intelligence',
          abstract: '本文对计算机视觉领域的深度学习方法进行了全面综述，涵盖了从基础的卷积神经网络到最新的Transformer架构。我们分析了各种方法在图像分类、目标检测、语义分割等任务中的表现...',
          tags: ['深度学习', '计算机视觉', '综述'],
          uploadDate: '2024-01-15',
          citations: 156,
          impact: 'IF: 17.73',
          status: 'read',
          field: 'cv',
          category: 'journal',
          paperUrl: 'https://arxiv.org/abs/1904.05049',
          codeUrl: 'https://github.com/paperswithcode',
          datasets: ['ImageNet', 'COCO'],
          tasks: ['图像分类', '目标检测'],
          sotaCount: 0
        },
        {
          id: 2,
          title: 'Attention Mechanisms in Natural Language Processing: Recent Advances',
          authors: '李四, 赵六, 钱七',
          journal: 'ACL 2024',
          abstract: '注意力机制在自然语言处理中发挥着重要作用。本文回顾了近年来注意力机制的发展，包括自注意力、多头注意力、交叉注意力等变体，并分析了它们在机器翻译、文本摘要等任务中的应用...',
          tags: ['注意力机制', '自然语言处理', 'Transformer'],
          uploadDate: '2024-01-10',
          citations: 89,
          impact: '顶级会议',
          status: 'reading',
          field: 'nlp',
          category: 'conference',
          paperUrl: 'https://arxiv.org/abs/1706.03762',
          codeUrl: 'https://github.com/tensorflow/tensor2tensor',
          datasets: ['WMT14', 'WikiText'],
          tasks: ['机器翻译', '文本摘要'],
          sotaCount: 3
        },
        {
          id: 3,
          title: 'Efficient Training of Large Language Models with Gradient Compression',
          authors: '王五, 孙八, 周九',
          journal: 'NeurIPS 2023',
          abstract: '大型语言模型的训练需要大量的计算资源。本文提出了一种基于梯度压缩的高效训练方法，通过智能的梯度量化和稀疏化技术，显著减少了训练时间和内存消耗，同时保持了模型性能...',
          tags: ['大语言模型', '梯度压缩', '分布式训练'],
          uploadDate: '2023-12-08',
          citations: 89,
          impact: '顶级会议',
          status: 'important',
          field: 'ml',
          category: 'conference',
          paperUrl: 'https://arxiv.org/abs/2306.00001',
          codeUrl: 'https://github.com/openai',
          datasets: ['OpenWebText'],
          tasks: ['语言建模'],
          sotaCount: 1
        },
        {
          id: 4,
          title: 'Multi-Modal Learning for Autonomous Driving: A Survey',
          authors: '赵六, 钱七, 孙八',
          journal: 'IEEE Transactions on Intelligent Transportation Systems',
          abstract: '自动驾驶系统需要处理来自多种传感器的数据。本文综述了多模态学习在自动驾驶中的应用，包括视觉-激光雷达融合、视觉-语言理解等技术，并讨论了未来的发展方向...',
          tags: ['多模态学习', '自动驾驶', '传感器融合'],
          uploadDate: '2024-01-05',
          citations: 67,
          impact: '高影响因子',
          status: 'unread',
          field: 'cv',
          category: 'journal',
          paperUrl: 'https://arxiv.org/abs/2006.00002',
          codeUrl: 'https://github.com/waymo-research',
          datasets: ['KITTI', 'Waymo'],
          tasks: ['自动驾驶感知'],
          sotaCount: 0
        },
        {
          id: 5,
          title: 'Federated Learning with Differential Privacy: Challenges and Solutions',
          authors: '钱七, 孙八, 周九',
          journal: 'ICML 2023',
          abstract: '联邦学习在保护数据隐私的同时实现协作训练，但仍面临隐私泄露风险。本文结合差分隐私技术，提出了一种新的联邦学习框架，在保证强隐私保护的同时维持了良好的模型性能...',
          tags: ['联邦学习', '差分隐私', '隐私保护'],
          uploadDate: '2023-07-23',
          citations: 234,
          impact: '顶级会议',
          status: 'read',
          field: 'ml',
          category: 'conference',
          paperUrl: 'https://arxiv.org/abs/2012.00003',
          codeUrl: 'https://github.com/google-research',
          datasets: ['EMNIST'],
          tasks: ['联邦学习'],
          sotaCount: 2
        },
        {
          id: 6,
          title: 'Graph Neural Networks for Social Network Analysis: A Comprehensive Study',
          authors: '孙八, 周九, 吴十',
          journal: 'Nature Machine Intelligence',
          abstract: '图神经网络在社交网络分析中展现出强大的能力。本文系统研究了各种图神经网络架构在社交网络分析任务中的表现，包括节点分类、链接预测、社区检测等，并提出了改进方法...',
          tags: ['图神经网络', '社交网络', '网络分析'],
          uploadDate: '2023-11-15',
          citations: 178,
          impact: 'IF: 25.89',
          status: 'reading',
          field: 'ml',
          category: 'journal',
          paperUrl: 'https://arxiv.org/abs/1812.00004',
          codeUrl: 'https://github.com/pyg-team/pytorch_geometric',
          datasets: ['Cora', 'CiteSeer'],
          tasks: ['节点分类', '链接预测'],
          sotaCount: 4
        }
      ],
      filteredLiterature: [],
      selectedFolderId: null,
      expandedFolderIds: [],
      folderTree: [],
      showFolderPicker: false,
      selectedPaperForAdd: null,
      selectedFolderForAdd: null,
      isGlobalBlocking: false,
      blockingText: ''
    }
  },
  computed: {
    totalLiterature() {
      return this.literatureList.length;
    },
    readCount() {
      return this.literatureList.filter(lit => lit.status === 'read').length;
    },
    readingCount() {
      return this.literatureList.filter(lit => lit.status === 'reading').length;
    },
    unreadCount() {
      return this.literatureList.filter(lit => lit.status === 'unread').length;
    },
    displayedLiterature() {
      if (!this.selectedFolderId) return this.filteredLiterature;
      const ids = this.collectFolderPaperIds(this.selectedFolderId);
      const idSet = new Set(ids);
      return this.filteredLiterature.filter(p => idSet.has(p.id));
    },
    folderOptions() {
      const res = [];
      const walk = (nodes, prefix) => {
        (nodes || []).forEach(n => {
          const label = prefix ? `${prefix} / ${n.name}` : n.name;
          res.push({ id: n.id, label });
          if (n.children && n.children.length) walk(n.children, label);
        });
      };
      walk(this.folderTree, '');
      return res;
    }
  },
  mounted() {
    this.filteredLiterature = [...this.literatureList];
    this.fetchPapersList();
    this.fetchFolderTree();
  },
  methods: {
    async fetchFolderTree() {
      try {
        const base = import.meta.env.VITE_API_BASE || 'http://localhost:8080';
        const token = localStorage.getItem('token');
        const res = await axios.get(`${base}/folders/tree`, { headers: token ? { Authorization: `Bearer ${token}` } : {} });
        const data = res.data && res.data.data ? res.data.data : {};
        const folders = Array.isArray(data.folders) ? data.folders : [];
        this.folderTree = folders;
        if (folders.length && !this.selectedFolderId) this.selectedFolderId = folders[0].id;
        this.expandedFolderIds = folders.map(f => f.id);
      } catch (e) {
      }
    },
    onSearchInput() {
      this.page = 1;
      this.fetchPapersList();
    },
    onSortChange() {
      this.page = 1;
      this.fetchPapersList();
    },
    onSearchClick() {
      this.page = 1;
      this.fetchPapersList();
    },
    async fetchPapersList() {
      try {
        if (this.isLoading) return;
        this.isLoading = true;
        const base = import.meta.env.VITE_API_BASE || 'http://localhost:8080';
        const token = localStorage.getItem('token');
        const sortByParam = this.sortBy === 'citations' ? 'citations' : 'uploadTime';
        const res = await axios.get(`${base}/papers`, {
          params: {
            page: this.page,
            pageSize: this.pageSize,
            search: this.searchQuery,
            sortBy: sortByParam,
            sortOrder: this.sortOrder
          },
          headers: token ? { Authorization: `Bearer ${token}` } : {}
        });
        const data = res.data && res.data.data ? res.data.data : {};
        const records = Array.isArray(data.papers) ? data.papers : [];
        const toCode = (s) => {
          const map = { '已阅读': 'read', '未阅读': 'unread', '正在阅读': 'reading', '重要文献': 'important' };
          return map[s] || (['read','reading','unread','important'].includes(s) ? s : 'unread');
        };
        this.literatureList = records.map(p => ({
          id: p.id,
          title: p.title,
          authors: p.authors,
          journal: p.venue,
          abstract: p.paperAbstract,
          uploadDate: p.uploadTime,
          citations: p.citations || 0,
          impact: p.impact || '',
          status: toCode(p.status),
          field: 'ml',
          category: 'journal',
          paperUrl: p.paperUrl,
          codeUrl: p.codeUrl,
          tags: [],
          datasets: [],
          tasks: [],
          sotaCount: 0
        }));
        this.filteredLiterature = [...this.literatureList];
        this.filterLiterature();
      } catch (e) {
      } finally {
        this.isLoading = false;
      }
    },
    setActiveCategory(categoryId) {
      this.activeCategory = categoryId;
      this.filterLiterature();
    },
    setActiveStatus(statusId) {
      this.activeStatus = statusId;
      this.filterLiterature();
    },
    
    setViewMode(mode) {
      this.viewMode = mode;
    },
    filterLiterature() {
      let filtered = [...this.literatureList];
      
      // 分类过滤
      if (this.activeCategory && this.activeCategory !== 'all') {
        filtered = filtered.filter(lit => lit.category === this.activeCategory);
      }
      
      
      
      // 状态过滤
      if (this.activeStatus) {
        filtered = filtered.filter(lit => lit.status === this.activeStatus);
      }
      
      this.filteredLiterature = filtered;
    },
    
    isExpanded(id) { return this.expandedFolderIds.includes(id); },
    hasChildren(n) { return n && Array.isArray(n.children) && n.children.length > 0; },
    toggleFolderExpand(id) {
      const i = this.expandedFolderIds.indexOf(id);
      if (i >= 0) this.expandedFolderIds.splice(i, 1); else this.expandedFolderIds.push(id);
    },
    selectFolder(node) { this.selectedFolderId = node.id; },
    async createFolderRoot() {
      const name = window.prompt('请输入目录名称');
      if (!name) return;
      try {
        const base = import.meta.env.VITE_API_BASE || 'http://localhost:8080';
        const token = localStorage.getItem('token');
        const res = await axios.post(`${base}/folders`, { name }, { headers: token ? { Authorization: `Bearer ${token}` } : {} });
        const payload = res.data && res.data.data ? res.data.data : {};
        const node = { id: payload.folderId, name: payload.name, paperIds: [], children: [] };
        this.folderTree.push(node);
        this.selectedFolderId = node.id;
      } catch (e) {
      }
    },
    async createSubfolder(parentId) {
      const name = window.prompt('请输入子目录名称');
      if (!name) return;
      const parent = this.findFolderById(parentId);
      if (!parent) return;
      try {
        const base = import.meta.env.VITE_API_BASE || 'http://localhost:8080';
        const token = localStorage.getItem('token');
        const res = await axios.post(`${base}/folders`, { name, parentId }, { headers: token ? { Authorization: `Bearer ${token}` } : {} });
        const payload = res.data && res.data.data ? res.data.data : {};
        const node = { id: payload.folderId, name: payload.name, paperIds: [], children: [] };
        if (!parent.children) parent.children = [];
        parent.children.push(node);
        this.selectedFolderId = node.id;
        if (!this.isExpanded(parentId)) this.expandedFolderIds.push(parentId);
      } catch (e) {
      }
    },
    findFolderById(id) {
      const stack = [...this.folderTree];
      while (stack.length) {
        const node = stack.shift();
        if (node.id === id) return node;
        if (node.children) stack.push(...node.children);
      }
      return null;
    },
    collectFolderPaperIds(id) {
      const node = this.findFolderById(id);
      if (!node) return [];
      const res = new Set(node.paperIds || []);
      const stack = [...(node.children || [])];
      while (stack.length) {
        const n = stack.shift();
        (n.paperIds || []).forEach(x => res.add(x));
        if (n.children) stack.push(...n.children);
      }
      return Array.from(res);
    },
    countFolderPapers(node) {
      const set = new Set((node.paperIds || []));
      const stack = [...(node.children || [])];
      while (stack.length) {
        const n = stack.shift();
        (n.paperIds || []).forEach(x => set.add(x));
        if (n.children) stack.push(...n.children);
      }
      return set.size;
    },
    getStatusText(status) {
      const statusMap = {
        'read': '已阅读',
        'reading': '正在阅读',
        'unread': '未阅读',
        'important': '重要文献'
      };
      return statusMap[status] || status;
    },
    addNewLiterature() {
      // 跳转到添加文献页面
      this.$router.push('/upload');
    },
    viewLiteratureDetail(literature) {
      // 跳转到文献详情页面
      this.$router.push(`/literature/${literature.id}`);
    },
    viewDetail(literature) {
      this.$router.push(`/literature/${literature.id}`);
    },
    openPaper(literature) {
      this.$router.push(`/literature/${literature.id}`);
    },
    openCode(literature) {
      this.$router.push({ path: `/literature/${literature.id}`, query: { section: 'code' } });
    },
    openDatasets(literature) {
      this.$router.push({ path: `/literature/${literature.id}`, query: { section: 'datasets' } });
    },
    viewSota(literature) {
      this.$router.push({ path: `/literature/${literature.id}`, query: { section: 'results' } });
    },
    openAddToFolder(literature) {
      this.selectedPaperForAdd = literature;
      this.selectedFolderForAdd = this.selectedFolderId;
      this.showFolderPicker = true;
    },
    closeFolderPicker() {
      this.showFolderPicker = false;
      this.selectedPaperForAdd = null;
    },
    async confirmAddToFolder() {
      if (!this.selectedPaperForAdd || !this.selectedFolderForAdd) return;
      const node = this.findFolderById(this.selectedFolderForAdd);
      const pid = this.selectedPaperForAdd.id;
      if (node && Array.isArray(node.paperIds) && node.paperIds.includes(pid)) {
        const names = this.getPaperFolders(pid).map(n => n.name).join('、');
        window.alert(`该文献已存在于所选目录。所属目录：${names || '未归档'}`);
        this.closeFolderPicker();
        return;
      }
      try {
        this.setBlocked(true, '正在添加到目录...');
        const base = import.meta.env.VITE_API_BASE || 'http://localhost:8080';
        const token = localStorage.getItem('token');
        await axios.post(`${base}/folders/${this.selectedFolderForAdd}/papers`, { paperId: pid }, { headers: token ? { Authorization: `Bearer ${token}` } : {} });
        if (node) {
          node.paperIds = Array.isArray(node.paperIds) ? node.paperIds : [];
          if (!node.paperIds.includes(pid)) node.paperIds.push(pid);
        }
      } catch (e) {
      } finally {
        this.closeFolderPicker();
        this.setBlocked(false);
      }
    },
    findFoldersByPaperId(paperId) {
      const res = [];
      const stack = [...this.folderTree];
      while (stack.length) {
        const n = stack.shift();
        if ((n.paperIds || []).includes(paperId)) res.push(n);
        if (n.children) stack.push(...n.children);
      }
      return res;
    },
    getPaperFolders(paperId) { return this.findFoldersByPaperId(paperId); },
    collectSubtreePaperIds(id) {
      const node = this.findFolderById(id);
      if (!node) return [];
      const set = new Set(node.paperIds || []);
      const stack = [...(node.children || [])];
      while (stack.length) {
        const n = stack.shift();
        (n.paperIds || []).forEach(x => set.add(x));
        if (n.children) stack.push(...n.children);
      }
      return Array.from(set);
    },
    removeFolderById(id) {
      let removed = null;
      const dfs = (nodes) => {
        for (let i = 0; i < nodes.length; i++) {
          const n = nodes[i];
          if (n.id === id) { removed = nodes.splice(i, 1)[0]; return true; }
          if (n.children && dfs(n.children)) return true;
        }
        return false;
      };
      dfs(this.folderTree);
      return removed;
    },
    async deleteFolderOnServer(folderId) {
      const base = import.meta.env.VITE_API_BASE || 'http://localhost:8080';
      const token = localStorage.getItem('token');
      const headers = token ? { Authorization: `Bearer ${token}` } : {};
      try { await axios.delete(`${base}/folders/${folderId}`, { headers }); return true; } catch (e) {}
      try { await axios.post(`${base}/folders/${folderId}/delete`, {}, { headers }); return true; } catch (e) {}
      try { await axios.post(`${base}/folders/delete`, { folderId }, { headers }); return true; } catch (e) {}
      return false;
    },
    async deleteFolder(folderId) {
      const node = this.findFolderById(folderId);
      if (!node) return;
      if (node.name === '我的文库') { window.alert('无法删除我的文库'); return; }
      const root = this.folderTree.find(f => f.name === '我的文库') || this.folderTree[0];
      if (!root) return;
      const ids = this.collectSubtreePaperIds(folderId);
      try {
        this.setBlocked(true, '正在删除目录...');
        const base = import.meta.env.VITE_API_BASE || 'http://localhost:8080';
        const token = localStorage.getItem('token');
        for (const pid of ids) {
          root.paperIds = Array.isArray(root.paperIds) ? root.paperIds : [];
          if (!root.paperIds.includes(pid)) {
            try {
              await axios.post(`${base}/folders/${root.id}/papers`, { paperId: pid }, { headers: token ? { Authorization: `Bearer ${token}` } : {} });
              root.paperIds.push(pid);
            } catch (e) {}
          }
        }
        const ok = await this.deleteFolderOnServer(folderId);
        if (ok) {
          this.removeFolderById(folderId);
          this.selectedFolderId = root.id;
        } else {
          window.alert('后端删除接口不可用或未对齐，已回退。');
        }
      } finally {
        this.setBlocked(false);
      }
    },
    setBlocked(flag, text) {
      this.isGlobalBlocking = !!flag;
      this.blockingText = text || '';
      try { document.body.style.overflow = flag ? 'hidden' : ''; } catch (e) {}
    }
  }
}
</script>

<style scoped>
* , *::before, *::after { box-sizing: border-box; }
/* 全局样式 */
.literature-management {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow-x: hidden;
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
  animation: float 12s infinite ease-in-out;
}

.floating-book:nth-child(1) {
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.floating-book:nth-child(2) {
  top: 60%;
  left: 80%;
  animation-delay: 3s;
}

.floating-book:nth-child(3) {
  top: 80%;
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

/* 主容器 */
.main-container {
  width: 80%;
  max-width: none;
  margin: 2rem auto;
  padding: 2rem;
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 2rem;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.1),
    0 0 0 1px rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: visible;
}

.main-container::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 64px;
  background: linear-gradient(180deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  border-radius: 20px 0 0 20px;
  box-shadow: inset -2px 0 4px rgba(0, 0, 0, 0.1);
}

/* 书脊装饰 */
.book-spine {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 64px;
  background: linear-gradient(180deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  border-radius: 20px 0 0 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  color: white;
  font-weight: bold;
  font-size: 1.1rem;
  letter-spacing: 2px;
  z-index: 2;
}

/* 侧边栏样式 */
.sidebar {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 2rem 2rem 2rem 3rem;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  backdrop-filter: blur(10px);
  height: fit-content;
  position: sticky;
  top: 2rem;
  margin-left: 64px;
}

.sidebar-section {
  margin-bottom: 2rem;
}

.sidebar-title {
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #f0f0f0;
}

.sidebar-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 1rem;
  margin-bottom: 0.5rem;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.5);
}

.sidebar-item:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateX(5px);
}

.sidebar-item.active {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.sidebar-item-content {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.sidebar-icon {
  font-size: 1.2rem;
}

.sidebar-count {
  background: rgba(0,0,0,0.1);
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: bold;
}

.sidebar-item.active .sidebar-count {
  background: rgba(255,255,255,0.2);
}

/* 内容区域 */
.content-area {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  backdrop-filter: blur(10px);
  min-width: 0;
}

.content-header {
  margin-bottom: 2rem;
}

.content-title {
  font-size: 2.5rem;
  font-weight: bold;
  background: linear-gradient(45deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.5rem;
}

.content-subtitle {
  color: #666;
  font-size: 1.1rem;
}

/* 操作栏 */
.action-bar {
  margin-bottom: 2rem;
}

.new-literature-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.new-literature-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 搜索和筛选栏 */
.search-filter-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  align-items: center;
  flex-wrap: wrap;
}

.search-container {
  position: relative;
  flex: 1;
  min-width: 300px;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
  font-size: 1.2rem;
}

.search-input {
  width: 100%;
  padding: 1rem 1rem 1rem 3rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.filter-select {
  padding: 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #667eea;
}

.view-toggle {
  display: flex;
  gap: 0.5rem;
}

.view-btn {
  padding: 1rem 1.5rem;
  border: 2px solid #e0e0e0;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.view-btn.active {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  border-color: transparent;
}



/* 统计栏 */
.stats-bar {
  display: flex;
  gap: 2rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: linear-gradient(45deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border-radius: 12px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 0.5rem;
}

.stat-label {
  color: #666;
  font-size: 0.9rem;
}

/* 文献列表视图 */
.literature-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.literature-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 1rem;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: flex-start;
  gap: 1.5rem;
  width: 100%;
}

.literature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.literature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  border-color: #667eea;
}

.literature-card:hover::before {
  opacity: 1;
}

.literature-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.literature-sidebar {
  flex-shrink: 0;
  width: 200px;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  justify-content: space-between;
  min-height: 100%;
}

.literature-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.literature-title {
  font-weight: 700;
  color: #1e293b;
  font-size: 1.45rem;
  line-height: 1.45;
  margin-bottom: 0.6rem;
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.literature-status {
  padding: 0.375rem 1rem;
  border-radius: 25px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.status-read {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #166534;
  border-color: #22c55e;
}

.status-read::before {
  content: '✅ ';
  font-weight: bold;
}

.status-unread {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
  border-color: #f59e0b;
}

.status-unread::before {
  content: '📄 ';
}

.status-reading {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1e40af;
  border-color: #3b82f6;
}

.status-reading::before {
  content: '📖 ';
}

.status-important {
  background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%);
  color: #be185d;
  border-color: #ec4899;
}

.status-important::before {
  content: '⭐ ';
}

.literature-authors {
  color: #64748b;
  font-size: 1.05rem;
  margin-bottom: 0.6rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.literature-authors::before {
  content: '👥';
  font-size: 0.875rem;
}

.literature-journal {
  color: #3b82f6;
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  padding: 0.5rem 0.75rem;
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  border-radius: 8px;
  border-left: 4px solid #3b82f6;
  display: inline-block;
}

.literature-abstract {
  color: #64748b;
  font-size: 1rem;
  line-height: 1.6;
  margin-bottom: 0.75rem;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  padding: 0.75rem;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 3px solid #e2e8f0;
  position: relative;
}

.literature-abstract::before {
  content: '"';
  position: absolute;
  top: 0.5rem;
  left: 0.5rem;
  font-size: 1.5rem;
  color: #cbd5e1;
  font-family: serif;
}

.literature-tags {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
}

.tag {
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  color: #475569;
  padding: 0.375rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.tag:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-1px);
}

.literature-sidebar {
  width: 200px;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.literature-meta {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  font-size: 0.95rem;
  color: #64748b;
  margin-bottom: 0;
  padding: 0.75rem;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.literature-meta span {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-weight: 500;
  padding: 0.25rem 0;
}

.literature-bottom {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-top: 0.5rem;
  gap: 1rem;
}

.literature-actions {
  display: flex;
  gap: 0.5rem;
  flex-direction: column;
  width: 100%;
  margin-top: 1rem;
}

.btn {
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  justify-content: center;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-secondary:hover {
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
  transform: translateY(-1px);
}

.btn-accent {
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
  color: #fff;
  font-size: 1.05rem;
  box-shadow: 0 8px 25px rgba(249, 115, 22, 0.35);
}

.btn-accent:hover { transform: translateY(-2px); }

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
  
  .literature-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .literature-grid {
    grid-template-columns: 1fr;
  }
  
  .search-filter-bar {
    flex-direction: column;
  }
  
  .literature-card {
    flex-direction: column;
  }
  
  .literature-sidebar {
    width: 100%;
  }
  
  .literature-actions {
    flex-direction: row;
  }
}
/* 目录树样式 */
.folder-actions { display: flex; gap: 0.5rem; margin-bottom: 0.75rem; }
.folder-tree { display: flex; flex-direction: column; gap: 0.25rem; }
.folder-node { margin-left: 0; }
.folder-item { display: flex; align-items: center; gap: 0.5rem; padding: 0.6rem 0.6rem; border-radius: 10px; cursor: pointer; }
.folder-item.active { background: rgba(102, 126, 234, 0.12); }
.caret { width: 1.2rem; text-align: center; color: #64748b; font-size: 1.6rem; }
.caret-placeholder { width: 1.2rem; display: inline-block; }
.folder-name { flex: 1; color: #1f2937; font-size: 1rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.folder-count { color: #64748b; background: rgba(0,0,0,0.06); border-radius: 10px; padding: 0.1rem 0.5rem; font-size: 0.95rem; }
.folder-add { border: none; background: transparent; color: #667eea; font-weight: 700; cursor: pointer; }
.folder-delete { border: none; background: transparent; color: #ef4444; font-weight: 700; cursor: pointer; }
.folder-delete:hover { color: #dc2626; }
.folder-children { margin-left: 1.25rem; border-left: 1px dashed #e5e7eb; padding-left: 0.75rem; }
</style>

<style scoped>
/* 弹窗样式 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 9999; }
.modal-card { background: #fff; border-radius: 12px; width: 420px; max-width: 92vw; padding: 1rem; box-shadow: 0 20px 60px rgba(0,0,0,0.2); }
.modal-header { font-size: 1.2rem; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem; }
.modal-body { display: flex; flex-direction: column; gap: 0.75rem; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.75rem; }

/* 全局阻断遮罩与横幅 */
.blocker-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.25); z-index: 10000; }
.blocking-banner { position: fixed; top: 0; left: 0; right: 0; padding: 1rem; text-align: center; font-weight: 800; font-size: 1.4rem; color: #fff; background: linear-gradient(135deg, #3b82f6 0%, #9333ea 100%); box-shadow: 0 10px 25px rgba(0,0,0,0.25); }
</style>
