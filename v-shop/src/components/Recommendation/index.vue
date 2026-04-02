<template>
  <div v-if="topRecommendations.length > 0" class="recommendation">
    <h3>推荐商品</h3>
    <div class="recommendation-carousel">
      <el-carousel :interval="10000" arrow="hover" height="450px">
        <el-carousel-item v-for="(item, index) in topRecommendations" :key="item.id">
          <div class="recommendation-item">
            <div class="content-wrapper">
              <table class="main-content-table" style="width: 100%; border-collapse: collapse">
                <tr>
                  <!-- 左侧：商品图片（参考样式） -->
                  <td style="width: 40%; padding: 10px; vertical-align: top">
                    <div class="left-content">
                      <!-- 商品图片（带左右切换按钮） -->
                      <div class="product-image-container" style="position: relative; width: 100%; cursor: pointer">
                        <van-swipe :autoplay="3000" class="swiper" :show-indicators="false">
                          <van-swipe-item
                            v-for="(pic, picIndex) in getPicList(item)"
                            :key="picIndex"
                            class="swiper-item"
                          >
                            <van-image
                              class="swiper-item-img"
                              fit="contain"
                              :src="pic.pic"
                              :alt="item.name"
                              lazy-load
                              placeholder="/src/assets/images/avatar_default.png"
                              :fade="true"
                              @load="handleImageLoad"
                              @error="handleImageError"
                              @click="goToDetail(item.id)"
                            />
                          </van-swipe-item>
                        </van-swipe>
                      </div>
                    </div>
                  </td>
                  <!-- 右侧：商品信息（参考样式排布） -->
                  <td style="width: 60%; padding: 10px; vertical-align: top">
                    <div class="right-content">
                      <!-- 商品名称 -->
                      <div class="product-title" @click="goToDetail(item.id)">
                        {{ item.name }}
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
                        ¥{{ getDisplayPrice(item) }}
                      </div>

                      <!-- 热度 -->
                      <div
                        class="product-heat"
                        style="margin-bottom: 20px; display: flex; align-items: center; gap: 8px"
                      >
                        <span style="font-size: 14px; color: #666">热度:</span>
                        <span style="font-size: 16px; color: #ff4400; font-weight: 600">{{ item.heat || '0' }}</span>
                      </div>

                      <!-- 规格选择 -->
                      <div class="specification-section" style="margin-bottom: 20px">
                        <!-- 尺寸选择 -->
                        <div class="specification-group" style="margin-bottom: 15px">
                          <div
                            class="specification-label"
                            style="font-size: 16px; font-weight: 600; margin-bottom: 10px"
                          >
                            选择规格
                          </div>
                          <div class="specification-buttons" style="display: flex; gap: 10px">
                            <div
                              v-for="spec in item.specifications || [
                                {
                                  id: item.id,
                                  specification: '默认规格',
                                  price: item.price,
                                  stock: item.stock,
                                },
                              ]"
                              :key="spec.id"
                              class="size-button"
                              :class="{ selected: selectedSpecifications[item.id]?.id === spec.id }"
                              style="
                                padding: 8px 16px;
                                border: 1px solid #ddd;
                                border-radius: 4px;
                                cursor: pointer;
                                transition: all 0.3s ease;
                              "
                              :style="
                                selectedSpecifications[item.id]?.id === spec.id
                                  ? {
                                      'background-color': 'var(--color-primary)',
                                      color: 'white',
                                      'border-color': 'var(--color-primary)',
                                    }
                                  : {}
                              "
                              @click="handleSpecificationChange(item, spec)"
                            >
                              {{ spec.specification.split(' ')[0] }}
                            </div>
                          </div>
                        </div>
                      </div>

                      <!-- 库存提示 -->
                      <div class="stock-alert" style="margin-bottom: 20px; color: #666; font-style: italic">
                        {{ getGoodStock(item) > 0 ? `剩余 ${getGoodStock(item)} 件，立即购买！` : '库存不足' }}
                      </div>

                      <!-- 操作按钮 -->
                      <div
                        class="action-buttons"
                        style="display: flex; gap: 15px; margin-top: 30px; align-items: center"
                      >
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
                            @click="if (quantities[item.id] > 1) quantities[item.id]--;"
                          >
                            -
                          </button>
                          <input
                            v-model="quantities[item.id]"
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
                            @click="if (quantities[item.id] < getGoodStock(item)) quantities[item.id]++;"
                          >
                            +
                          </button>
                        </div>
                        <van-button
                          class="add-cart-btn"
                          size="large"
                          type="primary"
                          style="flex: 1; padding: 10px 20px"
                          @click="addToCart(item)"
                        >
                          加入购物车
                        </van-button>
                      </div>
                    </div>
                  </td>
                </tr>
              </table>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { recommendationApi } from '@/apis/recommendation';
