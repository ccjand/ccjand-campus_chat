import CONFIG from '@/config.js'

const WS_URL = CONFIG.WS_BASE_URL

let socketTask = null
let connectingPromise = null
const messageListeners = new Set()

const safeJsonParse = (text) => {
  try {
    return JSON.parse(text)
  } catch (e) {
    return null
  }
}

const notifyMessageListeners = (payload) => {
  messageListeners.forEach((listener) => {
    try {
      listener(payload)
    } catch (e) {}
  })
}

const buildWsUrl = ({ token, terminalType }) => {
  const query = `token=${encodeURIComponent(String(token))}&terminalType=${encodeURIComponent(String(terminalType))}`
  return `${WS_URL}?${query}`
}

const connect = ({ token, terminalType }) => {
  if (!token) return Promise.reject(new Error('Missing token'))
  if (connectingPromise) return connectingPromise

  disconnect()

  connectingPromise = new Promise((resolve, reject) => {
    const url = buildWsUrl({ token, terminalType })
    console.log(`WS：准备连接（terminalType=${String(terminalType)}）`)

    socketTask = uni.connectSocket({
      url,
      fail: (err) => {
        console.log('WS：连接失败', err)
        socketTask = null
        connectingPromise = null
        reject(err)
      }
    })

    if (!socketTask) {
      console.log('WS：连接失败（未获取到 socketTask）')
      connectingPromise = null
      reject(new Error('connectSocket failed'))
      return
    }

    socketTask.onOpen(() => {
      console.log('WS：连接成功')
      connectingPromise = null
      resolve(true)
    })

    socketTask.onError((err) => {
      console.log('WS：连接异常', err)
      socketTask = null
      connectingPromise = null
      reject(err)
    })

    socketTask.onClose(() => {
      console.log('WS：连接已关闭')
      socketTask = null
      connectingPromise = null
    })

    socketTask.onMessage((evt) => {
      const data = evt?.data
      const parsed = typeof data === 'string' ? safeJsonParse(data) : data
      notifyMessageListeners(parsed ?? data)
    })
  })

  return connectingPromise
}

const disconnect = () => {
  try {
    const swallowNotConnected = (err) => {
      const msg = err?.errMsg ?? err?.message ?? String(err ?? '')
      if (String(msg).includes('WebSocket is not connected')) return
      console.log('WS：断开连接失败', err)
    }

    if (socketTask) {
      console.log('WS：主动断开连接')
      const task = socketTask
      socketTask = null
      task.close({
        fail: swallowNotConnected,
        complete: () => {}
      })
    } else {
      console.log('WS：主动断开连接（无活动连接）')
      uni.closeSocket({
        fail: swallowNotConnected,
        complete: () => {}
      })
    }
  } catch (e) {
    console.log('WS：断开连接异常', e)
    socketTask = null
  } finally {
    connectingPromise = null
  }
}

const send = (data) => {
  if (!socketTask) throw new Error('Socket not connected')
  const payload = typeof data === 'string' ? data : JSON.stringify(data)
  return socketTask.send({ data: payload })
}

const isConnected = () => Boolean(socketTask)

const onMessage = (listener) => {
  if (typeof listener !== 'function') return () => {}
  messageListeners.add(listener)
  return () => {
    messageListeners.delete(listener)
  }
}

export default {
  connect,
  disconnect,
  send,
  isConnected,
  onMessage
}
