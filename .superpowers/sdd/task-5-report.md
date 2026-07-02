# Task 5 验证报告

## 目标

创建 `useTaskStore` Pinia store，接管任务详情页的状态——任务数据、步骤日志、轮询逻辑、进度计算。

## 产物

| 文件 | 操作 | 说明 |
|------|------|------|
| `frontend/src/stores/task.ts` | 新建 | 任务详情状态管理 store |

## 暴露接口

| 成员 | 类型 | 说明 |
|------|------|------|
| `task` | `Ref<TaskVO \| null>` | 当前任务数据 |
| `stepLogs` | `Ref<StepLogVO[]>` | 步骤日志列表 |
| `loading` | `Ref<boolean>` | 加载状态 |
| `activeStep` | `ComputedRef<number>` | 已成功步骤数（用于 el-steps） |
| `isRunning` | `ComputedRef<boolean>` | 任务是否运行中（pending/running） |
| `currentStepIndex` | `ComputedRef<number>` | 当前待执行步骤索引（首个 pending/running） |
| `fetchTaskDetail(taskId)` | `async function` | 获取任务详情及步骤日志 |
| `startPolling(taskId, intervalMs?)` | `function` | 启动轮询，默认间隔 3000ms，任务结束自动停止 |
| `stopPolling()` | `function` | 停止轮询 |

## 编译验证

| 命令 | 结果 |
|------|------|
| `npx vue-tsc --noEmit` | ✅ 零错误，通过 |
| `npm run build` | ✅ 构建成功 (6.57s) |

## 总结

3 项验证标准全部通过：
1. ✅ 文件正确创建在 `frontend/src/stores/task.ts`
2. ✅ `vue-tsc --noEmit` 零错误
3. ✅ `npm run build` 成功
