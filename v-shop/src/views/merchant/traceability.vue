<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';

const router = useRouter();

const activeTab = ref('plant');
const traceList = ref([
  {
    id: 1,
    productName: '红富士苹果',
    batchNo: 'P20240108001',
    category: 'plant',
    status: 'completed',
    createTime: '2024-01-08',
    plantDate: '2023-03-15',
    harvestDate: '2023-10-20',
    质检合格: true,
    steps: [
      { stage: '种植', date: '2023-03-15', desc: '春季种植，选用优质苹果树苗', operator: '张农户' },
      { stage: '施肥', date: '2023-04-10', desc: '有机肥施肥，使用农家肥', operator: '李农户' },
      { stage: '农药', date: '2023-05-20', desc: '生物农药防治病虫害', operator: '张农户' },
      { stage: '采摘', date: '2023-10-20', desc: '人工采摘，选择成熟度达标果实', operator: '王农户' },
      { stage: '质检', date: '2023-10-22', desc: '农残检测合格，品质评级A级', operator: '质检员' },
    ],
  },
  {
    id: 2,
    productName: '有机青菜',
    batchNo: 'P20240108002',
    category: 'plant',
    status: 'in_progress',
    createTime: '2024-01-05',
    plantDate: '2024-01-01',
    harvestDate: '',
    质检合格: null,
    steps: [{ stage: '种植', date: '2024-01-01', desc: '大棚有机种植', operator: '赵农户' }],
  },
]);

const traceForm = ref({
  productName: '',
  batchNo: '',
  category: 'plant',
  plantDate: '',
  fertilizer: '',
  pesticide: '',
  harvestDate: '',
  质检信息: '',
});

const showAddDialog = ref(false);

onMounted(() => {
  getTraceList();
});

function getTraceList() {
  showToast('加载完成');
}

function onAddTrace() {
  showAddDialog.value = true;
}

function onSubmitTrace() {
  if (!traceForm.value.productName || !traceForm.value.batchNo) {
    showToast('请填写完整信息');
    return;
  }

  const newTrace = {
    id: Date.now(),
    productName: traceForm.value.productName,
    batchNo: traceForm.value.batchNo,
    category: traceForm.value.category,
    status: 'in_progress',
    createTime: new Date().toISOString().split('T')[0],
    plantDate: traceForm.value.plantDate,
    harvestDate: '',
    质检合格: null,
    steps: [
      {
        stage: '种植',
        date: traceForm.value.plantDate,
        desc: traceForm.value.fertilizer || '已完成种植',
        operator: '当前用户',
      },
    ],
  };

  traceList.value.unshift(newTrace);
  showAddDialog.value = false;
  showToast({
    type: 'success',
    message: '溯源信息录入成功',
  });

  traceForm.value = {
    productName: '',
    batchNo: '',
    category: 'plant',
    plantDate: '',
    fertilizer: '',
    pesticide: '',
    harvestDate: '',
    质检信息: '',
  };
}

function onAddStep(trace: (typeof traceList.value)[0]) {
  router.push(`/merchant/traceability/step?batchNo=${trace.batchNo}`);
}

function onHarvest(trace: (typeof traceList.value)[0]) {
  router.push(`/merchant/traceability/harvest?batchNo=${trace.batchNo}`);
}

function onQC(trace: (typeof traceList.value)[0]) {
  router.push(`/merchant/traceability/qc?batchNo=${trace.batchNo}`);
}

function onViewTrace(trace: (typeof traceList.value)[0]) {
  router.push(`/merchant/traceability/detail?batchNo=${trace.batchNo}`);
}
</script>

