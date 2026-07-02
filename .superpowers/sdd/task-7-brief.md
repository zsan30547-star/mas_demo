# Task 7: 重构工作流编辑器 WorkflowEditor.vue —— 使用 Store + 拆分为子组件

**Goal:** 将原本单文件 150+ 行的 `WorkflowEditor.vue` 拆分为布局主容器和两个核心子组件，并接入刚刚创建的 `useWorkflowStore`。

**Files:**
- Create: `frontend/src/components/workflow/StepDraggableList.vue`
- Create: `frontend/src/components/workflow/AgentConfigPanel.vue`
- Modify: `frontend/src/views/workflow/WorkflowEditor.vue`

---

### Step 1: 创建 `StepDraggableList.vue`

```vue
<!-- /frontend/src/components/workflow/StepDraggableList.vue -->
<!-- 职责描述：工作流步骤拖拽排序列表 -->

<template>
  <div class="step-list">
    <div v-for="(step, idx) in store.steps" :key="step.key" class="step-item" :class="{ active: store.selectedStepIndex === idx }" @click="store.selectStep(idx)">
      <div class="step-header">
        <span class="step-index">步骤 {{ idx + 1 }}</span>
        <div>
          <el-button text type="primary" size="small" @click.stop="store.selectStep(idx)">配置</el-button>
          <el-button text type="danger" size="small" @click.stop="store.removeStep(idx)">删除</el-button>
        </div>
      </div>
      <div class="step-body">
        <div class="step-agent">{{ stepAgentLabel(idx) }}</div>
        <div class="step-template">{{ step.inputTemplate }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useWorkflowStore } from '../../stores/workflow'

const store = useWorkflowStore()

function stepAgentLabel(idx: number): string {
  const agent = store.agents.find((a) => a.id === store.steps[idx]?.agentId)
  return agent?.name || '未选择 Agent'
}
</script>

<style scoped>
.step-list { display: flex; flex-direction: column; gap: 8px; }
.step-item {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.2s;
}
.step-item:hover { border-color: #409eff; }
.step-item.active { border-color: #409eff; background: #ecf5ff; }
.step-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.step-index { font-weight: 600; color: #409eff; }
.step-agent { font-size: 13px; color: #606266; margin-bottom: 4px; }
.step-template { font-size: 12px; color: #909399; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>
```

### Step 2: 创建 `AgentConfigPanel.vue`

```vue
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
        <el-input v-model="store.selectedStep.inputTemplate" type="textarea" :rows="3" placeholder="如：将以下课题拆解为子任务：{{input}}" />
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
```

### Step 3: 重构 `WorkflowEditor.vue` 为布局容器

覆盖 `E:\Code\jobs\project_1\frontend\src\views\workflow\WorkflowEditor.vue`：

```vue
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
                <div><code>{<!-- -->{input}}</code> — 用户提交任务时的原始输入文本</div>
                <div><code>{<!-- -->{step1.output}}</code> — 步骤1 执行后的输出结果</div>
                <div><code>{<!-- -->{step2.output}}</code> — 步骤2 执行后的输出结果</div>
                <div><code>{<!-- -->{stepN.output}}</code> — 步骤N 执行后的输出结果（N 为步骤序号）</div>
                <div style="margin-top: 8px; color: #909399; font-size: 12px">当前步骤只能引用它前面步骤的输出，不能引用后面的步骤或当前步骤。</div>
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
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.placeholder-guide { font-size: 13px; line-height: 2; }
.placeholder-guide code { background: #ecf5ff; color: #409eff; padding: 2px 6px; border-radius: 3px; font-size: 12px; }
</style>
```

### 验证

执行 `vue-tsc --noEmit` 和 `npm run build`
