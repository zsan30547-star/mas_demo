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
          meta: { title: '模型 & Agent 管理', requireAuth: true },
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
        {
          path: 'knowledge',
          name: 'KnowledgeBase',
          component: () => import('../views/knowledge/KnowledgeBase.vue'),
          meta: { title: '知识库', requireAuth: true },
        },
        {
          path: 'settings',
          name: 'UserSettings',
          component: () => import('../views/user/UserSettings.vue'),
          meta: { title: '个人设置', requireAuth: true },
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
