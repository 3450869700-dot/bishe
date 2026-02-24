<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';

const router = useRouter();

const searchForm = ref({
  name: '',
  merchant: '',
  category: '',
  status: '',
});

const productList = ref([
  {
    id: 1,
    name: '红富士苹果',
    merchant: '阳光果园',
    category: '水果',
    price: 12.8,
    status: 'on_audit',
    auditStatus: 'pending',
    sales: 1280,
    stock: 500,
    createTime: '2024-01-08',
  },
  {
    id: 2,
    name: '有机青菜',
    merchant: '有机蔬菜基地',
    category: '蔬菜',
    price: 5.5,
    status: 'on_sale',
    auditStatus: 'approved',
    sales: 560,
    stock: 200,
    createTime: '2024-01-05',
  },
  {
    id: 3,
    name: '龙井新茶',
    merchant: '龙井茶庄',
    category: '茶叶',
    price: 188.0,
    status: 'on_sale',
    auditStatus: 'approved',
    sales: 89,
    stock: 50,
    createTime: '2024-01-03',
  },
  {
    id: 4,
    name: '红心火龙果',
    merchant: '阳光果园',
    category: '水果',
    price: 15.9,
    status: 'off_shelf',
    auditStatus: 'approved',
    sales: 342,
    stock: 15,
    createTime: '2024-01-01',
  },
  {
    id: 5,
    name: '高山蜂蜜',
    merchant: '高山蜂蜜',
    category: '其他',
    price: 68.0,
    status: 'on_audit',
    auditStatus: 'rejected',
    sales: 0,
    stock: 100,
    createTime: '2024-01-08',
    rejectReason: '商品信息不完整',
  },
]);

const categoryList = ref([
  { value: '', label: '全部分类' },
  { value: '水果', label: '水果' },
  { value: '蔬菜', label: '蔬菜' },
  { value: '茶叶', label: '茶叶' },
  { value: '其他', label: '其他' },
]);

const statusList = ref([
  { value: '', label: '全部状态' },
  { value: 'on_sale', label: '上架中' },
  { value: 'off_shelf', label: '已下架' },
  { value: 'on_audit', label: '待审核' },
]);

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(100);
const loading = ref(false);

onMounted(() => {
  getProductList();
});

function getProductList() {
  loading.value = true;
  setTimeout(() => {
    loading.value = false;
    showToast('加载完成');
  }, 300);
}

function onSearch() {
  currentPage.value = 1;
  getProductList();
}

function onReset() {
  searchForm.value = { name: '', merchant: '', category: '', status: '' };
  onSearch();
}

function onViewDetail(id: number) {
  router.push(`/admin/products/detail?id=${id}`);
}

function onAuditProduct(product: (typeof productList.value)[0]) {
  showConfirmDialog({
    title: '商品审核',
    message: `确认通过商品「${product.name}」的审核吗？`,
  })
    .then(() => {
      product.auditStatus = 'approved';
      product.status = 'on_sale';
      showToast({
        type: 'success',
        message: '审核通过，商品已上架',
      });
    })
    .catch(() => {});
}

function onRejectProduct(product: (typeof productList.value)[0]) {
  showConfirmDialog({
    title: '拒绝审核',
    message: `确定要拒绝商品「${product.name}」吗？请输入原因`,
  })
    .then(() => {
      product.auditStatus = 'rejected';
      product.status = 'off_shelf';
      product.rejectReason = '商品信息不符合规范';
      showToast({
        type: 'success',
        message: '已拒绝该商品',
      });
    })
    .catch(() => {});
}

function onToggleStatus(product: (typeof productList.value)[0]) {
  const action = product.status === 'on_sale' ? '下架' : '上架';
  product.status = product.status === 'on_sale' ? 'off_shelf' : 'on_sale';
  showToast({
    type: 'success',
    message: `${action}成功`,
  });
}

function handlePageChange(page: number) {
  currentPage.value = page;
  getProductList();
}

function getAuditStatusType(status: string): string {
  const map: Record<string, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
  };
  return map[status] || 'info';
}
</script>

<template>
  <div class="product-page">
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="商户名称">
          <el-input v-model="searchForm.merchant" placeholder="请输入商户名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable style="width: 150px">
            <el-option v-for="item in categoryList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option v-for="item in statusList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">搜索</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 待审核商品提示 -->
    <div v-if="productList.some((p) => p.auditStatus === 'pending')" class="audit-tips">
      <van-icon name="info-o" />
      <span
        >有 <strong>{{ productList.filter((p) => p.auditStatus === 'pending').length }}</strong> 件商品待审核</span
      >
      <el-button
        size=" small"
        type="primary"
        text
        @click="
          searchForm.status = 'on_audit';
          onSearch();
        "
        >去审核</el-button
      >
    </div>

    <!-- 商品列表 -->
    <div class="product-list">
      <el-table
        v-loading="loading"
        :data="productList"
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column label="商品信息" min-width="240">
          <template #default="{ row }">
            <div class="product-info">
              <div class="product-image">{{ row.name.charAt(0) }}</div>
              <div class="product-detail">
                <div class="product-name">{{ row.name }}</div>
                <div class="product-meta">
                  <span>{{ row.category }}</span>
                  <span class="separator">|</span>
                  <span>{{ row.merchant }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.price.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus !== 'approved'" :type="getAuditStatusType(row.auditStatus)" size="small">
              {{ row.auditStatus === 'pending' ? '待审核' : '已拒绝' }}
            </el-tag>
            <span v-else class="approved-tag">已审核</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="上架状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'on_sale' ? 'success' : 'info'" size="small">
              {{ row.status === 'on_sale' ? '上架中' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="110" />
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="onViewDetail(row.id)">查看</el-button>
            <template v-if="row.auditStatus === 'pending'">
              <el-button type="success" size="small" text @click="onAuditProduct(row)">通过</el-button>
              <el-button type="danger" size="small" text @click="onRejectProduct(row)">拒绝</el-button>
            </template>
            <template v-else>
              <el-button
                :type="row.status === 'on_sale' ? 'warning' : 'success'"
                size="small"
                text
                @click="onToggleStatus(row)"
              >
                {{ row.status === 'on_sale' ? '下架' : '上架' }}
              </el-button>
            </template>
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
  name: 'AdminProducts',
};
</script>

<style lang="less" scoped>
.product-page {
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

.audit-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 8px;
  margin-bottom: 20px;
  color: #d48806;

  strong {
    color: #d48806;
  }
}

.product-list {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  background: linear-gradient(135deg, #0080ff 0%, #67c23a 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
}

.product-detail {
  flex: 1;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.product-meta {
  font-size: 12px;
  color: #999;

  .separator {
    margin: 0 8px;
  }
}

.price {
  font-size: 16px;
  font-weight: 600;
  color: #ff7d00;
}

.approved-tag {
  color: #52c41a;
  font-size: 12px;
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
