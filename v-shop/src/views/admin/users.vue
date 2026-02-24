<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';

const router = useRouter();

const searchForm = ref({
  name: '',
  phone: '',
  status: '',
  dateRange: [],
});

const userList = ref([
  {
    id: 1001,
    nick: '用户张三',
    avatar: '',
    phone: '138****1234',
    region: '浙江 杭州',
    orders: 15,
    totalSpent: '¥1,286',
    lastOrderTime: '2024-01-08',
    status: 'active',
    createTime: '2023-06-15',
    lastLoginTime: '2024-01-08 10:30',
  },
  {
    id: 1002,
    nick: '李四用户',
    avatar: '',
    phone: '139****5678',
    region: '江苏 南京',
    orders: 8,
    totalSpent: '¥568',
    lastOrderTime: '2024-01-07',
    status: 'active',
    createTime: '2023-08-20',
    lastLoginTime: '2024-01-07 15:20',
  },
  {
    id: 1003,
    nick: '王五',
    avatar: '',
    phone: '137****9012',
    region: '上海',
    orders: 25,
    totalSpent: '¥3,560',
    lastOrderTime: '2024-01-08',
    status: 'active',
    createTime: '2023-03-10',
    lastLoginTime: '2024-01-08 09:45',
  },
  {
    id: 1004,
    nick: '赵六',
    avatar: '',
    phone: '136****3456',
    region: '北京',
    orders: 3,
    totalSpent: '¥189',
    lastOrderTime: '2023-12-20',
    status: 'active',
    createTime: '2023-11-05',
    lastLoginTime: '2024-01-01 12:00',
  },
  {
    id: 1005,
    nick: '钱七',
    avatar: '',
    phone: '135****7890',
    region: '广东 广州',
    orders: 0,
    totalSpent: '¥0',
    lastOrderTime: '-',
    status: 'disabled',
    createTime: '2024-01-01',
    lastLoginTime: '2024-01-01 08:00',
  },
]);

const statusList = [
  { value: '', label: '全部状态' },
  { value: 'active', label: '正常' },
  { value: 'disabled', label: '已禁用' },
];

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(100);
const loading = ref(false);

onMounted(() => {
  getUserList();
});

function getUserList() {
  loading.value = true;
  setTimeout(() => {
    loading.value = false;
    showToast('加载完成');
  }, 300);
}

function onSearch() {
  currentPage.value = 1;
  getUserList();
}

function onReset() {
  searchForm.value = { name: '', phone: '', status: '', dateRange: [] };
  onSearch();
}

function onViewDetail(id: number) {
  router.push(`/admin/users/detail?id=${id}`);
}

function onToggleStatus(user: (typeof userList.value)[0]) {
  const action = user.status === 'active' ? '禁用' : '启用';
  user.status = user.status === 'active' ? 'disabled' : 'active';
  showToast({
    type: 'success',
    message: `${action}成功`,
  });
}

function onViewOrders(id: number) {
  router.push(`/admin/orders?userId=${id}`);
}

function handlePageChange(page: number) {
  currentPage.value = page;
  getUserList();
}

function getStatusType(status: string): string {
  return status === 'active' ? 'success' : 'info';
}
</script>

<template>
  <div class="user-page">
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户昵称">
          <el-input v-model="searchForm.name" placeholder="请输入用户昵称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option v-for="item in statusList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">搜索</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户统计 -->
    <div class="stats-row">
      <div class="stat-item">
        <span class="value">{{ userList.length }}</span>
        <span class="label">用户总数</span>
      </div>
      <div class="stat-item">
        <span class="value">{{ userList.filter((u) => u.status === 'active').length }}</span>
        <span class="label">活跃用户</span>
      </div>
      <div class="stat-item">
        <span class="value">{{ userList.reduce((sum, u) => sum + u.orders, 0) }}</span>
        <span class="label">总订单数</span>
      </div>
      <div class="stat-item">
        <span class="value"
          >¥{{
            (userList.reduce((sum, u) => sum + parseFloat(u.totalSpent.replace('¥', '')), 0) / 10000).toFixed(2)
          }}万</span
        >
        <span class="label">总消费金额</span>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="user-list">
      <el-table v-loading="loading" :data="userList" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
        <el-table-column label="用户信息" min-width="200">
          <template #default="{ row }">
            <div class="user-info">
              <div class="user-avatar">
                {{ row.nick.charAt(0) }}
              </div>
              <div class="user-detail">
                <div class="user-nick">{{ row.nick }}</div>
                <div class="user-phone">{{ row.phone }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="region" label="地区" width="120" />
        <el-table-column prop="orders" label="订单数" width="100" />
        <el-table-column prop="totalSpent" label="消费金额" width="120">
          <template #default="{ row }">
            <span class="spent-amount">{{ row.totalSpent }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastOrderTime" label="最近订单" width="120" />
        <el-table-column prop="lastLoginTime" label="最近登录" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.status === 'active' ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="onViewDetail(row.id)">查看</el-button>
            <el-button type="primary" size="small" text @click="onViewOrders(row.id)">订单</el-button>
            <el-button
              :type="row.status === 'active' ? 'warning' : 'success'"
              size="small"
              text
              @click="onToggleStatus(row)"
            >
              {{ row.status === 'active' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'AdminUsers',
};
</script>

<style lang="less" scoped>
.user-page {
  max-width: 1400px;
  margin: 0 auto;
}

.search-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stats-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .value {
    display: block;
    font-size: 28px;
    font-weight: bold;
    color: #0080ff;
  }

  .label {
    font-size: 14px;
    color: #999;
    margin-top: 4px;
  }
}

.user-list {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0080ff 0%, #67c23a 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}

.user-detail {
  flex: 1;
}

.user-nick {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.user-phone {
  font-size: 12px;
  color: #999;
}

.spent-amount {
  font-weight: 600;
  color: #ff7d00;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
</style>
