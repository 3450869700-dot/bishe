<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router';
import { onMounted, reactive, ref, unref, computed } from 'vue';
import { showToast, showConfirmDialog } from 'vant';
import API_DISCOUNTS from '@/apis/discounts';
// hooks
import { usePage } from '@/hooks/shared/usePage';
// Element Plus
import { ShoppingCart, User, Ticket, Star } from '@element-plus/icons-vue';
// components
import Navbar from '@/components/Navbar/index.vue';
import ExchangeCoupon from './components/ExchangeCoupon/index.vue';

const router = useRouter();
const route = useRoute();
const activeIndex = ref(route.path);

const { isOmnipotent } = usePage();

const activeTab = ref(0);

// 页面初始化时自动加载数据
onMounted(() => {
  getDataList();
});

const tabList = ref<Recordable[]>([
  { name: '待领取', status: -1, type: 'unreceived' },
  { name: '可用优惠券', status: 0, type: 'available' },
  { name: '已使用', status: 3, type: 'used' },
  { name: '已过期', status: 2, type: 'expired' },
]);

const filterType = ref('all');
const filterOptions = ref([
  { label: '全部面额', value: 'all' },
  { label: '满100可用', value: '100' },
  { label: '满200可用', value: '200' },
  { label: '满500可用', value: '500' },
]);

const list = ref<Recordable[]>([]);
const unreceivedList = ref<Recordable[]>([]);
const listLoading = ref(true);
const exchangeShow = ref(false);
const showCouponDetail = ref(false);
const currentCoupon = ref<Recordable | null>(null);
const receiveLimit = ref({ perPerson: 3, perDay: 10 });
const receivedCount = ref(5);

const filteredList = computed(() => {
  if (filterType.value === 'all') return list.value;
  return list.value.filter((item) => item.moneyHreshold >= parseInt(filterType.value));
});

const availableCount = computed(() => list.value.filter((item) => item.status === 0).length);
const usedCount = computed(() => list.value.filter((item) => item.status === 3).length);
const expiredCount = computed(() => list.value.filter((item) => item.status === 2).length);

// 添加防抖函数，避免频繁切换标签时的重复请求
function debounce(func, wait) {
  let timeout;
  return function () {
    clearTimeout(timeout);
    timeout = setTimeout(() => {
      func.apply(this, arguments);
    }, wait);
  };
}

// 使用防抖处理标签切换，提高流畅度
const onTabChange = debounce(() => {
  getDataList();
}, 100);

function onFilterChange() {
  listLoading.value = true;
  setTimeout(() => {
    listLoading.value = false;
  }, 300);
}

// 状态值映射：将数据库中的状态值映射到前端使用的状态值
function mapStatus(status) {
  // 数据库中use_status=1表示未使用
  if (status === 1) return 0; // 前端使用0表示可用
  return status;
}

// 数据转换：将后端返回的UserCoupon对象转换为前端期望的格式
function transformUserCouponData(userCoupons) {
  return userCoupons.map((item) => {
    // 尝试从未领取列表中查找对应的优惠券信息
    const couponInfo = unreceivedList.value.find((coupon) => coupon.id === item.couponId);

    return {
      id: item.userCouponId,
      name: couponInfo ? couponInfo.coupon_name : `优惠券 ${item.couponId}`, // 使用真实的优惠券名称
      money: couponInfo ? (couponInfo.coupon_type === 3 ? couponInfo.face_value : couponInfo.face_value) : 20, // 使用真实的金额或默认值
      moneyHreshold: couponInfo ? couponInfo.min_amount : 100, // 使用真实的使用门槛或默认值
      status: mapStatus(item.useStatus),
      useRange: couponInfo
        ? couponInfo.product_scope === 1
          ? '全平台'
          : couponInfo.product_scope === 2
          ? '水蜜桃品类专享'
          : '指定商品'
        : '全场通用', // 使用真实的使用范围
      description: '优惠券描述',
      canReceive: false,
      received: true,
      useTime: item.useTime,
      orderId: item.orderId,
      dateStart: item.receiveTime,
      dateEnd: couponInfo ? couponInfo.end_time : item.receiveTime, // 使用真实的过期时间
    };
  });
}