import { useUserStore } from '@/store/modules/user';
import API_CART from '@/apis/cart';
import { ElMessage } from 'element-plus';
import { showToast } from 'vant';
import { Swipe, SwipeItem, Image as VanImage, Button as VanButton } from 'vant';

const router = useRouter();
const userStore = useUserStore();
const recommendations = ref<any[]>([]);
const quantities = ref<Record<number, number>>({});
const selectedSpecifications = ref<Record<number, any>>({});

// 缓存相关配置
const CACHE_KEY_PREFIX = 'recommendations_';
const CACHE_EXPIRE_TIME = 300; // 缓存过期时间（秒）

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

// 获取前5个推荐商品
const topRecommendations = computed(() => {
  console.log('计算topRecommendations:', recommendations.value);
  const result = recommendations.value.slice(0, 5);
  console.log('topRecommendations结果:', result);
  console.log('topRecommendations长度:', result.length);
  return result;
});

// 跳转到商品详情页
function goToDetail(id: number) {
  console.log('goToDetail函数被调用，商品ID:', id);
  try {
    console.log('router实例:', router);
    // 使用 router.push 在当前窗口跳转，与商品列表保持一致
    router.push({ path: '/good/detail', query: { id } });
    console.log('路由跳转已执行');
  } catch (error) {
    console.error('跳转失败:', error);
  }
}

// 图片加载完成处理
function handleImageLoad() {
  // 图片加载完成后的处理
}

// 图片加载失败处理
function handleImageError(e: Event) {
  const target = e.target as HTMLImageElement;
  target.src = '/src/assets/images/avatar_default.png';
}

// 获取商品图片列表
function getPicList(item: any) {
  // 如果后端返回了pics数组，使用它
  if (item.pics && Array.isArray(item.pics) && item.pics.length > 0) {
    return item.pics;
  }
  // 如果后端返回了pic字段（单个图片URL），包装成数组
  if (item.pic) {
    return [
      {
        id: 1,
        pic: item.pic,
      },
    ];
  }
  // 如果有specs并且specs中有图片，使用第一个spec的图片
  if (item.specs && Array.isArray(item.specs) && item.specs.length > 0 && item.specs[0].pic) {
    return [
      {
        id: 1,
        pic: item.specs[0].pic,
      },
    ];
  }
  // 如果没有图片数据，返回默认图片
  return [
    {
      id: 1,
      pic: '/src/assets/images/avatar_default.png',
    },
  ];
}

// 获取商品规格列表
function getSpecifications(item: any) {
  // 模拟商品规格列表，与商品详情页结构保持一致
  if (item.specifications && Array.isArray(item.specifications)) {
    return item.specifications;
  }
  // 如果有specs字段，使用它
  if (item.specs && Array.isArray(item.specs)) {
    return item.specs.map((spec: any) => ({
      id: spec.id || item.id,
      specification: spec.specification || '默认规格',
      price: spec.price || item.minPrice || item.price || 0,
      stock: spec.stock || item.stock || 100,
    }));
  }
  // 如果没有规格数据，返回默认规格
  return [
    {
      id: item.id || 1,
      specification: '默认规格',
      price: item.minPrice || item.price || 0,
      stock: item.stock || 100,
    },
  ];
}

