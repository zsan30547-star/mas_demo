# Task 3: 路由守卫增强

**Goal:** 将路由守卫从直接读取 `localStorage` 改为使用 Pinia `useUserStore`，并为所有子路由添加 `meta.requireAuth` 标记，以提供更清晰的权限控制。

**Files:**
- Modify: `frontend/src/router/index.ts`

**Global Constraints:**
- TypeScript strict mode 已启用
- 编译验证命令：`npm run build`（在 frontend 目录执行）
- 现有页面路由和组件结构保持不变，不改变 URL

---

### 操作步骤

源文件当前位于 `E:\Code\jobs\project_1\frontend\src\router\index.ts`。

请用以下完整内容覆盖该文件：

```typescript
// /frontend/src/router/index.ts
// 职责描述：Vue Router 配置 + 导航守卫（使用 Pinia Store 判断登录状态）

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/login/Login.vue'),
      meta: { guest: true },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/register/Register.vue'),
      meta: { guest: true },
    },
    {
      path: '/',
      component: () => import('../components/layout/AppLayout.vue'),
      redirect: '/dashboard',
      meta: { requireAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/dashboard/Dashboard.vue'),
          meta: { title: '仪表盘', requireAuth: true },
        },
        {
          path: 'models',
          name: 'ModelList',
          component: () => import('../views/model/ModelConfigList.vue'),
          meta: { title: '模型管理', requireAuth: true },
        },
        {
          path: 'agents',
          name: 'AgentList',
          component: () => import('../views/agent/AgentList.vue'),
          meta: { title: 'Agent 管理', requireAuth: true },
        },
        {
          path: 'agents/new',
          name: 'AgentCreate',
          component: () => import('../views/agent/AgentForm.vue'),
          meta: { title: '新建 Agent', requireAuth: true },
        },
        {
          path: 'agents/:id/edit',
          name: 'AgentEdit',
          component: () => import('../views/agent/AgentForm.vue'),
          meta: { title: '编辑 Agent', requireAuth: true },
        },
        {
          path: 'workflows',
          name: 'WorkflowList',
          component: () => import('../views/workflow/WorkflowList.vue'),
          meta: { title: '工作流模板', requireAuth: true },
        },
        {
          path: 'workflows/new',
          name: 'WorkflowCreate',
          component: () => import('../views/workflow/WorkflowEditor.vue'),
          meta: { title: '新建工作流', requireAuth: true },
        },
        {
          path: 'workflows/:id/edit',
          name: 'WorkflowEdit',
          component: () => import('../views/workflow/WorkflowEditor.vue'),
          meta: { title: '编辑工作流', requireAuth: true },
        },
        {
          path: 'tasks/new',
          name: 'TaskSubmit',
          component: () => import('../views/task/TaskSubmit.vue'),
          meta: { title: '提交任务', requireAuth: true },
        },
        {
          path: 'tasks',
          name: 'TaskHistory',
          component: () => import('../views/task/TaskHistory.vue'),
          meta: { title: '任务历史', requireAuth: true },
        },
        {
          path: 'tasks/:id',
          name: 'TaskDetail',
          component: () => import('../views/task/TaskDetail.vue'),
          meta: { title: '任务详情', requireAuth: true },
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.requireAuth && !userStore.isLoggedIn) {
    return '/login'
  }
})

export default router
```

### 编译验证

在 `E:\Code\jobs\project_1\frontend` 目录执行：
```bash
npx vue-tsc --noEmit
npm run build
```

注意：`userStore.isLoggedIn` 现在是 computed ref，模板中自动解包，但在 `beforeEach` 脚本中直接使用即可（`!!token.value` 返回 boolean）。

---

**验证标准：**
1. 路由守卫从 `localStorage.getItem` 改为 `useUserStore().isLoggedIn`
2. 所有需要登录的页面添加 `meta.requireAuth: true`
3. 公开页面（login/register）标记 `meta.guest: true`
4. `vue-tsc --noEmit` 零错误
5. `npm run build` 成功
