---
--- Created by ccj.
--- DateTime: 2024/7/9 23:43
--- 查找群里面的指定用户uid
---

local roomIdKey = 'campus-chat:groupMember:roomId_' .. tonumber(ARGV[1])
local uidList = cjson.decode(ARGV[2])

if redis.call('EXISTS', roomIdKey) == 0 then
    return nil
end

local foundUids = {} -- 用来存储找到的uid

local uidScores = redis.call('ZMSCORE', roomIdKey, unpack(uidList))
for i, uidScore in ipairs(uidScores) do
    if uidScore ~= false then
        table.insert(foundUids, uidList[i])
    end
end

return cjson.encode(foundUids)
