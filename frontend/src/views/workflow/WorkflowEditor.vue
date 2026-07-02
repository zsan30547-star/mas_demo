<!-- /frontend/src/views/workflow/WorkflowEditor.vue -->
<!-- 职责描述：工作流编辑器主页面，组合 StepDraggableList + AgentConfigPanel -->

<template>
  <div class="editor">
    <h2>{{ isEdit ? '编辑工作流' : '新建工作流' }}</h2>

    <el-card style="margin-top: 20px">
      <el-form label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="store.name" placeholder="工作流名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="store.description" placeholder="描述（可选）" />
        </el-form-item>
      </el-form>
    </el-card>

    <div class="editor-body" style="display: flex; gap: 16px; margin-top: 16px">
      <div class="editor-left" style="flex: 1">
        <el-card header="步骤列表">
          <StepDraggableList />
          <el-button type="primary" plain @click="store.addStep" style="margin-top: 12px; width: 100%">+ 添加步骤</el-button>

          <el-collapse style="margin-top: 16px">
            <el-collapse-item title="占位符说明">
              <div class="placeholder-guide">
                <div><code>$i</code> : 用户提交任务时的原始输入文本</div>
                <div><code>$o1</code> : 步骤 1（上一步 Agent）执行后的输出结果</div>
                <div><code>$o2</code> : 步骤 2 执行后的输出结果</div>
                <div><code>$oN</code> : 步骤 N 执行后的输出结果（N 为步骤序号）</div>
                <div style="margin-top: 8px; color: var(--color-text-placeholder); font-size: 12px">提示：当前步骤只能引用它前面步骤的输出。如步骤 2 可用 $i 和 $o1</div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </div>

      <div class="editor-right" style="flex: 1">
        <AgentConfigPanel />
      </div>
    </div>

    <div style="margin-top: 20px">
      <el-button type="primary" :loading="loading" @click="handleSave">保存模板</el-button>
      <el-button @click="$router.back()">取消</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createWorkflow, updateWorkflow, getWorkflow } from '../../api/workflow'
import { getAgentList } from '../../api/agent'
import { useWorkflowStore } from '../../stores/workflow'
import StepDraggableList from '../../components/workflow/StepDraggableList.vue'
import AgentConfigPanel from '../../components/workflow/AgentConfigPanel.vue'

const route = useRoute()
const router = useRouter()
const store = useWorkflowStore()
const isEdit = !!route.params.id
const loading = ref(false)

onMounted(async () => {
  store.reset()
  const res = await getAgentList()
  store.setAgents(res.data)

  if (isEdit) {
    const wfRes = await getWorkflow(Number(route.params.id))
    const wf = wfRes.data
    store.name = wf.name
    store.description = wf.description || ''
    const parsed = JSON.parse(wf.steps || '[]')
    store.loadFromSteps(parsed)
  }
})

async function handleSave() {
  if (!store.name) { ElMessage.warning('请输入名称'); return }
  if (!store.steps.length) { ElMessage.warning('请添加至少一个步骤'); return }
  loading.value = true
  try {
    const data = {
      name: store.name,
      description: store.description,
      steps: JSON.stringify(store.toSubmitSteps()),
    }
    if (isEdit) {
      await updateWorkflow(Number(route.params.id), data)
      ElMessage.success('更新成功')
    } else {
      await createWorkflow(data)
      ElMessage.success('创建成功')
    }
    router.push('/workflows')
  } catch (e: any) {
    // 后端校验失败（如凭证无效）时，显示后端返回的具体错误信息
    const msg = e?.response?.data?.message || e?.message || '保存失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.placeholder-guide { font-size: 13px; line-height: 2; }
.placeholder-guide code { background: var(--color-primary-bg); color: var(--color-primary); padding: 2px 6px; border-radius: 3px; font-size: 12px; }
</style>