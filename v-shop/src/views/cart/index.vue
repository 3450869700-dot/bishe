<script lang="ts">
export default {
  name: 'Cart',
};
</script>

<script setup lang="ts">
import NP from 'number-precision';
import { showConfirmDialog, showToast } from 'vant';
import { useDebounceFn } from '@vueuse/core';
import { computed, onMounted, ref, unref } from 'vue';
import { useRouter } from 'vue-router';
import { decimalFormat } from '@/utils/format';
import API_CART from '@/apis/cart';
// store
import { useOrderStore } from '@/store/modules/order';
import { useUserStore } from '@/store/modules/user';
// hooks
import { usePage } from '@/hooks/shared/usePage';
// components
import Navbar from '@/components/Navbar/index.vue';
// assets
import IMAGE_LIST_EMPTY from '@/assets/images/empty/cart.png';

const router = useRouter();
const orderStore = useOrderStore();
const userStore = useUserStore();
const { isOmnipotent, goHome } = usePage();

// ==================== 状态管理 ====================
const list = ref<Recordable[]>([]);
const listLoading = ref(true);
const editStatus = ref(1);
const freight = ref(0);
const discountAmount = ref(0);
const selectedCoupon = ref<Recordable | null>(null);

// ==================== 计算属性 ====================
const selectedList = computed(() => unref(list).filter((v) => v.selected));

const totalGoodCount = computed(() => unref(selectedList).reduce((acc, cur) => NP.plus(acc, cur.number || 0), 0));

const totalPrice = computed(() =>
  unref(selectedList).reduce((acc, cur) => NP.plus(acc, NP.times(cur.price || 0, cur.number || 0)), 0),
);

const actualPrice = computed(() => NP.minus(NP.plus(unref(totalPrice), unref(freight)), unref(discountAmount)));

const selectedAll = computed({
  get() {
    return unref(list).length > 0 && unref(selectedList).length === unref(list).length;
  },
  set(val) {
    unref(list).forEach((v) => (v.selected = val));
  },
});

// ==================== 数据获取 ====================
async function getList() {
  listLoading.value = true;
  try {
    // 从用户状态中获取真实的用户ID
    const userInfo = userStore.getUserInfo;
    const userId = userInfo?.user_id || userInfo?.id || 1;
    console.log('购物车 - 当前用户ID:', userId);

    const res = await API_CART.cartListByUserId(userId);
    if (res.code === 0 && res.data?.length > 0) {
      list.value = res.data
        .filter((item: any) => item.isValid === 1)
        .map((item: any) => ({
          key: item.cartId.toString(),
          id: parseInt(item.productCode),
          name: item.productName || `商品 ${item.productCode}`,
          pic: item.productImage || '',
          price: item.productPrice || 0,
          number: item.productNum > 0 ? item.productNum : 1,
          selected: item.isChecked === 1,
          stock: 100,
          spec: item.productSpec || '',
        }));
    } else {
      list.value = [];
    }
  } catch (error) {
    console.error('获取购物车列表失败:', error);
    list.value = [];
  } finally {
    listLoading.value = false;
  }
}

// ==================== 事件处理 ====================
function onEditStatusChange() {
  editStatus.value = unref(editStatus) === 1 ? 2 : 1;
}

const onGoodChange = useDebounceFn((number: number, index: number) => {
  const item = list.value[index];
  const oldNumber = item.number;
  item.number = number;

  API_CART.cartUpdate(parseInt(item.key), { productNum: number }).catch(() => {
    item.number = oldNumber;
    showToast({ message: '修改数量失败', duration: 1500 });
  });
}, 1000);

function onDelete() {
  if (!unref(selectedList).length) {
    showToast({ message: '您还没有选择商品哦', duration: 1500 });
    return;
  }

  const isEmpty = unref(selectedList).length === unref(list).length;
  const message = isEmpty ? '确定要清空购物车吗？' : `确定要删除这${unref(selectedList).length}个商品吗？`;

  showConfirmDialog({ message })
    .then(() => {
      if (isEmpty) {
        cartEmptyHandle();
      } else {
        cartRemoveHandle();
      }
    })
    .catch(() => {});
}

function cartEmptyHandle() {
  unref(list).forEach((item: any) => API_CART.cartDelete(parseInt(item.key)));
  list.value = [];
  showToast({ message: '购物车已清空', duration: 1500 });
}

function cartRemoveHandle() {
  const selectedKeys = unref(selectedList).map((v: any) => parseInt(v.key));
  Promise.all(selectedKeys.map((key: number) => API_CART.cartDelete(key)))
    .then(() => {
      getList();
      showToast({ message: '已删除选中商品', duration: 1500 });
    })
    .catch(() => {
      list.value = list.value.filter((item: any) => !item.selected);
      showToast({ message: '已删除选中商品', duration: 1500 });
    });
}

function onDeleteItem(item: any) {
  showConfirmDialog({ message: `确定要删除${item.name}吗？` })
    .then(() => {
      API_CART.cartDelete(parseInt(item.key))
        .then(() => getList())
        .catch(() => {
          list.value = list.value.filter((v: any) => v.key !== item.key);
        });
      showToast({ message: '商品已删除', duration: 1500 });
    })
    .catch(() => {});
}

