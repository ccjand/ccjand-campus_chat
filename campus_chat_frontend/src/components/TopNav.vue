<template>
  <view class="top-nav">
    <view class="status-bar"></view>
    <view class="content">
      <view class="left">
        <template v-if="showAvatar">
          <view class="avatar-box">
            <u-avatar :src="userAvatar" size="40"></u-avatar>
            <view v-if="userHonor" class="honor-badge" :class="userHonorType">
              <text class="honor-text">{{ userHonor }}</text>
            </view>
          </view>
        </template>
        <view v-if="showBack" class="back-icon" @click="handleBack">
          <u-icon name="arrow-left" size="24" color="#000"></u-icon>
        </view>
        <text class="title">{{ title }}</text>
      </view>
      <view class="right">
        <slot name="right">
          <template v-if="showDefaultIcons">
            <view class="icon-item">
              <u-icon name="search" size="28" color="#333"></u-icon>
            </view>
            <view class="icon-item">
              <u-icon name="phone" size="28" color="#333"></u-icon>
            </view>
            <view class="icon-item">
              <u-icon name="plus-circle" size="28" color="#333"></u-icon>
            </view>
          </template>
        </slot>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import uIcon from 'uview-plus/components/u-icon/u-icon.vue'
import uAvatar from 'uview-plus/components/u-avatar/u-avatar.vue'
import CONFIG from '@/config.js'

const props = defineProps({
  title: {
    type: String,
    default: '消息'
  },
  showAvatar: {
    type: Boolean,
    default: true
  },
  showBack: {
    type: Boolean,
    default: false
  },
  showDefaultIcons: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['back'])

const userInfo = ref({})

const userAvatar = computed(() => {
  const avatar = userInfo.value?.avatar
  if (!avatar) return '/static/logo.png'
  const text = String(avatar)
  if (text.startsWith('http') || text.startsWith('data:')) return text
  return CONFIG.IMG_BASE_URL + text
})

const userHonor = computed(() => {
  const role = userInfo.value?.role
  const num = typeof role === 'number' ? role : Number(role)
  if (!Number.isNaN(num)) {
    if (num === 0) return '管理员'
    if (num === 2) return '教师'
    return '学生'
  }
  return role == null ? '' : String(role)
})

const userHonorType = computed(() => {
  const role = userInfo.value?.role
  const num = typeof role === 'number' ? role : Number(role)
  if (!Number.isNaN(num)) {
    if (num === 0) return 'admin'
    if (num === 2) return 'teacher'
    return 'student'
  }
  const text = role == null ? '' : String(role)
  if (text === '管理员') return 'admin'
  if (text === '教师') return 'teacher'
  if (text === '学生') return 'student'
  return ''
})

try {
  userInfo.value = uni.getStorageSync('userInfo') || {}
} catch (e) {
  userInfo.value = {}
}

const handleBack = () => {
  emit('back')
  // Also try default navigation back if listener doesn't prevent it
  // But here we just emit
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.top-nav {
  background-color: #F3F3F3; // Updated background
  padding-bottom: 0; // Removed bottom padding
  
  .status-bar {
    height: var(--status-bar-height);
    background-color: #F3F3F3; // Updated background
  }
  
  .content {
    display: flex;
    justify-content: space-between;
    align-items: center; // Vertical center alignment
    padding: 0 15px;
    height: 44px;
    
    .left {
      display: flex;
      align-items: center; // Vertical center alignment
      height: 100%;

        .avatar-box {
          position: relative;
          flex-shrink: 0;
        }

        .honor-badge {
          position: absolute;
          right: -6px;
          bottom: -6px;
          height: 18px;
          padding: 0 6px;
          border-radius: 999px;
          display: flex;
          align-items: center;
          justify-content: center;
          background-color: #8f8f8f;
          border: 2px solid #F3F3F3;

          &.teacher {
            background-color: #3C4A80;
          }
          &.student {
            background-color: #5C6BC0;
          }
          &.admin {
            background-color: #E67E22;
          }
        }

        .honor-text {
          color: #fff;
          font-size: 10px;
          line-height: 1;
        }
      
      .back-icon {
        margin-right: 10px;
        display: flex;
        align-items: center;
      }
      
      .title {
        font-size: 16px; // Reduced from 18px
        font-weight: bold;
        margin-left: 10px;
        color: #000;
      }
    }
    
    .right {
      display: flex;
      align-items: center; // Vertical center alignment
      height: 100%;
      
      .icon-item {
        margin-left: 15px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}
</style>
