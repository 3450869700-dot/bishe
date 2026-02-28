<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router';
import { onMounted, ref, unref, computed } from 'vue';
import { showToast, showConfirmDialog, Dialog } from 'vant';
// hooks
import { usePage } from '@/hooks/shared/usePage';
// Element Plus
import { ShoppingCart, User, Ticket, Star } from '@element-plus/icons-vue';
// components
import Navbar from '@/components/Navbar/index.vue';

const router = useRouter();
const route = useRoute();
const activeIndex = ref(route.path);

const { isOmnipotent } = usePage();

const currentPoints = ref(12800);
const totalPoints = ref(15600);
const integralList = ref<Recordable[]>([
  { id: 1, type: 'earn', change: 100, desc: '每日签到', time: '2026-01-08 09:00:00', balance: 12800 },
  { id: 2, type: 'earn', change: 500, desc: '订单返积分', time: '2026-01-07 16:30:00', balance: 12700 },
  { id: 3, type: 'spend', change: -500, desc: '兑换商品', time: '2026-01-06 14:20:00', balance: 12200 },
  { id: 4, type: 'earn', change: 200, desc: '评价奖励', time: '2026-01-05 11:15:00', balance: 12700 },
  { id: 5, type: 'spend', change: -200, desc: '兑换优惠券', time: '2026-01-04 09:45:00', balance: 12500 },
]);

const activeCategory = ref('all');
const sortType = ref('default');
const activeTab = ref(0);

const categoryList = ref([
  { label: '全部', value: 'all' },
  { label: '实物商品', value: 'physical' },
  { label: '优惠券', value: 'coupon' },
  { label: '虚拟商品', value: 'virtual' },
]);

const sortOptions = ref([
  { label: '默认排序', value: 'default' },
  { label: '积分从低到高', value: 'points_asc' },
  { label: '积分从高到低', value: 'points_desc' },
  { label: '兑换热门', value: 'hot' },
  { label: '最新上架', value: 'new' },
]);

const productList = ref<Recordable[]>([
  {
    id: 1,
    name: '精美水杯 500ml',
    pic: '',
    points: 500,
    marketPrice: 49.9,
    stock: 100,
    category: 'physical',
    sales: 256,
    description: '304不锈钢材质，保温效果长达12小时',
    specs: ['500ml', '600ml', '750ml'],
  },
  {
    id: 2,
    name: '20元优惠券',
    pic: '',
    points: 200,
    marketPrice: 20,
    stock: 500,
    category: 'coupon',
    sales: 1234,
    description: '满100元可用，有效期30天',
    validDays: 30,
    minAmount: 100,
  },
  {
    id: 3,
    name: '视频会员月卡',
    pic: '',
    points: 800,
    marketPrice: 25,
    stock: 200,
    category: 'virtual',
    sales: 567,
    description: '各大视频平台会员月卡',
    validDays: 30,
  },
  {
    id: 4,
    name: '品牌帆布袋',
    pic: '',
    points: 300,
    marketPrice: 39.9,
    stock: 80,
    category: 'physical',
    sales: 189,
    description: '简约时尚帆布袋',
    specs: ['单肩', '斜挎'],
  },
  {
    id: 5,
    name: '50元优惠券',
    pic: '',
    points: 500,
    marketPrice: 50,
    stock: 300,
    category: 'coupon',
    sales: 890,
    description: '满200元可用，有效期30天',
    validDays: 30,
    minAmount: 200,
  },
  {
    id: 6,
    name: '游戏点券100点',
    pic: '',
    points: 1000,
    marketPrice: 10,
    stock: 1000,
    category: 'virtual',
    sales: 2345,
    description: '适用主流游戏平台',
    validDays: 90,
  },
  {
    id: 7,
    name: '迷你充电宝',
    pic: '',
    points: 1200,
    marketPrice: 79.9,
    stock: 50,
    category: 'physical',
    sales: 145,
    description: '10000mAh轻薄便携',
    specs: ['白色', '黑色', '粉色'],
  },
  {
    id: 8,
    name: '100元优惠券',
    pic: '',
    points: 1000,
    marketPrice: 100,
    stock: 200,
    category: 'coupon',
    sales: 678,
    description: '满500元可用，有效期30天',
    validDays: 30,
    minAmount: 500,
  },
]);

