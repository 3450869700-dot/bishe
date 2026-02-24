// postcss.config.js
module.exports = {
  plugins: {
    autoprefixer: {},
    // 调整 viewportWidth 为电脑屏幕宽度
    'postcss-px-to-viewport': {
      viewportWidth: 1920, // 改为电脑屏幕宽度
      unitPrecision: 2,
      viewportUnit: 'vw',
      selectorBlackList: ['.ignore', /^body$/, '.hairlines', /^\.dp/, /^\.scroller/],
      minPixelValue: 1,
      mediaQuery: false,
    },
  },
};
