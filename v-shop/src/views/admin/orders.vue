<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';

const router = useRouter();

const activeTab = ref('all');
const searchForm = ref({
  orderNo: '',
  merchant: '',
  user: '',
  dateRange: [],
});

const orderList = ref([
  {
    id: 1,
    orderNo: 'DD20240108001',
    merchant: '阳光果园',
    user: { name: '张三', phone: '138****1234' },
    items: [{ name: '红富士苹果', quantity: 2, price: 12.8 }],
    totalAmount: 31.1,
    status: 'completed',
    paymentMethod: '微信支付',
    createTime: '2024-01-08 10:30:25',
  },
  {
    id: 2,
    orderNo: 'DD20240108002',
    merchant: '龙井茶庄',
    user: { name: '李四', phone: '139****5678' },
    items: [{ name: '龙井新茶', quantity: 1, price: 188.0 }],
    totalAmount: 188.0,
    status: 'refund',
    paymentMethod: '支付宝',
    createTime: '2024-01-08 10:15:10',
  },
  {
    id: 3,
    orderNo: 'DD20240108003',
    merchant: '有机蔬菜基地',
    user: { name: '王五', phone: '137****9012' },
    items: [
      { name: '有机青菜', quantity: 2, price: 5.5 },
      { name: '西红柿', quantity: 3, price: 8.9 },
    ],
    totalAmount: 37.7,
    status: 'shipped',
    paymentMethod: '微信支付',
    createTime: '2024-01-08 09:45:33',
  },
]);

const statusList = [
  { value: 'all', label: '全部' },
  { value: 'pending_pay', label: '待支付' },
  { value: 'pending_ship', label: '待发货' },
  { value: 'shipped', label: '已发货' },
  { value: 'completed', label: '已完成' },
  { value: 'refund', label: '退款/售后' },
];

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(100);
const loading = ref(false);

onMounted(() => {
  getOrderList();
});

function getOrderList() {
  loading.value = true;
  setTimeout(() => {
    loading.value = false;
    showToast('加载完成');
  }, 300);
}

function handleTabChange(tab: string) {
  activeTab.value = tab;
  currentPage.value = 1;
  getOrderList();
}

function onViewDetail(id: number) {
  router.push(`/admin/orders/detail?id=${id}`);
}

function onCloseOrder(order: (typeof orderList.value)[0]) {
  showToast('订单已关闭');
}

function onProcessRefund(order: (typeof orderList.value)[0]) {
  showToast('退款处理完成');
}

function onViewLogistics(order: (typeof orderList.value)[0]) {
  router.push(`/admin/orders/logistics?id=${order.id}`);
}

function handlePageChange(page: number) {
  currentPage.value = page;
  getOrderList();
}

function getStatusType(status: string): string {
  const map: Record<string, string> = {
    pending_pay: 'warning',
    pending_ship: 'primary',
    shipped: 'success',
    completed: 'success',
    refund: 'danger',
  };
  return map[status] || 'info';
}

function getStatusText(status: string): string {
  const map: Record<string, string> = {
    pending_pay: '待支付',
    pending_ship: '待发货',
    shipped: '已发货',
    completed: '已完成',
    refund: '退款中',
  };
  return map[status] || status;
}
</script>

<template>
  <div class="order-page">
    <div class="tabs-section">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane v-for="item in statusList" :key="item.value" :label="item.label" :name="item.value" />
      </el-tabs>
    </div>

    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="商户">
          <el-input v-model="searchForm.merchant" placeholder="请输入商户名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="用户">
          <el-input v-model="searchForm.user" placeholder="请输入用户姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary">搜索</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="order-list">
      <div v-for="order in orderList" :key="order.id" class="order-card">
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">{{ order.orderNo }}</span>
            <span class="order-time">{{ order.createTime }}</span>
          </div>
          <el-tag :type="getStatusType(order.status)" size="small">{{ getStatusText(order.status) }}</el-tag>
        </div>

        <div class="order-content">
          <div class="order-meta">
            <span class="merchant">{{ order.merchant }}</span>
            <span class="user">{{ order.user.name }} {{ order.user.phone }}</span>
          </div>
          <div class="order-items">
            <div v-for="(item, index) in order.items" :key="index" class="item">
              <span class="item-name">{{ item.name }}</span>
              <span class="item-qty">x{{ item.quantity }}</span>
              <span class="item-price">¥{{ item.price.toFixed(2) }}</span>
            </div>
          </div>
          <div class="order-total">
            实付 <span class="amount">¥{{ order.totalAmount.toFixed(2) }}</span>
            <span class="payment">{{ order.paymentMethod }}</span>
          </div>
        </div>

        <div class="order-footer">
          <div class="order-actions">
            <el-button size="small" @click="onViewDetail(order.id)">查看详情</el-button>
            <template v-if="order.status === 'refund'">
              <el-button type="success" size="small" @click="onProcessRefund(order)">处理退款</el-button>
            </template>
            <template v-else-if="order.status === 'pending_pay'">
              <el-button type="danger" size="small" @click="onCloseOrder(order)">关闭订单</el-button>
            </template>
            <template v-else-if="order.status === 'shipped'">
              <el-button size="small" @click="onViewLogistics(order)">查看物流</el-button>
            </template>
          </div>
        </div>
      </div>
    </div>

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
  name: 'AdminOrders',
};
</script>

<style lang="less" scoped>
.order-page {
  max-width: 1400px;
  margin: 0 auto;
}

.tabs-section {
  background: #fff;
  border-radius: 12px 12px 0 0;
  padding: 0 20px;
  margin-bottom: 1px;
}

.search-section {
  background: #fff;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.order-list {
  background: #fff;
}

.order-card {
  border-bottom: 1px solid #f0f0f0;
  padding: 20px;

  &:last-child {
    border-bottom: none;
  }
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f5f5f5;
}

.order-info {
  display: flex;
  gap: 16px;

  .order-no {
    font-size: 14px;
    font-weight: 500;
    color: #333;
  }

  .order-time {
    font-size: 12px;
    color: #999;
  }
}

.order-content {
  margin-bottom: 12px;
}

.order-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 13px;

  .merchant {
    color: #0080ff;
    font-weight: 500;
  }

  .user {
    color: #999;
  }
}

.order-items {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;

  .item {
    display: flex;
    align-items: center;
    padding: 4px 0;
    font-size: 14px;

    .item-name {
      flex: 1;
      color: #333;
    }

    .item-qty {
      color: #999;
      margin-right: 16px;
    }

    .item-price {
      color: #ff7d00;
      font-weight: 500;
    }
  }
}

.order-total {
  text-align: right;
  font-size: 14px;
  color: #666;

  .amount {
    font-size: 18px;
    font-weight: 600;
    color: #ff7d00;
    margin-right: 12px;
  }

  .payment {
    color: #999;
    font-size: 12px;
  }
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 20px;
  background: #fff;
}
</style>
