<template>
  <view class="container">
    <!-- Header Section -->
    <view class="header-section">
      <view class="user-info">
        <view class="avatar-wrapper">
          <image 
            :src="userInfo.avatar || '/static/logo.png'" 
            class="custom-avatar"
            mode="aspectFill"
          ></image>
          <view class="verified-badge" v-if="userInfo.isVerified">
            <u-icon name="checkmark" color="#fff" size="10"></u-icon>
          </view>
        </view>
        <view class="info-content">
          <view class="name-row">
            <text class="name">{{ userInfo.name }}</text>
            <view class="role-tag" :class="userInfo.roleType">
              <text>{{ userInfo.role }}</text>
            </view>
          </view>
          <text class="dept">{{ userInfo.department }}</text>
          <view class="id-row">
            <text class="label">工号：</text>
            <text class="value">{{ userInfo.id }}</text>
          </view>
        </view>
      </view>
      
      <!-- Stats/Quick Actions (Optional) -->
      <!-- <view class="stats-row">
        <view class="stat-item">
          <text class="num">12</text>
          <text class="label">待办</text>
        </view>
        <view class="stat-item">
          <text class="num">5</text>
          <text class="label">消息</text>
        </view>
        <view class="stat-item">
          <text class="num">89%</text>
          <text class="label">考勤率</text>
        </view>
      </view> -->
    </view>

    <!-- Menu List -->
    <view class="menu-list">
      <!-- Group 1: Account & Auth -->
      <view class="menu-group">

        <view class="menu-item" @click="handleNavigate('info')">
          <view class="item-left">
            <view class="icon-box purple">
              <u-icon name="account" color="#fff" size="20"></u-icon>
            </view>
            <text class="item-title">个人信息</text>
          </view>
          <u-icon name="arrow-right" color="#ccc" size="14"></u-icon>
        </view>
      </view>

      <!-- Group 2: Settings & Support -->
      <view class="menu-group">
        <view class="menu-item" @click="handleNavigate('password')">
          <view class="item-left">
            <view class="icon-box green">
              <u-icon name="lock-fill" color="#fff" size="20"></u-icon>
            </view>
            <text class="item-title">修改密码</text>
          </view>
          <u-icon name="arrow-right" color="#ccc" size="14"></u-icon>
        </view>
      </view>

      <!-- Logout Button -->
      <view class="logout-btn" @click="handleLogout">
        <text>退出登录</text>
      </view>
    </view>

    <bottom-nav current="my"></bottom-nav>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import uIcon from 'uview-plus/components/u-icon/u-icon.vue'
import BottomNav from '@/components/BottomNav.vue'
import request from '@/utils/request' // 引入封装的请求
import imSocket from '@/utils/imSocket'
import CONFIG from '@/config.js'

const userInfo = ref({})

const resolveRoleType = (role) => {
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
}

onShow(() => {
  const info = uni.getStorageSync('userInfo')
  if (info) {
    const roleType = resolveRoleType(info.role)
    
    // 处理头像路径
    let avatar = info.avatar
    if (avatar && !avatar.startsWith('http') && !avatar.startsWith('data:')) {
        avatar = CONFIG.IMG_BASE_URL + avatar
    }
    
    userInfo.value = { ...info, roleType, avatar }
  }
})

const handleNavigate = (path) => {
  if (path === 'info') {
    uni.navigateTo({ url: '/pages/my/info/index' })
  } else if (path === 'password') {
    uni.navigateTo({ url: '/pages/my/password/index' })
  }
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: async function (res) {
      if (res.confirm) {
        const cache = uni.getStorageSync('userInfo')
        const uid = cache?.uid ?? userInfo.value?.uid
        const header = {
          ...(uid != null ? { uid: String(uid) } : {})
        }

        try {
          await request({
            url: '/capi/users/logout',
            method: 'POST',
            header
          })
        } catch (e) {
          console.warn('Logout api failed:', e)
        }

        imSocket.disconnect()

        // 清除本地缓存
        uni.removeStorageSync('token')
        uni.removeStorageSync('uid')
        uni.removeStorageSync('userInfo')
        
        // 跳转回登录页
        uni.reLaunch({ url: '/pages/login/index' })
      }
    }
  })
}
</script>
<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #F5F6FA;
  padding-bottom: 80px; // Space for bottom nav
}

