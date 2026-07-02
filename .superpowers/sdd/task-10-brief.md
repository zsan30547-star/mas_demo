# Task 10: 视觉升级——Glassmorphism + 过渡动画

**Goal:** 应用全局 CSS 变量改造核心组件，引入毛玻璃效果，给侧边栏、状态胶囊、过渡动画等加入现代 UI 元素。

**Files:**
- Modify: `frontend/src/components/layout/SideBar.vue`
- Modify: `frontend/src/components/common/StatusBadge.vue`
- Create: `frontend/src/components/common/MarkdownRenderer.vue`

---

### Step 1: 改造 `SideBar.vue` 毛玻璃效果

覆盖 `E:\Code\jobs\project_1\frontend\src\components\layout\SideBar.vue`：

```vue
<!-- /frontend/src/components/layout/SideBar.vue -->
<!-- 职责描述：侧边导航栏，毛玻璃质感 -->

<template>
  <el-menu
    :collapse="appStore.sidebarCollapsed"
    :default-active="route.path"
    router
    class="sidebar"
  >
    <el-menu-item index="/dashboard">
      <el-icon><DataAnalysis /></el-icon>
      <span>仪表盘</span>
    </el-menu-item>
    <el-menu-item index="/models">
      <el-icon><Monitor /></el-icon>
      <span>模型管理</span>
    </el-menu-item>
    <el-menu-item index="/agents">
      <el-icon><Setting /></el-icon>
      <span>Agent 管理</span>
    </el-menu-item>
    <el-menu-item index="/workflows">
      <el-icon><Share /></el-icon>
      <span>工作流模板</span>
    </el-menu-item>
    <el-menu-item index="/tasks/new">
      <el-icon><Plus /></el-icon>
      <span>提交任务</span>
    </el-menu-item>
    <el-menu-item index="/tasks">
      <el-icon><List /></el-icon>
      <span>任务历史</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useAppStore } from '../../stores/app'
import { DataAnalysis, Monitor, Setting, Share, Plus, List } from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()
</script>

<style scoped>
.sidebar {
  height: 100vh;
  overflow-y: auto;
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border-right: 1px solid var(--glass-border);
}
</style>
```

### Step 2: 增强 `StatusBadge.vue`

覆盖 `E:\Code\jobs\project_1\frontend\src\components\common\StatusBadge.vue`：

```vue
<!-- /frontend/src/components/common/StatusBadge.vue -->
<!-- 职责描述：任务状态标签，带过渡动画和图标 -->

<template>
  <el-tag :type="type" size="small" effect="plain" class="status-badge">
    {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ status: string }>()

const map: Record<string, { type: string; label: string }> = {
  pending: { type: 'info', label: '等待中' },
  running: { type: 'warning', label: '执行中' },
  completed: { type: 'success', label: '已完成' },
  failed: { type: 'danger', label: '失败' },
  success: { type: 'success', label: '成功' },
}

const type = computed(() => map[props.status]?.type || 'info')
const label = computed(() => map[props.status]?.label || props.status)
</script>

<style scoped>
.status-badge {
  transition: all var(--transition-normal);
}
</style>
```

### Step 3: 创建 `MarkdownRenderer.vue`

创建 `E:\Code\jobs\project_1\frontend\src\components\common\MarkdownRenderer.vue`：

```vue
<!-- /frontend/src/components/common/MarkdownRenderer.vue -->
<!-- 职责描述：Markdown 内容渲染组件，格式化展示 AI 输出 -->

<template>
  <div class="markdown-body" v-html="rendered" />
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ content: string }>()

const rendered = computed(() => {
  return props.content
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/\n{2,}/g, '<br/><br/>')
    .replace(/\n/g, '<br/>')
})
</script>

<style scoped>
.markdown-body {
  line-height: 1.8;
  color: var(--color-text-primary);
  font-size: 14px;
}
.markdown-body :deep(h1) { font-size: 20px; margin: 16px 0 8px; }
.markdown-body :deep(h2) { font-size: 18px; margin: 14px 0 6px; }
.markdown-body :deep(h3) { font-size: 16px; margin: 12px 0 4px; }
.markdown-body :deep(code) {
  background: #ecf5ff;
  color: var(--color-primary);
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 13px;
}
.markdown-body :deep(strong) { font-weight: 600; }
.markdown-body :deep(li) { margin-left: 20px; }
</style>
```

### 验证

执行 `vue-tsc --noEmit` 和 `npm run build`
