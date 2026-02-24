<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const isCollapse = ref(false);

const menuList = [
  {
    path: '/merchant/dashboard',
    name: '工作台',
    icon: 'home-o',
  },
  {
    path: '/merchant/products',
    name: '商品管理',
    icon: 'bag-o',
  },
  {
    path: '/merchant/orders',
    name: '订单管理',
    icon: 'orders-o',
  },
  {
    path: '/merchant/traceability',
    name: '溯源管理',
    icon: 'search',
  },
  {
    path: '/merchant/analysis',
    name: '数据分析',
    icon: 'chart-trending-o',
  },
  {
    path: '/merchant/settings',
    name: '个人中心',
    icon: 'user-o',
  },
];

const currentPath = computed(() => route.path);

function handleMenuClick(path: string) {
  router.push(path);
}

function handleLogout() {
  userStore.logout({ goLogin: true });
}

function goConsumer() {
  userStore.setRole('consumer');
  router.push('/home');
}
</script>

<template>
  <div class="merchant-layout">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed: isCollapse }">
      <div class="logo">
        <img v-if="!isCollapse" src="@/assets/images/logo.png" alt="Logo" />
        <span v-else class="logo-text">V</span>
      </div>
      <div class="switch-role" @click="router.push('/role-select')">
        <van-icon name="arrow-left" />
        <span>返回身份选择</span>
      </div>
      <el-menu
        :default-active="currentPath"
        class="sidebar-menu"
        :collapse="isCollapse"
        router
        background-color="#1a1a2e"
        text-color="#a0a0b0"
        active-text-color="#fff"
      >
        <el-menu-item v-for="item in menuList" :key="item.path" :index="item.path" @click="handleMenuClick(item.path)">
          <van-icon :name="item.icon" size="20" />
          <template #title>{{ item.name }}</template>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-button type="primary" plain size="small" @click="isCollapse = !isCollapse">
          <van-icon :name="isCollapse ? 'expand' : 'contract'" />
          <span>{{ isCollapse ? '展开' : '收起' }}</span>
        </el-button>
        <el-button type="danger" plain size="small" @click="handleLogout">
          <van-icon name="logout" />
          <span>退出登录</span>
        </el-button>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-wrapper">
      <!-- 顶部栏 -->
      <div class="header-bar">
        <div class="breadcrumb">
          <span class="role-tag merchant">商户端</span>
          <span class="page-title">{{ route.meta.title || '工作台' }}</span>
        </div>
        <div class="header-actions">
          <el-badge :value="3" :max="99" class="notification-badge">
            <van-icon name="bell-o" size="22" />
          </el-badge>
          <div class="user-info">
            <van-image round width="36" height="36" src="" />
            <span class="user-name">商户管理员</span>
          </div>
        </div>
      </div>

      <!-- 页面内容 -->
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.merchant-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  width: 220px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;

  &.collapsed {
    width: 64px;

    .logo-text {
      font-size: 24px;
    }

    .switch-role span {
      display: none;
    }
  }
}

.logo {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);

  img {
    width: 40px;
    height: 40px;
    border-radius: 8px;
  }

  .logo-text {
    font-size: 28px;
    font-weight: bold;
    color: #fff;
  }
}

.switch-role {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  color: #a0a0b0;
  cursor: pointer;
  font-size: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.3s;

  &:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.05);
  }

  .van-icon {
    margin-right: 6px;
  }
}

.sidebar-menu {
  flex: 1;
  border-right: none;

  :deep(.el-menu-item) {
    display: flex;
    align-items: center;
    height: 52px;
    padding: 0 20px;
    margin: 4px 8px;
    border-radius: 8px;
    transition: all 0.3s;

    &:hover {
      background: rgba(255, 255, 255, 0.1) !important;
      color: #fff !important;
    }

    &.is-active {
      background: linear-gradient(90deg, #ff7d00 0%, #ff9f43 100%) !important;
      color: #fff !important;
    }

    .van-icon {
      margin-right: 12px;
    }
  }
}

.sidebar-footer {
  padding: 12px;
  display: flex;
  gap: 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);

  .el-button {
    flex: 1;
    padding: 8px;
  }

  :deep(.el-button--primary) {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.2);
    color: #a0a0b0;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
      color: #fff;
    }
  }
}

.main-wrapper {
  flex: 1;
  margin-left: 220px;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.3s;
}

.sidebar.collapsed + .main-wrapper {
  margin-left: 64px;
}

.header-bar {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 99;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 12px;

  .role-tag {
    padding: 4px 12px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;

    &.merchant {
      background: linear-gradient(135deg, #ff7d00 0%, #ff9f43 100%);
      color: #fff;
    }
  }

  .page-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;

  .notification-badge {
    cursor: pointer;
    padding: 8px;
    border-radius: 8px;
    transition: background 0.3s;

    &:hover {
      background: #f5f7fa;
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    padding: 6px 12px;
    border-radius: 8px;
    transition: background 0.3s;

    &:hover {
      background: #f5f7fa;
    }

    .user-name {
      font-size: 14px;
      color: #333;
    }
  }
}

.content {
  flex: 1;
  padding: 24px;
}
</style>
