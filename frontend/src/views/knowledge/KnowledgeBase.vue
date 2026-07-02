<template>
  <div>
    <h2>知识库</h2>
    <p style="color: var(--color-text-secondary); font-size: 13px; margin: 4px 0 16px">
      上传文档到知识库，Agent 执行任务时可检索相关内容以增强回答质量（RAG）
    </p>

    <el-card style="max-width: 600px">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :file-list="fileList"
        :on-change="handleChange"
        :on-remove="handleRemove"
        accept=".txt"
        multiple
      >
        <el-button type="primary" plain>选择 TXT 文件</el-button>
        <template #tip>
          <span style="font-size: 12px; color: var(--color-text-placeholder)">支持 UTF-8 编码的 .txt 文件</span>
        </template>
      </el-upload>

      <div style="margin-top: 16px" v-if="fileList.length">
        <el-button type="primary" :loading="uploading" @click="handleUpload">上传并入库</el-button>
      </div>
    </el-card>

    <el-card header="入库记录" style="max-width: 600px; margin-top: 20px">
      <div v-if="records.length">
        <div v-for="(r, i) in records" :key="i" class="record-item">
          <div class="record-name">{{ r.fileName }}</div>
          <div class="record-meta">{{ r.chars }} 字符 · {{ r.docId }}</div>
        </div>
      </div>
      <el-empty v-else description="暂无入库文档" :image-size="60" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadDoc } from '../../api/knowledge'
import type { UploadFile, UploadFiles } from 'element-plus'

const fileList = ref<UploadFile[]>([])
const uploadRef = ref()
const uploading = ref(false)
const records = ref<{ fileName: string; docId: string; chars: number }[]>([])

function handleChange(_file: UploadFile, files: UploadFiles) {
  fileList.value = [...files]
}

function handleRemove(_file: UploadFile, files: UploadFiles) {
  fileList.value = [...files]
}

async function handleUpload() {
  uploading.value = true
  try {
    for (const f of fileList.value) {
      if (!f.raw) continue
      const res = await uploadDoc(f.raw)
      records.value.unshift(res.data)
      ElMessage.success(`${res.data.fileName} 入库成功`)
    }
    fileList.value = []
  } catch { /* ignore */ } finally { uploading.value = false }
}
</script>

<style scoped>
.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--color-border-light);
}
.record-item:last-child { border-bottom: none; }
.record-name { font-weight: 500; color: var(--color-text-primary); font-size: 14px; }
.record-meta { font-size: 12px; color: var(--color-text-placeholder); }
</style>
