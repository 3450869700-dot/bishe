<script lang="ts">
export default {
  name: 'Search',
};
</script>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router';
import { onMounted, reactive, ref, watch } from 'vue';
import API_GOODS from '@/apis/goods';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
// components
import Navbar from '@/components/Navbar/index.vue';
// hooks
import { usePage } from '@/hooks/shared/usePage';
import { useRecordSearch } from '@/hooks/shared/useBehavior';

const router = useRouter();
const route = useRoute();
const { isOmnipotent } = usePage();

const keyword = ref('');
const list = ref<Recordable[]>([]);
const loading = ref(false);
const hasSearched = ref(false);

const pagination = reactive({
  pageCurrent: 1,
  pageSize: 24,
  total: 0,
});

onMounted(() => {
  // 从URL参数获取关键词
  const queryKeyword = route.query.keyword as string;
  if (queryKeyword) {
    keyword.value = queryKeyword;
    performSearch();
  }
});

// 监听路由参数变化
watch(
  () => route.query.keyword,
  (newKeyword) => {
    if (newKeyword && newKeyword !== keyword.value) {
      keyword.value = newKeyword as string;
      performSearch();
    }
  },
);

function performSearch() {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词');
    return;
  }

  // 记录搜索行为
  useRecordSearch(keyword.value.trim());

  hasSearched.value = true;
  pagination.pageCurrent = 1;
  getGoodList();
}

function getGoodList() {
  loading.value = true;

  const params = {
    keyword: keyword.value.trim(),
    page: pagination.pageCurrent - 1,
    pageSize: pagination.pageSize,
  };

  API_GOODS.goodsList(params)
    .then((res) => {
      if (res.code === 0 && res.data) {
        list.value = res.data.result || [];
        pagination.total = res.data.totalRow || 0;
      } else {
        list.value = [];
        pagination.total = 0;
      }
    })
    .catch((error) => {
      console.error('搜索商品失败:', error);
      ElMessage.error('搜索失败，请稍后重试');
      list.value = [];
      pagination.total = 0;
    })
    .finally(() => {
      loading.value = false;
    });
}

function onGoodClicked(id: number) {
  const url = router.resolve({ path: '/good/detail', query: { id } });
  window.open(url.href, '_blank');
}

function handleSizeChange(val: number) {
  pagination.pageSize = val;
  pagination.pageCurrent = 1;
  getGoodList();
}

function handleCurrentChange(val: number) {
  pagination.pageCurrent = val;
  getGoodList();
}

function handleKeyup(event: KeyboardEvent) {
  if (event.key === 'Enter') {
    performSearch();
  }
}
</script>

<template>
  <div class="page-container">
    <!-- 统一导航栏 -->
    <Navbar :show-role-select="true" :is-omnipotent="isOmnipotent" />

    <!-- 主内容区 -->
    <main class="main-content">
      <div class="white-container">
        <!-- 搜索区域 -->
        <div class="search-header">
          <h2>商品搜索</h2>
          <div class="search-input-wrapper">
            <el-input v-model="keyword" placeholder="请输入商品名称" size="large" clearable @keyup="handleKeyup">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
              <template #append>
                <el-button type="primary" @click="performSearch">搜索</el-button>
              </template>
            </el-input>
          </div>
        </div>

        <!-- 搜索结果 -->
        <div class="search-results">
          <!-- 加载状态 -->
          <div v-if="loading" class="skeleton-container">
            <div v-for="i in 12" :key="i" class="skeleton-item">
              <div class="skeleton-image"></div>
              <div class="skeleton-info">
                <div class="skeleton-title"></div>
                <div class="skeleton-price"></div>
              </div>
            </div>
          </div>

          <!-- 未搜索状态 -->
          <div v-else-if="!hasSearched" class="empty-state">
            <el-empty description="请输入关键词搜索商品" :image-size="120">
              <template #image>
                <el-icon :size="60" color="#ccc"><Search /></el-icon>
              </template>
            </el-empty>
          </div>

          <!-- 无结果状态 -->
          <div v-else-if="list.length === 0" class="empty-state">
            <el-empty description="未找到相关商品" :image-size="120" />
          </div>

          <!-- 商品列表 -->
          <div v-else class="goods-grid">
            <div v-for="item in list" :key="item.id" class="goods-item" @click="onGoodClicked(item.id)">
              <div class="goods-image-wrapper">
                <img
                  class="goods-image"
                  :src="item.pic || ''"
                  :alt="item.name"
                  loading="lazy"
                  @error="
                    (e) => {
                      e.target.src = '/src/assets/images/avatar_default.png';
                      e.target.classList.add('image-error');
                    }
                  "
                />
              </div>
              <div class="goods-info">
                <div class="goods-title">{{ item.name }}</div>
                <div class="goods-price">¥{{ item.minPrice }}</div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="list.length > 0" class="pagination-container">
            <el-pagination
              v-model:current-page="pagination.pageCurrent"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[12, 24, 36, 48]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pagination.total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style lang="less" scoped>
/* 搜索头部 */
.search-header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;

  h2 {
    margin: 0 0 20px 0;
    font-size: 24px;
    font-weight: 600;
    color: #333;
  }
}

.search-input-wrapper {
  max-width: 600px;

  :deep(.el-input__wrapper) {
    border-radius: 4px 0 0 4px;
  }

  :deep(.el-input-group__append) {
    padding: 0;

    .el-button {
      border-radius: 0 4px 4px 0;
      padding: 0 30px;
      height: 100%;
    }
  }
}

/* 搜索结果区域 */
.search-results {
  padding: 20px;
}

/* 商品网格 - 6列布局 */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

/* 商品项 */
.goods-item {
  background-color: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;

  &:hover {
    border-color: var(--color-primary);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }
}

/* 商品图片 */
.goods-image-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  background-color: #f8f8f8;
  overflow: hidden;

  .goods-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
    border-radius: 4px 4px 0 0;

    &:hover {
      transform: scale(1.05);
    }
  }
}

/* 商品信息 */
.goods-info {
  padding: 10px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 商品标题 */
.goods-title {
  font-size: 14px;
  color: #333333;
  margin-bottom: 8px;
  line-height: 20px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 商品价格 */
.goods-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff4400;
  margin-top: auto;
}

/* 骨架屏样式 - 6列布局 */
.skeleton-container {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.skeleton-item {
  display: flex;
  flex-direction: column;
  background-color: #f8f8f8;
  border-radius: 8px;
  padding: 16px;
  height: 240px;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-image {
  width: 100%;
  height: 160px;
  background-color: #e8e8e8;
  border-radius: 4px;
  margin-bottom: 12px;
}

.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.skeleton-title {
  width: 80%;
  height: 16px;
  background-color: #e8e8e8;
  border-radius: 4px;
  margin-bottom: 8px;
}

.skeleton-price {
  width: 60%;
  height: 18px;
  background-color: #e8e8e8;
  border-radius: 4px;
}

@keyframes skeleton-loading {
  0% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 0.6;
  }
}

/* 空状态 */
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 20px;
  text-align: center;
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eaeaea;
}

/* 响应式设计 - 6列布局 */
@media (max-width: 1400px) {
  .goods-grid,
  .skeleton-container {
    grid-template-columns: repeat(5, 1fr);
    gap: 14px;
  }
}

@media (max-width: 1200px) {
  .goods-grid,
  .skeleton-container {
    grid-template-columns: repeat(4, 1fr);
    gap: 14px;
  }
}

@media (max-width: 992px) {
  .goods-grid,
  .skeleton-container {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .goods-grid,
  .skeleton-container {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
}
</style>
