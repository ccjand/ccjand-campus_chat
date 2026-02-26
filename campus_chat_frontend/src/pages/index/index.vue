<template>
  <view class="container">
    <view class="custom-header">
      <view class="status-bar"></view>
      <view class="main-bar">
        <view class="main-tabs">
          <view 
            class="tab-item" 
            :class="{ active: currentMainTab === 0 }"
            @click="currentMainTab = 0"
          >
            <text>消息中心</text>
            <view class="indicator" v-if="currentMainTab === 0"></view>
          </view>
          <view 
            class="tab-item" 
            :class="{ active: currentMainTab === 1 }"
            @click="currentMainTab = 1"
          >
            <text>平台消息</text>
            <view class="indicator" v-if="currentMainTab === 1"></view>
          </view>
        </view>

        <view class="header-actions">
          <view class="action-btn" @click.stop="openQuickMenu">
            <u-icon name="plus" color="#3C4A80" size="22"></u-icon>
          </view>
        </view>
      </view>
    </view>

    <view v-if="showQuickMenu" class="menu-mask" @click="closeQuickMenu">
      <view class="quick-menu" @click.stop>
        <view class="quick-menu-item" @click="handleQuickAction('add_friend')">
          <text class="quick-menu-text">添加好友</text>
        </view>
        <view class="quick-menu-item" @click="handleQuickAction('create_group')">
          <text class="quick-menu-text">创建群聊</text>
        </view>
        <view class="quick-menu-item" @click="handleQuickAction('join_group')">
          <text class="quick-menu-text">加入群聊</text>
        </view>
      </view>
    </view>
    
    <view v-show="currentMainTab === 0" class="tab-content">
      <scroll-view scroll-y class="content-scroll">
        <view class="message-scroll-body">
          <message-list v-if="messages && messages.length" :messages="messages"></message-list>
          <view v-else class="empty">
            <text class="empty-text">消息为空</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- Platform Messages Content -->
    <view v-show="currentMainTab === 1" class="tab-content platform-content">
      <scroll-view scroll-y class="content-scroll">
        <view class="platform-list">
          <view 
            class="platform-item" 
            v-for="(item, index) in platformMessages" 
            :key="index"
          >
            <view class="icon-wrapper" :style="{ backgroundColor: item.iconBgColor }">
              <u-icon :name="item.icon" color="#fff" size="24"></u-icon>
            </view>
            <view class="item-content">
              <view class="row-top">
                <text class="item-title">{{ item.title }}</text>
                <text class="item-time">{{ item.time }}</text>
              </view>
              <view class="row-bottom">
                <text class="item-desc">{{ item.desc }}</text>
                <view class="badge" v-if="item.count > 0">
                  <text>{{ item.count }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- Bottom Navigation -->
    <bottom-nav current="message" :unread-count="totalUnread"></bottom-nav>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onHide } from '@dcloudio/uni-app'
import uIcon from 'uview-plus/components/u-icon/u-icon.vue'
import MessageList from '@/components/MessageList.vue'
import BottomNav from '@/components/BottomNav.vue'
import request from '@/utils/request'
import imSocket from '@/utils/imSocket'
import dayjs from 'dayjs'

const currentMainTab = ref(0)
const messages = ref([])

const platformMessages = ref([])

const showQuickMenu = ref(false)
let removeWsListener = null
let refreshRecentTimer = null

const ACTIVE_ROOM_KEY = 'activeChatRoomId'

const totalUnread = computed(() => {
  const list = Array.isArray(messages.value) ? messages.value : []
  return list.reduce((sum, item) => sum + (Number(item?.unreadCount) > 0 ? Number(item.unreadCount) : 0), 0)
})

