// postcss.config.js
module.exports = {
  plugins: {
    autoprefixer: {},
    // 只在开发移动端时启用，桌面端使用原始像素
    'postcss-px-to-viewport':
      process.env.NODE_ENV === 'production'
        ? {
            viewportWidth: 375,
            unitPrecision: 2,
            viewportUnit: 'vw',
            selectorBlackList: ['.ignore', /^body$/, '.hairlines', /^\.dp/, /^\.scroller/],
            minPixelValue: 1,
            mediaQuery: false,
          }
        : false,
  },
};
