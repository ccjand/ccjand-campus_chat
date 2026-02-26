<template>
  <view class="message-list">
    <message-item 
      v-for="msg in messages" 
      :key="msg.id" 
      :message="msg"
    ></message-item>
  </view>
</template>

<script setup>

import MessageItem from './MessageItem.vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  }
})
</script>

<style lang="scss" scoped>
.message-list {
  background-color: #fff;
  padding-bottom: 60px; // Space for bottom nav
}
</style>