// 处理规格选择
function handleSpecificationChange(item: any, spec: any) {
  selectedSpecifications.value[item.id] = spec;
}

// 获取商品库存
function getGoodStock(item: any) {
  const selectedSpec = selectedSpecifications.value[item.id];
  if (selectedSpec) {
    return selectedSpec.stock || 0;
  }
  return item.stock || 100;
}

// 从价格字符串中提取数字
function extractPrice(priceStr: any): number {
  if (!priceStr) return 0;
  if (typeof priceStr === 'number') return priceStr;
  const match = String(priceStr).match(/(\d+\.\d+|\d+)/);
  return match ? Number(match[1]) : 0;
}

// 获取显示价格 - 与商品详情页保持一致
function getDisplayPrice(item: any) {
  const selectedSpec = selectedSpecifications.value[item.id];
  if (selectedSpec && selectedSpec.price !== undefined) {
    return extractPrice(selectedSpec.price).toFixed(2);
  }
  if (item.price !== undefined) {
    return extractPrice(item.price).toFixed(2);
  }
  if (item.minPrice !== undefined) {
    return extractPrice(item.minPrice).toFixed(2);
  }
  return '0.00';
}

// 添加到购物车
function addToCart(item: any) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    return;
  }

  const quantity = quantities.value[item.id] || 1;
  const selectedSpec = selectedSpecifications.value[item.id];
  const productCode = selectedSpec?.id || item.id;

  API_CART.cartAdd({
    userId: userStore.user?.userId || 1,
    productCode: parseInt(productCode),
    productNum: quantity,
    productId: item.id,
    productPrice: selectedSpec?.price || item.price,
  })
    .then((res) => {
      if (res.code === 0) {
        ElMessage.success(`已将${item.name}加入购物车`);
        quantities.value[item.id] = 1;
      } else {
        ElMessage.error(res.msg || '加入购物车失败');
      }
    })
    .catch((error) => {
      console.error('加入购物车失败:', error);
      ElMessage.error('加入购物车失败');
    });
}

// 预加载图片
function preloadImages(images: string[]) {
  images.forEach((imageUrl) => {
    if (imageUrl) {
      const img = new Image();
      img.src = imageUrl;
    }
  });
}

// 获取推荐商品
onMounted(async () => {
  try {
    console.log('开始获取推荐商品...');
    console.log('用户登录状态:', userStore.isLoggedIn);

    // 生成缓存键
    const cacheKey = CACHE_KEY_PREFIX + (userStore.isLoggedIn ? `user_${userStore.user?.userId || 0}` : 'popular');

    // 检查缓存
    const cachedData = getCacheData(cacheKey);
    if (cachedData) {
      console.log('使用缓存的推荐商品数据');
      recommendations.value = cachedData;

      // 预加载图片
      const imageUrls = recommendations.value.flatMap((item: any) => {
        const urls = [];
        if (item.pic) urls.push(item.pic);
        if (item.pics && Array.isArray(item.pics)) {
          item.pics.forEach((pic: any) => {
            if (pic.pic) urls.push(pic.pic);
          });
        }
        if (item.specs && Array.isArray(item.specs)) {
          item.specs.forEach((spec: any) => {
            if (spec.pic) urls.push(spec.pic);
          });
        }
        return urls;
      });
      preloadImages(imageUrls);
    } else {
      if (userStore.isLoggedIn) {
        console.log('获取用户推荐商品...');
        const response = await recommendationApi.getUserRecommendations(userStore.user?.userId || 0);
        console.log('用户推荐商品响应:', response);
        recommendations.value = response.data || [];
      } else {
        console.log('获取热门商品...');
        const response = await recommendationApi.getPopularItems();
        console.log('热门商品响应:', response);
        console.log('热门商品数据:', response.data);
        console.log('热门商品数量:', response.data ? response.data.length : 0);
        recommendations.value = response.data || [];
      }

      console.log('推荐商品数据:', recommendations.value);
      console.log('推荐商品数量:', recommendations.value.length);

      // 缓存数据
      setCacheData(cacheKey, recommendations.value);

      // 预加载图片
      const imageUrls = recommendations.value.flatMap((item: any) => {
        const urls = [];
        if (item.pic) urls.push(item.pic);
        if (item.pics && Array.isArray(item.pics)) {
          item.pics.forEach((pic: any) => {
            if (pic.pic) urls.push(pic.pic);
          });
        }
        if (item.specs && Array.isArray(item.specs)) {
          item.specs.forEach((spec: any) => {
            if (spec.pic) urls.push(spec.pic);
          });
        }
        return urls;
      });
      preloadImages(imageUrls);
    }

    // 初始化数量和规格
    recommendations.value.forEach((item) => {
      quantities.value[item.id] = 1;
      const specs = item.specifications ||
        item.specs || [
          { id: item.id, specification: '默认规格', price: item.price || item.minPrice, stock: item.stock || 100 },
        ];
      if (specs.length > 0) {
        selectedSpecifications.value[item.id] = specs[0];
      }
    });
  } catch (error) {
    console.error('获取推荐失败:', error);
  }
});
</script>

