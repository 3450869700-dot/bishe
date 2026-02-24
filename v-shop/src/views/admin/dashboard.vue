<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';

const router = useRouter();

const statsData = ref([
  { label: '总订单数', value: '12,586', change: '+15.3%', trend: 'up', icon: 'orders-o', color: '#0080ff' },
  { label: '总销售额', value: '¥286.5万', change: '+22.8%', trend: 'up', icon: 'gold-coin-o', color: '#52c41a' },
  { label: '今日新增用户', value: '386', change: '+8.5%', trend: 'up', icon: 'user-add-o', color: '#884cff' },
  { label: '商户数量', value: '128', change: '+5.2%', trend: 'up', icon: 'shop-o', color: '#ff7d00' },
]);

const platformData = ref({
  totalMerchants: 128,
  activeMerchants: 115,
  totalUsers: 56890,
  totalProducts: 2560,
  pendingAudit: 8,
});

const recentOrders = ref([
  { id: 'DD20240108001', merchant: '阳光果园', user: '张三', amount: '¥128.00', status: 'completed', time: '10:30' },
  { id: 'DD20240108002', merchant: '有机蔬菜基地', user: '李四', amount: '¥89.00', status: 'shipped', time: '10:15' },
  {
    id: 'DD20240108003',
    merchant: '龙井茶庄',
    user: '王五',
    amount: '¥1,280.00',
    status: 'pending_pay',
    time: '09:45',
  },
  { id: 'DD20240108004', merchant: '阳光果园', user: '赵六', amount: '¥256.00', status: 'completed', time: '09:20' },
  { id: 'DD20240108005', merchant: '高山蜂蜜', user: '钱七', amount: '¥168.00', status: 'refund', time: '08:55' },
]);

const chartRef = ref<HTMLElement>();
let platformChart: echarts.ECharts | null = null;

onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

function initChart() {
  if (!chartRef.value) return;

  platformChart = echarts.init(chartRef.value);
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#eee',
      textStyle: { color: '#333' },
    },
    legend: {
      data: ['销售额', '订单量', '用户增长'],
      bottom: 0,
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLine: { lineStyle: { color: '#eee' } },
      axisLabel: { color: '#666' },
    },
    yAxis: [
      {
        type: 'value',
        name: '金额/订单',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: '#f0f0f0' } },
      },
      {
        type: 'value',
        name: '用户',
        axisLine: { show: false },
        splitLine: { show: false },
      },
    ],
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        data: [32000, 41000, 38000, 52000, 48000, 61000, 56000],
        itemStyle: { color: '#0080ff' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(0, 128, 255, 0.3)' },
              { offset: 1, color: 'rgba(0, 128, 255, 0)' },
            ],
          },
        },
      },
      {
        name: '订单量',
        type: 'bar',
        data: [320, 410, 380, 520, 480, 610, 560],
        itemStyle: { color: '#52c41a', borderRadius: [4, 4, 0, 0] },
      },
      {
        name: '用户增长',
        type: 'line',
        yAxisIndex: 1,
        data: [120, 150, 180, 200, 250, 280, 320],
        itemStyle: { color: '#884cff' },
        smooth: true,
      },
    ],
  };

  platformChart.setOption(option);
}

function handleResize() {
  platformChart?.resize();
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

function goMerchants() {
  router.push('/admin/merchants');
}

function goUsers() {
  router.push('/admin/users');
}

function goProducts() {
  router.push('/admin/products');
}

function goOrders() {
  router.push('/admin/orders');
}
</script>

<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div v-for="(item, index) in statsData" :key="index" class="stat-card" @click="goOrders()">
        <div class="stat-icon" :style="{ background: item.color }">
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

    <!-- 平台数据概览 -->
    <div class="overview-section">
      <div class="overview-card">
        <div class="overview-header">
          <h4>商户数据</h4>
          <el-button type="primary" size="small" text @click="goMerchants">查看全部</el-button>
        </div>
        <div class="overview-content">
          <div class="overview-item">
            <span class="value">{{ platformData.totalMerchants }}</span>
            <span class="label">总商户数</span>
          </div>
          <div class="overview-item">
            <span class="value">{{ platformData.activeMerchants }}</span>
            <span class="label">活跃商户</span>
          </div>
          <div class="overview-item">
            <span class="value warning">{{ platformData.pendingAudit }}</span>
            <span class="label">待审核</span>
          </div>
        </div>
      </div>

      <div class="overview-card">
        <div class="overview-header">
          <h4>用户数据</h4>
          <el-button type="primary" size="small" text @click="goUsers">查看全部</el-button>
        </div>
        <div class="overview-content">
          <div class="overview-item">
            <span class="value">{{ (platformData.totalUsers / 10000).toFixed(1) }}万</span>
            <span class="label">总用户数</span>
          </div>
          <div class="overview-item">
            <span class="value">{{ platformData.totalProducts }}</span>
            <span class="label">商品总数</span>
          </div>
          <div class="overview-item">
            <span class="value success">99.2%</span>
            <span class="label">订单完成率</span>
          </div>
        </div>
      </div>

      <div class="overview-card">
        <div class="overview-header">
          <h4>快捷操作</h4>
        </div>
        <div class="quick-actions">
          <div class="action-item" @click="goMerchants">
            <van-icon name="shop-o" size="24" color="#0080ff" />
            <span>商户审核</span>
          </div>
          <div class="action-item" @click="goProducts">
            <van-icon name="bag-o" size="24" color="#52c41a" />
            <span>商品审核</span>
          </div>
          <div class="action-item" @click="router.push('/admin/system')">
            <van-icon name="setting-o" size="24" color="#884cff" />
            <span>系统设置</span>
          </div>
          <div class="action-item" @click="router.push('/admin/analysis')">
            <van-icon name="chart-trending-o" size="24" color="#ff7d00" />
            <span>数据分析</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 平台趋势图表 -->
    <div class="chart-card">
      <div class="card-header">
        <h3>平台运营趋势</h3>
        <el-radio-group v-model="timeRange" size="small">
          <el-radio-button label="week">本周</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
          <el-radio-button label="year">本年</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="chartRef" class="chart-container"></div>
    </div>

    <!-- 近期订单 -->
    <div class="orders-card">
      <div class="card-header">
        <h3>全平台订单</h3>
        <el-button type="primary" size="small" text @click="goOrders">查看全部</el-button>
      </div>
      <el-table :data="recentOrders" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
        <el-table-column prop="id" label="订单号" width="150" />
        <el-table-column prop="merchant" label="商户" width="120" />
        <el-table-column prop="user" label="用户" width="100" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">
            <span class="amount">{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="时间" width="80" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="router.push(`/admin/orders/detail?id=${row.id}`)"
              >查看</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'AdminDashboard',
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
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
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

.overview-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.overview-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h4 {
    font-size: 15px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }
}

.overview-content {
  display: flex;
  justify-content: space-around;
}

.overview-item {
  text-align: center;

  .value {
    display: block;
    font-size: 24px;
    font-weight: bold;
    color: #333;

    &.warning {
      color: #ff7d00;
    }

    &.success {
      color: #52c41a;
    }
  }

  .label {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 8px;
  background: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: #e8ecf1;
  }

  span {
    font-size: 13px;
    color: #666;
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

.amount {
  color: #0080ff;
  font-weight: 600;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .overview-section {
    grid-template-columns: 1fr;
  }
}
</style>