// 数据转换：将后端返回的Coupon对象转换为前端期望的格式
function transformCouponData(coupons) {
  return coupons.map((item) => ({
    id: item.couponId,
    coupon_name: item.couponName,
    coupon_type: item.couponType,
    face_value: item.faceValue,
    min_amount: item.minAmount,
    product_scope: item.productScope,
    start_time: item.startTime,
    end_time: item.endTime,
    total_quantity: item.totalQuantity,
    remain_quantity: item.remainQuantity,
    user_limit: item.userLimit,
    is_valid: item.isValid,
  }));
}

function getDataList() {
  // 立即显示加载状态，提供即时反馈
  listLoading.value = true;

  const currentTab = unref(tabList)[unref(activeTab)];

  // 使用requestAnimationFrame确保DOM更新在浏览器重绘前完成，提高流畅度
  requestAnimationFrame(() => {
    if (currentTab.type === 'unreceived') {
      // 待领取优惠券，从coupon表获取
      getUnreceivedCoupons();
    } else {
      // 其他状态优惠券，从user_coupon表获取
      // 注意：数据库中use_status=1表示未使用，需要映射到前端的状态值
      const dbStatus = currentTab.status === 0 ? 1 : currentTab.status;

      API_DISCOUNTS.discountsMy({ status: dbStatus })
        .then((res) => {
          // 使用requestAnimationFrame确保数据更新在浏览器重绘前完成
          requestAnimationFrame(() => {
            // 转换后端返回的数据格式
            list.value = transformUserCouponData(res.data || []);
          });
        })
        .catch(() => {
          // 错误时使用空数组
          requestAnimationFrame(() => {
            list.value = [];
          });
        })
        .finally(() => {
          // 延迟一点时间再隐藏加载状态，避免闪烁
          setTimeout(() => {
            listLoading.value = false;
          }, 100);
        });
    }
  });
}

function getUnreceivedCoupons() {
  // 从后端API获取待领取优惠券
  API_DISCOUNTS.discountsCoupons()
    .then((res) => {
      // 使用requestAnimationFrame确保DOM更新在浏览器重绘前完成
      requestAnimationFrame(() => {
        // 转换后端返回的数据格式
        unreceivedList.value = transformCouponData(res.data || []);
      });
    })
    .catch(() => {
      // 错误时使用空数组
      requestAnimationFrame(() => {
        unreceivedList.value = [];
      });
    })
    .finally(() => {
      // 延迟一点时间再隐藏加载状态，避免闪烁
      setTimeout(() => {
        listLoading.value = false;
      }, 100);
    });
}

// 移除模拟数据函数，使用真实的后端API数据
// function getMockData() {
//   const baseCoupons = [
//     {
//       id: 1,
//       name: '新人专享券',
//       money: 20,
//       moneyHreshold: 100,
//       dateStart: '2026-01-01',
//       dateEnd: '2026-12-31',
//       status: 0,
//       useRange: '全场通用',
//       description: '适用于平台所有商品',
//       canReceive: true,
//       received: false,
//     },
//     {
//       id: 2,
//       name: '满减优惠券',
//       money: 50,
//       moneyHreshold: 500,
//       dateStart: '2026-01-01',
//       dateEnd: '2026-06-30',
//       status: 0,
//       useRange: '指定品类',
//       description: '适用于服饰、鞋包类商品',
//       canReceive: true,
//       received: true,
//     },
//     {
//       id: 3,
//       name: '限时特惠券',
//       money: 100,
//       moneyHreshold: 1000,
//       dateStart: '2026-01-01',
//       dateEnd: '2026-03-31',
//       status: 0,
//       useRange: '特定商品',
//       description: '仅限指定活动商品使用',
//       canReceive: false,
//       received: true,
//     },
//     {
//       id: 4,
//       name: '会员专属券',
//       money: 30,
//       moneyHreshold: 200,
//       dateStart: '2025-12-01',
//       dateEnd: '2025-12-31',
//       status: 3,
//       useRange: '全场通用',
//       description: '适用于平台所有商品',
//       canReceive: false,
//       received: true,
//       usedTime: '2025-12-15 14:30:25',
//       orderId: 'Order202512150001',
//     },
//     {
//       id: 5,
//       name: '生日礼券',
//       money: 100,
//       moneyHreshold: 500,
//       dateStart: '2025-06-01',
//       dateEnd: '2025-06-30',
//       status: 2,
//       useRange: '全场通用',
//       description: '适用于平台所有商品',
//       canReceive: false,
//       received: true,
//     },
//   ];

