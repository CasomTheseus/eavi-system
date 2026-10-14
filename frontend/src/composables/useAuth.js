import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

// 全局状态
const token = ref(localStorage.getItem('token') || null)
const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

export function useAuth() {
  const router = useRouter()

  // 计算属性：是否已登录
  const isAuthenticated = computed(() => !!token.value)

  // 登录方法
  const login = async (username, password) => {
    try {
      // 调用真实的后端API
      const response = await fetch('http://localhost:8080/auth/login', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify({ username, password })
      })
      
      const data = await response.json()
      
      if (response.ok && data.code === 200) {
        const userData = data.data.user
        const authToken = data.data.token
        
        // 保存到状态和本地存储
        token.value = authToken
        user.value = userData
        localStorage.setItem('token', authToken)
        localStorage.setItem('user', JSON.stringify(userData))
        
        return { success: true, user: userData, token: authToken }
      } else {
        throw new Error(data.message || '登录失败')
      }
    } catch (error) {
      // 如果是网络错误或后端未启动，回退到模拟登录
      if (error.name === 'TypeError' || error.message.includes('fetch')) {
        console.warn('后端服务器连接失败，使用模拟登录')
        
        // 模拟API响应
        await new Promise(resolve => setTimeout(resolve, 1000))

        // 演示账号（与 src/data/projects.js 中的成员 username 对应）
        const mockAccounts = {
          admin: { id: 1, username: 'admin', email: 'admin@eavi.xidian.edu.cn', name: '管理员', role: 'admin', department: 'EAVI团队' },
          lisi: { id: 2, username: 'lisi', email: 'lisi@eavi.xidian.edu.cn', name: '李四', role: 'member', department: 'EAVI团队' },
          wangwu: { id: 3, username: 'wangwu', email: 'wangwu@eavi.xidian.edu.cn', name: '王五', role: 'member', department: 'EAVI团队' },
          zhaoliu: { id: 4, username: 'zhaoliu', email: 'zhaoliu@eavi.xidian.edu.cn', name: '赵六', role: 'member', department: 'EAVI团队' },
          chenqi: { id: 7, username: 'chenqi', email: 'chenqi@eavi.xidian.edu.cn', name: '陈七', role: 'member', department: 'EAVI团队' }
        }

        if (mockAccounts[username] && password === username) {
          const mockUser = mockAccounts[username]
          const mockToken = 'mock-jwt-token-' + Date.now()

          // 保存到状态和本地存储
          token.value = mockToken
          user.value = mockUser
          localStorage.setItem('token', mockToken)
          localStorage.setItem('user', JSON.stringify(mockUser))

          return { success: true, user: mockUser, token: mockToken }
        } else {
          throw new Error('用户名或密码错误')
        }
      } else {
        throw new Error(error.message || '登录失败')
      }
    }
  }

  // 登出方法
  const logout = async () => {
    try {
      // 调用后端登出API
      if (token.value) {
        await fetch('http://localhost:8080/auth/logout', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token.value}`,
            'Content-Type': 'application/json'
          }
        })
      }
    } catch (error) {
      console.warn('登出API调用失败:', error.message)
    } finally {
      // 无论API调用是否成功，都清除本地状态
      token.value = null
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }
  }

  // 获取用户信息
  const fetchUserProfile = async () => {
    try {
      if (!token.value) return null
      
      const response = await fetch('http://localhost:8080/auth/profile', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token.value}`,
          'Accept': 'application/json'
        }
      })
      
      const data = await response.json()
      
      if (response.ok && data.code === 200) {
        user.value = data.data
        localStorage.setItem('user', JSON.stringify(data.data))
        return data.data
      } else {
        throw new Error(data.message || '获取用户信息失败')
      }
    } catch (error) {
      console.warn('获取用户信息失败:', error.message)
      return null
    }
  }

  // 检查token有效性
  const checkAuth = () => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    if (storedToken && storedUser) {
      token.value = storedToken
      user.value = JSON.parse(storedUser)
      return true
    } else {
      logout()
      return false
    }
  }

  // 初始化认证状态
  const initAuth = () => {
    return checkAuth()
  }

  return {
    // 状态
    token: computed(() => token.value),
    user: computed(() => user.value),
    isAuthenticated,
    
    // 方法
    login,
    logout,
    checkAuth,
    initAuth,
    fetchUserProfile
  }
}