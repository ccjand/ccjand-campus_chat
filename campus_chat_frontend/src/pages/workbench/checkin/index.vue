<template>
  <view class="checkin-container">
    <top-nav title="今日签到" :show-avatar="false" :show-back="true" :show-default-icons="false" :bg-transparent="false"></top-nav>
    
    <view class="main-content">
      <!-- 头部状态卡片 -->
      <view class="status-card">
        <view class="date-row">
          <text class="date">{{ currentDate }}</text>
          <text class="week">{{ currentWeek }}</text>
        </view>
        <view class="time-row">
          <text class="time">{{ currentTime }}</text>
        </view>
        <view class="location-row" v-if="locationDebugText">
          <text class="location">{{ locationDebugText }}</text>
        </view>
      </view>
      
      <!-- 签到主操作区 -->
      <view class="action-area">
        <view class="checkin-circle" @click="handleLocationCheckin" hover-class="circle-hover">
          <view class="inner">
            <text class="label">{{ isTeacher ? '发起' : '签到' }}</text>
            <text class="sub-label">{{ isTeacher ? 'Check-in' : 'Location' }}</text>
          </view>
        </view>
      </view>

      <view class="teacher-panel" v-if="isTeacher">
        <view class="panel-card">
          <view class="row clickable" @click="openTeacherCoursePicker">
            <text class="label">课程</text>
            <view class="value-container">
              <text class="value" :class="{ placeholder: !teacherSelectedCourseName }">{{ teacherSelectedCourseName || '请选择' }}</text>
              <u-icon name="arrow-right" size="16" color="#bbb"></u-icon>
            </view>
          </view>
          <view class="row clickable" @click="openTeacherClassPicker">
            <text class="label">班级</text>
            <view class="value-container">
              <view class="chips" v-if="teacherSelectedClassIds.length > 0">
                <view class="chip" v-for="cid in teacherSelectedClassIds" :key="cid">
                  <text>{{ teacherClassNameMap[cid] || cid }}</text>
                </view>
              </view>
              <text class="value placeholder" v-else>请选择</text>
              <u-icon name="arrow-right" size="16" color="#bbb"></u-icon>
            </view>
          </view>
          <view class="row" v-if="!teacherHideRadius">
            <text class="label">半径(米)</text>
            <view class="value">
              <u-input v-model="teacherRadiusMeters" type="number" placeholder="例如 100" border="surround"></u-input>
            </view>
          </view>
          <view class="row">
            <text class="label">有效(分钟)</text>
            <view class="value">
              <u-input v-model="teacherDurationMinutes" type="number" placeholder="例如 10" border="surround"></u-input>
            </view>
          </view>
          <view class="row">
            <text class="label">标题</text>
            <view class="value">
              <u-input v-model="teacherTitle" placeholder="可选" border="surround"></u-input>
            </view>
          </view>
          <view class="row" v-if="teacherLastSessionId">
            <text class="label">最新签到</text>
            <text class="value">#{{ teacherLastSessionId }}</text>
          </view>
          <view class="row" v-if="teacherLastEndTimeText">
            <text class="label">截止时间</text>
            <text class="value">{{ teacherLastEndTimeText }}</text>
          </view>
        </view>
      </view>

      <view class="student-panel" v-else>
        <view class="panel-card">
          <view class="row">
            <text class="label">可签到</text>
            <text class="value">{{ studentActiveSessions.length }}</text>
          </view>
          <view class="session-list" v-if="studentActiveSessions.length > 0">
            <view class="session-item" v-for="s in studentActiveSessions" :key="s.sessionId" @click="selectStudentSession(s)">
              <view class="left">
                <text class="title">{{ (s.courseName || '') + (s.title ? (' - ' + s.title) : '') }}</text>
                <text class="sub">{{ formatTimeRange(s.startTime, s.endTime) }}</text>
              </view>
              <view class="right">
                <text class="status" :class="{ done: !!s.checkedIn }">{{ s.checkedIn ? '已签到' : '未签到' }}</text>
              </view>
            </view>
          </view>
          <view class="empty" v-else>
            <text>暂无可签到任务</text>
          </view>
        </view>
      </view>
      
      <!-- 辅助功能入口 -->
      <view class="tools-grid">
        <view class="tool-item" @click="handleScan">
          <view class="icon-box blue">
            <u-icon name="scan" color="#3C4A80" size="24"></u-icon>
          </view>
          <text class="name">{{ isTeacher ? '签到二维码' : '扫码签到' }}</text>
        </view>
        
        <view class="tool-item" @click="openCodeTool">
          <view class="icon-box green">
            <u-icon name="edit-pen" color="#2E7D32" size="24"></u-icon>
          </view>
          <text class="name">签到码</text>
        </view>
        
        <view class="tool-item" @click="handleSupplement">
          <view class="icon-box orange">
            <u-icon name="file-text" color="#EF6C00" size="24"></u-icon>
          </view>
          <text class="name">申请补签</text>
        </view>

        <view class="tool-item" v-if="!isTeacher" @click="handleRefreshStudentSessions">
          <view class="icon-box purple">
            <u-icon name="reload" color="#6B6CFF" size="24"></u-icon>
          </view>
          <text class="name">刷新</text>
        </view>

        <view class="tool-item" v-if="!isTeacher" @click="openStudentHistory">
          <view class="icon-box purple">
            <u-icon name="list-dot" color="#6B6CFF" size="24"></u-icon>
          </view>
          <text class="name">签到记录</text>
        </view>
      </view>
    </view>
    
    <u-modal :show="showCodeModal" title="输入签到码" :show-cancel-button="true" @confirm="handleCodeCheckin" @cancel="cancelStudentCodeModal">
      <view class="modal-content">
        <u-input
          placeholder="请输入4位签到码"
          border="surround"
          v-model="checkinCode"
          type="number"
          maxlength="4"
          customStyle="text-align: center; font-size: 20px; letter-spacing: 4px; height: 44px;"
        ></u-input>
      </view>
    </u-modal>

    <u-modal :show="showTeacherCodeInputModal" title="设置签到码" :show-cancel-button="true" @confirm="confirmTeacherCodeInput" @cancel="cancelTeacherCodeInput">
      <view class="modal-content">
        <u-input
          placeholder="请输入4位签到码"
          border="surround"
          v-model="teacherDesiredCode"
          type="number"
          maxlength="4"
          customStyle="text-align: center; font-size: 20px; letter-spacing: 4px; height: 44px;"
        ></u-input>
      </view>
    </u-modal>

    <u-modal :show="showTeacherCodeModal" title="签到码" :show-cancel-button="true" @confirm="copyTeacherCode" @cancel="closeTeacherCodeModal">
      <view class="modal-content">
        <view class="teacher-code">
          <text class="code-text">{{ teacherCode || '----' }}</text>
        </view>
        <view class="teacher-code-sub">
          <text class="sub-text">{{ teacherCodeExpireText }}</text>
        </view>
        <view class="teacher-code-actions">
          <u-button type="primary" color="#3C4A80" text="重新生成" @click="openTeacherCodeInputModal"></u-button>
        </view>
      </view>
    </u-modal>

    <u-modal :show="showTeacherQrModal" title="签到二维码" :show-cancel-button="true" @confirm="copyTeacherQrContent" @cancel="closeTeacherQrModal">
      <view class="modal-content">
        <view class="teacher-qr" v-if="teacherQrImageBase64">
          <image class="qr-image" :src="teacherQrImageBase64" mode="aspectFit"></image>
        </view>
        <view class="teacher-code-sub" v-if="teacherQrExpireText">
          <text class="sub-text">{{ teacherQrExpireText }}</text>
        </view>
      </view>
    </u-modal>

    <u-picker
      :show="showTeacherCoursePicker"
      :columns="teacherCourseColumns"
      @confirm="confirmTeacherCourse"
      @cancel="showTeacherCoursePicker = false"
    ></u-picker>

    <u-popup :show="showTeacherClassPicker" mode="bottom" @close="cancelTeacherClassPicker">
      <view class="class-picker">
        <view class="picker-header">
          <text class="title">选择班级</text>
          <view class="header-actions">
            <text class="action" @click="selectAllTeacherClasses">全选</text>
            <text class="action" @click="clearTeacherClassPicker">清空</text>
          </view>
        </view>
        <scroll-view scroll-y class="picker-body">
          <u-checkbox-group v-model="teacherClassPickerTempIds" placement="column">
            <u-checkbox v-for="c in teacherClasses" :key="c.classId" :name="c.classId" :label="c.className"></u-checkbox>
          </u-checkbox-group>
        </scroll-view>
        <view class="picker-footer">
          <u-button text="取消" @click="cancelTeacherClassPicker"></u-button>
          <u-button type="primary" color="#3C4A80" text="确定" @click="confirmTeacherClassPicker"></u-button>
        </view>
      </view>
    </u-popup>

    <u-popup :show="showStudentHistory" mode="bottom" @close="closeStudentHistory">
      <view class="history-popup">
        <view class="popup-header">
          <text class="title">签到记录</text>
          <text class="action" @click="closeStudentHistory">关闭</text>
        </view>
        <scroll-view scroll-y class="popup-body">
          <view class="history-groups" v-if="studentHistoryCourses.length > 0">
            <view class="history-group" v-for="c in studentHistoryCourses" :key="c.courseId">
              <view class="group-title-row" @click="toggleHistoryCourse(c.courseId)">
                <text class="group-title">{{ c.courseName || ('课程 ' + c.courseId) }}</text>
                <view class="group-right">
                  <text class="group-count">{{ (c.records || []).length }}</text>
                  <u-icon :name="isHistoryCourseExpanded(c.courseId) ? 'arrow-up' : 'arrow-down'" size="16" color="#999"></u-icon>
                </view>
              </view>
              <view v-if="isHistoryCourseExpanded(c.courseId)">
                <view class="history-item" v-for="r in (c.records || [])" :key="c.courseId + '-' + r.sessionId">
                  <view class="left">
                    <text class="title">{{ r.sessionTitle || ('签到 #' + r.sessionId) }}</text>
                    <text class="sub">{{ formatTimeRange(r.startTime, r.endTime) }}</text>
                  </view>
                  <view class="right">
                    <text class="status" :class="{ done: !!r.checkedIn }">{{ r.checkedIn ? '已签到' : '未签到' }}</text>
                    <text class="time">{{ r.checkInTime ? dayjs(r.checkInTime).format('MM-DD HH:mm') : '' }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
          <view class="empty" v-else>
            <text>暂无签到记录</text>
          </view>
        </scroll-view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import TopNav from '@/components/TopNav.vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import request from '@/utils/request'

dayjs.locale('zh-cn')

const currentTime = ref(dayjs().format('HH:mm:ss'))
const currentDate = ref(dayjs().format('YYYY年MM月DD日'))
const currentWeek = ref(dayjs().format('dddd'))
const showCodeModal = ref(false)
const checkinCode = ref('')
const showTeacherCodeInputModal = ref(false)
const teacherDesiredCode = ref('')
const showTeacherCodeModal = ref(false)
const teacherCode = ref('')
const teacherCodeExpireAt = ref(0)
const teacherHideRadius = ref(false)
const showTeacherQrModal = ref(false)
const teacherQrImageBase64 = ref('')
const teacherQrContent = ref('')
const teacherQrExpireAt = ref(0)
const teacherQrSessionId = ref('')
let teacherQrTimer = null
let teacherQrLoading = false
let timer = null

const userInfo = ref({})

const resolveRoleType = (info) => {
  const role = info?.roleType ?? info?.role
  const num = typeof role === 'number' ? role : Number(role)
  if (!Number.isNaN(num)) {
    if (num === 0) return 'admin'
    if (num === 2) return 'teacher'
    return 'student'
  }
  const text = role == null ? '' : String(role)
  if (text === '管理员' || text.includes('管理员')) return 'admin'
  if (text === '教师' || text.includes('教师') || text === 'teacher') return 'teacher'
  if (text === '学生' || text.includes('学生') || text === 'student') return 'student'
  return ''
}

const isTeacher = computed(() => resolveRoleType(userInfo.value) === 'teacher')

const showTeacherCoursePicker = ref(false)
const teacherCourses = ref([])
const teacherCourseColumns = computed(() => [teacherCourses.value.map((c) => c.courseName || String(c.courseId))])
const teacherSelectedCourseIndex = ref(0)
const teacherSelectedCourseId = computed(() => teacherCourses.value[teacherSelectedCourseIndex.value]?.courseId ?? null)
const teacherSelectedCourseName = computed(() => teacherCourses.value[teacherSelectedCourseIndex.value]?.courseName ?? '')
const teacherClasses = ref([])
const teacherSelectedClassIds = ref([])
const showTeacherClassPicker = ref(false)
const teacherClassPickerTempIds = ref([])
const teacherTitle = ref('')
const teacherRadiusMeters = ref('100')
const teacherDurationMinutes = ref('10')
const teacherLastSessionId = ref('')
const teacherLastEndTimeText = ref('')

const teacherClassNameMap = computed(() => {
  const map = {}
  for (const c of teacherClasses.value) {
    map[c.classId] = c.className
  }
  return map
})

const studentActiveSessions = ref([])
const showStudentHistory = ref(false)
const studentHistoryCourses = ref([])
const historyCourseExpandedMap = ref({})
const teacherCodeExpireText = computed(() => {
  currentTime.value
  if (!teacherCodeExpireAt.value) return ''
  const ms = Number(teacherCodeExpireAt.value) - Date.now()
  if (!Number.isFinite(ms) || ms <= 0) return '已过期'
  const sec = Math.ceil(ms / 1000)
  return `剩余 ${sec}s`
})
const teacherQrExpireText = computed(() => {
  currentTime.value
  if (!teacherQrExpireAt.value) return ''
  const ms = Number(teacherQrExpireAt.value) - Date.now()
  if (!Number.isFinite(ms) || ms <= 0) return '已过期'
  const sec = Math.ceil(ms / 1000)
  return `剩余 ${sec}s`
})

const isLocating = ref(false)
const lastLocation = ref(null)
const lastLocationAt = ref(0)
const locationFreshMs = 60 * 1000
const hasShownLocationError = ref(false)
const locationDebugText = ref('')

onMounted(() => {
  timer = setInterval(() => {
    currentTime.value = dayjs().format('HH:mm:ss')
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (teacherQrTimer) clearInterval(teacherQrTimer)
})

const loadUserInfo = () => {
  const info = uni.getStorageSync('userInfo')
  userInfo.value = info && typeof info === 'object' ? info : {}
}

const loadStudentSessions = async () => {
  try {
    const list = await request({ url: '/capi/checkin/student/active', method: 'GET' })
    const now = Date.now()
    const raw = Array.isArray(list) ? list : []
    studentActiveSessions.value = raw.filter((s) => {
      if (!s) return false
      if (s.checkedIn) return false
      const end = Number(s.endTime)
      if (Number.isFinite(end) && end < now) return false
      return true
    })
  } catch (e) {
    studentActiveSessions.value = []
  }
}

const loadStudentHistory = async () => {
  const list = await request({ url: '/capi/checkin/student/history', method: 'GET' })
  studentHistoryCourses.value = Array.isArray(list) ? list : []
}

const isHistoryCourseExpanded = (courseId) => {
  if (courseId == null) return false
  return !!historyCourseExpandedMap.value[String(courseId)]
}

const toggleHistoryCourse = (courseId) => {
  if (courseId == null) return
  const key = String(courseId)
  historyCourseExpandedMap.value = {
    ...historyCourseExpandedMap.value,
    [key]: !historyCourseExpandedMap.value[key]
  }
}

const openStudentHistory = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    await loadStudentHistory()
    uni.hideLoading()
    if (studentHistoryCourses.value.length > 0) {
      const firstId = studentHistoryCourses.value[0]?.courseId
      if (firstId != null && historyCourseExpandedMap.value[String(firstId)] == null) {
        historyCourseExpandedMap.value = { ...historyCourseExpandedMap.value, [String(firstId)]: true }
      }
    }
    showStudentHistory.value = true
  } catch (e) {
    uni.hideLoading()
  }
}

const closeStudentHistory = () => {
  showStudentHistory.value = false
}

const handleRefreshStudentSessions = async () => {
  try {
    uni.showLoading({ title: '刷新中...' })
    await loadStudentSessions()
    uni.hideLoading()
    uni.showToast({ title: '已刷新', icon: 'success' })
  } catch (e) {
    uni.hideLoading()
  }
}

const loadTeacherCourses = async () => {
  const list = await request({ url: '/capi/checkin/teacher/courses', method: 'GET' })
  teacherCourses.value = Array.isArray(list) ? list : []
  teacherSelectedCourseIndex.value = 0
}

const loadTeacherClasses = async (courseId) => {
  if (!courseId) {
    teacherClasses.value = []
    teacherSelectedClassIds.value = []
    return
  }
  const list = await request({ url: `/capi/checkin/teacher/course/${courseId}/classes`, method: 'GET' })
  teacherClasses.value = Array.isArray(list) ? list : []
  teacherSelectedClassIds.value = []
}

const pi = 3.14159265358979324
const a = 6378245.0
const ee = 0.00669342162296594323

const outOfChina = (lat, lon) => {
  return lon < 72.004 || lon > 137.8347 || lat < 0.8293 || lat > 55.8271
}

const transformLat = (x, y) => {
  let ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x))
  ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0
  ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0
  ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0
  return ret
}

