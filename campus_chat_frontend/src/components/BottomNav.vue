<template>
  <view class="bottom-nav">
    <view 
      class="nav-item" 
      :class="{ active: current === 'message' }"
      @click="navigateTo('/pages/index/index')"
    >
      <view class="icon-container">
        <u-icon :name="current === 'message' ? 'chat-fill' : 'chat'" size="24" :color="current === 'message' ? '#3C4A80' : '#999'"></u-icon>
        <view v-if="badgeText" class="badge">{{ badgeText }}</view>
      </view>
      <text class="label">消息</text>
    </view>
    
    <view 
      class="nav-item" 
      :class="{ active: current === 'contacts' }"
      @click="navigateTo('/pages/contacts/index')"
    >
      <u-icon :name="current === 'contacts' ? 'account-fill' : 'account'" size="24" :color="current === 'contacts' ? '#3C4A80' : '#999'"></u-icon>
      <text class="label">通讯录</text>
    </view>
    
    <view 
      class="nav-item" 
      :class="{ active: current === 'workbench' }"
      @click="navigateTo('/pages/workbench/index')"
    >
      <u-icon :name="current === 'workbench' ? 'grid-fill' : 'grid'" size="24" :color="current === 'workbench' ? '#3C4A80' : '#999'"></u-icon>
      <text class="label">工作台</text>
    </view>
    
    <view 
      class="nav-item" 
      :class="{ active: current === 'my' }"
      @click="navigateTo('/pages/my/index')"
    >
      <u-icon :name="current === 'my' ? 'account-fill' : 'account'" size="24" :color="current === 'my' ? '#3C4A80' : '#999'"></u-icon>
      <text class="label">我的</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import uIcon from 'uview-plus/components/u-icon/u-icon.vue'

const props = defineProps({
  current: {
    type: String,
    default: 'message'
  },
  unreadCount: {
    type: [Number, String],
    default: 0
  }
})

const badgeText = computed(() => {
  const n = Number(props.unreadCount || 0)
  if (!Number.isFinite(n) || n <= 0) return ''
  if (n > 99) return '99+'
  return String(Math.floor(n))
})

const navigateTo = (url) => {
  uni.reLaunch({
    url
  })
}
</script>

<style lang="scss" scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50px;
  background-color: #fff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  border-top: 1px solid #eee;
  padding-bottom: env(safe-area-inset-bottom);
  box-sizing: content-box;
  z-index: 100;
  
  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    flex: 1;
    height: 100%;
    
    &.active {
      .label {
        color: #3C4A80;
      }
    }
    
    .label {
      font-size: 10px;
      margin-top: 2px;
      color: #999;
    }
    
    .icon-container {
      position: relative;
      
      .badge {
        position: absolute;
        top: -2px;
        right: -8px;
        background-color: #ff3b30;
        color: #fff;
        font-size: 9px;
        padding: 0 3px;
        border-radius: 8px;
        min-width: 14px;
        text-align: center;
        line-height: 14px;
      }
    }
  }
}
</style>
