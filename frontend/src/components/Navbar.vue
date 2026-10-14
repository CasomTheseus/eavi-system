<template>
  <nav class="navbar">
    <div class="nav-container">
      <router-link to="/" class="logo">
        <div class="logo-icon">🤖</div>
        <span>EAVI团队数据管理系统</span>
      </router-link>
      
      <div class="nav-menu">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/datasets" class="nav-link">数据集</router-link>
        <router-link to="/literature" class="nav-link">文献</router-link>
        <router-link to="/projects" class="nav-link">项目</router-link>
        <router-link to="/resources" class="nav-link">资源</router-link>
      </div>
      
      <div class="user-menu">
        <button v-if="showBackButton" class="btn btn-secondary" @click="goBack">
          <span>←</span>
          <span>返回</span>
        </button>
        <button @click="toggleTheme" class="theme-toggle" :title="isDark ? '切换到亮色主题' : '切换到暗色主题'">
          <span v-if="isDark">☀️</span>
          <span v-else">🌙</span>
        </button>
        
        <!-- 用户信息下拉菜单 -->
        <div class="user-dropdown" @click="toggleUserMenu" v-click-outside="closeUserMenu">
          <div class="user-avatar" :title="user?.name || '用户'">
            {{ getUserInitial() }}
          </div>
          <div v-if="showUserMenu" class="dropdown-menu">
            <div class="user-info">
              <div class="user-name">{{ user?.name || '用户' }}</div>
              <div class="user-email">{{ user?.email || '' }}</div>
            </div>
            <hr class="dropdown-divider">
            <button class="dropdown-item" @click="handleLogout">
              <span>🚪</span>
              <span>退出登录</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
import { useAuth } from '../composables/useAuth.js'

export default {
  name: 'Navbar',
  setup() {
    const { user, logout } = useAuth()
    return { user, logout }
  },
  props: {
    showBackButton: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      isDark: false,
      showUserMenu: false
    }
  },
  mounted() {
    // 应用保存的主题
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme) {
      this.isDark = savedTheme === 'dark';
      document.documentElement.setAttribute('data-theme', savedTheme);
    }
  },
  methods: {
    goBack() {
      this.$router.go(-1)
    },
    toggleTheme() {
      this.isDark = !this.isDark;
      const theme = this.isDark ? 'dark' : 'light';
      document.documentElement.setAttribute('data-theme', theme);
      localStorage.setItem('theme', theme);
    },
    toggleUserMenu() {
      this.showUserMenu = !this.showUserMenu
    },
    closeUserMenu() {
      this.showUserMenu = false
    },
    getUserInitial() {
      if (this.user?.name) {
        return this.user.name.charAt(0).toUpperCase()
      }
      return '用'
    },
    handleLogout() {
      if (confirm('确定要退出登录吗？')) {
        this.logout()
        this.showUserMenu = false
      }
    }
  },
  directives: {
    'click-outside': {
      mounted(el, binding) {
        el.clickOutsideEvent = function(event) {
          if (!(el === event.target || el.contains(event.target))) {
            binding.value()
          }
        }
        document.addEventListener('click', el.clickOutsideEvent)
      },
      unmounted(el) {
        document.removeEventListener('click', el.clickOutsideEvent)
      }
    }
  }
}
</script>

<style scoped>
/* 顶部导航栏 */
.navbar {
  background: rgba(255, 255, 255, 0.95);
  border-bottom: 1px solid rgba(226, 232, 240, 0.5);
  padding: 0 2rem;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 0 0 20px 20px;
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  text-decoration: none;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
  box-shadow: 
    0 8px 32px rgba(59, 130, 246, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.nav-menu {
  display: flex;
  gap: 2rem;
}

.nav-link {
  text-decoration: none;
  color: #64748b;
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.nav-link:hover, .nav-link.router-link-active {
  color: #667eea;
  background: #f1f5f9;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.theme-toggle {
  background: none;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 0.5rem;
  cursor: pointer;
  font-size: 1.2rem;
  transition: all 0.2s ease;
  color: #64748b;
}

.theme-toggle:hover {
  background: #f1f5f9;
}

.user-dropdown {
  position: relative;
  cursor: pointer;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  transition: all 0.2s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(226, 232, 240, 0.5);
  min-width: 200px;
  z-index: 1000;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.user-info {
  padding: 1rem;
  border-bottom: 1px solid #f1f5f9;
}

.user-name {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.25rem;
}

.user-email {
  font-size: 0.875rem;
  color: #64748b;
}

.dropdown-divider {
  margin: 0;
  border: none;
  border-top: 1px solid #f1f5f9;
}

.dropdown-item {
  width: 100%;
  padding: 0.75rem 1rem;
  border: none;
  background: none;
  text-align: left;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #64748b;
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.dropdown-item:hover {
  background: #f8fafc;
  color: #1e293b;
}

/* 移动端特定样式 */
@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }
  
  .navbar {
    padding: 0 1rem;
  }
  
  .nav-container {
    height: 60px;
  }
  
  .logo {
    font-size: 1.3rem;
  }
  
  .logo-icon {
    width: 36px;
    height: 36px;
  }
}

/* 暗色主题支持 */
[data-theme="dark"] .navbar {
  background: rgba(30, 41, 59, 0.95);
  border-bottom: 1px solid rgba(71, 85, 105, 0.5);
}

[data-theme="dark"] .logo {
  color: #f1f5f9;
}

[data-theme="dark"] .nav-link {
  color: #94a3b8;
}

[data-theme="dark"] .nav-link:hover,
[data-theme="dark"] .nav-link.router-link-active {
  color: #60a5fa;
  background: rgba(59, 130, 246, 0.1);
}

[data-theme="dark"] .theme-toggle {
  border-color: #475569;
  color: #94a3b8;
}

[data-theme="dark"] .theme-toggle:hover {
  background: rgba(71, 85, 105, 0.3);
}

/* 暗色主题下拉菜单样式 */
[data-theme="dark"] .dropdown-menu {
  background: rgba(30, 41, 59, 0.95);
  border-color: rgba(71, 85, 105, 0.5);
}

[data-theme="dark"] .user-info {
  border-bottom-color: rgba(71, 85, 105, 0.5);
}

[data-theme="dark"] .user-name {
  color: #f1f5f9;
}

[data-theme="dark"] .user-email {
  color: #94a3b8;
}

[data-theme="dark"] .dropdown-divider {
  border-top-color: rgba(71, 85, 105, 0.5);
}

[data-theme="dark"] .dropdown-item {
  color: #94a3b8;
}

[data-theme="dark"] .dropdown-item:hover {
  background: rgba(71, 85, 105, 0.3);
  color: #f1f5f9;
}
</style>