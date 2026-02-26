<template>
  <view class="message-item" @click="handleClick">
    <view class="avatar-container">
      <image 
        :src="avatarSrc" 
        class="custom-avatar"
        mode="aspectFill"
      ></image>
      <view v-if="message.unreadCount > 0" class="badge">
        <text class="badge-text">{{ message.unreadCount }}</text>
      </view>
    </view>
    
    <view class="content-container">
      <view class="header">
        <text class="name">{{ message.name }}</text>
        <text class="time">{{ message.timestamp }}</text>
      </view>
      <view class="footer">
        <text class="summary u-line-1">{{ message.summary }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import CONFIG from '@/config.js'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

// 计算头像地址，处理相对路径和全路径
const avatarSrc = computed(() => {
  const avatar = props.message.avatar
  if (!avatar) return '/static/logo.png' // 默认头像
  
  // 如果是 http 开头，说明是全路径，直接使用
  if (avatar.startsWith('http')) {
    return avatar
  }
  
  // 否则拼接基础路径
  return CONFIG.IMG_BASE_URL + avatar
})

const handleClick = () => {
  const rid = props.message.roomId ?? props.message.id
  if (rid == null) return
  const type = props.message.messageType || 'single'
  const title = props.message.name ? encodeURIComponent(String(props.message.name)) : ''
  uni.navigateTo({
    url: `/pages/chat/index?roomId=${encodeURIComponent(String(rid))}&type=${encodeURIComponent(String(type))}&title=${title}`
  })
}
</script>

<style lang="scss" scoped>
.message-item {
  display: flex;
  padding: 12px 15px;
  background-color: #fff;
  border-bottom: 1px solid #f5f5f5;
  
  &:active {
    background-color: #f9f9f9;
  }
  
  .avatar-container {
    position: relative;
    margin-right: 12px;
    width: 48px;
    height: 48px;
    
    .custom-avatar {
      width: 48px;
      height: 48px;
      border-radius: 4px; /* square shape */
      background-color: #f0f0f0;
    }
    
    .badge {
      position: absolute;
      top: -5px;
      right: -5px;
      background-color: #ff3b30;
      border-radius: 10px;
      min-width: 18px;
      height: 18px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 4px;
      z-index: 1;
      
      .badge-text {
        color: #fff;
        font-size: 10px;
        line-height: 1;
      }
    }
  }
  
  .content-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    overflow: hidden;
    
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;
      
      .name {
        font-size: 16px;
        font-weight: 500;
        color: #333;
      }
      
      .time {
        font-size: 12px;
        color: #999;
      }
    }
    
    .footer {
      display: flex;
      
      .summary {
        font-size: 13px;
        color: #888;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        flex: 1;
        width: 0; // Important for text-overflow to work in flex item
      }
    }
  }
}
</style>
