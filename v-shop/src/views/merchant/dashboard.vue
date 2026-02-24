<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const statsData = ref([
  { label: '今日订单数', value: 128, change: '+12.5%', trend: 'up', icon: 'orders-o' },
  { label: '待发货订单', value: 23, change: '-5.2%', trend: 'down', icon: 'tosend' },
  { label: '库存预警', value: 8, change: '+3', trend: 'up', icon: 'warning-o' },
  { label: '今日销售额', value: '¥8,560', change: '+18.3%', trend: 'up', icon: 'gold-coin-o' },
]);

const recentOrders = ref([
  { id: 'DD20240108001', customer: '张三', amount: '¥128.00', status: '待发货', time: '10:30' },
  { id: 'DD20240108002', customer: '李四', amount: '¥256.00', status: '待支付', time: '10:15' },
  { id: 'DD20240108003', customer: '王五', amount: '¥89.00', status: '已发货', time: '09:45' },
  { id: 'DD20240108004', customer: '赵六', amount: '¥512.00', status: '已完成', time: '09:20' },
  { id: 'DD20240108005', customer: '钱七', amount: '¥199.00', status: '待发货', time: '08:55' },
]);

const salesTrend = ref([
  { day: '周一', sales: 3200, orders: 45 },
  { day: '周二', sales: 4100, orders: 58 },
  { day: '周三', sales: 3800, orders: 52 },
  { day: '周四', sales: 5200, orders: 73 },
  { day: '周五', sales: 4800, orders: 68 },
  { day: '周六', sales: 6100, orders: 85 },
  { day: '周日', sales: 5600, orders: 76 },
]);

const timeRange = ref('week');

onMounted(() => {
  // 数据已加载
});

function goOrders(status?: string) {
  router.push(status ? `/merchant/orders?status=${status}` : '/merchant/orders');
}

function goProducts() {
  router.push('/merchant/products');
}
</script>

<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div
        v-for="(item, index) in statsData"
        :key="index"
        class="stat-card"
        @click="index < 3 ? goOrders() : goProducts()"
      >
        <div class="stat-icon">
          <van-icon :name="item.icon" size="28" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
        <div class="stat-trend" :class="item.trend">
          <van-icon :name="item.trend === 'up' ? 'arrow-up' : 'arrow-down'" />
          <span>{{ item.change }}</span>
        </div>
      </div>
    </div>

    <!-- 销售趋势 -->
    <div class="chart-card">
      <div class="card-header">
        <h3>销售趋势</h3>
        <div class="card-actions">
          <el-radio-group v-model="timeRange" size="small">
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
            <el-radio-button label="year">本年</el-radio-button>
          </el-radio-group>
        </div>
      </div>
      <div class="sales-trend">
        <div class="trend-header">
          <div v-for="(item, index) in salesTrend" :key="index" class="trend-item">
            <div class="trend-day">{{ item.day }}</div>
            <div class="trend-bar-container">
              <div class="trend-bar" :style="{ height: item.sales / 70 + '%' }">
                <span class="trend-value">¥{{ item.sales }}</span>
              </div>
            </div>
            <div class="trend-orders">{{ item.orders }}单</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 近期订单 -->
    <div class="orders-card">
      <div class="card-header">
        <h3>近期订单</h3>
        <el-button type="primary" size="small" text @click="goOrders()">查看全部</el-button>
      </div>
      <el-table :data="recentOrders" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
        <el-table-column prop="id" label="订单号" width="140" />
        <el-table-column prop="customer" label="客户" width="100" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">
            <span class="amount">{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === '待支付' ? 'warning' : row.status === '待发货' ? 'primary' : 'success'"
              size="small"
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="时间" width="80" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="router.push(`/merchant/orders?id=${row.id}`)"
              >查看</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="action-grid">
        <div class="action-item" @click="router.push('/merchant/products?action=add')">
          <div class="action-icon add">
            <van-icon name="plus" size="24" />
          </div>
          <span>发布商品</span>
        </div>
        <div class="action-item" @click="goOrders('1')">
          <div class="action-icon ship">
            <van-icon name="tosend" size="24" />
          </div>
          <span>待发货订单</span>
        </div>
        <div class="action-item" @click="router.push('/merchant/traceability?action=add')">
          <div class="action-icon trace">
            <van-icon name="search" size="24" />
          </div>
          <span>录入溯源</span>
        </div>
        <div class="action-item" @click="router.push('/merchant/analysis')">
          <div class="action-icon chart">
            <van-icon name="chart-trending-o" size="24" />
          </div>
          <span>数据分析</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'MerchantDashboard',
};
</script>

<style lang="less" scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff7d00 0%, #ff9f43 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-content {
  flex: 1;

  .stat-value {
    font-size: 28px;
    font-weight: bold;
    color: #333;
    line-height: 1.2;
  }

  .stat-label {
    font-size: 14px;
    color: #999;
    margin-top: 4px;
  }
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;

  &.up {
    color: #52c41a;
    background: rgba(82, 196, 26, 0.1);
  }

  &.down {
    color: #ff4d4f;
    background: rgba(255, 77, 79, 0.1);
  }
}

.chart-card,
.orders-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }
}

.chart-container {
  height: 300px;
}

.sales-trend {
  padding: 20px 0;
}

.trend-header {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 200px;
}

.trend-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 12%;
}

.trend-day {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.trend-bar-container {
  width: 100%;
  height: 150px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.trend-bar {
  width: 80%;
  background: linear-gradient(180deg, #ff7d00 0%, #ff9f43 100%);
  border-radius: 4px 4px 0 0;
  min-height: 20px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  position: relative;
  transition: all 0.3s;

  &:hover {
    opacity: 0.8;
  }
}

.trend-value {
  position: absolute;
  top: -24px;
  font-size: 11px;
  color: #ff7d00;
  font-weight: 600;
  white-space: nowrap;
}

.trend-orders {
  font-size: 11px;
  color: #999;
  margin-top: 8px;
}

.amount {
  color: #ff7d00;
  font-weight: 600;
}

.quick-actions {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0 0 20px;
  }
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-radius: 12px;
  background: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: #ff7d00;

    .action-icon {
      background: rgba(255, 255, 255, 0.2);
      color: #fff;
    }

    span {
      color: #fff;
    }
  }

  span {
    font-size: 14px;
    color: #333;
    transition: color 0.3s;
  }
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff7d00;
  background: rgba(255, 125, 0, 0.1);
  transition: all 0.3s;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
