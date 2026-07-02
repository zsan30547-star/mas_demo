<!-- /frontend/src/components/layout/SideBar.vue -->
<!-- 职责描述：侧边导航栏 -->

<template>
  <el-menu
    :collapse="appStore.sidebarCollapsed"
    :default-active="route.path"
    router
    class="sidebar"
  >
    <div class="logo-area">
      <el-icon class="logo-icon"><Platform /></el-icon>
      <span v-if="!appStore.sidebarCollapsed" class="logo-text">Agent Platform</span>
    </div>
    
    <el-menu-item index="/dashboard">
      <el-icon><DataAnalysis /></el-icon>
      <span>仪表盘</span>
    </el-menu-item>
    <el-menu-item index="/models">
      <el-icon><Operation /></el-icon>
      <span>模型 & Agent 管理</span>
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
    <el-menu-item index="/knowledge">
      <el-icon><Reading /></el-icon>
      <span>知识库</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useAppStore } from '../../stores/app'
import { DataAnalysis, Operation, Share, Plus, List, Platform, Reading } from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()
</script>

<style scoped>
.sidebar {
  height: 100vh;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-right: 1px solid rgba(229, 231, 235, 0.5);
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.04);
}

:root.dark .sidebar,
html.dark .sidebar {
  background: rgba(29, 30, 31, 0.85);
  border-right: 1px solid rgba(65, 66, 67, 0.5);
}

.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid var(--color-border-light);
  margin-bottom: 8px;
}

.logo-icon {
  font-size: 24px;
  color: var(--color-primary);
}

.logo-text {
  margin-left: 12px;
  font-weight: 700;
  font-size: 16px;
  color: var(--color-text-primary);
  white-space: nowrap;
}

/* 优化菜单项的悬浮和选中状态 */
:deep(.el-menu-item) {
  margin: 4px 12px;
  border-radius: var(--radius-sm);
  height: 44px;
  line-height: 44px;
}
:deep(.el-menu-item.is-active) {
  background-color: rgba(37, 99, 235, 0.08);
  font-weight: 600;
}
</style>