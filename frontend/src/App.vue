<template>
  <div id="app">
    <!-- 除登录页面外，其他页面都显示导航栏 -->
    <Navbar v-if="!isLoginPage" />
    <router-view />
  </div>
</template>

<script>
import Navbar from './components/Navbar.vue'

export default {
  name: 'App',
  components: {
    Navbar
  },
  computed: {
    isLoginPage() {
      return this.$route.path === '/login'
    }
  }
}
</script>

<style>
#app {
  width: 100%;
  height: 100vh;
}

/* 全局CSS规则 - 强制确保markdown预览中的颜色样式能够正确显示 */
.markdown-preview span[style*="color"] {
  /* 强制保持内联颜色样式，覆盖所有其他样式 */
  color: inherit !important;
}

.markdown-preview span[style*="rgb"] {
  /* 强制确保RGB颜色能够显示 */
  color: inherit !important;
}

/* 强制显示红色 - 测试用 */
.markdown-preview span[style*="rgb(230, 0, 0)"] {
  color: rgb(230, 0, 0) !important;
  background-color: yellow !important; /* 添加背景色以便更容易看到 */
}

/* 强制显示所有颜色 - 最高优先级 */
.markdown-preview span {
  /* 确保span元素的内联样式不被任何规则覆盖 */
}

/* 尝试直接针对具体的颜色值 */
.markdown-preview span[style*="color: rgb(230, 0, 0)"] {
  color: rgb(230, 0, 0) !important;
  font-weight: bold !important;
  background-color: yellow !important;
}

/* 通用的强制颜色规则 */
.markdown-preview * {
  /* 确保所有元素都能保持其内联样式 */
}
</style>