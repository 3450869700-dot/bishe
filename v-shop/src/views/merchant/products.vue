<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';

const router = useRouter();
const route = useRoute();

const searchForm = ref({
  name: '',
  category: '',
  status: '',
});

const categoryList = ref([
  { value: '', label: '全部分类' },
  { value: 'fruit', label: '水果' },
  { value: 'vegetable', label: '蔬菜' },
  { value: 'tea', label: '茶叶' },
  { value: 'other', label: '其他' },
]);

const statusList = ref([
  { value: '', label: '全部状态' },
  { value: 'on', label: '上架中' },
  { value: 'off', label: '已下架' },
]);

const productList = ref([
  {
    id: 1,
    name: '红富士苹果',
    category: 'fruit',
    price: 12.8,
    stock: 500,
    status: 'on',
    sales: 1280,
    image: '/src/assets/images/avatar_default.png',
    specs: '5斤装',
    maturity: '一级',
  },
  {
    id: 2,
    name: '有机青菜',
    category: 'vegetable',
    price: 5.5,
    stock: 200,
    status: 'on',
    sales: 560,
    image: '/src/assets/images/avatar_default.png',
    specs: '500g',
    maturity: '特级',
  },
  {
    id: 3,
    name: '龙井新茶',
    category: 'tea',
    price: 188.0,
    stock: 50,
    status: 'off',
    sales: 89,
    image: 'https://via.placeholder.com/80',
    specs: '250g',
    maturity: '特级',
  },
  {
    id: 4,
    name: '红心火龙果',
    category: 'fruit',
    price: 15.9,
    stock: 15,
    status: 'on',
    sales: 342,
    image: 'https://via.placeholder.com/80',
    specs: '6斤装',
    maturity: '一级',
  },
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
  }, 500);
}

function handleSearch() {
  currentPage.value = 1;
  getProductList();
}

function handleReset() {
  searchForm.value = { name: '', category: '', status: '' };
  handleSearch();
}

function onAddProduct() {
  router.push('/merchant/products/edit');
}

function onEditProduct(id: number) {
  router.push(`/merchant/products/edit?id=${id}`);
}

function onToggleStatus(product: (typeof productList.value)[0]) {
  const newStatus = product.status === 'on' ? 'off' : 'on';
  showConfirmDialog({
    title: '确认操作',
    message: `确定要${newStatus === 'on' ? '上架' : '下架'}「${product.name}」吗？`,
  })
    .then(() => {
      product.status = newStatus;
      showToast({
        type: 'success',
        message: `${newStatus === 'on' ? '上架' : '下架'}成功`,
      });
    })
    .catch(() => {});
}

function onDeleteProduct(product: (typeof productList.value)[0]) {
  showConfirmDialog({
    title: '确认删除',
    message: `确定要删除「${product.name}」吗？此操作不可恢复。`,
  })
    .then(() => {
      const index = productList.value.findIndex((p) => p.id === product.id);
      if (index > -1) {
        productList.value.splice(index, 1);
        showToast({
          type: 'success',
          message: '删除成功',
        });
      }
    })
    .catch(() => {});
}

function onManageStock(id: number) {
  router.push(`/merchant/products/stock?id=${id}`);
}

function onManagePresale(id: number) {
  router.push(`/merchant/products/presale?id=${id}`);
}

function onManageGroup(id: number) {
  router.push(`/merchant/products/group?id=${id}`);
}

function handlePageChange(page: number) {
  currentPage.value = page;
  getProductList();
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
        <el-form-item label="商品分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable style="width: 150px">
            <el-option v-for="item in categoryList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="上架状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option v-for="item in statusList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="onAddProduct"> <van-icon name="plus" /> 发布商品 </el-button>
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
              <el-image :src="row.image" fit="cover" class="product-image" />
              <div class="product-detail">
                <div class="product-name">{{ row.name }}</div>
                <div class="product-meta">
                  <el-tag size="small" type="info">{{
                    categoryList.find((c) => c.value === row.category)?.label
                  }}</el-tag>
                  <span class="specs">{{ row.specs }}</span>
                </div>
                <div class="maturity">
                  <van-icon name="award-o" color="#ff7d00" />
                  <span>{{ row.maturity }}成熟度</span>
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
        <el-table-column prop="stock" label="库存" width="100">
          <template #default="{ row }">
            <span :class="{ 'low-stock': row.stock < 20 }">{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sales" label="销量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'on' ? 'success' : 'info'" size="small">
              {{ row.status === 'on' ? '上架中' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="onEditProduct(row.id)">编辑</el-button>
            <el-button
              :type="row.status === 'on' ? 'warning' : 'success'"
              size="small"
              text
              @click="onToggleStatus(row)"
            >
              {{ row.status === 'on' ? '下架' : '上架' }}
            </el-button>
            <el-button type="primary" size="small" text @click="onManageStock(row.id)">库存</el-button>
            <el-dropdown
              trigger="click"
              @command="
                (cmd) => {
                  if (cmd === 'presale') onManagePresale(row.id);
                  else if (cmd === 'group') onManageGroup(row.id);
                }
              "
            >
              <el-button size="small" text> 更多<van-icon name="arrow-down" /> </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="presale">预售设置</el-dropdown-item>
                  <el-dropdown-item command="group">拼团管理</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button type="danger" size="small" text @click="onDeleteProduct(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'MerchantProducts',
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

.action-bar {
  margin-bottom: 20px;
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
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.product-detail {
  flex: 1;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;

  .specs {
    font-size: 12px;
    color: #999;
  }
}

.maturity {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #ff7d00;
}

.price {
  font-size: 16px;
  font-weight: 600;
  color: #ff7d00;
}

.low-stock {
  color: #ff4d4f;
  font-weight: 600;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
</style>