const toMsFromTimestampValue = (value) => {
  if (value == null) return null
  if (typeof value === 'number' && Number.isFinite(value)) return value
  if (typeof value === 'string') {
    const text = value.trim()
    if (!text) return null
    const d1 = dayjs(text)
    if (d1.isValid()) return d1.valueOf()
    const d2 = dayjs(text.replace('T', ' '))
    return d2.isValid() ? d2.valueOf() : null
  }
  if (Array.isArray(value) && value.length >= 3) {
    const [y, m, d, hh = 0, mm = 0, ss = 0, ns = 0] = value
    const ms = Math.floor(Number(ns || 0) / 1e6)
    const date = new Date(Number(y), Number(m) - 1, Number(d), Number(hh), Number(mm), Number(ss), ms)
    const out = date.getTime()
    return Number.isFinite(out) ? out : null
  }
  return null
}

const formatSessionTime = (ts) => {
  const ms = toMsFromTimestampValue(ts)
  if (!ms) return ''
  const t = dayjs(ms)
  if (!t.isValid()) return ''
  const now = dayjs()
  if (t.isSame(now, 'day')) return t.format('HH:mm')
  if (t.isSame(now, 'year')) return t.format('MM-DD')
  return t.format('YYYY-MM-DD')
}

const mapRecentContactToUi = (item) => {
  if (!item) return null
  const roomId = item.roomId ?? item.id
  if (roomId == null) return null
  const messageType = item.messageType === 'group' || item.messageType === 'single' ? item.messageType : 'single'
  return {
    id: roomId,
    roomId,
    messageType,
    name: item.name || '',
    avatar: item.avatar || '',
    summary: item.summary || '',
    unreadCount: Number(item.unreadCount || 0),
    timestamp: formatSessionTime(item.timestamp)
  }
}

const loadRecentContacts = async () => {
  try {
    const list = await request({
      url: '/capi/chat/contact/recent',
      method: 'POST',
      data: {}
    })
    messages.value = (Array.isArray(list) ? list : []).map(mapRecentContactToUi).filter(Boolean)
  } catch (e) {
    messages.value = []
  }
}

const extractSummaryFromWs = (data) => {
  const content = data?.message?.messageContent
  if (typeof content === 'string') return content
  if (content && typeof content === 'object') {
    const text = content.content
    if (typeof text === 'string') return text
  }
  return '新消息'
}

const scheduleRefreshRecent = (delayMs = 300) => {
  if (refreshRecentTimer) return
  refreshRecentTimer = setTimeout(async () => {
    refreshRecentTimer = null
    await loadRecentContacts()
  }, Math.max(0, Number(delayMs) || 0))
}

const handleWsPayload = (payload) => {
  if (!payload || typeof payload !== 'object') return
  if (payload.type !== 4) return
  const data = payload.data
  const rid = data?.message?.roomId
  if (rid == null) return

  const myUid = uni.getStorageSync('uid') ?? uni.getStorageSync('userInfo')?.uid
  const isSelf = myUid != null && data?.fromUser?.uid != null && String(myUid) === String(data.fromUser.uid)
  const activeRoom = uni.getStorageSync(ACTIVE_ROOM_KEY)
  const isActiveRoom = activeRoom != null && String(activeRoom) === String(rid)
  const shouldIncUnread = !isSelf && !isActiveRoom

  const idx = messages.value.findIndex((m) => String(m?.roomId ?? m?.id) === String(rid))
  if (idx < 0) {
    scheduleRefreshRecent()
    return
  }

  const prev = messages.value[idx]
  const nextUnread = shouldIncUnread ? Number(prev?.unreadCount || 0) + 1 : Number(prev?.unreadCount || 0)
  const updated = {
    ...prev,
    summary: extractSummaryFromWs(data),
    timestamp: formatSessionTime(data?.message?.sendTime),
    unreadCount: Math.max(0, nextUnread)
  }
  messages.value.splice(idx, 1)
  messages.value.unshift(updated)
}

const openQuickMenu = () => {
  showQuickMenu.value = !showQuickMenu.value
}

const closeQuickMenu = () => {
  showQuickMenu.value = false
}

