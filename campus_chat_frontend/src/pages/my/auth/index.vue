<template>
  <view class="container">
    <view class="auth-card">
      <view class="status-icon">
        <u-icon name="checkmark-circle-fill" color="#4CAF50" size="80"></u-icon>
        <text class="status-text">已认证</text>
      </view>
      
      <view class="info-list">
        <view class="list-item">
          <text class="label">姓名</text>
          <text class="value">{{ userInfo.name }}</text>
        </view>
        <view class="list-item">
          <text class="label">职责</text>
          <text class="value">{{ userInfo.role }}</text>
        </view>
        <view class="list-item">
          <text class="label">学院</text>
          <text class="value">{{ userInfo.department }}</text>
        </view>
        <view class="list-item">
          <text class="label">工号</text>
          <text class="value">{{ userInfo.id }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const userInfo = ref({
  name: '李明哲',
  role: '教师',
  department: '计算机科学与技术学院',
  id: 'T2023001'
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #F5F6FA;
  padding: 20px 15px;
}

.auth-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 30px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  
  .status-icon {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 40px;
    
    .status-text {
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin-top: 15px;
    }
  }
  
  .info-list {
    .list-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px 0;
      border-bottom: 1px solid #f5f5f5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .label {
        font-size: 15px;
        color: #666;
      }
      
      .value {
        font-size: 15px;
        color: #333;
        font-weight: 500;
      }
    }
  }
}
</style>