const transformLon = (x, y) => {
  let ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
  ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0
  ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0
  ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0
  return ret
}

const wgs84ToGcj02 = (lat, lon) => {
  if (!Number.isFinite(lat) || !Number.isFinite(lon)) return [lat, lon]
  if (outOfChina(lat, lon)) return [lat, lon]
  let dLat = transformLat(lon - 105.0, lat - 35.0)
  let dLon = transformLon(lon - 105.0, lat - 35.0)
  const radLat = lat / 180.0 * pi
  let magic = Math.sin(radLat)
  magic = 1 - ee * magic * magic
  const sqrtMagic = Math.sqrt(magic)
  dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi)
  dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi)
  const mgLat = lat + dLat
  const mgLon = lon + dLon
  return [mgLat, mgLon]
}

const normalizeLocationToGcj02 = (loc) => {
  const lat = Number(loc?.latitude)
  const lon = Number(loc?.longitude)
  if (!Number.isFinite(lat) || !Number.isFinite(lon)) return loc
  const [gLat, gLon] = wgs84ToGcj02(lat, lon)
  return { ...loc, latitude: gLat, longitude: gLon }
}

const requestLocation = () => {
  try {
    const isH5 = typeof window !== 'undefined' && typeof document !== 'undefined'
    if (isH5 && window.location) {
      console.warn('【定位】准备获取定位（H5环境）', {
        protocol: window.location.protocol,
        hostname: window.location.hostname,
        secureContext: window.isSecureContext
      })
    } else {
      console.warn('【定位】准备获取定位（非H5环境）')
    }
  } catch (e) {
    console.warn('【定位】准备获取定位（环境信息读取失败）')
  }

  try {
    const isH5 = typeof window !== 'undefined' && typeof document !== 'undefined'
    if (isH5 && window.location) {
      const hostname = window.location.hostname || ''
      const isLocalHost = hostname === 'localhost' || hostname === '127.0.0.1'
      if (!isLocalHost && window.isSecureContext === false) {
        console.error('【定位失败】H5 非安全环境，浏览器可能拦截定位')
        return Promise.reject(new Error('H5 insecure context'))
      }
    }
  } catch (e) {}

  if (lastLocation.value && Date.now() - lastLocationAt.value <= locationFreshMs) {
    console.warn('【定位】命中缓存，直接复用', {
      ageMs: Date.now() - lastLocationAt.value,
      latitude: lastLocation.value?.latitude,
      longitude: lastLocation.value?.longitude,
      accuracy: lastLocation.value?.accuracy
    })
    return Promise.resolve(lastLocation.value)
  }
  if (isLocating.value) {
    console.warn('【定位】已有定位请求进行中，等待结果...')
    return new Promise((resolve, reject) => {
      const startedAt = Date.now()
      const timerId = setInterval(() => {
        if (lastLocation.value) {
          clearInterval(timerId)
          resolve(lastLocation.value)
          return
        }
        if (!isLocating.value && Date.now() - startedAt > 8000) {
          clearInterval(timerId)
          reject(new Error('location timeout'))
        }
      }, 120)
    })
  }

  isLocating.value = true
  return new Promise((resolve, reject) => {
    const isH5 = typeof window !== 'undefined' && typeof document !== 'undefined'
    const locType = isH5 ? 'wgs84' : 'gcj02'
    console.warn('【定位】调用 uni.getLocation', { type: locType, timeout: 8000 })
    uni.getLocation({
      type: locType,
      isHighAccuracy: true,
      timeout: 8000,
      success: (res) => {
        const normalized = isH5 ? normalizeLocationToGcj02(res) : res
        lastLocation.value = normalized
        lastLocationAt.value = Date.now()
        isLocating.value = false
        const lat = Number(normalized?.latitude)
        const lon = Number(normalized?.longitude)
        const acc = normalized?.accuracy == null ? null : Number(normalized?.accuracy)
        locationDebugText.value = `lat=${Number.isFinite(lat) ? lat.toFixed(6) : String(normalized?.latitude)}, lon=${Number.isFinite(lon) ? lon.toFixed(6) : String(normalized?.longitude)}${Number.isFinite(acc) ? `, acc=${acc.toFixed(1)}m` : ''}`
        console.warn('【定位成功】已获取定位（用于签到/发起）', locationDebugText.value)
        console.log('【定位详情】', { ...normalized })
        resolve(normalized)
      },
      fail: (e) => {
        isLocating.value = false
        console.error('【定位失败】uni.getLocation 调用失败', e)
        reject(e)
      }
    })
  })
}

