<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast } from 'vant';

const router = useRouter();
const route = useRoute();

const activeTab = ref(route.query.status?.toString() || 'all');
const searchForm = ref({
  orderNo: '',
  customerName: '',
  dateRange: [],
});

const orderList = ref([
  {
    id: 1,
    orderNo: 'DD20240108001',
    customer: { name: '张三', phone: '138****1234' },
    items: [
      { name: '红富士苹果', quantity: 2, price: 12.8 },
      { name: '有机青菜', quantity: 1, price: 5.5 },
    ],
    totalAmount: 31.1,
    status: 'pending_ship',
    statusText: '待发货',
    createTime: '2024-01-08 10:30:25',
    paymentMethod: '微信支付',
  },
  {
    id: 2,
    orderNo: 'DD20240108002',
    customer: { name: '李四', phone: '139****5678' },
    items: [{ name: '龙井新茶', quantity: 1, price: 188.0 }],
    totalAmount: 188.0,
    status: 'pending_pay',
    statusText: '待支付',
    createTime: '2024-01-08 10:15:10',
    paymentMethod: '待支付',
  },
  {
    id: 3,
    orderNo: 'DD20240108003',
    customer: { name: '王五', phone: '137****9012' },
    items: [{ name: '红心火龙果', quantity: 3, price: 15.9 }],
    totalAmount: 47.7,
    status: 'shipped',
    statusText: '已发货',
    createTime: '2024-01-08 09:45:33',
    paymentMethod: '支付宝',
  },
  {
    id: 4,
    orderNo: 'DD20240108004',
    customer: { name: '赵六', phone: '136****3456' },
    items: [
      { name: '红富士苹果', quantity: 5, price: 12.8 },
      { name: '红心火龙果', quantity: 2, price: 15.9 },
    ],
    totalAmount: 101.8,
    status: 'completed',
    statusText: '已完成',
    createTime: '2024-01-08 09:20:18',
    paymentMethod: '微信支付',
  },
  {
    id: 5,
    orderNo: 'DD20240108005',
    customer: { name: '钱七', phone: '135****7890' },
    items: [{ name: '有机青菜', quantity: 2, price: 5.5 }],
    totalAmount: 11.0,
    status: 'pending_ship',
    statusText: '待发货',
    createTime: '2024-01-08 08:55:42',
    paymentMethod: '微信支付',
  },
]);

const statusList = [
  { value: 'all', label: '全部订单' },
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
  router.push(`/merchant/orders/detail?id=${id}`);
}

function onShipOrder(order: (typeof orderList.value)[0]) {
  router.push(`/merchant/orders/ship?id=${order.id}`);
}

function onPrintInvoice(order: (typeof orderList.value)[0]) {
  showToast({
    type: 'success',
    message: '打印发货单成功',
  });
}

function onCloseOrder(order: (typeof orderList.value)[0]) {
  showToast('订单已关闭');
}

function onViewLogistics(order: (typeof orderList.value)[0]) {
  router.push(`/merchant/orders/logistics?id=${order.id}`);
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
</script>

<template>
  <div class="order-page">
    <!-- 标签页 -->
    <div class="tabs-section">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane v-for="item in statusList" :key="item.value" :label="item.label" :name="item.value" />
      </el-tabs>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary">搜索</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 订单列表 -->
    <div class="order-list">
      <div v-for="order in orderList" :key="order.id" class="order-card">
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">{{ order.createTime }}</span>
          </div>
          <div class="order-status">
            <el-tag :type="getStatusType(order.status)" size="small">{{ order.statusText }}</el-tag>
          </div>
        </div>

        <div class="order-content">
          <div class="customer-info">
            <van-icon name="user-o" />
            <span>{{ order.customer.name }}</span>
            <span class="phone">{{ order.customer.phone }}</span>
          </div>

          <div class="order-items">
            <div v-for="(item, index) in order.items" :key="index" class="item">
              <span class="item-name">{{ item.name }}</span>
              <span class="item-qty">x{{ item.quantity }}</span>
              <span class="item-price">¥{{ item.price.toFixed(2) }}</span>
            </div>
          </div>

          <div class="order-total">
            共 {{ order.items.reduce((sum, item) => sum + item.quantity, 0) }} 件商品， 合计
            <span class="amount">¥{{ order.totalAmount.toFixed(2) }}</span>
          </div>
        </div>

        <div class="order-footer">
          <div class="payment-info">
            <van-icon name="card" />
            <span>{{ order.paymentMethod }}</span>
          </div>
          <div class="order-actions">
            <el-button size="small" @click="onViewDetail(order.id)">查看详情</el-button>
            <template v-if="order.status === 'pending_ship'">
              <el-button type="primary" size="small" @click="onShipOrder(order)">发货</el-button>
              <el-button size="small" @click="onPrintInvoice(order)">打印发货单</el-button>
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
  name: 'MerchantOrders',
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
  transition: background 0.3s;

  &:hover {
    background: #fafafa;
  }

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
  align-items: center;
  gap: 16px;

  .order-no {
    font-size: 14px;
    color: #333;
    font-weight: 500;
  }

  .order-time {
    font-size: 12px;
    color: #999;
  }
}

.order-content {
  margin-bottom: 12px;
}

.customer-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;

  .phone {
    color: #999;
    font-size: 12px;
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
  }
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.payment-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
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
