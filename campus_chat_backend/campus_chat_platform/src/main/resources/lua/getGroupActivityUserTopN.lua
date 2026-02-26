---
--- Created by ccj.
--- DateTime: 2024/7/2 18:49
--- 获取群聊的前面n个活跃用户, 不超过n个，但是至少都是活跃的

-- ZREVRANGEBYSCORE cache:shop:type +inf 1 limit 0 3

local roomId = tonumber(ARGV[1]) -- 房间号
local minActive = tonumber(ARGV[2]) or '-inf' -- 最低允许的活跃值。默认值设为负无穷，如果不需要过滤则使用这个逻辑
local limitUserCont = tonumber(ARGV[3]) or 1000 -- 最多支持1000个活跃用户进行推模式

local key = 'campus-chat:groupMember:roomId_' .. roomId
local groupMemberCount = redis.call('ZCARD', key) -- 查看群里面有多少个成员

if groupMemberCount < limitUserCont then
    minActive = '-inf' -- 如果群成员个数小于指定的（如1000）那么就全部返回， 全部使用推模式推送消息
end

local topNActiveUser = redis.call('ZREVRANGEBYSCORE', key, '+inf', minActive, 'LIMIT', 0, limitUserCont)

return cjson.encode(topNActiveUser)