const buildLocationDiagText = (e) => {
  const parts = []
  const code = e?.code ?? e?.errCode
  const errMsg = e?.errMsg ?? e?.message
  if (code != null) parts.push(`code: ${String(code)}`)
  if (errMsg) parts.push(`errMsg: ${String(errMsg)}`)

  try {
    const isH5 = typeof window !== 'undefined' && typeof document !== 'undefined'
    if (isH5 && window.location) {
      const hostname = window.location.hostname || ''
      const protocol = window.location.protocol || ''
      const secure = window.isSecureContext
      parts.push(`protocol: ${protocol}`)
      parts.push(`hostname: ${hostname}`)
      parts.push(`secureContext: ${String(secure)}`)
    }
  } catch (err) {}

  return parts.join('\n')
}

const getLocationErrorTitle = (e) => {
  const code = e?.code ?? e?.errCode
  const msg = String(e?.errMsg || e?.message || e || '').toLowerCase()

  if (msg.includes('insecure context') || msg.includes('secure context') || msg.includes('secure')) return 'H5定位需HTTPS或localhost'
  if (code === 1 || msg.includes('permission')) return '定位权限被拒绝'
  if (code === 2 || msg.includes('position unavailable')) return '无法获取位置'
  if (code === 3 || msg.includes('timeout')) return '定位超时'
  if (
    msg.includes('secure') ||
    msg.includes('insecure') ||
    msg.includes('only secure origins') ||
    msg.includes('https')
  ) {
    return 'H5定位需HTTPS或localhost'
  }
  return '定位失败'
}

