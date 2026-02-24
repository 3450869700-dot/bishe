<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore, UserRole } from '@/store/modules/user';

const router = useRouter();
const userStore = useUserStore();

const isOmnipotent = computed(() => userStore.getIsOmnipotent);
const hasLogin = computed(() => !!userStore.getToken);

const allRoleList = [
  {
    value: 'consumer' as UserRole,
    label: '消费者',
    desc: '我是顾客，我要购物',
    icon: 'user-o',
    color: '#1ba784',
    path: '/home',
  },
  {
    value: 'merchant' as UserRole,
    label: '商户',
    desc: '我是商家，我要管理店铺',
    icon: 'shop-o',
    color: '#ff7d00',
    path: '/merchant/dashboard',
  },
  {
    value: 'admin' as UserRole,
    label: '管理员',
    desc: '我是管理员，我要管理平台',
    icon: 'setting-o',
    color: '#0080ff',
    path: '/admin/dashboard',
  },
];

const roleList = computed(() => {
  if (isOmnipotent.value) {
    // 万能账号模式下显示所有角色（包括管理员）
    return allRoleList;
  } else {
    // 非万能账号模式下过滤掉管理员角色
    return allRoleList.filter((item) => item.value !== 'admin');
  }
});

function onRoleSelect(role: UserRole, path: string) {
  userStore.setRole(role);
  // 确保万能账户状态被正确设置
  const isOmnipotentNow = userStore.getIsOmnipotent;
  console.log('Role selected:', role, 'Path:', path, 'Is omnipotent:', isOmnipotentNow);
  if (isOmnipotentNow) {
    // 直接跳转，不经过登录页面
    router.push(path);
  } else {
    router.push('/login?redirect=' + encodeURIComponent(path));
  }
}

function enableOmnipotent() {
  if (isOmnipotent.value) {
    userStore.setOmnipotent(false);
  } else {
    userStore.setOmnipotent(true);
    userStore.setRole('admin');
  }
}
</script>

<template>
  <div class="container role-select-page">
    <div class="header">
      <!-- Logo removed -->
      <h1 class="title">V-Shop</h1>
      <p class="subtitle">选择您的身份</p>
    </div>
    <div class="role-list">
      <div
        v-for="item in roleList"
        :key="item.value"
        class="role-item"
        :style="{ '--role-color': item.color }"
        @click="onRoleSelect(item.value, item.path)"
      >
        <div class="role-icon">
          <van-icon :name="item.icon" size="40" />
        </div>
        <div class="role-info">
          <div class="role-label">{{ item.label }}</div>
          <div class="role-desc">{{ item.desc }}</div>
        </div>
        <div class="role-arrow">
          <van-icon name="arrow" />
        </div>
      </div>
    </div>
    <div class="debug-section">
      <van-button v-if="!isOmnipotent" type="primary" size="small" @click="enableOmnipotent">
        启用万能账号（调试用）
      </van-button>
      <van-button v-else type="warning" size="small" @click="enableOmnipotent"> 关闭万能账号 </van-button>
    </div>
  </div>
</template>

<style lang="less" scoped>
.role-select-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
  padding: 40px 20px;
}

.header {
  text-align: center;
  margin-bottom: 50px;

  .logo {
    width: 80px;
    height: 80px;
    margin: 0 auto 20px;
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .title {
    font-size: 32px;
    font-weight: bold;
    color: #333;
    margin: 0 0 10px;
  }

  .subtitle {
    font-size: 16px;
    color: #999;
    margin: 0;
  }
}

.role-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.role-item {
  display: flex;
  align-items: center;
  padding: 24px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    border-color: var(--role-color);
  }

  &:active {
    transform: translateY(0);
  }
}

.role-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--role-color) 0%, rgba(255, 255, 255, 0.3) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.role-info {
  flex: 1;

  .role-label {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin-bottom: 4px;
  }

  .role-desc {
    font-size: 14px;
    color: #999;
  }
}

.role-arrow {
  color: #ccc;
  font-size: 16px;
}

.debug-section {
  margin-top: 40px;
  text-align: center;

  .omnipotent-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 20px;
    font-size: 14px;
    font-weight: 500;
  }
}
</style>
