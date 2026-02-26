<template>
  <view class="leave-page">
    <top-nav title="请假申请" :show-avatar="false" :show-back="true" :show-default-icons="false"></top-nav>
    
    <view class="content">
      <view class="form-card">
        <!-- Leave Type -->
        <view class="form-row" @click="showTypeSelect = true">
          <text class="label">请假类型</text>
          <view class="value-container">
            <text class="value" :class="{ placeholder: !leaveType }">{{ leaveType || '请选择' }}</text>
            <u-icon name="arrow-right" size="16" color="#999"></u-icon>
          </view>
        </view>

        <view class="form-row">
          <text class="label">学院</text>
          <view class="value-container">
            <text class="value">{{ userCollege || '-' }}</text>
          </view>
        </view>

        <view class="form-row" v-if="showUserClass">
          <text class="label">班级</text>
          <view class="value-container">
            <text class="value">{{ userClass || '-' }}</text>
          </view>
        </view>
        
        <!-- Start Time -->
        <view class="form-row" @click="openDatePicker('start')">
          <text class="label">开始时间</text>
          <view class="value-container">
            <text class="value">{{ startDate || '请选择' }}</text>
            <u-icon name="arrow-right" size="16" color="#999"></u-icon>
          </view>
        </view>
        
        <!-- End Time -->
        <view class="form-row" @click="openDatePicker('end')">
          <text class="label">结束时间</text>
          <view class="value-container">
            <text class="value">{{ endDate || '请选择' }}</text>
            <u-icon name="arrow-right" size="16" color="#999"></u-icon>
          </view>
        </view>
        
        <!-- Duration -->
        <view class="form-row">
          <text class="label">请假时长</text>
          <view class="value-container">
            <text class="value">共{{ duration }}天</text>
          </view>
        </view>
      </view>
      
      <view class="divider"></view>
      
      <!-- Reason -->
      <view class="form-card reason-card">
        <text class="label">请假理由</text>
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
        
        <!-- File List -->
        <view class="file-list" v-if="fileList.length > 0">
          <view class="file-item" v-for="(item, index) in fileList" :key="index">
            <view class="file-info">
               <u-icon :name="item.fileType === 2 ? 'photo' : 'file-text'" size="20" color="#666"></u-icon>
               <text class="file-name">{{ item.fileName }}</text>
            </view>
            <u-icon name="close-circle" size="20" color="#ff4d4f" @click="removeFile(index)"></u-icon>
          </view>
        </view>
      </view>
      
      <!-- Submit Button -->
      <view class="submit-btn-container">
        <u-button type="primary" text="提交申请" color="#007aff" @click="submit"></u-button>
      </view>
    </view>
    
    <!-- Type Picker -->
    <u-picker 
      :show="showTypeSelect" 
      :columns="typeColumns" 
      @confirm="confirmType" 
      @cancel="showTypeSelect = false"
    ></u-picker>
    
    <!-- Datetime Picker -->
    <u-datetime-picker
      :show="showDatePicker"
      v-model="pickerValue"
      mode="datetime"
      @confirm="confirmDate"
      @cancel="showDatePicker = false"
    ></u-datetime-picker>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import TopNav from '@/components/TopNav.vue'
import request from '@/utils/request'
import CONFIG from '@/config.js'

const BASE_URL = CONFIG.API_BASE_URL

const userInfo = ref({})

const leaveType = ref('')
const startDate = ref('')
const endDate = ref('')
const reason = ref('')
const fileList = ref([])
const showTypeSelect = ref(false)
const showDatePicker = ref(false)
const pickerValue = ref(Number(new Date()))
const dateType = ref('') // 'start' or 'end'

const typeColumns = ref([['病假', '事假', '其他']])
const typeMap = {
  '病假': 1,
  '事假': 2,
  '其他': 3
}

const duration = computed(() => {
  if (!startDate.value || !endDate.value) return 0
  // Parse date strings like "2024-01-04 12:00:00"
  // Note: Safari might need replace(/-/g, '/')
  const start = new Date(startDate.value.replace(/-/g, '/'))
  const end = new Date(endDate.value.replace(/-/g, '/'))
  
  const diffTime = end - start
  if (diffTime <= 0) return 0
  
  // Calculate days:
  // < 24h = 1 day
  // > 24h & < 48h = 2 days
  // etc.
  const hours = diffTime / (1000 * 60 * 60)
  const days = Math.ceil(hours / 24)
  
  return days > 0 ? days : 1
})

const confirmType = (e) => {
  leaveType.value = e.value[0]
  showTypeSelect.value = false
}

const openDatePicker = (type) => {
  dateType.value = type
  showDatePicker.value = true
  // Set picker value to current selection or now
  if (type === 'start' && startDate.value) {
    pickerValue.value = new Date(startDate.value.replace(/-/g, '/')).getTime()
  } else if (type === 'end' && endDate.value) {
    pickerValue.value = new Date(endDate.value.replace(/-/g, '/')).getTime()
  } else {
    pickerValue.value = Number(new Date())
  }
}