//   if (unref(activeTab) === 0) {
//     return baseCoupons.filter((c) => c.status === 0);
//   } else if (unref(activeTab) === 1) {
//     return baseCoupons.filter((c) => c.status === 3);
//   } else {
//     return baseCoupons.filter((c) => c.status === 2);
//   }
// }

function dateFormat(val: string) {
  if (!val) return '';
  return val.slice(0, 10);
}

function thresholdTitle(val: number) {
  if (val === 0) return '满任意金额可用';
  return `满${val}元可用`;
}

function onReceiveCoupon(coupon: Recordable) {
  if (coupon.received) {
    showToast({ message: '您已领取过该优惠券', duration: 1500 });
    return;
  }

  if (receivedCount.value >= receiveLimit.value.perPerson) {
    showToast({ message: '您已达到领取上限', duration: 1500 });
    return;
  }

  // 处理待领取优惠券（来自coupon表）
  if (coupon.coupon_name) {
    showConfirmDialog({
      message: `确定领取「${coupon.coupon_name}」吗？\n${
        coupon.coupon_type === 3 ? `折扣：${(coupon.face_value * 10).toFixed(1)}折` : `面额：¥${coupon.face_value}`
      }\n使用门槛：${coupon.min_amount > 0 ? `满${coupon.min_amount}可用` : '无门槛'}`,
    })
      .then(() => {
        // 从待领取列表中移除
        const index = unreceivedList.value.findIndex((item) => item.id === coupon.id);
        if (index > -1) {
          unreceivedList.value.splice(index, 1);
        }
        receivedCount.value++;
        showToast({ message: '领取成功', duration: 1500 });
      })
      .catch(() => {});
  } else {
    // 处理其他状态优惠券（来自user_coupon表）
    showConfirmDialog({
      message: `确定领取「${coupon.name}」吗？\n面额：¥${coupon.money}\n使用门槛：${thresholdTitle(
        coupon.moneyHreshold,
      )}`,
    })
      .then(() => {
        coupon.received = true;
        receivedCount.value++;
        showToast({ message: '领取成功', duration: 1500 });
      })
      .catch(() => {});
  }
}

function onUseCoupon(coupon: Recordable) {
  router.push('/category');
}

function onShowDetail(coupon: Recordable) {
  // 根据优惠券类型处理数据，确保弹窗显示正确的字段
  let processedCoupon = { ...coupon };

  // 如果是待领取的优惠券（来自coupon表），转换字段名
  if (coupon.coupon_name) {
    processedCoupon = {
      ...coupon,
      money: coupon.coupon_type === 3 ? coupon.face_value : coupon.face_value,
      moneyHreshold: coupon.min_amount,
      name: coupon.coupon_name,
      useRange: coupon.product_scope === 1 ? '全平台' : coupon.product_scope === 2 ? '水蜜桃品类专享' : '指定商品',
      description: '优惠券描述',
      status: 0,
      received: false,
      canReceive: true,
      dateStart: coupon.start_time,
      dateEnd: coupon.end_time,
    };
  }

  currentCoupon.value = processedCoupon;
  showCouponDetail.value = true;
}

