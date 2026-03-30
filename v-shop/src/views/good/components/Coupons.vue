<script setup lang="ts">
import { showToast, showLoadingToast, closeToast } from 'vant';
import { onMounted, ref, unref } from 'vue';
import API_DISCOUNTS from '@/apis/discounts';

defineProps({
  title: { type: String },
});

// 优惠券
onMounted(() => {
  getCouponList();
});

const show = ref(false);
const couponList = ref<Recordable[]>([]);

function getCouponList() {
  API_DISCOUNTS.discountsCoupons({ type: 'NO_PWD' })
    .then((res) => {
      if (res.data) {
        couponList.value = res.data;
      }
    })
    .catch((error) => {
      // 处理请求失败，显示暂无优惠券
      console.log('获取优惠券失败:', error);
      // 不做任何操作，默认不显示优惠券
      couponList.value = [];
    });
}

function onItemClicked(index: number) {
  const coupon = unref(couponList)[index];

  // if (coupon.pwd) {
  //   showToast({ message: '本券需要使用口令才能领取', duration: 1500 });
  //   return;
  // }

  showLoadingToast({
    forbidClick: true,
    message: '加载中...',
    duration: 0,
  });

  const params = {
    id: coupon.id,
  };

  API_DISCOUNTS.discountsFetch(params)
    .then(() => {
      closeToast();
      showToast({
        message: '恭喜,抢到了~',
        duration: 2000,
      });
    })
    .catch((err) => {
      console.error(err);
      closeToast();
      if (Number(err.code) === 700) {
        showToast({
          message: '很遗憾,没抢到~',
          duration: 2000,
        });
      } else {
        showToast({
          message: err.msg,
          duration: 2000,
        });
      }
    });
}

function close() {
  toggle(false);
}

function open() {
  toggle(true);
}

function toggle(value: boolean) {
  show.value = value;
}

defineExpose({
  open,
  close,
  toggle,
});
</script>

<template>
  <div class="coupons">
    <van-cell v-if="couponList.length" class="mb10" :title="title" is-link @click="open" />
    <!-- 弹层 -->
    <van-popup v-model:show="show" round closeable position="bottom">
      <div class="coupons-header van-hairline--bottom">优惠券</div>
      <div class="coupons-body">
        <div class="coupon-list">
          <div v-for="(item, index) in couponList" :key="item.id" class="coupon-list-item">
            <div class="coupon-list-item-content">
              <div class="coupon-list-item-hd">
                <div class="coupon-list-item-money"><span class="fz12">¥</span>{{ item.moneyMin }}</div>
                <div class="coupon-list-item-moneyHreshold">满{{ item.moneyHreshold }}元可用</div>
              </div>
              <div class="coupon-list-item-bd">
                <div class="coupon-list-item-name">{{ item.moneyMin }}元券</div>
                <div v-if="item.dateEndType === 0" class="coupon-list-item-dateEndDays">
                  领取后 {{ item.dateEnd.slice(0, 10) }} 到期
                </div>
                <div v-if="item.dateEndType === 1" class="coupon-list-item-dateEndDays">
                  领取后 {{ item.dateEndDays }} 天后到期
                </div>
              </div>
            </div>
            <div
              class="coupon-list-item-btn"
              style="display: block; margin: 0 auto; text-align: center; margin-top: 8px"
              @click="onItemClicked(index)"
            >
              立即领取
            </div>
          </div>
        </div>
      </div>
      <div class="coupons-footer" @click="close">
        <van-button type="primary" round block @click="close">完成</van-button>
      </div>
    </van-popup>
  </div>
</template>

<style lang="less" scoped>
.coupons {
  &-header {
    box-sizing: border-box;
    text-align: center;
    padding: 0 15px;
    width: 100%;
    font-size: 16px;
    color: var(--color-text-1);
    height: 50px;
    line-height: 50px;
  }

  &-body {
    // padding: 10px 15px;
  }

  &-footer {
    box-sizing: border-box;
    display: flex;
    padding: 8px 15px;
    width: 100%;
  }
}

.coupon-list {
  padding: 10px 15px;
  max-height: 70vh;
  overflow-y: auto;
  &-header {
    position: absolute;
    box-sizing: border-box;
    width: 100%;
    top: 0;
    left: 0;
    font-size: 14px;
    color: #333;
    padding: 10px 0;
    text-align: center;
  }
}

/* 优惠券项容器 */
.coupon-list-item {
  box-sizing: border-box;
  min-height: 80px;
  position: relative;
  background: #ffeeee;
  color: #ff4b52;
  margin-bottom: 10px;
  border-radius: 10px;
  padding: 10px 15px;
  display: flex;
  flex-direction: column;
}

/* 优惠券内容区域 */
.coupon-list-item-content {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

/* 优惠券头部（金额部分） */
.coupon-list-item-hd {
  width: 100px;
  text-align: center;
  border-right: 1px solid #eee;
}

/* 金额 */
.coupon-list-item-money {
  font-size: 24px;
  font-weight: bold;
}

/* 使用门槛 */
.coupon-list-item-moneyHreshold {
  font-size: 12px;
}

/* 优惠券主体内容 */
.coupon-list-item-bd {
  flex: 1;
  padding-left: 15px;
}

/* 优惠券名称 */
.coupon-list-item-name {
  font-size: 14px;
  padding: 5px 0;
}

/* 到期时间 */
.coupon-list-item-dateEndDays {
  font-size: 12px;
  color: #d7a0a5;
}

/* 立即领取按钮 */
.coupon-list-item-btn {
  display: block;
  margin: 0 auto;
  padding: 6px 20px;
  color: #fff;
  background: #ff4444;
  font-size: 12px;
  line-height: normal;
  border-radius: 12px;
  text-align: center;
  margin-top: 5px;
}

/* 已领取按钮 */
.coupon-list-item-btn-clicked {
  color: #ff4444;
  background: #ffffff;
  border: 1px solid #ff4444;
  pointer-events: none;
}

.mb10 {
  margin-bottom: 10px;
}

.fz12 {
  font-size: 12px;
}
</style>