<style lang="less" scoped>
.recommendation {
  margin: 20px 0;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  h3 {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #333;
  }
}

.recommendation-carousel {
  width: 100%;
  height: 450px;
  overflow: hidden;
}

.recommendation-item {
  height: 100%;
  padding: 0 20px;
}

.content-wrapper {
  margin-bottom: 20px;
  padding: 20px;
  background: var(--color-bg-2);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
  height: 100%;
}

/* 左侧内容 */
.left-content {
  flex: 1;
  min-width: 300px;
  max-width: 600px;
}

/* 右侧内容 */
.right-content {
  flex: 1;
  min-width: 350px;
  max-width: 500px;
}

/* 商品图片样式 */
.product-image-container {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f8f8f8;
}

/* 轮播图样式 */
.swiper {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
}

.swiper-item {
  width: 100%;
  height: 100%;
}

.swiper-item-img {
  width: 100%;
  height: 100%;
  transition: opacity 0.3s ease;
}

/* 商品标题 */
.product-title {
  font-size: 20px !important;
  font-weight: 600;
  margin-bottom: 10px;
  line-height: 1.4;
  cursor: pointer;
}

.product-title:hover {
  color: var(--color-primary);
}

/* 评分 */
.product-rating {
  margin-bottom: 15px;
}

.stars {
  color: #ffd700;
  font-size: 16px;
}

.rating-text {
  margin-left: 10px;
  color: #666;
}

/* 价格 */
.product-price {
  font-size: 22px !important;
  font-weight: bold;
  color: #ff4400;
  margin-bottom: 20px;
}

/* 规格选择 */
.specification-section {
  margin-bottom: 20px;
}

.specification-label {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
}

.specification-buttons {
  display: flex;
  gap: 10px;
}

.size-button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;

  &.selected {
    background-color: var(--color-primary);
    color: white;
    border-color: var(--color-primary);
  }
}

/* 库存提示 */
.stock-alert {
  margin-bottom: 20px;
  color: #666;
  font-style: italic;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 15px;
  align-items: center;
  margin-top: 30px;
}

/* 数量选择器 */
.quantity-selector {
  display: flex;
  align-items: center;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.quantity-btn {
  width: 30px;
  height: 30px;
  border: none;
  background-color: #f5f5f5;
  cursor: pointer;
  font-size: 16px;
}

.quantity-selector input {
  width: 50px;
  height: 30px;
  text-align: center;
  border: none;
  border-left: 1px solid #ddd;
  border-right: 1px solid #ddd;
}

/* 加入购物车按钮 */
.add-cart-btn {
  flex: 1;
  padding: 10px 20px;
  background-color: var(--color-primary) !important;
  color: white !important;
  border-color: var(--color-primary) !important;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.add-cart-btn:hover {
  background-color: rgba(var(--color-primary-rgb), 0.9) !important;
  border-color: rgba(var(--color-primary-rgb), 0.9) !important;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .left-content,
  .right-content {
    max-width: 100%;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons van-button {
    width: 100%;
  }
}
</style>
