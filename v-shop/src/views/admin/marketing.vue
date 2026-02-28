<script setup lang="ts">
import { ref } from 'vue';
import { showToast, showConfirmDialog } from 'vant';

const activeTab = ref('coupons');

const couponList = ref([
  {
    id: 1,
    name: '新人专享券',
    type: 'discount',
    amount: 20,
    condition: 100,
    totalCount: 1000,
    usedCount: 456,
    startTime: '2024-01-01',
    endTime: '2024-01-31',
    status: 'active',
  },
  {
    id: 2,
    name: '满减优惠券',
    type: 'reduction',
    amount: 50,
    condition: 200,
    totalCount: 500,
    usedCount: 123,
    startTime: '2024-01-05',
    endTime: '2024-02-05',
    status: 'active',
  },
  {
    id: 3,
    name: '会员专属券',
    type: 'discount',
    amount: 30,
    condition: 150,
    totalCount: 200,
    usedCount: 89,
    startTime: '2023-12-01',
    endTime: '2023-12-31',
    status: 'expired',
  },
]);

const activityList = ref([
  {
    id: 1,
    name: '新年特惠拼团',
    type: 'group',
    status: 'active',
    participants: 128,
    orders: 89,
    startTime: '2024-01-01',
    endTime: '2024-01-31',
  },
  {
    id: 2,
    name: '春节预售活动',
    type: 'presale',
    status: 'pending',
    participants: 56,
    orders: 45,
    startTime: '2024-01-15',
    endTime: '2024-02-10',
  },
  {
    id: 3,
    name: '会员日特惠',
    type: 'discount',
    status: 'ended',
    participants: 320,
    orders: 256,
    startTime: '2023-12-12',
    endTime: '2023-12-12',
  },
]);

const adPositionList = ref([
  { id: 1, name: '首页轮播图', key: 'home_banner', count: 5, price: 500 },
  { id: 2, name: '分类页广告', key: 'category_ad', count: 3, price: 300 },
  { id: 3, name: '商品详情广告', key: 'detail_ad', count: 2, price: 800 },
]);

const messageList = ref([
  { id: 1, title: '新品上架通知', type: 'system', sendCount: 56890, sendTime: '2024-01-08 10:00', status: 'sent' },
  { id: 2, title: '优惠活动提醒', type: 'marketing', sendCount: 12850, sendTime: '2024-01-05 15:30', status: 'sent' },
  { id: 3, title: '物流更新通知', type: 'system', sendCount: 892, sendTime: '-', status: 'draft' },
]);

function onAddCoupon() {
  showToast('跳转添加优惠券');
}

function onToggleCouponStatus(coupon: (typeof couponList.value)[0]) {
  coupon.status = coupon.status === 'active' ? 'inactive' : 'active';
  showToast({ type: 'success', message: '状态已更新' });
}

function onDeleteCoupon(coupon: (typeof couponList.value)[0]) {
  showConfirmDialog({
    title: '确认删除',
    message: `确定要删除优惠券「${coupon.name}」吗？`,
  })
    .then(() => {
      const index = couponList.value.findIndex((c) => c.id === coupon.id);
      if (index > -1) {
        couponList.value.splice(index, 1);
        showToast({ type: 'success', message: '删除成功' });
      }
    })
    .catch(() => {});
}

function onAddActivity() {
  showToast('跳转添加活动');
}

function onToggleActivityStatus(activity: (typeof activityList.value)[0]) {
  activity.status = activity.status === 'active' ? 'ended' : 'active';
  showToast({ type: 'success', message: '状态已更新' });
}

function onSendMessage() {
  showToast('跳转发送消息');
}

function getStatusType(status: string): string {
  const map: Record<string, string> = {
    active: 'success',
    pending: 'warning',
    ended: 'info',
    inactive: 'info',
    expired: 'danger',
    draft: 'info',
  };
  return map[status] || 'info';
}

function getStatusText(status: string): string {
  const map: Record<string, string> = {
    active: '进行中',
    pending: '待开始',
    ended: '已结束',
    inactive: '已停用',
    expired: '已过期',
    draft: '草稿',
  };
  return map[status] || status;
}
</script>

