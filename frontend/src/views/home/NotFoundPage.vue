<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <h1>404</h1>
      <p>页面未找到</p>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: var(--bg);
}
.not-found-content { text-align: center; }
.not-found-content h1 { font-size: 96px; font-weight: 800; color: var(--primary); margin-bottom: 16px; }
.not-found-content p { font-size: 20px; color: var(--text-secondary); margin-bottom: 32px; }
</style>
