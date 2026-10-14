<template>
  <div class="login-page">
    <div class="floating-elements">
      <div class="floating-book">📖</div>
      <div class="floating-book">📚</div>
      <div class="floating-book">📓</div>
      <div class="floating-book">📔</div>
    </div>
    
    <div class="book-container">
      <div class="book-spine"></div>
      
      <!-- 左页 -->
      <div class="book-page left-page">
        <div class="page-content">
          <div class="welcome-section">
            <div class="logo">E</div>
            <h1 class="system-title">EAVI团队</h1>
            <h2 class="system-subtitle">数据管理系统</h2>
            <p class="welcome-text">西安电子科技大学 · 智能数据管理平台</p>
          </div>
        </div>
      </div>
      
      <!-- 右页 -->
      <div class="book-page right-page">
        <div class="page-content">
          <div class="login-section">
            <h3 class="login-title">登录账户</h3>
            <p class="login-subtitle">请输入您的登录信息</p>
            
            <form class="login-form" @submit.prevent="handleLogin">
              <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
              <div v-if="successMessage" class="success-message">{{ successMessage }}</div>
              
              <div class="form-group">
                <label for="username" class="form-label">用户名或邮箱</label>
                <input 
                  type="text" 
                  id="username" 
                  v-model="loginForm.username"
                  class="form-input" 
                  placeholder="请输入用户名或邮箱"
                  required
                  @focus="handleInputFocus"
                  @blur="handleInputBlur"
                >
              </div>
              
              <div class="form-group">
                <label for="password" class="form-label">密码</label>
                <input 
                  type="password" 
                  id="password" 
                  v-model="loginForm.password"
                  class="form-input" 
                  placeholder="请输入密码"
                  required
                  @focus="handleInputFocus"
                  @blur="handleInputBlur"
                >
              </div>
              
              <button type="submit" class="login-btn" :disabled="isLoading">
                <span v-if="!isLoading">登录</span>
                <span v-else class="loading"></span>
              </button>
              
              <div class="forgot-password">
                <a href="#" class="forgot-link" @click.prevent="handleForgotPassword">忘记密码？</a>
              </div>
              
              <div class="divider">
                <span>或者</span>
              </div>
              
              <div class="social-login">
                <button type="button" class="social-btn" title="微信登录" @click="handleSocialLogin('微信登录')">
                  💬
                </button>
                <button type="button" class="social-btn" title="QQ登录" @click="handleSocialLogin('QQ登录')">
                  🐧
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuth } from '../composables/useAuth.js'