const exchangeRecord = ref<Recordable[]>([
  {
    id: 1,
    name: '20元优惠券',
    points: 200,
    time: '2026-01-06 14:20:00',
    status: 'completed',
    type: 'coupon',
  },
  {
    id: 2,
    name: '精美水杯',
    points: 500,
    time: '2026-01-03 10:30:00',
    status: 'shipping',
    logistics: '顺丰快递 SF1234567890',
    address: '北京市朝阳区xxx街道xxx号',
    type: 'physical',
  },
  {
    id: 3,
    name: '视频会员月卡',
    points: 800,
    time: '2026-01-01 16:00:00',
    status: 'completed',
    code: 'VIP12345678',
    type: 'virtual',
  },
]);

const taskList = ref([
  { id: 1, name: '每日签到', desc: '每天签到获得积分', points: 100, progress: 1, maxProgress: 1, completed: true },
  { id: 2, name: '首单返积分', desc: '首次下单返积分', points: 500, progress: 0, maxProgress: 1, completed: false },
  { id: 3, name: '评价返积分', desc: '发表评价返积分', points: 200, progress: 3, maxProgress: 5, completed: false },
  { id: 4, name: '分享商品', desc: '分享商品到社交平台', points: 50, progress: 2, maxProgress: 5, completed: false },
  { id: 5, name: '完善信息', desc: '完善个人资料', points: 100, progress: 1, maxProgress: 1, completed: true },
]);

const filteredProducts = computed(() => {
  let result = productList.value;

  if (activeCategory.value !== 'all') {
    result = result.filter((item) => item.category === activeCategory.value);
  }

  switch (sortType.value) {
    case 'points_asc':
      result = [...result].sort((a, b) => a.points - b.points);
      break;
    case 'points_desc':
      result = [...result].sort((a, b) => b.points - a.points);
      break;
    case 'hot':
      result = [...result].sort((a, b) => b.sales - a.sales);
      break;
    case 'new':
      result = [...result].sort((a, b) => b.id - a.id);
      break;
  }

  return result;
});

const canExchange = computed(() => {
  return (points: number) => currentPoints.value >= points;
});

function onCategoryChange(category: string) {
  activeCategory.value = category;
}

function onSortChange() {
  sortType.value;
}

function onSignIn() {
  const todayTask = taskList.value.find((t) => t.name === '每日签到');
  if (todayTask && todayTask.completed) {
    showToast({ message: '今日已签到，明天再来吧', duration: 1500 });
    return;
  }

  currentPoints.value += 100;
  todayTask.completed = true;
  todayTask.progress = 1;
  showToast({ message: '签到成功，获得100积分', duration: 1500 });
}

function onReceiveTask(task: Recordable) {
  if (task.completed) {
    showToast({ message: '任务已完成', duration: 1500 });
    return;
  }

  if (task.progress >= task.maxProgress) {
    currentPoints.value += task.points;
    task.completed = true;
    showToast({ message: `领取成功，获得${task.points}积分`, duration: 1500 });
  } else {
    showToast({ message: '任务未完成', duration: 1500 });
  }
}

function onExchange(product: Recordable) {
  if (!canExchange.value(product.points)) {
    showToast({ message: '积分不足', duration: 1500 });
    return;
  }

  showConfirmDialog({
    title: '确认兑换',
    message: `确定用${product.points}积分兑换「${product.name}」吗？\n当前积分：${currentPoints.value}\n兑换后积分：${
      currentPoints.value - product.points
    }`,
  })
    .then(() => {
      currentPoints.value -= product.points;

      const newRecord = {
        id: Date.now(),
        name: product.name,
        points: product.points,
        time: new Date().toLocaleString(),
        status: product.category === 'virtual' ? 'completed' : 'pending',
        type: product.category,
      };

      if (product.category === 'virtual') {
        newRecord.code = 'CODE' + Math.random().toString(36).substr(2, 10).toUpperCase();
      }

      exchangeRecord.value.unshift(newRecord);

      product.stock--;

      showToast({ message: '兑换成功', duration: 1500 });
    })
    .catch(() => {});
}

function onShowDetail(product: Recordable) {
  router.push(`/integral/detail?id=${product.id}`);
}

function onViewRecord() {
  activeTab.value = 2;
}

function onViewTasks() {
  activeTab.value = 3;
}

