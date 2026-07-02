<template>
  <el-container class="app-wrapper">
    <SideBar />
    <el-container direction="vertical">
      <NavBar />
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import SideBar from './SideBar.vue';
import NavBar from './NavBar.vue';
</script>

<style scoped>
.app-wrapper {
  height: 100vh;
  background-color: var(--color-bg); /* 回归纯净的浅灰底色 */
}
.el-main {
  background: transparent;
  padding: 24px; /* 增大内边距让页面更有呼吸感 */
}
</style>
