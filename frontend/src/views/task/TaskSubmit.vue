<template>
  <div>
    <h2>提交任务</h2>
    <el-card style="max-width:700px; margin-top:20px">
      <el-form label-width="120px">
        <el-form-item label="工作流模板">
          <el-select v-model="workflowId" placeholder="选择工作流" filterable style="width:100%">
            <el-option v-for="wf in workflows" :key="wf.id" :label="wf.name" :value="wf.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务标题">
          <el-input v-model="title" placeholder="输入任务标题" />
        </el-form-item>
        <el-form-item label="输入内容">
          <el-input v-model="inputText" type="textarea" :rows="6" placeholder="输入任务描述，如：研究2026年AI Agent在企业服务中的应用趋势" />
        </el-form-item>
        <el-form-item label="上传附件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :file-list="fileList"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            multiple
            accept=".pdf,.doc,.docx,.txt,.jpg,.png"
          >
            <el-button type="primary" plain>选择文件</el-button>
            <template #tip>
              <span style="font-size:12px;color:var(--color-text-placeholder)">支持 PDF/DOC/TXT/JPG/PNG</span>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">提交执行</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getWorkflowList } from '../../api/workflow';
import type { WorkflowVO } from '../../types/workflow';
import { submitTask } from '../../api/task';
import { uploadFile } from '../../api/file';
import type { UploadFile, UploadFiles } from 'element-plus';

const router = useRouter();
const loading = ref(false);
const workflows = ref<WorkflowVO[]>([]);
const workflowId = ref<number | null>(null);
const title = ref('');
const inputText = ref('');
const fileList = ref<UploadFile[]>([]);
const uploadRef = ref();

onMounted(async () => {
  const res = await getWorkflowList();
  workflows.value = res.data;
});

function handleFileChange(_file: UploadFile, files: UploadFiles) {
  fileList.value = [...files];
}

function handleFileRemove(_file: UploadFile, files: UploadFiles) {
  fileList.value = [...files];
}

async function handleSubmit() {
  if (!workflowId.value) { ElMessage.warning('请选择工作流'); return }
  if (!title.value) { ElMessage.warning('请输入标题'); return }
  if (!inputText.value) { ElMessage.warning('请输入内容'); return }
  loading.value = true;
  try {
    // 先上传文件
    const fileIds: number[] = [];
    for (const f of fileList.value) {
      if (f.raw) {
        const res = await uploadFile(f.raw);
        fileIds.push(res.data.id);
      }
    }

    const taskRes = await submitTask({
      workflowId: workflowId.value,
      title: title.value,
      inputData: { text: inputText.value },
      fileIds: fileIds.length > 0 ? fileIds : undefined,
    });
    ElMessage.success('任务已提交');
    router.push(`/tasks/${taskRes.data.id}`);
  } finally {
    loading.value = false;
  }
}
</script>
