import { request } from '@/utils/request';
import { ContentTypeEnum } from '@/utils/request/types';

/**
 * 获取所有购物车项目
 */
export function cartList(data?: Recordable) {
  return request({
    url: `/shopping-cart`,
    method: 'get',
    params: data,
  });
}

/**
 * 根据用户ID获取购物车项目
 */
export function cartListByUserId(userId: number) {
  return request({
    url: `/shopping-cart/user/${userId}`,
    method: 'get',
  });
}

/**
 * 加入购物车
 */
export function cartAdd(data?: Recordable) {
  console.log('cartAdd called with data:', data);
  return request({
    url: `/shopping-cart/add`,
    method: 'post',
    data,
    headers: {
      'Content-Type': ContentTypeEnum.JSON,
    },
    requestOptions: {
      errorMessageMode: 'message',
    },
  })
    .then((res) => {
      console.log('cartAdd response:', res);
      return res;
    })
    .catch((error) => {
      console.error('cartAdd error:', error);
      throw error;
    });
}

/**
 * 更新购物车项目
 */
export function cartUpdate(id: number, data?: Recordable) {
  return request({
    url: `/shopping-cart/${id}`,
    method: 'put',
    data,
    headers: {
      'Content-Type': ContentTypeEnum.JSON,
    },
  });
}

/**
 * 删除购物车项目
 */
export function cartDelete(id: number) {
  return request({
    url: `/shopping-cart/${id}`,
    method: 'delete',
  });
}

/**
 * 获取购物车项目总数
 */
export function cartCount(data?: Recordable) {
  return request({
    url: `/shopping-cart/count`,
    method: 'get',
    params: data,
  });
}

export default {
  cartList,
  cartListByUserId,
  cartAdd,
  cartUpdate,
  cartDelete,
  cartCount,
};
