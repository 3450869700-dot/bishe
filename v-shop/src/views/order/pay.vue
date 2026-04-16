<script lang="ts">
export default {
  name: 'OrderPay',
};
</script>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { onMounted, ref } from 'vue';
import { showToast, showLoadingToast, closeToast } from 'vant';
import API_ORDER from '@/apis/order';
import { decimalFormat } from '@/utils/format';

const route = useRoute();
const router = useRouter();

const orderInfo = ref({
  orderNumber: '',
  amountReal: 0,
});

onMounted(() => {
  // 从路由参数获取订单信息
  const { orderNumber, amountReal } = route.query;
  if (orderNumber && amountReal) {
    orderInfo.value.orderNumber = orderNumber as string;
    orderInfo.value.amountReal = parseFloat(amountReal as string);
  } else {
    showToast({ message: '订单信息错误', duration: 1500 });
    router.back();
  }
});

function handlePaySuccess() {
  showLoadingToast({
    forbidClick: true,
    message: '支付处理中...',
    duration: 0,
  });

  // 调用支付接口
  API_ORDER.orderPay({
    orderNumber: orderInfo.value.orderNumber,
  })
    .then(() => {
      closeToast();
      showToast({ message: '支付成功', duration: 1500 });
      // 跳转到待发货页面（订单详情页）
      setTimeout(() => {
        router.push({
          path: '/order/detail',
          query: { orderNumber: orderInfo.value.orderNumber },
        });
      }, 1500);
    })
    .catch((err) => {
      closeToast();
      showToast({ message: '支付失败，请重试', duration: 1500 });
      console.error(err);
    });
}
</script>

<template>
  <div class="container">
    <div class="pay-card">
      <div class="pay-header">
        <h2>模拟支付</h2>
      </div>
      <div class="pay-body">
        <div class="pay-info">
          <div class="pay-info-item">
            <span class="pay-info-label">订单编号：</span>
            <span class="pay-info-value">{{ orderInfo.orderNumber }}</span>
          </div>
          <div class="pay-info-item">
            <span class="pay-info-label">支付金额：</span>
            <span class="pay-info-value pay-amount">¥ {{ decimalFormat(orderInfo.amountReal) }}</span>
          </div>
        </div>
        <div class="pay-actions">
          <van-button class="pay-button" round type="primary" @click="handlePaySuccess"> 模拟支付成功 </van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.container {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.pay-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.pay-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  text-align: center;
  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: bold;
    color: #333;
  }
}

.pay-body {
  padding: 24px;
}

.pay-info {
  margin-bottom: 32px;
}

.pay-info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  &:last-child {
    border-bottom: none;
  }
}

.pay-info-label {
  font-size: 14px;
  color: #666;
}

.pay-info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.pay-amount {
  font-size: 24px;
  color: #ff4d4f;
  font-weight: bold;
}

.pay-actions {
  margin-top: 32px;
}

.pay-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
}
</style>
