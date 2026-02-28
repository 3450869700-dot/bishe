<script lang="ts">
export default {
  name: 'GoodDetail',
};
</script>

<script setup lang="ts">
import { showConfirmDialog, showToast } from 'vant';
import { useRoute, useRouter } from 'vue-router';
import { computed, onMounted, ref, unref, defineAsyncComponent } from 'vue';
import type { shoppingCartAddParams } from '@/apis/cart/types';
import type { ISku, IInitialSku } from '@/components/Sku/types';
import { debounce } from '@/utils';
import { decimalFormat, priceIntegerFormat } from '@/utils/format';
import { getAfterSaleTitle } from '@/model/modules/order/afterSale';
import API_GOODS from '@/apis/goods';
import API_CART from '@/apis/cart';
// components
import Navbar from '@/components/Navbar/index.vue';
// 懒加载组件
const Coupons = defineAsyncComponent(() => import('./components/Coupons.vue'));
const Reputations = defineAsyncComponent(() => import('./components/Reputations.vue'));
// store
import { useOrderStore } from '@/store/modules/order';
import { useUserStore } from '@/store/modules/user';
// hooks
import { usePage } from '@/hooks/shared/usePage';
import { ElDialog, ElInputNumber, ElButton } from 'element-plus';

// 缓存相关配置
const CACHE_KEY_PREFIX = 'goods_detail_';
const CACHE_EXPIRE_TIME = 600; // 缓存过期时间（秒），延长到10分钟

// 加载状态
const loading = ref(true);
const imageLoading = ref(true);

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

// 防抖处理的 API 调用
const debouncedGetGoodsDetail = debounce(getGoodsDetail, 300);

onMounted(() => {
  debouncedGetGoodsDetail();

  if (unref(hasLogin)) {
    getCartCount();
  }
});

const route = useRoute();
const router = useRouter();
const orderStore = useOrderStore();
const userStore = useUserStore();
const { hasLogin, isOmnipotent } = usePage();

const picList = ref<Recordable[]>([]);
const basicInfo = ref<Recordable>({});
const logistics = ref<Recordable>({});
const content = ref('');

// 规格相关
const specifications = ref<Recordable[]>([]);
const selectedSpecification = ref<Recordable>({});

// 规格索引映射，用于O(1)时间复杂度查找规格
const specificationIndex = computed(() => {
  const index: Record<string | number, Recordable> = {};
  specifications.value.forEach((spec) => {
    if (spec.id) {
      index[spec.id] = spec;
    }
  });
  return index;
});

// 动态更新的价格和库存
const goodPrice = ref<number>(0);
const goodStock = ref<number>(0);
const goodSpecificationDesc = ref<string>('');

// 图片加载完成处理
function handleImageLoad() {
  imageLoading.value = false;
}

// 图片加载失败处理
function handleImageError(e: Event) {
  const target = e.target as HTMLImageElement;
  target.src = '/src/assets/images/avatar_default.png';
  imageLoading.value = false;
}

const goodDeliveryTitle = computed(() => {
  return `运费 ${unref(logistics).isFree ? '包邮' : '不包邮'}`;
});

// 购物车弹窗相关
const cartDialogVisible = ref(false);
const cartQuantity = ref(1);

// 计算总价 - 使用缓存的计算结果
const totalPrice = computed(() => {
  // 确保goodPrice是数字类型
  const price = Number(goodPrice.value) || 0;
  const quantity = Number(cartQuantity.value) || 1;
  return price * quantity;
});

// 获取价格单位
const priceUnit = computed(() => {
  return basicInfo.value.price_unit || '';
});

// 打开购物车弹窗
function openCartDialog() {
  if (!selectedSpecification.value.id) {
    showToast('请选择商品规格');
    return;
  }
  cartQuantity.value = 1;
  cartDialogVisible.value = true;
}

