<template>
  <div class="rich-text-editor">
    <!-- 工具栏 -->
    <div class="toolbar">
      <!-- 加粗按钮 -->
      <button 
        class="toolbar-btn"
        :class="{ active: isBold }"
        @click="toggleBold"
        title="加粗"
      >
        <svg width="16" height="16" viewBox="0 0 16 16">
          <path d="M4 2h4.5c1.38 0 2.5 1.12 2.5 2.5 0 .74-.4 1.39-1 1.75.6.36 1 1.01 1 1.75 0 1.38-1.12 2.5-2.5 2.5H4V2zm2 2v2h2.5c.28 0 .5-.22.5-.5s-.22-.5-.5-.5H6zm0 4v2h3c.28 0 .5-.22.5-.5s-.22-.5-.5-.5H6z"/>
        </svg>
      </button>

      <!-- 颜色选择器 -->
      <div class="color-picker-container">
        <button 
          class="toolbar-btn color-btn"
          @click="toggleColorPicker"
          title="文字颜色"
        >
          <svg width="16" height="16" viewBox="0 0 16 16">
            <path d="M8 1l3 7h-2l-.5-1.5h-3L5 8H3l3-7zm0 2.5L7.25 6h1.5L8 3.5z"/>
            <rect x="2" y="12" width="12" height="2" :fill="currentColor"/>
          </svg>
        </button>
        
        <!-- 颜色面板 -->
        <div v-if="showColorPicker" class="color-panel">
          <div class="color-grid">
            <div 
              v-for="color in colors" 
              :key="color"
              class="color-item"
              :style="{ backgroundColor: color }"
              :class="{ active: currentColor === color }"
              @click="selectColor(color)"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑区域 -->
    <div 
      ref="editor"
      class="editor-content"
      contenteditable="true"
      @input="handleInput"
      @keydown="handleKeydown"
      @mouseup="updateToolbarState"
      @keyup="updateToolbarState"
      placeholder="请输入内容..."
    ></div>
  </div>
</template>

<script>
export default {
  name: 'RichTextEditor',
  props: {
    modelValue: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue'],
  data() {
    return {
      isBold: false,
      currentColor: '#000000',
      showColorPicker: false,
      savedRange: null, // 保存光标位置
      colors: [
        '#000000', '#333333', '#666666', '#999999',
        '#FF0000', '#FF6600', '#FFCC00', '#00CC00',
        '#0066CC', '#6600CC', '#CC0066', '#FF3366',
        '#FF9900', '#CCFF00', '#00FFCC', '#0099FF',
        '#9900FF', '#FF0099', '#FF6666', '#FFCC66'
      ]
    }
  },
  mounted() {
    // 初始化内容
    if (this.modelValue) {
      this.$refs.editor.innerHTML = this.modelValue
    }
    
    // 点击外部关闭颜色选择器
    document.addEventListener('click', this.handleClickOutside)
    
    // 监听选择变化
    document.addEventListener('selectionchange', this.updateToolbarState)
    
    // 确保编辑器本身没有颜色样式
    this.$refs.editor.style.color = ''
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleClickOutside)
  },
  methods: {
    // 处理输入
    handleInput() {
      const content = this.$refs.editor.innerHTML
      this.$emit('update:modelValue', content)
    },

    // 处理键盘事件
    handleKeydown(event) {
      // 关闭颜色选择器
      if (this.showColorPicker) {
        this.showColorPicker = false
      }

      // 让浏览器自然处理键盘输入，execCommand会自动应用当前设置的颜色
    },

    // 切换加粗
    toggleBold() {
      document.execCommand('bold', false, null)
      this.updateToolbarState()
      this.$refs.editor.focus()
    },

    // 切换颜色选择器
    toggleColorPicker() {
      // 如果要显示颜色选择器，先保存当前光标位置
      if (!this.showColorPicker) {
        this.saveSelection()
      }
      this.showColorPicker = !this.showColorPicker
    },

    // 选择颜色
    selectColor(color) {
      this.currentColor = color
      this.showColorPicker = false
      
      // 恢复之前保存的光标位置
      this.restoreSelection()
      this.$refs.editor.focus()
      
      document.execCommand('foreColor', false, color)
      this.updateToolbarState()
    },



    // 更新工具栏状态
    updateToolbarState() {
      this.isBold = document.queryCommandState('bold')
      
      // 尝试获取当前颜色，如果失败则使用默认黑色
      try {
        const color = document.queryCommandValue('foreColor')
        if (color && color !== 'rgb(0, 0, 0)' && color !== '#000000') {
          this.currentColor = this.rgbToHex(color) || '#000000'
        } else {
          this.currentColor = '#000000'
        }
      } catch (e) {
        this.currentColor = '#000000'
      }
    },

    // RGB转HEX
    rgbToHex(rgb) {
      if (!rgb || rgb === 'rgb(0, 0, 0)') return '#000000'
      
      const result = rgb.match(/\d+/g)
      if (result && result.length >= 3) {
        const r = parseInt(result[0])
        const g = parseInt(result[1])
        const b = parseInt(result[2])
        return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1)
      }
      return '#000000'
    },

    // 点击外部关闭颜色选择器
    handleClickOutside(event) {
      const colorContainer = this.$el.querySelector('.color-picker-container')
      if (colorContainer && !colorContainer.contains(event.target)) {
        this.showColorPicker = false
      }
    },

    // 保存当前光标位置
    saveSelection() {
      const selection = window.getSelection()
      if (selection.rangeCount > 0) {
        this.savedRange = selection.getRangeAt(0).cloneRange()
      }
    },

    // 恢复光标位置
    restoreSelection() {
      if (this.savedRange) {
        const selection = window.getSelection()
        selection.removeAllRanges()
        selection.addRange(this.savedRange)
      }
    }
  }
}
</script>

<style scoped>
.rich-text-editor {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background: #fff;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 工具栏样式 */
.toolbar {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #e0e0e0;
  background: #fafafa;
  gap: 8px;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid #d0d0d0;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toolbar-btn:hover {
  background: #f0f0f0;
  border-color: #b0b0b0;
}

.toolbar-btn.active {
  background: #e3f2fd;
  border-color: #2196f3;
  color: #2196f3;
}

.toolbar-btn svg {
  fill: currentColor;
}

/* 颜色选择器 */
.color-picker-container {
  position: relative;
}

.color-btn svg rect {
  transition: fill 0.2s ease;
}

.color-panel {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 4px;
  padding: 8px;
  background: #fff;
  border: 1px solid #d0d0d0;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1000;
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
  width: 120px;
}

.color-item {
  width: 24px;
  height: 24px;
  border-radius: 3px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
}

.color-item:hover {
  transform: scale(1.1);
  border-color: #999;
}

.color-item.active {
  border-color: #2196f3;
  transform: scale(1.1);
}

/* 编辑区域样式 */
.editor-content {
  min-height: 200px;
  padding: 16px;
  outline: none;
  line-height: 1.6;
  word-wrap: break-word;
  overflow-wrap: break-word;
  word-break: break-word;
}

.editor-content:empty::before {
  content: attr(placeholder);
  color: #999;
  pointer-events: none;
}

/* 编辑器内容样式 */
.editor-content strong,
.editor-content b {
  font-weight: bold;
}

.editor-content p {
  margin: 0 0 8px 0;
}

.editor-content p:last-child {
  margin-bottom: 0;
}

/* 选中文本样式 */
.editor-content::selection {
  background: #b3d4fc;
}
</style>