const prefetchLocation = async () => {
  try {
    await requestLocation()
  } catch (e) {
    hasShownLocationError.value = true
    const title = getLocationErrorTitle(e)
    if (title === 'H5定位需HTTPS或localhost') {
      uni.showModal({
        title: '定位不可用',
        content: 'H5 浏览器定位需要使用 https:// 域名 或 http://localhost 打开页面（http 的外网域名/IP 通常会被浏览器拦截）。',
        showCancel: false
      })
      return
    }
    uni.showModal({
      title,
      content: buildLocationDiagText(e) || '请检查系统定位服务与浏览器定位权限',
      showCancel: false
    })
  }
}

onShow(async () => {
  loadUserInfo()
  hasShownLocationError.value = false
  locationDebugText.value = ''
  try {
    console.warn('【签到页】页面进入', {
      uid: userInfo.value?.uid,
      role: userInfo.value?.role,
      roleType: resolveRoleType(userInfo.value)
    })
  } catch (e) {
    console.warn('【签到页】页面进入')
  }
  const roleType = resolveRoleType(userInfo.value)
  if (roleType === 'teacher') {
    try {
      await loadTeacherCourses()
      await loadTeacherClasses(teacherSelectedCourseId.value)
    } catch (e) {}
  } else {
    await loadStudentSessions()
  }
})

