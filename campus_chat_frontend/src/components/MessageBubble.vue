<template>
  <view class="message-row" :class="{ 'is-own': isOwn, 'is-recall': message.type === 'recall' }">
    <template v-if="message.type !== 'recall'">
      <view class="avatar-wrapper" v-if="!isOwn && showAvatar">
        <u-avatar :src="message.sender.avatar" size="40" shape="square"></u-avatar>
      </view>
      <view class="avatar-placeholder" v-else-if="!isOwn"></view>
      
      <view class="content-wrapper">
        <text v-if="!isOwn && showName" class="sender-name">{{ message.sender.name }}</text>
        
        <view class="bubble-container">
          <view v-if="message.type === 'text'" class="text-bubble" @longpress.stop="handleLongPress">
            <view v-if="message.reply && message.reply.text" class="reply-snippet">
              <text class="reply-user">{{ message.reply.username || '回复' }}</text>
              <text class="reply-text">{{ message.reply.text }}</text>
            </view>
            <text class="message-text">{{ message.content }}</text>
          </view>
          
          <view v-else-if="message.type === 'card' || message.type === 'image' || message.type === 'file'" class="card-bubble" @longpress.stop="handleLongPress">
            <attachment-card 
              :type="message.attachment.type" 
              :data="message.attachment"
            ></attachment-card>
          </view>
        </view>
      </view>
      
      <view class="avatar-wrapper" v-if="isOwn">
        <u-avatar :src="myAvatarSrc" size="40" shape="square"></u-avatar>
      </view>
    </template>

    <template v-else>
      <view class="recall-bubble">
        <text class="recall-text">{{ message.content }}</text>
      </view>
    </template>
  </view>
</template>

<script setup>

import { computed } from 'vue'
import AttachmentCard from './AttachmentCard.vue'
import CONFIG from '@/config.js'

const props = defineProps({
  message: {
    type: Object,
    required: true
  },
  isOwn: {
    type: Boolean,
    default: false
  },
  showAvatar: {
    type: Boolean,
    default: true
  },
  showName: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['longpress'])

const myAvatarSrc = computed(() => {
  try {
    const cache = uni.getStorageSync('userInfo') || {}
    const avatar = cache?.avatar
    if (!avatar) return '/static/logo.png'
    const text = String(avatar)
    if (text.startsWith('http') || text.startsWith('data:')) return text
    return CONFIG.IMG_BASE_URL + text
  } catch (e) {
    return '/static/logo.png'
  }
})

const handleLongPress = (e) => {
  const touch = Array.isArray(e?.touches) && e.touches.length > 0 ? e.touches[0] : null
  const detail = e?.detail ?? {}
  const x = detail?.x ?? touch?.clientX ?? touch?.pageX ?? 0
  const y = detail?.y ?? touch?.clientY ?? touch?.pageY ?? 0
  emit('longpress', { message: props.message, isOwn: props.isOwn, x, y })
}
</script>

<style lang="scss" scoped>
.message-row {
  display: flex;
  margin-bottom: 20px;
  padding: 0 15px;
  
  &.is-own {
    justify-content: flex-end;
    
    .content-wrapper {
      align-items: flex-end;
      margin-right: 10px;
      margin-left: 0;
      
      .text-bubble {
        background-color: #fff; // Own message also white as per design
        border-top-right-radius: 2px;
        border-top-left-radius: 8px;
      }
    }
  }

  &.is-recall {
    justify-content: center;
    padding: 0 20px;
    margin-bottom: 12px;
  }
  
  .avatar-wrapper {
    flex-shrink: 0;
  }
  
  .avatar-placeholder {
    width: 40px;
    flex-shrink: 0;
  }
  
  .content-wrapper {
    display: flex;
    flex-direction: column;
    margin-left: 10px;
    max-width: 70%;
    min-width: 0;
    
    .sender-name {
      font-size: 12px;
      color: #999;
      margin-bottom: 4px;
    }
    
    .bubble-container {
      min-width: 0;
      max-width: 100%;
    }

    .text-bubble {
      background-color: #fff;
      padding: 10px 14px;
      border-radius: 8px;
      border-top-left-radius: 2px;
      box-shadow: 0 1px 2px rgba(0,0,0,0.05);
      max-width: 100%;

      .reply-snippet {
        display: flex;
        align-items: center;
        padding-left: 10px;
        border-left: 2px solid rgba(60, 74, 128, 0.5);
        margin-bottom: 6px;
        max-width: 100%;
      }

      .reply-user {
        font-size: 12px;
        color: #3C4A80;
        margin-right: 6px;
        flex-shrink: 0;
      }

      .reply-text {
        font-size: 12px;
        color: #666;
        flex: 1;
        min-width: 0;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
      }
      
      .message-text {
        font-size: 16px;
        color: #333;
        line-height: 1.5;
        max-width: 100%;
        white-space: pre-wrap;
        word-break: break-all;
        overflow-wrap: anywhere;
        word-wrap: break-word;
        display: block;
      }
    }
    
    .card-bubble {
      // Cards handle their own styling
    }
  }

  .recall-bubble {
    background-color: rgba(0, 0, 0, 0.06);
    border-radius: 10px;
    padding: 6px 10px;
    max-width: 80%;
  }

  .recall-text {
    font-size: 12px;
    color: #666;
    max-width: 100%;
    white-space: pre-wrap;
    word-break: break-all;
    overflow-wrap: anywhere;
    word-wrap: break-word;
    display: block;
  }
}
</style>
