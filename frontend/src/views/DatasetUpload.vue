<template>
  <div class="dataset-upload">
    <div class="container">
      <h1 class="page-title">数据集上传</h1>
      <div style="margin: 8px 0 16px;">
        <button @click="goBack" class="preview-btn" style="background-color: #6c757d;">返回列表</button>
      </div>
      
      <!-- 基本信息区域 -->
      <div class="section">
        <h2 class="section-title">基本信息</h2>
        <div class="mode-toggle">
          <button :class="['toggle-btn', { active: uploadMode==='editor' }]" @click="uploadMode='editor'">结构化编辑</button>
          <button :class="['toggle-btn', { active: uploadMode==='markdown' }]" @click="uploadMode='markdown'">上传Markdown文件</button>
        </div>
        <div class="form-grid">
          <div class="form-group">
            <label for="datasetName">数据集名称 *</label>
            <input 
              id="datasetName"
              v-model="formData.name" 
              type="text" 
              placeholder="请输入数据集名称"
              required
            />
          </div>
          

          
          <div class="form-group full-width">
            <label for="datasetDescription">数据集简介</label>
            <textarea 
              id="datasetDescription"
              v-model="formData.description" 
              placeholder="请简要描述数据集的用途和特点"
              rows="3"
            ></textarea>
          </div>
          
          <div class="form-group full-width">
            <label for="datasetTasks">任务类别（下拉多选）</label>
            <div class="task-multiselect" ref="taskSelector">
              <div class="selector-input" @click="openTaskDropdown">
                <div class="chips">
                  <span v-for="task in selectedTasks" :key="task.id" class="chip">{{ task.name }}</span>
                  <span v-if="selectedTasks.length===0" class="placeholder">请选择任务类别</span>
                </div>
                <span class="caret">▾</span>
              </div>
              <div v-if="taskDropdownOpen" class="dropdown" ref="taskDropdown">
                <input type="text" v-model="taskSearch" class="search-input" placeholder="搜索任务..." />
                <div class="options">
                  <label v-for="task in filteredTasks" :key="task.id" class="option">
                    <input type="checkbox" :checked="isTaskSelected(task.id)" @change="toggleTask(task.id)" />
                    <span>{{ task.name }}</span>
                  </label>
                </div>
                <div class="dropdown-actions">
                  <button type="button" class="btn btn-secondary" @click="clearTaskSelection">清空</button>
                  <button type="button" class="btn btn-primary" @click="closeTaskDropdown">完成</button>
                </div>
              </div>
            </div>
            <small class="text-muted">支持搜索与多选；可不选</small>
          </div>
        </div>
      </div>

      <!-- 数据集地址与大小 -->
      <div class="section">
        <h2 class="section-title">数据集地址与大小</h2>
        <div class="form-grid">
          <div class="form-group full-width">
            <label for="trueAddress">数据集真实地址 *</label>
            <input
              id="trueAddress"
              v-model="formData.trueAddress"
              type="text"
              placeholder="请输入数据集真实地址，如：https://example.com/datasets/voc2007.zip"
              required
            />
          </div>
          <div class="form-group">
            <label for="sizeFormatted">数据集大小（格式化）</label>
            <input
              id="sizeFormatted"
              v-model="formData.sizeFormatted"
              type="text"
              placeholder="如：1.2GB 或 850MB"
            />
          </div>
        </div>
      </div>

      <!-- 数据集详情编辑器 -->
      <div class="section" v-if="uploadMode==='editor'">
        <h2 class="section-title">数据集详情</h2>
        <div class="detail-editor">
          <!-- 固定的markdown模板标题 -->
          <div class="template-headers">
            <h3>文档结构模板</h3>
            <div class="template-info">
              <p>基于 markdownTemplate.md</p>
            </div>
            <div class="header-item">
              <h4># 数据集标题</h4>
            </div>
            <div class="header-item">
              <h4>## 1. 数据集说明</h4>
              <div class="sub-headers">
                <div class="sub-header">### 1.1 简介</div>
                <div class="sub-header">### 1.2 数据集特性</div>
              </div>
            </div>
            <div class="header-item">
              <h4>## 2. 下载方式</h4>
            </div>
            <div class="header-item">
              <h4>## 3. 文件结构</h4>
            </div>
            <div class="header-item">
              <h4>## 4. 使用示例</h4>
            </div>
            <div class="header-item">
              <h4>## 5. 参考资料</h4>
              <div class="sub-headers">
                <div class="sub-header">### 5.1 相关论文</div>
                <div class="sub-header">### 5.2 其它资料</div>
              </div>
            </div>
            <div class="header-item">
              <h4>## 6. 备注</h4>
            </div>
          </div>

          <!-- 内容编辑区域 -->
          <div class="content-editor">
            <div v-for="(section, sectionIndex) in detailSections" :key="sectionIndex" class="section-editor">
              <div class="section-header">
                <h4>{{ section.title }}</h4>
              </div>
              
              <!-- 有子章节的部分 -->
              <div v-if="section.subsections" class="subsections-container">
                <div v-for="(subsection, subIndex) in section.subsections" :key="subIndex" class="subsection-editor">
                  <div class="subsection-header">
                    <h5>{{ subsection.title }}</h5>
                  </div>
                  
                  <!-- 插块列表 -->
                  <div class="blocks-container">
                    <div v-for="(block, blockIndex) in subsection.blocks" :key="block.id" class="block-item">
                      <div class="block-header">
                        <span class="block-type">{{ blockTypes.find(t => t.type === block.type)?.icon }} {{ blockTypes.find(t => t.type === block.type)?.label }}</span>
                        <div class="block-controls">
                          <button @click="moveBlock(sectionIndex, blockIndex, 'up', subIndex)" :disabled="blockIndex === 0" class="move-btn">↑</button>
                          <button @click="moveBlock(sectionIndex, blockIndex, 'down', subIndex)" :disabled="blockIndex === subsection.blocks.length - 1" class="move-btn">↓</button>
                          <button @click="removeBlock(sectionIndex, blockIndex, subIndex)" class="remove-btn">🗑️</button>
                        </div>
                      </div>
                      
                      <!-- 不同类型的插块内容 -->
                      <div class="block-content">
                        <!-- 文本插块 -->
                        <div v-if="block.type === 'text'" class="text-block">
                          <RichTextEditor 
                            v-model="block.content"
                            :placeholder="'请输入文本内容...'"
                            @update:modelValue="updateBlockContent(sectionIndex, subIndex, blockIndex, $event)"
                          />
                        </div>
                        
                        <!-- 图片上传插块 -->
                        <div v-else-if="block.type === 'image-upload'" class="image-upload-block">
                          <div class="image-upload-area">
                            <input 
                            type="file" 
                            accept="image/*" 
                            @change="handleImageUpload($event, sectionIndex, blockIndex, subIndex)"
                            style="display: none"
                            :id="'image-' + sectionIndex + '-' + subIndex + '-' + blockIndex"
                          />
                          <label :for="'image-' + sectionIndex + '-' + subIndex + '-' + blockIndex" class="image-upload-label">
                              <div v-if="!block.url" class="upload-placeholder">
                                <i class="fas fa-camera"></i>
                                <span>点击上传图片</span>
                                <small>支持 JPG、PNG、GIF 格式，最大 5MB</small>
                              </div>
                              <div v-else class="image-preview">
                                <img :src="block.url" :alt="block.alt" class="uploaded-image" />
                                <button @click.prevent="removeImage(sectionIndex, blockIndex, subIndex)" class="remove-image-btn">
                                  <i class="fas fa-times"></i>
                                </button>
                              </div>
                            </label>

                          </div>
                          <input 
                            v-model="block.caption" 
                            placeholder="图注（如：图1. VOC2007数据集示例）" 
                            class="block-input caption-input" 
                          />
                          <textarea 
                            v-model="block.description" 
                            placeholder="图片说明（必填）"
                            rows="3"
                            class="block-textarea description-required"
                          ></textarea>
                        </div>
                        
                        <!-- 文件树插块 -->
                        <div v-else-if="block.type === 'file-tree'" class="file-tree-block">
                          <div class="tree-editor">
                            <div class="tree-header">
                              <h4 class="tree-title">
                                <i class="fas fa-folder-tree"></i>
                                文件目录结构
                              </h4>
                              <div class="tree-actions">
                                <button @click="addTreeNode(sectionIndex, blockIndex, '', subIndex)" class="tree-btn add-btn">
                                  <i class="fas fa-plus"></i> 添加文件/文件夹
                                </button>
                              </div>
                            </div>
                            
                            <div class="tree-container">
                              <div v-if="block.treeData && block.treeData.length > 0" class="tree-structure">
                                <div class="tree-content">
                                  <div v-for="(node, nodeIndex) in block.treeData" :key="nodeIndex" class="tree-node" :class="{ 'is-folder': node.type === 'folder' }">
                                    <div class="tree-item">
                                      <div class="tree-item-content">
                                        <span class="tree-connector"></span>
                                        <span class="tree-icon">
                                          <i :class="node.type === 'folder' ? 'fas fa-folder' : getFileIcon(node.name)"></i>
                                        </span>
                                        <span class="tree-name" :contenteditable="true" @blur="updateNodeName(sectionIndex, blockIndex, nodeIndex, $event, subIndex)">{{ node.name }}</span>
                                      </div>
                                      <div class="tree-item-actions">
                                        <button v-if="node.type === 'folder'" @click="addChildNode(sectionIndex, blockIndex, nodeIndex, subIndex)" class="tree-action-btn add-child-btn" title="添加子项">
                                          <i class="fas fa-plus-circle"></i>
                                        </button>
                                        <button @click="removeTreeNode(sectionIndex, blockIndex, node.name, subIndex)" class="tree-action-btn remove-btn" title="删除">
                                          <i class="fas fa-trash-alt"></i>
                                        </button>
                                      </div>
                                    </div>
                                    <!-- 递归显示子节点 -->
                                    <div v-if="node.children && node.children.length > 0" class="tree-children">
                                      <div v-for="(child, childIndex) in node.children" :key="childIndex" class="tree-node tree-child" :class="{ 'is-folder': child.type === 'folder' }">
                                        <div class="tree-item">
                                          <div class="tree-item-content">
                                            <span class="tree-connector child-connector"></span>
                                            <span class="tree-icon">
                                              <i :class="child.type === 'folder' ? 'fas fa-folder' : getFileIcon(child.name)"></i>
                                            </span>
                                            <span class="tree-name" :contenteditable="true" @blur="updateChildNodeName(sectionIndex, blockIndex, nodeIndex, childIndex, $event, subIndex)">{{ child.name }}</span>
                                      </div>
                                      <div class="tree-item-actions">
                                        <button v-if="child.type === 'folder'" @click="addChildNode(sectionIndex, blockIndex, nodeIndex, subIndex, childIndex)" class="tree-action-btn add-child-btn" title="添加子项">
                                          <i class="fas fa-plus-circle"></i>
                                        </button>
                                        <button @click="removeTreeNode(sectionIndex, blockIndex, child.name, subIndex)" class="tree-action-btn remove-btn" title="删除">
                                              <i class="fas fa-trash-alt"></i>
                                            </button>
                                          </div>
                                        </div>
                                        <!-- 递归显示子节点的子节点 -->
                                        <div v-if="child.children && child.children.length > 0" class="tree-children">
                                          <div v-for="(grandChild, grandChildIndex) in child.children" :key="grandChildIndex" class="tree-node tree-child" :class="{ 'is-folder': grandChild.type === 'folder' }">
                                            <div class="tree-item">
                                              <div class="tree-item-content">
                                                <span class="tree-connector child-connector"></span>
                                                <span class="tree-icon">
                                                  <i :class="grandChild.type === 'folder' ? 'fas fa-folder' : getFileIcon(grandChild.name)"></i>
                                                </span>
                                                <span class="tree-name" :contenteditable="true" @blur="updateGrandChildNodeName(sectionIndex, blockIndex, nodeIndex, childIndex, grandChildIndex, $event, subIndex)">{{ grandChild.name }}</span>
                                              </div>
                                              <div class="tree-item-actions">
                                                <button v-if="grandChild.type === 'folder'" @click="addGrandChildNode(sectionIndex, blockIndex, nodeIndex, subIndex, childIndex, grandChildIndex)" class="tree-action-btn add-child-btn" title="添加子项">
                                                  <i class="fas fa-plus-circle"></i>
                                                </button>
                                                <button @click="removeTreeNode(sectionIndex, blockIndex, grandChild.name, subIndex)" class="tree-action-btn remove-btn" title="删除">
                                                  <i class="fas fa-trash-alt"></i>
                                                </button>
                                              </div>
                                            </div>
                                          </div>
                                          <!-- 孙子节点内联添加区域 (subsection) -->
                                          <div v-if="grandChildInlineAddContext && grandChildInlineAddContext.sectionIndex === sectionIndex && grandChildInlineAddContext.blockIndex === blockIndex && grandChildInlineAddContext.nodeIndex === nodeIndex && grandChildInlineAddContext.childIndex === childIndex && grandChildInlineAddContext.subsectionIndex === subIndex" class="tree-children">
                                            <div class="tree-node tree-grandchild inline-add">
                                              <div class="tree-item">
                                                <div class="tree-item-content">
                                                  <span class="tree-connector child-connector"></span>
                                                  <input 
                                                    type="text" 
                                                    v-model="grandChildInlineAddContext.name"
                                                    @keyup.enter="confirmGrandChildInlineAdd"
                                                    @keyup.esc="cancelGrandChildInlineAdd"
                                                    @input="detectGrandChildType"
                                                    placeholder="输入文件名或文件夹名"
                                                    class="inline-add-input"
                                                    ref="grandChildInlineAddInput"
                                                  />
                                                  <span class="type-badge" :class="grandChildInlineAddContext.type === 'folder' ? 'folder-badge' : 'file-badge'">
                                                    <i :class="grandChildInlineAddContext.type === 'folder' ? 'fas fa-folder' : 'fas fa-file'"></i>
                                                    {{ grandChildInlineAddContext.type === 'folder' ? '文件夹' : '文件' }}
                                                  </span>
                                                </div>
                                                <div class="tree-item-actions">
                                                  <button @click="confirmGrandChildInlineAdd" :disabled="!grandChildInlineAddContext.name || !grandChildInlineAddContext.name.trim()" class="tree-action-btn confirm-btn" title="确认">
                                                    <i class="fas fa-check"></i>
                                                  </button>
                                                  <button @click="cancelGrandChildInlineAdd" class="tree-action-btn cancel-btn" title="取消">
                                                    <i class="fas fa-times"></i>
                                                  </button>
                                                </div>
                                              </div>
                                            </div>
                                          </div>
                                        </div>
                                        <!-- 子节点的子节点内联添加区域 -->
                                        <div v-if="childInlineAddMode && childInlineAddContext && childInlineAddContext.sectionIndex === sectionIndex && childInlineAddContext.blockIndex === blockIndex && childInlineAddContext.subsectionIndex === subIndex && childInlineAddContext.nodeIndex === nodeIndex && childInlineAddContext.childIndex === childIndex" class="tree-children">
                                          <div class="inline-add-container child-inline-add">
                                            <div class="inline-add-input-group">
                                              <span class="tree-connector child-connector"></span>
                                              <input 
                                                type="text" 
                                                v-model="childInlineAddName"
                                                @keyup.enter="confirmChildInlineAdd"
                                                @keyup.esc="cancelChildInlineAdd"
                                                @input="detectChildNodeType"
                                                placeholder="输入文件名或文件夹名"
                                                class="inline-add-input"
                                                ref="childInlineAddInput"
                                              />
                                              <span class="type-badge" :class="childInlineAddType === 'folder' ? 'folder-badge' : 'file-badge'">
                                                <i :class="childInlineAddType === 'folder' ? 'fas fa-folder' : 'fas fa-file'"></i>
                                                {{ childInlineAddType === 'folder' ? '文件夹' : '文件' }}
                                              </span>
                                              <button @click="confirmChildInlineAdd" :disabled="!childInlineAddName.trim()" class="inline-confirm-btn">
                                                <i class="fas fa-check"></i>
                                              </button>
                                              <button @click="cancelChildInlineAdd" class="inline-cancel-btn">
                                                <i class="fas fa-times"></i>
                                              </button>
                                            </div>
                                          </div>
                                        </div>
                                      </div>
                                    </div>

                                  </div>
                                  
                                  <!-- 根节点的子节点内联添加区域 -->
                                  <div v-if="childInlineAddMode && childInlineAddContext && childInlineAddContext.sectionIndex === sectionIndex && childInlineAddContext.blockIndex === blockIndex && childInlineAddContext.subsectionIndex === subIndex && childInlineAddContext.childIndex === null" class="tree-children">
                                    <div class="tree-node tree-child inline-add">
                                      <div class="tree-item">
                                        <div class="tree-item-content">
                                          <span class="tree-connector child-connector"></span>
                                          <input 
                                            type="text" 
                                            v-model="childInlineAddName"
                                            @keyup.enter="confirmChildInlineAdd"
                                            @keyup.esc="cancelChildInlineAdd"
                                            @input="detectChildNodeType"
                                            placeholder="输入文件名或文件夹名"
                                            class="inline-add-input"
                                            ref="childInlineAddInput"
                                          />
                                          <span class="type-badge" :class="childInlineAddType === 'folder' ? 'folder-badge' : 'file-badge'">
                                            <i :class="childInlineAddType === 'folder' ? 'fas fa-folder' : 'fas fa-file'"></i>
                                            {{ childInlineAddType === 'folder' ? '文件夹' : '文件' }}
                                          </span>
                                        </div>
                                        <div class="tree-item-actions">
                                          <button @click="confirmChildInlineAdd" :disabled="!childInlineAddName.trim()" class="tree-action-btn confirm-btn" title="确认">
                                            <i class="fas fa-check"></i>
                                          </button>
                                          <button @click="cancelChildInlineAdd" class="tree-action-btn cancel-btn" title="取消">
                                            <i class="fas fa-times"></i>
                                          </button>
                                        </div>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                                
                                <!-- 内联添加区域 -->
                                <div v-if="inlineAddMode && inlineAddContext && inlineAddContext.sectionIndex === sectionIndex && inlineAddContext.blockIndex === blockIndex && inlineAddContext.subsectionIndex === subIndex" class="inline-add-container">
                                  <div class="inline-add-input-group">
                                    <span class="tree-connector"></span>
                                    <input 
                                      type="text" 
                                      v-model="inlineAddName" 
                                      @keyup.enter="confirmInlineAdd"
                                      @keyup.escape="cancelInlineAdd"
                                      @input="detectInlineNodeType"
                                      placeholder="输入文件名或文件夹名"
                                      class="inline-add-input"
                                      ref="inlineAddInput"
                                    />
                                    <div class="inline-add-actions">
                                      <span class="type-badge" :class="inlineAddType === 'folder' ? 'folder-badge' : 'file-badge'">
                                        <i :class="inlineAddType === 'folder' ? 'fas fa-folder' : 'fas fa-file'"></i>
                                        {{ inlineAddType === 'folder' ? '文件夹' : '文件' }}
                                      </span>
                                      <button @click="confirmInlineAdd" :disabled="!inlineAddName.trim()" class="inline-confirm-btn">
                                        <i class="fas fa-check"></i>
                                      </button>
                                      <button @click="cancelInlineAdd" class="inline-cancel-btn">
                                        <i class="fas fa-times"></i>
                                      </button>
                                    </div>
                                  </div>
                                </div>
                                
                                <!-- 添加按钮 -->
                                <div v-if="!inlineAddMode" class="tree-add-footer">
                                  <button @click="startInlineAdd(sectionIndex, blockIndex, subIndex)" class="tree-add-footer-btn">
                                    <i class="fas fa-plus"></i> 添加文件/文件夹
                                  </button>
                                </div>
                              </div>
                              <div v-else class="tree-empty">
                                <div class="empty-state">
                                  <i class="fas fa-folder-open empty-icon"></i>
                                  <p>暂无文件，在下方输入框中添加文件或文件夹</p>
                                  
                                  <!-- 快速添加输入框 -->
                                  <div class="quick-add-container">
                                    <div class="quick-add-input-group">
                                      <input 
                                        type="text" 
                                        v-model="quickAddName" 
                                        @keyup.enter="quickAddNode(sectionIndex, blockIndex, subIndex)"
                                        @input="detectNodeType"
                                        placeholder="输入文件名（如 data.txt）或文件夹名（如 images/）"
                                        class="quick-add-input"
                                      />
                                      <div class="quick-add-type-indicator">
                                        <span v-if="quickAddType === 'folder'" class="type-badge folder-badge">
                                          <i class="fas fa-folder"></i> 文件夹
                                        </span>
                                        <span v-else class="type-badge file-badge">
                                          <i class="fas fa-file"></i> 文件
                                        </span>
                                      </div>
                                    </div>
                                    <button 
                                      @click="quickAddNode(sectionIndex, blockIndex, subIndex)"
                                      :disabled="!quickAddName.trim()"
                                      class="quick-add-btn"
                                    >
                                      <i class="fas fa-plus"></i> 添加
                                    </button>
                                  </div>
                                  
                                  <div class="quick-add-tips">
                                    <p><i class="fas fa-lightbulb"></i> 小贴士：</p>
                                    <ul>
                                      <li>以 <code>/</code> 结尾自动识别为文件夹</li>
                                      <li>包含文件扩展名自动识别为文件</li>
                                      <li>按 Enter 键快速添加</li>
                                    </ul>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                          <textarea 
                            v-model="block.description" 
                            placeholder="文件树说明（必填）"
                            rows="3"
                            class="block-textarea description-required"
                          ></textarea>
                        </div>
                        
                        <!-- 代码插块 -->
                        <div v-else-if="block.type === 'code'" class="code-block">
                          <div class="code-header">
                            <select v-model="block.language" class="language-select">
                              <option value="plaintext">Plain Text</option>
                              <option value="javascript">JavaScript</option>
                              <option value="python">Python</option>
                              <option value="java">Java</option>
                              <option value="cpp">C++</option>
                              <option value="html">HTML</option>
                              <option value="css">CSS</option>
                              <option value="bash">Bash</option>
                              <option value="json">JSON</option>
                              <option value="xml">XML</option>
                            </select>
                          </div>
                          <textarea 
                            v-model="block.code" 
                            placeholder="请输入代码..."
                            rows="8"
                            class="code-textarea"
                          ></textarea>
                          <textarea 
                            v-model="block.description" 
                            placeholder="代码说明（如：在分类任务中，我们仅使用VOCtrainval_06-Nov-2007.tar与VOCtest_06-Nov-2007.tar...）"
                            rows="4"
                            class="block-textarea"
                          ></textarea>
                        </div>
                        
                        <!-- 下载插块 -->
                        <div v-else-if="block.type === 'download'" class="download-block">
                          <div class="download-header">
                            <i class="fas fa-download"></i>
                            <span>下载资源配置</span>
                          </div>
                          <input 
                            v-model="block.title" 
                            placeholder="下载链接标题（如：官方网站、GitHub仓库）" 
                            class="block-input download-title" 
                          />
                          <input 
                            v-model="block.url" 
                            placeholder="下载链接URL（必填）" 
                            class="block-input download-url" 
                            type="url"
                          />
                          <textarea 
                            v-model="block.description" 
                            placeholder="下载说明（必填）"
                            rows="3"
                            class="block-textarea description-required"
                          ></textarea>
                          <input 
                            v-model="block.note" 
                            placeholder="备注（如：经常打不开，建议从本文档提供的连接下载）" 
                            class="block-input download-note" 
                          />
                          <div class="download-preview" v-if="block.url">
                            <small>预览链接：<a :href="block.url" target="_blank" rel="noopener">{{ block.url }}</a></small>
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    <!-- 添加插块提示 -->
                    <div class="add-block-area clickable-empty" @click="openBlockSelector(sectionIndex, subIndex)">
                      <p v-if="subsection.blocks.length === 0">暂无内容，点击此处添加插块</p>
                      <p v-else>点击此处继续添加插块</p>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 没有子章节的部分 -->
              <div v-else class="section-content">
                
                <!-- 插块列表 -->
                <div class="blocks-container">
                  <div v-for="(block, blockIndex) in section.blocks" :key="block.id" class="block-item">
                    <div class="block-header">
                      <span class="block-type">{{ blockTypes.find(t => t.type === block.type)?.icon }} {{ blockTypes.find(t => t.type === block.type)?.label }}</span>
                      <div class="block-controls">
                        <button @click="moveBlock(sectionIndex, blockIndex, 'up')" :disabled="blockIndex === 0" class="move-btn">↑</button>
                        <button @click="moveBlock(sectionIndex, blockIndex, 'down')" :disabled="blockIndex === section.blocks.length - 1" class="move-btn">↓</button>
                        <button @click="removeBlock(sectionIndex, blockIndex)" class="remove-btn">🗑️</button>
                      </div>
                    </div>
                    
                    <!-- 插块内容（与上面相同的结构） -->
                    <div class="block-content">
                      <!-- 文本插块 -->
                      <div v-if="block.type === 'text'" class="text-block">
                        <RichTextEditor 
                          v-model="block.content"
                          :placeholder="'请输入文本内容...'"
                          @update:modelValue="updateBlockContent(sectionIndex, null, blockIndex, $event)"
                        />
                      </div>
                      
                      <!-- 图片上传插块 -->
                      <div v-else-if="block.type === 'image-upload'" class="image-upload-block">
                        <div class="image-upload-area">
                          <input 
                            type="file" 
                            accept="image/*" 
                            @change="handleImageUpload($event, sectionIndex, blockIndex)"
                            class="image-file-input"
                            :id="'image-main-' + sectionIndex + '-' + blockIndex"
                          />
                          <label :for="'image-main-' + sectionIndex + '-' + blockIndex" class="image-upload-label">
                            <div v-if="!block.url" class="upload-placeholder">
                              <i class="fas fa-camera"></i>
                              <span>点击上传图片</span>
                              <small>支持 JPG、PNG、GIF 格式，最大 5MB</small>
                            </div>
                            <div v-else class="image-preview">
                              <img :src="block.url" :alt="block.alt" class="uploaded-image" />
                              <button @click.prevent="removeImage(sectionIndex, blockIndex)" class="remove-image-btn">
                                <i class="fas fa-times"></i>
                              </button>
                            </div>
                          </label>

                        </div>
                        <input 
                          v-model="block.caption" 
                          placeholder="图注（如：图1. VOC2007数据集示例）" 
                          class="block-input caption-input" 
                        />
                        <textarea 
                          v-model="block.description" 
                          placeholder="图片说明（必填）"
                          rows="3"
                          class="block-textarea description-required"
                        ></textarea>
                      </div>
                      
                      <!-- 文件树插块 -->
                      <div v-else-if="block.type === 'file-tree'" class="file-tree-block">
                        <div class="tree-editor">
                          <div class="tree-header">
                            <h4 class="tree-title">
                              <i class="fas fa-folder-tree"></i>
                              文件目录结构
                            </h4>
                            <div class="tree-actions">
                              <button @click="addTreeNode(sectionIndex, blockIndex)" class="tree-btn add-btn">
                                <i class="fas fa-plus"></i> 添加文件/文件夹
                              </button>
                            </div>
                          </div>
                          
                          <div class="tree-container">
                            <div v-if="block.treeData && block.treeData[0] && block.treeData[0].children && block.treeData[0].children.length > 0" class="tree-structure">
                              <div class="tree-root-name">
                                <i class="fas fa-folder-open tree-root-icon"></i>
                                <span>{{ block.treeData[0].name || 'root' }}</span>
                              </div>
                              <div class="tree-content">
                                <div v-for="(node, nodeIndex) in block.treeData[0].children" :key="nodeIndex" class="tree-node" :class="{ 'is-folder': node.type === 'folder' }">
                                  <div class="tree-item">
                                    <div class="tree-item-content">
                                      <span class="tree-connector"></span>
                                      <span class="tree-icon">
                                        <i :class="node.type === 'folder' ? 'fas fa-folder' : getFileIcon(node.name)"></i>
                                      </span>
                                      <span class="tree-name" :contenteditable="true" @blur="updateNodeName(sectionIndex, blockIndex, nodeIndex, $event)">{{ node.name }}</span>
                                    </div>
                                    <div class="tree-item-actions">
                                      <button v-if="node.type === 'folder'" @click="addChildNode(sectionIndex, blockIndex, nodeIndex)" class="tree-action-btn" title="添加子项">
                                        <i class="fas fa-plus"></i>
                                      </button>
                                      <button @click="removeTreeNode(sectionIndex, blockIndex, node.name)" class="tree-action-btn remove-btn" title="删除">
                                        <i class="fas fa-trash"></i>
                                      </button>
                                    </div>
                                  </div>
                                  <!-- 递归显示子节点 -->
                                  <div v-if="node.children && node.children.length > 0" class="tree-children">
                                    <div v-for="(child, childIndex) in node.children" :key="childIndex" class="tree-node tree-child" :class="{ 'is-folder': child.type === 'folder' }">
                                      <div class="tree-item">
                                        <div class="tree-item-content">
                                          <span class="tree-connector child-connector"></span>
                                          <span class="tree-icon">
                                            <i :class="child.type === 'folder' ? 'fas fa-folder' : getFileIcon(child.name)"></i>
                                          </span>
                                          <span class="tree-name" :contenteditable="true" @blur="updateChildNodeName(sectionIndex, blockIndex, nodeIndex, childIndex, $event)">{{ child.name }}</span>
                                        </div>
                                        <div class="tree-item-actions">
                                          <button v-if="child.type === 'folder'" @click="addGrandChildNode(sectionIndex, blockIndex, nodeIndex, childIndex)" class="tree-action-btn add-child-btn" title="添加子项">
                                            <i class="fas fa-plus-circle"></i>
                                          </button>
                                          <button @click="removeTreeNode(sectionIndex, blockIndex, child.name)" class="tree-action-btn remove-btn" title="删除">
                                            <i class="fas fa-trash"></i>
                                          </button>
                                        </div>
                                      </div>
                                      <!-- 递归显示孙子节点 -->
                                      <div v-if="child.children && child.children.length > 0" class="tree-children">
                                        <div v-for="(grandChild, grandChildIndex) in child.children" :key="grandChildIndex" class="tree-node tree-grandchild" :class="{ 'is-folder': grandChild.type === 'folder' }">
                                          <div class="tree-item">
                                            <div class="tree-item-content">
                                              <span class="tree-connector grandchild-connector"></span>
                                              <span class="tree-icon">
                                                <i :class="grandChild.type === 'folder' ? 'fas fa-folder' : getFileIcon(grandChild.name)"></i>
                                              </span>
                                              <span class="tree-name" :contenteditable="true" @blur="updateGrandChildNodeName(sectionIndex, blockIndex, nodeIndex, childIndex, grandChildIndex, $event)">{{ grandChild.name }}</span>
                                            </div>
                                            <div class="tree-item-actions">
                                              <button @click="removeTreeNode(sectionIndex, blockIndex, grandChild.name)" class="tree-action-btn remove-btn" title="删除">
                                                <i class="fas fa-trash"></i>
                                              </button>
                                            </div>
                                          </div>
                                        </div>
                                      </div>
                                      <!-- 孙子节点内联添加区域 -->
                                      <div v-if="grandChildInlineAddContext && grandChildInlineAddContext.sectionIndex === sectionIndex && grandChildInlineAddContext.blockIndex === blockIndex && grandChildInlineAddContext.nodeIndex === nodeIndex && grandChildInlineAddContext.childIndex === childIndex && (grandChildInlineAddContext.subsectionIndex === undefined || grandChildInlineAddContext.subsectionIndex === null)" class="tree-children">
                                        <div class="tree-node tree-grandchild inline-add">
                                          <div class="tree-item">
                                            <div class="tree-item-content">
                                              <span class="tree-connector grandchild-connector"></span>
                                              <span class="tree-icon">
                                                <i :class="grandChildInlineAddContext.type === 'folder' ? 'fas fa-folder' : 'fas fa-file'"></i>
                                              </span>
                                              <input 
                                                ref="grandChildInlineInput"
                                                v-model="grandChildInlineAddContext.name"
                                                @keyup.enter="confirmGrandChildInlineAdd"
                                                @keyup.esc="cancelGrandChildInlineAdd"
                                                @input="detectGrandChildType"
                                                class="tree-name-input"
                                                placeholder="输入名称"
                                              />
                                            </div>
                                            <div class="tree-item-actions">
                                              <button @click="confirmGrandChildInlineAdd" class="tree-action-btn confirm-btn" title="确认">
                                                <i class="fas fa-check"></i>
                                              </button>
                                              <button @click="cancelGrandChildInlineAdd" class="tree-action-btn cancel-btn" title="取消">
                                                <i class="fas fa-times"></i>
                                              </button>
                                            </div>
                                          </div>
                                        </div>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                            <div v-else class="tree-empty">
                              <div class="empty-state">
                                <i class="fas fa-folder-open empty-icon"></i>
                                <p>暂无文件，点击上方按钮添加文件或文件夹</p>
                              </div>
                            </div>
                          </div>
                        </div>
                        <textarea 
                          v-model="block.description" 
                          placeholder="文件树说明（必填）"
                          rows="3"
                          class="block-textarea description-required"
                        ></textarea>
                      </div>
                      
                      <!-- 代码插块 -->
                      <div v-else-if="block.type === 'code'" class="code-block">
                        <div class="code-header">
                          <select v-model="block.language" class="language-select">
                            <option value="plaintext">Plain Text</option>
                            <option value="javascript">JavaScript</option>
                            <option value="python">Python</option>
                            <option value="java">Java</option>
                            <option value="cpp">C++</option>
                            <option value="html">HTML</option>
                            <option value="css">CSS</option>
                            <option value="bash">Bash</option>
                            <option value="json">JSON</option>
                            <option value="xml">XML</option>
                          </select>
                        </div>
                        <textarea 
                          v-model="block.code" 
                          placeholder="请输入代码..."
                          rows="8"
                          class="code-textarea"
                        ></textarea>
                        <textarea 
                          v-model="block.description" 
                          placeholder="代码说明（如：在分类任务中，我们仅使用VOCtrainval_06-Nov-2007.tar与VOCtest_06-Nov-2007.tar...）"
                          rows="4"
                          class="block-textarea"
                        ></textarea>
                      </div>
                      
                      <!-- 下载插块 -->
                      <div v-else-if="block.type === 'download'" class="download-block">
                        <div class="download-header">
                          <i class="fas fa-download"></i>
                          <span>下载资源配置</span>
                        </div>
                        <input 
                          v-model="block.title" 
                          placeholder="下载链接标题（如：官方网站、GitHub仓库）" 
                          class="block-input download-title" 
                        />
                        <input 
                          v-model="block.url" 
                          placeholder="下载链接URL（必填）" 
                          class="block-input download-url" 
                          type="url"
                        />
                        <textarea 
                          v-model="block.description" 
                          placeholder="下载说明（必填）"
                          rows="3"
                          class="block-textarea description-required"
                        ></textarea>
                        <input 
                          v-model="block.note" 
                          placeholder="备注（如：经常打不开，建议从本文档提供的连接下载）" 
                          class="block-input download-note" 
                        />
                        <div class="download-preview" v-if="block.url">
                          <small>预览链接：<a :href="block.url" target="_blank" rel="noopener">{{ block.url }}</a></small>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 添加插块提示 -->
                  <div class="add-block-area clickable-empty" @click="openBlockSelector(sectionIndex)">
                    <p v-if="section.blocks.length === 0">暂无内容，点击此处添加插块</p>
                    <p v-else>点击此处继续添加插块</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Markdown上传模式 -->
      <div class="section" v-else>
        <h2 class="section-title">上传Markdown文件</h2>
        <div class="form-grid">
          <div class="form-group full-width">
            <label for="markdownFile">选择Markdown文件 *</label>
            <input id="markdownFile" type="file" accept=".md,text/markdown" @change="onMarkdownFileSelect" />
          </div>
          <div class="form-group full-width">
            <label for="markdownImages">上传相关图片（可选，多选）</label>
            <input id="markdownImages" type="file" accept="image/*" multiple @change="onMarkdownImagesSelect" />
            <div class="file-list" v-if="markdownImages.length">
              <div class="file-item" v-for="(f,i) in markdownImages" :key="i">
                <div class="file-name">{{ f.name }}</div>
                <div class="file-size">{{ formatFileSize(f.size) }}</div>
                <button class="file-remove" @click="removeMarkdownImage(i)">删除</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 预览和提交区域 -->
      <div class="section">
        <div class="action-buttons">
          <button @click="previewMarkdown" class="preview-btn">预览Markdown</button>
          <button @click="previewHTML" class="preview-btn html-preview-btn">预览HTML</button>
          <button @click="testColorTags" class="test-btn" style="background-color: #17a2b8; margin-right: 10px;">测试颜色标签</button>
          <button @click="submitDataset" class="submit-btn" :disabled="!isFormValid">提交数据集</button>
        </div>
        
        <!-- 调试信息 -->
        <div class="debug-info" style="margin-top: 1rem; padding: 1rem; background: #f8f9fa; border-radius: 8px; font-size: 0.875rem;">
          <h4>表单验证状态：</h4>
          <ul>
            <li>数据集名称: {{ formData.name ? '✓ 已填写' : '✗ 未填写' }} ({{ formData.name }})</li>
            <li>任务选择: {{ formData.taskIds.length > 0 ? '✓ 已选择' : '✗ 未选择' }} ({{ formData.taskIds.length }}个)</li>
            <li>模式: {{ uploadMode }}</li>
            <li v-if="uploadMode==='markdown'">Markdown文件: {{ markdownFile ? markdownFile.name : '未选择' }}</li>
            <li>任务ID列表: {{ JSON.stringify(formData.taskIds) }}</li>
            <li>数据集文件: {{ uploadedFiles.length > 0 ? '✓ 已上传' : '✗ 未上传' }} ({{ uploadedFiles.length }}个)</li>
            <li>表单有效性: {{ isFormValid ? '✓ 有效' : '✗ 无效' }}</li>
            <li>用户Token: {{ getUserTokenStatus() }}</li>
          </ul>
        </div>
      </div>

      <!-- Markdown预览模态框 -->
      <div v-if="showPreview" class="modal-overlay" @click="closePreview">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>Markdown预览</h3>
            <button @click="closePreview" class="close-btn">×</button>
          </div>
          <div class="modal-body">
            <div class="markdown-preview" ref="markdownPreview" v-html="markdownHtml"></div>
          </div>
        </div>
      </div>

      <!-- HTML预览模态框 -->
      <div v-if="showHTMLPreview" class="modal-overlay" @click="closeHTMLPreview">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>HTML预览</h3>
            <button @click="closeHTMLPreview" class="close-btn">×</button>
          </div>
          <div class="modal-body">
            <div class="html-preview">
              <div class="html-raw">
                <h4>HTML源码：</h4>
                <pre class="html-code">{{ generatedHTML }}</pre>
              </div>
              <div class="html-rendered">
                <h4>渲染效果：</h4>
                <div class="html-content" v-html="generatedHTML"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 插块选择弹窗 -->
      <div v-if="showBlockSelector" class="modal-overlay" @click="closeBlockSelector">
        <div class="block-selector-modal" @click.stop>
          <div class="modal-header">
            <h3>选择插块类型</h3>
            <button @click="closeBlockSelector" class="close-btn">×</button>
          </div>
          <div class="block-selector-grid">
            <div 
              v-for="blockType in blockTypes" 
              :key="blockType.type"
              @click="selectBlockType(blockType.type)"
              class="block-type-card"
            >
              <div class="block-icon">{{ blockType.icon }}</div>
              <div class="block-info">
                <h4>{{ blockType.label }}</h4>
                <p>{{ blockType.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import RichTextEditor from '../components/RichTextEditor.vue'
import { marked } from 'marked'
import hljs from 'highlight.js'

export default {
  name: 'DatasetUpload',
  components: {
    RichTextEditor
  },
  data() {
    return {
      formData: {
        name: '',
        description: '',
        taskIds: [],
        trueAddress: '',
        sizeFormatted: ''
      },
      availableTasks: [],
      uploadMode: 'editor',
      markdownFile: null,
      markdownImages: [],
      taskDropdownOpen: false,
      taskSearch: '',
      newTag: '',
      uploadedFiles: [],
      showPreview: false,
      generatedMarkdown: '',
      showHTMLPreview: false,
      generatedHTML: '',
      templateContent: '',
      // 快速添加文件树节点
      quickAddName: '',
      quickAddType: 'file',
      // 内联添加状态
      inlineAddMode: false,
      inlineAddName: '',
      inlineAddType: 'file',
      inlineAddContext: null,
      // 子节点内联添加状态
      childInlineAddMode: false,
      childInlineAddName: '',
      childInlineAddType: 'file',
      childInlineAddContext: null,
      // 孙子节点内联添加状态
      grandChildInlineAddContext: null,
      // 输入防抖定时器
      inputTimeout: null,
      // Markdown预览状态
      markdownPreviewStates: {},
      detailSections: [
        {
          title: '1. 数据集说明',
          subsections: [
            { title: '1.1 简介', blocks: [] },
            { title: '1.2 数据集特性', blocks: [] }
          ]
        },
        {
          title: '2. 下载方式',
          blocks: []
        },
        {
          title: '3. 文件结构',
          blocks: []
        },
        {
          title: '4. 使用示例',
          blocks: []
        },
        {
          title: '5. 参考资料',
          subsections: [
            { title: '5.1 相关论文', blocks: [] },
            { title: '5.2 其它资料', blocks: [] }
          ]
        },
        {
          title: '6. 备注',
          blocks: []
        }
      ],
      blockTypes: [
        { 
          type: 'text', 
          label: '文本块', 
          icon: '📝',
          description: '添加文本内容，支持富文本编辑'
        },
        { 
          type: 'image-upload', 
          label: '图片上传块', 
          icon: '🖼️',
          description: '上传图片并添加图注说明'
        },
        { 
          type: 'file-tree', 
          label: '文件树块', 
          icon: '📁',
          description: '展示文件目录结构'
        },
        { 
          type: 'code', 
          label: '代码块', 
          icon: '💻',
          description: '添加代码示例，支持语法高亮'
        },
        { 
          type: 'download', 
          label: '下载块', 
          icon: '⬇️',
          description: '提供下载链接和说明'
        }
      ],
      // 插块选择弹窗状态
      showBlockSelector: false,
      blockSelectorContext: {
        sectionIndex: null,
        subsectionIndex: null
      }
    }
  },
  async mounted() {
    // 配置marked以支持HTML标签和代码高亮
    marked.setOptions({
      highlight: function(code, lang) {
        if (lang && hljs.getLanguage(lang)) {
          try {
            return hljs.highlight(code, { language: lang }).value
          } catch (err) {}
        }
        return hljs.highlightAuto(code).value
      },
      breaks: true,
      gfm: true,
      sanitize: false // 允许HTML标签，包括颜色样式
    })
    await this.loadTemplate()
    await this.loadTasks()
    document.addEventListener('click', this.handleGlobalClick, true)
    
    // 延迟测试颜色标签功能
    setTimeout(() => {
      console.log('🧪 准备测试颜色标签功能...')
      // 可以通过控制台手动调用: this.testColorTags()
    }, 2000)
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleGlobalClick, true)
  },
  computed: {
    isFormValid() {
      const nameValid = this.formData.name.trim()
      const addressValid = (this.formData.trueAddress || '').trim()
      const modeOk = this.uploadMode === 'editor' ? true : !!this.markdownFile
      const result = nameValid && addressValid && modeOk
      console.log('isFormValid check:', { nameValid, addressValid, result })
      return result
    },
    markdownHtml() {
      if (!this.generatedMarkdown) return ''
      
      console.log('原始Markdown内容:', this.generatedMarkdown)
      
      try {
        // 使用marked库进行标准Markdown渲染
        let html = marked(this.generatedMarkdown)
        
        console.log('转换后的HTML:', html)
        return html
      } catch (error) {
        console.error('Markdown渲染错误:', error)
        return '<p>Markdown渲染失败，请检查内容格式</p>'
      }
    },
    filteredTasks() {
      const q = (this.taskSearch || '').trim().toLowerCase()
      if (!q) return this.availableTasks
      return this.availableTasks.filter(t => (t.name || '').toLowerCase().includes(q))
    },
    selectedTasks() {
      const set = new Set(this.formData.taskIds)
      return this.availableTasks.filter(t => set.has(t.id))
    }
  },
  watch: {
    // 监听markdownHtml的变化，自动处理颜色标签
    markdownHtml() {
      this.processColorTags()
    },
    
    // 监听generatedMarkdown的变化
    generatedMarkdown() {
      this.processColorTags()
    }
  },
  methods: {
    onMarkdownFileSelect(e) {
      const f = e.target.files?.[0] || null
      this.markdownFile = f
    },
    onMarkdownImagesSelect(e) {
      const files = Array.from(e.target.files || [])
      this.markdownImages.push(...files)
    },
    removeMarkdownImage(i) {
      this.markdownImages.splice(i, 1)
    },
    openTaskDropdown() {
      this.taskDropdownOpen = true
      this.$nextTick(() => {
        const input = this.$refs.taskDropdown?.querySelector('.search-input')
        if (input) input.focus()
      })
    },
    closeTaskDropdown() {
      this.taskDropdownOpen = false
    },
    handleGlobalClick(event) {
      const selector = this.$refs.taskSelector
      const dropdown = this.$refs.taskDropdown
      if (!selector) return
      if (dropdown && dropdown.contains(event.target)) return
      if (selector.contains(event.target)) return
      this.taskDropdownOpen = false
    },
    clearTaskSelection() {
      this.formData.taskIds = []
    },
    async loadTasks() {
      try {
        const token = localStorage.getItem('userToken') || localStorage.getItem('token')
        const resp = await fetch('http://localhost:8080/tasks', {
          headers: token ? { 'Authorization': `Bearer ${token}` } : {}
        })
        if (!resp.ok) return
        const result = await resp.json()
        if (result.code === 200 && Array.isArray(result.data)) {
          this.availableTasks = result.data
        }
      } catch (e) {}
    },
    toggleTask(id) {
      const idx = this.formData.taskIds.indexOf(id)
      if (idx >= 0) {
        this.formData.taskIds.splice(idx, 1)
      } else {
        this.formData.taskIds.push(id)
      }
    },
    isTaskSelected(id) {
      return this.formData.taskIds.includes(id)
    },
    // 通过JavaScript动态处理颜色标签
    processColorTags() {
      this.$nextTick(() => {
        const previewElement = this.$refs.markdownPreview
        if (!previewElement) {
          console.log('❌ 预览元素未找到')
          return
        }
        
        console.log('🎨 开始处理颜色标签...')
        console.log('📄 预览元素HTML:', previewElement.innerHTML.substring(0, 500))
        
        // 查找所有带有颜色样式的span标签
        const colorSpans = previewElement.querySelectorAll('span[style*="color"]')
        console.log(`🔍 找到 ${colorSpans.length} 个带颜色的span元素`)
        
        colorSpans.forEach((span, index) => {
          const style = span.getAttribute('style')
          const textContent = span.textContent
          console.log(`📝 处理第 ${index + 1} 个span:`, {
            text: textContent,
            originalStyle: style,
            innerHTML: span.innerHTML
          })
          
          if (style) {
            // 提取颜色值
            const colorMatch = style.match(/color:\s*([^;]+)/)
            if (colorMatch) {
              const color = colorMatch[1].trim()
              
              // 直接设置元素的颜色样式
              span.style.setProperty('color', color, 'important')
              
              // 添加data-color属性
              span.setAttribute('data-color', color)
              
              // 添加一个类名用于调试
              span.classList.add('color-processed')
              
              console.log(`✅ 成功处理颜色标签:`, {
                color: color,
                text: textContent,
                finalStyle: span.getAttribute('style'),
                computedColor: window.getComputedStyle(span).color
              })
            }
          }
        })
        
        // 处理可能被嵌套在其他元素中的颜色标签
        const allElements = previewElement.querySelectorAll('*[style*="color"]')
        console.log(`🔍 总共找到 ${allElements.length} 个带颜色的元素`)
        
        allElements.forEach((element, index) => {
          if (element.tagName.toLowerCase() !== 'span') {
            const style = element.getAttribute('style')
            const textContent = element.textContent
            console.log(`📝 处理非span元素 ${element.tagName}:`, {
              text: textContent,
              originalStyle: style
            })
            
            if (style) {
              const colorMatch = style.match(/color:\s*([^;]+)/)
              if (colorMatch) {
                const color = colorMatch[1].trim()
                element.style.setProperty('color', color, 'important')
                element.setAttribute('data-color', color)
                element.classList.add('color-processed')
                
                console.log(`✅ 成功处理非span元素:`, {
                  tag: element.tagName,
                  color: color,
                  text: textContent,
                  computedColor: window.getComputedStyle(element).color
                })
              }
            }
          }
        })
        
        console.log('🎨 颜色标签处理完成')
      })
    },
    
    // 测试颜色标签功能
    testColorTags() {
      console.log('🧪 开始测试颜色标签功能...')
      
      // 创建测试内容
      const testContent = `
        <p>这是一个测试段落，包含 <span style="color: red;">红色文字</span> 和 <span style="color: blue;">蓝色文字</span>。</p>
        <p>还有 <span style="color: #00ff00;">绿色文字</span> 和 <span style="color: rgb(255, 165, 0);">橙色文字</span>。</p>
      `
      
      // 临时设置测试内容
      const originalContent = this.generatedMarkdown
      this.sections = [{
        title: '测试颜色标签',
        blocks: [{
          type: 'text',
          content: testContent
        }]
      }]
      
      // 等待DOM更新后恢复原内容
      this.$nextTick(() => {
        setTimeout(() => {
          // 恢复原内容
          this.generatedMarkdown = originalContent
        }, 3000)
      })
    },
    // 加载模板文件
    async loadTemplate() {
      try {
        const response = await fetch('/markdownTemplate.md')
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        this.templateContent = await response.text()
      } catch (error) {
        console.error('加载模板文件失败:', error)
        this.templateContent = ''
      }
    },
    
    // 任务选择管理
    
    // 文件上传
    handleFileSelect(event) {
      const files = Array.from(event.target.files)
      this.uploadedFiles.push(...files)
    },
    handleDrop(event) {
      event.preventDefault()
      const files = Array.from(event.dataTransfer.files)
      this.uploadedFiles.push(...files)
    },
    removeFile(index) {
      this.uploadedFiles.splice(index, 1)
    },
    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },
    
    // 内容块管理
    addBlock(sectionIndex) {
      this.detailSections[sectionIndex].blocks.push({
        type: 'text',
        content: '',
        imageUrl: '',
        caption: '',
        treeStructure: '',
        language: 'python',
        code: '',
        description: ''
      })
    },
    removeBlock(sectionIndex, blockIndex) {
      this.detailSections[sectionIndex].blocks.splice(blockIndex, 1)
    },
    onBlockTypeChange(sectionIndex, blockIndex) {
      const block = this.detailSections[sectionIndex].blocks[blockIndex]
      // 重置块内容
      block.content = ''
      block.imageUrl = ''
      block.caption = ''
      block.treeStructure = ''
      block.code = ''
      block.description = ''
    },
    


    // 插块选择弹窗方法
    openBlockSelector(sectionIndex, subsectionIndex = null) {
      this.blockSelectorContext = {
        sectionIndex,
        subsectionIndex
      }
      this.showBlockSelector = true
    },
    
    closeBlockSelector() {
      this.showBlockSelector = false
      this.blockSelectorContext = {
        sectionIndex: null,
        subsectionIndex: null
      }
    },
    
    selectBlockType(blockType) {
      const { sectionIndex, subsectionIndex } = this.blockSelectorContext
      this.addBlock(sectionIndex, subsectionIndex, blockType)
      this.closeBlockSelector()
    },

    // 插块管理方法
    addBlock(sectionIndex, subsectionIndex = null, blockType = 'text') {
      const newBlock = this.createBlock(blockType)
      
      if (subsectionIndex !== null) {
        // 添加到子章节
        this.detailSections[sectionIndex].subsections[subsectionIndex].blocks.push(newBlock)
      } else {
        // 添加到主章节
        this.detailSections[sectionIndex].blocks.push(newBlock)
      }
    },

    createBlock(type) {
      const blockId = Date.now() + Math.random()
      const baseBlock = {
        id: blockId,
        type: type
      }

      switch (type) {
        case 'text':
          return { ...baseBlock, content: '' }
        case 'image-upload':
          return { 
            ...baseBlock, 
            file: null, 
            url: '', 
            caption: '', 
            alt: '',
            description: '' 
          }
        case 'file-tree':
          return { 
            ...baseBlock, 
            treeData: [],
            description: '' 
          }
        case 'code':
          return { 
            ...baseBlock, 
            language: 'plaintext', 
            code: '', 
            description: '' 
          }
        case 'download':
          return { 
            ...baseBlock, 
            title: '', 
            url: '', 
            description: '',
            note: '' 
          }
        default:
          return { ...baseBlock, content: '' }
      }
    },

    removeBlock(sectionIndex, blockIndex, subsectionIndex = null) {
      if (subsectionIndex !== null) {
        this.detailSections[sectionIndex].subsections[subsectionIndex].blocks.splice(blockIndex, 1)
      } else {
        this.detailSections[sectionIndex].blocks.splice(blockIndex, 1)
      }
    },

    moveBlock(sectionIndex, blockIndex, direction, subsectionIndex = null) {
      const blocks = subsectionIndex !== null 
        ? this.detailSections[sectionIndex].subsections[subsectionIndex].blocks
        : this.detailSections[sectionIndex].blocks

      if (direction === 'up' && blockIndex > 0) {
        [blocks[blockIndex], blocks[blockIndex - 1]] = [blocks[blockIndex - 1], blocks[blockIndex]]
      } else if (direction === 'down' && blockIndex < blocks.length - 1) {
        [blocks[blockIndex], blocks[blockIndex + 1]] = [blocks[blockIndex + 1], blocks[blockIndex]]
      }
    },

    // 图片上传处理方法
    handleImageUpload(event, sectionIndex, blockIndex, subsectionIndex = null) {
      const file = event.target.files[0]
      if (file) {
        // 验证文件类型
        if (!file.type.startsWith('image/')) {
          alert('请选择图片文件')
          return
        }
        
        // 验证文件大小 (5MB)
        if (file.size > 5 * 1024 * 1024) {
          alert('图片文件大小不能超过5MB')
          return
        }

        const reader = new FileReader()
        reader.onload = (e) => {
          const block = subsectionIndex !== null 
            ? this.detailSections[sectionIndex].subsections[subsectionIndex].blocks[blockIndex]
            : this.detailSections[sectionIndex].blocks[blockIndex]
          
          block.file = file
          block.url = e.target.result
          block.alt = file.name
        }
        reader.readAsDataURL(file)
      }
    },

    removeImage(sectionIndex, blockIndex, subsectionIndex = null) {
      const block = subsectionIndex !== null 
        ? this.detailSections[sectionIndex].subsections[subsectionIndex].blocks[blockIndex]
        : this.detailSections[sectionIndex].blocks[blockIndex]
      
      block.file = null
      block.url = ''
      block.alt = ''
    },

    // 收集所有图片文件
    collectImageFiles() {
      const imageFiles = []
      
      this.detailSections.forEach(section => {
        if (section.subsections) {
          // 有子章节的情况
          section.subsections.forEach(subsection => {
            subsection.blocks.forEach(block => {
              if (block.type === 'image-upload' && block.file) {
                imageFiles.push(block.file)
              }
            })
          })
        } else {
          // 没有子章节的情况
          section.blocks.forEach(block => {
            if (block.type === 'image-upload' && block.file) {
              imageFiles.push(block.file)
            }
          })
        }
      })
      
      return imageFiles
    },



    // 文件树处理方法
    addTreeNode(sectionIndex, blockIndex, parentPath = '', subsectionIndex = null) {
      // 使用内联添加方式
      this.startInlineAdd(sectionIndex, blockIndex, subsectionIndex);
    },

    findAndAddToParent(nodes, parentPath, newNode) {
      for (let node of nodes) {
        if (node.name === parentPath && node.type === 'folder') {
          node.children.push(newNode)
          return true
        }
        if (node.children && this.findAndAddToParent(node.children, parentPath, newNode)) {
          return true
        }
      }
      return false
    },

    removeTreeNode(sectionIndex, blockIndex, nodePath, subsectionIndex = null) {
      const block = subsectionIndex !== null 
        ? this.detailSections[sectionIndex].subsections[subsectionIndex].blocks[blockIndex]
        : this.detailSections[sectionIndex].blocks[blockIndex]
      
      this.removeNodeFromTree(block.treeData, nodePath)
    },

    removeNodeFromTree(nodes, targetPath) {
      for (let i = 0; i < nodes.length; i++) {
        if (nodes[i].name === targetPath) {
          nodes.splice(i, 1)
          return true
        }
        if (nodes[i].children && this.removeNodeFromTree(nodes[i].children, targetPath)) {
          return true
        }
      }
      return false
    },

    // 获取文件图标
    getFileIcon(fileName) {
      const ext = fileName.split('.').pop()?.toLowerCase()
      const iconMap = {
        'js': 'fab fa-js-square',
        'ts': 'fas fa-code',
        'py': 'fab fa-python',
        'java': 'fab fa-java',
        'html': 'fab fa-html5',
        'css': 'fab fa-css3-alt',
        'json': 'fas fa-file-code',
        'xml': 'fas fa-file-code',
        'md': 'fab fa-markdown',
        'txt': 'fas fa-file-alt',
        'pdf': 'fas fa-file-pdf',
        'doc': 'fas fa-file-word',
        'docx': 'fas fa-file-word',
        'xls': 'fas fa-file-excel',
        'xlsx': 'fas fa-file-excel',
        'ppt': 'fas fa-file-powerpoint',
        'pptx': 'fas fa-file-powerpoint',
        'zip': 'fas fa-file-archive',
        'rar': 'fas fa-file-archive',
        '7z': 'fas fa-file-archive',
        'jpg': 'fas fa-file-image',
        'jpeg': 'fas fa-file-image',
        'png': 'fas fa-file-image',
        'gif': 'fas fa-file-image',
        'svg': 'fas fa-file-image',
        'mp4': 'fas fa-file-video',
        'avi': 'fas fa-file-video',
        'mov': 'fas fa-file-video',
        'mp3': 'fas fa-file-audio',
        'wav': 'fas fa-file-audio'
      }
      return iconMap[ext] || 'fas fa-file'
    },

    // 检测节点类型
    detectNodeType() {
      const name = this.quickAddName.trim()
      if (name.endsWith('/')) {
        this.quickAddType = 'folder'
      } else if (name.includes('.')) {
        this.quickAddType = 'file'
      } else if (name.length > 0) {
        // 如果没有扩展名且不以/结尾，默认为文件
        this.quickAddType = 'file'
      }
    },

    // 快速添加节点
    quickAddNode(sectionIndex, blockIndex, subsectionIndex = null) {
      const name = this.quickAddName.trim()
      if (!name) return

      const block = subsectionIndex !== null 
        ? this.detailSections[sectionIndex].subsections[subsectionIndex].blocks[blockIndex]
        : this.detailSections[sectionIndex].blocks[blockIndex]

      // 确保 treeData 存在
      if (!block.treeData || !Array.isArray(block.treeData)) {
        block.treeData = []
      }

      // 处理名称和类型
      let finalName = name
      let nodeType = this.quickAddType

      if (nodeType === 'folder' && !finalName.endsWith('/')) {
        finalName += '/'
      } else if (nodeType === 'file' && finalName.endsWith('/')) {
        finalName = finalName.slice(0, -1)
      }

      const newNode = {
        name: finalName,
        type: nodeType,
        children: nodeType === 'folder' ? [] : undefined
      }

      // 直接添加到treeData
      block.treeData.push(newNode)

      // 清空输入框
      this.quickAddName = ''
      this.quickAddType = 'file'
    },

    // 开始内联添加
    startInlineAdd(sectionIndex, blockIndex, subsectionIndex) {
      console.log('startInlineAdd called with:', { sectionIndex, blockIndex, subsectionIndex });
      this.inlineAddMode = true;
      this.inlineAddName = '';
      this.inlineAddType = 'file';
      this.inlineAddContext = {
        sectionIndex,
        blockIndex,
        subsectionIndex
      };
      console.log('inlineAddContext set to:', this.inlineAddContext);
      console.log('inlineAddMode set to:', this.inlineAddMode);
      
      // 下一个tick后聚焦输入框
      this.$nextTick(() => {
        const input = this.$refs.inlineAddInput;
        console.log('Input element found:', input);
        if (input) {
          input.focus();
        }
      });
    },
    
    // 检测内联节点类型
    detectInlineNodeType() {
      const name = this.inlineAddName.trim();
      if (name.endsWith('/')) {
        this.inlineAddType = 'folder';
      } else if (name.includes('.')) {
        this.inlineAddType = 'file';
      } else if (name.length > 0) {
        this.inlineAddType = 'file';
      }
    },
    
    // 确认内联添加
    confirmInlineAdd() {
      if (!this.inlineAddName.trim() || !this.inlineAddContext) return;
      
      const { sectionIndex, blockIndex, subsectionIndex } = this.inlineAddContext;
      const block = this.detailSections[sectionIndex].subsections[subsectionIndex].blocks[blockIndex];
      
      // 确保 treeData 存在
      if (!block.treeData || !Array.isArray(block.treeData)) {
        block.treeData = [];
      }
      
      // 处理名称和类型
      let finalName = this.inlineAddName.trim();
      let nodeType = this.inlineAddType;

      if (nodeType === 'folder' && !finalName.endsWith('/')) {
        finalName += '/';
      } else if (nodeType === 'file' && finalName.endsWith('/')) {
        finalName = finalName.slice(0, -1);
      }

      const newNode = {
        name: finalName,
        type: nodeType,
        children: nodeType === 'folder' ? [] : undefined
      };

      // 直接添加到treeData
      block.treeData.push(newNode);
      
      // 重置状态
      this.cancelInlineAdd();
    },
    
    // 取消内联添加
    cancelInlineAdd() {
      this.inlineAddMode = false;
      this.inlineAddName = '';
      this.inlineAddType = 'file';
      this.inlineAddContext = null;
    },

    // 检测子节点类型
    detectChildNodeType() {
      if (this.childInlineAddName.endsWith('/')) {
        this.childInlineAddType = 'folder';
      } else if (this.childInlineAddName.includes('.')) {
        this.childInlineAddType = 'file';
      } else {
        this.childInlineAddType = 'file';
      }
    },

    // 确认子节点内联添加
    confirmChildInlineAdd() {
      if (!this.childInlineAddName.trim() || !this.childInlineAddContext) return;

      const { sectionIndex, blockIndex, subsectionIndex, nodeIndex, childIndex } = this.childInlineAddContext;
      const block = subsectionIndex !== null 
        ? this.detailSections[sectionIndex].subsections[subsectionIndex].blocks[blockIndex]
        : this.detailSections[sectionIndex].blocks[blockIndex];

      // 处理名称和类型
      let finalName = this.childInlineAddName.trim();
      let nodeType = this.childInlineAddType;

      if (nodeType === 'folder' && !finalName.endsWith('/')) {
        finalName += '/';
      } else if (nodeType === 'file' && finalName.endsWith('/')) {
        finalName = finalName.slice(0, -1);
      }

      const newNode = {
        name: finalName,
        type: nodeType,
        children: nodeType === 'folder' ? [] : undefined
      };

      // 确定父节点
      let parentNode;
      if (childIndex !== null) {
        // 添加到子节点的子节点
        parentNode = block.treeData[nodeIndex].children[childIndex];
      } else {
        // 添加到根节点的子节点
        parentNode = block.treeData[nodeIndex];
      }

      // 确保父节点存在children数组
      if (!parentNode.children) {
        parentNode.children = [];
      }

      // 添加到父节点的children
      parentNode.children.push(newNode);
      
      // 重置状态
      this.cancelChildInlineAdd();
    },

    // 取消子节点内联添加
    cancelChildInlineAdd() {
      this.childInlineAddMode = false;
      this.childInlineAddName = '';
      this.childInlineAddType = 'file';
      this.childInlineAddContext = null;
    },

    // 添加孙子节点（子节点的子节点）
    addGrandChildNode(sectionIndex, blockIndex, nodeIndex, childIndexOrSubIndex, childIndex = null, grandChildIndex = null) {
      console.log('addGrandChildNode called with:', { sectionIndex, blockIndex, nodeIndex, childIndexOrSubIndex, childIndex, grandChildIndex });
      
      // 处理两种不同的调用方式
      let actualChildIndex;
      let subsectionIndex = null;
      
      if (childIndex !== null) {
        // 6参数调用：(sectionIndex, blockIndex, nodeIndex, subIndex, childIndex, grandChildIndex)
        subsectionIndex = childIndexOrSubIndex;
        actualChildIndex = childIndex;
      } else {
        // 4参数调用：(sectionIndex, blockIndex, nodeIndex, childIndex)
        actualChildIndex = childIndexOrSubIndex;
      }
      
      console.log('Processed parameters:', { actualChildIndex, subsectionIndex });
      
      // 设置孙子节点内联添加状态
      this.grandChildInlineAddContext = {
        sectionIndex,
        blockIndex,
        nodeIndex,
        childIndex: actualChildIndex,
        subsectionIndex,
        name: '',
        type: 'file'
      };
      
      console.log('Set grandChildInlineAddContext:', this.grandChildInlineAddContext);
      
      // 下一帧聚焦输入框
      this.$nextTick(() => {
        const input = this.$refs.grandChildInlineInput || this.$refs.grandChildInlineAddInput;
        console.log('Looking for input refs:', { grandChildInlineInput: this.$refs.grandChildInlineInput, grandChildInlineAddInput: this.$refs.grandChildInlineAddInput });
        if (input) {
          input.focus();
          console.log('Input focused successfully');
        } else {
          console.log('No input found to focus');
        }
      });
    },

    // 更新孙子节点名称
    updateGrandChildNodeName(sectionIndex, blockIndex, nodeIndex, childIndex, grandChildIndex, event, subsectionIndex = null) {
      const newName = event.target.textContent.trim();
      if (!newName) return;

      const block = subsectionIndex !== null 
        ? this.detailSections[sectionIndex].subsections[subsectionIndex].blocks[blockIndex]
        : this.detailSections[sectionIndex].blocks[blockIndex];

      if (block.treeData[nodeIndex] && 
          block.treeData[nodeIndex].children && 
          block.treeData[nodeIndex].children[childIndex] &&
          block.treeData[nodeIndex].children[childIndex].children &&
          block.treeData[nodeIndex].children[childIndex].children[grandChildIndex]) {
        
        const grandChild = block.treeData[nodeIndex].children[childIndex].children[grandChildIndex];
        
        // 根据名称判断类型
        if (newName.endsWith('/')) {
          grandChild.type = 'folder';
          grandChild.name = newName;
          if (!grandChild.children) {
            grandChild.children = [];
          }
        } else {
          grandChild.type = 'file';
          grandChild.name = newName;
          delete grandChild.children;
        }
      }
    },

    // 确认孙子节点内联添加
    confirmGrandChildInlineAdd() {
      if (!this.grandChildInlineAddContext || !this.grandChildInlineAddContext.name.trim()) {
        return;
      }

      const { sectionIndex, blockIndex, nodeIndex, childIndex, subsectionIndex } = this.grandChildInlineAddContext;
      const block = subsectionIndex !== null 
        ? this.detailSections[sectionIndex].subsections[subsectionIndex].blocks[blockIndex]
        : this.detailSections[sectionIndex].blocks[blockIndex];

      const parentNode = block.treeData[nodeIndex].children[childIndex];
      
      if (!parentNode.children) {
        parentNode.children = [];
      }

      const newNode = {
        name: this.grandChildInlineAddContext.name,
        type: this.grandChildInlineAddContext.type
      };

      if (newNode.type === 'folder') {
        newNode.children = [];
      }

      parentNode.children.push(newNode);
      
      // 重置状态
      this.cancelGrandChildInlineAdd();
    },

    // 取消孙子节点内联添加
    cancelGrandChildInlineAdd() {
      this.grandChildInlineAddContext = null;
    },

    // 检测孙子节点类型
    detectGrandChildType() {
      if (this.grandChildInlineAddContext) {
        const name = this.grandChildInlineAddContext.name;
        if (name.endsWith('/')) {
          this.grandChildInlineAddContext.type = 'folder';
        } else {
          this.grandChildInlineAddContext.type = 'file';
        }
      }
    },

    // 添加子节点
    addChildNode(sectionIndex, blockIndex, nodeIndex, subsectionIndex = null, childIndex = null) {
      const block = subsectionIndex !== null 
        ? this.detailSections[sectionIndex].subsections[subsectionIndex].blocks[blockIndex]
        : this.detailSections[sectionIndex].blocks[blockIndex]
      
      let parentNode;
      if (childIndex !== null) {
        // 添加到子节点的子节点
        parentNode = block.treeData[nodeIndex].children[childIndex];
      } else {
        // 添加到根节点的子节点
        parentNode = block.treeData[nodeIndex];
      }
      
      // 确保父节点是文件夹类型
      if (!parentNode || parentNode.type !== 'folder') {
        alert('只能在文件夹中添加子项');
        return;
      }
      
      // 启动子节点内联添加模式
      this.childInlineAddMode = true;
      this.childInlineAddName = '';
      this.childInlineAddType = 'file';
      this.childInlineAddContext = {
        sectionIndex,
        blockIndex,
        subsectionIndex,
        nodeIndex,
        childIndex
      };
      
      // 聚焦到输入框
      this.$nextTick(() => {
        const input = this.$refs.childInlineAddInput;
        if (input) {
          input.focus();
        }
      });
    },

    // 更新节点名称
    updateNodeName(sectionIndex, blockIndex, nodeIndex, event, subsectionIndex = null) {
      const block = subsectionIndex !== null 
        ? this.detailSections[sectionIndex].subsections[subsectionIndex].blocks[blockIndex]
        : this.detailSections[sectionIndex].blocks[blockIndex]
      
      const newName = event.target.textContent.trim()
      if (newName && block.treeData[nodeIndex]) {
        block.treeData[nodeIndex].name = newName
      }
    },

    // 更新子节点名称
    updateChildNodeName(sectionIndex, blockIndex, nodeIndex, childIndex, event, subsectionIndex = null) {
      const block = subsectionIndex !== null 
        ? this.detailSections[sectionIndex].subsections[subsectionIndex].blocks[blockIndex]
        : this.detailSections[sectionIndex].blocks[blockIndex]
      
      const newName = event.target.textContent.trim()
      if (newName && block.treeData[0].children[nodeIndex] && block.treeData[0].children[nodeIndex].children[childIndex]) {
        block.treeData[0].children[nodeIndex].children[childIndex].name = newName
      }
    },

    // Markdown生成和预览
    generateMarkdown() {
      // 使用数据集名称作为标题，如果没有则使用默认标题
      let markdown = `# ${this.formData.name || '数据集名称'}\n\n`
      
      this.detailSections.forEach(section => {
        // 添加二级标题
        markdown += `## ${section.title}\n\n`
        
        if (section.subsections) {
          // 有子章节的情况
          section.subsections.forEach(subsection => {
            markdown += `### ${subsection.title}\n\n`
            markdown += this.generateBlocksMarkdown(subsection.blocks)
          })
        } else {
          // 没有子章节的情况
          markdown += this.generateBlocksMarkdown(section.blocks)
        }
      })
      
      return markdown.trim() + '\n'
    },

    // 生成用于后端提交的标准Markdown格式
    generateStandardMarkdown() {
      let markdown = `# ${this.formData.name || '数据集名称'}\n\n`
      
      this.detailSections.forEach(section => {
        // 添加二级标题
        markdown += `## ${section.title}\n\n`
        
        if (section.subsections) {
          // 有子章节的情况
          section.subsections.forEach(subsection => {
            markdown += `### ${subsection.title}\n\n`
            markdown += this.generateStandardBlocksMarkdown(subsection.blocks)
          })
        } else {
          // 没有子章节的情况
          markdown += this.generateStandardBlocksMarkdown(section.blocks)
        }
      })
      
      return markdown.trim() + '\n'
    },

    // 生成标准格式的块内容（用于后端）
    generateStandardBlocksMarkdown(blocks) {
      let markdown = ''
      
      blocks.forEach(block => {
        switch (block.type) {
          case 'text':
            if (block.content && block.content.trim()) {
              // 将HTML富文本内容转换为标准Markdown，保留颜色标签
              const markdownText = this.htmlToStandardMarkdown(block.content.trim())
              markdown += `${markdownText}\n\n`
            }
            break
            
          case 'image-upload':
            if (block.url) {
              // 添加图片，使用实际的文件名而不是占位符
              const alt = block.description || block.caption || '图片'
              const imageName = block.url.split('/').pop() || 'image.png'
              markdown += `![${alt}](${imageName})\n\n`
              
              // 添加图注
              if (block.caption && block.caption.trim()) {
                markdown += `>${block.caption.trim()}\n\n`
              }
            }
            break
            
          case 'file-tree':
            if (block.treeData && block.treeData.length > 0) {
              // 添加文件树说明
              if (block.description && block.description.trim()) {
                const description = this.htmlToStandardMarkdown(block.description.trim())
                markdown += `${description}\n\n`
              }
              
              // 生成文件树markdown
              markdown += '```plaintext\n'
              markdown += this.generateTreeMarkdown(block.treeData[0], 0)
              markdown += '```\n\n'
            }
            break
            
          case 'code':
            if (block.code && block.code.trim()) {
              // 添加代码说明
              if (block.description && block.description.trim()) {
                const description = this.htmlToStandardMarkdown(block.description.trim())
                markdown += `${description}\n\n`
              }
              
              // 添加代码块
              markdown += `\`\`\`${block.language || 'python'}\n`
              markdown += `${block.code.trim()}\n`
              markdown += '```\n\n'
            }
            break
            
          case 'download':
            if (block.url && block.url.trim()) {
              // 添加下载说明
              if (block.description && block.description.trim()) {
                const description = this.htmlToStandardMarkdown(block.description.trim())
                markdown += `${description}\n\n`
              }
              
              // 添加下载链接
              const title = block.title && block.title.trim() ? block.title.trim() : '下载链接'
              markdown += `- [${title}](${block.url.trim()})`
              
              // 添加备注
              if (block.note && block.note.trim()) {
                markdown += ` （${block.note.trim()}）`
              }
              markdown += '\n\n'
            }
            break
        }
      })
      
      return markdown
    },

    generateBlocksMarkdown(blocks) {
      let markdown = ''
      
      blocks.forEach(block => {
        switch (block.type) {
          case 'text':
            if (block.content && block.content.trim()) {
              // 将HTML富文本内容转换为Markdown
              const markdownText = this.htmlToMarkdown(block.content.trim())
              markdown += `${markdownText}\n\n`
            }
            break
            
          case 'image-upload':
            if (block.url) {
              // 添加图片，使用description作为alt文本，如果没有则使用caption
              const alt = block.description || block.caption || '图片'
              // 在markdown预览中始终使用占位符，而不是真实的图片URL
              const imageUrl = 'placeholder_image.png'
              markdown += `![${alt}](${imageUrl})\n\n`
              
              // 添加图注
              if (block.caption && block.caption.trim()) {
                markdown += `>${block.caption.trim()}\n\n`
              }
            }
            break
            
          case 'file-tree':
            if (block.treeData && block.treeData.length > 0) {
              // 添加文件树说明
              if (block.description && block.description.trim()) {
                markdown += `${block.description.trim()}\n\n`
              }
              
              // 生成文件树markdown
              markdown += '```plaintext\n'
              markdown += this.generateTreeMarkdown(block.treeData[0], 0)
              markdown += '```\n\n'
            }
            break
            
          case 'code':
            if (block.code && block.code.trim()) {
              // 添加代码说明
              if (block.description && block.description.trim()) {
                markdown += `${block.description.trim()}\n\n`
              }
              
              // 添加代码块
              markdown += `\`\`\`${block.language || 'plaintext'}\n`
              markdown += `${block.code.trim()}\n`
              markdown += '```\n\n'
            }
            break
            
          case 'download':
            if (block.url && block.url.trim()) {
              // 添加下载说明
              if (block.description && block.description.trim()) {
                markdown += `${block.description.trim()}\n\n`
              }
              
              // 添加下载链接
              const title = block.title && block.title.trim() ? block.title.trim() : '下载链接'
              markdown += `[${title}](${block.url.trim()})\n\n`
              
              // 添加备注
              if (block.note && block.note.trim()) {
                markdown += `> **备注：** ${block.note.trim()}\n\n`
              }
            }
            break
        }
      })
      
      return markdown
    },
    
    // 生成文件树markdown（与VOC2007.md格式完全一致）
    generateTreeMarkdown(node, depth, isLast = true, parentPrefix = '') {
      let markdown = ''
      
      // 根据深度和位置生成前缀
      let prefix = ''
      if (depth === 0) {
        prefix = ''
      } else {
        prefix = parentPrefix + (isLast ? '└── ' : '├── ')
      }
      
      if (node.type === 'folder') {
        markdown += `${prefix}${node.name}/`
        // 添加注释（如果有）
        if (node.comment && node.comment.trim()) {
          markdown += `\t# ${node.comment.trim()}`
        }
        markdown += '\n'
        
        if (node.children && node.children.length > 0) {
          const childPrefix = parentPrefix + (isLast ? '    ' : '│   ')
          node.children.forEach((child, index) => {
            const isChildLast = index === node.children.length - 1
            
            // 如果不是最后一个节点且当前节点有子节点，添加分隔线
            if (!isChildLast && child.children && child.children.length > 0) {
              // 添加垂直分隔线
              for (let i = 0; i <= depth; i++) {
                markdown += '│'
                if (i < depth) {
                  markdown += '   '
                }
              }
              markdown += '\n'
            }
            
            markdown += this.generateTreeMarkdown(child, depth + 1, isChildLast, childPrefix)
          })
        }
      } else {
        markdown += `${prefix}${node.name}`
        // 添加注释（如果有）
        if (node.comment && node.comment.trim()) {
          markdown += `\t# ${node.comment.trim()}`
        }
        markdown += '\n'
      }
      
      return markdown
    },
    previewMarkdown() {
      this.generatedMarkdown = this.generateMarkdown()
      this.showPreview = true
    },

    // 预览HTML内容
    previewHTML() {
      this.generatedHTML = this.generateHTML()
      this.showHTMLPreview = true
    },

    closeHTMLPreview() {
      this.showHTMLPreview = false
    },
    closePreview() {
      this.showPreview = false
    },
    
    // 获取用户token
    getUserToken() {
      // 从localStorage获取用户token
      const token = localStorage.getItem('userToken') || localStorage.getItem('token')
      if (!token) {
        throw new Error('用户未登录，请先登录')
      }
      return token
    },

    // 获取用户token状态（用于调试显示）
    getUserTokenStatus() {
      try {
        const token = localStorage.getItem('userToken') || localStorage.getItem('token')
        if (!token) {
          return '✗ 未登录'
        }
        return `✓ 已登录 (${token.substring(0, 10)}...)`
      } catch (error) {
        return '✗ 获取失败'
      }
    },

    // 提交数据集
    async submitDataset() {
      // 调试输出：查看完整的上传数据结构
      console.log('=== 数据集上传调试信息 ===')
      console.log('1. 基本表单数据:', {
        name: this.formData.name,
        description: this.formData.description,
        taskIds: this.formData.taskIds
      })
      console.log('2. 真实下载地址:', this.formData.trueAddress)
      console.log('3. 详情章节数据结构:', JSON.stringify(this.detailSections, null, 2))
      console.log('4. 生成的HTML内容:', this.generateHTML())
      
      // 收集图片文件信息
      const imageFiles = this.collectImageFiles()
      console.log('5. 收集到的图片文件:', imageFiles.map(file => ({
        name: file.name,
        size: file.size,
        type: file.type
      })))
      
      console.log('6. 图片文件统计:', {
        imageFiles: imageFiles.length
      })
      console.log('========================')
      
      if (!this.isFormValid) {
        alert('请填写必填信息')
        return
      }
      
      try {
        // 获取用户token
        const userToken = this.getUserToken()
        
        // 生成HTML格式用于后端转换为Markdown
        const htmlContent = this.generateHTML()
        
        // 创建FormData对象用于文件上传
        const formData = new FormData()
        
        // 添加基本信息
        formData.append('datasetName', this.formData.name)
        formData.append('description', this.formData.description || '')
        formData.append('taskIds', JSON.stringify(this.formData.taskIds))
        if (this.uploadMode === 'editor') {
          formData.append('htmlContent', htmlContent)
        }
        
        formData.append('trueAddress', this.formData.trueAddress || '')
        formData.append('sizeFormatted', this.formData.sizeFormatted || '')
        
        // 收集并添加图片文件（从各个block中收集）
        if (this.uploadMode === 'editor') {
          const imageFiles = this.collectImageFiles()
          imageFiles.forEach((imageFile) => {
            formData.append('files', imageFile)
          })
        } else {
          if (this.markdownFile) {
            formData.append('markdownFile', this.markdownFile)
          }
          this.markdownImages.forEach((img) => {
            formData.append('files', img)
          })
        }
        
        // 输出文件统计信息
        console.log(`准备上传文件 - 图片文件: ${imageFiles.length}个`)
        console.log('图片文件列表:', imageFiles.map(f => f.name))
        
        // 调用API提交数据
        const response = await fetch('http://localhost:8080/datasets/upload', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${userToken}`,
            // 注意：使用FormData时不要设置Content-Type，让浏览器自动设置
          },
          body: formData
        })
        
        if (!response.ok) {
          const errorData = await response.json()
          throw new Error(errorData.message || '上传失败')
        }
        
        const payload = await response.json()
        if (payload.code !== 200) {
          throw new Error(payload.message || '上传失败')
        }
        const data = payload.data || {}
        console.log('数据集上传成功:', data)
        alert('数据集上传成功！')

        // 重置表单
        this.resetForm()

        // 跳转到数据集列表页
        this.$router.push('/datasets')
        
      } catch (error) {
        console.error('上传失败:', error)
        if (error.message.includes('用户未登录')) {
          alert('用户未登录，请先登录')
          // 跳转到登录页
          this.$router.push('/login')
        } else {
          alert(`上传失败：${error.message}`)
        }
      }
    },
    goBack() {
      this.$router.push('/datasets')
    },

    // 生成HTML格式内容（用于后端转换为Markdown）
    generateHTML() {
      let html = `<h1>${this.formData.name || '数据集名称'}</h1>\n\n`
      
      this.detailSections.forEach(section => {
        // 添加二级标题
        html += `<h2>${section.title}</h2>\n\n`
        
        if (section.subsections) {
          // 有子章节的情况
          section.subsections.forEach(subsection => {
            html += `<h3>${subsection.title}</h3>\n\n`
            html += this.generateBlocksHTML(subsection.blocks)
          })
        } else {
          // 没有子章节的情况
          html += this.generateBlocksHTML(section.blocks)
        }
      })
      
      return html.trim()
    },

    // 生成块内容的HTML格式
    generateBlocksHTML(blocks) {
      let html = ''
      
      blocks.forEach(block => {
        switch (block.type) {
          case 'text':
            if (block.content && block.content.trim()) {
              // 直接使用富文本编辑器的HTML内容，确保保留颜色标签
              html += `<div class="text-content">${block.content.trim()}</div>\n\n`
            }
            break
            
          case 'image-upload':
            if (block.url) {
              // 生成图片HTML，使用实际的文件名
              const alt = block.description || block.caption || '图片'
              const imageName = block.file ? block.file.name : 'image.png'
              html += `<div class="image-content">\n`
              html += `  <img src="${imageName}" alt="${alt}" />\n`
              
              // 添加图注
              if (block.caption && block.caption.trim()) {
                html += `  <blockquote class="image-caption">${block.caption.trim()}</blockquote>\n`
              }
              html += `</div>\n\n`
            }
            break
            
          case 'file-tree':
            if (block.treeData && block.treeData.length > 0) {
              html += `<div class="file-tree-content">\n`
              
              // 添加文件树说明
              if (block.description && block.description.trim()) {
                html += `  <div class="tree-description">${block.description.trim()}</div>\n\n`
              }
              
              // 生成文件树HTML，使用特殊标记便于后端识别
              html += `  <pre class="file-tree">\n`
              html += this.generateTreeHTML(block.treeData[0], 0)
              html += `  </pre>\n`
              html += `</div>\n\n`
            }
            break
            
          case 'code':
            if (block.code && block.code.trim()) {
              html += `<div class="code-content">\n`
              
              // 添加代码说明
              if (block.description && block.description.trim()) {
                html += `  <div class="code-description">${block.description.trim()}</div>\n\n`
              }
              
              // 添加代码块，使用data属性标记语言
              html += `  <pre class="code-block" data-language="${block.language || 'python'}">\n`
              html += `    <code>${this.escapeHtml(block.code.trim())}</code>\n`
              html += `  </pre>\n`
              html += `</div>\n\n`
            }
            break
            
          case 'download':
            if (block.url && block.url.trim()) {
              html += `<div class="download-content">\n`
              
              // 添加下载说明
              if (block.description && block.description.trim()) {
                html += `  <div class="download-description">${block.description.trim()}</div>\n\n`
              }
              
              // 添加下载链接
              const title = block.title && block.title.trim() ? block.title.trim() : '下载链接'
              html += `  <ul class="download-list">\n`
              html += `    <li><a href="${block.url.trim()}">${title}</a>`
              
              // 添加备注
              if (block.note && block.note.trim()) {
                html += ` <span class="download-note">（${block.note.trim()}）</span>`
              }
              html += `</li>\n`
              html += `  </ul>\n`
              html += `</div>\n\n`
            }
            break
        }
      })
      
      return html
    },

    // 生成文件树HTML（保持与VOC2007.md一致的格式）
    generateTreeHTML(node, depth, isLast = true, parentPrefix = '') {
      let html = ''
      
      // 根据深度和位置生成前缀
      let prefix = ''
      if (depth === 0) {
        prefix = ''
      } else {
        prefix = parentPrefix + (isLast ? '└── ' : '├── ')
      }
      
      if (node.type === 'folder') {
        html += `${prefix}${node.name}/`
        // 添加注释（如果有）
        if (node.comment && node.comment.trim()) {
          html += `\t# ${node.comment.trim()}`
        }
        html += '\n'
        
        if (node.children && node.children.length > 0) {
          const childPrefix = parentPrefix + (isLast ? '    ' : '│   ')
          node.children.forEach((child, index) => {
            const isChildLast = index === node.children.length - 1
            
            // 如果不是最后一个节点且当前节点有子节点，添加分隔线
            if (!isChildLast && child.children && child.children.length > 0) {
              // 添加垂直分隔线
              for (let i = 0; i <= depth; i++) {
                html += '│'
                if (i < depth) {
                  html += '   '
                }
              }
              html += '\n'
            }
            
            html += this.generateTreeHTML(child, depth + 1, isChildLast, childPrefix)
          })
        }
      } else {
        html += `${prefix}${node.name}`
        // 添加注释（如果有）
        if (node.comment && node.comment.trim()) {
          html += `\t# ${node.comment.trim()}`
        }
        html += '\n'
      }
      
      return html
    },

    // HTML转义函数
    escapeHtml(text) {
      const div = document.createElement('div')
      div.textContent = text
      return div.innerHTML
    },
    
    // 富文本编辑器方法
    formatText(command, sectionIndex, subsectionIndex, blockIndex) {
      document.execCommand(command, false, null)
      this.updateBlockContent(sectionIndex, subsectionIndex, blockIndex)
    },
    
    insertLink(sectionIndex, subsectionIndex, blockIndex) {
      const url = prompt('请输入链接地址:')
      if (url) {
        document.execCommand('createLink', false, url)
        this.updateBlockContent(sectionIndex, subsectionIndex, blockIndex)
      }
    },

    insertHeading(sectionIndex, subsectionIndex, blockIndex, level) {
      const selection = window.getSelection()
      if (selection.rangeCount > 0) {
        const range = selection.getRangeAt(0)
        const selectedText = range.toString()
        
        if (selectedText) {
          const headingElement = document.createElement(`h${level}`)
          headingElement.textContent = selectedText
          range.deleteContents()
          range.insertNode(headingElement)
          
          // 清除选择
          selection.removeAllRanges()
          
          this.updateBlockContent(sectionIndex, subsectionIndex, blockIndex)
        } else {
          alert('请先选择要设置为标题的文本')
        }
      }
    },

    insertCode(sectionIndex, subsectionIndex, blockIndex) {
      const selection = window.getSelection()
      if (selection.rangeCount > 0) {
        const range = selection.getRangeAt(0)
        const selectedText = range.toString()
        
        if (selectedText) {
          const codeElement = document.createElement('code')
          codeElement.textContent = selectedText
          range.deleteContents()
          range.insertNode(codeElement)
          
          // 清除选择
          selection.removeAllRanges()
          
          this.updateBlockContent(sectionIndex, subsectionIndex, blockIndex)
        } else {
          const code = prompt('请输入代码内容:')
          if (code) {
            const codeElement = document.createElement('code')
            codeElement.textContent = code
            range.insertNode(codeElement)
            this.updateBlockContent(sectionIndex, subsectionIndex, blockIndex)
          }
        }
      }
    },

    insertCodeBlock(sectionIndex, subsectionIndex, blockIndex) {
      const code = prompt('请输入代码块内容:')
      if (code) {
        const preElement = document.createElement('pre')
        const codeElement = document.createElement('code')
        codeElement.textContent = code
        preElement.appendChild(codeElement)
        
        const selection = window.getSelection()
        if (selection.rangeCount > 0) {
          const range = selection.getRangeAt(0)
          range.insertNode(preElement)
          this.updateBlockContent(sectionIndex, subsectionIndex, blockIndex)
        }
      }
    },

    insertQuote(sectionIndex, subsectionIndex, blockIndex) {
       const selection = window.getSelection()
       if (selection.rangeCount > 0) {
         const range = selection.getRangeAt(0)
         const selectedText = range.toString()
         
         if (selectedText) {
           const blockquoteElement = document.createElement('blockquote')
           blockquoteElement.textContent = selectedText
           range.deleteContents()
           range.insertNode(blockquoteElement)
           
           // 清除选择
           selection.removeAllRanges()
           
           this.updateBlockContent(sectionIndex, subsectionIndex, blockIndex)
         } else {
           const quote = prompt('请输入引用内容:')
           if (quote) {
             const blockquoteElement = document.createElement('blockquote')
             blockquoteElement.textContent = quote
             range.insertNode(blockquoteElement)
             this.updateBlockContent(sectionIndex, subsectionIndex, blockIndex)
           }
         }
       }
     },

     toggleMarkdownPreview(sectionIndex, subsectionIndex, blockIndex) {
       const key = `${sectionIndex}-${subsectionIndex}-${blockIndex}`
       this.$set(this.markdownPreviewStates, key, !this.markdownPreviewStates[key])
     },

     isMarkdownPreview(sectionIndex, subsectionIndex, blockIndex) {
       const key = `${sectionIndex}-${subsectionIndex}-${blockIndex}`
       return this.markdownPreviewStates[key] || false
     },
    
    updateTextContent(event, sectionIndex, subsectionIndex, blockIndex) {
      // 使用防抖机制，避免频繁更新导致光标重置
      clearTimeout(this.inputTimeout)
      this.inputTimeout = setTimeout(() => {
        const content = event.target.innerHTML
        this.updateBlockContentValue(sectionIndex, subsectionIndex, blockIndex, content)
      }, 500) // 500ms后更新数据
    },
    
    saveTextContent(event, sectionIndex, subsectionIndex, blockIndex) {
      const content = event.target.innerHTML
      this.updateBlockContentValue(sectionIndex, subsectionIndex, blockIndex, content)
    },
    
    updateBlockContent(sectionIndex, subsectionIndex, blockIndex) {
      this.$nextTick(() => {
        const selection = window.getSelection()
        if (selection.rangeCount > 0) {
          const range = selection.getRangeAt(0)
          const container = range.commonAncestorContainer
          const editorElement = container.nodeType === Node.TEXT_NODE 
            ? container.parentElement.closest('.editor-content')
            : container.closest('.editor-content')
          
          if (editorElement) {
            const content = editorElement.innerHTML
            this.updateBlockContentValue(sectionIndex, subsectionIndex, blockIndex, content)
          }
        }
      })
    },
    
    updateBlockContentValue(sectionIndex, subsectionIndex, blockIndex, content) {
      if (subsectionIndex !== null) {
        // 更新子章节的块
        this.detailSections[sectionIndex].subsections[subsectionIndex].blocks[blockIndex].content = content
      } else {
        // 更新主章节的块
        this.detailSections[sectionIndex].blocks[blockIndex].content = content
      }
    },

    // 新的富文本编辑器内容更新方法
    updateBlockContent(sectionIndex, subsectionIndex, blockIndex, content) {
      this.updateBlockContentValue(sectionIndex, subsectionIndex, blockIndex, content)
    },

    // HTML到Markdown转换方法（用于预览）
    htmlToMarkdown(html) {
      if (!html) return ''
      
      // 创建临时DOM元素来解析HTML
      const tempDiv = document.createElement('div')
      tempDiv.innerHTML = html
      
      return this.convertNodeToMarkdown(tempDiv)
    },

    // HTML到标准Markdown转换方法（用于后端提交）
    htmlToStandardMarkdown(html) {
      if (!html) return ''
      
      // 创建临时DOM元素来解析HTML
      const tempDiv = document.createElement('div')
      tempDiv.innerHTML = html
      
      return this.convertNodeToStandardMarkdown(tempDiv)
    },

    convertNodeToMarkdown(node) {
      let markdown = ''
      
      for (let child of node.childNodes) {
        if (child.nodeType === Node.TEXT_NODE) {
          markdown += child.textContent
        } else if (child.nodeType === Node.ELEMENT_NODE) {
          const tagName = child.tagName.toLowerCase()
          const childContent = this.convertNodeToMarkdown(child)
          
          switch (tagName) {
             case 'strong':
             case 'b':
               markdown += `**${childContent}**`
               break
             case 'em':
             case 'i':
               markdown += `*${childContent}*`
               break
             case 'u':
               markdown += `<u>${childContent}</u>`
               break
             case 'strike':
             case 's':
               markdown += `~~${childContent}~~`
               break
             case 'h1':
               markdown += `# ${childContent}\n\n`
               break
             case 'h2':
               markdown += `## ${childContent}\n\n`
               break
             case 'h3':
               markdown += `### ${childContent}\n\n`
               break
             case 'h4':
               markdown += `#### ${childContent}\n\n`
               break
             case 'h5':
               markdown += `##### ${childContent}\n\n`
               break
             case 'h6':
               markdown += `###### ${childContent}\n\n`
               break
             case 'code':
               markdown += `\`${childContent}\``
               break
             case 'pre':
               // 检查是否包含code标签
               const codeChild = child.querySelector('code')
               if (codeChild) {
                 markdown += `\n\`\`\`\n${codeChild.textContent}\n\`\`\`\n\n`
               } else {
                 markdown += `\n\`\`\`\n${childContent}\n\`\`\`\n\n`
               }
               break
             case 'blockquote':
               const lines = childContent.split('\n')
               markdown += lines.map(line => `> ${line}`).join('\n') + '\n\n'
               break
             case 'a':
               const href = child.getAttribute('href') || ''
               markdown += `[${childContent}](${href})`
               break
             case 'ul':
               markdown += '\n' + this.convertListToMarkdown(child, false) + '\n'
               break
             case 'ol':
               markdown += '\n' + this.convertListToMarkdown(child, true) + '\n'
               break
             case 'li':
               // 这个会在convertListToMarkdown中处理
               markdown += childContent
               break
             case 'br':
               markdown += '\n'
               break
             case 'p':
               markdown += childContent + '\n\n'
               break
             case 'div':
               markdown += childContent + '\n'
               break
             default:
               // 检查是否有颜色样式
               const style = child.getAttribute('style')
               if (style && style.includes('color:')) {
                 const colorMatch = style.match(/color:\s*([^;]+)/)
                 if (colorMatch) {
                   const color = colorMatch[1].trim()
                   // 使用HTML标签保留颜色信息，因为标准Markdown不支持颜色
                   markdown += `<span style="color: ${color}">${childContent}</span>`
                 } else {
                   markdown += childContent
                 }
               } else {
                 markdown += childContent
               }
           }
        }
      }
      
      return markdown
    },

    // 转换为标准Markdown格式（用于后端提交）
    convertNodeToStandardMarkdown(node) {
      let markdown = ''
      
      for (let child of node.childNodes) {
        if (child.nodeType === Node.TEXT_NODE) {
          markdown += child.textContent
        } else if (child.nodeType === Node.ELEMENT_NODE) {
          const tagName = child.tagName.toLowerCase()
          const childContent = this.convertNodeToStandardMarkdown(child)
          
          switch (tagName) {
             case 'strong':
             case 'b':
               markdown += `**${childContent}**`
               break
             case 'em':
             case 'i':
               markdown += `*${childContent}*`
               break
             case 'u':
               markdown += `<u>${childContent}</u>`
               break
             case 'strike':
             case 's':
               markdown += `~~${childContent}~~`
               break
             case 'h1':
               markdown += `# ${childContent}\n\n`
               break
             case 'h2':
               markdown += `## ${childContent}\n\n`
               break
             case 'h3':
               markdown += `### ${childContent}\n\n`
               break
             case 'h4':
               markdown += `#### ${childContent}\n\n`
               break
             case 'h5':
               markdown += `##### ${childContent}\n\n`
               break
             case 'h6':
               markdown += `###### ${childContent}\n\n`
               break
             case 'code':
               markdown += `\`${childContent}\``
               break
             case 'pre':
               // 检查是否包含code标签
               const codeChild = child.querySelector('code')
               if (codeChild) {
                 markdown += `\n\`\`\`\n${codeChild.textContent}\n\`\`\`\n\n`
               } else {
                 markdown += `\n\`\`\`\n${childContent}\n\`\`\`\n\n`
               }
               break
             case 'blockquote':
               const lines = childContent.split('\n').filter(line => line.trim())
               markdown += lines.map(line => `> ${line.trim()}`).join('\n') + '\n\n'
               break
             case 'a':
               const href = child.getAttribute('href') || ''
               markdown += `[${childContent}](${href})`
               break
             case 'ul':
               markdown += '\n' + this.convertListToStandardMarkdown(child, false) + '\n'
               break
             case 'ol':
               markdown += '\n' + this.convertListToStandardMarkdown(child, true) + '\n'
               break
             case 'li':
               // 这个会在convertListToStandardMarkdown中处理
               markdown += childContent
               break
             case 'br':
               markdown += '\n'
               break
             case 'p':
               // 处理段落，确保格式正确
               const trimmedContent = childContent.trim()
               if (trimmedContent) {
                 // 检查是否以中文标点开头，如果是则添加制表符
                 if (trimmedContent.match(/^[　\s]*[，。！？；：]/)) {
                   markdown += `\t${trimmedContent}\n\n`
                 } else {
                   markdown += `${trimmedContent}\n\n`
                 }
               }
               break
             case 'div':
               const trimmedDiv = childContent.trim()
               if (trimmedDiv) {
                 markdown += `${trimmedDiv}\n`
               }
               break
             default:
               // 检查是否有颜色样式
               const style = child.getAttribute('style')
               if (style && style.includes('color:')) {
                 const colorMatch = style.match(/color:\s*([^;]+)/)
                 if (colorMatch) {
                   const color = colorMatch[1].trim()
                   // 保留颜色标签，与VOC2007.md格式一致
                   markdown += `<span style="color:${color}">${childContent}</span>`
                 } else {
                   markdown += childContent
                 }
               } else {
                 markdown += childContent
               }
           }
        }
      }
      
      return markdown
    },

    convertListToMarkdown(listNode, isOrdered = false) {
      let markdown = ''
      let index = 1
      
      for (let child of listNode.children) {
        if (child.tagName.toLowerCase() === 'li') {
          const content = this.convertNodeToMarkdown(child).trim()
          if (isOrdered) {
            markdown += `${index}. ${content}\n`
            index++
          } else {
            markdown += `- ${content}\n`
          }
        }
      }
      
      return markdown.trim()
    },

    convertListToStandardMarkdown(listNode, isOrdered = false) {
      let markdown = ''
      let index = 1
      
      for (let child of listNode.children) {
        if (child.tagName.toLowerCase() === 'li') {
          const content = this.convertNodeToStandardMarkdown(child).trim()
          if (isOrdered) {
            markdown += `${index}. ${content}\n`
            index++
          } else {
            markdown += `- ${content}\n`
          }
        }
      }
      
      return markdown.trim()
    },

    // 获取块的Markdown内容
    getBlockMarkdown(blockOrSectionIndex, subsectionIndex, blockIndex) {
      let block
      
      // 如果第一个参数是对象，直接使用
      if (typeof blockOrSectionIndex === 'object' && blockOrSectionIndex !== null) {
        block = blockOrSectionIndex
      } else {
        // 否则按照原来的方式获取block
        if (subsectionIndex !== null) {
          block = this.detailSections[blockOrSectionIndex].subsections[subsectionIndex].blocks[blockIndex]
        } else {
          block = this.detailSections[blockOrSectionIndex].blocks[blockIndex]
        }
      }
      
      if (block.type === 'text') {
        return this.htmlToMarkdown(block.content)
      } else if (block.type === 'image') {
        return `![${block.alt || ''}](${block.src || ''})`
      }
      
      return ''
    },

  resetForm() {
      this.formData = {
        name: '',
        description: '',
        taskIds: [],
        trueAddress: '',
        sizeFormatted: ''
      }
      this.uploadMode = 'editor'
      this.markdownFile = null
      this.markdownImages = []
      this.uploadedFiles = []
      this.detailSections.forEach(section => {
        section.blocks = []
      })
    }
  }
}
</script>

