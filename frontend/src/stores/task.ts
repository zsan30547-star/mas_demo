// /frontend/src/stores/task.ts
// 职责描述：任务详情状态管理——使用 SSE 接收后端完成事件与实时流式输出
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getTaskDetail } from '../api/task'
import { getWorkflow } from '../api/workflow'
import type { TaskVO, StepLogVO } from '../types/task'

export const useTaskStore = defineStore('task', () => {
  const task = ref<TaskVO | null>(null)
  const stepLogs = ref<StepLogVO[]>([])
  // 预加载的静态工作流节点
  const workflowNodes = ref<any[]>([]) 
  const loading = ref(false)
  let eventSource: EventSource | null = null

  // 抛弃平滑假动画，计算真实激活的步骤（用于 timeline 进度）
  const activeStep = computed(() => {
    if (!workflowNodes.value.length) return 0
    if (task.value?.status === 'completed') return workflowNodes.value.length + 1
    
    // 如果还没完成，找最后一个成功的步骤的索引 + 1，或者正在运行的步骤的索引
    const lastSuccessIdx = stepLogs.value.map(s => s.status).lastIndexOf('success')
    const runningIdx = stepLogs.value.findIndex(s => s.status === 'running')
    
    if (runningIdx !== -1) return runningIdx
    if (lastSuccessIdx !== -1) return lastSuccessIdx + 1
    return 0
  })

  const isRunning = computed(() =>
    task.value?.status === 'pending' || task.value?.status === 'running',
  )

  async function fetchTaskDetail(taskId: number, isInitial = false) {
    if (isInitial) loading.value = true
    try {
      const res = await getTaskDetail(taskId)
      task.value = res.data.task
      
      // 注意：这里需要合并，不能直接覆盖，因为流式输出可能正在进行
      // 如果后端传来的数据比较旧，而前端已经通过 stream 拿到了更新的内容，要以新内容为主
      // 简单策略：仅覆盖状态和错误，保留前端已经累积的 output
      const newLogs = res.data.stepLogs || []
      if (stepLogs.value.length === 0) {
        stepLogs.value = newLogs
      } else {
        newLogs.forEach((newLog: StepLogVO) => {
          const existingLog = stepLogs.value.find(l => l.stepIndex === newLog.stepIndex)
          if (existingLog) {
            existingLog.status = newLog.status
            existingLog.error = newLog.error
            existingLog.durationMs = newLog.durationMs
            // 如果后端还没有output（可能还在执行），保留前端累积的
            if (!existingLog.output && newLog.output) {
                existingLog.output = newLog.output
            } else if (newLog.status === 'success') {
                // 如果后端明确执行完了，强制覆盖最终结果，保证一致性
                existingLog.output = newLog.output
            }
          } else {
            stepLogs.value.push(newLog)
          }
        })
      }
      
      // 如果是第一次加载且 task 存在，去抓取它对应的工作流模板信息
      if (isInitial && task.value && task.value.workflowId) {
        try {
           const wfRes = await getWorkflow(task.value.workflowId)
           if (wfRes.data && wfRes.data.steps) {
              workflowNodes.value = typeof wfRes.data.steps === 'string' 
                ? JSON.parse(wfRes.data.steps) 
                : wfRes.data.steps;
           }
        } catch (e) {
           console.error('获取工作流节点失败', e)
        }
      }
    } finally {
      if (isInitial) loading.value = false
    }
  }

  function startPolling(taskId: number) {
    stopPolling()
    // 1. 首次全量拉取
    fetchTaskDetail(taskId, true).then(() => {
        // 2. 如果任务未完成，建立 SSE 监听
        if (isRunning.value) {
           const token = localStorage.getItem('accessToken')
           eventSource = new EventSource(`/api/tasks/${taskId}/stream?token=${encodeURIComponent(token || '')}`)
           
           eventSource.addEventListener('connected', (e) => {
               console.log('SSE connected:', e.data)
           })
           
           // 监听真正的流式字符事件
           eventSource.addEventListener('stream_chunk', (e) => {
               try {
                   const data = JSON.parse(e.data)
                   // console.log("Stream data:", data) // 如果需要调试可以打开
                   
                   // 如果是收到 "步骤开始" 的事件
                   if (data.type === 'step_start') {
                       const stepIdx = data.stepIndex
                       let existingLog = stepLogs.value.find(l => l.stepIndex === stepIdx)
                       if (!existingLog) {
                           // 前端提前创建占位日志
                           existingLog = {
                               stepIndex: stepIdx,
                               agentName: '...',
                               agentType: '',
                               input: '',
                               output: '',
                               status: 'running',
                               durationMs: 0,
                               error: ''
                           }
                           stepLogs.value.push(existingLog)
                       } else {
                           existingLog.status = 'running'
                       }
                   } 
                   // 如果收到的是 "流式字符"
                   else if (data.type === 'chunk') {
                       const stepIdx = data.stepIndex
                       let existingLog = stepLogs.value.find(l => l.stepIndex === stepIdx)
                       
                       // 防止乱序，如果当前步骤还没被创建，我们先创建它
                       if (!existingLog) {
                            existingLog = {
                               stepIndex: stepIdx,
                               agentName: '...',
                               agentType: '',
                               input: '',
                               output: '',
                               status: 'running',
                               durationMs: 0,
                               error: ''
                           }
                           stepLogs.value.push(existingLog)
                       }
                       // 真正的流式追加！
                       existingLog.output += data.content
                       // 只要在输出，状态必须是运行中
                       existingLog.status = 'running'
                   }
               } catch (err) {
                   console.error("Failed to parse stream chunk:", err)
               }
           })

           // 当后端处理完毕（保存了最终状态）后发送 update 事件
           eventSource.addEventListener('task_update', (e) => {
               console.log('Received task_update event:', e.data)
               // 收到通知后，重新拉取一次最终的详细数据，覆盖前端的缓存并更新最终状态
               fetchTaskDetail(taskId, false).then(() => {
                   if (!isRunning.value) stopPolling()
               })
           })
           
           eventSource.onerror = (e) => {
               console.error('SSE Error:', e)
               stopPolling()
               // 降级：如果 SSE 出错断开，可以用 setTimeout 重新拉取一次保底
               setTimeout(() => fetchTaskDetail(taskId, false), 3000)
           }
        }
    })
  }

  function stopPolling() {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
  }

  return {
    task, stepLogs, workflowNodes, loading, activeStep, isRunning,
    fetchTaskDetail, startPolling, stopPolling,
  }
})