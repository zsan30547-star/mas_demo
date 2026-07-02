<template>
  <el-header class="navbar">
    <div class="navbar-left">
      <el-button @click="appStore.toggleSidebar" text>
        <el-icon><Fold /></el-icon>
      </el-button>
    </div>
    <div class="navbar-right">
      <!-- 暗黑模式切换开关 -->
      <el-switch
        v-model="appStore.isDark"
        class="theme-switch"
        inline-prompt
        :active-icon="Moon"
        :inactive-icon="Sunny"
        style="margin-right: 20px;"
      />

      <el-dropdown trigger="click">
        <span class="user-info">
          <el-icon><User /></el-icon>
          {{ userStore.username || 'admin' }}
        </span>
        <template #dropdown>
          <el-dropdown-item @click="$router.push('/settings')">个人设置</el-dropdown-item>
          <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAppStore } from '../../stores/app';
import { useUserStore } from '../../stores/user';
import { Fold, User, Moon, Sunny } from '@element-plus/icons-vue';

const router = useRouter();
const appStore = useAppStore();
const userStore = useUserStore();

function handleLogout() {
  userStore.logout();
  router.push('/login');
}
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  height: 60px;
  padding: 0 20px;
}
.navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.page-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}
.navbar-right {
  display: flex;
  align-items: center;
}
.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-primary);
  font-size: 14px;
}
.theme-switch {
  --el-switch-on-color: #2c2c2c;
  --el-switch-off-color: #f2f2f2;
}
</style>