function onExchangeSuccess() {
  exchangeShow.value = false;
  if (unref(activeTab) === 0) {
    getDataList();
  }
}

function onGoShopping() {
  router.push('/category');
}
</script>

<template>
  <div class="page-container">
    <!-- 统一导航栏 -->
    <Navbar :show-role-select="true" :is-omnipotent="isOmnipotent" />

    <main class="main-content">
      <div class="coupon-container">
        <div class="page-header">
          <h2>我的优惠券</h2>
          <el-button type="primary" size="small" class="exchange-btn" @click="exchangeShow = true"
            >兑换优惠券</el-button
          >
        </div>

        <div class="tab-section">
          <van-tabs v-model:active="activeTab" @change="onTabChange">
            <van-tab>
              <template #title>
                <span class="tab-title">待领取</span>
              </template>
            </van-tab>
            <van-tab>
              <template #title>
                <span class="tab-title">可用</span>
                <span v-if="availableCount > 0" class="tab-badge">{{ availableCount }}</span>
              </template>
            </van-tab>
            <van-tab>
              <template #title>
                <span class="tab-title">已使用</span>
                <span v-if="usedCount > 0" class="tab-badge used">{{ usedCount }}</span>
              </template>
            </van-tab>
            <van-tab>
              <template #title>
                <span class="tab-title">已过期</span>
                <span v-if="expiredCount > 0" class="tab-badge expired">{{ expiredCount }}</span>
              </template>
            </van-tab>
          </van-tabs>
          <div v-if="activeTab === 0" class="filter-section">
            <el-select v-model="filterType" placeholder="筛选" size="small" @change="onFilterChange">
              <el-option v-for="item in filterOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </div>
        </div>

        <div v-if="listLoading" class="coupon-list">
          <div v-for="i in 3" :key="i" class="loading-skeleton">
            <el-skeleton :rows="2" animated />
          </div>
        </div>

        <template v-else>
          <!-- 待领取优惠券 -->
          <template v-if="unref(tabList)[unref(activeTab)].type === 'unreceived'">
            <template v-if="unreceivedList.length">
              <div class="coupon-cards">
                <div v-for="item in unreceivedList" :key="item.id" class="coupon-card" @click="onShowDetail(item)">
                  <div class="coupon-left">
                    <div class="coupon-amount">
                      <span class="symbol">{{ item.coupon_type === 3 ? '折' : '¥' }}</span>
                      <span class="value">{{
                        item.coupon_type === 3 ? (item.face_value * 10).toFixed(1) : item.face_value
                      }}</span>
                    </div>
                    <div class="coupon-threshold">
                      {{ item.min_amount > 0 ? `满${item.min_amount}可用` : '无门槛' }}
                    </div>
                  </div>
                  <div class="coupon-divider"></div>
                  <div class="coupon-right">
                    <div class="coupon-name">{{ item.coupon_name }}</div>
                    <div class="coupon-range">
                      {{
                        item.product_scope === 1 ? '全平台' : item.product_scope === 2 ? '水蜜桃品类专享' : '指定商品'
                      }}
                    </div>
                    <div class="coupon-date">{{ dateFormat(item.start_time) }} - {{ dateFormat(item.end_time) }}</div>
                    <div class="coupon-quantity">
                      剩余: {{ item.remain_quantity }} / {{ item.total_quantity === 0 ? '不限' : item.total_quantity }}
                    </div>
                    <div class="coupon-action">
                      <el-button type="primary" size="small" @click.stop="onReceiveCoupon(item)"> 立即领取 </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </template>
            <div v-else class="empty-section">
              <van-empty description="暂无待领取优惠券">
                <div class="empty-tips">敬请期待更多优惠</div>
              </van-empty>
            </div>
          </template>

          <!-- 其他状态优惠券 -->
          <template v-else>
            <template v-if="filteredList.length">
              <div class="coupon-cards">
                <div
                  v-for="item in filteredList"
                  :key="item.id"
                  class="coupon-card"
                  :class="{ used: item.status === 3, expired: item.status === 2 }"
                  @click="onShowDetail(item)"
                >
                  <div class="coupon-left">
                    <div class="coupon-amount">
                      <span class="symbol">¥</span>
                      <span class="value">{{ item.money }}</span>
                    </div>
                    <div class="coupon-threshold">{{ thresholdTitle(item.moneyHreshold) }}</div>
                  </div>
                  <div class="coupon-divider"></div>
                  <div class="coupon-right">
                    <div class="coupon-name">{{ item.name }}</div>
                    <div class="coupon-range">{{ item.useRange }}</div>
                    <div class="coupon-date">{{ dateFormat(item.dateStart) }} - {{ dateFormat(item.dateEnd) }}</div>
                    <div class="coupon-action">
                      <template v-if="item.status === 0">
                        <el-button
                          v-if="item.canReceive && !item.received"
                          type="primary"
                          size="small"
                          @click.stop="onReceiveCoupon(item)"
                        >
                          立即领取
                        </el-button>
                        <el-button
                          v-else-if="item.received"
                          type="success"
                          size="small"
                          plain
                          @click.stop="onUseCoupon(item)"
                        >
                          去使用
                        </el-button>
                        <el-button v-else type="info" size="small" plain disabled> 已领取 </el-button>
                      </template>
                      <template v-else-if="item.status === 3">
                        <el-tag type="info" size="small">已使用</el-tag>
                      </template>
                      <template v-else>
                        <el-tag type="danger" size="small">已过期</el-tag>
                      </template>
                    </div>
                  </div>
                  <div v-if="item.status === 3" class="coupon-stamp">
                    <span>已使用</span>
                  </div>
                  <div v-if="item.status === 2" class="coupon-stamp expired">
                    <span>已过期</span>
                  </div>
                </div>
              </div>
            </template>
            <div v-else class="empty-section">
              <van-empty description="暂无优惠券">
                <div class="empty-tips">快去领取优惠券吧</div>
                <el-button type="primary" class="go-shopping-btn" @click="onGoShopping">去逛逛</el-button>
              </van-empty>
            </div>
          </template>
        </template>

        <div v-if="activeTab === 0" class="coupon-tips">
          <div class="tips-title">领取说明</div>
          <div class="tips-content">
            <p>• 每种优惠券每人限领 {{ receiveLimit.perPerson }} 张</p>
            <p>• 每日限领 {{ receiveLimit.perDay }} 张优惠券</p>
            <p>• 已领取 {{ receivedCount }} 张</p>
            <p>• 优惠券有效期以券面信息为准</p>
          </div>
        </div>

        <div v-if="activeTab === 1 && list.length > 0" class="usage-records">
          <div class="section-title">使用记录</div>
          <div class="record-list">
            <div v-for="item in list" :key="item.id" class="record-item">
              <div class="record-info">
                <span class="record-name">{{ item.name }}</span>
                <span class="record-amount">-¥{{ item.money }}</span>
              </div>
              <div class="record-detail">
                <span class="record-time">{{ item.usedTime }}</span>
                <span class="record-order">订单号：{{ item.orderId }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <van-popup v-model:show="showCouponDetail" round position="bottom" style="height: 60%">
      <div v-if="currentCoupon" class="coupon-detail-popup">
        <div class="detail-header">
          <h3>优惠券详情</h3>
          <van-icon name="close" size="24" @click="showCouponDetail = false" />
        </div>
        <div class="detail-content">
          <div class="detail-coupon">
            <div class="coupon-left">
              <div class="coupon-amount">
                <span class="symbol">{{ currentCoupon.coupon_type === 3 ? '折' : '¥' }}</span>
                <span class="value">{{
                  currentCoupon.coupon_type === 3
                    ? (currentCoupon.face_value * 10).toFixed(1)
                    : currentCoupon.money || currentCoupon.face_value
                }}</span>
              </div>
              <div class="coupon-threshold">
                {{
                  currentCoupon.min_amount
                    ? currentCoupon.min_amount > 0
                      ? `满${currentCoupon.min_amount}可用`
                      : '无门槛'
                    : thresholdTitle(currentCoupon.moneyHreshold)
                }}
              </div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ currentCoupon.name || currentCoupon.coupon_name }}</div>
              <div class="coupon-range">
                {{
                  currentCoupon.useRange ||
                  (currentCoupon.product_scope === 1
                    ? '全平台'
                    : currentCoupon.product_scope === 2
                    ? '水蜜桃品类专享'
                    : '指定商品')
                }}
              </div>
            </div>
          </div>
          <div class="detail-info">
            <div class="info-row">
              <span class="label">使用范围</span>
              <span class="value">{{
                currentCoupon.useRange ||
                (currentCoupon.product_scope === 1
                  ? '全平台'
                  : currentCoupon.product_scope === 2
                  ? '水蜜桃品类专享'
                  : '指定商品')
              }}</span>
            </div>
            <div class="info-row">
              <span class="label">有效期</span>
              <span class="value">
                {{ dateFormat(currentCoupon.dateStart || currentCoupon.start_time) }} 至
                {{ dateFormat(currentCoupon.dateEnd || currentCoupon.end_time) }}
              </span>
            </div>
            <div class="info-row">
              <span class="label">使用规则</span>
              <span class="value">{{ currentCoupon.description || '优惠券描述' }}</span>
            </div>
            <div v-if="currentCoupon.status === 0" class="info-row">
              <span class="label">领取状态</span>
              <span class="value">{{ currentCoupon.received ? '已领取' : '未领取' }}</span>
            </div>
            <div v-if="currentCoupon.status === 3" class="info-row">
              <span class="label">使用时间</span>
              <span class="value">{{ currentCoupon.usedTime }}</span>
            </div>
            <div v-if="currentCoupon.status === 3" class="info-row">
              <span class="label">使用订单</span>
              <span class="value">{{ currentCoupon.orderId }}</span>
            </div>
          </div>
        </div>
        <div v-if="currentCoupon.status === 0 && currentCoupon.canReceive" class="detail-footer">
          <el-button type="primary" block :disabled="currentCoupon.received" @click="onReceiveCoupon(currentCoupon)">
            {{ currentCoupon.received ? '已领取' : '立即领取' }}
          </el-button>
        </div>
      </div>
    </van-popup>

    <ExchangeCoupon v-model:show="exchangeShow" @success="onExchangeSuccess" />
  </div>
