<template>
  <div>
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 20px;">
      <h2>模型 & Agent 管理</h2>
    </div>

    <el-tabs v-model="activeTab">
      <!-- 选项卡 1：API 凭证库 -->
      <el-tab-pane label="API 凭证库" name="credentials">
        <div style="margin-bottom: 16px;">
          <el-button type="primary" @click="openCreateCred">添加凭证</el-button>
        </div>

        <el-table :data="credList" stripe>
          <el-table-column prop="name" label="凭证名称" />
          <el-table-column prop="endpoint" label="端点" show-overflow-tooltip />
          <el-table-column label="Key状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.hasApiKey ? 'success' : 'info'" size="small">
                {{ row.hasApiKey ? '已配置' : '未配置' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" @click="openEditCred(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDeleteCred(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!credList.length" description="暂无 API 凭证" />
      </el-tab-pane>

      <!-- 选项卡 2：模型配置 -->
      <el-tab-pane label="模型配置" name="models">
        <div style="margin-bottom: 16px;">
          <el-button type="primary" @click="openCreateModel">添加模型</el-button>
        </div>

        <el-table :data="modelList" stripe>
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="model" label="模型标识" />
          <el-table-column label="绑定的凭证" show-overflow-tooltip>
            <template #default="{ row }">
              <el-tag v-if="row.apiCredentialId" type="primary" size="small" effect="plain">
                {{ getCredName(row.apiCredentialId) }}
              </el-tag>
              <el-tag v-else type="info" size="small">未绑定</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.isPreset" type="warning" size="small">预置</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" @click="openEditModel(row)">编辑</el-button>
              <el-button size="small" type="danger" :disabled="!!row.isPreset" @click="handleDeleteModel(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!modelList.length" description="暂无模型配置" />
      </el-tab-pane>

      <!-- 选项卡 3：Agent 配置 -->
      <el-tab-pane label="Agent 配置" name="agents">
        <div style="margin-bottom: 16px;">
          <el-button type="primary" @click="openCreateAgent">添加 Agent</el-button>
        </div>

        <el-table :data="agentList" stripe>
          <el-table-column prop="name" label="名称" />
          <el-table-column label="角色类型" width="120">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">{{ row.agentType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="绑定模型" show-overflow-tooltip>
            <template #default="{ row }">
              <el-tag type="success" size="small" effect="plain">
                {{ getModelName(row.modelConfigId) || '未知模型' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.isPreset" type="warning" size="small">预置</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" @click="openEditAgent(row)">编辑</el-button>
              <el-button size="small" type="danger" :disabled="!!row.isPreset" @click="handleDeleteAgent(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!agentList.length" description="暂无 Agent 配置" />
      </el-tab-pane>
    </el-tabs>

    <!-- API 凭证对话框 -->
    <el-dialog v-model="showCredDialog" :title="editingCredId ? '编辑 API 凭证' : '添加 API 凭证'" width="520px">
      <el-form :model="credForm" label-width="100px">
        <el-form-item label="凭证名称">
          <el-input v-model="credForm.name" placeholder="如: 我的 DeepSeek 账号" />
        </el-form-item>
        <el-form-item label="API端点">
          <el-input v-model="credForm.endpoint" placeholder="如: https://api.deepseek.com/v1" />
        </el-form-item>
        <el-form-item label="API Key">
          <el-input v-model="credForm.apiKey" type="password" show-password placeholder="API Key" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCredDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingCred" @click="handleSaveCred">保存</el-button>
      </template>
    </el-dialog>

    <!-- 模型对话框 -->
    <el-dialog v-model="showModelDialog" :title="editingModelId ? '编辑模型' : '添加模型'" width="500px">
      <el-form :model="modelForm" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="modelForm.name" placeholder="如: DeepSeek-V3" />
        </el-form-item>
        <el-form-item label="模型标识">
          <el-input v-model="modelForm.model" placeholder="如: deepseek-chat" />
        </el-form-item>
        <el-form-item label="绑定凭证">
          <el-select v-model="modelForm.apiCredentialId" placeholder="选择使用的 API 凭证" style="width: 100%" clearable>
            <el-option v-for="c in credList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showModelDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingModel" @click="handleSaveModel">保存</el-button>
      </template>
    </el-dialog>

    <!-- Agent 对话框 -->
    <el-dialog v-model="showAgentDialog" :title="editingAgentId ? '编辑 Agent' : '添加 Agent'" width="600px">
      <el-form :model="agentForm" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="agentForm.name" placeholder="如: 规划专家" />
        </el-form-item>
        <el-form-item label="角色类型" required>
          <el-select v-model="agentForm.agentType" style="width:100%">
            <el-option label="规划(Planner)" value="planner" />
            <el-option label="视觉(Vision)" value="vision" />
            <el-option label="执行(Executor)" value="executor" />
            <el-option label="验证(Validator)" value="validator" />
            <el-option label="搜索(Search)" value="search" />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定模型" required>
          <el-select v-model="agentForm.modelConfigId" style="width:100%" placeholder="选择底层驱动模型" filterable>
            <el-option v-for="m in modelList" :key="m.id" :label="m.name + ' (' + m.model + ')'" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="提示词">
          <el-input v-model="agentForm.systemPrompt" type="textarea" :rows="4" placeholder="System Prompt（系统角色设定）" />
        </el-form-item>
        <el-form-item label="温度">
          <el-slider v-model="agentForm.temperature" :min="0" :max="2" :step="0.1" style="width:80%" />
        </el-form-item>
        <el-form-item label="最大Token">
          <el-input-number v-model="agentForm.maxTokens" :min="512" :max="32768" :step="512" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAgentDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingAgent" @click="handleSaveAgent">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getModelList, createModel, updateModel, deleteModel } from '../../api/model';
import { getCredentialList, createCredential, updateCredential, deleteCredential, testCredential } from '../../api/credential';
import { getAgentList, createAgent, updateAgent, deleteAgent } from '../../api/agent';
import type { ModelConfigVO } from '../../types/model';
import type { ApiCredentialVO } from '../../types/credential';
import type { AgentVO } from '../../types/agent';

const activeTab = ref('models');

// --- 凭证逻辑 ---
const credList = ref<ApiCredentialVO[]>([]);
const showCredDialog = ref(false);
const editingCredId = ref<number | null>(null);
const savingCred = ref(false);

const credForm = reactive({ name: '', endpoint: '', apiKey: '' });

// --- 模型逻辑 ---
const modelList = ref<ModelConfigVO[]>([]);
const showModelDialog = ref(false);
const editingModelId = ref<number | null>(null);
const savingModel = ref(false);

const modelForm = reactive({ name: '', model: '', apiCredentialId: null as number | null });

// --- Agent 逻辑 ---
const agentList = ref<AgentVO[]>([]);
const showAgentDialog = ref(false);
const editingAgentId = ref<number | null>(null);
const savingAgent = ref(false);

const agentForm = reactive({
  name: '', agentType: 'planner', modelConfigId: null as number | null,
  systemPrompt: '', temperature: 0.7, maxTokens: 4096
});

onMounted(async () => {
  await fetchCreds();
  await fetchModels();
  await fetchAgents();
});

async function fetchCreds() {
  const r = await getCredentialList();
  credList.value = r.data;
}

async function fetchModels() {
  const r = await getModelList();
  modelList.value = r.data;
}

async function fetchAgents() {
  const r = await getAgentList();
  agentList.value = r.data;
}

function getCredName(id: number) {
  return credList.value.find(c => c.id === id)?.name || '未知凭证';
}

function getModelName(id: number) {
  return modelList.value.find(m => m.id === id)?.name || '未知模型';
}

// -- 凭证方法 --
function openCreateCred() {
  editingCredId.value = null;
  Object.assign(credForm, { name: '', endpoint: '', apiKey: '' });
  showCredDialog.value = true;
}

function openEditCred(row: ApiCredentialVO) {
  editingCredId.value = row.id;
  Object.assign(credForm, { name: row.name, endpoint: row.endpoint, apiKey: '' });
  showCredDialog.value = true;
}

async function handleSaveCred() {
  if (!credForm.name || !credForm.endpoint) { ElMessage.warning('请填写名称和端点'); return; }

  savingCred.value = true;
  try {
    const data: any = { name: credForm.name, endpoint: credForm.endpoint };
    if (editingCredId.value) {
      if (credForm.apiKey) data.apiKey = credForm.apiKey;
      await updateCredential(editingCredId.value, data);
      ElMessage.success('凭证已更新');
    } else {
      data.apiKey = credForm.apiKey || undefined;
      await createCredential(data);
      ElMessage.success('凭证已添加');
    }
    showCredDialog.value = false;
    await fetchCreds();
  } finally { savingCred.value = false; }
}

async function handleDeleteCred(id: number) {
  await ElMessageBox.confirm('确定删除凭证吗？（依赖该凭证的模型将自动解绑）');
  await deleteCredential(id);
  ElMessage.success('已删除');
  await fetchCreds();
  await fetchModels();
}

// -- 模型方法 --
function openCreateModel() {
  editingModelId.value = null;
  Object.assign(modelForm, { name: '', model: '', apiCredentialId: null });
  showModelDialog.value = true;
}

function openEditModel(row: ModelConfigVO) {
  editingModelId.value = row.id;
  Object.assign(modelForm, { name: row.name, model: row.model, apiCredentialId: row.apiCredentialId });
  showModelDialog.value = true;
}

async function handleSaveModel() {
  if (!modelForm.name || !modelForm.model) { ElMessage.warning('请填写名称和模型标识'); return; }
  savingModel.value = true;
  try {
    // 如果绑定了凭证，保存前验证连通性
    if (modelForm.apiCredentialId) {
      const testRes = await testCredential(modelForm.apiCredentialId, modelForm.model);
      if (!testRes.data.valid) {
        ElMessage.error('验证失败: ' + (testRes.data.message || '无法连接到该模型'));
        return;
      }
    }

    const data = { ...modelForm };
    if (editingModelId.value) {
      await updateModel(editingModelId.value, data);
      ElMessage.success('模型已更新');
    } else {
      await createModel(data);
      ElMessage.success('模型已添加');
    }
    showModelDialog.value = false;
    await fetchModels();
    await fetchAgents(); // 模型更改可能影响 Agent 展示
  } finally { savingModel.value = false; }
}

async function handleDeleteModel(id: number) {
  await ElMessageBox.confirm('确定删除模型吗？（使用该模型的 Agent 将失效）');
  await deleteModel(id);
  ElMessage.success('已删除');
  await fetchModels();
  await fetchAgents();
}

// -- Agent 方法 --
function openCreateAgent() {
  editingAgentId.value = null;
  Object.assign(agentForm, {
    name: '', agentType: 'planner', modelConfigId: null,
    systemPrompt: '', temperature: 0.7, maxTokens: 4096
  });
  showAgentDialog.value = true;
}

function openEditAgent(row: AgentVO) {
  editingAgentId.value = row.id;
  Object.assign(agentForm, {
    name: row.name, agentType: row.agentType, modelConfigId: row.modelConfigId,
    systemPrompt: row.systemPrompt || '', temperature: row.temperature, maxTokens: row.maxTokens
  });
  showAgentDialog.value = true;
}

async function handleSaveAgent() {
  if (!agentForm.name || !agentForm.agentType || !agentForm.modelConfigId) {
    ElMessage.warning('请填写必填项（名称、类型、模型）');
    return;
  }
  savingAgent.value = true;
  try {
    const data: any = { ...agentForm };
    if (editingAgentId.value) {
      await updateAgent(editingAgentId.value, data);
      ElMessage.success('Agent 已更新');
    } else {
      await createAgent(data);
      ElMessage.success('Agent 已添加');
    }
    showAgentDialog.value = false;
    await fetchAgents();
  } finally { savingAgent.value = false; }
}

async function handleDeleteAgent(id: number) {
  await ElMessageBox.confirm('确定删除此 Agent 吗？');
  await deleteAgent(id);
  ElMessage.success('已删除');
  await fetchAgents();
}
</script>