const openTeacherCoursePicker = async () => {
  if (!teacherCourses.value || teacherCourses.value.length === 0) {
    try {
      await loadTeacherCourses()
      await loadTeacherClasses(teacherSelectedCourseId.value)
    } catch (e) {}
  }
  showTeacherCoursePicker.value = true
}

const confirmTeacherCourse = async (e) => {
  const pickedName = e?.value?.[0]
  const idx = teacherCourses.value.findIndex((c) => (c.courseName || String(c.courseId)) === pickedName)
  teacherSelectedCourseIndex.value = idx >= 0 ? idx : 0
  showTeacherCoursePicker.value = false
  try {
    await loadTeacherClasses(teacherSelectedCourseId.value)
  } catch (err) {}
}

const openTeacherClassPicker = async () => {
  if (!teacherSelectedCourseId.value) {
    uni.showToast({ title: '请先选择课程', icon: 'none' })
    return
  }
  if (!teacherClasses.value || teacherClasses.value.length === 0) {
    try {
      await loadTeacherClasses(teacherSelectedCourseId.value)
    } catch (e) {}
  }
  teacherClassPickerTempIds.value = teacherSelectedClassIds.value.slice()
  showTeacherClassPicker.value = true
}

const cancelTeacherClassPicker = () => {
  showTeacherClassPicker.value = false
}

const confirmTeacherClassPicker = () => {
  teacherSelectedClassIds.value = teacherClassPickerTempIds.value.slice()
  showTeacherClassPicker.value = false
}

const clearTeacherClassPicker = () => {
  teacherClassPickerTempIds.value = []
}

const selectAllTeacherClasses = () => {
  teacherClassPickerTempIds.value = teacherClasses.value.map((c) => c.classId)
}

const confirmTeacherCreate = async () => {
  try {
    if (!teacherSelectedCourseId.value) {
      uni.showToast({ title: '请选择课程', icon: 'none' })
      return
    }
    if (!teacherSelectedClassIds.value || teacherSelectedClassIds.value.length === 0) {
      uni.showToast({ title: '请选择班级', icon: 'none' })
      return
    }
    const radius = Number(teacherRadiusMeters.value)
    const duration = Number(teacherDurationMinutes.value)
    if (!Number.isFinite(radius) || radius <= 0) {
      uni.showToast({ title: '请输入正确的半径', icon: 'none' })
      return
    }
    if (!Number.isFinite(duration) || duration <= 0) {
      uni.showToast({ title: '请输入正确的有效分钟', icon: 'none' })
      return
    }

    uni.showLoading({ title: '定位中...' })
    const loc = await requestLocation()
    uni.hideLoading()

    uni.showLoading({ title: '创建中...' })
    const resp = await request({
      url: '/capi/checkin/teacher/session',
      method: 'POST',
      data: {
        courseId: teacherSelectedCourseId.value,
        title: teacherTitle.value || null,
        centerLatitude: loc.latitude,
        centerLongitude: loc.longitude,
        radiusMeters: radius,
        durationMinutes: duration,
        classIds: teacherSelectedClassIds.value
      }
    })
    uni.hideLoading()

    teacherLastSessionId.value = resp?.sessionId ? String(resp.sessionId) : ''
    teacherLastEndTimeText.value = resp?.endTime ? dayjs(resp.endTime).format('YYYY-MM-DD HH:mm') : ''
    uni.showToast({ title: '已发起', icon: 'success' })
  } catch (e) {
    uni.hideLoading()
  }
}

const openCodeTool = async () => {
  if (isTeacher.value) {
    if (!teacherSelectedCourseId.value) {
      uni.showToast({ title: '请选择课程', icon: 'none' })
      return
    }
    if (!teacherSelectedClassIds.value || teacherSelectedClassIds.value.length === 0) {
      uni.showToast({ title: '请选择班级', icon: 'none' })
      return
    }
    const duration = Number(teacherDurationMinutes.value)
    if (!Number.isFinite(duration) || duration <= 0) {
      uni.showToast({ title: '请输入正确的有效分钟', icon: 'none' })
      return
    }
    openTeacherCodeInputModal()
    return
  }
  showCodeModal.value = true
}

const cancelStudentCodeModal = () => {
  showCodeModal.value = false
  checkinCode.value = ''
}

const openTeacherCodeInputModal = () => {
  showTeacherCodeModal.value = false
  teacherHideRadius.value = true
  teacherDesiredCode.value = ''
  showTeacherCodeInputModal.value = true
}