</template>

<style lang="less" scoped>
/* 添加全局过渡效果，使页面切换更加流畅 */
.page-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 为优惠券卡片添加过渡效果 */
.coupon-cards {
  transition: opacity 0.3s ease-in-out, transform 0.3s ease-in-out;
}

/* 为内容区域添加过渡效果 */
.main-content {
  transition: all 0.3s ease-in-out;
}

/* 为加载状态添加过渡效果 */
.loading-skeleton {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 为标签栏添加过渡效果 */
.tab-section {
  transition: all 0.3s ease-in-out;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

.coupon-container {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    color: #333;
  }
}

.tab-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #eaeaea;

  :deep(.van-tabs) {
    flex: 1;
  }

  :deep(.van-tabs__wrap) {
    border-bottom: none;
  }

  .tab-title {
    margin-right: 6px;
  }

  .tab-badge {
    background-color: var(--color-primary);
    color: #fff;
    font-size: 12px;
    padding: 2px 6px;
    border-radius: 10px;

    &.used {
      background-color: #909399;
    }

    &.expired {
      background-color: #ff0000; /* 已过期标签旁边的数量提示使用红色 */
    }
  }

  .filter-section {
    margin-left: 20px;
  }
}

.coupon-list {
  .loading-skeleton {
    margin-bottom: 15px;
  }
}