// 确认加入购物车
function confirmAddToCart() {
  if (!selectedSpecification.value.id) {
    showToast('请选择商品规格');
    return;
  }

  // 从用户状态中获取真实的用户ID
  // 优先使用 user_id（数据库主键），如果不存在则使用 id
  const userInfo = userStore.getUserInfo;
  console.log('=== confirmAddToCart - userInfo:', userInfo);
  console.log('=== confirmAddToCart - userInfo.user_id:', userInfo?.user_id);
  console.log('=== confirmAddToCart - userInfo.id:', userInfo?.id);

  const userId = userInfo?.user_id || userInfo?.id || 1;

  // 使用选中规格的id作为productCode，确保是字符串类型
  // 注意：每个规格实际上是一个独立的商品记录，有自己的productCode
  const productCode = (selectedSpecification.value.id || '').toString();
  console.log('selectedSpecification:', selectedSpecification.value);
  console.log('productCode:', productCode);
  console.log('userId:', userId);
  console.log('=== confirmAddToCart - final userId:', userId, typeof userId);

  // 检查productCode是否有效
  if (!productCode || productCode === 'NaN' || productCode === 'undefined' || productCode === 'null') {
    showToast('商品编码无效，无法加入购物车');
    return;
  }

  API_CART.cartAdd({
    userId,
    productCode,
    productNum: cartQuantity.value,
    productPrice: goodPrice.value,
    productSpec: selectedSpecification.value.specification,
  })
    .then((res) => {
      console.log('加入购物车成功:', res);
      if (res.code === 0) {
        showToast('已成功加入购物车');
        cartDialogVisible.value = false;
        getCartCount();
      } else {
        showToast(`加入购物车失败: ${res.msg || '未知错误'}`);
      }
    })
    .catch((error) => {
      console.error('加入购物车失败:', error);
      showToast('加入购物车失败，请检查后端服务是否正常运行');
    });
}

// 从价格字符串中提取数字
function extractPrice(priceStr: string): number {
  if (!priceStr) return 0;
  const match = priceStr.match(/(\d+\.\d+|\d+)/);
  return match ? Number(match[1]) : 0;
}

// 处理规格选择
function handleSpecificationChange(spec: Recordable) {
  selectedSpecification.value = spec;
  // 从price字符串中提取数字作为价格
  goodPrice.value = extractPrice(spec.price) || 0;
  goodStock.value = Number(spec.stock) || 0;
  goodSpecificationDesc.value = spec.specificationDesc || '';
}

// 通过规格ID快速查找规格（使用索引，O(1)时间复杂度）
function getSpecificationById(specId: string | number): Recordable | undefined {
  return specificationIndex.value[specId];
}

