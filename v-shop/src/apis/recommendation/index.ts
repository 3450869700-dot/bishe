import { request } from '@/utils/request';

// 推荐服务API
export const recommendationApi = {
  // 获取用户推荐商品
  getUserRecommendations: (userId: number) => {
    return request({
      url: `/recommendation/user/${userId}`,
      method: 'GET',
    });
  },
  // 获取热门商品
  getPopularItems: () => {
    return request({
      url: '/recommendation/popular',
      method: 'GET',
    });
  },
};
