import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import './style.css'
// 导入highlight.js样式
import 'highlight.js/styles/github.css'
// 导入认证composable
import { useAuth } from './composables/useAuth.js'

// 导入页面组件
import Home from './views/Home.vue'
import Login from './views/Login.vue'
import DatasetList from './views/DatasetList.vue'
import DatasetDetail from './views/DatasetDetail.vue'
import DatasetUpload from './views/DatasetUpload.vue'
import LiteratureDetail from './views/LiteratureDetail.vue'
import LiteratureManagement from './views/LiteratureManagement.vue'
import ProjectManagement from './views/ProjectManagement.vue'
import ProjectCreate from './views/ProjectCreate.vue'
import ProjectDetail from './views/ProjectDetail.vue'
import ResourceManagement from './views/ResourceManagement.vue'
import ResourceDetail from './views/ResourceDetail.vue'
import ResourceApplication from './views/ResourceApplication.vue'

// 路由配置
const routes = [
  { 
    path: '/', 
    redirect: (to) => {
      // 根据认证状态决定重定向目标
      const token = localStorage.getItem('token')
      return token ? '/home' : '/login'
    }
  },
  { 
    path: '/login', 
    component: Login, 
    name: 'Login',
    meta: { requiresAuth: false } // 登录页不需要认证
  },
  { 
    path: '/home', 
    component: Home, 
    name: 'Home',
    meta: { requiresAuth: true } // 需要认证
  },
  { 
    path: '/datasets', 
    component: DatasetList, 
    name: 'DatasetList',
    meta: { requiresAuth: true }
  },
  { 
    path: '/datasets/upload', 
    component: DatasetUpload, 
    name: 'DatasetUpload',
    meta: { requiresAuth: true }
  },
  { 
    path: '/datasets/:name', 
    component: DatasetDetail, 
    name: 'DatasetDetail',
    meta: { requiresAuth: true }
  },
  { 
    path: '/literature', 
    component: LiteratureManagement, 
    name: 'LiteratureManagement',
    meta: { requiresAuth: true }
  },
  { 
    path: '/literature/:id', 
    component: LiteratureDetail, 
    name: 'LiteratureDetail',
    meta: { requiresAuth: true }
  },
  { 
    path: '/projects', 
    component: ProjectManagement, 
    name: 'ProjectManagement',
    meta: { requiresAuth: true }
  },
  { 
    path: '/projects/create', 
    component: ProjectCreate, 
    name: 'ProjectCreate',
    meta: { requiresAuth: true }
  },
  { 
    path: '/projects/:id', 
    component: ProjectDetail, 
    name: 'ProjectDetail',
    meta: { requiresAuth: true }
  },
  { 
    path: '/resources', 
    component: ResourceManagement, 
    name: 'ResourceManagement',
    meta: { requiresAuth: true }
  },
  { 
    path: '/resources/:id', 
    component: ResourceDetail, 
    name: 'ResourceDetail',
    meta: { requiresAuth: true }
  },
  { 
    path: '/resource-application', 
    component: ResourceApplication, 
    name: 'ResourceApplication',
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 创建临时的认证实例来检查状态
  const token = localStorage.getItem('token')
  const isAuthenticated = !!token
  
  // 如果路由需要认证但用户未登录，重定向到登录页
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  }
  // 如果用户已登录但访问登录页，重定向到首页
  else if (to.path === '/login' && isAuthenticated) {
    next('/home')
  }
  // 其他情况正常导航
  else {
    next()
  }
})

const app = createApp(App)
app.use(router)
app.mount('#app')