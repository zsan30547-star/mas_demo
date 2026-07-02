// /frontend/src/stores/app.ts
// 职责描述：全局应用状态（侧边栏、暗黑模式）

import { defineStore } from 'pinia';
import { ref, watch } from 'vue';

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false);
  
  // 初始化时从 localStorage 读取暗黑模式设置，默认亮色
  const isDark = ref(localStorage.getItem('theme-dark') === 'true');

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value;
  }

  function toggleDarkMode() {
    isDark.value = !isDark.value;
  }

  // 监听 isDark 的变化，动态给 HTML 标签添加/移除 'dark' class
  watch(isDark, (val) => {
    if (val) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
    localStorage.setItem('theme-dark', String(val));
  }, { immediate: true }); // immediate 确保应用启动时立刻应用状态

  return { sidebarCollapsed, isDark, toggleSidebar, toggleDarkMode };
});