function getGoodsDetail() {
  // 显示加载状态
  loading.value = true;

  // 重置选中的规格，避免从其他商品页面跳转过来时保留旧的规格
  selectedSpecification.value = {};
  goodPrice.value = 0;
  goodStock.value = 0;
  goodSpecificationDesc.value = '';

  // 将route.query.id转换为数字
  const idParam = route.query.id;
  let goodsId;

  if (Array.isArray(idParam)) {
    // 如果是数组类型，取第一个元素
    goodsId = parseInt(idParam[0], 10);
  } else if (typeof idParam === 'string') {
    goodsId = parseInt(idParam, 10);
  } else if (typeof idParam === 'number') {
    goodsId = idParam;
  } else {
    // 处理id参数不存在或无效的情况
    console.error('无效的商品ID参数:', idParam);
    showToast('无效的商品ID');
    loading.value = false;
    // 只在当前路径不是分类页面时才跳转，避免循环重定向
    if (route.path !== '/category') {
      router.push({ path: '/category', replace: true });
    }
    return;
  }

  if (isNaN(goodsId)) {
    console.error('商品ID转换失败:', idParam);
    showToast('无效的商品ID');
    loading.value = false;
    // 只在当前路径不是分类页面时才跳转，避免循环重定向
    if (route.path !== '/category') {
      router.push({ path: '/category', replace: true });
    }
    return;
  }

  console.log('获取商品详情，ID:', goodsId);

  // 检查缓存
  const cacheKey = `${CACHE_KEY_PREFIX}${goodsId}`;
  const cachedData = getCacheData(cacheKey);

  if (cachedData) {
    // 使用缓存数据
    console.log('使用缓存数据:', cachedData);
    picList.value = cachedData.pics || [];
    basicInfo.value = cachedData.basicInfo || {};
    logistics.value = cachedData?.logistics ?? {};
    content.value = cachedData.content || '<p>暂无商品详情</p>';
    specifications.value = cachedData.specifications || [];

    // 商品已下架
    if (unref(basicInfo).status === 1) {
      showToast(unref(basicInfo).statusStr || '商品已下架');
      loading.value = false;
      return;
    }

    // 初始化默认规格
    if (specifications.value.length > 0) {
      handleSpecificationChange(specifications.value[0]);
    } else {
      // 如果没有规格数据，使用基本信息
      goodPrice.value = extractPrice(basicInfo.value.minPrice) || extractPrice(basicInfo.value.originalPrice) || 0;
      goodStock.value = Number(basicInfo.value.stores) || 0;
    }

    document.title = unref(basicInfo).name || '商品详情';
    getAfterService();
    loading.value = false;
    return;
  }

  API_GOODS.goodsDetail({ id: goodsId })
    .then((res) => {
      console.log('商品详情数据:', res);

      // 检查响应数据格式
      // 注意：res参数就是后端返回的response.data，因为响应拦截器已经处理过了
      if (res && res.code === 0 && res.data) {
        console.log('响应数据结构:', res.data);
        console.log('基本信息:', res.data.basicInfo);
        console.log('规格列表:', res.data.specifications);

        picList.value = res.data.pics || [];
        basicInfo.value = res.data.basicInfo || {};
        logistics.value = res.data?.logistics ?? {};
        content.value = res.data.content || '<p>暂无商品详情</p>';
        console.log('商品详情内容:', content.value);
        specifications.value = res.data.specifications || [];

        // 缓存商品详情数据
        setCacheData(cacheKey, res.data);

        // 商品已下架
        if (unref(basicInfo).status === 1) {
          showToast(unref(basicInfo).statusStr || '商品已下架');
          loading.value = false;
          return;
        }

        document.title = unref(basicInfo).name || '商品详情';

        // 初始化默认规格
        if (specifications.value.length > 0) {
          handleSpecificationChange(specifications.value[0]);
        } else {
          // 如果没有规格数据，使用基本信息
          goodPrice.value = extractPrice(basicInfo.value.minPrice) || extractPrice(basicInfo.value.originalPrice) || 0;
          goodStock.value = Number(basicInfo.value.stores) || 0;
        }

        getAfterService();
        // TODO 商品收藏
      } else {
        console.error('商品详情数据格式错误:', res);
        showToast('商品详情数据格式错误');
        // 重置所有数据
        picList.value = [];
        basicInfo.value = {};
        logistics.value = {};
        content.value = '<p>暂无商品详情</p>';
        specifications.value = [];
        goodPrice.value = 0;
        goodStock.value = 0;
        goodSpecificationDesc.value = '';
      }
    })
    .catch((error) => {
      console.error('获取商品详情失败:', error);
      showToast('获取商品详情失败，请检查后端服务是否正常运行');
      // 删除模拟数据，只显示错误提示
      picList.value = [];
      basicInfo.value = {};
      logistics.value = {};
      content.value = '<p>暂无商品详情</p>';
      specifications.value = [];
      goodPrice.value = 0;
      goodStock.value = 0;
      goodSpecificationDesc.value = '';
    })
    .finally(() => {
      // 无论成功失败，都关闭加载状态
      loading.value = false;
    });
}

function onConcatService() {
  showToast('未开放：客服');
}

// 售后服务
const afterSaleTitle = ref('');
function getAfterService() {
  afterSaleTitle.value = getAfterSaleTitle(unref(basicInfo).afterSale);
}

// 购物车
const cartCount = ref<number | undefined>(undefined);
function getCartCount() {
  API_CART.cartCount()
    .then((res) => {
      console.log('获取购物车数量成功:', res);
      cartCount.value = res.data as number;
    })
    .catch((error) => {
      // 处理请求失败，忽略错误，不显示购物车数量
      console.log('获取购物车信息失败:', error);
      // 不做任何操作，cartCount保持undefined
    });
}

