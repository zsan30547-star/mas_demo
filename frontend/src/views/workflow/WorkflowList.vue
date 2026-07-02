<template>
  <div>
    <div style="display:flex; justify-content:space-between; align-items:center">
      <h2>工作流模板</h2>
      <el-button type="primary" @click="$router.push('/workflows/new')">新建工作流</el-button>
    </div>

    <el-table :data="workflows" stripe style="margin-top:20px" v-if="workflows.length">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="步骤数" width="80">
        <template #default="{ row }">
          <el-tag size="small">{{ JSON.parse(row.steps || '[]').length }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isPreset" type="warning" size="small">预置</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/workflows/${row.id}/edit`)">编辑</el-button>
          <el-button size="small" type="danger" :disabled="row.isPreset" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-else description="暂无工作流模板" style="margin-top:40px" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getWorkflowList, deleteWorkflow } from '../../api/workflow';
import type { WorkflowVO } from '../../types/workflow';

const workflows = ref<WorkflowVO[]>([]);

onMounted(async () => {
  const res = await getWorkflowList();
  workflows.value = res.data;
});

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除？');
  await deleteWorkflow(id);
  workflows.value = workflows.value.filter((w) => w.id !== id);
  ElMessage.success('已删除');
}
</script>
