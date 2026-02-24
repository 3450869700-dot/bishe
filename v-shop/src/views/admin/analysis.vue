<script setup lang="ts">
import { ref, onMounted } from 'vue';
import * as echarts from 'echarts';

const timeRange = ref('month');

const platformStats = ref({
  totalSales: 2865000,
  totalOrders: 12586,
  totalUsers: 56890,
  avgOrderValue: 227.6,
  growthRate: 18.5,
});

const salesTrendRef = ref<HTMLElement>();
const categoryChartRef = ref<HTMLElement>();
const merchantRankRef = ref<HTMLElement>();
let salesChart: echarts.ECharts | null = null;
let categoryChart: echarts.ECharts | null = null;
let merchantChart: echarts.ECharts | null = null;

const merchantRank = ref([
  { name: '龙井茶庄', sales: 256890, orders: 1365, growth: 25.3 },
  { name: '阳光果园', sales: 128560, orders: 2856, growth: 15.8 },
  { name: '有机蔬菜基地', sales: 89320, orders: 4580, growth: 12.6 },
  { name: '高山蜂蜜', sales: 45670, orders: 672, growth: 8.9 },
  { name: '生态养殖', sales: 32150, orders: 428, growth: 5.2 },
]);

const categoryData = ref([
  { name: '水果', value: 35 },
  { name: '蔬菜', value: 28 },
  { name: '茶叶', value: 22 },
  { name: '其他', value: 15 },
]);

onMounted(() => {
  initCharts();
  window.addEventListener('resize', handleResize);
});

function initCharts() {
  if (salesTrendRef.value) {
    salesChart = echarts.init(salesTrendRef.value);
    salesChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['销售额', '订单量'], bottom: 0 },
      grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
      xAxis: {
        type: 'category',
        data: ['1月', '2月', '3月', '4月', '5月', '6月'],
        axisLine: { lineStyle: { color: '#eee' } },
      },
      yAxis: [
        { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
        { type: 'value', axisLine: { show: false }, splitLine: { show: false } },
      ],
      series: [
        {
          name: '销售额',
          type: 'bar',
          data: [380000, 420000, 480000, 520000, 560000, 865000],
          itemStyle: { color: '#0080ff', borderRadius: [4, 4, 0, 0] },
        },
        {
          name: '订单量',
          type: 'line',
          yAxisIndex: 1,
          data: [1680, 1890, 2150, 2380, 2560, 2926],
          itemStyle: { color: '#52c41a' },
          smooth: true,
        },
      ],
    });
  }

  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value);
    categoryChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c}%' },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          data: categoryData.value.map((item, index) => ({
            ...item,
            itemStyle: { color: ['#0080ff', '#52c41a', '#ff7d00', '#884cff'][index] },
          })),
        },
      ],
    });
  }

  if (merchantRankRef.value) {
    merchantChart = echarts.init(merchantRankRef.value);
    merchantChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', top: '10%', bottom: '10%', containLabel: true },
      xAxis: {
        type: 'value',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: '#f0f0f0' } },
      },
      yAxis: {
        type: 'category',
        data: merchantRank.value.map((item) => item.name),
        axisLine: { show: false },
      },
      series: [
        {
          type: 'bar',
          data: merchantRank.value.map((item) => item.sales),
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#0080ff' },
              { offset: 1, color: '#67C23A' },
            ]),
            borderRadius: [0, 4, 4, 0],
          },
        },
      ],
    });
  }
}

function handleResize() {
  salesChart?.resize();
  categoryChart?.resize();
  merchantChart?.resize();
}

function onExportReport() {
  console.log('导出报表');
}
</script>

<template>
  <div class="analysis-page">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon blue">
          <van-icon name="gold-coin-o" size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-value">¥{{ (platformStats.totalSales / 10000).toFixed(2) }}万</div>
          <div class="stat-label">总销售额</div>
        </div>
        <div class="stat-trend up">
          <van-icon name="arrow-up" />
          <span>{{ platformStats.growthRate }}%</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <van-icon name="orders-o" size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ platformStats.totalOrders }}</div>
          <div class="stat-label">总订单量</div>
        </div>
        <div class="stat-trend up">+12.3%</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple">
          <van-icon name="user-o" size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ (platformStats.totalUsers / 10000).toFixed(1) }}万</div>
          <div class="stat-label">用户总数</div>
        </div>
        <div class="stat-trend up">+8.5%</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">
          <van-icon name="coupon-o" size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-value">¥{{ platformStats.avgOrderValue }}</div>
          <div class="stat-label">客单价</div>
        </div>
        <div class="stat-trend up">+5.2%</div>
      </div>
    </div>

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
        <div ref="salesTrendRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="card-header">
          <h3>商品分类占比</h3>
        </div>
        <div ref="categoryChartRef" class="chart-container small"></div>
      </div>
    </div>

    <div class="bottom-row">
      <div class="chart-card wide">
        <div class="card-header">
          <h3>商户销售额排行</h3>
          <el-button type="primary" size="small" @click="onExportReport">
            <van-icon name="down-load" /> 导出报表
          </el-button>
        </div>
        <div ref="merchantRankRef" class="chart-container"></div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'AdminAnalysis',
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

    &.blue {
      background: linear-gradient(135deg, #0080ff 0%, #409eff 100%);
    }
    &.green {
      background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
    }
    &.purple {
      background: linear-gradient(135deg, #884cff 0%, #9254de 100%);
    }
    &.orange {
      background: linear-gradient(135deg, #ff7d00 0%, #ff9f43 100%);
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
    font-size: 12px;
    padding: 4px 8px;
    border-radius: 4px;

    &.up {
      color: #52c41a;
      background: rgba(82, 196, 26, 0.1);
    }
  }
}

.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.bottom-row {
  .chart-card.wide {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

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
      height: 300px;
    }
  }
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

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
