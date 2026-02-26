<template>
  <view class="date-separator">
    <text class="date-text">{{ date }}</text>
  </view>
</template>

<script setup>


const props = defineProps({
  date: {
    type: String,
    required: true
  }
})
</script>

<style lang="scss" scoped>
.date-separator {
  display: flex;
  justify-content: center;
  padding: 15px 0;
  
  .date-text {
    font-size: 12px;
    color: #999;
    background-color: rgba(255, 255, 255, 0.4);
    padding: 2px 8px;
    border-radius: 4px;
  }
}
</style>