function onViewDetails() {
  router.push('/integral/details');
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
      <div class="integral-container">
        <div class="points-header">
          <div class="points-info">
            <div class="points-label">当前积分</div>
            <div class="points-value">{{ currentPoints }}</div>
            <div class="points-total">累计获得 {{ totalPoints }} 积分</div>
          </div>
          <div class="points-actions">
            <el-button type="primary" :disabled="taskList[0]?.completed" @click="onSignIn">
              {{ taskList[0]?.completed ? '已签到' : '立即签到' }}
            </el-button>
            <el-button type="warning" text @click="onViewDetails">积分明细</el-button>
          </div>
        </div>

        <div class="tab-section">
          <van-tabs v-model:active="activeTab">
            <van-tab title="商品兑换" />
            <van-tab title="兑换记录" />
            <van-tab title="积分任务" />
          </van-tabs>
        </div>

        <div v-if="activeTab === 0" class="tab-content">
          <div class="category-bar">
            <div
              v-for="cat in categoryList"
              :key="cat.value"
              class="category-item"
              :class="{ active: activeCategory === cat.value }"
              @click="onCategoryChange(cat.value)"
            >
              {{ cat.label }}
            </div>
          </div>

          <div class="sort-bar">
            <el-select v-model="sortType" placeholder="排序" size="small">
              <el-option v-for="opt in sortOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </div>

          <div class="product-grid">
            <div
              v-for="product in filteredProducts"
              :key="product.id"
              class="product-card"
              @click="onShowDetail(product)"
            >
              <div class="product-image">
                <van-image width="100%" height="140" fit="cover" src="" />
                <div class="stock-badge" :class="{ low: product.stock < 10 }">
                  {{ product.stock > 0 ? `剩${product.stock}` : '已兑完' }}
                </div>
              </div>
              <div class="product-info">
                <div class="product-name">{{ product.name }}</div>
                <div class="product-points">
                  <span class="points-icon">★</span>
                  <span class="points-value">{{ product.points }}</span>
                  <span class="points-unit">积分</span>
                </div>
                <div class="product-market">市场价 ¥{{ product.marketPrice }}</div>
                <el-button
                  type="primary"
                  size="small"
                  :disabled="product.stock <= 0 || !canExchange(product.points)"
                  @click.stop="onExchange(product)"
                >
                  {{ product.stock <= 0 ? '已兑完' : !canExchange(product.points) ? '积分不足' : '立即兑换' }}
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 1" class="tab-content">
          <div v-if="exchangeRecord.length" class="record-list">
            <div v-for="record in exchangeRecord" :key="record.id" class="record-card">
              <div class="record-header">
                <span class="record-name">{{ record.name }}</span>
                <el-tag :type="record.status === 'completed' ? 'success' : 'warning'" size="small">
                  {{ record.status === 'completed' ? '已完成' : record.status === 'shipping' ? '配送中' : '处理中' }}
                </el-tag>
              </div>
              <div class="record-info">
                <span class="record-points">-{{ record.points }}积分</span>
                <span class="record-time">{{ record.time }}</span>
              </div>
              <div v-if="record.type === 'virtual' && record.code" class="record-detail">
                <span class="detail-label">兑换码：</span>
                <span class="detail-value">{{ record.code }}</span>
                <el-button type="primary" size="small" text>复制</el-button>
              </div>
              <div v-if="record.type === 'physical' && record.logistics" class="record-detail">
                <span class="detail-label">物流：</span>
                <span class="detail-value">{{ record.logistics }}</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-section">
            <van-empty description="暂无兑换记录">
              <div class="empty-tips">快去兑换喜欢的商品吧</div>
              <el-button type="primary" @click="activeTab = 0">去兑换</el-button>
            </van-empty>
          </div>
        </div>

        <div v-if="activeTab === 2" class="tab-content">
          <div class="task-list">
            <div v-for="task in taskList" :key="task.id" class="task-card">
              <div class="task-info">
                <div class="task-name">{{ task.name }}</div>
                <div class="task-desc">{{ task.desc }}</div>
              </div>
              <div class="task-progress">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: (task.progress / task.maxProgress) * 100 + '%' }"></div>
                </div>
                <div class="progress-text">
                  <span>{{ task.progress }}/{{ task.maxProgress }}</span>
                  <span class="task-points">+{{ task.points }}积分</span>
                </div>
              </div>
              <div class="task-action">
                <el-button type="primary" size="small" :disabled="task.completed" @click="onReceiveTask(task)">
                  {{ task.completed ? '已完成' : task.progress >= task.maxProgress ? '领取奖励' : '去完成' }}
                </el-button>
              </div>
            </div>
          </div>

          <div class="rules-section">
            <div class="rules-title">积分规则</div>
            <div class="rules-content">
              <p>• 积分可用于兑换商城商品、优惠券等</p>
              <p>• 积分不能兑现、转让或叠加到其他账户</p>
              <p>• 订单完成后，积分将在7天内到账</p>
              <p>• 签到获得的积分每日上限100分</p>
              <p>• 积分有效期为一年，到期未使用将自动清零</p>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style lang="less" scoped>
