// 项目数据文件（列表页 / 详情页共享，保证数据一致）
// 说明：
// - leaderUsername / leaderId 用于判断"当前登录用户是否为项目负责人"
// - members 中 username 与当前登录用户匹配即视为"项目成员"
// - activities 为项目动态时间线，展示时按 time 倒序（越靠上越新）
// - files 为项目文件，支持下载与上传

export const projects = [
  {
    // ===== 列表页字段（ProjectManagement.vue 使用）=====
    id: 'project_1',
    name: '智能推荐系统',
    description: '基于深度学习的个性化推荐算法研究与实现，提升用户体验和转化率。',
    leader: '张三',
    team: ['张三', '李四', '王五', '赵六'],
    tags: ['机器学习', '推荐算法', '深度学习'],
    progress: 75,
    status: 'active',
    startDate: '2023-10-15',
    endDate: '2024-03-30',
    category: 'ai',

    // ===== 详情页字段（ProjectDetail.vue 使用）=====
    title: '智能推荐系统',
    subtitle: '基于深度学习的个性化推荐算法研究与实现，通过多目标排序模型提升用户体验和业务转化率。',
    categoryName: 'AI项目',
    statusText: '进行中',
    publisher: 'EAVI 团队数据管理委员会',
    publishedAt: '2023-10-10',
    leaderId: 1,
    leaderUsername: 'admin', // 当前演示账号 admin 为本项目负责人
    leaderRole: '项目负责人',
    techStack: ['Python', 'PyTorch', 'Flask', 'PostgreSQL', 'Docker', '推荐算法'],
    members: [
      { id: 1, username: 'admin', name: '张三', role: '项目负责人' },
      { id: 2, username: 'lisi', name: '李四', role: '算法工程师' },
      { id: 3, username: 'wangwu', name: '王五', role: '后端开发工程师' },
      { id: 4, username: 'zhaoliu', name: '赵六', role: '测试工程师' }
    ],
    description: {
      overview: '智能推荐系统是一个面向电商与内容平台的个性化推荐服务平台。项目基于深度学习技术构建召回、粗排、精排完整的推荐链路，支持多目标（点击率、转化率、停留时长）联合优化，为不同业务场景提供高质量的个性化推荐能力。',
      features: [
        '基于双塔模型的多路召回策略',
        '多目标联合排序模型（MMoE）',
        '实时特征计算与在线推理服务',
        'A/B 实验平台与效果评估体系',
        '推荐效果可视化分析看板'
      ],
      architecture: '系统采用微服务架构：在线服务层使用 Flask + Redis 提供高性能推理接口，离线训练层基于 PyTorch 完成模型训练与评估，数据层使用 PostgreSQL + ClickHouse 存储用户行为与特征数据，整体通过 Docker + K8s 完成容器化部署。',
      scenarios: '适用于电商首页个性化推荐、内容资讯流推荐、搜索广告排序等场景，可根据业务需要灵活接入不同的推荐物品域，支撑日均千万级请求的在线服务。'
    },
    milestones: [
      { title: '需求分析与数据调研', date: '2023-10-15 ~ 2023-11-15', status: 'completed' },
      { title: '推荐链路框架搭建', date: '2023-11-16 ~ 2023-12-31', status: 'completed' },
      { title: '召回与排序模型训练', date: '2024-01-01 ~ 2024-02-28', status: 'current' },
      { title: '在线服务与 A/B 实验', date: '2024-03-01 ~ 2024-03-30', status: 'pending' }
    ],
    files: [
      { name: '项目立项申请书.pdf', size: '1.2 MB', uploader: '张三', uploadedAt: '2023-10-12 09:30', type: 'pdf' },
      { name: '需求规格说明书.docx', size: '2.3 MB', uploader: '张三', uploadedAt: '2023-10-20 14:00', type: 'doc' },
      { name: '推荐链路技术方案.pptx', size: '4.7 MB', uploader: '李四', uploadedAt: '2023-12-05 11:20', type: 'ppt' },
      { name: '模型训练实验报告.docx', size: '3.1 MB', uploader: '李四', uploadedAt: '2024-02-18 16:40', type: 'doc' }
    ],
    activities: [
      { type: 'progress', author: '张三', role: '项目负责人', content: '将项目整体进度更新至 75%', time: '2024-02-20 10:15' },
      { type: 'file', author: '李四', role: '算法工程师', content: '上传了文件：模型训练实验报告.docx', time: '2024-02-18 16:40' },
      { type: 'update', author: '张三', role: '项目负责人', content: '完成精排模型 v2 多目标实验，CTR 提升 8.3%，下一步进入线上 A/B 测试阶段', time: '2024-02-15 11:20' },
      { type: 'milestone', author: '张三', role: '项目负责人', content: '里程碑「召回与排序模型训练」正式启动', time: '2024-01-01 09:00' },
      { type: 'update', author: '李四', role: '算法工程师', content: '双塔召回模型完成离线评估，Recall@100 达到 0.92', time: '2023-12-28 17:30' },
      { type: 'file', author: '张三', role: '项目负责人', content: '上传了文件：推荐链路技术方案.pptx', time: '2023-12-05 11:20' },
      { type: 'member', author: '张三', role: '项目负责人', content: '李四、王五、赵六 加入项目团队', time: '2023-10-18 10:00' },
      { type: 'update', author: '张三', role: '项目负责人', content: '完成项目立项，启动需求分析与数据调研工作', time: '2023-10-15 09:30' }
    ]
  },
  {
    id: 'project_2',
    name: '数据可视化平台',
    description: '构建交互式数据可视化平台，支持多种图表类型和实时数据更新。',
    leader: '李四',
    team: ['李四', '王五', '陈七'],
    tags: ['数据可视化', 'Web开发', 'React'],
    progress: 60,
    status: 'active',
    startDate: '2023-11-01',
    endDate: '2024-04-15',
    category: 'web',

    title: '数据可视化平台',
    subtitle: '构建交互式数据可视化平台，支持多种图表类型、实时数据更新与自定义仪表盘。',
    categoryName: 'Web开发',
    statusText: '进行中',
    publisher: 'EAVI 团队数据管理委员会',
    publishedAt: '2023-10-28',
    leaderId: 2,
    leaderUsername: 'lisi',
    leaderRole: '项目负责人',
    techStack: ['React', 'Vue', 'ECharts', 'D3.js', 'Node.js', 'WebSocket'],
    members: [
      { id: 2, username: 'lisi', name: '李四', role: '项目负责人' },
      { id: 3, username: 'wangwu', name: '王五', role: '前端开发工程师' },
      { id: 5, username: 'admin', name: '管理员', role: '前端开发工程师' }, // 当前演示账号为项目成员
      { id: 7, username: 'chenqi', name: '陈七', role: '后端开发工程师' }
    ],
    description: {
      overview: '数据可视化平台旨在为企业提供一站式、可拖拽配置的数据可视化解决方案。平台内置丰富图表组件，支持拖拽布局、数据源接入、实时刷新与团队协作，帮助用户快速搭建业务监控大屏与数据分析仪表盘。',
      features: [
        '50+ 可视化图表组件，支持自由拖拽布局',
        '多数据源接入（MySQL / PostgreSQL / REST API / WebSocket）',
        '仪表盘实时刷新与联动筛选',
        '模板市场与团队共享协作',
        '图表导出（图片 / PDF）'
      ],
      architecture: '前端基于 React + ECharts 构建组件化渲染引擎，配置层使用 Vue 实现低代码画布，服务端基于 Node.js 提供配置持久化与数据代理能力，实时数据通过 WebSocket 推送，整体采用前后端分离架构。',
      scenarios: '适用于企业经营监控大屏、实时业务看板、数据中台可视化门户、运维监控平台等场景，帮助企业快速沉淀数据资产并驱动业务决策。'
    },
    milestones: [
      { title: '技术选型与框架搭建', date: '2023-11-01 ~ 2023-11-30', status: 'completed' },
      { title: '图表组件库开发', date: '2023-12-01 ~ 2024-01-31', status: 'completed' },
      { title: '画布与配置系统', date: '2024-02-01 ~ 2024-03-15', status: 'current' },
      { title: '数据接入与上线', date: '2024-03-16 ~ 2024-04-15', status: 'pending' }
    ],
    files: [
      { name: '可视化平台需求文档.pdf', size: '1.8 MB', uploader: '李四', uploadedAt: '2023-11-05 10:00', type: 'pdf' },
      { name: '图表组件规范.docx', size: '2.6 MB', uploader: '王五', uploadedAt: '2023-12-10 15:30', type: 'doc' },
      { name: '实时数据推送方案.pptx', size: '3.4 MB', uploader: '陈七', uploadedAt: '2024-01-20 09:45', type: 'ppt' }
    ],
    activities: [
      { type: 'progress', author: '李四', role: '项目负责人', content: '将项目整体进度更新至 60%', time: '2024-03-10 14:20' },
      { type: 'update', author: '王五', role: '前端开发工程师', content: '完成画布拖拽布局能力，支持组件自由缩放与吸附对齐', time: '2024-03-05 16:10' },
      { type: 'update', author: '李四', role: '项目负责人', content: '图表组件库开发完成，共沉淀 52 个可复用组件', time: '2024-02-01 11:00' },
      { type: 'file', author: '陈七', role: '后端开发工程师', content: '上传了文件：实时数据推送方案.pptx', time: '2024-01-20 09:45' },
      { type: 'milestone', author: '李四', role: '项目负责人', content: '里程碑「图表组件库开发」完成', time: '2024-01-31 18:00' },
      { type: 'update', author: '李四', role: '项目负责人', content: '项目启动，完成技术选型与框架搭建', time: '2023-11-01 09:00' }
    ]
  },
  {
    id: 'project_3',
    name: '移动端APP',
    description: '开发跨平台移动应用，提供便捷的用户服务和数据管理功能。',
    leader: '王五',
    team: ['王五', '赵六', '孙八'],
    tags: ['移动开发', 'React Native', 'UI/UX'],
    progress: 90,
    status: 'completed',
    startDate: '2023-08-20',
    endDate: '2024-01-10',
    category: 'mobile',

    title: '移动端APP',
    subtitle: '跨平台移动应用，基于 React Native 实现一套代码多端运行，提供便捷的用户服务与数据管理能力。',
    categoryName: '移动应用',
    statusText: '已完成',
    publisher: 'EAVI 团队数据管理委员会',
    publishedAt: '2023-08-15',
    leaderId: 3,
    leaderUsername: 'wangwu',
    leaderRole: '项目负责人',
    techStack: ['React Native', 'TypeScript', 'Node.js', 'MongoDB', 'Firebase'],
    members: [
      { id: 3, username: 'wangwu', name: '王五', role: '项目负责人' },
      { id: 4, username: 'zhaoliu', name: '赵六', role: '移动开发工程师' },
      { id: 8, username: 'sunba', name: '孙八', role: 'UI/UX 设计师' }
    ],
    description: {
      overview: '移动端 APP 是一套面向高校师生的综合服务平台，涵盖通知公告、课程管理、数据填报等核心功能，基于 React Native 实现一套代码同时运行于 iOS 与 Android 平台，显著降低开发与维护成本。',
      features: [
        '一套代码多端运行（iOS / Android）',
        '消息推送与离线缓存能力',
        '表单动态渲染与数据填报',
        '深色模式与主题换肤',
        '崩溃上报与性能监控'
      ],
      architecture: '客户端基于 React Native + TypeScript 构建，使用 Redux 管理应用状态；服务端基于 Node.js + MongoDB 提供 REST API；文件与图片存储在对象存储，通过 Firebase Cloud Messaging 实现消息推送。',
      scenarios: '适用于校园服务、企业内部办公、行业数据采集等移动化场景，支持快速迭代发布，兼顾研发效率与用户体验。'
    },
    milestones: [
      { title: '需求梳理与原型设计', date: '2023-08-20 ~ 2023-09-20', status: 'completed' },
      { title: '基础框架与核心模块', date: '2023-09-21 ~ 2023-11-20', status: 'completed' },
      { title: '联调测试与优化', date: '2023-11-21 ~ 2023-12-20', status: 'completed' },
      { title: '发布上线', date: '2023-12-21 ~ 2024-01-10', status: 'completed' }
    ],
    files: [
      { name: 'APP 产品需求文档.pdf', size: '2.1 MB', uploader: '王五', uploadedAt: '2023-08-25 10:30', type: 'pdf' },
      { name: 'UI 设计规范.pptx', size: '6.8 MB', uploader: '孙八', uploadedAt: '2023-09-15 14:00', type: 'ppt' },
      { name: '移动端技术架构说明.docx', size: '1.5 MB', uploader: '赵六', uploadedAt: '2023-11-02 16:20', type: 'doc' },
      { name: '发布上线记录.md', size: '256 KB', uploader: '王五', uploadedAt: '2024-01-10 09:00', type: 'md' }
    ],
    activities: [
      { type: 'progress', author: '王五', role: '项目负责人', content: '项目进度更新至 100%，正式发布上线', time: '2024-01-10 09:00' },
      { type: 'file', author: '王五', role: '项目负责人', content: '上传了文件：发布上线记录.md', time: '2024-01-10 09:00' },
      { type: 'milestone', author: '王五', role: '项目负责人', content: '里程碑「发布上线」完成', time: '2024-01-10 08:30' },
      { type: 'update', author: '赵六', role: '移动开发工程师', content: '完成性能优化，启动时间由 2.8s 降至 1.4s', time: '2023-12-15 17:40' },
      { type: 'milestone', author: '王五', role: '项目负责人', content: '里程碑「联调测试与优化」完成', time: '2023-12-20 18:00' },
      { type: 'update', author: '王五', role: '项目负责人', content: '项目启动，完成需求梳理与原型设计', time: '2023-08-20 09:00' }
    ]
  },
  {
    id: 'project_4',
    name: '区块链溯源系统',
    description: '利用区块链技术构建产品溯源系统，确保数据透明性和不可篡改性。',
    leader: '赵六',
    team: ['赵六', '孙八', '周九', '吴十'],
    tags: ['区块链', '溯源', '智能合约'],
    progress: 25,
    status: 'planning',
    startDate: '2024-01-05',
    endDate: '2024-06-30',
    category: 'web',

    title: '区块链溯源系统',
    subtitle: '基于区块链技术构建的产品全流程溯源系统，确保溯源数据透明、可信、不可篡改。',
    categoryName: 'Web开发',
    statusText: '规划中',
    publisher: 'EAVI 团队数据管理委员会',
    publishedAt: '2024-01-03',
    leaderId: 4,
    leaderUsername: 'zhaoliu',
    leaderRole: '项目负责人',
    techStack: ['Ethereum', 'Solidity', 'Web3.js', 'IPFS', 'Vue.js'],
    members: [
      { id: 4, username: 'zhaoliu', name: '赵六', role: '项目负责人' },
      { id: 8, username: 'sunba', name: '孙八', role: '区块链开发工程师' },
      { id: 9, username: 'zhoujiu', name: '周九', role: '后端开发工程师' },
      { id: 10, username: 'wushi', name: '吴十', role: '测试工程师' }
    ],
    description: {
      overview: '区块链溯源系统旨在利用区块链不可篡改、全程可追溯的特性，为农产品、工业品等提供从生产、加工、仓储到流通的全生命周期溯源服务，消费者扫码即可查验商品来源信息。',
      features: [
        '链上数据上链与存证',
        '全流程环节记录与节点溯源',
        '基于 IPFS 的溯源文件存储',
        '扫码查验与防伪验证',
        '多角色权限管理（生产商 / 经销商 / 监管方）'
      ],
      architecture: '底层基于 Ethereum 与 Solidity 编写溯源智能合约，Web3.js 负责链上交互；溯源元数据与文件通过 IPFS 分布式存储；前端基于 Vue.js 构建管理后台与查询门户，合约测试使用 Hardhat 框架。',
      scenarios: '适用于农产品防伪溯源、食品药品监管、品牌商品防窜货、供应链金融风控等场景，为政府监管与企业品牌保护提供可信数据支撑。'
    },
    milestones: [
      { title: '可行性分析与方案设计', date: '2024-01-05 ~ 2024-02-29', status: 'current' },
      { title: '智能合约开发', date: '2024-03-01 ~ 2024-04-30', status: 'pending' },
      { title: '溯源平台开发', date: '2024-05-01 ~ 2024-05-31', status: 'pending' },
      { title: '测试与试运行', date: '2024-06-01 ~ 2024-06-30', status: 'pending' }
    ],
    files: [
      { name: '区块链溯源可行性报告.pdf', size: '2.8 MB', uploader: '赵六', uploadedAt: '2024-01-10 15:00', type: 'pdf' },
      { name: '智能合约技术选型文档.docx', size: '1.2 MB', uploader: '孙八', uploadedAt: '2024-01-18 10:20', type: 'doc' }
    ],
    activities: [
      { type: 'update', author: '赵六', role: '项目负责人', content: '完成技术选型论证，确定以太坊 + IPFS 技术路线', time: '2024-02-01 15:30' },
      { type: 'file', author: '孙八', role: '区块链开发工程师', content: '上传了文件：智能合约技术选型文档.docx', time: '2024-01-18 10:20' },
      { type: 'file', author: '赵六', role: '项目负责人', content: '上传了文件：区块链溯源可行性报告.pdf', time: '2024-01-10 15:00' },
      { type: 'update', author: '赵六', role: '项目负责人', content: '项目立项完成，进入可行性分析与方案设计阶段', time: '2024-01-05 09:00' }
    ]
  },
  {
    id: 'project_5',
    name: '智能文本分析',
    description: '开发智能文本分析和处理系统，支持多语言文档的自动分类和摘要生成。',
    leader: '陈七',
    team: ['陈七', '林十一', '黄十二'],
    tags: ['NLP', '文本分析', '深度学习'],
    progress: 45,
    status: 'active',
    startDate: '2024-01-20',
    endDate: '2024-05-30',
    category: 'ai',

    title: '智能文本分析',
    subtitle: '面向多语言文档的智能文本分析系统，支持自动分类、摘要生成与关键词抽取。',
    categoryName: 'AI项目',
    statusText: '进行中',
    publisher: 'EAVI 团队数据管理委员会',
    publishedAt: '2024-01-18',
    leaderId: 7,
    leaderUsername: 'chenqi',
    leaderRole: '项目负责人',
    techStack: ['Python', 'Transformers', 'BERT', 'FastAPI', 'Milvus'],
    members: [
      { id: 7, username: 'chenqi', name: '陈七', role: '项目负责人' },
      { id: 11, username: 'lin11', name: '林十一', role: 'NLP 算法工程师' },
      { id: 12, username: 'huang12', name: '黄十二', role: '后端开发工程师' }
    ],
    description: {
      overview: '智能文本分析系统面向科研与政务场景，提供多语言文档的自动分类、摘要生成、关键词抽取与语义检索能力，帮助用户从海量非结构化文本中高效获取结构化知识。',
      features: [
        '多语言文本自动分类',
        '基于生成式模型的文档摘要',
        'TF-IDF 与 TextRank 关键词抽取',
        '向量化语义检索',
        '批量文档处理与任务调度'
      ],
      architecture: '基于预训练模型（BERT 系列）微调实现文本分类与摘要，特征向量存储使用 Milvus 向量数据库；服务端基于 FastAPI 提供异步处理接口，支持批量任务队列与进度查询。',
      scenarios: '适用于文献资料智能归档、政策文件自动分类、新闻聚合摘要、舆情分析等场景，显著降低人工处理成本。'
    },
    milestones: [
      { title: '语料构建与模型选型', date: '2024-01-20 ~ 2024-02-29', status: 'completed' },
      { title: '分类与摘要模型训练', date: '2024-03-01 ~ 2024-04-15', status: 'current' },
      { title: '检索服务与平台开发', date: '2024-04-16 ~ 2024-05-15', status: 'pending' },
      { title: '上线交付', date: '2024-05-16 ~ 2024-05-30', status: 'pending' }
    ],
    files: [
      { name: '语料标注规范.pdf', size: '1.6 MB', uploader: '陈七', uploadedAt: '2024-01-25 11:00', type: 'pdf' },
      { name: '模型选型对比实验.docx', size: '2.9 MB', uploader: '林十一', uploadedAt: '2024-02-20 14:30', type: 'doc' }
    ],
    activities: [
      { type: 'progress', author: '陈七', role: '项目负责人', content: '将项目整体进度更新至 45%', time: '2024-03-18 09:40' },
      { type: 'update', author: '林十一', role: 'NLP 算法工程师', content: '文本分类模型在验证集上 F1 达到 0.91，满足上线要求', time: '2024-03-12 17:20' },
      { type: 'file', author: '陈七', role: '项目负责人', content: '上传了文件：模型选型对比实验.docx', time: '2024-02-20 14:30' },
      { type: 'milestone', author: '陈七', role: '项目负责人', content: '里程碑「语料构建与模型选型」完成', time: '2024-02-29 18:00' },
      { type: 'update', author: '陈七', role: '项目负责人', content: '项目启动，开始语料构建与模型选型', time: '2024-01-20 09:00' }
    ]
  },
  {
    id: 'project_6',
    name: '云计算平台',
    description: '构建企业级云计算平台，提供弹性计算资源和数据存储服务。',
    leader: '刘八',
    team: ['刘八', '杨十三', '徐十四', '朱十五'],
    tags: ['云计算', '分布式系统', '微服务'],
    progress: 30,
    status: 'active',
    startDate: '2024-02-15',
    endDate: '2024-08-30',
    category: 'web',

    title: '云计算平台',
    subtitle: '构建企业级云计算平台，提供弹性计算资源、对象存储与容器编排服务。',
    categoryName: 'Web开发',
    statusText: '进行中',
    publisher: 'EAVI 团队数据管理委员会',
    publishedAt: '2024-02-10',
    leaderId: 13,
    leaderUsername: 'liuba',
    leaderRole: '项目负责人',
    techStack: ['Kubernetes', 'Docker', 'Go', 'etcd', 'Prometheus', '微服务'],
    members: [
      { id: 13, username: 'liuba', name: '刘八', role: '项目负责人' },
      { id: 14, username: 'yang13', name: '杨十三', role: '云平台开发工程师' },
      { id: 15, username: 'xu14', name: '徐十四', role: '基础设施工程师' },
      { id: 16, username: 'zhu15', name: '朱十五', role: '运维工程师' }
    ],
    description: {
      overview: '企业级云计算平台面向内部研发团队提供 IaaS/PaaS 层基础能力，包括弹性计算实例管理、容器编排、对象存储、监控告警与资源配额管理，目标是将基础设施资源交付效率提升 80%。',
      features: [
        '弹性计算实例全生命周期管理',
        '基于 Kubernetes 的容器编排',
        '对象存储与生命周期管理',
        '资源配额与成本核算',
        'Prometheus 监控与告警'
      ],
      architecture: '控制面基于 Go + etcd 实现资源状态管理与调度，数据面基于 Kubernetes 与 Docker 提供容器运行环境，监控体系采用 Prometheus + Grafana，全部组件以微服务方式部署并支持高可用。',
      scenarios: '适用于企业私有云建设、研发测试环境管理、容器化应用托管、弹性扩缩容等场景，为上层业务提供稳定可靠的基础设施底座。'
    },
    milestones: [
      { title: '平台架构设计', date: '2024-02-15 ~ 2024-03-31', status: 'current' },
      { title: '计算资源模块开发', date: '2024-04-01 ~ 2024-05-31', status: 'pending' },
      { title: '存储与监控模块开发', date: '2024-06-01 ~ 2024-07-15', status: 'pending' },
      { title: '内部试用与交付', date: '2024-07-16 ~ 2024-08-30', status: 'pending' }
    ],
    files: [
      { name: '云平台总体架构设计.pdf', size: '5.2 MB', uploader: '刘八', uploadedAt: '2024-02-20 10:00', type: 'pdf' },
      { name: 'K8s 集群部署手册.docx', size: '1.9 MB', uploader: '徐十四', uploadedAt: '2024-03-01 15:45', type: 'doc' }
    ],
    activities: [
      { type: 'update', author: '刘八', role: '项目负责人', content: '完成平台总体架构设计评审，确定微服务 + K8s 技术路线', time: '2024-03-05 14:00' },
      { type: 'file', author: '徐十四', role: '基础设施工程师', content: '上传了文件：K8s 集群部署手册.docx', time: '2024-03-01 15:45' },
      { type: 'file', author: '刘八', role: '项目负责人', content: '上传了文件：云平台总体架构设计.pdf', time: '2024-02-20 10:00' },
      { type: 'update', author: '刘八', role: '项目负责人', content: '项目启动，进入平台架构设计阶段', time: '2024-02-15 09:00' }
    ]
  }
]

