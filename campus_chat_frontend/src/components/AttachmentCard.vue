<template>
  <view class="attachment-container">
    <!-- Approval Card -->
    <view v-if="type === 'approval'" class="attachment-card approval">
      <view class="content">
        <view class="icon-wrapper">
          <u-icon name="file-text" size="24" color="#666"></u-icon>
        </view>
        <view class="text-content">
          <text class="title">{{ data.title }}</text>
        </view>
      </view>
      <view class="footer">
        <u-icon name="desktop" size="14" color="#999"></u-icon>
        <text class="subtitle">{{ data.subtitle }}</text>
      </view>
    </view>
    
    <!-- Image Card -->
    <view v-else-if="type === 'image'" class="attachment-card image">
      <view class="image-poster">
        <image 
          :src="data.url" 
          mode="widthFix" 
          class="poster-img"
        ></image>
        <view class="description" v-if="data.isAtAll">
          <text>@所有人 产研小伙伴们，集团篮球俱乐部招新了，欢迎大家积极报名</text>
        </view>
      </view>
    </view>
    
    <!-- File Card -->
    <view v-else-if="type === 'file'" class="attachment-card file">
      <view class="file-content">
        <view class="file-info">
          <text class="file-name u-line-2">{{ data.title }}</text>
          <text class="file-size">{{ data.fileSize || '未知大小' }}</text>
        </view>
        <view class="file-icon">
          <u-icon name="file-text-fill" size="40" color="#26D06A"></u-icon>
        </view>
      </view>
      <view class="footer link-footer">
        <text class="link-text">{{ data.title }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>


const props = defineProps({
  type: {
    type: String,
    default: 'approval'
  },
  data: {
    type: Object,
    required: true
  }
})

const handleClick = () => {
  // Handle click event
  console.log('Card clicked', props.data)
}
</script>

<style lang="scss" scoped>
.attachment-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  max-width: 260px;
  
  // Approval Card Styles
  &.approval {
    .approval-content {
      display: flex;
      padding: 15px;
      
      .icon-wrapper {
        margin-right: 12px;
      }
      
      .title {
        font-size: 16px;
        color: #007aff;
        text-decoration: underline;
        font-weight: 500;
      }
    }
    
    .footer {
      display: flex;
      align-items: center;
      padding: 8px 15px;
      border-top: 1px solid #f5f5f5;
      
      .footer-text {
        font-size: 12px;
        color: #999;
        margin-left: 6px;
      }
    }
  }
  
  // Image Card Styles
  &.image {
    background-color: #fff;
    padding: 10px;
    
    .poster-img {
      width: 100%;
      border-radius: 4px;
      display: block;
    }
    
    .description {
      margin-top: 10px;
      font-size: 14px;
      color: #333;
      line-height: 1.5;
    }
  }
  
  // File Card Styles
  &.file {
    .file-content {
      display: flex;
      justify-content: space-between;
      padding: 15px;
      
      .file-info {
        flex: 1;
        margin-right: 10px;
        
        .file-name {
          font-size: 16px;
          color: #333;
          margin-bottom: 5px;
        }
        
        .file-size {
          font-size: 12px;
          color: #999;
        }
      }
    }
    
    .footer.link-footer {
      padding: 10px 15px;
      border-top: 1px solid #f5f5f5;
      
      .link-text {
        color: #007aff;
        font-size: 14px;
      }
    }
  }
}
</style>