<template>
  <div class="marketing-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="优惠券管理" name="coupons">
        <div class="section-card">
          <div class="section-header">
            <h3>优惠券列表</h3>
            <el-button type="primary" size="small" @click="onAddCoupon">创建优惠券</el-button>
          </div>
          <el-table :data="couponList" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
            <el-table-column prop="name" label="优惠券名称" min-width="150" />
            <el-table-column label="优惠内容" width="150">
              <template #default="{ row }">
                <span>{{
                  row.type === 'discount'
                    ? '满' + row.condition + '减' + row.amount
                    : '满' + row.condition + '减' + row.amount
                }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="totalCount" label="发放总量" width="100" />
            <el-table-column prop="usedCount" label="已使用" width="100" />
            <el-table-column label="使用率" width="100">
              <template #default="{ row }"> {{ ((row.usedCount / row.totalCount) * 100).toFixed(1) }}% </template>
            </el-table-column>
            <el-table-column label="有效期" width="200">
              <template #default="{ row }"> {{ row.startTime }} ~ {{ row.endTime }} </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" text>编辑</el-button>
                <el-button
                  :type="row.status === 'active' ? 'warning' : 'success'"
                  size="small"
                  text
                  @click="onToggleCouponStatus(row)"
                >
                  {{ row.status === 'active' ? '停用' : '启用' }}
                </el-button>
                <el-button type="danger" size="small" text @click="onDeleteCoupon(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="活动管理" name="activities">
        <div class="section-card">
          <div class="section-header">
            <h3>营销活动</h3>
            <el-button type="primary" size="small" @click="onAddActivity">创建活动</el-button>
          </div>
          <el-table :data="activityList" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
            <el-table-column prop="name" label="活动名称" min-width="180" />
            <el-table-column label="活动类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{
                  row.type === 'group' ? '拼团' : row.type === 'presale' ? '预售' : '折扣'
                }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="participants" label="参与人数" width="100" />
            <el-table-column prop="orders" label="订单数" width="100" />
            <el-table-column label="活动时间" width="220">
              <template #default="{ row }"> {{ row.startTime }} ~ {{ row.endTime }} </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" text>编辑</el-button>
                <el-button
                  v-if="row.status !== 'ended'"
                  :type="row.status === 'active' ? 'warning' : 'success'"
                  size="small"
                  text
                  @click="onToggleActivityStatus(row)"
                >
                  {{ row.status === 'active' ? '结束' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="广告位管理" name="ads">
        <div class="section-card">
          <div class="section-header">
            <h3>广告位配置</h3>
            <el-button type="primary" size="small">添加广告位</el-button>
          </div>
          <el-table :data="adPositionList" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
            <el-table-column prop="name" label="广告位名称" width="150" />
            <el-table-column prop="key" label="广告位标识" width="150" />
            <el-table-column prop="count" label="广告数量" width="100" />
            <el-table-column prop="price" label="参考价格(元/天)" width="150">
              <template #default="{ row }"> ¥{{ row.price }} </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" text>编辑</el-button>
                <el-button type="primary" size="small" text>投放广告</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="消息推送" name="messages">
        <div class="section-card">
          <div class="section-header">
            <h3>消息列表</h3>
            <el-button type="primary" size="small" @click="onSendMessage">发送消息</el-button>
          </div>
          <el-table :data="messageList" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
            <el-table-column prop="title" label="消息标题" min-width="200" />
            <el-table-column label="消息类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.type === 'system' ? '系统通知' : '营销消息' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sendCount" label="发送人数" width="100" />
            <el-table-column prop="sendTime" label="发送时间" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'sent' ? 'success' : 'info'" size="small">
                  {{ row.status === 'sent' ? '已发送' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" text>查看</el-button>
                <el-button type="primary" size="small" text>再次发送</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script lang="ts">
export default {
  name: 'AdminMarketing',
};
</script>

<style lang="less" scoped>
.marketing-page {
  max-width: 1760px;
  margin: 0 auto;
}

.section-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }
}
</style>