function addCartHandle() {
  openCartDialog();
}

// 立即购买
function handleBuyNow() {
  if (!selectedSpecification.value.id) {
    showToast('请选择商品规格');
    return;
  }

  orderStore.setTradeGoods({
    origin: 'buy',
    list: [
      {
        goodsId: selectedSpecification.value.id,
        name: `${basicInfo.value.name} ${selectedSpecification.value.specification}`,
        number: 1,
        pic: basicInfo.value.pic,
        price: goodPrice.value,
        logisticsId: basicInfo.value.logisticsId,
        propertyList: [selectedSpecification.value],
      },
    ],
  });
}

// 收藏商品
function handleFavorite() {
  showToast('收藏功能已触发');
  // TODO: 实现收藏商品的后端API调用
}
</script>

<template>
  <div class="page-container">
    <!-- 统一导航栏 -->
    <Navbar :show-role-select="true" :is-omnipotent="isOmnipotent" />

    <!-- 主内容区域 - 使用表格布局确保左右布局 -->
    <main class="main-content">
      <!-- 骨架屏 -->
      <div v-if="loading" class="skeleton-container">
        <div class="skeleton-content">
          <table class="main-content-table" style="width: 100%; border-collapse: collapse">
            <tr>
              <!-- 左侧：商品图片骨架屏 -->
              <td style="width: 40%; padding: 10px; vertical-align: top">
                <div class="skeleton-image-container"></div>
              </td>
              <!-- 右侧：商品信息骨架屏 -->
              <td style="width: 60%; padding: 10px; vertical-align: top">
                <div class="skeleton-info">
                  <div class="skeleton-title"></div>
                  <div class="skeleton-rating"></div>
                  <div class="skeleton-price"></div>
                  <div class="skeleton-spec-section">
                    <div class="skeleton-spec-label"></div>
                    <div class="skeleton-spec-buttons">
                      <div class="skeleton-spec-button"></div>
                      <div class="skeleton-spec-button"></div>
                      <div class="skeleton-spec-button"></div>
                    </div>
                  </div>
                  <div class="skeleton-stock"></div>
                  <div class="skeleton-actions">
                    <div class="skeleton-quantity"></div>
                    <div class="skeleton-button"></div>
                  </div>
                </div>
              </td>
            </tr>
          </table>
        </div>
      </div>

      <!-- 实际内容 -->
      <div v-else class="content-wrapper">
        <table class="main-content-table" style="width: 100%; border-collapse: collapse">
          <tr>
            <!-- 左侧：商品图片（参考样式） -->
            <td style="width: 40%; padding: 10px; vertical-align: top">
              <div class="left-content">
                <!-- 商品图片（带左右切换按钮） -->
                <div class="product-image-container" style="position: relative; width: 100%">
                  <van-swipe :autoplay="3000" class="swiper">
                    <van-swipe-item v-for="item in picList" :key="item.id" class="swiper-item">
                      <van-image
                        class="swiper-item-img"
                        fit="contain"
                        :src="item.pic"
                        alt=""
                        lazy-load
                        placeholder="/src/assets/images/avatar_default.png"
                        :fade="true"
                        @load="handleImageLoad"
                        @error="handleImageError"
                      />
                    </van-swipe-item>
                  </van-swipe>
                  <!-- 左右切换按钮 -->
                  <div
                    class="image-nav-buttons"
                    style="
                      position: absolute;
                      top: 50%;
                      left: 0;
                      right: 0;
                      transform: translateY(-50%);
                      display: flex;
                      justify-content: space-between;
                      padding: 0 10px;
                    "
                  >
                    <button
                      class="nav-button"
                      style="
                        width: 30px;
                        height: 30px;
                        border-radius: 50%;
                        background-color: rgba(255, 255, 255, 0.8);
                        border: none;
                        cursor: pointer;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                      "
                    >
                      <span style="font-size: 16px; font-weight: bold">&lt;</span>
                    </button>
                    <button
                      class="nav-button"
                      style="
                        width: 30px;
                        height: 30px;
                        border-radius: 50%;
                        background-color: rgba(255, 255, 255, 0.8);
                        border: none;
                        cursor: pointer;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                      "
                    >
                      <span style="font-size: 16px; font-weight: bold">&gt;</span>
                    </button>
                  </div>
                </div>
              </div>
            </td>

            <!-- 右侧：商品信息（参考样式排布） -->
            <td style="width: 60%; padding: 10px; vertical-align: top">
              <div class="right-content">
                <!-- 商品名称 -->
                <div class="product-title">
                  {{ basicInfo.name }}
                </div>

                <!-- 评分 -->
                <div class="product-rating" style="margin-bottom: 15px">
                  <span class="stars" style="color: #ffd700; font-size: 16px"> ★★★★★ </span>
                  <span style="margin-left: 10px; color: #666"> (4.9) </span>
                </div>

                <!-- 价格 -->
                <div
                  class="product-price"
                  style="font-size: 22px !important; font-weight: bold; color: #ff4400; margin-bottom: 20px"
                >
                  ¥{{ goodPrice }}
                </div>

                <!-- 规格选择 -->
                <div class="specification-section" style="margin-bottom: 20px">
                  <!-- 尺寸选择 -->
                  <div class="specification-group" style="margin-bottom: 15px">
                    <div class="specification-label" style="font-size: 16px; font-weight: 600; margin-bottom: 10px">
                      选择规格
                    </div>
                    <div class="specification-buttons" style="display: flex; gap: 10px">
                      <div
                        v-for="spec in specifications"
                        :key="spec.id"
                        class="size-button"
                        :class="{ selected: selectedSpecification.id === spec.id }"
                        style="
                          padding: 8px 16px;
                          border: 1px solid #ddd;
                          border-radius: 4px;
                          cursor: pointer;
                          transition: all 0.3s ease;
                        "
                        :style="
                          selectedSpecification.id === spec.id
                            ? {
                                'background-color': 'var(--color-primary)',
                                color: 'white',
                                'border-color': 'var(--color-primary)',
                              }
                            : {}
                        "
                        @click="handleSpecificationChange(spec)"
                      >
                        {{ spec.specification.split(' ')[0] }}
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 库存提示 -->
                <div class="stock-alert" style="margin-bottom: 20px; color: #666; font-style: italic">
                  {{ goodStock > 0 ? `剩余 ${goodStock} 件，立即购买！` : '库存不足' }}
                </div>

                <!-- 操作按钮 -->
                <div class="action-buttons" style="display: flex; gap: 15px; margin-top: 30px; align-items: center">
                  <div
                    class="quantity-selector"
                    style="display: flex; align-items: center; border: 1px solid #ddd; border-radius: 4px"
                  >
                    <button
                      class="quantity-btn"
                      style="
                        width: 30px;
                        height: 30px;
                        border: none;
                        background-color: #f5f5f5;
                        cursor: pointer;
                        font-size: 16px;
                      "
                      @click="if (cartQuantity > 1) cartQuantity--;"
                    >
                      -
                    </button>
                    <input
                      v-model="cartQuantity"
                      type="text"
                      style="
                        width: 50px;
                        height: 30px;
                        text-align: center;
                        border: none;
                        border-left: 1px solid #ddd;
                        border-right: 1px solid #ddd;
                      "
                    />
                    <button
                      class="quantity-btn"
                      style="
                        width: 30px;
                        height: 30px;
                        border: none;
                        background-color: #f5f5f5;
                        cursor: pointer;
                        font-size: 16px;
                      "
                      @click="if (cartQuantity < goodStock) cartQuantity++;"
                    >
                      +
                    </button>
                  </div>
                  <van-button
                    class="add-cart-btn"
                    size="large"
                    type="primary"
                    style="flex: 1; padding: 10px 20px"
                    @click="confirmAddToCart"
                  >
                    加入购物车
                  </van-button>
                </div>
              </div>
            </td>
          </tr>
        </table>
      </div>

      <!-- 下半部分内容容器 -->
      <div v-if="!loading" class="bottom-content">
        <Coupons title="领券" />
        <van-cell>
          <template #title>
            <div class="cell-bar">
              <div class="cell-bar-title">服务</div>
              <div class="cell-bar-text">{{ afterSaleTitle }}</div>
            </div>
          </template>
        </van-cell>

        <Reputations v-if="basicInfo.id" class="mt10" :goods-id="basicInfo.id" />
        <Plate title="商品详情" class="mt10" />
        <div class="goods-content">
          <table class="goods-detail-table">
            <tbody>
              <tr>
                <th>商品名称</th>
                <td>{{ basicInfo.name || '' }}</td>
              </tr>
              <tr>
                <th>商品价格</th>
                <td>¥{{ goodPrice }} {{ priceUnit }}</td>
              </tr>
              <tr>
                <th>商品产地</th>
                <td>{{ basicInfo.origin || basicInfo.productionArea || '' }}</td>
              </tr>
              <tr>
                <th>商品店铺</th>
                <td>{{ basicInfo.shop || '' }}</td>
              </tr>
              <tr>
                <th>商品等级</th>
                <td>{{ basicInfo.grade || '' }}</td>
              </tr>
              <tr>
                <th>商品品种</th>
                <td>{{ basicInfo.variety || basicInfo.specie || '' }}</td>
              </tr>
              <tr>
                <th>商品规格</th>
                <td>{{ goodSpecificationDesc || '' }}</td>
              </tr>
              <tr>
                <th>运费信息</th>
                <td>{{ goodDeliveryTitle }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="action-bar-perch"></div>

      <!-- 商品导航栏 -->
    </main>
  </div>
</template>

<style lang="less" scoped>
/* 页面容器 */
.page-container {
  min-height: 100vh;
  background-color: #f5f5f5;

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

/* 主内容区域布局 */
.main-content {
  padding: 0 20px;

  .content-wrapper {
    margin-bottom: 20px;
    padding: 20px;
    background: var(--color-bg-2);
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    box-sizing: border-box;
  }

  .bottom-content {
    margin-bottom: 20px;
    padding: 20px;
    background: var(--color-bg-2);
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    box-sizing: border-box;
  }
}

/* 内容包装器 */
.content-wrapper {
  width: 100%;
  box-sizing: border-box;

  .left-content {
    flex: 1;
    min-width: 300px;
    max-width: 600px;

    @media (max-width: 1024px) {
      max-width: 45%;
    }

    @media (max-width: 768px) {
      max-width: 100%;
    }
  }

  .right-content {
    flex: 1;
    min-width: 350px;
    max-width: 500px;

    @media (max-width: 1024px) {
      max-width: 45%;
    }

    @media (max-width: 768px) {
      max-width: 100%;
    }
  }

  /* 商品标题 */
  .product-title {
    font-size: 20px !important;
    font-weight: 600;
    margin-bottom: 10px;
    line-height: 1.4;
  }
}

/* 商品基本信息样式 */
.product-info {
  margin-top: 20px;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.swiper {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;

  &-item,
  &-item-img {
    width: 100%;
    height: 100%;
  }
}

.section {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  padding: 0 15px 10px;
  background: var(--color-bg-2);
}

.price {
  margin-top: 10px;

  .price-current {
    display: flex;
    align-items: flex-end;
    align-items: center;
    margin-right: 8px;
    font-size: 16px;
    color: #ff4400;

    &-symbol {
      margin-right: 2px;
    }

    &-integer {
      font-size: 22px;
      font-weight: bold;
      font-family: @font-family-price-integer;
    }
  }

  &-origin {
    display: flex;
    align-items: flex-end;
    align-items: center;
    margin-right: 8px;
    line-height: 18px;
    color: var(--color-text-3);

    &-label {
      margin-right: 4px;
    }

    &-integer {
      text-decoration: line-through;
      font-family: @font-family-price-integer;
    }
  }

  &-tag {
    box-sizing: border-box;
    margin-left: 10px;
    display: inline-flex;
    font-size: 12px;
    padding: 2px 4px;
    border-radius: 4px;
    color: #f44;
    border: 1px solid #f44;
  }
}

.desc {
  margin-top: 10px;

  &-title {
    font-size: 16px;
    line-height: 20px;
  }

  &-brief {
    margin-top: 4px;
    color: var(--color-text-3);
    word-break: break-all;
  }
}

.stock {
  box-sizing: border-box;
  width: 100%;
  display: flex;
  justify-content: space-between;
  padding: 10px 15px;
  background: var(--color-bg-2);
  margin-bottom: 10px;

  &-item {
    flex: 1;
    color: var(--color-text-3);

    &:last-child {
      text-align: right;
    }
  }
}

.goods-content {
  box-sizing: border-box;
  background: var(--color-bg-2);
  padding: 0 10px;
  padding-top: 10px;
  overflow: hidden;
  color: var(--color-text-1);
  font-size: 16px !important;
  line-height: 1.5;
  text-align: left;
  word-wrap: break-word;

  :deep(i)mg {
    display: block;
    max-width: 100% !important;
    height: auto !important;
  }

  :deep(p) {
    margin: 0;
    padding: 0;
    font-size: 14px !important;
  }
}

/* 商品详情表格样式 */
.goods-detail-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  th,
  td {
    padding: 12px 16px;
    text-align: left;
    border-bottom: 1px solid #f0f0f0;
    font-size: 14px !important;
  }

  th {
    width: 120px;
    background-color: #f9f9f9;
    font-weight: 600;
    color: var(--color-text-2);
    font-size: 14px !important;
  }

  td {
    flex: 1;
    color: var(--color-text-1);
    font-size: 14px !important;
  }

  tr:last-child th,
  tr:last-child td {
    border-bottom: none;
  }
}

.mb10 {
  margin-bottom: 10px;
}

.mt10 {
  margin-top: 10px;
}

.cell-bar {
  display: flex;
  color: var(--color-text-1);

  &-title {
    flex-shrink: 0;
    margin-right: 12px;
    color: var(--color-text-3);
  }

  &-txt {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    text-align: left;
  }
}

.action-bar-perch {
  height: calc(50px + var(--safe-area-height-bottom));
}

/* 规格选择样式 */
.specification-section {
  margin: 20px 0;
  padding: 15px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.specification-label {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-1);
  margin-bottom: 15px;
}

.specification-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.specification-button {
  margin: 0;
  border-radius: 4px;
}

.specification-button.selected {
  background-color: var(--color-primary) !important;
  color: white !important;
  border-color: var(--color-primary) !important;
}

.specification-desc {
  color: var(--color-text-3);
  margin-top: 10px;
  padding-left: 2px;
}

/* 数量选择区域 */
.quantity-section {
  margin: 20px 0;
  padding: 15px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.quantity-label {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-1);
}

/* 总价显示区域 */
.total-section {
  margin: 20px 0;
  padding: 15px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.total-label {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-1);
}

.total-price {
  font-size: 20px;
  font-weight: 700;
  color: #ff4400;
}

/* 操作按钮区域 */
.action-buttons {
  margin: 20px 0;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.favorite-btn {
  flex: 1;
  min-width: 100px;
  background-color: #f5f5f5 !important;
  color: var(--color-text-1) !important;
  border-color: #e0e0e0 !important;
}

.add-cart-btn {
  flex: 2;
  min-width: 150px;
  background-color: var(--color-primary) !important;
  color: white !important;
  border-color: var(--color-primary) !important;
}

.buy-now-btn {
  flex: 2;
  min-width: 150px;
  background-color: #ff4400 !important;
  color: white !important;
  border-color: #ff4400 !important;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .action-buttons {
    flex-direction: column;
  }

  .action-buttons van-button {
    width: 100%;
  }
}

/* 骨架屏样式 */
.skeleton-container {
  width: 100%;
  margin-bottom: 20px;
  padding: 20px;
  background: var(--color-bg-2);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
}

.skeleton-content {
  width: 100%;
}

.skeleton-image-container {
  width: 100%;
  height: 400px;
  background-color: #f0f0f0;
  border-radius: 8px;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-info {
  width: 100%;
}

.skeleton-title {
  width: 80%;
  height: 24px;
  background-color: #f0f0f0;
  border-radius: 4px;
  margin-bottom: 16px;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-rating {
  width: 40%;
  height: 16px;
  background-color: #f0f0f0;
  border-radius: 4px;
  margin-bottom: 20px;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-price {
  width: 60%;
  height: 28px;
  background-color: #f0f0f0;
  border-radius: 4px;
  margin-bottom: 24px;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-spec-section {
  margin-bottom: 20px;
}

.skeleton-spec-label {
  width: 30%;
  height: 16px;
  background-color: #f0f0f0;
  border-radius: 4px;
  margin-bottom: 12px;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-spec-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.skeleton-spec-button {
  width: 80px;
  height: 32px;
  background-color: #f0f0f0;
  border-radius: 4px;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-stock {
  width: 50%;
  height: 14px;
  background-color: #f0f0f0;
  border-radius: 4px;
  margin-bottom: 24px;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.skeleton-quantity {
  width: 120px;
  height: 36px;
  background-color: #f0f0f0;
  border-radius: 4px;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-button {
  flex: 1;
  height: 44px;
  background-color: #f0f0f0;
  border-radius: 4px;
  animation: skeleton-loading 1.5s ease-in-out infinite;
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

/* 修复按钮样式 */
:deep(.van-button--small) {
  padding: 8px 16px;
  border-radius: 4px;
}

:deep(.van-button--plain) {
  border-color: var(--color-primary);
  background-color: rgba(var(--color-primary-rgb), 0.05);
  color: var(--color-primary);
  border-radius: 4px;
  transition: all 0.3s ease;
}

:deep(.van-button--default.van-button--plain) {
  border-color: var(--color-primary);
  background-color: rgba(var(--color-primary-rgb), 0.05);
  color: var(--color-primary);
}

:deep(.van-button--plain:hover) {
  background-color: rgba(var(--color-primary-rgb), 0.1);
  border-color: var(--color-primary);
}

:deep(.van-button--plain:active) {
  background-color: rgba(var(--color-primary-rgb), 0.15);
}

:deep(.van-button--primary) {
  border-radius: 4px;
  transition: all 0.3s ease;
}

:deep(.van-button--primary:hover) {
  background-color: rgba(var(--color-primary-rgb), 0.9);
  border-color: rgba(var(--color-primary-rgb), 0.9);
}

/* 购物车弹窗样式 */
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
  font-size: 16px;
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

  .dialog-good-spec {
    color: #999;
    margin-bottom: 8px;
  }
}

.dialog-good-price {
  font-size: 18px;
  font-weight: 600;
  color: #ff4400;
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
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.dialog-total-price {
  font-size: 20px;
  font-weight: 600;
  color: #ff4400;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-input-number__decrease),
:deep(.el-input-number__increase) {
  border-color: var(--color-primary);
}

:deep(.el-input-number__input) {
  border-color: var(--color-primary);
  color: #333;
}

/* 加入购物车按钮样式 */
:deep(.add-cart-btn) {
  background-color: rgba(var(--color-primary-rgb), 0.1) !important;
  color: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
}

:deep(.add-cart-btn:active) {
  background-color: rgba(var(--color-primary-rgb), 0.2) !important;
}

/* 立即购买按钮样式 */
:deep(.buy-now-btn) {
  background-color: var(--color-primary) !important;
  color: white !important;
  border-color: var(--color-primary) !important;
}

:deep(.buy-now-btn:active) {
  background-color: rgba(var(--color-primary-rgb), 0.9) !important;
}

/* 确认加入购物车按钮样式 */
:deep(.confirm-cart-btn) {
  background-color: var(--color-primary) !important;
  color: white !important;
  border-color: var(--color-primary) !important;
}

:deep(.confirm-cart-btn:hover) {
  background-color: rgba(var(--color-primary-rgb), 0.9) !important;
  border-color: rgba(var(--color-primary-rgb), 0.9) !important;
}
</style>
