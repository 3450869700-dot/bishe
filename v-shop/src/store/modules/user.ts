import { defineStore } from 'pinia';
import { store } from '@/store';
import { router } from '@/router';
import { loginProviderType } from '@/constants/modules/user';
import { goodStorage } from '@/utils';
import API_USER from '@/apis/user';

export type UserRole = 'consumer' | 'merchant' | 'admin';

export interface UserState {
  token: string;
  userInfo: NonNullable<UserInfo>;
  userLevel: NonNullable<UserLevel>;
  role: UserRole;
  isOmnipotent: boolean;
}

export const useUserStore = defineStore({
  id: 'user',
  state: (): UserState => ({
    token: goodStorage.get('token', ''),
    userInfo: goodStorage.get('userInfo', {}),
    userLevel: {},
    role: goodStorage.get('role', 'consumer'),
    isOmnipotent: goodStorage.get('isOmnipotent', false),
  }),
  getters: {
    getToken: (state): string => state.token,
    hasLogin: (state): boolean => !!state.token || state.isOmnipotent,
    getUserInfo: (state): UserInfo => state.userInfo,
    getUserLevel: (state): UserLevel => state.userLevel,
    getRole: (state): UserRole => state.role,
    isConsumer: (state): boolean => state.role === 'consumer' || state.isOmnipotent,
    isMerchant: (state): boolean => state.role === 'merchant' || state.isOmnipotent,
    isAdmin: (state): boolean => state.role === 'admin' || state.isOmnipotent,
    getIsOmnipotent: (state): boolean => state.isOmnipotent,
  },
  actions: {
    async login(payload: Recordable = {}) {
      const { provider = 'system', params } = payload;

      try {
        const loginProvider = loginProviderType[provider];

        const res = await API_USER[loginProvider.apiName](params);
        const { token } = res.data;

        this.token = token;
        goodStorage.set('token', token);

        // 普通用户登录时，清除万能账户状态
        if (this.isOmnipotent) {
          this.isOmnipotent = false;
          goodStorage.set('isOmnipotent', false);
          console.log('=== login - cleared omnipotent state for normal user');
        }

        // 登录成功后获取用户详情
        await this.getUserDetail();

        return res.data;
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async logout(payload: Recordable = {}) {
      const { goLogin = false } = payload;

      if (this.getToken && !this.isOmnipotent) {
        try {
          await API_USER.userLoginOut();
        } catch (error) {
          console.error('退出登录失败', error);
        }
      }

      this.token = '';
      goodStorage.set('token', '');
      this.userInfo = {};
      goodStorage.set('userInfo', {});
      this.role = 'consumer';
      goodStorage.set('role', 'consumer');
      this.isOmnipotent = false;
      goodStorage.set('isOmnipotent', false);
      goLogin && router.push('/role-select');
    },
    setRole(role: UserRole) {
      this.role = role;
      goodStorage.set('role', role);
    },
    setOmnipotent(enabled = true) {
      this.isOmnipotent = enabled;
      goodStorage.set('isOmnipotent', enabled);
      if (enabled) {
        this.token = 'omnipotent_token_' + Date.now();
        goodStorage.set('token', this.token);

        // 清除旧的 userInfo 缓存，确保使用新的 user_id
        goodStorage.set('userInfo', {});

        this.userInfo = {
          id: 2,
          user_id: 2,
          nick: '万能管理员',
          avatar: '',
        };
        goodStorage.set('userInfo', this.userInfo);

        console.log('=== setOmnipotent - userInfo:', this.userInfo);
      }
    },
    async getUserDetail() {
      try {
        const res = await API_USER.userDetail();
        const { base = {}, userLevel = {} } = res.data;

        // 确保 userInfo 中有 user_id 字段
        const userInfo = {
          ...base,
          user_id: base.user_id || base.id || 1,
        };

        this.userInfo = userInfo;
        goodStorage.set('userInfo', userInfo);
        this.userLevel = userLevel;

        console.log('=== getUserDetail - userInfo:', userInfo);
      } catch (error) {
        console.error('获取用户详情失败', error);
      }
    },
  },
});

export function useUserStoreWithOut() {
  return useUserStore(store);
}

export interface UserInfo {
  id?: number;
  user_id?: number;
  nick?: string;
  avatar?: string;
  [key: string]: any;
}
export interface UserLevel {
  id?: number;
  [key: string]: any;
}
