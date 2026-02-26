<script>
export default {
  onLaunch: function () {
    console.log('App Launch')
  },
  onShow: function () {
    console.log('App Show')
  },
  onHide: function () {
    console.log('App Hide')
  },
}
</script>

<style lang="scss">
/* 强制覆盖 uview-plus 字体加载路径，使用 LocalUViewFont 避免与组件内部冲突 */
@font-face {
  font-family: 'uicon-iconfont';
  src: url('/static/uview-plus.woff?v=1') format('woff'),
       url('/static/uview-plus.ttf?v=1') format('truetype');
}

/* 强制重写 u-icon 字体引用 */
.u-icon__icon, .u-iconfont {
  font-family: 'uicon-iconfont' !important;
}

@import 'uview-plus/index.scss';
</style>
