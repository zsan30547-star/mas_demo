# Task 4 验证报告

## 任务
创建 `useWorkflowStore` Pinia store —— `frontend/src/stores/workflow.ts`

## 创建文件
- `frontend/src/stores/workflow.ts` — 已创建

## 暴露接口
| 状态/方法 | 类型 | 说明 |
|-----------|------|------|
| `steps` | `Ref<StepItem[]>` | 工作流步骤列表 |
| `agents` | `Ref<AgentVO[]>` | 可用 Agent 列表 |
| `selectedStepIndex` | `Ref<number \| null>` | 当前选中步骤索引 |
| `name` | `Ref<string>` | 工作流名称 |
| `description` | `Ref<string>` | 工作流描述 |
| `selectedStep` | `ComputedRef<StepItem \| null>` | 当前选中步骤（计算属性） |
| `addStep()` | 函数 | 新增步骤，自动选中 |
| `removeStep(index)` | 函数 | 删除步骤，重排 order |
| `reorderSteps(newOrder)` | 函数 | 拖拽排序后更新顺序 |
| `selectStep(index)` | 函数 | 选中步骤 |
| `setAgents(list)` | 函数 | 设置 Agent 列表 |
| `loadFromSteps(rawSteps)` | 函数 | 从后端数据加载步骤 |
| `toSubmitSteps()` | 函数 | 剥离 key 后返回提交数据 |
| `reset()` | 函数 | 重置所有状态 |

## 编译验证

### `npx vue-tsc --noEmit`
**结果：零错误**（无输出）

### `npm run build`
**结果：构建成功**（✓ built in 6.22s）

- 1703 modules transformed
- 仅存在 Rollup 对 `@vueuse/core` 注释位置的提示（可忽略）和 chunk 大小建议（可忽略），均不影响正常构建

## 结论
任务 4 完成，所有验证标准通过。