const confirmDate = (e) => {
  const date = new Date(e.value)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  
  const formattedDate = `${year}-${month}-${day} ${hour}:${minute}`
  
  if (dateType.value === 'start') {
    startDate.value = formattedDate
  } else {
    endDate.value = formattedDate
  }
  showDatePicker.value = false
}

const uploadFile = (filePath, type, fileName, fileSize) => {
  const token = uni.getStorageSync('token')
  uni.showLoading({ title: '上传中' })
  uni.uploadFile({
    url: BASE_URL + '/file/upload',
    filePath: filePath,
    name: 'file',
    header: {
      'Authorization': token ? `Bearer ${token}` : '',
      'terminalType': '4'
    },
    success: (uploadRes) => {
      uni.hideLoading()
      try {
        const data = JSON.parse(uploadRes.data)
        if (data.success) {
          fileList.value.push({
            fileType: type,
            fileUrl: data.data.fileUrl,
            fileName: fileName || '未知文件',
            fileSize: fileSize || 0
          })
        } else {
          uni.showToast({ title: '上传失败: ' + data.errMsg, icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '上传响应解析失败', icon: 'none' })
      }
    },
    fail: (e) => {
      uni.hideLoading()
      uni.showToast({ title: '上传出错', icon: 'none' })
    }
  })
}

const handleUploadFile = () => {
  // #ifdef H5
  uni.chooseFile({
    count: 1,
    extension: ['.pdf', '.doc', '.docx', '.txt'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      const file = res.tempFiles[0]
      uploadFile(tempFilePath, 1, file.name, file.size)
    }
  })
  // #endif
  // #ifdef MP-WEIXIN
  wx.chooseMessageFile({
    count: 1,
    type: 'file',
    success: (res) => {
      const tempFilePath = res.tempFiles[0].path
      const file = res.tempFiles[0]
      uploadFile(tempFilePath, 1, file.name, file.size)
    }
  })
  // #endif
  // #ifndef H5 || MP-WEIXIN
  uni.showToast({ title: '当前环境不支持选择文件，请使用图片', icon: 'none' })
  // #endif
}

const handleUploadImage = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      const file = res.tempFiles[0]
      uploadFile(tempFilePath, 2, file.name || 'image.jpg', file.size)
    }
  })
}

const removeFile = (index) => {
  fileList.value.splice(index, 1)
}

const isTeacher = computed(() => {
  const role = userInfo.value?.role
  if (typeof role === 'number') return role === 2
  return String(role || '').includes('教师')
})

const userCollege = computed(() => {
  return userInfo.value?.department || userInfo.value?.college || userInfo.value?.collegeName || ''
})

const userClass = computed(() => {
  if (isTeacher.value) return ''
  return userInfo.value?.className || userInfo.value?.class || userInfo.value?.clazzName || userInfo.value?.clazz || ''
})

const showUserClass = computed(() => !isTeacher.value)

const loadUserInfo = () => {
  const info = uni.getStorageSync('userInfo')
  userInfo.value = info && typeof info === 'object' ? info : {}
}

onShow(() => {
  loadUserInfo()
})

const submit = async () => {
  if (!leaveType.value || !startDate.value || !endDate.value || !reason.value) {
    uni.showToast({
      title: '请填写完整信息',
      icon: 'none'
    })
    return
  }
  
  try {
    uni.showLoading({ title: '提交中' })
    await request({
        url: '/capi/leave/submit',
        method: 'POST',
        data: {
            leaveType: typeMap[leaveType.value],
            startTime: new Date(startDate.value.replace(/-/g, '/')).getTime(),
            endTime: new Date(endDate.value.replace(/-/g, '/')).getTime(),
            className: userClass.value || null,
            reason: reason.value,
            attachments: fileList.value
        }
    })
    uni.hideLoading()
    uni.showToast({
        title: '提交成功',
        icon: 'success'
    })
    
    // Clear form
    leaveType.value = ''
    startDate.value = ''
    endDate.value = ''
    reason.value = ''
    fileList.value = []
    
    setTimeout(() => {
        uni.navigateBack()
    }, 1500)
  } catch (e) {
    uni.hideLoading()
    // Error already handled in request.js
  }
}
</script>

<style lang="scss" scoped>
.leave-page {
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
        border-bottom: 1px solid #f5f5f5;
        
        &:last-child {
          border-bottom: none;
        }
        
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

      .file-list {
        margin-top: 15px;
        .file-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 10px;
          background: #f9f9f9;
          border-radius: 4px;
          margin-bottom: 8px;
          .file-info {
             display: flex;
             align-items: center;
             .file-name {
                margin-left: 8px;
                font-size: 14px;
                color: #333;
                max-width: 200px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
             }
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