const cancelTeacherCodeInput = () => {
  showTeacherCodeInputModal.value = false
  teacherDesiredCode.value = ''
  teacherHideRadius.value = false
}

const closeTeacherCodeModal = () => {
  showTeacherCodeModal.value = false
  teacherHideRadius.value = false
}

const closeTeacherQrModal = () => {
  showTeacherQrModal.value = false
  teacherQrImageBase64.value = ''
  teacherQrContent.value = ''
  teacherQrExpireAt.value = 0
  teacherQrSessionId.value = ''
  teacherQrLoading = false
  if (teacherQrTimer) {
    clearInterval(teacherQrTimer)
    teacherQrTimer = null
  }
}

const confirmTeacherCodeInput = async () => {
  if (!teacherDesiredCode.value || !/^\d{4}$/.test(String(teacherDesiredCode.value))) {
    uni.showToast({ title: '请输入正确的签到码', icon: 'none' })
    return
  }
  try {
    if (!teacherSelectedCourseId.value) {
      uni.showToast({ title: '请选择课程', icon: 'none' })
      return
    }
    if (!teacherSelectedClassIds.value || teacherSelectedClassIds.value.length === 0) {
      uni.showToast({ title: '请选择班级', icon: 'none' })
      return
    }
    const duration = Number(teacherDurationMinutes.value)
    if (!Number.isFinite(duration) || duration <= 0) {
      uni.showToast({ title: '请输入正确的有效分钟', icon: 'none' })
      return
    }

    const radius = Number(teacherRadiusMeters.value)
    if (!Number.isFinite(radius) || radius <= 0) {
      uni.showToast({ title: '系统默认半径异常', icon: 'none' })
      return
    }

    uni.showLoading({ title: '定位中...' })
    const loc = await requestLocation()
    uni.hideLoading()

    uni.showLoading({ title: '创建中...' })
    const createResp = await request({
      url: '/capi/checkin/teacher/session',
      method: 'POST',
      data: {
        courseId: teacherSelectedCourseId.value,
        title: teacherTitle.value || null,
        centerLatitude: loc.latitude,
        centerLongitude: loc.longitude,
        radiusMeters: radius,
        durationMinutes: duration,
        classIds: teacherSelectedClassIds.value
      }
    })
    uni.hideLoading()

    const sessionId = createResp?.sessionId ? String(createResp.sessionId) : ''
    teacherLastSessionId.value = sessionId
    teacherLastEndTimeText.value = createResp?.endTime ? dayjs(createResp.endTime).format('YYYY-MM-DD HH:mm') : ''

    if (!sessionId) {
      uni.showToast({ title: '创建签到失败', icon: 'none' })
      return
    }

    uni.showLoading({ title: '生成中...' })
    const resp = await request({
      url: `/capi/checkin/teacher/session/${sessionId}/code`,
      method: 'POST',
      data: { code: String(teacherDesiredCode.value) }
    })
    uni.hideLoading()
    teacherCode.value = resp?.code ? String(resp.code) : ''
    teacherCodeExpireAt.value = resp?.expireAt ? Number(resp.expireAt) : 0
    showTeacherCodeInputModal.value = false
    teacherHideRadius.value = false
    try {
      if (teacherCode.value) {
        await uni.setClipboardData({ data: String(teacherCode.value) })
        uni.showToast({ title: `已发起，签到码${teacherCode.value}已复制`, icon: 'none' })
      } else {
        uni.showToast({ title: '已发起', icon: 'success' })
      }
    } catch (e) {
      uni.showToast({ title: teacherCode.value ? `已发起，签到码${teacherCode.value}` : '已发起', icon: 'none' })
    }
  } catch (e) {
    uni.hideLoading()
  }
}

const openStudentCodeModal = () => {
  checkinCode.value = ''
  showCodeModal.value = true
}

const copyTeacherCode = async () => {
  if (!teacherCode.value) {
    closeTeacherCodeModal()
    return
  }
  try {
    await uni.setClipboardData({ data: String(teacherCode.value) })
    uni.showToast({ title: '已复制', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: '复制失败', icon: 'none' })
  }
}

const copyTeacherQrContent = async () => {
  if (!teacherQrContent.value) {
    closeTeacherQrModal()
    return
  }
  try {
    await uni.setClipboardData({ data: String(teacherQrContent.value) })
    uni.showToast({ title: '已复制', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: '复制失败', icon: 'none' })
  }
}

const selectStudentSession = async (s) => {
  if (!s || !s.sessionId) return
  if (s.checkedIn) {
    uni.showToast({ title: '已签到', icon: 'none' })
    return
  }
  if (s.codeEnabled) {
    openStudentCodeModal()
    return
  }
  try {
    uni.showLoading({ title: '定位中...' })
    const loc = await requestLocation()
    uni.hideLoading()

    uni.showLoading({ title: '签到中...' })
    const resp = await request({
      url: '/capi/checkin/student/checkin',
      method: 'POST',
      data: {
        sessionId: s.sessionId,
        latitude: loc.latitude,
        longitude: loc.longitude,
        accuracy: loc.accuracy || null,
        location: ''
      }
    })
    uni.hideLoading()
    uni.showToast({ title: '签到成功', icon: 'success' })
    setTimeout(() => {
      loadStudentSessions()
    }, 800)
  } catch (e) {
    uni.hideLoading()
  }
}

const handleLocationCheckin = async () => {
  try {
    console.warn('【签到页】点击主按钮', { isTeacher: !!isTeacher.value })
  } catch (e) {
    console.warn('【签到页】点击主按钮')
  }
  if (isTeacher.value) {
    if (!teacherCourses.value || teacherCourses.value.length === 0) {
      try {
        await loadTeacherCourses()
        await loadTeacherClasses(teacherSelectedCourseId.value)
      } catch (e) {}
    }
    await confirmTeacherCreate()
    return
  }

  if (!studentActiveSessions.value || studentActiveSessions.value.length === 0) {
    uni.showToast({ title: '暂无可签到任务', icon: 'none' })
    return
  }
  const first = studentActiveSessions.value[0]
  if (first && first.codeEnabled) {
    openStudentCodeModal()
    return
  }
  selectStudentSession(first)
}

