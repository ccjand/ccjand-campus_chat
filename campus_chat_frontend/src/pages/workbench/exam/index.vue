<template>
  <view class="exam-page">
    <top-nav title="期末考试" :show-avatar="false" :show-back="true" :show-default-icons="false"></top-nav>
    
    <view class="content">
      <view class="exam-list">
        <view class="exam-item" v-for="(exam, index) in examList" :key="index">
          <view class="exam-header">
            <text class="course-name">{{ exam.course }}</text>
          </view>
          
          <view class="exam-info">
            <view class="info-row">
              <u-icon name="clock" size="16" color="#666"></u-icon>
              <text class="info-text">{{ exam.time }}</text>
            </view>
            <view class="info-row">
              <u-icon name="map" size="16" color="#666"></u-icon>
              <text class="info-text">{{ exam.location }}</text>
            </view>
            <view class="info-row">
              <u-icon name="account" size="16" color="#666"></u-icon>
              <text class="info-text">监考老师：{{ exam.teacher }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import TopNav from '@/components/TopNav.vue'

const examList = ref([
  {
    course: '高等数学 (上)',
    time: '2024-01-10 09:00 - 11:00',
    location: '教学楼 A101',
    teacher: '张老师'
  },
  {
    course: '大学英语 (III)',
    time: '2024-01-12 14:00 - 16:00',
    location: '教学楼 B203',
    teacher: '李老师'
  },
  {
    course: '计算机基础',
    time: '2024-01-15 09:00 - 11:00',
    location: '实验楼 C305',
    teacher: '王老师'
  },
  {
    course: '线性代数',
    time: '2024-01-18 14:00 - 16:00',
    location: '教学楼 A202',
    teacher: '赵老师'
  }
])
</script>

<style lang="scss" scoped>
.exam-page {
  min-height: 100vh;
  background-color: #F3F3F3;
  
  .content {
    padding: 15px;
    
    .exam-item {
      background-color: #fff;
      border-radius: 12px;
      padding: 15px;
      margin-bottom: 15px;
      box-shadow: 0 2px 6px rgba(0,0,0,0.05);
      
      .exam-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;
        padding-bottom: 10px;
        border-bottom: 1px solid #f5f5f5;
        
        .course-name {
          font-size: 18px;
          font-weight: bold;
          color: #333;
        }
        
        .status-tag {
          background-color: #E3F2FD;
          color: #2196F3;
          font-size: 12px;
          padding: 2px 8px;
          border-radius: 4px;
        }
      }
      
      .exam-info {
        .info-row {
          display: flex;
          align-items: center;
          margin-bottom: 8px;
          
          &:last-child {
            margin-bottom: 0;
          }
          
          .info-text {
            font-size: 14px;
            color: #666;
            margin-left: 8px;
          }
        }
      }
    }
  }
}
</style>
