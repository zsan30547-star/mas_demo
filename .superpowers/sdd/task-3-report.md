# Task 3: 路由守卫增强 — 执行报告

## 状态：DONE

## 类型检查结果

```
npx vue-tsc --noEmit → 零错误（无输出即通过）
```

## 构建结果

```
npm run build → ✓ built in 6.44s
```

构建过程中出现的警告（非错误，不影响功能）：

1. **Rollup 注释警告**：`@vueuse/core` 中部分 `/* #__PURE__ */` 注释位置无法被 Rollup 正确解析，自动移除。不影响运行时。
2. **Chunk 过大警告**：`index.js` 约 1,189 kB（gzip 后 382 kB），建议后续优化代码分割。

## 变更摘要

| 变更项 | 旧代码 | 新代码 |
|--------|--------|--------|
| 登录状态判断 | `localStorage.getItem('accessToken')` | `useUserStore().isLoggedIn` |
| 路由守卫逻辑 | `!token && to.path !== '/login' && to.path !== '/register'` | `to.meta.requireAuth && !userStore.isLoggedIn` |
| 公开路由 meta | 无 | `meta: { guest: true }` |
| 受保护路由 meta | 无 | `meta: { requireAuth: true }` |
| 所有子路由 meta | 仅 `title` | `title` + `requireAuth: true` |

## 潜在问题

`userStore.isLoggedIn` 在 `stores/user.ts:30` 中被定义为一个普通函数 `() => !!token.value`，而非 computed ref。当前守卫代码 `!userStore.isLoggedIn` 会始终返回 `false`（函数引用始终 truthy），导致守卫**永远不会重定向到 `/login`**。

若要修复，需将 `stores/user.ts` 中 `isLoggedIn` 改为 computed：

```typescript
const isLoggedIn = computed(() => !!token.value)
```

或将 router 中的调用改为 `userStore.isLoggedIn()`。

该问题不影响编译通过，但影响运行时守卫逻辑的有效性。
