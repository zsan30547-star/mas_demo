# Task 6: 增强用户 Store 类型约束

**Goal:** 增强 `useUserStore`，将 `isLoggedIn` 从函数改为计算属性（computed），补充 `setUsername` 方法，以便配合路由守卫和组件。

**Files:**
- Modify: `frontend/src/stores/user.ts`
- Modify: `frontend/src/components/layout/NavBar.vue` (更新 `isLoggedIn` 的调用方式)

**Global Constraints:**
- TypeScript strict mode 已启用
- 编译验证命令：`npm run build`（在 frontend 目录执行）
- 禁止引入新的第三方依赖

---

### Step 1: 增强 `stores/user.ts`

源文件当前位于 `E:\Code\jobs\project_1\frontend\src\stores\user.ts`。

请用以下完整内容覆盖该文件：

```typescript
// /frontend/src/stores/user.ts
// 职责描述：用户状态管理——Token、用户名、登录/登出/刷新

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('accessToken') || '')
  const refreshTokenVal = ref(localStorage.getItem('refreshToken') || '')
  const username = ref(localStorage.getItem('username') || '')

  const isLoggedIn = computed(() => !!token.value)

  function setToken(accessToken: string, refreshToken: string) {
    token.value = accessToken
    refreshTokenVal.value = refreshToken
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
  }

  function setUsername(name: string) {
    username.value = name
    localStorage.setItem('username', name)
  }

  function logout() {
    token.value = ''
    refreshTokenVal.value = ''
    username.value = ''
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('username')
  }

  return { token, refreshTokenVal, username, isLoggedIn, setToken, setUsername, logout }
})
```

### Step 2: 更新 `NavBar.vue` 等组件中的调用

由于 `isLoggedIn` 现在变成了 computed 属性，而在之前的版本中是一个函数 `isLoggedIn()`。
如果项目中（比如 `NavBar.vue` 或 `Login.vue`）有用到 `userStore.isLoggedIn()` 的地方，请将其修改为 `userStore.isLoggedIn`（如果在 script setup 内部引用需去掉括号，但在 template 中直接用不需要括号，注意如果是 `if (userStore.isLoggedIn())` 需改为 `if (userStore.isLoggedIn)`）。

请使用 `grep` 搜索前端目录下是否有 `isLoggedIn()` 的调用，如果有，修改它。

### 编译验证

在 `E:\Code\jobs\project_1\frontend` 目录执行：
```bash
npx vue-tsc --noEmit
npm run build
```

---

**验证标准：**
1. `stores/user.ts` 更新正确，`isLoggedIn` 变为 computed 属性
2. 全局搜索没有残留的 `isLoggedIn()` 函数调用
3. `vue-tsc --noEmit` 零错误
4. `npm run build` 成功