const handleScan = () => {
  if (isTeacher.value) {
    handleTeacherQr()
    return
  }
  uni.scanCode({
    onlyFromCamera: true,
    success: async (res) => {
      try {
        const content = res?.result ? String(res.result) : ''
        if (!content) {
          uni.showToast({ title: '未识别到内容', icon: 'none' })
          return
        }
        uni.showLoading({ title: '签到中...' })
        await request({
          url: '/capi/checkin/student/checkin/qrcode',
          method: 'POST',
          data: { content }
        })
        uni.hideLoading()
        uni.showToast({ title: '签到成功', icon: 'success' })
        setTimeout(() => {
          loadStudentSessions()
        }, 800)
      } catch (e) {
        uni.hideLoading()
      }
    }
  })
}

const handleTeacherQr = async () => {
  try {
    if (!teacherSelectedCourseId.value) {
      uni.showToast({ title: '请选择课程', icon: 'none' })
      return
    }
    if (!teacherSelectedClassIds.value || teacherSelectedClassIds.value.length === 0) {
      uni.showToast({ title: '请选择班级', icon: 'none' })
      return
    }
    const radius = Number(teacherRadiusMeters.value)
    const duration = Number(teacherDurationMinutes.value)
    if (!Number.isFinite(radius) || radius <= 0) {
      uni.showToast({ title: '请输入正确的半径', icon: 'none' })
      return
    }
    if (!Number.isFinite(duration) || duration <= 0) {
      uni.showToast({ title: '请输入正确的有效分钟', icon: 'none' })
      return
    }

    uni.showLoading({ title: '定位中...' })
    const loc = await requestLocation()
    uni.hideLoading()

    uni.showLoading({ title: '创建中...' })
    const created = await request({
      url: '/capi/checkin/teacher/session',
      method: 'POST',
      data: {
        courseId: teacherSelectedCourseId.value,
        title: teacherTitle.value || null,
        centerLatitude: loc.latitude,
        centerLongitude: loc.longitude,
        radiusMeters: radius,
        durationMinutes: duration,
        classIds: teacherSelectedClassIds.value
      }
    })
    uni.hideLoading()

    const sessionId = created?.sessionId ? String(created.sessionId) : ''
    if (!sessionId) {
      uni.showToast({ title: '创建签到失败', icon: 'none' })
      return
    }
    teacherLastSessionId.value = sessionId
    teacherLastEndTimeText.value = created?.endTime ? dayjs(created.endTime).format('YYYY-MM-DD HH:mm') : ''

    showTeacherQrModal.value = true
    teacherQrSessionId.value = sessionId
    await refreshTeacherQr()
    if (teacherQrTimer) clearInterval(teacherQrTimer)
    teacherQrTimer = setInterval(() => {
      refreshTeacherQr()
    }, 10 * 1000)
  } catch (e) {
    uni.hideLoading()
  }
}

const refreshTeacherQr = async () => {
  if (!showTeacherQrModal.value) return
  if (!teacherQrSessionId.value) return
  if (teacherQrLoading) return
  teacherQrLoading = true
  try {
    const resp = await request({
      url: `/capi/checkin/teacher/session/${teacherQrSessionId.value}/qrcode`,
      method: 'POST'
    })
    teacherQrContent.value = resp?.content ? String(resp.content) : ''
    teacherQrImageBase64.value = resp?.imageBase64 ? String(resp.imageBase64) : ''
    teacherQrExpireAt.value = resp?.expireAt ? Number(resp.expireAt) : 0
    try {
      if (teacherQrContent.value) {
        await uni.setClipboardData({ data: String(teacherQrContent.value) })
      }
    } catch (e) {}
  } catch (e) {
  } finally {
    teacherQrLoading = false
  }
}

const handleCodeCheckin = async () => {
  if (!checkinCode.value || !/^\d{4}$/.test(String(checkinCode.value))) {
    uni.showToast({ title: '请输入正确的签到码', icon: 'none' })
    return
  }
  try {
    showCodeModal.value = false
    uni.showLoading({ title: '验证中...' })
    await request({
      url: '/capi/checkin/student/checkin/code',
      method: 'POST',
      data: { code: String(checkinCode.value) }
    })
    uni.hideLoading()
    uni.showToast({ title: '签到成功', icon: 'success' })
    checkinCode.value = ''
    setTimeout(() => {
      loadStudentSessions()
    }, 800)
  } catch (e) {
    uni.hideLoading()
  }
}

const handleSupplement = () => {
  uni.navigateTo({ url: '/pages/workbench/supplement/index' })
}

const formatTimeRange = (startTime, endTime) => {
  const start = startTime ? dayjs(startTime).format('HH:mm') : ''
  const end = endTime ? dayjs(endTime).format('HH:mm') : ''
  if (!start && !end) return ''
  return `${start} - ${end}`
}
</script>

<style lang="scss" scoped>
.checkin-container {
  min-height: 100vh;
  background-color: #F5F6FA;
}

.main-content {
  padding: 20px;
}

.status-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
  
  .date-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
    
    .date { font-size: 15px; color: #333; }
    .week { font-size: 15px; color: #666; }
  }
  
  .time-row {
    text-align: center;
    margin: 15px 0;
    
    .time {
      font-size: 36px;
      font-weight: bold;
      color: #333;
      font-family: monospace;
    }
  }

  .location-row {
    margin-top: 6px;
    text-align: center;

    .location {
      font-size: 12px;
      color: #666;
      word-break: break-all;
    }
  }
}