// 根据 id 获取项目
export function getProjectById(id) {
  const found = projects.find(p => p.id === id)
  if (found) return found
  // 用户自定义项目（localStorage 持久化）
  try {
    const custom = JSON.parse(localStorage.getItem(CUSTOM_PROJECTS_KEY) || '[]')
    const c = custom.find(p => p.id === id)
    if (c) return c
  } catch (e) {
    /* 忽略存储异常 */
  }
  return null
}

// 自定义项目本地存储键
export const CUSTOM_PROJECTS_KEY = 'custom_projects'

// 获取全部项目（内置 + 用户自定义）
export function getAllProjects() {
  try {
    const custom = JSON.parse(localStorage.getItem(CUSTOM_PROJECTS_KEY) || '[]')
    return [...projects, ...custom]
  } catch (e) {
    return [...projects]
  }
}

// 生成唯一项目 id
export function generateProjectId() {
  return 'project_' + Date.now().toString(36) + Math.random().toString(36).slice(2, 6)
}

// 创建自定义项目：保存到本地列表 + 项目数据缓存（详情页可读取），返回新项目
export function createProject(data) {
  const now = new Date()
  const timeStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
  const project = {
    id: data.id || generateProjectId(),
    name: data.name,
    title: data.name,
    subtitle: data.subtitle || '',
    category: data.category || 'ai',
    categoryName: categoryMap[data.category] || 'AI项目',
    status: data.status || 'planning',
    statusText: statusMap[data.status] || '规划中',
    description: data.description || '',
    publisher: data.publisher || 'EAVI 团队数据管理委员会',
    publishedAt: data.publishedAt || timeStr.slice(0, 10),
    startDate: data.startDate || '',
    endDate: data.endDate || '',
    leader: data.leader,
    leaderId: data.leaderId || '',
    leaderUsername: data.leaderUsername || '',
    leaderRole: '项目负责人',
    team: data.team || [],
    members: data.members || [],
    tags: data.tags || [],
    techStack: data.techStack || [],
    progress: 0,
    milestones: data.milestones || [],
    files: data.files || [],
    activities: data.activities || [
      {
        type: 'update',
        author: data.leader || '未知用户',
        role: '项目负责人',
        content: `创建项目「${data.name}」，项目正式立项`,
        time: timeStr
      }
    ]
  }
  // 持久化：本地自定义项目列表 + 详情页数据缓存
  try {
    const list = JSON.parse(localStorage.getItem(CUSTOM_PROJECTS_KEY) || '[]')
    list.unshift(project)
    localStorage.setItem(CUSTOM_PROJECTS_KEY, JSON.stringify(list))
  } catch (e) {
    /* 忽略存储异常 */
  }
  try {
    localStorage.setItem('project_data_' + project.id, JSON.stringify(project))
  } catch (e) {
    /* 忽略存储异常 */
  }
  return project
}

// 类别映射
export const categoryMap = {
  ai: 'AI项目',
  web: 'Web开发',
  mobile: '移动应用',
  data: '数据分析'
}

// 状态映射
export const statusMap = {
  active: '进行中',
  planning: '规划中',
  completed: '已完成',
  paused: '已暂停'
}
