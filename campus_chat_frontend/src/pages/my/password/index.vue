<template>
  <view class="container">
    <view class="form-container">
      <view class="form-item">
        <text class="label">新密码</text>
        <u-input type="password" placeholder="请输入新密码" border="none"></u-input>
      </view>
      <view class="form-item">
        <text class="label">确认密码</text>
        <u-input type="password" placeholder="请再次输入新密码" border="none"></u-input>
      </view>
      
      <view class="submit-btn">
        <u-button type="primary" text="确认修改" color="#3C4A80"></u-button>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #F5F6FA;
  padding: 15px;
}

.form-container {
  background-color: #fff;
  border-radius: 12px;
  padding: 0 15px;
  
  .form-item {
    display: flex;
    align-items: center;
    padding: 15px 0;
    border-bottom: 1px solid #f5f5f5;
    
    .label {
      width: 80px;
      font-size: 15px;
      color: #333;
    }
    
    &:last-child {
      border-bottom: none;
    }
  }
}

.submit-btn {
  margin-top: 30px;
}
</style>