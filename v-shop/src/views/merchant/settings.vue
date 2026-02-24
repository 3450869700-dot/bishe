<script setup lang="ts">
import { ref } from 'vue';
import { showToast } from 'vant';

const activeTab = ref('account');

const shopInfo = ref({
  name: '阳光果园',
  logo: '',
  contact: '138****1234',
  address: '浙江省杭州市余杭区良渚街道',
  description: '主营有机水果，产地直销，新鲜直达',
});

const logisticsInfo = ref({
  address: '浙江省杭州市余杭区良渚街道88号',
  contact: '张先生',
  phone: '138****1234',
  freightTemplate: '统一运费',
  freightAmount: 8,
});

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const showLogoDialog = ref(false);

function onSaveShopInfo() {
  showToast({
    type: 'success',
    message: '店铺信息已保存',
  });
}

function onSaveLogistics() {
  showToast({
    type: 'success',
    message: '物流设置已保存',
  });
}

function onChangePassword() {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    showToast('请填写完整信息');
    return;
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    showToast('两次密码不一致');
    return;
  }
  showToast({
    type: 'success',
    message: '密码修改成功',
  });
  passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' };
}

function onSelectLogo() {
  showLogoDialog.value = true;
}

function onSelectAddress() {
  showToast('地图选址功能开发中');
}
</script>

<template>
  <div class="settings-page">
    <el-tabs v-model="activeTab" class="settings-tabs">
      <el-tab-pane label="账户信息" name="account">
        <div class="settings-section">
          <h3>修改密码</h3>
          <el-form :model="passwordForm" label-width="120px" class="password-form">
            <el-form-item label="当前密码">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="onChangePassword">确认修改</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="settings-section">
          <h3>绑定手机</h3>
          <el-form label-width="120px">
            <el-form-item label="当前手机号">
              <span>138****1234</span>
              <el-button type="primary" size="small" style="margin-left: 16px">更换手机号</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane label="店铺设置" name="shop">
        <div class="settings-section">
          <h3>基本信息</h3>
          <el-form :model="shopInfo" label-width="120px">
            <el-form-item label="店铺Logo">
              <div class="logo-upload" @click="onSelectLogo">
                <img v-if="shopInfo.logo" :src="shopInfo.logo" alt="logo" class="logo-preview" />
                <div v-else class="logo-placeholder">
                  <van-icon name="plus" size="32" />
                  <span>上传Logo</span>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="店铺名称">
              <el-input v-model="shopInfo.name" placeholder="请输入店铺名称" style="width: 300px" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="shopInfo.contact" placeholder="请输入联系电话" style="width: 300px" />
            </el-form-item>
            <el-form-item label="店铺简介">
              <el-input
                v-model="shopInfo.description"
                type="textarea"
                :rows="3"
                placeholder="请输入店铺简介"
                style="width: 400px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="onSaveShopInfo">保存设置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="settings-section">
          <h3>店铺位置</h3>
          <el-form label-width="120px">
            <el-form-item label="果园位置">
              <el-input v-model="shopInfo.address" placeholder="请选择位置" style="width: 400px" />
              <el-button style="margin-left: 12px" @click="onSelectAddress">
                <van-icon name="location-o" /> 在地图上选择
              </el-button>
            </el-form-item>
          </el-form>
          <div class="map-preview">
            <div class="map-placeholder">
              <van-icon name="location" size="40" color="#ff7d00" />
              <span>地图预览区域</span>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="物流设置" name="logistics">
        <div class="settings-section">
          <h3>发货地址</h3>
          <el-form :model="logisticsInfo" label-width="120px">
            <el-form-item label="发货地址">
              <el-input v-model="logisticsInfo.address" placeholder="请输入详细地址" style="width: 500px" />
            </el-form-item>
            <el-form-item label="联系人">
              <el-input v-model="logisticsInfo.contact" placeholder="请输入联系人姓名" style="width: 200px" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="logisticsInfo.phone" placeholder="请输入联系电话" style="width: 200px" />
            </el-form-item>
          </el-form>
        </div>

        <div class="settings-section">
          <h3>运费设置</h3>
          <el-form :model="logisticsInfo" label-width="120px">
            <el-form-item label="运费模板">
              <el-select v-model="logisticsInfo.freightTemplate" style="width: 200px">
                <el-option label="统一运费" value="统一运费" />
                <el-option label="按重量计费" value="按重量计费" />
                <el-option label="按件数计费" value="按件数计费" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="logisticsInfo.freightTemplate === '统一运费'" label="运费金额">
              <el-input-number v-model="logisticsInfo.freightAmount" :min="0" :precision="2" />
              <span class="unit">元</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="onSaveLogistics">保存设置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Logo选择对话框 -->
    <el-dialog v-model="showLogoDialog" title="选择店铺Logo" width="400px">
      <div class="logo-options">
        <div class="logo-option">
          <img src="/src/assets/images/avatar_default.png" alt="logo" />
        </div>
        <div class="logo-option">
          <img src="/src/assets/images/avatar_default.png" alt="logo" />
        </div>
        <div class="logo-option">
          <img src="/src/assets/images/avatar_default.png" alt="logo" />
        </div>
        <div class="logo-option">
          <img src="/src/assets/images/avatar_default.png" alt="logo" />
        </div>
      </div>
      <template #footer>
        <el-button @click="showLogoDialog = false">取消</el-button>
        <el-button type="primary">确认选择</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
export default {
  name: 'MerchantSettings',
};
</script>

<style lang="less" scoped>
.settings-page {
  max-width: 800px;
  margin: 0 auto;
}

.settings-tabs {
  :deep(.el-tabs__header) {
    background: #fff;
    border-radius: 12px 12px 0 0;
    padding: 0 20px;
    margin-bottom: 0;
  }

  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }
}

.settings-section {
  background: #fff;
  padding: 24px;
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0 0 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;
  }
}

.password-form {
  max-width: 500px;
}

.logo-upload {
  width: 100px;
  height: 100px;
  border: 2px dashed #ddd;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: #ff7d00;
  }
}

.logo-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
}

.logo-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #999;

  span {
    font-size: 12px;
  }
}

.map-preview {
  margin-top: 16px;
  margin-left: 120px;
}

.map-placeholder {
  width: 400px;
  height: 200px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #999;
}

.unit {
  margin-left: 8px;
  color: #666;
}

.logo-options {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.logo-option {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  transition: all 0.3s;

  &:hover {
    border-color: #ff7d00;
  }

  img {
    width: 100%;
    display: block;
  }
}
</style>
