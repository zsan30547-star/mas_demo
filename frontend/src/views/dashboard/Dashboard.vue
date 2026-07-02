<template>
  <div>
    <h2>仪表盘</h2>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">总任务数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value" style="color:var(--color-success)">{{ stats.completed }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value" style="color:var(--color-warning)">{{ stats.running }}</div>
            <div class="stat-label">执行中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value" style="color:var(--color-danger)">{{ stats.failed }}</div>
            <div class="stat-label">失败</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top:20px" header="最近任务">
      <el-table :data="recentTasks" v-if="recentTasks.length" stripe>
        <el-table-column prop="title" label="任务名称" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusBadge :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="$router.push(`/tasks/${row.id}`)">查看</el-link>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无任务" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getTaskStats } from '../../api/task';
import type { TaskVO } from '../../types/task';
import StatusBadge from '../../components/common/StatusBadge.vue';

const stats = ref({ total: 0, completed: 0, running: 0, failed: 0, pending: 0 });
const recentTasks = ref<TaskVO[]>([]);

onMounted(async () => {
  try {
    const res = await getTaskStats();
    stats.value = {
      total: res.data.total,
      completed: res.data.completed,
      running: res.data.running,
      failed: res.data.failed,
      pending: res.data.pending,
    };
    recentTasks.value = res.data.recent || [];
  } catch {}
});
</script>

<style scoped>
.stat-item { text-align: center; padding: 10px 0; }
.stat-value { font-size: 32px; font-weight: 700; }
.stat-label { font-size: 14px; color: var(--color-text-placeholder); margin-top: 8px; }
</style>
