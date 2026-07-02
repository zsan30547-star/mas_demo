// /frontend/src/main.ts
// 鑱岃矗鎻忚堪锛歏ue 搴旂敤鍏ュ彛锛屾敞鍐?Element Plus / Pinia / Router

import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import 'element-plus/theme-chalk/dark/css-vars.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import './styles/variables.css';

const app = createApp(App);

// 娉ㄥ唽 Element Plus 鍥炬爣
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

app.use(ElementPlus);
app.use(createPinia());
app.use(router);
app.mount('#app');

