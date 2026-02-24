import type { AxiosError } from 'axios';
import { ServiceResultCodeEnum, ServiceResult } from './types';
import { useUserStoreWithOut } from '@/store/modules/user';
import { showToast, closeToast } from 'vant';
import { requestOptions } from './index';

/**
 * 业务错误
 * @param {ServiceResult} res 业务约定的返回数据
 * @param {number} res.code 业务约定的错误码
 * @param {string} res.msg 业务上的错误信息
 * @param {*} res.data
 */
export function serviceErrorHandle(res: ServiceResult) {
  const { code, msg } = res;
  console.log('serviceErrorHandle - code:', code, 'msg:', msg, 'requestOptions:', requestOptions);

  // 检查是否是万能账户
  const userStore = useUserStoreWithOut();
  const isOmnipotent = userStore.getIsOmnipotent;
  console.log('serviceErrorHandle - isOmnipotent:', isOmnipotent);

  // 如果是万能账户，无论什么错误码，都不显示错误提示，避免影响用户体验
  if (isOmnipotent) {
    console.log('Omnipotent user, skipping error message:', msg);
    return;
  }

  // 非万能账户，正常处理错误
  if (Number(code) === ServiceResultCodeEnum.LOGIN_EXPIRED) {
    closeToast();
    userStore.logout({ goLogin: true });
  } else {
    if (requestOptions.errorMessageMode === 'message') {
      showToast({
        message: msg || 'Result Error',
        duration: 3 * 1000,
      });
    }
  }
}

/**
 * HTTP 错误
 */
export function httpErrorHandle(error: AxiosError) {
  // 检查是否是万能账户，如果是则不显示网络错误提示
  const userStore = useUserStoreWithOut();
  const isOmnipotent = userStore.getIsOmnipotent;
  if (isOmnipotent) {
    console.log('Omnipotent user, skipping http error message:', error.message);
    return;
  }

  // console.log('[httpErrorHandle]', error);
  // console.log('[httpErrorHandle]', error.toString());

  let errMessage = ''; // Http Error

  if (error?.response) {
    const { status } = error.response;

    switch (status) {
      case 403:
        errMessage = `${status} 网络请求被拒绝`;
        break;
      case 404:
        errMessage = `${status} 网络请求不存在`;
        break;
      case 500:
        errMessage = `${status} 服务器内部错误`;
        break;
      default:
        errMessage = `${status || error.message}`;
        break;
    }
  }

  // 网络出错
  if (error?.message) {
    if (error.message.includes('timeout')) {
      errMessage = '请求超时';
    }
    if (error.message.includes('Network Error')) {
      errMessage = '当前网络不可用，请检查你的网络设置';
    }
  }

  if (errMessage) {
    if (requestOptions.networkErrorMessageMode === 'message') {
      showToast({
        message: errMessage,
        duration: 3 * 1000,
      });
    }
  }
}