.header-section {
  background: linear-gradient(135deg, #3C4A80 0%, #5C6BC0 100%);
  padding: calc(var(--status-bar-height) + 20px) 20px 30px; // Extra top padding for status bar area
  border-bottom-left-radius: 24px;
  border-bottom-right-radius: 24px;
  color: #fff;
  
  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 25px;
    
    .avatar-wrapper {
      position: relative;
      margin-right: 15px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-radius: 50%;
      padding: 2px;
      
      .custom-avatar {
        width: 64px;
        height: 64px;
        border-radius: 50%;
        display: block; /* 消除图片底部间隙 */
      }
      
      .verified-badge {
        position: absolute;
        bottom: 0;
        right: 0;
        background-color: #4CAF50;
        width: 18px;
        height: 18px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 2px solid #fff;
      }
    }
    
    .info-content {
      flex: 1;
      
      .name-row {
        display: flex;
        align-items: center;
        margin-bottom: 4px;
        
        .name {
          font-size: 20px;
          font-weight: 600;
          margin-right: 10px;
        }
        
        .role-tag {
          padding: 2px 8px;
          border-radius: 10px;
          font-size: 10px;
          
          &.admin {
            background-color: rgba(255, 255, 255, 0.2);
            color: #fff;
          }

          &.teacher {
            background-color: rgba(255, 255, 255, 0.2);
            color: #fff;
          }
          
          &.student {
            background-color: rgba(76, 175, 80, 0.2);
            color: #81C784;
          }
        }
      }
      
      .dept {
        font-size: 14px;
        opacity: 0.9;
        margin-bottom: 4px;
        display: block;
      }
      
      .id-row {
        font-size: 12px;
        opacity: 0.7;
        
        .value {
          font-family: monospace;
        }
      }
    }
    
  }
  
  .stats-row {
    display: flex;
    justify-content: space-around;
    
    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .num {
        font-size: 18px;
        font-weight: 600;
        margin-bottom: 4px;
      }
      
      .label {
        font-size: 12px;
        opacity: 0.8;
      }
    }
  }
}

.menu-list {
  padding: 20px 15px;
  margin-top: -20px; // Overlap effect
  
  .menu-group {
    background-color: #fff;
    border-radius: 12px;
    margin-bottom: 15px;
    padding: 0 15px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
    
    .menu-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 0;
      border-bottom: 1px solid #f5f5f5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .item-left {
        display: flex;
        align-items: center;
        
        .icon-box {
          width: 32px;
          height: 32px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 12px;
          
          &.blue { background-color: #E3F2FD; :deep(.u-icon__icon) { color: #2196F3 !important; } }
          &.purple { background-color: #F3E5F5; :deep(.u-icon__icon) { color: #9C27B0 !important; } }
          &.green { background-color: #E8F5E9; :deep(.u-icon__icon) { color: #4CAF50 !important; } }
          &.orange { background-color: #FFF3E0; :deep(.u-icon__icon) { color: #FF9800 !important; } }
          &.gray { background-color: #F5F5F5; :deep(.u-icon__icon) { color: #757575 !important; } }
        }
        
        .item-title {
          font-size: 15px;
          color: #333;
        }
      }
      
      .item-right {
        display: flex;
        align-items: center;
        
        .status {
          font-size: 12px;
          margin-right: 8px;
          
          &.verified {
            color: #4CAF50;
          }
          
          &.unverified {
            color: #FF9800;
          }
        }
      }
    }
  }
  
  .logout-btn {
    background-color: #fff;
    border-radius: 12px;
    padding: 15px;
    text-align: center;
    color: #FF3B30;
    font-size: 15px;
    font-weight: 500;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  }
}
</style>
