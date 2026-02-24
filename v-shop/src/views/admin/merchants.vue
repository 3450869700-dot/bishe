<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';

const router = useRouter();

const activeTab = ref('all');
const searchForm = ref({
  name: '',
  contact: '',
  status: '',
});

const merchantList = ref([
  {
    id: 1,
    name: '阳光果园',
    logo: '',
    contact: '张先生',
    phone: '138****1234',
    products: 28,
    totalSales: '¥128,560',
    status: 'active',
    createTime: '2023-06-15',
    joinDays: 180,
  },
  {
    id: 2,
    name: '有机蔬菜基地',
    logo: '',
    contact: '李女士',
    phone: '139****5678',
    products: 45,
    totalSales: '¥89,230',
    status: 'active',
    createTime: '2023-08-20',
    joinDays: 120,
  },
  {
    id: 3,
    name: '龙井茶庄',
    logo: '',
    contact: '王先生',
    phone: '137****9012',
    products: 12,
    totalSales: '¥256,890',
    status: 'active',
    createTime: '2023-03-10',
    joinDays: 280,
  },
  {
    id: 4,
    name: '高山蜂蜜',
    logo: '',
    contact: '赵女士',
    phone: '136****3456',
    products: 8,
    totalSales: '¥45,670',
    status: 'pending',
    createTime: '2024-01-05',
    joinDays: 3,
  },
  {
    id: 5,
    name: '生态养殖',
    logo: '',
    contact: '钱先生',
    phone: '135****7890',
    products: 15,
    totalSales: '¥0',
    status: 'disabled',
    createTime: '2024-01-01',
    joinDays: 7,
  },
]);

const statusList = [
  { value: '', label: '全部状态' },
  { value: 'pending', label: '待审核' },
  { value: 'active', label: '已启用' },
  { value: 'disabled', label: '已禁用' },
];

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(100);
const loading = ref(false);

onMounted(() => {
  getMerchantList();
});

function getMerchantList() {
  loading.value = true;
  setTimeout(() => {
    loading.value = false;
    showToast('加载完成');
  }, 300);
}

function handleTabChange(tab: string) {
  activeTab.value = tab;
  currentPage.value = 1;
  getMerchantList();
}

function onViewDetail(id: number) {
  router.push(`/admin/merchants/detail?id=${id}`);
}

function onAuditMerchant(merchant: (typeof merchantList.value)[0]) {
  showConfirmDialog({
    title: '商户审核',
    message: `确认通过商户「${merchant.name}」的入驻申请吗？`,
  })
    .then(() => {
      merchant.status = 'active';
      showToast({
        type: 'success',
        message: '审核通过',
      });
    })
    .catch(() => {});
}

function onToggleStatus(merchant: (typeof merchantList.value)[0]) {
  const action = merchant.status === 'active' ? '禁用' : '启用';
  showConfirmDialog({
    title: '确认操作',
    message: `确定要${action}商户「${merchant.name}」吗？`,
  })
    .then(() => {
      merchant.status = merchant.status === 'active' ? 'disabled' : 'active';
      showToast({
        type: 'success',
        message: `${action}成功`,
      });
    })
    .catch(() => {});
}

function onViewProducts(id: number) {
  router.push(`/admin/products?merchantId=${id}`);
}

function onViewData(id: number) {
  router.push(`/admin/analysis?merchantId=${id}`);
}

function handlePageChange(page: number) {
  currentPage.value = page;
  getMerchantList();
}

function getStatusType(status: string): string {
  const map: Record<string, string> = {
    pending: 'warning',
    active: 'success',
    disabled: 'info',
  };
  return map[status] || 'info';
}

function getStatusText(status: string): string {
  const map: Record<string, string> = {
    pending: '待审核',
    active: '已启用',
    disabled: '已禁用',
  };
  return map[status] || status;
}
</script>

<template>
  <div class="merchant-page">
    <!-- 标签页 -->
    <div class="tabs-section">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部商户" name="all" />
        <el-tab-pane label="待审核" name="pending" />
        <el-tab-pane label="已启用" name="active" />
        <el-tab-pane label="已禁用" name="disabled" />
      </el-tabs>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商户名称">
          <el-input v-model="searchForm.name" placeholder="请输入商户名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="searchForm.contact" placeholder="请输入联系人" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option v-for="item in statusList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary">搜索</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 商户列表 -->
    <div class="merchant-list">
      <el-table
        v-loading="loading"
        :data="merchantList"
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column label="商户信息" min-width="200">
          <template #default="{ row }">
            <div class="merchant-info">
              <div class="merchant-logo">
                {{ row.name.charAt(0) }}
              </div>
              <div class="merchant-detail">
                <div class="merchant-name">{{ row.name }}</div>
                <div class="merchant-contact">
                  <span>{{ row.contact }}</span>
                  <span class="separator">|</span>
                  <span>{{ row.phone }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="products" label="商品数" width="100" />
        <el-table-column prop="totalSales" label="总销售额" width="120">
          <template #default="{ row }">
            <span class="sales-amount">{{ row.totalSales }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="joinDays" label="入驻时长" width="100">
          <template #default="{ row }"> {{ row.joinDays }}天 </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="入驻时间" width="120" />
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="onViewDetail(row.id)">查看</el-button>
            <template v-if="row.status === 'pending'">
              <el-button type="success" size="small" text @click="onAuditMerchant(row)">审核</el-button>
            </template>
            <template v-else>
              <el-button type="warning" size="small" text @click="onToggleStatus(row)">
                {{ row.status === 'active' ? '禁用' : '启用' }}
              </el-button>
            </template>
            <el-button type="primary" size="small" text @click="onViewProducts(row.id)">商品</el-button>
            <el-button type="primary" size="small" text @click="onViewData(row.id)">数据</el-button>
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
  name: 'AdminMerchants',
};
</script>

<style lang="less" scoped>
.merchant-page {
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

.merchant-list {
  background: #fff;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.merchant-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.merchant-logo {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: linear-gradient(135deg, #0080ff 0%, #409eff 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
}

.merchant-detail {
  flex: 1;
}

.merchant-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.merchant-contact {
  font-size: 12px;
  color: #999;

  .separator {
    margin: 0 8px;
  }
}

.sales-amount {
  font-weight: 600;
  color: #52c41a;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
</style>
