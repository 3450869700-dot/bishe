import { request } from '@/utils/request';
import { ContentTypeEnum } from '@/utils/request/types';

/**
 * 记录浏览行为
 * @param data { userId: number, productId: number, stayDuration?: number }
 */
export function recordView(data: { userId: number; productId: number; stayDuration?: number }) {
  return request({
    url: '/behavior/view',
    method: 'post',
    data,
    headers: {
      'Content-Type': ContentTypeEnum.JSON,
    },
  });
}

/**
 * 记录收藏行为
 * @param data { userId: number, productId: number }
 */
export function recordFavorite(data: { userId: number; productId: number }) {
  return request({
    url: '/behavior/favorite',
    method: 'post',
    data,
    headers: {
      'Content-Type': ContentTypeEnum.JSON,
    },
  });
}

/**
 * 记录加购行为
 * @param data { userId: number, productId: number }
 */
export function recordCart(data: { userId: number; productId: number }) {
  return request({
    url: '/behavior/cart',
    method: 'post',
    data,
    headers: {
      'Content-Type': ContentTypeEnum.JSON,
    },
  });
}

/**
 * 记录购买行为
 * @param data { userId: number, productId: number }
 */
export function recordPurchase(data: { userId: number; productId: number }) {
  return request({
    url: '/behavior/purchase',
    method: 'post',
    data,
    headers: {
      'Content-Type': ContentTypeEnum.JSON,
    },
  });
}

/**
 * 记录搜索行为
 * @param data { userId: number, keyword: string }
 */
export function recordSearch(data: { userId: number; keyword: string }) {
  return request({
    url: '/behavior/search',
    method: 'post',
    data,
    headers: {
      'Content-Type': ContentTypeEnum.JSON,
    },
  });
}

/**
 * 获取用户行为统计
 * @param userId 用户ID
 */
export function getBehaviorStats(userId: number) {
  return request({
    url: '/behavior/stats',
    method: 'get',
    params: { userId },
  });
}

export default {
  recordView,
  recordFavorite,
  recordCart,
  recordPurchase,
  recordSearch,
  getBehaviorStats,
};
