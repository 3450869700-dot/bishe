<script lang="ts">
export default {
  name: 'Category',
};
</script>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { onMounted, reactive, ref, computed } from 'vue';
import API_GOODS from '@/apis/goods';
import { ElMessage } from 'element-plus';
// utils
import { debounce } from '@/utils/lodash';
// components
import Navbar from '@/components/Navbar/index.vue';
// hooks
import { usePage } from '@/hooks/shared/usePage';

const router = useRouter();
const { isOmnipotent } = usePage();

onMounted(() => {
  // 首先检查缓存
  const cachedCategories = getCacheData('category_list');
  if (cachedCategories) {
    categoryList.value = cachedCategories;
    getGoodList();
  }
  // 无论是否有缓存，都调用API更新数据
  getCategoryList();

  // 设置滚动预加载
  setupScrollPreload();
});
const categoryIndex = ref(0);
const categoryList = ref<Recordable[]>([]);
const list = ref<Recordable[]>([]);

const loading = ref(false);
const pagination = reactive({
  pageCurrent: 1,
  pageSize: 24,
  total: 0,
});

// 缓存相关配置
const CACHE_KEY_PREFIX = 'category_goods_';
const CACHE_EXPIRE_TIME = 600; // 缓存过期时间（秒），延长到10分钟

// 获取缓存数据
function getCacheData(key: string) {
  try {
    const cacheItem = localStorage.getItem(key);
    if (!cacheItem) return null;

    const { data, expireTime } = JSON.parse(cacheItem);
    // 检查缓存是否过期
    if (Date.now() > expireTime) {
      localStorage.removeItem(key);
      return null;
    }
    return data;
  } catch (error) {
    localStorage.removeItem(key);
    return null;
  }
}

// 设置缓存数据
function setCacheData(key: string, data: any) {
  try {
    const cacheItem = {
      data,
      expireTime: Date.now() + CACHE_EXPIRE_TIME * 1000,
    };
    localStorage.setItem(key, JSON.stringify(cacheItem));
  } catch (error) {
    // 缓存失败不影响正常功能
    console.log('缓存设置失败:', error);
  }
}

// 清除指定分类的缓存
function clearCategoryCache(categoryId: number) {
  const cacheKey = `${CACHE_KEY_PREFIX}${categoryId}`;
  localStorage.removeItem(cacheKey);
}

// 计算当前分类名称，确保加载前有默认值
const currentCategoryName = computed(() => {
  return categoryList.value[categoryIndex.value]?.name || '商品列表';
});

function onCategoryChange(index: number) {
  categoryIndex.value = index;
  getGoodList();
}

function getCategoryList() {
  // 先显示骨架屏
  loading.value = true;

  // 调用API获取分类列表
  API_GOODS.goodsCategoryAll()
    .then((res) => {
      if (res.data?.length) {
        categoryList.value = res.data;
        // 缓存分类列表
        setCacheData('category_list', res.data);
        // 如果是首次加载，获取商品列表
        if (list.value.length === 0) {
          getGoodList();
        }
      }
    })
    .catch((error) => {
      console.error('获取分类列表失败:', error);
      ElMessage.error('获取分类失败，请检查后端服务是否正常运行');
    })
    .finally(() => {
      // 分类加载完成后再关闭加载状态
      if (categoryList.value.length > 0) {
        loading.value = false;
      }
    });
}

