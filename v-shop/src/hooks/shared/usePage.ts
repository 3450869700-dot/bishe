import { computed, unref } from 'vue';
import { useRouter } from 'vue-router';

import { useUserStore } from '@/store/modules/user';
import { useAppStore } from '@/store/modules/app';
import { onMountedOrActivated } from '../core/onMountedOrActivated';

export function usePage() {
  const userStore = useUserStore();
  const appStore = useAppStore();
  const router = useRouter();

  const token = computed(() => userStore.getToken);
  const isOmnipotent = computed(() => userStore.getIsOmnipotent);
  // 确保hasLogin正确计算，包含万能账户状态
  const hasLogin = computed(() => {
    const hasToken = !!unref(token);
    const isOmni = unref(isOmnipotent);
    const result = hasToken || isOmni;
    console.log('usePage - hasLogin computed:', { hasToken, isOmni, result });
    return result;
  });
  const theme = computed(() => appStore.getTheme);

  function goPage(path: string) {
    router.push({ path });
  }

  function goLogin() {
    router.replace('/role-select');
  }

  function goHome() {
    router.replace('/');
  }

  return {
    token,
    isOmnipotent,
    hasLogin,
    theme,
    // 跳转页面
    goHome,
    goLogin,
    goPage,
    //
    onMountedOrActivated,
  };
}