<style scoped>
.dataset-upload {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  text-align: center;
  color: white;
  font-size: 2.5rem;
  margin-bottom: 2rem;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.section {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
}

.section-title {
  color: #333;
  font-size: 1.5rem;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid #667eea;
  padding-bottom: 0.5rem;
}
.mode-toggle { display: flex; gap: 8px; margin-bottom: 12px; }
.toggle-btn { border: 1px solid #667eea; color: #667eea; background: #fff; border-radius: 6px; padding: 6px 10px; cursor: pointer; }
.toggle-btn.active { background: #667eea; color: #fff; }

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #333;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.75rem;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.tags-input {
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  padding: 0.5rem;
  min-height: 3rem;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
}

.tag {
  background: #667eea;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.875rem;
}

.tag.selected {
  background: #28a745;
}

/* 下拉多选样式 */
.task-multiselect { position: relative; }
.selector-input {
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  padding: 0.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  background: #fff;
}
.chips { display: flex; gap: 0.5rem; flex-wrap: wrap; }
.chip { background: #eef2ff; color: #333; padding: 2px 6px; border-radius: 12px; font-size: 12px; }
.caret { color: #666; }
.dropdown {
  position: absolute;
  top: 100%; left: 0; right: 0; margin-top: 6px;
  background: white; border: 1px solid #ddd; border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  z-index: 20; padding: 8px;
}
.search-input {
  width: 100%; padding: 8px; border: 1px solid #e1e5e9; border-radius: 6px; margin-bottom: 8px;
}
.options { max-height: 220px; overflow: auto; }
.option { display: flex; align-items: center; gap: 8px; padding: 6px; border-radius: 6px; }
.option:hover { background: #f8f9fa; }
.dropdown-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px; }
.placeholder { color: #888; }

.tag-remove {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 1rem;
  padding: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tag-input {
  border: none;
  outline: none;
  flex: 1;
  min-width: 120px;
  padding: 0.25rem;
}

.upload-area {
  border: 2px dashed #667eea;
  border-radius: 12px;
  padding: 3rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-area:hover {
  border-color: #5a67d8;
  background: #f7fafc;
}

.upload-content {
  /* 移除 pointer-events: none; 以允许点击事件 */
}

.upload-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.upload-hint {
  color: #666;
  font-size: 0.875rem;
  margin-top: 0.5rem;
}

.file-list {
  margin-top: 1.5rem;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 0.5rem;
}

.file-name {
  font-weight: 500;
  flex: 1;
}

.file-size {
  color: #666;
  font-size: 0.875rem;
  margin-right: 1rem;
}

.file-remove {
  background: #dc3545;
  color: white;
  border: none;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.detail-editor {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 1.5rem;
}

.template-headers {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
  height: fit-content;
  font-size: 0.9rem;
}

.header-item {
  margin-bottom: 1rem;
}

.header-item h4 {
  color: #333;
  margin-bottom: 0.5rem;
}

.sub-headers {
  margin-left: 1rem;
}

.sub-header {
  color: #666;
  font-size: 0.875rem;
  margin-bottom: 0.25rem;
}

.content-editor {
  flex: 1;
}

/* 插块相关样式 */

.blocks-container {
  min-height: 100px;
}

.block-item {
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  margin-bottom: 1rem;
  overflow: hidden;
  background: white;
}

.block-header {
  background: #f8f9fa;
  padding: 0.75rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e1e5e9;
}

.block-type {
  font-weight: 500;
  color: #333;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.block-controls {
  display: flex;
  gap: 0.25rem;
}

.move-btn,
.remove-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.75rem;
  min-width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.move-btn:disabled {
  background: #dee2e6;
  cursor: not-allowed;
}

.remove-btn {
  background: #dc3545;
}

.remove-btn:hover {
  background: #c82333;
}

.block-content {
  padding: 1rem;
}

.block-textarea,
.block-input,
.code-textarea {
  width: 100%;
  border: 1px solid #e1e5e9;
  border-radius: 4px;
  padding: 0.75rem;
  font-family: inherit;
  resize: vertical;
  box-sizing: border-box;
}

.block-input {
  margin-bottom: 0.5rem;
}

.code-header {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  align-items: center;
}

.language-select {
  padding: 0.5rem;
  border: 1px solid #e1e5e9;
  border-radius: 4px;
  background: white;
}

.list-header {
  margin-bottom: 0.5rem;
}

.list-item {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  align-items: center;
}

.remove-item-btn,
.add-item-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.add-item-btn {
  background: #28a745;
  margin-top: 0.5rem;
}

.table-headers,
.table-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  align-items: center;
}

.table-input {
  flex: 1;
  padding: 0.5rem;
  border: 1px solid #e1e5e9;
  border-radius: 4px;
}

.add-col-btn,
.remove-row-btn,
.add-row-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
  white-space: nowrap;
}

.add-col-btn,
.add-row-btn {
  background: #28a745;
}

.empty-blocks {
  text-align: center;
  padding: 2rem;
  color: #666;
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px dashed #dee2e6;
}

.add-block-area {
  text-align: center;
  padding: 1.5rem;
  margin-top: 1rem;
  color: #666;
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px dashed #dee2e6;
}

.clickable-empty {
  cursor: pointer;
  transition: all 0.3s ease;
}

.clickable-empty:hover {
  background: #e9ecef;
  border-color: #667eea;
  color: #495057;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.clickable-empty:active {
  transform: translateY(0);
}

.subsection-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
  padding: 0.75rem;
  background: #f8f9fa;
  border-bottom: 1px solid #e1e5e9;
}

.subsection-header h5 {
  margin: 0;
  color: #333;
}

.section-header-with-buttons {
  margin-bottom: 1rem;
}

.section-editor {
  margin-bottom: 2rem;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  overflow: hidden;
}

.section-header {
  background: #f8f9fa;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e1e5e9;
}

.section-header h4 {
  margin: 0;
  color: #333;
}



.blocks-container {
  padding: 1rem;
}

.content-block {
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  margin-bottom: 1rem;
  overflow: hidden;
}

.block-header {
  background: #f1f3f4;
  padding: 0.75rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.block-header select {
  padding: 0.25rem 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.remove-block-btn {
  background: #dc3545;
  color: white;
  border: none;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.text-block,
.image-block,
.filetree-block,
.code-block {
  padding: 1rem;
}

.text-block textarea,
.code-textarea {
  width: 100%;
  border: 1px solid #e1e5e9;
  border-radius: 4px;
  padding: 0.75rem;
  font-family: inherit;
  resize: vertical;
}

.image-upload-area {
  border: 2px dashed #ccc;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  margin-bottom: 1rem;
}



.uploaded-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: 4px;
}

.image-caption {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.image-caption input {
  flex: 1;
  padding: 0.5rem;
  border: 1px solid #e1e5e9;
  border-radius: 4px;
}

.filetree-editor,
.filetree-preview {
  margin-bottom: 1rem;
}

.filetree-editor textarea {
  width: 100%;
  font-family: 'Courier New', monospace;
  border: 1px solid #e1e5e9;
  border-radius: 4px;
  padding: 0.75rem;
}

.tree-preview {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  border: 1px solid #e1e5e9;
}

/* 新的文件树样式 */
.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e0e0e0;
}

.tree-header h4 {
  margin: 0;
  color: #333;
  font-size: 14px;
  font-weight: 600;
}

.tree-actions {
  display: flex;
  gap: 8px;
}

.tree-btn {
  padding: 4px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.tree-btn:hover {
  background: #f0f0f0;
  border-color: #ccc;
}

.add-btn {
  color: #007bff;
  border-color: #007bff;
}

.add-btn:hover {
  background: #e7f3ff;
}

.tree-container {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  padding: 12px;
  min-height: 120px;
}

.tree-root-name {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-weight: 600;
  color: #333;
}

.tree-root-icon {
  margin-right: 6px;
  color: #ffa500;
}

.tree-content {
  margin-left: 20px;
}

.tree-node {
  margin-bottom: 4px;
}

.tree-item {
  display: flex;
  align-items: center;
  padding: 2px 0;
  position: relative;
}

.tree-item-content {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.tree-connector {
  position: absolute;
  left: -16px;
  top: 0;
  bottom: 0;
  width: 16px;
  border-left: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
}

.tree-connector::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 8px;
  height: 1px;
  background: #ddd;
}

.tree-icon {
  margin-right: 6px;
  width: 16px;
  text-align: center;
  color: #666;
}

.tree-name {
  flex: 1;
  padding: 2px 4px;
  border-radius: 3px;
  cursor: text;
  min-width: 60px;
}

.tree-name:hover {
  background: #f0f0f0;
}

.tree-name:focus {
  outline: 1px solid #007bff;
  background: white;
}

.tree-item-actions {
  display: flex;
  gap: 6px;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.tree-item:hover .tree-item-actions {
  opacity: 1;
}

.tree-action-btn {
  padding: 4px 6px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: #ffffff;
  cursor: pointer;
  font-size: 11px;
  color: #666;
  transition: all 0.2s;
  min-width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tree-action-btn:hover {
  background: #f0f8ff;
  border-color: #1890ff;
  color: #1890ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.tree-action-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

/* 添加子项按钮样式 */
.add-child-btn {
  background: #f6ffed;
  border-color: #b7eb8f;
  color: #52c41a;
}

.add-child-btn:hover {
  background: #52c41a;
  border-color: #52c41a;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);
}

/* 删除按钮样式 */
.remove-btn {
  background: #fff2f0;
  border-color: #ffccc7;
  color: #dc3545;
}

.remove-btn:hover {
  background: #dc3545;
  border-color: #dc3545;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
}

.tree-children {
  margin-left: 20px;
  position: relative;
}

.tree-children::before {
  content: '';
  position: absolute;
  left: -16px;
  top: 0;
  bottom: 20px;
  width: 1px;
  background: #ddd;
}

.empty-state {
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 20px;
}

.empty-icon {
  font-size: 48px;
  color: #ddd;
  margin-bottom: 16px;
}

.quick-add-container {
  margin-top: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px dashed #dee2e6;
}

.quick-add-input-group {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.quick-add-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.quick-add-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.quick-add-type-indicator {
  min-width: 80px;
}

.type-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.folder-badge {
  background: #e3f2fd;
  color: #1976d2;
}

.file-badge {
  background: #f3e5f5;
  color: #7b1fa2;
}

.quick-add-btn {
  padding: 10px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.quick-add-btn:hover:not(:disabled) {
  background: #0056b3;
}

.quick-add-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
}

.quick-add-tips {
  margin-top: 16px;
  text-align: left;
  background: white;
  padding: 12px;
  border-radius: 6px;
  border-left: 4px solid #ffc107;
}

.quick-add-tips p {
  margin: 0 0 8px 0;
  font-weight: 500;
  color: #856404;
  display: flex;
  align-items: center;
  gap: 6px;
}

.quick-add-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #6c757d;
}

.quick-add-tips li {
  margin-bottom: 4px;
  font-size: 13px;
}

.quick-add-tips code {
  background: #f8f9fa;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  color: #e83e8c;
}

.code-settings {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
}

.code-language select,
.code-description input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #e1e5e9;
  border-radius: 4px;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.preview-btn,
.submit-btn {
  padding: 0.75rem 2rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.preview-btn {
  background: #6c757d;
  color: white;
}

.html-preview-btn {
  background: #28a745;
  color: white;
}

.submit-btn {
  background: #667eea;
  color: white;
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 800px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #e1e5e9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
}

.modal-body {
  padding: 1.5rem;
  overflow-y: auto;
  flex: 1;
}

.markdown-preview {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 4px;
  white-space: pre-wrap;
  font-family: 'Courier New', monospace;
  font-size: 0.875rem;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .detail-editor {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .template-headers {
    order: 2;
    margin-top: 1rem;
  }
  
  .content-editor {
    order: 1;
  }
  
  .code-settings {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}

@media (max-width: 1024px) {
  .detail-editor {
    grid-template-columns: 180px 1fr;
    gap: 1rem;
  }
  
  .template-headers {
    font-size: 0.85rem;
    padding: 0.75rem;
  }
}



/* 插块选择弹窗样式 */
.block-selector-modal {
  background: white;
  border-radius: 12px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.block-selector-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  padding: 24px;
  max-height: 60vh;
  overflow-y: auto;
}

.block-type-card {
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  min-height: 140px;
}

.block-type-card:hover {
  border-color: #667eea;
  background: #f0f4ff;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
}

.block-icon {
  font-size: 48px;
  margin-bottom: 12px;
  line-height: 1;
}

.block-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.block-info p {
  margin: 0;
  font-size: 14px;
  color: #6c757d;
  line-height: 1.4;
}

/* 富文本编辑器样式 */
.rich-text-editor {
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  background: white;
}

.editor-toolbar {
  background: #f8f9fa;
  border-bottom: 1px solid #ddd;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.toolbar-btn {
  background: none;
  border: 1px solid transparent;
  border-radius: 4px;
  padding: 6px 8px;
  cursor: pointer;
  color: #495057;
  font-size: 14px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
}

.toolbar-btn:hover {
  background: #e9ecef;
  border-color: #adb5bd;
}

.toolbar-btn:active {
  background: #dee2e6;
}

.toolbar-btn i {
  font-size: 14px;
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: #dee2e6;
  margin: 0 4px;
}

.editor-content {
  min-height: 120px;
  padding: 12px 16px;
  outline: none;
  line-height: 1.6;
  font-size: 14px;
  color: #495057;
  background: white;
  direction: ltr;
  unicode-bidi: normal;
}

.editor-content:empty:before {
  content: attr(data-placeholder);
  color: #6c757d;
  font-style: italic;
}

.editor-content:focus {
  background: #fff;
}

/* 富文本内容样式 */
.editor-content strong {
  font-weight: bold;
}

.editor-content em {
  font-style: italic;
}

.editor-content u {
  text-decoration: underline;
}

.editor-content a {
  color: #007bff;
  text-decoration: underline;
}

.editor-content ul {
  margin: 8px 0;
  padding-left: 24px;
}

.editor-content ol {
  margin: 8px 0;
  padding-left: 24px;
}

.editor-content li {
  margin: 4px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .block-selector-grid {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 16px;
  }
  
  .block-type-card {
    padding: 16px;
    min-height: 120px;
  }
  
  .block-icon {
    font-size: 36px;
    margin-bottom: 8px;
  }
  
  .block-info h4 {
    font-size: 14px;
  }
  
  .block-info p {
    font-size: 12px;
  }
  
  .editor-toolbar {
    padding: 6px 8px;
    gap: 2px;
  }
  
  .toolbar-btn {
    padding: 4px 6px;
    min-width: 28px;
    height: 28px;
    font-size: 12px;
  }
  
  .toolbar-btn i {
    font-size: 12px;
  }
  
  .editor-content {
    min-height: 100px;
    padding: 10px 12px;
    font-size: 13px;
  }
}

/* 内联添加样式 */
.inline-add-container {
  margin-top: 8px;
  padding: 8px 0;
  border-top: 1px solid #f0f0f0;
}

.inline-add-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: 20px;
}

.inline-add-input {
  flex: 1;
  padding: 6px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;
  outline: none;
  transition: border-color 0.3s;
}

.inline-add-input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.inline-add-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.inline-confirm-btn,
.inline-cancel-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.inline-confirm-btn {
  background: #52c41a;
  color: white;
}

.inline-confirm-btn:hover:not(:disabled) {
  background: #389e0d;
}

.inline-confirm-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.inline-cancel-btn {
  background: #ff4d4f;
  color: white;
}

.inline-cancel-btn:hover {
  background: #cf1322;
}

.tree-add-footer {
  margin-top: 8px;
  padding: 8px 0;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}

.tree-add-footer-btn {
  padding: 6px 12px;
  background: #f8f9fa;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  color: #666;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
  width: 100%;
}

.tree-add-footer-btn:hover {
  background: #e6f7ff;
  border-color: #1890ff;
  color: #1890ff;
}

.tree-add-footer-btn i {
  margin-right: 4px;
}

/* Markdown预览样式 - 与DatasetDetail.vue保持一致 */
.markdown-preview {
  min-height: 400px;
  padding: 20px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.8;
  color: #2c3e50;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.markdown-preview h1 {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 30px 0 20px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  border-bottom: 3px solid rgba(102, 126, 234, 0.2);
  padding-bottom: 10px;
}

.markdown-preview h2 {
  font-size: 2rem;
  font-weight: 600;
  margin: 25px 0 15px 0;
  color: #667eea;
  border-left: 4px solid #667eea;
  padding-left: 15px;
}

.markdown-preview h3 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 20px 0 10px 0;
  color: #764ba2;
}

.markdown-preview p {
  margin: 15px 0;
  text-align: justify;
}

.markdown-preview ul,
.markdown-preview ol {
  margin: 15px 0;
  padding-left: 30px;
}

.markdown-preview li {
  margin: 8px 0;
}

.markdown-preview blockquote {
  border-left: 4px solid #667eea;
  background: rgba(102, 126, 234, 0.05);
  padding: 15px 20px;
  margin: 20px 0;
  border-radius: 0 10px 10px 0;
  position: relative;
}

.markdown-preview blockquote::before {
  content: '"';
  font-size: 4rem;
  color: rgba(102, 126, 234, 0.3);
  position: absolute;
  top: -10px;
  left: 10px;
  font-family: serif;
}

.markdown-preview code {
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  color: #667eea;
  font-weight: 600;
}

.markdown-preview pre {
  background: #2c3e50;
  color: #ecf0f1;
  padding: 20px;
  border-radius: 10px;
  overflow-x: auto;
  margin: 20px 0;
  position: relative;
}

.markdown-preview pre::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 10px 10px 0 0;
}

.markdown-preview pre code {
  background: none;
  padding: 0;
  color: inherit;
  font-weight: normal;
}

.markdown-preview table {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.markdown-preview th,
.markdown-preview td {
  padding: 15px 20px;
  text-align: left;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.markdown-preview th {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-weight: 600;
}

.markdown-preview tr:hover {
  background: rgba(102, 126, 234, 0.05);
}

.markdown-preview img {
  max-width: 100%;
  height: auto;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  margin: 20px 0;
  transition: transform 0.3s ease;
}

.markdown-preview img:hover {
  transform: scale(1.02);
}

.markdown-preview a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
}

.markdown-preview a:hover {
  border-bottom-color: #667eea;
}

.markdown-preview strong {
  font-weight: 600;
}

.markdown-preview em {
  font-style: italic;
}

/* 重要：确保颜色标签正确显示 */
.markdown-preview span[style*="color"] {
  /* 强制应用内联颜色样式，覆盖所有其他样式 */
  color: inherit !important;
}

.markdown-preview *[style*="color"] {
  /* 确保所有带颜色样式的元素都能正确显示 */
  color: inherit !important;
}

/* 特别处理常用颜色 */
.markdown-preview span[style*="color:red"],
.markdown-preview span[style*="color: red"],
.markdown-preview span[style*="color:#ff0000"],
.markdown-preview span[style*="color: #ff0000"],
.markdown-preview span[style*="color:rgb(255,0,0)"],
.markdown-preview span[style*="color: rgb(255,0,0)"] {
  color: red !important;
}

.markdown-preview span[style*="color:blue"],
.markdown-preview span[style*="color: blue"],
.markdown-preview span[style*="color:#0000ff"],
.markdown-preview span[style*="color: #0000ff"],
.markdown-preview span[style*="color:rgb(0,0,255)"],
.markdown-preview span[style*="color: rgb(0,0,255)"] {
  color: blue !important;
}

.markdown-preview span[style*="color:green"],
.markdown-preview span[style*="color: green"],
.markdown-preview span[style*="color:#008000"],
.markdown-preview span[style*="color: #008000"],
.markdown-preview span[style*="color:rgb(0,128,0)"],
.markdown-preview span[style*="color: rgb(0,128,0)"] {
  color: green !important;
}

.markdown-preview span[style*="color:orange"],
.markdown-preview span[style*="color: orange"],
.markdown-preview span[style*="color:#ffa500"],
.markdown-preview span[style*="color: #ffa500"] {
  color: orange !important;
}

.markdown-preview span[style*="color:purple"],
.markdown-preview span[style*="color: purple"],
.markdown-preview span[style*="color:#800080"],
.markdown-preview span[style*="color: #800080"] {
  color: purple !important;
}

.markdown-preview span[style*="color:yellow"],
.markdown-preview span[style*="color: yellow"],
.markdown-preview span[style*="color:#ffff00"],
.markdown-preview span[style*="color: #ffff00"] {
  color: yellow !important;
}

/* 通用颜色处理 - 使用data-color属性 */
.markdown-preview span[data-color="red"] {
  color: red !important;
}

.markdown-preview span[data-color="blue"] {
  color: blue !important;
}

.markdown-preview span[data-color="green"] {
  color: green !important;
}

.markdown-preview span[data-color="orange"] {
  color: orange !important;
}

.markdown-preview span[data-color="purple"] {
  color: purple !important;
}

.markdown-preview span[data-color="yellow"] {
  color: yellow !important;
}

.markdown-preview span[data-color="#ff0000"] {
  color: #ff0000 !important;
}

.markdown-preview span[data-color="#0000ff"] {
  color: #0000ff !important;
}

.markdown-preview span[data-color="#008000"] {
  color: #008000 !important;
}

/* 最强力的颜色覆盖规则 */
.markdown-preview span[style*="!important"] {
  /* 确保带有!important的内联样式能够生效 */
}

/* HTML预览样式 */
.html-preview {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.html-raw,
.html-rendered {
  flex: 1;
}

.html-raw h4,
.html-rendered h4 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 2px solid #667eea;
  padding-bottom: 5px;
}

.html-code {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 15px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
  color: #495057;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 300px;
  overflow-y: auto;
}

.html-content {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  max-height: 400px;
  overflow-y: auto;
}

.html-content h1,
.html-content h2,
.html-content h3,
.html-content h4,
.html-content h5,
.html-content h6 {
  margin: 15px 0 10px 0;
  color: #2c3e50;
}

.html-content p {
  margin: 10px 0;
  line-height: 1.6;
}

.html-content ul,
.html-content ol {
  margin: 10px 0;
  padding-left: 30px;
}

.html-content li {
  margin: 5px 0;
}

.html-content pre {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  padding: 10px;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.html-content code {
  background: #f8f9fa;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.html-content img {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 10px 0;
}

.html-content a {
  color: #667eea;
  text-decoration: none;
}

.html-content a:hover {
  text-decoration: underline;
}
</style>