function getGoodList() {
  if (!categoryList.value.length) return;

  const categoryId = categoryList.value[categoryIndex.value].id;
  console.log('当前分类ID:', categoryId);

  // 检查缓存
  const cacheKey = `${CACHE_KEY_PREFIX}${categoryId}_${pagination.pageCurrent}_${pagination.pageSize}`;
  const cachedData = getCacheData(cacheKey);

  if (cachedData) {
    // 使用缓存数据
    list.value = cachedData.result || [];
    pagination.total = cachedData.total || 0;
    loading.value = false;
    return;
  }

  loading.value = true;
  // 不清空列表，保持布局稳定

  const params = {
    categoryId: categoryId,
    page: pagination.pageCurrent - 1, // 后端使用0-based分页
    pageSize: pagination.pageSize,
  };

  // 使用API_GOODS.goodsList函数
  API_GOODS.goodsList(params)
    .then((res) => {
      if (res.code === 0 && res.data) {
        list.value = res.data.result || [];
        pagination.total = res.data.totalRow || 0;
        // 缓存商品数据
        setCacheData(cacheKey, {
          result: res.data.result,
          total: res.data.totalRow,
        });

        // 预加载图片
        preloadImages(list.value);
      } else {
        console.error('获取商品列表失败：响应格式错误', res);
        ElMessage.error('获取商品列表失败：响应格式错误');
      }
    })
    .catch((error) => {
      console.error('获取商品列表失败:', error);
      ElMessage.error('获取商品列表失败，请检查后端服务是否正常运行');
    })
    .finally(() => {
      loading.value = false;
    });
}

// 预加载图片 - 只预加载前12张图片，其余图片使用懒加载
function preloadImages(products: Recordable[]) {
  // 只预加载前12张图片，其余图片使用懒加载
  const productsToPreload = products.slice(0, 12);
  productsToPreload.forEach((product) => {
    if (product.pic) {
      const img = new Image();
      img.src = product.pic;
    }
  });
}

// 预加载商品详情
async function preloadGoodsDetail(id: number) {
  // 检查是否已缓存
  const cacheKey = `goods_detail_${id}`;
  if (getCacheData(cacheKey)) return;

  try {
    // 确保传递对象参数，与API_GOODS.goodsDetail的预期一致
    const detail = await API_GOODS.goodsDetail({ id });
    setCacheData(cacheKey, detail.data);
  } catch (error) {
    // 预加载失败不影响用户体验，忽略错误
    console.log('预加载商品详情失败:', error);
  }
}

// 滚动预加载下一页 - 添加防抖处理
function setupScrollPreload() {
  // 添加防抖处理，避免频繁触发预加载
  const handleScroll = debounce(() => {
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
    const scrollHeight = document.documentElement.scrollHeight;
    const clientHeight = document.documentElement.clientHeight;

    // 当滚动到页面底部100px时，预加载下一页
    if (scrollTop + clientHeight >= scrollHeight - 100) {
      // 检查是否还有下一页
      const totalPages = Math.ceil(pagination.total / pagination.pageSize);
      if (pagination.pageCurrent < totalPages) {
        // 预加载下一页数据
        const nextPage = pagination.pageCurrent + 1;
        const cacheKey = `${CACHE_KEY_PREFIX}${categoryList.value[categoryIndex.value]?.id || 'all'}_${nextPage}_${
          pagination.pageSize
        }`;

        // 检查下一页是否已经缓存
        if (!getCacheData(cacheKey)) {
          const params = {
            categoryId: categoryList.value[categoryIndex.value]?.id,
            page: nextPage - 1, // 后端使用0-based分页
            pageSize: pagination.pageSize,
          };

          // 预加载下一页数据
          API_GOODS.goodsList(params)
            .then((res) => {
              if (res.code === 0 && res.data) {
                // 缓存下一页数据
                setCacheData(cacheKey, {
                  result: res.data.result,
                  total: res.data.totalRow,
                });

                // 预加载下一页图片
                if (res.data.result && res.data.result.length) {
                  preloadImages(res.data.result);
                }
              }
            })
            .catch((error) => {
              // 预加载失败不影响用户体验，忽略错误
              console.log('预加载下一页失败:', error);
            });
        }
      }
    }
  }, 200); // 200ms防抖延迟

  // 添加滚动事件监听
  window.addEventListener('scroll', handleScroll);

  // 组件卸载时移除事件监听
  return () => {
    window.removeEventListener('scroll', handleScroll);
  };
}