function onSubmit() {
  if (!unref(selectedList).length) {
    showToast({ message: '您还没有选择商品哦', duration: 1500 });
    return;
  }

  if (unref(selectedList).some((v) => (v.stock || 0) <= 0)) {
    showToast({ message: '请删除库存不足的商品', duration: 1500 });
    return;
  }

  orderStore.setTradeGoods({ origin: 'cart', list: unref(selectedList) });
  router.push('/order/submit');
}

function onGoDetail(id: number) {
  router.push(`/good/detail?id=${id}`);
}

function onGoShopping() {
  router.push('/category');
}

// ==================== 生命周期 ====================
onMounted(() => {
  getList();
});
</script>

<template>
  <div class="page-container">
    <Navbar :show-role-select="true" :is-omnipotent="isOmnipotent" />

    <main class="main-content">
      <div class="cart-container">
        <div class="cart-header">
          <h2>购物车</h2>
          <el-button link class="edit-btn" @click="onEditStatusChange">
            {{ editStatus === 1 ? '编辑' : '完成' }}
          </el-button>
        </div>

        <!-- 加载状态 -->
        <div v-if="listLoading" class="loading-container">
          <el-skeleton :rows="6" animated style="width: 100%" />
        </div>

        <!-- 购物车列表 -->
        <template v-else-if="list.length">
          <div class="select-all-section">
            <el-checkbox v-model="selectedAll" class="select-all-checkbox">全选</el-checkbox>
            <span v-if="editStatus === 1" class="goods-count">共{{ list.length }}件商品</span>
          </div>

          <div class="goods-list">
            <div v-for="(item, index) in list" :key="item.key" class="goods-item">
              <div class="item-checkbox">
                <el-checkbox v-model="item.selected" />
              </div>
              <el-image fit="contain" class="item-image" :src="item.pic" @click="onGoDetail(item.id)" />
              <div class="item-info">
                <div class="item-title">
                  <span v-if="(item.stock || 0) <= 0" class="stock-warning">【库存不足】</span>
                  {{ item.name }}
                </div>
                <div class="item-sku">
                  <span v-if="item.spec">{{ item.spec }}</span>
                </div>
                <div v-if="item.stock && item.stock < 10 && item.stock > 0" class="item-stock">
                  仅剩{{ item.stock }}件
                </div>
                <div class="item-bottom">
                  <div class="item-price">
                    <span class="price-symbol">¥</span>
                    <span class="price-value">{{ decimalFormat(item.price) }}</span>
                  </div>
                  <div v-if="editStatus === 2" class="item-actions">
                    <el-button type="danger" size="small" text @click="onDeleteItem(item)">删除</el-button>
                  </div>
                  <template v-else>
                    <el-input-number
                      v-model="item.number"
                      :min="1"
                      :max="item.stock || 999"
                      :step="1"
                      class="sku-num-stepper"
                      @change="onGoodChange(item.number, index)"
                    />
                  </template>
                </div>
                <div v-if="editStatus === 1" class="item-subtotal">
                  小计: <span class="subtotal-price">¥{{ decimalFormat(NP.times(item.price, item.number)) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 价格计算 -->
          <div class="price-calculation">
            <div class="price-row">
              <span class="price-label">商品总价</span>
              <span class="price-value">¥{{ decimalFormat(totalPrice) }}</span>
            </div>
            <div class="price-row">
              <span class="price-label">运费</span>
              <span class="price-value">
                <template v-if="totalPrice >= 99">免运费</template>
                <template v-else>¥{{ freight }}</template>
              </span>
            </div>
            <div v-if="discountAmount > 0" class="price-row discount">
              <span class="price-label">优惠券抵扣</span>
              <span class="price-value">-¥{{ decimalFormat(discountAmount) }}</span>
            </div>
            <div class="price-row total">
              <span class="price-label">实付金额</span>
              <span class="price-value actual">¥{{ decimalFormat(actualPrice) }}</span>
            </div>
          </div>

          <!-- 提交栏 -->
          <div class="submit-bar">
            <div class="submit-info">
              <span class="total-count">共{{ totalGoodCount }}件商品</span>
              <span class="total-amount"
                >合计: <span class="amount">¥{{ decimalFormat(actualPrice) }}</span></span
              >
            </div>
            <el-button type="primary" class="submit-btn" :disabled="!selectedList.length" @click="onSubmit">
              去结算
            </el-button>
          </div>
        </template>

        <!-- 空购物车 -->
        <div v-else class="empty-cart">
          <van-empty :image="IMAGE_LIST_EMPTY" description="购物车快饿瘪了 T.T">
            <div class="empty-tips">购物车空空如也，快去挑选商品吧</div>
            <el-button type="primary" class="go-shopping-btn" @click="onGoShopping">去逛逛</el-button>
          </van-empty>
        </div>
      </div>
    </main>
  </div>
</template>

<style lang="less">
// 导入公共样式
@import '@/styles/common.less';
</style>

<style lang="less" scoped>
// 使用主题色变量
@theme-color: var(--color-primary, #ff6b6b);
@theme-color-light: var(--color-primary-light, #ff8e8e);
@theme-color-dark: var(--color-primary-dark, #ff4848);

// 购物车页面特定的内容区样式（只添加额外的padding，不覆盖max-width）
:deep(.main-content) {
  padding-top: 60px !important;
  padding-bottom: 100px !important;
}

.cart-container {
  width: 100%;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  h2 {
    margin: 0;
    font-size: 20px;
    color: #333;
  }

  // 编辑按钮使用主题色
  .edit-btn {
    color: @theme-color;

    &:hover {
      color: @theme-color-dark;
    }
  }
}

.loading-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.select-all-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  background: #fff;
  border-radius: 8px 8px 0 0;
  border-bottom: 1px solid #f0f0f0;
}

// 全选复选框样式
.select-all-checkbox {
  :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    background-color: @theme-color;
    border-color: @theme-color;
  }

  :deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) {
    background-color: @theme-color;
    border-color: @theme-color;
  }

  :deep(.el-checkbox__inner:hover) {
    border-color: @theme-color;
  }

  :deep(.el-checkbox__input.is-focus .el-checkbox__inner) {
    border-color: @theme-color;
  }

  :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
    color: @theme-color;
  }

  :deep(.el-checkbox__label) {
    color: #333;
  }
}

.goods-count {
  color: #999;
}

.goods-list {
  background: #fff;
  padding: 0 20px;
}

.goods-item {
  display: flex;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.item-checkbox {
  margin-right: 15px;

  // 复选框使用主题色
  :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    background-color: @theme-color;
    border-color: @theme-color;
  }

  :deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) {
    background-color: @theme-color;
    border-color: @theme-color;
  }

  :deep(.el-checkbox__inner:hover) {
    border-color: @theme-color;
  }

  :deep(.el-checkbox__input.is-focus .el-checkbox__inner) {
    border-color: @theme-color;
  }

  :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
    color: @theme-color;
  }

  :deep(.el-checkbox) {
    --el-checkbox-checked-text-color: @theme-color;
    --el-checkbox-checked-input-border-color: @theme-color;
    --el-checkbox-checked-bg-color: @theme-color;
  }
}

