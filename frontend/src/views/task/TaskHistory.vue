<template>
  <div>
    <h2>任务历史</h2>
    <el-table :data="tasks" stripe style="margin-top:20px" v-loading="loading" v-if="tasks.length">
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
    <el-empty v-else-if="!loading" description="暂无任务" style="margin-top:40px" />

    <div style="margin-top: 20px; display: flex; justify-content: center" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchTasks"
        @size-change="fetchTasks"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getTaskList } from '../../api/task';
import type { TaskVO } from '../../types/task';
import StatusBadge from '../../components/common/StatusBadge.vue';

const tasks = ref<TaskVO[]>([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

onMounted(() => fetchTasks());

async function fetchTasks() {
  loading.value = true;
  try {
    const res = await getTaskList(currentPage.value, pageSize.value);
    tasks.value = res.data.records;
    total.value = res.data.total;
  } finally { loading.value = false; }
}
</script>
