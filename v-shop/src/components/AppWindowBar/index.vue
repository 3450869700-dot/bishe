<script setup lang="ts">
// 返回顶部组件，移除了夜间模式切换功能
</script>

<template>
  <div class="app-window-bar">
    <!-- 返回顶部 -->
    <van-back-top right="5vw" bottom="10vh" teleport="body" />
  </div>
</template>

<style lang="less">
.app-window-bar {
  /* 仅保留返回顶部功能，移除了夜间模式按钮样式 */
}
</style>