.item-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  margin-right: 15px;
  cursor: pointer;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 16px !important;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.5;

  .stock-warning {
    color: @theme-color;
  }
}

.item-sku {
  color: #999;
  margin-bottom: 8px;
}

.item-stock {
  color: @theme-color;
  margin-bottom: 8px;
}

.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-price {
  color: @theme-color;
  font-weight: 600;

  .price-value {
    font-size: 18px;
  }
}

.item-subtotal {
  margin-top: 8px;
  color: #666;

  .subtotal-price {
    color: @theme-color;
    font-weight: 600;
  }
}

// 输入框数字调节按钮使用主题色
:deep(.el-input-number__decrease),
:deep(.el-input-number__increase) {
  &:hover {
    color: @theme-color;
  }

  &:hover:not(.is-disabled) ~ .el-input .el-input__inner:not(.is-disabled) {
    border-color: @theme-color;
  }
}

:deep(.el-input-number__increase) {
  &:hover:not(.is-disabled) {
    color: @theme-color;
  }
}

:deep(.el-input__inner) {
  &:focus {
    border-color: @theme-color;
  }
}

.price-calculation {
  background: #fff;
  padding: 20px;
  margin-top: 20px;
  border-radius: 8px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;

  &.total {
    margin-top: 15px;
    padding-top: 15px;
    border-top: 1px solid #f0f0f0;
    font-weight: 600;

    .actual {
      color: @theme-color;
      font-size: 20px;
    }
  }
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 15px 20px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
}

.submit-info {
  display: flex;
  flex-direction: column;

  .total-count {
    color: #999;
    margin-bottom: 4px;
  }

  .total-amount {
    color: #333;

    .amount {
      color: @theme-color;
      font-size: 20px;
      font-weight: 600;
    }
  }
}

// 提交按钮使用主题色
:deep(.el-button--primary) {
  background-color: @theme-color;
  border-color: @theme-color;

  &:hover,
  &:focus {
    background-color: @theme-color-light;
    border-color: @theme-color-light;
  }

  &:active {
    background-color: @theme-color-dark;
    border-color: @theme-color-dark;
  }

  &.is-disabled {
    background-color: @theme-color-light;
    border-color: @theme-color-light;
    opacity: 0.6;
  }
}

// 删除按钮使用主题色
:deep(.el-button--danger) {
  color: @theme-color;

  &:hover,
  &:focus {
    color: @theme-color-dark;
  }
}

.empty-cart {
  padding: 60px 20px;
  text-align: center;
  background: #fff;
  border-radius: 8px;
}

.empty-tips {
  color: #999;
  margin: 20px 0;
}

.go-shopping-btn {
  width: 200px;
}
</style>