const handleQuickAction = (type) => {
  closeQuickMenu()
  if (type === 'add_friend') {
    uni.showToast({ title: '添加好友', icon: 'none' })
    return
  }
  if (type === 'create_group') {
    uni.showToast({ title: '创建群聊', icon: 'none' })
    return
  }
  if (type === 'join_group') {
    uni.showToast({ title: '加入群聊', icon: 'none' })
  }
}

onShow(() => {
  loadRecentContacts()
  if (typeof removeWsListener !== 'function') {
    removeWsListener = imSocket.onMessage(handleWsPayload)
  }
})

onHide(() => {
  if (typeof removeWsListener === 'function') removeWsListener()
  removeWsListener = null
  if (refreshRecentTimer) clearTimeout(refreshRecentTimer)
  refreshRecentTimer = null
})
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #F5F6FA;
  
  .custom-header {
    background-color: #3C4A80; // Dark blue from image
    padding-bottom: 0;
    position: relative;

    .status-bar {
      height: var(--status-bar-height);
      background-color: #fff;
    }

    .main-bar {
      height: 44px;
      display: flex;
      align-items: center;
      background-color: #fff;
      overflow: hidden;
    }
    
    .main-tabs {
      flex: 1;
      height: 44px;
      display: flex;
      overflow: hidden;
      
      .tab-item {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        position: relative;
        font-size: 16px;
        color: #666;
        
        &.active {
          color: #3C4A80;
          font-weight: 500;
        }
        
        .indicator {
          position: absolute;
          bottom: 0;
          width: 40px;
          height: 3px;
          background-color: #3C4A80;
          border-radius: 2px;
        }
      }
    }

    .header-actions {
      width: 44px;
      height: 44px;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .action-btn {
      width: 44px;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .menu-mask {
    position: fixed;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    z-index: 999;
  }

  .quick-menu {
    position: absolute;
    right: 10px;
    top: calc(var(--status-bar-height) + 44px + 6px);
    width: 140px;
    background-color: #fff;
    border-radius: 10px;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
    overflow: hidden;
  }

  .quick-menu-item {
    height: 36px;
    padding: 0 12px;
    display: flex;
    align-items: center;
  }

  .quick-menu-item:active {
    background-color: #f5f6fa;
  }

  .quick-menu-text {
    font-size: 14px;
    color: #333;
  }
  
  .tab-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background-color: #F5F6FA;
  }
  
  .content-scroll {
    flex: 1;
    overflow-y: auto;
  }

  .message-scroll-body {
    min-height: 100%;
    background-color: #fff;
  }

  .empty {
    padding: 60px 0;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .empty-text {
    font-size: 14px;
    color: #999;
  }
  
  .platform-content {
    .search-box {
      padding: 10px 15px;
      background-color: #fff;
      margin-bottom: 10px;
    }
    
    .platform-list {
      padding: 12px 15px;
      
      .platform-item {
        background-color: #fff;
        border-radius: 20px;
        padding: 15px;
        display: flex;
        align-items: flex-start;
        margin-bottom: 12px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .icon-wrapper {
          width: 48px;
          height: 48px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 15px;
          flex-shrink: 0;
        }
        
        .item-content {
          flex: 1;
          
          .row-top {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
            
            .item-title {
              font-size: 16px;
              color: #333;
              font-weight: 500;
            }
            
            .item-time {
              font-size: 12px;
              color: #999;
            }
          }
          
          .row-bottom {
            display: flex;
            justify-content: space-between;
            align-items: center;
            
            .item-desc {
              font-size: 13px;
              color: #666;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
              max-width: 200px;
            }
            
            .badge {
              min-width: 18px;
              height: 18px;
              border-radius: 9px;
              background-color: #FF5A5A;
              display: flex;
              align-items: center;
              justify-content: center;
              padding: 0 5px;
              
              text {
                color: #fff;
                font-size: 11px;
                line-height: 1;
              }
            }
          }
        }
      }
    }
  }
}
</style>
