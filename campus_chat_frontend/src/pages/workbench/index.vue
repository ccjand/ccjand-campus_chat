<template>
  <view class="workbench-page">
    <top-nav title="工作台" :show-default-icons="false"></top-nav>
    
    <view class="content">
      <view class="grid-container">
        <view class="grid-item" @click="navigateTo('/pages/workbench/leave/index')">
          <view class="icon-wrapper leave">
            <u-icon name="calendar-fill" size="32" color="#fff"></u-icon>
          </view>
          <text class="grid-text">请假</text>
        </view>

        <view class="grid-item" @click="navigateTo('/pages/workbench/leave/history')">
          <view class="icon-wrapper history">
            <u-icon name="clock-fill" size="32" color="#fff"></u-icon>
          </view>
          <text class="grid-text">请假历史</text>
        </view>
        
        <view class="grid-item" @click="navigateTo('/pages/workbench/checkin/index')">
          <view class="icon-wrapper checkin">
            <u-icon name="map-fill" size="32" color="#fff"></u-icon>
          </view>
          <text class="grid-text">签到</text>
        </view>
        
        <view class="grid-item" @click="navigateTo('/pages/workbench/exam/index')">
          <view class="icon-wrapper exam">
            <u-icon name="edit-pen-fill" size="32" color="#fff"></u-icon>
          </view>
          <text class="grid-text">期末考试</text>
        </view>
      </view>
    </view>
    
    <bottom-nav current="workbench" />
  </view>
</template>

<script setup>
import BottomNav from '@/components/BottomNav.vue'
import TopNav from '@/components/TopNav.vue'

const navigateTo = (url) => {
  uni.navigateTo({
    url: url
  })
}
</script>

<style lang="scss" scoped>
.workbench-page {
  min-height: 100vh;
  background-color: #F3F3F3;
  display: flex;
  flex-direction: column;
  
  .content {
    flex: 1;
    padding: 15px;
    padding-bottom: 70px; // Space for bottom nav
    
    .grid-container {
      display: flex;
      flex-wrap: wrap;
      background-color: #fff;
      border-radius: 12px;
      padding: 20px 10px;
      
      .grid-item {
        width: 33.33%;
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-bottom: 20px;
        
        .icon-wrapper {
          width: 50px;
          height: 50px;
          border-radius: 16px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-bottom: 8px;
          
          &.leave {
            background-color: #FF9800;
          }

          &.history {
            background-color: #607D8B;
          }
          
          &.checkin {
            background-color: #4CAF50;
          }
          
          &.exam {
            background-color: #2196F3;
          }
        }
        
        .grid-text {
          font-size: 14px;
          color: #333;
        }
      }
    }
  }
}
</style>
