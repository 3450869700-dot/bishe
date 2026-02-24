import type { Router } from 'vue-router';
import { pageEnum } from '@/constants';
import { useUserStoreWithOut } from '@/store/modules/user';

export function createPermissionGuard(router: Router) {
  const userStore = useUserStoreWithOut();

  router.beforeEach(async (to, _from, next) => {
    // 每次路由跳转时重新获取isOmnipotent值，确保是最新的
    const isOmnipotent = userStore.getIsOmnipotent;
    const userRole = userStore.getRole;
    const token = userStore.getToken;

    console.log(
      'PermissionGuard - Route:',
      to.path,
      'Role:',
      userRole,
      'IsOmnipotent:',
      isOmnipotent,
      'Token:',
      !!token,
    );

    // 检查当前用户是否需要登录
    if (to.meta.needLogin) {
      // 对于需要登录的页面，检查用户是否已登录或是否是万能账户
      const hasValidLogin = !!token || isOmnipotent;
      console.log('Need login check - hasValidLogin:', hasValidLogin, 'needLogin:', to.meta.needLogin);

      if (!hasValidLogin) {
        const redirect = to.path || '/';
        console.log('Need login, redirecting to login with redirect:', redirect);
        next({
          path: pageEnum.LOGIN,
          query: {
            ...to.query,
            redirect,
          },
        });
        return;
      }
    }

    // 检查角色权限
    const requiredRoles = to.meta.roles as string[] | undefined;
    if (requiredRoles && requiredRoles.length > 0 && !isOmnipotent) {
      console.log('Checking role permissions, required:', requiredRoles, 'current:', userRole);
      if (!requiredRoles.includes(userRole)) {
        if (userRole === 'merchant') {
          next({ path: '/merchant/dashboard' });
        } else if (userRole === 'admin') {
          next({ path: '/admin/dashboard' });
        } else {
          next({ path: '/role-select' });
        }
        return;
      }
    }

    // 商户端路由需要商户权限（万能账户除外）
    if (to.path.startsWith('/merchant') && userRole !== 'merchant' && !isOmnipotent) {
      console.log('Merchant route access denied for role:', userRole, 'omnipotent:', isOmnipotent);
      if (userRole === 'admin') {
        next({ path: '/admin/dashboard' });
      } else {
        next({ path: '/role-select' });
      }
      return;
    }

    // 管理员端路由需要管理员权限（万能账户除外）
    if (to.path.startsWith('/admin') && userRole !== 'admin' && !isOmnipotent) {
      console.log('Admin route access denied for role:', userRole, 'omnipotent:', isOmnipotent);
      if (userRole === 'merchant') {
        next({ path: '/merchant/dashboard' });
      } else {
        next({ path: '/role-select' });
      }
      return;
    }

    // 消费者端路由检查 - 允许所有用户（包括万能账户）访问
    // 这里不需要额外的权限检查，因为消费者页面是公开的或只需要登录

    console.log('Route access granted:', to.path);
    next();
  });
}
