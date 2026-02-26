// src/utils/request.js

import CONFIG from '@/config.js'

// 根据实际环境修改后端地址
const BASE_URL = CONFIG.API_BASE_URL

const LOG_MAX_DEPTH = 20
const LOG_MAX_ARRAY_LENGTH = 200
const LOG_MAX_STRING_LENGTH = 4000

const sanitizeForLog = (value, depth = 0, seen = new WeakSet()) => {
  if (value == null) return value
  if (typeof value === 'string') {
    if (value.length <= LOG_MAX_STRING_LENGTH) return value
    return value.slice(0, LOG_MAX_STRING_LENGTH) + `...[TruncatedString len=${value.length}]`
  }
  if (typeof value !== 'object') return value

  if (seen.has(value)) return '[Circular]'
  seen.add(value)

  if (depth >= LOG_MAX_DEPTH) return '[MaxDepth]'

  if (Array.isArray(value)) {
    const sliced = value.length > LOG_MAX_ARRAY_LENGTH ? value.slice(0, LOG_MAX_ARRAY_LENGTH) : value
    const mapped = sliced.map((v) => sanitizeForLog(v, depth + 1, seen))
    if (value.length > LOG_MAX_ARRAY_LENGTH) {
      mapped.push(`[... ${value.length - LOG_MAX_ARRAY_LENGTH} more items]`)
    }
    return mapped
  }

  const result = {}
  for (const [k, v] of Object.entries(value)) {
    const keyLower = String(k).toLowerCase()
    if (keyLower.includes('token') || keyLower === 'authorization') {
      result[k] = '[REDACTED]'
      continue
    }
    result[k] = sanitizeForLog(v, depth + 1, seen)
  }
  return result
}

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    const tokenStr = token == null ? '' : String(token)
    
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }
    
    header['terminalType'] = CONFIG.TERMINAL_TYPE
    if (tokenStr) header['Authorization'] = tokenStr.toLowerCase().startsWith('bearer ') ? tokenStr : `Bearer ${tokenStr}`

    // 增强日志打印：路径 + 参数
    console.log(`>>> [Request] ${options.method || 'GET'} ${options.url}`);
    if (options.data) {
        console.log('    Params:', sanitizeForLog(options.data));
    }

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      success: (res) => {
        console.log('<<< 正确响应:', options.url, res.statusCode, sanitizeForLog(res.data))
        // 2. HTTP 状态码判断
        if (res.statusCode === 200) {
          const apiResult = res.data
          
          // 3. 业务状态码判断 (ApiResult.success)
          if (apiResult.success) {
            // 成功：直接返回 data 数据，解包 ApiResult
            resolve(apiResult.data)
          } else {
            // 失败：统一提示错误信息
            uni.showToast({
              title: apiResult.errMsg || '请求失败',
              icon: 'none',
              duration: 2000
            })
            // 如果需要特定处理（如 401 未登录），可以在这里添加逻辑
            if (apiResult.errCode === 401) {
                // 跳转登录页等
            }
            reject(new Error(apiResult.errMsg))
          }
        } else {
          uni.showToast({
            title: '服务器繁忙 (' + res.statusCode + ')',
            icon: 'none'
          })
          reject(new Error('HTTP Error ' + res.statusCode))
        }
      },
      fail: (err) => {
        console.log('<<< 错误响应:', options.url, 'REQUEST_FAIL', sanitizeForLog(err))
        uni.showToast({
          title: '网络连接失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

export default request
