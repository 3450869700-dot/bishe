import { ref } from 'vue';
import { useUserStore } from '@/store/modules/user';
import { recordView, recordFavorite, recordCart, recordPurchase, recordSearch } from '@/apis/behavior';

/**
 * 用户行为记录组合式函数
 * 用于在页面中方便地记录用户行为
 */

// 用于防止重复记录的缓存
const recordedViews = new Set<string>();

/**
 * 记录商品浏览行为
 * 触发时机：用户点击进入商品详情页时记录一次
 * @param productId 商品ID
 * @returns 相关方法和状态
 */
export function useProductView(productId: number | string) {
  const userStore = useUserStore();
  const isRecorded = ref(false);

  // 记录浏览行为（只记录一次）
  const recordViewBehavior = async () => {
    // 防止重复记录（使用内存缓存）
    const cacheKey = `${userStore.userInfo?.id}_${productId}`;
    if (recordedViews.has(cacheKey) || isRecorded.value) {
      console.log('浏览行为已记录过，跳过:', { productId });
      return;
    }
    
    const userId = userStore.userInfo?.id;
    if (!userId || !productId) {
      console.warn('无法记录浏览行为：缺少用户ID或商品ID', { userId, productId });
      return;
    }

    try {
      console.log('正在记录浏览行为:', { userId, productId });
      await recordView({
        userId: Number(userId),
        productId: Number(productId),
        stayDuration: 0, // 不再记录停留时长
      });
      recordedViews.add(cacheKey);
      isRecorded.value = true;
      console.log('浏览行为已记录成功:', { productId });
    } catch (error) {
      console.error('记录浏览行为失败:', error);
    }
  };

  // 立即执行记录
  recordViewBehavior();

  return {
    recordView: recordViewBehavior,
    isRecorded,
  };
}

/**
 * 记录收藏行为
 * @param productId 商品ID
 */
export async function useRecordFavorite(productId: number | string) {
  const userStore = useUserStore();
  const userId = userStore.userInfo?.id;
  
  if (!userId || !productId) {
    console.warn('无法记录收藏行为：缺少用户ID或商品ID');
    return false;
  }

  try {
    await recordFavorite({
      userId: Number(userId),
      productId: Number(productId),
    });
    console.log('收藏行为已记录:', productId);
    return true;
  } catch (error) {
    console.error('记录收藏行为失败:', error);
    return false;
  }
}

/**
 * 记录加购行为
 * @param productId 商品ID
 */
export async function useRecordCart(productId: number | string) {
  const userStore = useUserStore();
  const userId = userStore.userInfo?.id;
  
  if (!userId || !productId) {
    console.warn('无法记录加购行为：缺少用户ID或商品ID');
    return false;
  }

  try {
    await recordCart({
      userId: Number(userId),
      productId: Number(productId),
    });
    console.log('加购行为已记录:', productId);
    return true;
  } catch (error) {
    console.error('记录加购行为失败:', error);
    return false;
  }
}

/**
 * 记录购买行为
 * @param productId 商品ID
 */
export async function useRecordPurchase(productId: number | string) {
  const userStore = useUserStore();
  const userId = userStore.userInfo?.id;
  
  if (!userId || !productId) {
    console.warn('无法记录购买行为：缺少用户ID或商品ID');
    return false;
  }

  try {
    await recordPurchase({
      userId: Number(userId),
      productId: Number(productId),
    });
    console.log('购买行为已记录:', productId);
    return true;
  } catch (error) {
    console.error('记录购买行为失败:', error);
    return false;
  }
}

/**
 * 记录搜索行为
 * @param keyword 搜索关键词
 */
export async function useRecordSearch(keyword: string) {
  const userStore = useUserStore();
  const userId = userStore.userInfo?.id;
  
  if (!userId || !keyword) {
    console.warn('无法记录搜索行为：缺少用户ID或关键词');
    return false;
  }

  try {
    await recordSearch({
      userId: Number(userId),
      keyword: keyword.trim(),
    });
    console.log('搜索行为已记录:', keyword);
    return true;
  } catch (error) {
    console.error('记录搜索行为失败:', error);
    return false;
  }
}

// 默认导出所有方法
export default {
  useProductView,
  useRecordFavorite,
  useRecordCart,
  useRecordPurchase,
  useRecordSearch,
};
