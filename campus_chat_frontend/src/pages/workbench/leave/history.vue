<template>
  <view class="history-page">
    <top-nav title="请假历史" :show-avatar="false" :show-back="true" :show-default-icons="false"></top-nav>

    <scroll-view
      class="content"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <view class="list" v-if="items.length > 0">
        <view class="card" v-for="item in items" :key="item.id">
          <view class="row row-top">
            <text class="type">{{ leaveTypeText(item.leaveType) }}</text>
            <text class="status" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
          </view>

          <view class="row">
            <text class="label">时间</text>
            <text class="value">{{ formatRange(item.startTime, item.endTime) }}</text>
          </view>

          <view class="row">
            <text class="label">天数</text>
            <text class="value">{{ (item.durationDays || 0) + '天' }}</text>
          </view>

          <view class="row" v-if="item.courseName">
            <text class="label">学院/班级</text>
            <text class="value">{{ item.courseName }}</text>
          </view>

          <view class="row" v-if="item.reason">
            <text class="label">原因</text>
            <text class="value reason">{{ item.reason }}</text>
          </view>

          <view class="row" v-if="item.approverComment">
            <text class="label">审批意见</text>
            <text class="value reason">{{ item.approverComment }}</text>
          </view>

          <view class="row row-foot">
            <text class="time">{{ formatTime(item.createTime) }}</text>
          </view>
        </view>
      </view>

      <view class="empty" v-else>
        <text class="empty-text">{{ loading ? '加载中...' : '暂无请假记录' }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import TopNav from '@/components/TopNav.vue'
import request from '@/utils/request'

const items = ref([])
const loading = ref(false)
const refreshing = ref(false)

const fetchHistory = async () => {
  loading.value = true
  try {
    const data = await request({
      url: '/capi/leave/history',
      method: 'GET'
    })
    items.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const handleRefresh = async () => {
  refreshing.value = true
  await fetchHistory()
}

onShow(() => {
  fetchHistory()
})

const leaveTypeText = (type) => {
  const num = typeof type === 'number' ? type : Number(type)
  if (num === 1) return '病假'
  if (num === 2) return '事假'
  if (num === 3) return '其他'
  return '请假'
}

const statusText = (status) => {
  const num = typeof status === 'number' ? status : Number(status)
  if (num === 0) return '待审批'
  if (num === 1) return '已通过'
  if (num === 2) return '已驳回'
  if (num === 3) return '已撤销'
  return '未知状态'
}

const statusClass = (status) => {
  const num = typeof status === 'number' ? status : Number(status)
  if (num === 1) return 'pass'
  if (num === 2) return 'reject'
  if (num === 3) return 'cancel'
  return 'pending'
}

const pad2 = (n) => String(n).padStart(2, '0')

const formatTime = (ms) => {
  const num = typeof ms === 'number' ? ms : Number(ms)
  if (!num) return ''
  const d = new Date(num)
  const y = d.getFullYear()
  const m = pad2(d.getMonth() + 1)
  const day = pad2(d.getDate())
  const hh = pad2(d.getHours())
  const mm = pad2(d.getMinutes())
  return `${y}-${m}-${day} ${hh}:${mm}`
}

const formatRange = (startMs, endMs) => {
  const s = formatTime(startMs)
  const e = formatTime(endMs)
  if (!s && !e) return '-'
  if (!e) return s
  if (!s) return e
  return `${s} ~ ${e}`
}
</script>

<style lang="scss" scoped>
.history-page {
  min-height: 100vh;
  background-color: #F3F3F3;
}

.content {
  height: calc(100vh - 44px - var(--status-bar-height));
  padding: 12px 15px 20px;
  box-sizing: border-box;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card {
  background-color: #fff;
  border-radius: 16px;
  padding: 14px 14px 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 6px 0;
}

.row-top {
  padding-top: 0;
  align-items: center;
}

.row-foot {
  padding-bottom: 0;
  justify-content: flex-end;
}

.type {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  background-color: #f5f5f5;
  color: #666;
}

.status.pass {
  background-color: rgba(76, 175, 80, 0.12);
  color: #2e7d32;
}

.status.reject {
  background-color: rgba(244, 67, 54, 0.12);
  color: #c62828;
}

.status.cancel {
  background-color: rgba(158, 158, 158, 0.14);
  color: #616161;
}

.status.pending {
  background-color: rgba(255, 152, 0, 0.14);
  color: #ef6c00;
}

.label {
  font-size: 13px;
  color: #999;
  width: 80px;
  flex-shrink: 0;
}

.value {
  font-size: 13px;
  color: #333;
  flex: 1;
  text-align: right;
  line-height: 1.4;
}

.value.reason {
  white-space: pre-wrap;
}

.time {
  font-size: 12px;
  color: #bbb;
}

.empty {
  padding-top: 40px;
  display: flex;
  justify-content: center;
}

.empty-text {
  font-size: 14px;
  color: #999;
}
</style>
