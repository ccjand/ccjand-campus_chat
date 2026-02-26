<template>
  <view class="container">
    <!-- Search Bar -->
    <view class="search-container">
      <u-search placeholder="搜索" :show-action="false" bg-color="#F5F6FA" height="36"></u-search>
    </view>

    <!-- Content List -->
    <scroll-view scroll-y class="content-scroll">
      <!-- Departments -->
      <view class="section-list">
      </view>

      <!-- Contacts -->
      <view class="contact-list">
        <view v-if="loading" class="state">
          <text class="state-text">加载中...</text>
        </view>
        <view v-else-if="!contacts || contacts.length === 0" class="state">
          <text class="state-text">暂无联系人</text>
        </view>
        <view v-else v-for="(group, groupIndex) in contacts" :key="groupIndex">
          <!-- Section Header -->
          <view v-if="group.letter !== '#'" class="section-header">{{ group.letter }}</view>
          
          <!-- Contact Rows -->
          <view class="list-item" v-for="(contact, contactIndex) in group.list" :key="contactIndex" @click="openChat(contact)">
            <view class="item-left">
              <u-avatar :src="contact.avatar" size="40"></u-avatar>
              <text class="item-name">{{ contact.name }}</text>
              <view class="badge" :class="contact.role === '教师' ? 'teacher' : 'student'" v-if="contact.role">
                <text>{{ contact.role }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- Footer -->
      <view class="list-footer">
        <text>共{{ totalContacts }}人</text>
      </view>
    </scroll-view>
    
    <bottom-nav current="contacts"></bottom-nav>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import uSearch from 'uview-plus/components/u-search/u-search.vue'
import uAvatar from 'uview-plus/components/u-avatar/u-avatar.vue'
import BottomNav from '@/components/BottomNav.vue'
import request from '@/utils/request'
import CONFIG from '@/config.js'

const loading = ref(false)
const friendItems = ref([])

const normalizeAvatar = (avatar) => {
  if (avatar == null) return '/static/logo.png'
  const text = String(avatar).trim()
  if (!text || text === 'null' || text === 'undefined') return '/static/logo.png'
  if (text.startsWith('http') || text.startsWith('data:')) return text
  return CONFIG.IMG_BASE_URL + text.replace(/^\/+/, '')
}

const normalizeRoleText = (role) => {
  const num = typeof role === 'number' ? role : Number(role)
  if (!Number.isNaN(num)) {
    if (num === 0) return '管理员'
    if (num === 2) return '教师'
    return '学生'
  }
  return role == null ? '' : String(role)
}

const resolveLetter = (name) => {
  const text = String(name || '').trim()
  if (!text) return '#'
  const ch = text[0]
  return /[A-Za-z]/.test(ch) ? ch.toUpperCase() : '#'
}

const mapFriendToUi = (item) => {
  if (!item) return null
  const name = item.fullName || item.accountNumber || ''
  return {
    uid: item.uid,
    roomId: item.roomId,
    name,
    avatar: normalizeAvatar(item.avatar),
    role: normalizeRoleText(item.role)
  }
}

const contacts = computed(() => {
  const list = (friendItems.value || []).map(mapFriendToUi).filter(Boolean)
  list.sort((a, b) => String(a.name || '').localeCompare(String(b.name || ''), 'zh-Hans-CN'))

  const groupMap = new Map()
  list.forEach((c) => {
    const letter = resolveLetter(c.name)
    const arr = groupMap.get(letter) || []
    arr.push(c)
    groupMap.set(letter, arr)
  })

  const letters = Array.from(groupMap.keys())
  letters.sort((a, b) => {
    if (a === '#') return 1
    if (b === '#') return -1
    return a.localeCompare(b)
  })

  return letters.map((letter) => ({
    letter,
    list: groupMap.get(letter) || []
  }))
})

const totalContacts = computed(() => {
  return contacts.value.reduce((total, group) => total + (group?.list?.length || 0), 0)
})

const loadAllFriends = async () => {
  loading.value = true
  try {
    let cursor = null
    let offset = 1
    const all = []
    for (let i = 0; i < 200; i += 1) {
      const page = await request({
        url: '/capi/users/friend/page',
        method: 'POST',
        data: {
          pageSize: 60,
          cursor,
          offset
        }
      })
      const list = Array.isArray(page?.list) ? page.list : []
      list.forEach((item) => all.push(item))
      if (page?.isLast) break
      cursor = page?.cursor ?? null
      offset = page?.offset ?? 1
      if (!cursor) break
    }
    friendItems.value = all
  } catch (e) {
    friendItems.value = []
  } finally {
    loading.value = false
  }
}

const openChat = (contact) => {
  const roomId = contact?.roomId
  if (!roomId) {
    uni.showToast({ title: '缺少会话信息', icon: 'none' })
    return
  }
  const title = contact?.name ? encodeURIComponent(String(contact.name)) : ''
  uni.navigateTo({
    url: `/pages/chat/index?roomId=${encodeURIComponent(String(roomId))}&type=single&title=${title}`
  })
}

onShow(() => {
  loadAllFriends()
})
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #fff;
  position: relative;
  padding-top: var(--status-bar-height);
  box-sizing: border-box;
}

.header {
  background-color: #fff;
  .status-bar {
    height: var(--status-bar-height);
  }
  .nav-bar {
    height: 44px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 15px;
    
    .title {
      font-size: 18px;
      font-weight: bold;
      color: #000;
    }
  }
}

.search-container {
  padding: 10px 15px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  background-color: #fff;
  border-bottom: 1px solid #f5f5f5;
  
  .link {
    color: #4C8DFF;
    font-size: 14px;
  }
  
  .current {
    color: #333;
    font-size: 14px;
  }
  
  .separator {
    margin: 0 5px;
  }
}

.content-scroll {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 60px;
}

.state {
  padding: 60px 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.state-text {
  font-size: 14px;
  color: #999;
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px 12px 15px;
  background-color: #fff;
  border-bottom: 1px solid #f9f9f9;
  
  .item-left {
    display: flex;
    align-items: center;
    flex: 1;
    overflow: hidden; // Ensure flex shrinking works
    
    .avatar-placeholder {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
      flex-shrink: 0;
      
      &.dept-avatar {
        background-color: #A0CFFF;
      }
    }
    
    .item-name {
      font-size: 16px;
      color: #333;
      margin-left: 12px;
      flex-shrink: 0; // Prevent name from shrinking too much
    }
    
    .badge {
      margin-left: auto; // Push to right side of flex container
      margin-right: 10px; // Space from the right edge of item-left
      padding: 2px 8px;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      min-width: 36px; // Ensure consistent width
      
      &.teacher {
        background-color: #E6F1FF;
        border: 1px solid #B3D8FF;
        text {
          color: #4C8DFF;
        }
      }
      
      &.student {
        background-color: #F0F9EB;
        border: 1px solid #C2E7B0;
        text {
          color: #67C23A;
        }
      }
      
      text {
        font-size: 10px;
        text-align: center;
      }
    }
  }
}

.section-header {
  background-color: #F5F6FA;
  padding: 4px 15px;
  font-size: 12px;
  color: #999;
}

.list-footer {
  padding: 20px 0;
  text-align: center;
  background-color: #F5F6FA;
  
  text {
    font-size: 12px;
    color: #999;
  }
}
</style>
