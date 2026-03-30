import type { AxiosRequestConfig, AxiosInstance, AxiosResponse, AxiosError } from 'axios';
import axios from 'axios';
import qs from 'qs';
import { getAPI } from '@/utils';
import { RequestOptions, CustomRequestConfig, ServiceResultCodeEnum, ServiceResult, ContentTypeEnum } from './types';
import { httpErrorHandle, serviceErrorHandle } from './handler';
import { useUserStoreWithOut } from '@/store/modules/user';

/**
 * 扩展请求配置项，下面的选项都可以在独立的接口请求中覆盖
 */
export let requestOptions: RequestOptions = {};

/**
 * 创建 axios 实例
 */
const instance: AxiosInstance = axios.create({
  // 基础接口地址
  baseURL: getAPI(),
  // 请求超时事件
  timeout: 5 * 1000,
  // 使用 form-urlencoded 格式，即伪表单
  headers: {
    'Content-Type': ContentTypeEnum.FORM_URLENCODED,
  },
});

/**
 * 请求拦截器
 */
instance.interceptors.request.use((config: AxiosRequestConfig) => {
  // 追加token
  // userStore.getToken && (config.headers.token = userStore.getToken);
  const userStore = useUserStoreWithOut();
  // 检查是否是万能账户，如果是则使用特殊处理
  const isOmnipotent = userStore.getIsOmnipotent;
  const joinPayloadData = (data: any) => {
    // 如果是万能账户，确保token不为空（使用万能账户token）
    // 如果不是万能账户，才使用普通token
    const token = isOmnipotent ? userStore.getToken || 'omnipotent_token' : userStore.getToken;
    return { ...data, token };
  };

  // 序列化数据
  // 注意：axios headers 可能是 AxiosHeaders 对象，需要正确获取 Content-Type
  const contentType = config.headers?.['Content-Type'] || config.headers?.get?.('Content-Type');
  console.log('Request Content-Type:', contentType, 'Method:', config.method);

  // 注意：axios的method可能是大写的
  const method = (config.method || 'GET').toLowerCase();
  console.log('Request Method (normalized):', method);

  // 如果是 JSON 格式，保持数据不变（不序列化为 form-urlencoded）
  if (contentType === ContentTypeEnum.JSON || contentType?.includes?.('application/json')) {
    console.log('JSON request, keeping data as object');
    // 对于JSON格式的请求，也需要添加token
    if (['post', 'put', 'patch'].includes(method)) {
      config.data = joinPayloadData(config.data);
    }

    if (['delete', 'get', 'head'].includes(method)) {
      config.params = joinPayloadData(config.params);
    }
  } else {
    // 默认使用 form-urlencoded 格式
    console.log('Form request, serializing data');
    if (['post', 'put', 'patch'].includes(method)) {
      config.data = qs.stringify(joinPayloadData(config.data));
    }

    if (['delete', 'get', 'head'].includes(method)) {
      config.params = joinPayloadData(config.params);
    }
  }

  return config;
});

/**
 * 响应拦截器
 */
instance.interceptors.response.use(
  (response: AxiosResponse<any>) => {
    const result: ServiceResult = response.data;
    const { code } = result;

    // 不进行任何处理，直接返回原生响应
    if (requestOptions.isReturnNativeResponse) {
      return response;
    }

    // 不进行任何处理，直接返回数据
    if (!requestOptions.isTransformResponse) {
      return result;
    }

    if (Number(code) === ServiceResultCodeEnum.SUCCESS) {
      return result;
    } else if (Number(code) === ServiceResultCodeEnum.NO_DATA) {
      result.data = null;
      return result;
    } else {
      serviceErrorHandle(result);
      return Promise.reject(result);
    }
  },
  (error: AxiosError) => {
    const result = error?.response?.data;

    // 检查是否是万能账户，如果是则不显示网络错误提示
    const userStore = useUserStoreWithOut();
    if (!userStore.getIsOmnipotent) {
      if (result) {
        serviceErrorHandle(result);
      } else {
        httpErrorHandle(error);
      }
    } else {
      console.log('Omnipotent user, skipping network error message:', error.message);
    }

    return Promise.reject(error);
  },
);

/**
 * 通用请求函数
 */
export function request<T = ServiceResult>(config: CustomRequestConfig): Promise<T> {
  requestOptions = {
    isTransformResponse: true,
    isReturnNativeResponse: false,
    errorMessageMode: 'message',
    networkErrorMessageMode: 'message',
    ...config.requestOptions,
  };

  return new Promise((resolve, reject) => {
    instance
      .request<any, AxiosResponse<ServiceResult>>(config)
      .then((res: AxiosResponse<ServiceResult>) => {
        resolve(res as unknown as Promise<T>);
      })
      .catch((e: Error | AxiosError) => {
        reject(e);
      });
  });
}
