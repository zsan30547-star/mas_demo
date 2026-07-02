<template>
  <div>
    <h2>个人设置</h2>

    <el-card header="修改邮箱" style="max-width: 500px; margin-top: 20px">
      <el-form label-width="100px">
        <el-form-item label="新邮箱">
          <el-input v-model="email" placeholder="输入新邮箱" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingEmail" @click="handleUpdateEmail">保存邮箱</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="max-width: 500px; margin-top: 20px" class="info-card">
      <template #header>
        <span>搜索 API 配置</span>
      </template>
      <p style="margin: 0; font-size: 13px; color: var(--color-text-regular); line-height: 1.8">
        联网搜索功能需要配置 SerpAPI Key。请在 <code>ai_engine/.env</code> 文件中设置：
      </p>
      <pre style="background: var(--color-bg); padding: 12px; border-radius: 6px; margin-top: 8px; font-size: 13px">SERPAPI_API_KEY=your-serpapi-key</pre>
      <p style="margin: 8px 0 0; font-size: 12px; color: var(--color-text-secondary)">
        修改后重启 AI Engine 即可生效。可在 <a href="https://serpapi.com" target="_blank" style="color: var(--color-primary)">serpapi.com</a> 免费申请 API Key。
      </p>
    </el-card>

    <el-card header="修改密码" style="max-width: 500px; margin-top: 20px">
      <el-form label-width="100px">
        <el-form-item label="原密码">
          <el-input v-model="oldPassword" type="password" show-password placeholder="输入原密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="newPassword" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingPwd" @click="handleChangePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, changePassword } from '../../api/user'

const email = ref('')
const oldPassword = ref('')
const newPassword = ref('')
const savingEmail = ref(false)
const savingPwd = ref(false)

onMounted(async () => {
  try {
    const res = await getProfile()
    email.value = res.data.email || ''
  } catch { /* ignore */ }
})

async function handleUpdateEmail() {
  if (!email.value) { ElMessage.warning('请输入邮箱'); return }
  savingEmail.value = true
  try {
    await updateProfile(email.value)
    ElMessage.success('邮箱已更新')
  } finally { savingEmail.value = false }
}

async function handleChangePassword() {
  if (!oldPassword.value) { ElMessage.warning('请输入原密码'); return }
  if (newPassword.value.length < 6) { ElMessage.warning('新密码至少 6 位'); return }
  savingPwd.value = true
  try {
    await changePassword(oldPassword.value, newPassword.value)
    ElMessage.success('密码已修改')
    oldPassword.value = ''
    newPassword.value = ''
  } finally { savingPwd.value = false }
}
</script>
