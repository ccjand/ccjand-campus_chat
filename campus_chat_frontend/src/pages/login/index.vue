<template>
  <view class="login-container">
    <!-- 动态流体背景 -->
    <view class="fluid-bg">
      <view class="shape shape-1"></view>
      <view class="shape shape-2"></view>
      <view class="shape shape-3"></view>
    </view>
    
    <view class="content-wrapper">
      <view class="form-card">
        <!-- Logo区域 -->
        <view class="logo-area">
          <view class="logo-box">
            <u-icon name="home-fill" color="#fff" size="40"></u-icon>
          </view>
        </view>
        
        <view class="input-group">
          <u-icon name="account" color="#999" size="22"></u-icon>
          <input 
            class="input" 
            v-model="formData.account" 
            placeholder="请输入学号/工号" 
            placeholder-style="color: #ccc; font-size: 14px;"
          />
        </view>
        
        <view class="input-group">
          <u-icon name="lock" color="#999" size="22"></u-icon>
          <input 
            class="input" 
            v-model="formData.password" 
            password 
            placeholder="请输入密码" 
            placeholder-style="color: #ccc; font-size: 14px;"
          />
        </view>
        
        <view class="login-btn" @click="handleLogin" hover-class="btn-hover">
          <text class="btn-text">登 录</text>
        </view>
        
        <view class="footer-links">
          <text class="link">忘记密码</text>
          <text class="divider">|</text>
          <text class="link">新用户注册</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import uIcon from 'uview-plus/components/u-icon/u-icon.vue'
import request from '@/utils/request' // 引入封装的请求
import imSocket from '@/utils/imSocket'
import CONFIG from '@/config.js'

const formData = ref({
  account: '101',
  password: '123456'
})

const normalizeRoleText = (role) => {
  const num = typeof role === 'number' ? role : Number(role)
  if (!Number.isNaN(num)) {
    if (num === 0) return '管理员'
    if (num === 2) return '教师'
    return '学生'
  }
  return role == null ? '' : String(role)
}

const buildUserInfoFromLoginResp = (loginResp) => {
  const roleText = normalizeRoleText(loginResp?.role)
  const avatar = (() => {
    const raw = loginResp?.avatar
    if (!raw) return ''
    const text = String(raw)
    if (text.startsWith('http') || text.startsWith('data:')) return text
    return CONFIG.IMG_BASE_URL + text
  })()
  return {
    uid: loginResp?.uid,
    token: loginResp?.token,
    name: loginResp?.fullName,
    avatar,
    role: roleText,
    department: loginResp?.department,
    className: loginResp?.className,
    id: loginResp?.accountNumber
  }
}

const handleLogin = async () => {
  if (!formData.value.account || !formData.value.password) {
    uni.showToast({ title: '请输入账号和密码', icon: 'none' })
    return
  }
  
  uni.showLoading({ title: '登录中...' })
  
  try {
    const loginResp = await request({
      url: '/capi/users/login',
      method: 'POST',
      data: {
        accountNumber: formData.value.account,
        password: formData.value.password
      }
    })
    
    const userInfo = buildUserInfoFromLoginResp(loginResp)
    uni.setStorageSync('token', userInfo.token)
    uni.setStorageSync('uid', userInfo.uid)
    uni.setStorageSync('userInfo', userInfo)

    try {
      await imSocket.connect({ token: userInfo.token, terminalType: CONFIG.TERMINAL_TYPE })
    } catch (e) {
      console.error('WS连接失败:', e)
    }
    
    uni.hideLoading()
    uni.showToast({ title: '登录成功', icon: 'success' })
    
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/index/index' })
    }, 1500)
    
  } catch (error) {
    // 错误提示已在 request.js 中统一弹出，这里只需要隐藏 loading
    console.error('登录失败:', error)
    // 移除 uni.hideLoading() 以避免关闭 request.js 中可能弹出的 toast
    // uni.hideLoading()
  }
}
</script>
<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background-color: #f8f9fa;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
}

// 动态流体背景
.fluid-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
  
  .shape {
    position: absolute;
    filter: blur(80px);
    opacity: 0.6;
    animation: float 20s infinite ease-in-out;
    border-radius: 40% 60% 70% 30% / 40% 50% 60% 50%;
  }
  
  .shape-1 {
    top: -20%;
    left: -20%;
    width: 600px;
    height: 600px;
    background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
    animation-delay: 0s;
  }
  
  .shape-2 {
    bottom: -20%;
    right: -20%;
    width: 500px;
    height: 500px;
    background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
    animation-delay: -5s;
    animation-direction: reverse;
  }
  
  .shape-3 {
    top: 40%;
    left: 30%;
    width: 300px;
    height: 300px;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    opacity: 0.4;
    animation-delay: -10s;
  }
}

@keyframes float {
  0% { transform: translate(0, 0) rotate(0deg); border-radius: 40% 60% 70% 30% / 40% 50% 60% 50%; }
  33% { transform: translate(30px, -50px) rotate(120deg); border-radius: 70% 30% 50% 50% / 30% 30% 70% 70%; }
  66% { transform: translate(-20px, 20px) rotate(240deg); border-radius: 100% 60% 60% 100% / 100% 100% 60% 60%; }
  100% { transform: translate(0, 0) rotate(360deg); border-radius: 40% 60% 70% 30% / 40% 50% 60% 50%; }
}

.content-wrapper {
  padding: 0 40px;
  position: relative;
  z-index: 1;
}

.form-card {
  background: rgba(255, 255, 255, 0.85); // 增加半透明感
  backdrop-filter: blur(20px); // 磨砂效果
  border-radius: 24px;
  padding: 50px 30px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.5);
  
  .logo-area {
    display: flex;
    justify-content: center;
    margin-bottom: 50px;
    
    .logo-box {
      width: 88px;
      height: 88px;
      background: linear-gradient(135deg, #3C4A80 0%, #5C6BC0 100%);
      border-radius: 26px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 10px 25px rgba(60, 74, 128, 0.3);
      // 已移除旋转 transform: rotate(-5deg);
    }
  }
  
  .input-group {
    margin-bottom: 25px;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 16px;
    height: 56px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    border: 1px solid transparent;
    transition: all 0.3s;
    box-shadow: 0 2px 10px rgba(0,0,0,0.02);
    
    .input {
      flex: 1;
      margin-left: 12px;
      font-size: 16px;
      color: #333;
      height: 100%;
    }
    
    &:focus-within {
      background: #fff;
      border-color: #3C4A80;
      box-shadow: 0 4px 15px rgba(60, 74, 128, 0.1);
      transform: translateY(-2px);
    }
  }
  
  .login-btn {
    margin-top: 45px;
    height: 56px;
    background: linear-gradient(135deg, #3C4A80 0%, #5C6BC0 100%);
    border-radius: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 10px 25px rgba(60, 74, 128, 0.3);
    transition: all 0.3s;
    
    .btn-text {
      color: #fff;
      font-size: 18px;
      font-weight: 600;
      letter-spacing: 6px;
    }
    
    &.btn-hover {
      transform: scale(0.96);
      box-shadow: 0 5px 15px rgba(60, 74, 128, 0.2);
    }
  }
  
  .footer-links {
    margin-top: 30px;
    display: flex;
    justify-content: center;
    align-items: center;
    
    .link {
      color: #888;
      font-size: 14px;
      transition: color 0.2s;
      
      &:active {
        color: #3C4A80;
      }
    }
    
    .divider {
      color: #ddd;
      margin: 0 15px;
      font-size: 12px;
    }
  }
}
</style>