<template>
  <div class="traceability-page">
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="onAddTrace"> <van-icon name="plus" /> 录入溯源信息 </el-button>
      <el-button @click="router.push('/merchant/traceability/query')"> <van-icon name="search" /> 溯源查询 </el-button>
    </div>

    <!-- 溯源列表 -->
    <div class="trace-list">
      <div v-for="trace in traceList" :key="trace.id" class="trace-card">
        <div class="trace-header">
          <div class="trace-product">
            <span class="product-name">{{ trace.productName }}</span>
            <el-tag size="small" :type="trace.status === 'completed' ? 'success' : 'warning'">
              {{ trace.status === 'completed' ? '已完成溯源' : '溯源进行中' }}
            </el-tag>
          </div>
          <div class="trace-batch">批次号：{{ trace.batchNo }}</div>
        </div>

        <div class="trace-timeline">
          <div class="timeline-header">
            <van-icon name="clock" />
            <span>溯源时间线</span>
          </div>
          <div class="timeline-content">
            <div v-for="(step, index) in trace.steps" :key="index" class="timeline-item">
              <div class="timeline-dot" :class="{ active: index === trace.steps.length - 1 }"></div>
              <div class="timeline-info">
                <div class="timeline-stage">{{ step.stage }}</div>
                <div class="timeline-date">{{ step.date }}</div>
              </div>
              <div class="timeline-desc">{{ step.desc }}</div>
            </div>
          </div>
        </div>

        <div class="trace-footer">
          <div class="trace-info">
            <span v-if="trace.plantDate">种植日期：{{ trace.plantDate }}</span>
            <span v-if="trace.harvestDate">采摘日期：{{ trace.harvestDate }}</span>
            <span v-if="trace.质检合格 !== null">
              质检：<el-tag :type="trace.质检合格 ? 'success' : 'danger'" size="small">
                {{ trace.质检合格 ? '合格' : '不合格' }}
              </el-tag>
            </span>
          </div>
          <div class="trace-actions">
            <el-button size="small" @click="onViewTrace(trace)">查看详情</el-button>
            <el-button v-if="trace.status === 'in_progress'" size="small" type="primary" @click="onAddStep(trace)">
              继续录入
            </el-button>
            <el-button
              v-if="trace.status === 'in_progress' && trace.steps.some((s) => s.stage === '采摘')"
              size="small"
              type="success"
              @click="onQC(trace)"
            >
              质检
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加溯源对话框 -->
    <el-dialog v-model="showAddDialog" title="录入溯源信息" width="500px">
      <el-form :model="traceForm" label-width="100px">
        <el-form-item label="商品名称" required>
          <el-select v-model="traceForm.productName" placeholder="请选择商品" filterable allow-create>
            <el-option label="红富士苹果" value="红富士苹果" />
            <el-option label="有机青菜" value="有机青菜" />
            <el-option label="龙井新茶" value="龙井新茶" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次号" required>
          <el-input v-model="traceForm.batchNo" placeholder="自动生成或手动输入" />
        </el-form-item>
        <el-form-item label="种植日期">
          <el-date-picker v-model="traceForm.plantDate" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="种植情况">
          <el-input v-model="traceForm.fertilizer" type="textarea" placeholder="描述施肥、灌溉等情况" rows="2" />
        </el-form-item>
        <el-form-item label="农药使用">
          <el-input v-model="traceForm.pesticide" type="textarea" placeholder="描述农药使用情况（如有）" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="onSubmitTrace">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
export default {
  name: 'MerchantTraceability',
};
</script>

<style lang="less" scoped>
.traceability-page {
  max-width: 1760px;
  margin: 0 auto;
}

.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.trace-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.trace-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.trace-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.trace-product {
  display: flex;
  align-items: center;
  gap: 12px;

  .product-name {
    font-size: 18px;
    font-weight: 600;
    color: #333;
  }
}

.trace-batch {
  font-size: 13px;
  color: #999;
}

.trace-timeline {
  margin-bottom: 16px;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.timeline-content {
  padding-left: 16px;
  border-left: 2px solid #e8e8e8;
}

.timeline-item {
  position: relative;
  padding: 8px 0 8px 20px;

  .timeline-dot {
    position: absolute;
    left: -7px;
    top: 12px;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: #e0e0e0;
    border: 2px solid #fff;

    &.active {
      background: #ff7d00;
    }
  }
}

.timeline-stage {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.timeline-date {
  font-size: 12px;
  color: #999;
  margin: 4px 0;
}

.timeline-desc {
  font-size: 13px;
  color: #666;
}

.trace-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.trace-info {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #666;
}

.trace-actions {
  display: flex;
  gap: 8px;
}
</style>