.coupon-cards {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.coupon-card {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #fff;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(238, 90, 36, 0.3);
  }

  &.used {
    opacity: 0.7;
    cursor: default;

    &:hover {
      transform: none;
      box-shadow: none;
    }
  }

  &.expired {
    opacity: 0.5;
    cursor: default;

    &:hover {
      transform: none;
      box-shadow: none;
    }
  }
}

.coupon-left {
  width: 35%;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
  background-image: url('@/assets/images/coupon.png');
  background-size: cover;
  background-position: center;
}

.coupon-amount {
  display: flex;
  align-items: baseline;

  .symbol {
    font-size: 16px;
    font-weight: 500;
    color: #ff0000; /* 改为红色字体 */
  }

  .value {
    font-size: 36px;
    font-weight: 700;
    color: #ff0000; /* 改为红色字体 */
  }
}

.coupon-threshold {
  font-size: 12px;
  margin-top: 4px;
  opacity: 0.9;
  color: #ff0000; /* 改为红色字体 */
}

.coupon-divider {
  width: 2px;
  background: repeating-linear-gradient(0deg, #fff 0, #fff 8px, transparent 8px, transparent 16px);
}

.coupon-right {
  flex: 1;
  padding: 15px 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background-color: #fff;
}

.coupon-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.coupon-range {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}

.coupon-date {
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
}

.coupon-quantity {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}

.coupon-action {
  margin-top: auto;
}

.coupon-stamp {
  position: absolute;
  right: 40px;
  top: 50%;
  transform: translateY(-50%) rotate(-15deg);
  border: 3px solid #909399;
  color: #909399;
  font-size: 16px;
  font-weight: 700;
  padding: 8px 16px;
  border-radius: 8px;
  opacity: 0.6;

  &.expired {
    border-color: #d9d9d9;
    color: #d9d9d9;
  }
}

.empty-section {
  padding: 60px 20px;

  .empty-tips {
    font-size: 14px;
    color: #999;
    margin: 10px 0 20px;
  }
}

.coupon-tips {
  margin-top: 30px;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;

  .tips-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 15px;
  }

  .tips-content {
    font-size: 14px;
    color: #666;
    line-height: 2;

    p {
      margin: 0;
    }
  }
}

