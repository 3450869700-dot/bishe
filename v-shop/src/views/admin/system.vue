<script setup lang="ts">
import { ref } from 'vue';
import { showToast } from 'vant';

const activeTab = ref('roles');

const roleList = ref([
  { id: 1, name: '超级管理员', description: '拥有所有权限', users: 2, permissions: ['all'] },
  { id: 2, name: '商户管理员', description: '管理商户后台', users: 5, permissions: ['merchant'] },
  { id: 3, name: '客服人员', description: '处理用户问题', users: 3, permissions: ['order_view', 'refund'] },
  { id: 4, name: '普通用户', description: '普通消费者', users: 56890, permissions: ['buy'] },
]);

const menuPermissions = ref([
  { id: 1, name: '工作台', key: 'dashboard' },
  { id: 2, name: '商户管理', key: 'merchants' },
  { id: 3, name: '用户管理', key: 'users' },
  { id: 4, name: '商品管理', key: 'products' },
  { id: 5, name: '订单管理', key: 'orders' },
  { id: 6, name: '溯源管理', key: 'traceability' },
  { id: 7, name: '数据分析', key: 'analysis' },
  { id: 8, name: '系统管理', key: 'system' },
  { id: 9, name: '营销管理', key: 'marketing' },
]);

const systemConfig = ref({
  siteName: 'V-Shop 农产品商城',
  logo: '',
  contactPhone: '400-888-8888',
  contactEmail: 'service@vshop.com',
  icp: 'ICP备12345678号',
});

const bannerList = ref([
  { id: 1, title: '新品上市', image: '', link: '/good/detail?id=1', status: 'active', sort: 1 },
  { id: 2, title: '限时优惠', image: '', link: '/coupon', status: 'active', sort: 2 },
  { id: 3, title: '会员专享', image: '', link: '/integral', status: 'inactive', sort: 3 },
]);

const noticeList = ref([
  { id: 1, title: '关于春节期间物流调整通知', time: '2024-01-08', status: 'active' },
  { id: 2, title: '新增商品类目上线公告', time: '2024-01-05', status: 'active' },
  { id: 3, title: '系统维护通知', time: '2024-01-01', status: 'inactive' },
]);

function onSaveSystemConfig() {
  showToast({ type: 'success', message: '系统配置已保存' });
}

function onSaveRoles() {
  showToast({ type: 'success', message: '权限配置已保存' });
}

function onAddBanner() {
  showToast('跳转添加轮播图');
}

function onAddNotice() {
  showToast('跳转添加公告');
}

function onToggleBannerStatus(banner: (typeof bannerList.value)[0]) {
  banner.status = banner.status === 'active' ? 'inactive' : 'active';
  showToast({ type: 'success', message: '状态已更新' });
}

function onDeleteBanner(banner: (typeof bannerList.value)[0]) {
  const index = bannerList.value.findIndex((b) => b.id === banner.id);
  if (index > -1) {
    bannerList.value.splice(index, 1);
    showToast({ type: 'success', message: '删除成功' });
  }
}
</script>

<template>
  <div class="system-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="角色权限" name="roles">
        <div class="section-card">
          <div class="section-header">
            <h3>角色管理</h3>
            <el-button type="primary" size="small">添加角色</el-button>
          </div>
          <el-table :data="roleList" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
            <el-table-column prop="name" label="角色名称" width="150" />
            <el-table-column prop="description" label="描述" min-width="200" />
            <el-table-column prop="users" label="用户数" width="100" />
            <el-table-column label="权限" min-width="300">
              <template #default="{ row }">
                <el-tag
                  v-for="perm in row.permissions"
                  :key="perm"
                  size="small"
                  :type="perm === 'all' ? 'danger' : 'primary'"
                  class="permission-tag"
                >
                  {{ perm === 'all' ? '全部权限' : perm }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" text>编辑</el-button>
                <el-button type="danger" size="small" text :disabled="row.id === 1">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="section-card">
          <div class="section-header">
            <h3>菜单权限配置</h3>
          </div>
          <div class="menu-permissions">
            <div v-for="menu in menuPermissions" :key="menu.id" class="menu-item">
              <el-checkbox>{{ menu.name }}</el-checkbox>
            </div>
          </div>
          <el-button type="primary" style="margin-top: 16px" @click="onSaveRoles">保存配置</el-button>
        </div>
      </el-tab-pane>

      <el-tab-pane label="系统配置" name="config">
        <div class="section-card">
          <div class="section-header">
            <h3>基本信息</h3>
          </div>
          <el-form :model="systemConfig" label-width="120px" style="max-width: 500px">
            <el-form-item label="平台名称">
              <el-input v-model="systemConfig.siteName" />
            </el-form-item>
            <el-form-item label="客服电话">
              <el-input v-model="systemConfig.contactPhone" />
            </el-form-item>
            <el-form-item label="客服邮箱">
              <el-input v-model="systemConfig.contactEmail" />
            </el-form-item>
            <el-form-item label="ICP备案号">
              <el-input v-model="systemConfig.icp" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="onSaveSystemConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane label="轮播图管理" name="banners">
        <div class="section-card">
          <div class="section-header">
            <h3>首页轮播图</h3>
            <el-button type="primary" size="small" @click="onAddBanner">添加轮播图</el-button>
          </div>
          <el-table :data="bannerList" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
            <el-table-column type="index" label="排序" width="80" />
            <el-table-column prop="title" label="标题" width="150" />
            <el-table-column prop="link" label="跳转链接" min-width="200" />
            <el-table-column prop="sort" label="排序" width="80" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                  {{ row.status === 'active' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" text @click="onToggleBannerStatus(row)">
                  {{ row.status === 'active' ? '禁用' : '启用' }}
                </el-button>
                <el-button type="danger" size="small" text @click="onDeleteBanner(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="通知公告" name="notices">
        <div class="section-card">
          <div class="section-header">
            <h3>系统公告</h3>
            <el-button type="primary" size="small" @click="onAddNotice">发布公告</el-button>
          </div>
          <el-table :data="noticeList" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
            <el-table-column prop="title" label="公告标题" min-width="300" />
            <el-table-column prop="time" label="发布时间" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                  {{ row.status === 'active' ? '已发布' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" text>编辑</el-button>
                <el-button type="danger" size="small" text>删除</el-button>
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
  name: 'AdminSystem',
};
</script>

<style lang="less" scoped>
.system-page {
  max-width: 1200px;
  margin: 0 auto;
}

.section-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
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

.permission-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

.menu-permissions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.menu-item {
  width: 150px;
}
</style>
