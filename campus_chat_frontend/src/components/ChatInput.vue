<template>
  <view class="chat-input" :style="rootStyle">
    <view v-if="reply" class="reply-bar">
      <view class="reply-main">
        <text class="reply-title">回复</text>
        <text class="reply-text u-line-1">{{ reply.text }}</text>
      </view>
      <text class="reply-close" @click="handleClearReply">×</text>
    </view>

    <view class="input-row">
      <view class="left-icons">
        <view class="icon-btn">
          <u-icon name="pause-circle" size="28" color="#666"></u-icon>
        </view>
      </view>
      <view class="input-container">
        <textarea
          class="input-field" 
          placeholder="请输入内容" 
          placeholder-style="color: #999"
          confirm-type="send"
          :adjust-position="false"
          :cursor-spacing="0"
          @confirm="handleSend"
          @linechange="handleLineChange"
          v-model="inputValue"
          auto-height
        />
      </view>
      <view class="right-icons">
        <view class="icon-btn">
          <u-icon name="plus-circle" size="28" color="#666"></u-icon>
        </view>
        <view class="send-btn" :class="{ disabled: !canSend }" @click="handleSend">
          <text class="send-text">发送</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, getCurrentInstance, nextTick, onMounted, ref, watch } from 'vue'
import uIcon from 'uview-plus/components/u-icon/u-icon.vue'

const props = defineProps({
  reply: {
    type: Object,
    default: null
  },
  bottomOffset: {
    type: Number,
    default: 0
  }
})

const inputValue = ref('')
const emit = defineEmits(['send', 'clear-reply', 'height-change'])
const canSend = computed(() => Boolean(inputValue.value && inputValue.value.trim()))
const rootStyle = computed(() => ({
  bottom: `${Math.max(0, Number(props.bottomOffset) || 0)}px`
}))
const instance = getCurrentInstance()

const handleSend = () => {
  if (!inputValue.value.trim()) return
  emit('send', inputValue.value)
  inputValue.value = ''
  nextTick(() => {
    measureHeightAndEmit()
  })
}

const handleClearReply = () => {
  emit('clear-reply')
}

const measureHeightAndEmit = () => {
  const inst = instance?.proxy
  if (!inst) return
  const query = uni.createSelectorQuery().in(inst)
  query.select('.chat-input').boundingClientRect()
  query.exec((res) => {
    const rect = res?.[0]
    const h = Number(rect?.height)
    if (!Number.isFinite(h) || h <= 0) return
    emit('height-change', h)
  })
}

const handleLineChange = (e) => {
  const h = Number(e?.detail?.height)
  if (Number.isFinite(h) && h > 0) {
    nextTick(() => {
      measureHeightAndEmit()
    })
    return
  }
  nextTick(() => {
    measureHeightAndEmit()
  })
}

onMounted(() => {
  nextTick(() => {
    measureHeightAndEmit()
  })
})

watch(
  () => props.reply,
  () => {
    nextTick(() => {
      measureHeightAndEmit()
    })
  }
)
</script>

<style lang="scss" scoped>
.chat-input {
  display: flex;
  flex-direction: column;
  background-color: #f7f7f7;
  border-top: 1px solid #eee;
  padding-bottom: calc(8px + env(safe-area-inset-bottom)); // Reduced bottom padding
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;

  .reply-bar {
    padding: 8px 15px 0 15px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .reply-main {
      flex: 1;
      min-width: 0;
      background-color: #fff;
      border-radius: 8px;
      padding: 6px 10px;
      display: flex;
      align-items: center;
    }

    .reply-title {
      font-size: 12px;
      color: #3C4A80;
      margin-right: 8px;
      flex-shrink: 0;
    }

    .reply-text {
      font-size: 12px;
      color: #666;
      min-width: 0;
      flex: 1;
    }

    .reply-close {
      width: 28px;
      height: 28px;
      line-height: 28px;
      text-align: center;
      font-size: 18px;
      color: #999;
      margin-left: 8px;
    }
  }

  .input-row {
    display: flex;
    align-items: flex-end;
    padding: 8px 15px; // Reduced vertical padding
  }
  
  .left-icons, .right-icons {
    display: flex;
    align-items: flex-end;
    height: 100%; // Ensure vertical alignment
    
    .icon-btn {
      padding: 0 5px; // Adjusted padding
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  
  .right-icons {
    .icon-btn {
      margin-left: 8px;
    }
  }
  
  .input-container {
    flex: 1;
    margin: 0 8px;
    background-color: #fff;
    border-radius: 8px; // More rounded corners
    padding: 6px 12px; // Adjusted padding
    display: flex;
    align-items: stretch;
    
    .input-field {
      width: 100%;
      font-size: 14px; // Smaller font size
      min-height: 20px;
      line-height: 20px;
      max-height: 120px;
      overflow-y: auto;
    }
  }

  .send-btn {
    margin-left: 8px;
    height: 32px;
    padding: 0 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    background-color: rgba(135, 206, 235, 0.25);
    border: 1px solid rgba(135, 206, 235, 0.45);
  }

  .send-text {
    font-size: 14px;
    color: rgba(0, 122, 255, 0.9);
  }

  .send-btn.disabled {
    opacity: 0.5;
  }
}
</style>
