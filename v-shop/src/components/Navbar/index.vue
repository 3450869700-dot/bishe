<script lang="ts">
export default {
  name: 'Navbar',
};
</script>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router';
import { ref, watch } from 'vue';
import { ShoppingCart, User, Ticket, Star } from '@element-plus/icons-vue';

// Props
const props = defineProps({
  showLoginButtons: {
    type: Boolean,
    default: true,
  },
  showRoleSelect: {
    type: Boolean,
    default: false,
  },
  isOmnipotent: {
    type: Boolean,
    default: false,
  },
});

const router = useRouter();
const route = useRoute();
const activeIndex = ref(route.path);

// 监听路由变化，更新激活的菜单
watch(
  () => route.path,
  (newPath) => {
    activeIndex.value = newPath;
  },
);

// 处理菜单选择
function handleMenuSelect(key: string) {
  activeIndex.value = key;
  router.push(key);
}

// 处理登录
function handleLogin() {
  router.push('/login');
}

// 处理注册
function handleRegister() {
  router.push('/register');
}

// 处理返回身份选择
function goBackToRoleSelect() {
  router.push('/role-select');
}
</script>

<template>
  <el-header class="top-header">
    <div class="header-content">
      <div class="logo">
        <!-- Logo removed -->
      </div>
      <el-menu
        :default-active="activeIndex"
        class="main-menu"
        mode="horizontal"
        background-color="#fff"
        text-color="#333"
        router
        @select="handleMenuSelect"
      >
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/category">分类</el-menu-item>
        <el-menu-item index="/cart">
          <el-icon><ShoppingCart /></el-icon>
          购物车
        </el-menu-item>
        <el-menu-item index="/coupon">
          <el-icon><Ticket /></el-icon>
          优惠券
        </el-menu-item>
        <el-menu-item index="/integral">
          <el-icon><Star /></el-icon>
          积分商城
        </el-menu-item>
        <el-menu-item index="/mine">
          <el-icon><User /></el-icon>
          个人中心
        </el-menu-item>
      </el-menu>
      <div class="header-actions">
        <template v-if="props.showRoleSelect && props.isOmnipotent">
          <el-button type="primary" size="small" @click="handleLogin">返回登录界面</el-button>
        </template>
        <template v-else-if="props.showLoginButtons">
          <el-button type="primary" size="small" @click="handleLogin">登录</el-button>
          <el-button type="default" size="small" @click="handleRegister">注册</el-button>
        </template>
      </div>
    </div>
  </el-header>
</template>

<style lang="less" scoped>
/* 顶部导航 - 统一所有页面 */
.top-header {
  width: 100% !important;
  height: 70px !important;
  background-color: #ffffff !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08) !important;
  position: sticky !important;
  top: 0 !important;
  z-index: 100 !important;
  display: flex !important;
  align-items: stretch !important;
  margin: 0 !important;
  padding: 0 !important;
  box-sizing: border-box !important;
  overflow: visible !important;
}

.header-content {
  width: 100% !important;
  max-width: 1760px !important;
  margin: 0 auto !important;
  padding: 0 20px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: space-between !important;
  box-sizing: border-box !important;
  overflow: visible !important;
}

.logo {
  display: flex !important;
  align-items: center !important;
  margin: 0 !important;
  padding: 0 !important;
}

.main-menu {
  flex: 1 !important;
  margin: 0 40px !important;
  background-color: transparent !important;
  padding: 0 !important;
  box-sizing: border-box !important;
  height: 70px !important;
  min-height: 70px !important;
  max-height: 70px !important;

  /* 重置 Element Plus 菜单样式 */
  :deep(.el-menu) {
    background-color: transparent !important;
    border-bottom: none !important;
    height: 70px !important;
    min-height: 70px !important;
    max-height: 70px !important;
    overflow: visible !important;
    display: flex !important;
    align-items: flex-end !important;
  }

  :deep(.el-menu--horizontal) {
    background-color: transparent !important;
    border-bottom: none !important;
    overflow: visible !important;
    display: flex !important;
    align-items: flex-end !important;
    height: 70px !important;
  }

  :deep(.el-menu-item) {
    background-color: transparent !important;
    font-size: 16px !important;
    font-weight: 500 !important;
    height: 70px !important;
    padding: 0 16px !important;
    margin: 0 4px !important;
    box-sizing: border-box !important;
    border: none !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    position: relative !important;
    transition: all 0.3s ease !important;

    &::after {
      content: '' !important;
      position: absolute !important;
      bottom: 0 !important;
      left: 0 !important;
      right: 0 !important;
      width: 100% !important;
      height: 3px !important;
      background-color: transparent !important;
      transition: background-color 0.3s ease !important;
    }

    &:hover {
      background-color: rgba(0, 0, 0, 0.05) !important;
      color: var(--color-primary) !important;
      border-radius: 0 !important;
    }
  }

  :deep(.el-menu-item.is-active) {
    color: var(--color-primary) !important;
    background-color: transparent !important;
    height: 70px !important;

    &::after {
      background-color: var(--color-primary) !important;
    }
  }

  /* 移除按钮焦点高亮效果 */
  :deep(.el-menu-item:focus),
  :deep(.el-menu-item:focus-visible) {
    outline: none !important;
    box-shadow: none !important;
  }
}

.header-actions {
  display: flex !important;
  gap: 10px !important;
  margin: 0 !important;
  padding: 0 !important;

  :deep(.el-button) {
    font-size: 16px !important;
    padding: 8px 16px !important;
    height: auto !important;
  }

  :deep(.el-button--primary) {
    background-color: var(--color-primary) !important;
    border-color: var(--color-primary) !important;

    &:hover,
    &:focus,
    &:active {
      background-color: var(--color-primary) !important;
      border-color: var(--color-primary) !important;
      opacity: 0.9;
    }
  }
}

/* 修复Element Plus图标过大问题 */
:deep(.el-icon),
:deep(.element-plus-icon) {
  font-size: 16px !important;
  width: auto !important;
  height: auto !important;
  min-width: auto !important;
  min-height: auto !important;
  max-width: 20px !important;
  max-height: 20px !important;
  line-height: normal !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
}

/* 确保按钮图标大小合适 */
:deep(.el-button .el-icon),
:deep(.el-button .element-plus-icon) {
  font-size: 14px !important;
  margin-right: 4px !important;
  max-width: 16px !important;
  max-height: 16px !important;
}
</style>