.action-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40px;
  
  .checkin-circle {
    width: 140px;
    height: 140px;
    background: #e8ebf5;
    border-radius: 50%;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 15px;
    transition: all 0.2s;

    &::before {
      content: '';
      position: absolute;
      inset: -12px;
      border-radius: 50%;
      background: rgba(60, 74, 128, 0.18);
      animation: checkin-ring-pulse 1.8s ease-out infinite;
      z-index: 0;
    }
    
    .inner {
      width: 120px;
      height: 120px;
      background: linear-gradient(135deg, #3C4A80 0%, #5C6BC0 100%);
      border-radius: 50%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 15px rgba(60, 74, 128, 0.3);
      position: relative;
      z-index: 1;
      
      .label {
        font-size: 20px;
        color: #fff;
        font-weight: 600;
        margin-bottom: 4px;
      }
      
      .sub-label {
        font-size: 12px;
        color: rgba(255,255,255,0.8);
      }
    }
    
    &.circle-hover {
      transform: scale(0.95);
      background: #dbe0ed;
    }
  }
}

.tools-grid {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  row-gap: 18px;
  
  .tool-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 20%;
    
    .icon-box {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 8px;
      
      &.blue { background: #E8EAF6; }
      &.green { background: #E8F5E9; }
      &.orange { background: #FFF3E0; }
      &.purple { background: #EEF0FF; }
    }
    
    .name {
      font-size: 12px;
      color: #666;
    }
  }
}

.history-popup {
  background: #fff;
  border-top-left-radius: 14px;
  border-top-right-radius: 14px;
  padding: 12px 14px;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 0 10px;
    border-bottom: 1px solid #f5f5f5;

    .title {
      font-size: 16px;
      color: #333;
      font-weight: 600;
    }

    .action {
      font-size: 14px;
      color: #3C4A80;
    }
  }

  .popup-body {
    max-height: 70vh;
    padding-top: 10px;
  }

  .history-group {
    margin-bottom: 12px;
  }

  .group-title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
  }

  .group-title {
    font-size: 13px;
    color: #666;
  }

  .group-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .group-count {
    font-size: 12px;
    color: #999;
  }

  .history-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    .left {
      flex: 1;
      padding-right: 10px;
      display: flex;
      flex-direction: column;

      .title {
        font-size: 14px;
        color: #333;
        margin-bottom: 4px;
      }

      .sub {
        font-size: 12px;
        color: #999;
      }
    }

    .right {
      flex-shrink: 0;
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 2px;

      .time {
        font-size: 12px;
        color: #666;
      }

      .status {
        font-size: 12px;
        color: #999;

        &.done {
          color: #2E7D32;
        }
      }
    }
  }
}

.modal-content {
  padding: 20px 10px;
}

.teacher-code {
  display: flex;
  justify-content: center;
  padding: 6px 0 4px;

  .code-text {
    font-size: 36px;
    font-weight: 700;
    letter-spacing: 6px;
    color: #3C4A80;
    font-family: monospace;
  }
}

.teacher-qr {
  display: flex;
  justify-content: center;
  padding: 8px 0 4px;

  .qr-image {
    width: 220px;
    height: 220px;
    border-radius: 12px;
    background: #fff;
  }
}

.teacher-code-sub {
  display: flex;
  justify-content: center;
  padding: 6px 0 10px;

  .sub-text {
    font-size: 12px;
    color: #666;
  }
}

.teacher-code-actions {
  padding: 4px 10px 0;
}

.modal-form {
  padding: 10px 10px 0;
}

.form-row {
  padding: 10px 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;

  .label {
    width: 84px;
    font-size: 14px;
    color: #333;
    flex-shrink: 0;
  }

  .value {
    flex: 1;
    font-size: 14px;
    color: #666;
  }
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;

  .chip {
    padding: 4px 8px;
    border-radius: 999px;
    background: #eef0f8;
    color: #3C4A80;
    font-size: 12px;
  }
}

.teacher-panel,
.student-panel {
  margin-bottom: 20px;
}

.panel-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px 16px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);

  .row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    .label {
      font-size: 14px;
      color: #333;
    }

    .value {
      font-size: 14px;
      color: #666;
    }
  }
}

.clickable {
  .value-container {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
    max-width: 72%;

    .value {
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .chips {
      max-width: 100%;
    }
  }
}

.placeholder {
  color: #999 !important;
}

.class-picker {
  background: #fff;
  border-top-left-radius: 14px;
  border-top-right-radius: 14px;
  padding: 14px 16px 12px;

  .picker-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-bottom: 10px;
    border-bottom: 1px solid #f5f5f5;

    .title {
      font-size: 15px;
      color: #333;
      font-weight: 600;
    }

    .header-actions {
      display: flex;
      gap: 12px;

      .action {
        font-size: 13px;
        color: #3C4A80;
      }
    }
  }

  .picker-body {
    max-height: 45vh;
    padding: 12px 0;
  }

  .picker-footer {
    display: flex;
    gap: 12px;
    padding-top: 8px;
  }
}

.session-list {
  margin-top: 10px;

  .session-item {
    padding: 10px 0;
    border-top: 1px solid #f5f5f5;
    display: flex;
    justify-content: space-between;
    gap: 12px;

    .left {
      flex: 1;
    }

    .title {
      font-size: 14px;
      color: #333;
      display: block;
      margin-bottom: 4px;
    }

    .sub {
      font-size: 12px;
      color: #999;
    }

    .status {
      font-size: 12px;
      color: #ff8f00;
      &.done {
        color: #2e7d32;
      }
    }
  }
}

.empty {
  padding-top: 10px;
  color: #999;
  font-size: 13px;
}

@keyframes checkin-ring-pulse {
  0% {
    transform: scale(0.95);
    opacity: 0.65;
  }
  70% {
    transform: scale(1.18);
    opacity: 0;
  }
  100% {
    transform: scale(1.18);
    opacity: 0;
  }
}
</style>
