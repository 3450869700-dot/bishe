<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';

const router = useRouter();

const activeTab = ref('query');
const searchForm = ref({
  productName: '',
  batchNo: '',
  merchant: '',
});

const traceList = ref([
  {
    id: 1,
    productName: '红富士苹果',
    batchNo: 'P20240108001',
    merchant: '阳光果园',
    status: 'completed',
    plantDate: '2023-03-15',
    harvestDate: '2023-10-20',
    qcStatus: 'passed',
    steps: ['种植', '施肥', '农药', '采摘', '质检'],
  },
  {
    id: 2,
    productName: '有机青菜',
    batchNo: 'P20240108002',
    merchant: '有机蔬菜基地',
    status: 'in_progress',
    plantDate: '2024-01-01',
    harvestDate: '',
    qcStatus: 'pending',
    steps: ['种植'],
  },
]);

const statsData = ref({
  totalTraces: 128,
  completedTraces: 98,
  pendingTraces: 30,
  queryCount: 2560,
});

function onSearch() {
  showToast('搜索完成');
}

function onViewDetail(batchNo: string) {
  router.push(`/admin/traceability/detail?batchNo=${batchNo}`);
}

function onAuditTrace(id: number) {
  showToast('审核通过');
}
</script>

<template>
  <div class="traceability-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="溯源查询" name="query">
        <div class="search-section">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="商品名称">
              <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable style="width: 180px" />
            </el-form-item>
            <el-form-item label="批次号">
              <el-input v-model="searchForm.batchNo" placeholder="请输入批次号" clearable style="width: 180px" />
            </el-form-item>
            <el-form-item label="商户">
              <el-input v-model="searchForm.merchant" placeholder="请输入商户名称" clearable style="width: 150px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="onSearch">查询</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="stats-row">
          <div class="stat-item">
            <div class="value">{{ statsData.totalTraces }}</div>
            <div class="label">溯源批次总数</div>
          </div>
          <div class="stat-item">
            <div class="value">{{ statsData.completedTraces }}</div>
            <div class="label">已完成溯源</div>
          </div>
          <div class="stat-item">
            <div class="value">{{ statsData.pendingTraces }}</div>
            <div class="label">进行中</div>
          </div>
          <div class="stat-item">
            <div class="value">{{ statsData.queryCount }}</div>
            <div class="label">溯源查询次数</div>
          </div>
        </div>

        <div class="trace-list">
          <div v-for="trace in traceList" :key="trace.id" class="trace-card">
            <div class="trace-header">
              <div class="trace-info">
                <span class="product-name">{{ trace.productName }}</span>
                <span class="batch-no">批次号：{{ trace.batchNo }}</span>
                <span class="merchant">{{ trace.merchant }}</span>
              </div>
              <el-tag :type="trace.status === 'completed' ? 'success' : 'warning'" size="small">
                {{ trace.status === 'completed' ? '溯源完成' : '进行中' }}
              </el-tag>
            </div>
            <div class="trace-content">
              <div class="trace-steps">
                <span v-for="(step, index) in trace.steps" :key="index" class="step-tag">{{ step }}</span>
              </div>
              <div class="trace-dates">
                <span>种植：{{ trace.plantDate }}</span>
                <span v-if="trace.harvestDate">采摘：{{ trace.harvestDate }}</span>
              </div>
            </div>
            <div class="trace-footer">
              <el-button type="primary" size="small" @click="onViewDetail(trace.batchNo)">查看详情</el-button>
              <el-button
                v-if="trace.status === 'in_progress'"
                type="success"
                size="small"
                @click="onAuditTrace(trace.id)"
              >
                审核溯源
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="溯源统计" name="stats">
        <div class="stats-overview">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-card">
                <h4>溯源覆盖率</h4>
                <div class="value">76.5%</div>
                <div class="desc">已完成溯源商品占比</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <h4>溯源查询次数</h4>
                <div class="value">2,560</div>
                <div class="desc">本月查询次数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <h4>质检通过率</h4>
                <div class="value">98.2%</div>
                <div class="desc">质检合格批次占比</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script lang="ts">
export default {
  name: 'AdminTraceability',
};
</script>

<style lang="less" scoped>
.traceability-page {
  max-width: 1200px;
  margin: 0 auto;
}

.search-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;

  .value {
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

.trace-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.trace-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.trace-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.trace-info {
  display: flex;
  align-items: center;
  gap: 16px;

  .product-name {
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }

  .batch-no {
    font-size: 13px;
    color: #999;
  }

  .merchant {
    font-size: 13px;
    color: #0080ff;
  }
}

.trace-content {
  margin-bottom: 12px;
}

.trace-steps {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;

  .step-tag {
    padding: 4px 12px;
    background: #f0f5ff;
    color: #0080ff;
    border-radius: 4px;
    font-size: 12px;
  }
}

.trace-dates {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #999;
}

.trace-footer {
  display: flex;
  gap: 8px;
}

.stats-overview {
  .stat-card {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    text-align: center;

    h4 {
      font-size: 14px;
      color: #999;
      margin: 0 0 12px;
    }

    .value {
      font-size: 36px;
      font-weight: bold;
      color: #0080ff;
    }

    .desc {
      font-size: 13px;
      color: #999;
      margin-top: 8px;
    }
  }
}
</style>