export default {
  name: 'Login',
  setup() {
    const { login, isAuthenticated } = useAuth()
    return { login, isAuthenticated }
  },
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      isLoading: false,
      errorMessage: '',
      successMessage: ''
    }
  },
  mounted() {
    // 如果已经登录，直接跳转到首页
    if (this.isAuthenticated) {
      this.$router.push('/home')
    }
    
    // 键盘快捷键支持
    document.addEventListener('keydown', this.handleKeydown)
  },
  beforeUnmount() {
    document.removeEventListener('keydown', this.handleKeydown)
  },
  methods: {
    async handleLogin() {
      this.errorMessage = ''
      this.successMessage = ''
      this.isLoading = true
      
      try {
        // 验证输入
        if (!this.loginForm.username || !this.loginForm.password) {
          this.errorMessage = '请填写完整的登录信息'
          this.isLoading = false
          return
        }
        
        // 调用认证系统的登录方法
        const result = await this.login(this.loginForm.username, this.loginForm.password)
        
        if (result.success) {
          this.successMessage = '登录成功！正在跳转...'
          
          setTimeout(() => {
            this.$router.push('/home')
          }, 1500)
        }
      } catch (error) {
        this.errorMessage = error.message || '登录失败，请重试'
        this.isLoading = false
      }
    },
    
    handleSocialLogin(type) {
      alert(`${type}功能正在开发中...`)
    },
    
    handleForgotPassword() {
      alert('忘记密码功能正在开发中，请联系管理员重置密码。')
    },
    
    handleInputFocus(event) {
      event.target.parentElement.style.transform = 'scale(1.02)'
      event.target.parentElement.style.transition = 'transform 0.2s ease'
    },
    
    handleInputBlur(event) {
      event.target.parentElement.style.transform = 'scale(1)'
    },
    
    handleKeydown(event) {
      if (event.key === 'Enter' && event.ctrlKey) {
        this.handleLogin()
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.book-container {
  display: flex;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 
    0 25px 80px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.05);
  overflow: hidden;
  width: 100%;
  max-width: 900px;
  height: 600px;
  margin: 2rem;
  position: relative;
  z-index: 10;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transform-style: preserve-3d;
  perspective: 1000px;
}

.book-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
  border-radius: 20px;
  z-index: -1;
}

.book-spine {
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 8px;
  background: linear-gradient(180deg, #1e3a8a 0%, #3b82f6 100%);
  box-shadow: 
    inset 2px 0 4px rgba(0, 0, 0, 0.2),
    2px 0 8px rgba(0, 0, 0, 0.1),
    -2px 0 8px rgba(0, 0, 0, 0.1);
  transform: translateX(-50%);
  z-index: 20;
}

.book-spine::before {
  content: '';
  position: absolute;
  top: 20px;
  left: 1px;
  right: 1px;
  height: 2px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 1px;
}

.book-spine::after {
  content: '';
  position: absolute;
  bottom: 20px;
  left: 1px;
  right: 1px;
  height: 2px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 1px;
}

.book-page {
  flex: 1;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.left-page {
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  border-radius: 20px 0 0 20px;
  border-right: 1px solid rgba(226, 232, 240, 0.5);
}

.right-page {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 0 20px 20px 0;
}

.page-content {
  padding: 3rem 2.5rem;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
}

.welcome-section {
  text-align: center;
}

.login-section {
  text-align: left;
}

.welcome-text {
  color: #64748b;
  font-size: 1rem;
  margin: 1.5rem 0 2rem;
  line-height: 1.6;
}

.login-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.login-subtitle {
  color: #64748b;
  font-size: 0.95rem;
  margin-bottom: 2rem;
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  border-radius: 16px;
  color: white;
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  position: relative;
  box-shadow: 
    0 8px 32px rgba(59, 130, 246, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transform: translateZ(10px);
}

.logo::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg, #1e3a8a, #3b82f6, #60a5fa);
  border-radius: 18px;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.logo:hover::before {
  opacity: 1;
}

.logo::after {
  content: '📚';
  position: absolute;
  top: -8px;
  right: -8px;
  font-size: 1rem;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.system-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.system-subtitle {
  color: #3b82f6;
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.login-form {
  margin-top: 1rem;
}

.form-group {
  margin-bottom: 1.5rem;
  position: relative;
}

.form-group::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(30, 58, 138, 0.05), rgba(59, 130, 246, 0.05));
  border-radius: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.form-group:focus-within::before {
  opacity: 1;
}

.form-label {
  display: block;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.form-input {
  width: 100%;
  padding: 1rem 1.25rem;
  border: 2px solid transparent;
  border-radius: 12px;
  font-size: 0.9rem;
  color: #1e293b;
  transition: all 0.3s ease;
  background: linear-gradient(white, white) padding-box,
             linear-gradient(135deg, #e2e8f0, #f1f5f9) border-box;
  position: relative;
}

.form-input:focus {
  outline: none;
  color: #1e293b;
  background: linear-gradient(white, white) padding-box,
             linear-gradient(135deg, #1e3a8a, #3b82f6) border-box;
  box-shadow: 
    0 0 0 4px rgba(59, 130, 246, 0.1),
    0 8px 25px rgba(59, 130, 246, 0.15);
  transform: translateY(-2px);
}

.form-input:hover {
  color: #1e293b;
}

.form-input:active {
  color: #1e293b;
}

.form-input::placeholder {
  color: #64748b;
  opacity: 0.8;
}

.login-btn {
  width: 100%;
  padding: 1rem 1.5rem;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 
    0 8px 32px rgba(59, 130, 246, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transform: translateZ(5px);
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.login-btn:hover {
  transform: translateY(-3px) translateZ(5px);
  box-shadow: 
    0 12px 40px rgba(59, 130, 246, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  background: linear-gradient(135deg, #1e40af 0%, #2563eb 50%, #3b82f6 100%);
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.forgot-password {
  text-align: center;
  margin-top: 1.5rem;
}

.forgot-link {
  color: #3b82f6;
  text-decoration: none;
  font-size: 0.875rem;
  transition: color 0.2s ease;
}

.forgot-link:hover {
  color: #1e40af;
  text-decoration: underline;
}

.divider {
  margin: 1.5rem 0;
  text-align: center;
  position: relative;
  color: #94a3b8;
  font-size: 0.875rem;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e2e8f0;
  z-index: 1;
}

.divider span {
  background: white;
  padding: 0 1rem;
  position: relative;
  z-index: 2;
}

.social-login {
  display: flex;
  gap: 1rem;
}

.social-btn {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  position: relative;
  overflow: hidden;
}

.social-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(59, 130, 246, 0.1), transparent);
  transition: left 0.3s;
}

.social-btn:hover {
  border-color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.social-btn:hover::before {
  left: 100%;
}

.loading {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.success-message {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #16a34a;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

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
  animation: float 15s ease-in-out infinite;
}

.floating-book:nth-child(1) {
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.floating-book:nth-child(2) {
  top: 20%;
  right: 15%;
  animation-delay: 3s;
}

.floating-book:nth-child(3) {
  bottom: 20%;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .book-container {
    flex-direction: column;
    max-width: 420px;
    height: auto;
    margin: 1rem;
  }
  
  .left-page, .right-page {
    border-radius: 0;
  }
  
  .left-page {
    border-radius: 20px 20px 0 0;
    border-right: none;
    border-bottom: 1px solid rgba(226, 232, 240, 0.5);
  }
  
  .right-page {
    border-radius: 0 0 20px 20px;
  }
  
  .book-spine {
    left: 0;
    right: 0;
    top: 50%;
    bottom: auto;
    width: auto;
    height: 8px;
    transform: translateY(-50%);
  }
  
  .page-content {
    padding: 2rem 1.5rem;
  }
  
  .system-title {
    font-size: 1.5rem;
  }
  
  .login-title {
    font-size: 1.5rem;
  }
}
</style>