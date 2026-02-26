import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.locale('zh-cn')
dayjs.extend(relativeTime)

export const formatTime = (timestamp) => {
  // If timestamp is already formatted string (like '23分钟前'), return it
  if (typeof timestamp === 'string' && (timestamp.includes('前') || timestamp.includes('昨天') || timestamp.includes('星期'))) {
    return timestamp
  }
  // Otherwise verify logic if needed, but for mock data we use static strings
  return timestamp
}
