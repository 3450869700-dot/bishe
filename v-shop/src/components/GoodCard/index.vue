<script lang="ts">
import { defineComponent } from 'vue';
import { decimalFormat } from '@/utils/format';

export default defineComponent({
  name: 'GoodCard',
  props: {
    good: {
      type: Object,
      default: () => ({}),
    },
  },
  setup() {
    function propTitle(list: Recordable[]) {
      return list.map((v) => v.childName).join(',');
    }

    function getGoodImage(good: any) {
      if (good.pic) {
        return good.pic;
      }
      if (good.specs && good.specs.length > 0 && good.specs[0].pic) {
        return good.specs[0].pic;
      }
      return '/src/assets/images/avatar_default.png';
    }

    function getGoodPrice(good: any) {
      if (good.price) {
        return good.price;
      }
      if (good.minPrice) {
        return good.minPrice;
      }
      if (good.specs && good.specs.length > 0 && good.specs[0].price) {
        return good.specs[0].price;
      }
      return 0;
    }

    function handleImageError(e: Event) {
      const target = e.target as HTMLImageElement;
      target.src = '/src/assets/images/avatar_default.png';
    }

    return {
      decimalFormat,
      propTitle,
      getGoodImage,
      getGoodPrice,
      handleImageError,
    };
  },
});
</script>

<template>
  <div class="good-card">
    <van-image
      fit="contain"
      class="good-card-pic"
      :src="getGoodImage(good)"
      lazy-load
      placeholder="/src/assets/images/avatar_default.png"
      :fade="true"
      @error="handleImageError"
    />
    <div class="good-card-content">
      <div class="good-card-title">{{ good.name }}</div>
      <div class="good-card-desc">
        <div v-if="good.propertyList && good.propertyList.length" class="good-card-prop">
          {{ propTitle(good.propertyList) }}
        </div>
        <div v-else-if="good.specs && good.specs.length > 0" class="good-card-prop">
          {{ good.specs[0].specification }}
        </div>
      </div>
      <div class="good-card-bottom">
        <div class="good-card-price">
          <span class="good-card-price-symbol">¥</span>
          <span class="good-card-price-integer">{{ decimalFormat(getGoodPrice(good)) }}</span>
        </div>
        <div class="good-card-number">x{{ good.number || 1 }}</div>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.good {
  &-card {
    position: relative;
    box-sizing: border-box;
    padding: 8px 15px;
    display: flex;
    align-items: center;

    &-selected {
      margin-left: -10px;
      min-width: 40px;
      display: flex;
      justify-content: center;
    }

    &-pic {
      width: 90px;
      height: 90px;
      border-radius: 8px;
      margin-right: 10px;
      overflow: hidden;
    }

    &-content {
      min-width: 0;
      min-height: 90px;
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    &-bottom {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    &-title {
      font-size: 14px;
      line-height: 16px;
      color: var(--color-text-1);
      display: -webkit-box;
      overflow: hidden;
      text-overflow: ellipsis;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    &-desc {
      flex: 1;
      font-size: 12px;
      line-height: 20px;
      color: var(--color-text-3);
    }

    &-price {
      color: var(--color-text-1);
      &-symbol {
        font-size: 12px;
        margin-right: 2px;
      }

      &-integer {
        font-size: 18px;
        font-family: @font-family-price-integer;
      }
    }

    &-bottom {
      font-size: 12px;
      color: var(--color-text-3);
    }
  }
}
</style>
