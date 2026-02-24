import { defineStore } from 'pinia';
import { store } from '@/store';
import { theme, tabBar } from '@/constants/modules/app';
import type { ITheme } from '@/constants/modules/app/types';
import { goodStorage, getClientInfo, deepClone, fromCamelCase } from '@/utils';
import { appendStyle } from '@/utils/web';

// Rainbow theme variables
let rainbowInterval: number | null = null;
const rainbowColors = [
  '#ff5179', // 粉色
  '#ffaa00', // 黄色
  '#17c964', // 青色
  '#0080ff', // 蓝色
  '#884cff', // 紫色
  '#ff7d00', // 橙色
];
let currentRainbowIndex = 0;

export interface AppStore {
  theme: NonNullable<ITheme>;
  tabBar: NonNullable<Recordable>;
}

export const useAppStore = defineStore({
  id: 'app',
  state: (): AppStore => ({
    theme: getLocalTheme(),
    tabBar,
  }),
  getters: {
    getTheme: (state) => state.theme,
    getTabBar: (state) => state.tabBar,
  },
  actions: {
    // Generate complementary color from primary color
    generateComplementaryColor(primaryColor: string): string {
      // Convert hex to RGB
      const hex = primaryColor.replace('#', '');
      const r = parseInt(hex.substring(0, 2), 16);
      const g = parseInt(hex.substring(2, 4), 16);
      const b = parseInt(hex.substring(4, 6), 16);

      // Create a light complementary color by increasing lightness
      const lighten = (value: number, percent: number) => Math.min(255, Math.round(value + (255 - value) * percent));
      const rLight = lighten(r, 0.8);
      const gLight = lighten(g, 0.8);
      const bLight = lighten(b, 0.8);

      // Convert back to hex
      return `#${((1 << 24) + (rLight << 16) + (gLight << 8) + bLight).toString(16).slice(1)}`;
    },

    // Update rainbow theme colors
    updateRainbowTheme() {
      currentRainbowIndex = (currentRainbowIndex + 1) % rainbowColors.length;
      const primaryColor = rainbowColors[currentRainbowIndex];
      const viceColor = this.generateComplementaryColor(primaryColor);

      const myTheme: ITheme = deepClone(this.theme);
      myTheme.colors.primary = primaryColor;
      myTheme.colors.vice = viceColor;

      // Generate theme variables with transition
      const colorVarList = Object.keys(myTheme.colors).map(
        (key) => `--color-${fromCamelCase(key, '-')}: ${myTheme.colors[key]};`,
      );

      // 生成RGB格式的主色变量
      if (myTheme.colors.primary) {
        // 将十六进制颜色转换为RGB
        const hex = myTheme.colors.primary.replace('#', '');
        const r = parseInt(hex.substring(0, 2), 16);
        const g = parseInt(hex.substring(2, 4), 16);
        const b = parseInt(hex.substring(4, 6), 16);
        colorVarList.push(`--color-primary-rgb: ${r}, ${g}, ${b};`);
      }
      const vanVarList = Object.keys(myTheme.vanThemeOverrides).map(
        (key) =>
          `--van-${fromCamelCase(key, '-')}: ${
            myTheme.colors[myTheme.vanThemeOverrides[key]] ?? myTheme.vanThemeOverrides[key]
          };`,
      );
      // Add transition for smooth color changes
      const transitionVar = `--color-transition: all 0.3s ease-in-out;`;

      const cssText = `:root { ${transitionVar}\n${[...colorVarList, ...vanVarList].join('\n')} }`;
      appendStyle(cssText, 'theme');

      this.theme = myTheme;
    },

    async updateTheme(payload: Recordable = {}) {
      const myTheme: ITheme = { ...deepClone(this.theme), ...payload };

      // 切换主题深/浅
      if (payload.mode === 'system') {
        myTheme.mode = getClientInfo().theme;
      }
      if (myTheme.mode === 'dark') {
        document.documentElement.classList.add('dark');
      } else {
        document.documentElement.classList.remove('dark');
      }

      // 生成主题变量
      const colorVarList = Object.keys(myTheme.colors).map(
        (key) => `--color-${fromCamelCase(key, '-')}: ${myTheme.colors[key]};`,
      );

      // 生成RGB格式的主色变量
      if (myTheme.colors.primary) {
        // 将十六进制颜色转换为RGB
        const hex = myTheme.colors.primary.replace('#', '');
        const r = parseInt(hex.substring(0, 2), 16);
        const g = parseInt(hex.substring(2, 4), 16);
        const b = parseInt(hex.substring(4, 6), 16);
        colorVarList.push(`--color-primary-rgb: ${r}, ${g}, ${b};`);
      }
      const vanVarList = Object.keys(myTheme.vanThemeOverrides).map(
        (key) =>
          `--van-${fromCamelCase(key, '-')}: ${
            myTheme.colors[myTheme.vanThemeOverrides[key]] ?? myTheme.vanThemeOverrides[key]
          };`,
      );

      const cssText = `:root { ${[...colorVarList, ...vanVarList].join('\n')} }`;
      appendStyle(cssText, 'theme');

      this.theme = myTheme;
      goodStorage.set('theme', myTheme);

      // Handle rainbow theme
      // Check if the theme is rainbow by checking its value in the palettes
      const isRainbow = payload.isRainbow || this.theme.isRainbow;

      // Clear any existing interval
      if (rainbowInterval) {
        clearInterval(rainbowInterval);
        rainbowInterval = null;
      }

      // Start rainbow effect if needed
      if (isRainbow) {
        rainbowInterval = window.setInterval(() => {
          this.updateRainbowTheme();
        }, 500); // Change color every 0.5 seconds for faster flow
      }
    },
    /**
     * 切换暗黑模式
     */
    toggleThemeMode() {
      this.updateTheme({
        mode: this.theme.mode === 'dark' ? 'light' : 'dark',
      });
    },
  },
});

export function useAppStoreWithOut() {
  return useAppStore(store);
}

function getLocalTheme() {
  const result = goodStorage.get('theme', theme);
  if (result.version === theme.version) {
    return result;
  } else {
    return theme;
  }
}