.usage-records {
  margin-top: 30px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 1px solid #eaeaea;
  }

  .record-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .record-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    background-color: #fafafa;
    border-radius: 8px;
  }

  .record-info {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .record-name {
      font-size: 14px;
      color: #333;
    }

    .record-amount {
      font-size: 16px;
      font-weight: 600;
      color: #ff4400; /* 记录金额使用橙红色，参考淘宝价格颜色 */
    }
  }

  .record-detail {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 4px;

    .record-time {
      font-size: 12px;
      color: #999;
    }

    .record-order {
      font-size: 12px;
      color: #666;
    }
  }
}

.coupon-detail-popup {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      margin: 0;
      font-size: 18px;
      color: #333;
    }
  }

  .detail-content {
    flex: 1;
    overflow-y: auto;
  }

  .detail-coupon {
    display: flex;
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 20px;
    background-color: #fff;

    .coupon-left {
      color: #fff;
      background-image: url('@/assets/images/coupon.png');
      background-size: cover;
      background-position: center;

      .coupon-amount {
        .symbol,
        .value {
          color: #ff0000; /* 改为红色字体 */
        }
      }

      .coupon-threshold {
        color: #ff0000; /* 改为红色字体 */
      }
    }
  }

  .detail-info {
    .info-row {
      display: flex;
      padding: 12px 0;
      border-bottom: 1px solid #eaeaea;

      .label {
        width: 80px;
        font-size: 14px;
        color: #999;
        flex-shrink: 0;
      }

      .value {
        flex: 1;
        font-size: 14px;
        color: #333;
      }
    }
  }

  .detail-footer {
    padding-top: 20px;
    border-top: 1px solid #eaeaea;
  }
}
</style>