function onGoodClicked(id: number) {
  const url = router.resolve({ path: '/good/detail', query: { id } });
  window.open(url.href, '_blank');
}

// 商品卡片悬停时预加载详情
function onGoodHover(item: any) {
  preloadGoodsDetail(item.id);
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
</script>

<template>
  <div class="page-container">
    <!-- 统一导航栏 -->
    <Navbar :show-role-select="true" :is-omnipotent="isOmnipotent" />

    <!-- 主内容区 -->
    <main class="main-content">
      <div class="white-container category-layout">
        <div class="category-sidebar">
          <div class="card-header">
            <h3>商品分类</h3>
          </div>
          <div class="category-scroll-wrapper">
            <el-menu
              :default-active="categoryIndex.toString()"
              class="category-menu"
              mode="vertical"
              @select="onCategoryChange"
            >
              <el-menu-item v-for="(item, index) in categoryList" :key="index" :index="index.toString()">
                {{ item.name }}
              </el-menu-item>
            </el-menu>
          </div>
        </div>

        <div class="goods-area">
          <div class="card-header">
            <h3>{{ currentCategoryName }}</h3>
          </div>

          <!-- 商品网格 -->
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

          <!-- 空状态 -->
          <div v-else-if="list.length === 0" class="empty-state">
            <el-empty description="当前并无此类商品" :image-size="120"></el-empty>
          </div>

          <!-- 商品列表 -->
          <div v-else class="goods-grid">
            <div
              v-for="item in list"
              :key="item.id"
              class="goods-item"
              @click="onGoodClicked(item.id)"
              @mouseenter="onGoodHover(item)"
            >
              <div class="goods-image-wrapper">
                <!-- 商品图片 -->
                <img
                  class="goods-image"
                  :src="item.pic || ''"
                  :alt="item.name"
                  loading="lazy"
                  @error="
                    (e) => {
                      // 图片加载失败时显示默认占位图
                      e.target.src = '/src/assets/images/avatar_default.png';
                      e.target.classList.add('image-error');
                    }
                  "
                  @load="
                    (e) => {
                      // 图片加载成功时移除错误状态
                      e.target.classList.remove('image-error');
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
          <div class="pagination-container">
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
/* 分类页面布局 */
.category-layout {
  display: flex;
  gap: 20px;
}

/* 左侧分类栏 */
.category-sidebar {
  width: 240px;
  flex-shrink: 0;
}

/* 分类滚动包装器 */
.category-scroll-wrapper {
  max-height: 390px; /* 6个分类项的高度，每个约65px */
  overflow-y: auto;
  overflow-x: hidden;
  margin-top: 10px;

  /* 美化滚动条 */
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #a1a1a1;
  }
}

/* 右侧商品区域 */
.goods-area {
  flex: 1;
}

.category-menu {
  border-right: none;

  .el-menu-item {
    font-size: 16px;
    padding: 15px 20px;
    margin-bottom: 5px;
    border-radius: 8px;
    transition: all 0.3s ease;
    height: auto;
    line-height: 1.5;

    &:hover {
      background-color: var(--color-vice);
      color: var(--color-vice-text);
    }

    &.is-active {
      background-color: var(--color-primary);
      color: #ffffff;
    }
  }
}

/* 右侧商品卡片 */
.goods-card {
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;

  h3 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #333;
  }
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

/* 图片加载优化 */
.goods-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease, opacity 0.3s ease;
  opacity: 1;
}

.goods-image.image-error {
  opacity: 0;
}

/* 图片加载前的占位符 */
.goods-image-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #f8f8f8;
  z-index: 1;
}

.goods-image-wrapper img {
  z-index: 2;
}

.goods-image-wrapper img.image-error + .goods-image-wrapper::before {
  display: block;
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

/* 分页只在有商品时显示 */
:deep(.el-empty + .pagination-container) {
  display: none;
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
