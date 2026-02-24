<script setup lang="ts">
import { ref, onMounted } from 'vue';
import * as echarts from 'echarts';

const salesChartRef = ref<HTMLElement>();
const categoryChartRef = ref<HTMLElement>();
let salesChart: echarts.ECharts | null = null;
let categoryChart: echarts.ECharts | null = null;

const timeRange = ref('month');

const salesData = ref({
  totalSales: 125680,
  totalOrders: 1289,
  avgOrderValue: 97.5,
  growthRate: 18.5,
});

const topProducts = ref([
  { name: '红富士苹果', sales: 45600, orders: 3560, stock: 500 },
  { name: '有机青菜', sales: 32800, orders: 5960, stock: 200 },
  { name: '龙井新茶', sales: 28900, orders: 154, stock: 50 },
  { name: '红心火龙果', sales: 21500, orders: 1352, stock: 15 },
  { name: '红富士苹果礼盒', sales: 18900, orders: 630, stock: 120 },
]);

const salesTrend = ref([
  { date: '2024-01', sales: 32000, orders: 328 },
  { date: '2024-02', sales: 28000, orders: 289 },
  { date: '2024-03', sales: 35000, orders: 356 },
  { date: '2024-04', sales: 42000, orders: 428 },
  { date: '2024-05', sales: 38500, orders: 392 },
  { date: '2024-06', sales: 45000, orders: 456 },
  { date: '2024-07', sales: 52000, orders: 528 },
]);

const categoryData = ref([
  { name: '水果', value: 45 },
  { name: '蔬菜', value: 25 },
  { name: '茶叶', value: 20 },
  { name: '其他', value: 10 },
]);

const userData = ref({
  totalUsers: 5689,
  newUsers: 128,
  repeatRate: 35.8,
  avgOrderFrequency: 2.3,
});

onMounted(() => {
  initCharts();
  window.addEventListener('resize', handleResize);
});

function initCharts() {
  if (salesChartRef.value) {
    salesChart = echarts.init(salesChartRef.value);
    const option: echarts.EChartsOption = {
      tooltip: { trigger: 'axis' },
      legend: { data: ['销售额', '订单量'], bottom: 0 },
      grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
      xAxis: {
        type: 'category',
        data: salesTrend.value.map((item) => item.date),
        axisLine: { lineStyle: { color: '#eee' } },
        axisLabel: { color: '#666' },
      },
      yAxis: [
        { type: 'value', name: '销售额', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
        { type: 'value', name: '订单量', axisLine: { show: false }, splitLine: { show: false } },
      ],
      series: [
        {
          name: '销售额',
          type: 'bar',
          data: salesTrend.value.map((item) => item.sales),
          itemStyle: { color: '#ff7d00', borderRadius: [4, 4, 0, 0] },
        },
        {
          name: '订单量',
          type: 'line',
          yAxisIndex: 1,
          data: salesTrend.value.map((item) => item.orders),
          itemStyle: { color: '#0080ff' },
          smooth: true,
        },
      ],
    };
    salesChart.setOption(option);
  }

  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value);
    const option: echarts.EChartsOption = {
      tooltip: { trigger: 'item', formatter: '{b}: {c}%' },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
          label: { show: true, formatter: '{b}\n{d}%' },
          data: categoryData.value.map((item, index) => ({
            ...item,
            itemStyle: { color: ['#ff7d00', '#52c41a', '#0080ff', '#884cff'][index] },
          })),
        },
      ],
    };
    categoryChart.setOption(option);
  }
}

function handleResize() {
  salesChart?.resize();
  categoryChart?.resize();
}

function onExportReport() {
  console.log('导出报表');
}
</script>

<template>
  <div class="analysis-page">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon orange">
          <van-icon name="gold-coin-o" size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-value">¥{{ (salesData.totalSales / 10000).toFixed(2) }}万</div>
          <div class="stat-label">总销售额</div>
        </div>
        <div class="stat-trend up">
          <van-icon name="arrow-up" />
          <span>{{ salesData.growthRate }}%</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon blue">
          <van-icon name="orders-o" size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ salesData.totalOrders }}</div>
          <div class="stat-label">总订单量</div>
        </div>
        <div class="stat-trend up">
          <van-icon name="arrow-up" />
          <span>12.3%</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <van-icon name="coupon-o" size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-value">¥{{ salesData.avgOrderValue }}</div>
          <div class="stat-label">客单价</div>
        </div>
        <div class="stat-trend up">
          <van-icon name="arrow-up" />
          <span>5.8%</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple">
          <van-icon name="user-o" size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ userData.totalUsers }}</div>
          <div class="stat-label">用户数</div>
        </div>
        <div class="stat-trend up">
          <van-icon name="arrow-up" />
          <span>8.2%</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="card-header">
          <h3>销售趋势</h3>
          <el-radio-group v-model="timeRange" size="small">
            <el-radio-button label="week">周</el-radio-button>
            <el-radio-button label="month">月</el-radio-button>
            <el-radio-button label="year">年</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="salesChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="card-header">
          <h3>商品分类占比</h3>
        </div>
        <div ref="categoryChartRef" class="chart-container small"></div>
      </div>
    </div>

    <!-- 热销商品排行 -->
    <div class="table-card">
      <div class="card-header">
        <h3>热销商品排行</h3>
        <el-button type="primary" size="small" @click="onExportReport">
          <van-icon name="down-load" /> 导出报表
        </el-button>
      </div>
      <el-table :data="topProducts" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="name" label="商品名称" min-width="150" />
        <el-table-column prop="sales" label="销售额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.sales.toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orders" label="订单量" width="100" />
        <el-table-column prop="stock" label="库存" width="100">
          <template #default="{ row }">
            <span :class="{ 'low-stock': row.stock < 20 }">{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" text>查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 用户分析 -->
    <div class="user-stats">
      <div class="stat-card mini">
        <div class="stat-value">{{ userData.newUsers }}</div>
        <div class="stat-label">新增用户</div>
      </div>
      <div class="stat-card mini">
        <div class="stat-value">{{ userData.repeatRate }}%</div>
        <div class="stat-label">复购率</div>
      </div>
      <div class="stat-card mini">
        <div class="stat-value">{{ userData.avgOrderFrequency }}次</div>
        <div class="stat-label">平均购买频次</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'MerchantAnalysis',
};
</script>

<style lang="less" scoped>
.analysis-page {
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

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;

    &.orange {
      background: linear-gradient(135deg, #ff7d00 0%, #ff9f43 100%);
    }
    &.blue {
      background: linear-gradient(135deg, #0080ff 0%, #409eff 100%);
    }
    &.green {
      background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
    }
    &.purple {
      background: linear-gradient(135deg, #884cff 0%, #9254de 100%);
    }
  }

  .stat-content {
    flex: 1;

    .stat-value {
      font-size: 24px;
      font-weight: bold;
      color: #333;
    }

    .stat-label {
      font-size: 13px;
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

  &.mini {
    flex-direction: column;
    text-align: center;
    padding: 16px;

    .stat-value {
      font-size: 20px;
    }
  }
}

.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }
}

.chart-container {
  height: 280px;

  &.small {
    height: 240px;
  }
}

.table-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.amount {
  color: #ff7d00;
  font-weight: 600;
}

.low-stock {
  color: #ff4d4f;
  font-weight: 600;
}

.user-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-row {
    grid-template-columns: 1fr;
  }
}
</style>
