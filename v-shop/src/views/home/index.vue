<script lang="ts">
export default {
  name: 'Home',
};
</script>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { onMounted, reactive, ref, computed, onUnmounted } from 'vue';
import API_GOODS from '@/apis/goods';
import API_CART from '@/apis/cart';
import { ElMessage, ElDialog, ElInputNumber, ElButton } from 'element-plus';
import { Sort, Filter } from '@element-plus/icons-vue';
// components
import Navbar from '@/components/Navbar/index.vue';
import Recommendation from '@/components/Recommendation/index.vue';
// hooks
import { usePage } from '@/hooks/shared/usePage';
// utils
import { debounce } from '@/utils';

// ==================== 缓存工具 ====================
const CACHE_KEY_PREFIX = 'home_goods_';
const CACHE_EXPIRE_TIME = 600; // 10分钟
const BANNER_CACHE_KEY = 'home_banner_list';

const cacheUtils = {
  get(key: string) {
    try {
      const item = localStorage.getItem(key);
      if (!item) return null;
      const { data, expireTime } = JSON.parse(item);
      if (Date.now() > expireTime) {
        localStorage.removeItem(key);
        return null;
      }
      return data;
    } catch {
      localStorage.removeItem(key);
      return null;
    }
  },
  set(key: string, data: any) {
    try {
      localStorage.setItem(
        key,
        JSON.stringify({
          data,
          expireTime: Date.now() + CACHE_EXPIRE_TIME * 1000,
        }),
      );
    } catch (e) {
      console.log('缓存设置失败:', e);
    }
  },
};

// ==================== 图片工具 ====================
const imageUtils = {
  preload(urls: string[], limit = 12) {
    urls.slice(0, limit).forEach((url) => {
      if (url) {
        const img = new Image();
        img.src = url;
      }
    });
  },
  getPlaceholder() {
    return '/src/assets/images/avatar_default.png';
  },
  handleError(e: Event) {
    const target = e.target as HTMLImageElement;
    target.src = imageUtils.getPlaceholder();
    target.classList.add('image-error');
  },
  handleLoad(e: Event) {
    (e.target as HTMLImageElement).classList.remove('image-error');
  },
};

// ==================== 组件逻辑 ====================
const router = useRouter();
const { isOmnipotent } = usePage();

// 购物车弹窗
const cartDialogVisible = ref(false);
const currentGood = ref<Recordable>({});
const cartQuantity = ref(1);

const totalPrice = computed(() => {
  const price = currentGood.value.price_num || currentGood.value.minPriceNum || 0;
  return price * cartQuantity.value;
});

const priceUnit = computed(() => currentGood.value.price_unit || '');

// 商品列表
const bannerList = ref<Recordable[]>([]);
const list = ref<Recordable[]>([]);
const loading = ref(false);
const pagination = reactive({
  pageCurrent: 1,
  pageSize: 24,
  total: 0,
});

// 轮播图数据（静态配置，无需API请求）
const BANNER_DATA = [
  { id: 1, picUrl: '/src/assets/images/carousel_1.png', title: '轮播图1', linkUrl: '' },
  { id: 2, picUrl: '/src/assets/images/carousel_2.png', title: '轮播图2', linkUrl: '' },
  { id: 3, picUrl: '/src/assets/images/carousel_3.png', title: '轮播图3', linkUrl: '' },
];

// ==================== 数据获取 ====================
async function fetchGoodsList(page = pagination.pageCurrent, useCache = true) {
  const cacheKey = `${CACHE_KEY_PREFIX}${page}_${pagination.pageSize}`;

  if (useCache) {
    const cached = cacheUtils.get(cacheKey);
    if (cached) {
      list.value = cached.result || [];
      pagination.total = cached.total || 0;
      return;
    }
  }

  loading.value = true;
  try {
    const res = await API_GOODS.goodsList({
      page: page - 1,
      pageSize: pagination.pageSize,
    });

    if (res.code === 0 && res.data) {
      list.value = res.data.result || [];
      pagination.total = res.data.totalRow || 0;

      cacheUtils.set(cacheKey, {
        result: res.data.result,
        total: res.data.totalRow,
      });

      // 预加载图片（使用 requestIdleCallback 避免阻塞）
      const imageUrls = list.value.map((item) => item.pic).filter(Boolean);
      if ('requestIdleCallback' in window) {
        requestIdleCallback(() => imageUtils.preload(imageUrls), { timeout: 2000 });
      } else {
        setTimeout(() => imageUtils.preload(imageUrls), 100);
      }
    }
  } catch (error) {
    console.error('获取商品列表失败:', error);
    ElMessage.error('获取商品列表失败');
  } finally {
    loading.value = false;
  }
}

