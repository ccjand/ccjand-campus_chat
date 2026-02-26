<template>
  <view class="filter-tabs">
    <scroll-view scroll-x class="scroll-view" :show-scrollbar="false">
      <view class="tabs-container">
        <view 
          v-for="(tab, index) in tabs" 
          :key="tab.key"
          class="tab-item"
          :class="{ active: currentTab === tab.key }"
          @click="selectTab(tab.key)"
        >
          <text class="tab-text">{{ tab.label }}</text>
          <text v-if="tab.count > 0" class="tab-count">{{ tab.count }}</text>
        </view>
      </view>
    </scroll-view>
    <view class="menu-icon">
      <u-icon name="list" size="24" color="#666"></u-icon>
    </view>
  </view>
</template>

<script setup>


const props = defineProps({
  tabs: {
    type: Array,
    default: () => []
  },
  currentTab: {
    type: String,
    default: 'all'
  }
})

const emit = defineEmits(['change'])

const selectTab = (key) => {
  emit('change', key)
}
</script>

<style lang="scss" scoped>
.filter-tabs {
  display: flex;
  align-items: center;
  background-color: #F3F3F3; // Updated background
  padding: 10px 15px;
  
  .scroll-view {
    flex: 1;
    white-space: nowrap;
    margin-right: 10px;
    overflow-x: auto; // Enable horizontal scrolling
  }
  
  .tabs-container {
    display: flex;
    align-items: center;
  }
  
  .tab-item {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 4px 10px;
    background-color: #fff; // Updated to white background for contrast
    border-radius: 16px;
    margin-right: 8px;
    transition: all 0.3s;
    white-space: nowrap;
    flex-shrink: 0;
    
    &.active {
      background-color: #e6f0ff;
      
      .tab-text, .tab-count {
        color: #007aff;
      }
    }
    
    .tab-text {
      font-size: 13px;
      color: #666;
    }
    
    .tab-count {
      font-size: 12px;
      margin-left: 2px;
      color: #666;
    }
  }
  
  .menu-icon {
    padding-left: 10px;
    border-left: 1px solid #ddd;
  }
}
</style>
