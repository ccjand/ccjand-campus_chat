--- Created by ccj.
--- DateTime: 2024/6/25 18:31
--- 用于获取多个房间的成员个数
---

local function getRoomMemberCounts(roomIdsJson)
    local roomIds = cjson.decode(roomIdsJson)
    local roomCounts = {}  -- 用于存储房间ID及其成员数的映射

    for _, roomId in ipairs(roomIds) do
        local key = 'campus-chat:groupMember:roomId_' .. roomId
        local exists = redis.call('EXISTS', key)

        if exists == 1 then
            local count = redis.call('ZCARD', key)
            -- 不再需要单独跟踪需要加载的房间列表
            roomCounts[tostring(roomId)] = tostring(count)
        end
    end

    -- 直接返回房间成员计数的映射
    -- 检查roomCounts是否有数据
    if next(roomCounts) then
        -- 如果roomCounts非空，则进行编码并返回
        return cjson.encode(roomCounts)
    end

    -- 如果roomCounts为空，返回一个明确的信息或空对象，避免序列化问题
    return cjson.encode({});
end

return getRoomMemberCounts(ARGV[1])