// ==================== 事件处理 ====================
function openCartDialog(item: Recordable) {
  currentGood.value = item;
  cartQuantity.value = 1;
  cartDialogVisible.value = true;
}

async function confirmAddToCart() {
  if (!currentGood.value.id) return;

  try {
    const res = await API_CART.cartAdd({
      userId: 1,
      productCode: parseInt(currentGood.value.id),
      productNum: cartQuantity.value,
      productId: currentGood.value.id,
      productPrice: currentGood.value.price_num || currentGood.value.minPriceNum || 0,
    });

    if (res.code === 0) {
      ElMessage.success(`已将${currentGood.value.name}加入购物车`);
      cartDialogVisible.value = false;
    } else {
      ElMessage.error(res.msg || '加入购物车失败');
    }
  } catch (error) {
    console.error('加入购物车失败:', error);
    ElMessage.error('加入购物车失败');
  }
}

function onGoodClicked(id: number) {
  const url = router.resolve({ path: '/good/detail', query: { id } });
  window.open(url.href, '_blank');
}

function handleCurrentChange(val: number) {
  pagination.pageCurrent = val;
  fetchGoodsList(val, false);
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// ==================== 滚动预加载 ====================
let scrollCleanup: (() => void) | null = null;

function setupScrollPreload() {
  const handleScroll = debounce(() => {
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
    const scrollHeight = document.documentElement.scrollHeight;
    const clientHeight = document.documentElement.clientHeight;

    if (scrollTop + clientHeight >= scrollHeight - 200) {
      const totalPages = Math.ceil(pagination.total / pagination.pageSize);
      if (pagination.pageCurrent < totalPages) {
        const nextPage = pagination.pageCurrent + 1;
        const cacheKey = `${CACHE_KEY_PREFIX}${nextPage}_${pagination.pageSize}`;

        if (!cacheUtils.get(cacheKey)) {
          API_GOODS.goodsList({ page: nextPage - 1, pageSize: pagination.pageSize })
            .then((res) => {
              if (res.code === 0 && res.data) {
                cacheUtils.set(cacheKey, {
                  result: res.data.result,
                  total: res.data.totalRow,
                });
              }
            })
            .catch(() => {}); // 静默处理预加载错误
        }
      }
    }
  }, 300);

  window.addEventListener('scroll', handleScroll, { passive: true });
  scrollCleanup = () => window.removeEventListener('scroll', handleScroll);
}

// ==================== 生命周期 ====================
onMounted(() => {
  // 设置轮播图（优先使用缓存）
  bannerList.value = cacheUtils.get(BANNER_CACHE_KEY) || BANNER_DATA;
  cacheUtils.set(BANNER_CACHE_KEY, BANNER_DATA);

  // 获取商品列表
  fetchGoodsList();

  // 设置滚动预加载
  setupScrollPreload();
});

onUnmounted(() => {
  scrollCleanup?.();
});
</script>

<template>
  <div class="page-container">
    <!-- 导航栏 -->
    <Navbar :show-role-select="true" :is-omnipotent="isOmnipotent" />

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 轮播图 -->
      <div class="banner-section">
        <el-carousel :interval="10000" arrow="hover" height="100%" indicator-position="none">
          <el-carousel-item
            v-for="item in bannerList"
            :key="item.id"
            @click="item.linkUrl && (window.location.href = item.linkUrl)"
          >
            <el-image class="banner-image" fit="cover" :src="item.picUrl" :alt="item.title" />
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 推荐商品 -->
      <Recommendation />

      <!-- 商品列表 -->
      <div class="goods-section">
        <div class="section-title">
          <h2>商品列表</h2>
          <div class="section-actions">
            <el-button type="default" size="small">
              <el-icon><Sort /></el-icon>排序
            </el-button>
            <el-button type="default" size="small">
              <el-icon><Filter /></el-icon>筛选
            </el-button>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="skeleton-container">
          <div v-for="i in 12" :key="i" class="skeleton-item" />
        </div>

        <!-- 空状态 -->
        <div v-else-if="list.length === 0" class="empty-state">
          <el-empty description="暂无商品" :image-size="120" />
        </div>

        <!-- 商品列表 -->
        <div v-else class="goods-grid">
          <div v-for="item in list" :key="item.id" class="goods-card">
            <div class="goods-image-wrapper" @click="onGoodClicked(item.id)">
              <img
                class="goods-image"
                :src="item.pic"
                :alt="item.name"
                loading="lazy"
                @error="imageUtils.handleError"
                @load="imageUtils.handleLoad"
              />
              <div v-if="item.recommendStatus" class="recommend-badge">推荐</div>
            </div>
            <div class="goods-info">
              <div class="goods-title" @click="onGoodClicked(item.id)">{{ item.name }}</div>
              <div class="goods-price">
                <span class="current-price">¥{{ item.minPrice }}</span>
                <span v-if="item.originalPrice > 0" class="original-price">¥{{ item.originalPrice }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="pagination.pageCurrent"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[12, 24, 36, 48]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
            @size-change="fetchGoodsList(1, false)"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </main>

    <!-- 加入购物车弹窗 -->
    <ElDialog v-model="cartDialogVisible" title="加入购物车" width="400px" :close-on-click-modal="false">
      <div v-if="currentGood" class="cart-dialog-content">
        <div class="dialog-good-info">
          <img
            class="dialog-good-image"
            :src="currentGood.pic"
            :alt="currentGood.name"
            @error="imageUtils.handleError"
          />
          <div class="dialog-good-details">
            <div class="dialog-good-name">{{ currentGood.name }}</div>
            <div class="dialog-good-price">¥{{ currentGood.price_num || currentGood.minPriceNum }} {{ priceUnit }}</div>
          </div>
        </div>

        <div class="dialog-quantity-section">
          <div class="dialog-quantity-label">数量</div>
          <ElInputNumber v-model="cartQuantity" :min="1" :max="currentGood.stores || 999" :step="1" size="large" />
        </div>

        <div class="dialog-total-section">
          <div class="dialog-total-label">总价</div>
          <div class="dialog-total-price">¥{{ totalPrice.toFixed(2) }}</div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="cartDialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="confirmAddToCart">确认加入购物车</ElButton>
        </span>
      </template>
    </ElDialog>
  </div>
</template>

<style lang="less" scoped>
// 骨架屏动画
@keyframes skeleton-loading {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}

.page-container {
  min-height: 100vh;
  background-color: #ffffff;
}

// 轮播图区域
.banner-section {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
  overflow: hidden;
  width: 100%;
  height: 300px;
  box-sizing: border-box;
}

.banner-image {
  width: 100%;
  height: 100%;
  display: block;
}

// 确保轮播图填满容器
:deep(.el-carousel) {
  height: 100% !important;
}

:deep(.el-carousel__container) {
  height: 100% !important;
}

:deep(.el-carousel__item) {
  height: 100% !important;
}

:deep(.el-image) {
  width: 100% !important;
  height: 100% !important;
  display: block !important;
}

:deep(.el-image__inner) {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
  display: block !important;
}

// 商品列表区域
.goods-section {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
}

// 标题区域
.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #eaeaea;

  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #333333;
    margin: 0;
  }
}

