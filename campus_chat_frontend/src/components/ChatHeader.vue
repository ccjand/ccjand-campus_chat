<template>
  <view class="chat-header">
    <view class="status-bar"></view>
    <view class="content">
      <view class="left" @click="handleBack">
        <u-icon name="arrow-left" size="24" color="#000"></u-icon>
        <view class="title-container">
          <text class="title">{{ title }}</text>
          <text v-if="count" class="count">({{ count }})</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import uIcon from 'uview-plus/components/u-icon/u-icon.vue'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  count: {
    type: [Number, String],
    default: ''
  },
  type: {
    type: String,
    default: 'single' // single or group
  }
})

const emit = defineEmits(['back'])

const handleBack = () => {
  emit('back')
}
</script>

<style lang="scss" scoped>
.chat-header {
  background-color: transparent; // Allow background to show through or match page bg
  padding-bottom: 10px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  
  // Updated background color
  background-color: rgba(243, 243, 243, 0.95); // #F3F3F3 with slight transparency
  backdrop-filter: blur(10px);
  
  .status-bar {
    height: var(--status-bar-height);
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
      
      .title-container {
        display: flex;
        align-items: center;
        margin-left: 10px;
        
        .title {
          font-size: 16px; // Reduced from 18px
          font-weight: bold;
          color: #000;
        }
        
        .count {
          font-size: 16px; // Reduced from 18px
          font-weight: bold;
          color: #000;
          margin-left: 4px;
        }
      }
    }
  }
}
</style>
