<template>
  <view class="supplement-page">
    <top-nav title="补签申请" :show-avatar="false" :show-back="true" :show-default-icons="false"></top-nav>
    
    <view class="content">
      <view class="form-card">
        <!-- Class Selection -->
        <view class="form-row" @click="showClassSelect = true">
          <text class="label">课程班级</text>
          <view class="value-container">
            <text class="value" :class="{ placeholder: !selectedClass }">{{ selectedClass || '请选择' }}</text>
            <u-icon name="arrow-right" size="16" color="#999"></u-icon>
          </view>
        </view>
      </view>
      
      <view class="divider"></view>
      
      <!-- Reason -->
      <view class="form-card reason-card">
        <text class="label">补签理由</text>
        <textarea 
          class="reason-input" 
          placeholder="请输入" 
          placeholder-style="color: #999"
          v-model="reason"
        ></textarea>
      </view>
      
      <!-- Attachment -->
      <view class="form-card attachment-card">
        <text class="label">附件上传</text>
        <view class="upload-btns-container">
          <view class="upload-btn file-btn" @click="handleUploadFile">
            <u-icon name="file-text-fill" size="24" color="#fff"></u-icon>
            <text class="btn-text">上传文件</text>
          </view>
          <view class="upload-btn image-btn" @click="handleUploadImage">
            <u-icon name="photo-fill" size="24" color="#fff"></u-icon>
            <text class="btn-text">上传图片</text>
          </view>
        </view>
      </view>
      
      <!-- Submit Button -->
      <view class="submit-btn-container">
        <u-button type="primary" text="提交申请" color="#007aff" @click="submit"></u-button>
      </view>
    </view>
    
    <!-- Class Picker -->
    <u-picker 
      :show="showClassSelect" 
      :columns="classColumns" 
      @confirm="confirmClass" 
      @cancel="showClassSelect = false"
    ></u-picker>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import TopNav from '@/components/TopNav.vue'

const selectedClass = ref('')
const reason = ref('')
const showClassSelect = ref(false)

const classColumns = ref([['高等数学', '大学英语', '计算机基础', '线性代数']])

const confirmClass = (e) => {
  selectedClass.value = e.value[0]
  showClassSelect.value = false
}

const handleUploadFile = () => {
  uni.showToast({
    title: '上传文件功能开发中',
    icon: 'none'
  })
}

const handleUploadImage = () => {
  uni.showToast({
    title: '上传图片功能开发中',
    icon: 'none'
  })
}

const submit = () => {
  if (!selectedClass.value) {
    uni.showToast({
      title: '请选择课程班级',
      icon: 'none'
    })
    return
  }
  
  if (!reason.value) {
    uni.showToast({
      title: '请输入补签理由',
      icon: 'none'
    })
    return
  }
  
  uni.showToast({
    title: '提交成功',
    icon: 'success'
  })
  
  // Clear form
  selectedClass.value = ''
  reason.value = ''
  
  setTimeout(() => {
    uni.navigateBack()
  }, 1500)
}
</script>

<style lang="scss" scoped>
.supplement-page {
  min-height: 100vh;
  background-color: #F3F3F3;
  
  .content {
    padding-top: 10px;
    
    .form-card {
      background-color: #fff;
      padding: 0 15px;
      
      &.reason-card, &.attachment-card {
        padding: 15px;
        margin-top: 10px;
      }
      
      .form-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px 0;
        
        .label {
          font-size: 16px;
          color: #333;
          font-weight: 500;
        }
        
        .value-container {
          display: flex;
          align-items: center;
          
          .value {
            font-size: 16px;
            color: #333;
            margin-right: 8px;
            
            &.placeholder {
              color: #999;
            }
          }
        }
      }
      
      .label {
        font-size: 16px;
        color: #333;
        font-weight: 500;
        margin-bottom: 10px;
        display: block;
      }
      
      .reason-input {
        width: 100%;
        height: 100px;
        font-size: 16px;
        line-height: 1.5;
      }

      .upload-btns-container {
        display: flex;
        justify-content: space-between;
        margin-top: 10px;
        
        .upload-btn {
          flex: 1;
          height: 50px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin: 0 5px;
          
          &:first-child {
            margin-left: 0;
          }
          
          &:last-child {
            margin-right: 0;
          }
          
          .btn-text {
            color: #fff;
            font-size: 14px;
            margin-left: 6px;
          }
          
          &.file-btn {
            background-color: #FF9800;
          }
          
          &.image-btn {
            background-color: #9C27B0;
          }
        }
      }
    }
    
    .divider {
      height: 10px;
      background-color: #F3F3F3;
    }
    
    .submit-btn-container {
      margin-top: 30px;
      padding: 0 15px;
    }
  }
}
</style>