.section-actions {
  display: flex;
  gap: 10px;
}

// 骨架屏 - 固定6列布局，与商品网格保持一致
.skeleton-container {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.skeleton-item {
  height: 280px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

// 商品网格布局 - 固定6列布局，适配大屏幕
.goods-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

// 商品卡片
.goods-card {
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

// 商品图片
.goods-image-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  background-color: #f8f8f8;
  overflow: hidden;

  .goods-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease, opacity 0.3s ease;

    &:hover {
      transform: scale(1.05);
    }

    &.image-error {
      opacity: 0.8;
    }
  }
}

// 推荐标签
.recommend-badge {
  position: absolute;
  top: 10px;
  left: 0;
  background-color: #f56c6c;
  color: #ffffff;
  padding: 5px 14px;
  font-weight: 500;
  border-radius: 0 4px 4px 0;
}

// 商品信息
.goods-info {
  padding-top: 12px;
  padding-bottom: 12px;
  padding-left: 8px;
  padding-right: 8px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

// 商品标题
.goods-title {
  font-size: 16px !important;
  color: #333333;
  margin-bottom: 10px;
  line-height: 22px;
  height: 44px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

// 商品价格
.goods-price {
  display: flex;
  align-items: center;
  gap: 8px;

  .current-price {
    font-size: 20px;
    font-weight: 600;
    color: #ff4400;
  }

  .original-price {
    color: #999999;
    text-decoration: line-through;
  }
}

// 分页区域
.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eaeaea;
}

// 购物车弹窗样式
.cart-dialog-content {
  padding: 20px 0;
}

.dialog-good-info {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
}

.dialog-good-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 16px;
}

.dialog-good-details {
  flex: 1;
}

.dialog-good-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
  line-height: 20px;
  max-height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.dialog-good-price {
  font-size: 18px;
  font-weight: 600;
  color: #ff0000;
}

.dialog-quantity-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.dialog-quantity-label {
  color: #666;
}

.dialog-total-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 20px;
  border-top: 1px solid #eaeaea;
}

.dialog-total-label {
  font-weight: 500;
  color: #333;
}

.dialog-total-price {
  font-size: 20px;
  font-weight: 600;
  color: #ff0000;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

// 空状态
.empty-state {
  padding: 40px 0;
}
</style>
