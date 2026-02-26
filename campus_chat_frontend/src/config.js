const ip = '192.168.43.18'
const apiPort = 9090
const imgPort = 19000
const wsPort = 8090

let apiBaseUrl = `http://${ip}:${apiPort}`
let imgBaseUrl = 'http://115.190.249.67:19000/'
let wsBaseUrl = `ws://${ip}:${wsPort}/im`

try {
  const isH5 = typeof window !== 'undefined' && typeof document !== 'undefined'
  if (isH5 && window.location) {
    const hostname = window.location.hostname || ''
    const isLocalHost = hostname === 'localhost' || hostname === '127.0.0.1'
    const host = isLocalHost ? hostname : ip
    const pageProtocol = window.location.protocol || 'http:'
    const apiProtocol = pageProtocol === 'https:' ? 'https' : 'http'
    const wsProtocol = pageProtocol === 'https:' ? 'wss' : 'ws'
    apiBaseUrl = `${apiProtocol}://${host}:${apiPort}`
    wsBaseUrl = `${wsProtocol}://${host}:${wsPort}/im`
  }
} catch (e) {}

let terminalType = '4'

try {
  const platform = String(uni.getSystemInfoSync()?.platform ?? '')
  if (platform === 'android' || platform === 'ios') terminalType = '1'
  else if (platform === 'windows' || platform === 'mac') terminalType = '2'
  else terminalType = '4'
} catch (e) {}

export default {
  API_BASE_URL: apiBaseUrl,
  IMG_BASE_URL: imgBaseUrl,
  WS_BASE_URL: wsBaseUrl,
  TERMINAL_TYPE: terminalType
}
