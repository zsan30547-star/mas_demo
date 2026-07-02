<!-- /frontend/src/components/workflow/AgentConfigPanel.vue -->
<!-- 职责描述：选中步骤的 Agent 配置面板 -->

<template>
  <el-card v-if="store.selectedStep" header="步骤配置" class="config-panel">
    <el-form label-width="80px">
      <el-form-item label="Agent">
        <el-select v-model="store.selectedStep.agentId" placeholder="选择 Agent" filterable style="width: 100%">
          <el-option v-for="a in store.agents" :key="a.id" :label="a.name" :value="a.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="输入模板">
        <el-input v-model="store.selectedStep.inputTemplate" type="textarea" :rows="3" placeholder="如：将以下课题拆解为子任务：$i，用 $o1 引用上一步输出" />
      </el-form-item>
    </el-form>
  </el-card>
  <el-empty v-else description="点击左侧步骤进行配置" :image-size="80" style="margin-top: 40px" />
</template>

<script setup lang="ts">
import { useWorkflowStore } from '../../stores/workflow'
const store = useWorkflowStore()
</script>

<style scoped>
.config-panel { min-height: 200px; }
</style>