.page-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main-content {
  max-width: 1760px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

.integral-container {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

.points-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px;
  background: linear-gradient(135deg, var(--color-primary) 0%, rgba(255, 255, 255, 0.3) 100%);
  border-radius: 12px;
  color: #fff;
  margin-bottom: 20px;
}

.points-info {
  .points-label {
    opacity: 0.9;
  }

  .points-value {
    font-size: 42px;
    font-weight: 700;
    margin: 8px 0;
  }

  .points-total {
    opacity: 0.8;
  }
}

.points-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-end;

  :deep(.el-button) {
    width: 120px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0;
  }

  :deep(.el-button--primary) {
    background-color: #fff;
    color: var(--color-primary);
    border-color: #fff;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  :deep(.el-button--warning) {
    color: var(--color-primary);
    background-color: #fff;
    border-color: #fff;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.tab-section {
  margin-bottom: 20px;

  :deep(.van-tabs__wrap) {
    border-bottom: 1px solid #eaeaea;
  }
}

.category-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  overflow-x: auto;
  padding-bottom: 5px;

  .category-item {
    padding: 8px 16px;
    border-radius: 20px;
    color: #666;
    background-color: #f5f5f5;
    cursor: pointer;
    white-space: nowrap;
    transition: all 0.3s ease;

    &:hover {
      background-color: #e8e8e8;
    }

    &.active {
      background-color: var(--color-primary);
      color: #fff;
    }
  }
}

.sort-bar {
  margin-bottom: 15px;
  display: flex;
  justify-content: flex-end;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

.product-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #eaeaea;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
    border-color: var(--color-primary);
  }
}

.product-image {
  position: relative;
  background-color: #f5f5f5;
  border-radius: 4px 4px 0 0;
  overflow: hidden;

  .stock-badge {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 4px 8px;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
    text-align: center;

    &.low {
      background: rgba(245, 108, 108, 0.9);
    }
  }
}

.product-info {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.product-name {
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  height: 40px;
}

.product-points {
  display: flex;
  align-items: baseline;
  gap: 2px;

  .points-icon {
    color: #ffc107;
  }

  .points-value {
    font-size: 20px;
    font-weight: 700;
    color: #ff4400; /* 积分价格使用橙红色，参考淘宝价格颜色 */
  }

  .points-unit {
    color: #999;
  }
}

.product-market {
  color: #999;
  text-decoration: line-through;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.record-card {
  padding: 15px;
  background-color: #fafafa;
  border-radius: 8px;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;

  .record-name {
    font-weight: 600;
    color: #333;
  }
}

.record-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;

  .record-points {
    font-weight: 600;
    color: #ff4400; /* 记录积分价格使用橙红色，参考淘宝价格颜色 */
  }

  .record-time {
    color: #999;
  }
}

.record-detail {
  color: #666;

  .detail-label {
    color: #999;
  }

  .detail-value {
    color: #333;
  }
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 30px;
}

.task-card {
  display: flex;
  align-items: center;
  padding: 15px;
  background-color: #fafafa;
  border-radius: 8px;
  gap: 15px;
}

.task-info {
  flex: 1;

  .task-name {
    font-weight: 600;
    color: #333;
    margin-bottom: 4px;
  }

  .task-desc {
    color: #999;
  }
}

.task-progress {
  width: 150px;

  .progress-bar {
    height: 6px;
    background-color: #eaeaea;
    border-radius: 3px;
    overflow: hidden;
    margin-bottom: 4px;
  }

  .progress-fill {
    height: 100%;
    background-color: var(--color-primary);
    border-radius: 3px;
    transition: width 0.3s ease;
  }

  .progress-text {
    display: flex;
    justify-content: space-between;
    color: #999;

    .task-points {
      color: var(--color-primary);
    }
  }
}

.rules-section {
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;

  .rules-title {
    font-weight: 600;
    color: #333;
    margin-bottom: 15px;
  }

  .rules-content {
    color: #666;
    line-height: 2;

    p {
      margin: 0;
    }
  }
}

.empty-section {
  padding: 60px 20px;
  text-align: center;
}

@media (max-width: 1400px) {
  .product-grid {
    grid-template-columns: repeat(5, 1fr);
  }
}

@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 992px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .task-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .task-progress {
    width: 100%;
  }
}
</style>
