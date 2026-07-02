# Task 9: CSS 变量体系 + 暗黑模式基础

**Goal:** 建立全局 CSS 变量文件，并引入到项目中。这是为视觉升级和暗黑模式支持打基础。

**Files:**
- Create: `frontend/src/styles/variables.scss`
- Modify: `frontend/src/main.ts`

**Global Constraints:**
- TypeScript strict mode 已启用
- 编译验证命令：`npm run build`（在 frontend 目录执行）
- 禁止引入新的第三方依赖

---

### Step 1: 创建 `variables.scss`

我们需要在 `frontend/src/` 下创建 `styles` 目录，并写入 `variables.scss`。该文件当前应当被创建为 `E:\Code\jobs\project_1\frontend\src\styles\variables.scss`。

请用以下完整内容覆盖/创建该文件：

```scss
// /frontend/src/styles/variables.scss
// 职责描述：全局 CSS 变量——色板、间距、暗黑模式基础

:root {
  // Primary
  --color-primary: #409eff;
  --color-primary-light: #79bbff;
  --color-primary-dark: #337ecc;

  // Surface
  --color-bg: #f5f7fa;
  --color-surface: #ffffff;
  --color-surface-hover: #fafafa;

  // Text
  --color-text-primary: #303133;
  --color-text-regular: #606266;
  --color-text-secondary: #909399;
  --color-text-placeholder: #c0c4cc;

  // Border
  --color-border: #e4e7ed;
  --color-border-light: #ebeef5;

  // Status
  --color-success: #67c23a;
  --color-warning: #e6a23c;
  --color-danger: #f56c6c;
  --color-info: #909399;

  // Shadow
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.12);

  // Glassmorphism
  --glass-bg: rgba(255, 255, 255, 0.72);
  --glass-blur: 12px;
  --glass-border: rgba(255, 255, 255, 0.3);

  // Radius
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;

  // Transitions
  --transition-fast: 0.15s ease;
  --transition-normal: 0.25s ease;
  --transition-slow: 0.4s ease;
}
```

### Step 2: 更新 `main.ts` 引入样式

在 `E:\Code\jobs\project_1\frontend\src\main.ts` 中引入该文件。

覆盖为：

```typescript
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import './styles/variables.scss'

const app = createApp(App)

app.use(ElementPlus)
app.use(createPinia())
app.use(router)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
```

### 验证

在 `E:\Code\jobs\project_1\frontend` 目录执行：
```bash
npx vue-tsc --noEmit
npm run build
```
（由于用到了 scss，请确保项目原本支持 sass。如果执行 `npm run build` 报找不到 sass 编译器错误，说明原项目中未预装 `sass`，请记录在 Report 中，不用强行添加第三方包依赖。我们可以随时退回用普通 `.css` 的方案，但根据常规 Vite+ElementPlus 结构默认应已包含）。