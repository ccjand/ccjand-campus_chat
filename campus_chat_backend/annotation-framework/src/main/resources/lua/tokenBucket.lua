--- 令牌桶算法 [限流] [获取令牌]
--- Created by ccj.
--- DateTime: 2024/7/24 19:16

local token_bucket_key = KEYS[1] -- 令牌桶的键
local capacity = tonumber(ARGV[1])  -- 桶的容量
local required = tonumber(ARGV[2])  -- 想要多少个令牌
local refillRate = tonumber(ARGV[3]) -- 填充速率
local expireTimeSeconds = tonumber(ARGV[4]) -- 过期时间（秒）

local now = redis.call('TIME')[2]; -- 使用毫秒级时间戳

-- 创建令牌桶
local createBucket = function()
    redis.call('HSET', token_bucket_key, 'capacity', capacity)
    redis.call('HSET', token_bucket_key, 'tokens', capacity)
    redis.call('HSET', token_bucket_key, 'lastRefillTime', now)
    redis.call('EXPIRE', token_bucket_key, expireTimeSeconds) -- 设置过期时间
end

-- 获取令牌
local getTokens = function()
    local lastRefillTime = tonumber(redis.call('HGET', token_bucket_key, 'lastRefillTime'))
    local tokens = tonumber(redis.call('HGET', token_bucket_key, 'tokens'))
    local timeDelta = now - lastRefillTime
    -- 补充令牌
    tokens = math.min(capacity, tokens + timeDelta * refillRate)
    -- 更新状态
    redis.call('HSET', token_bucket_key, 'tokens', tokens)
    redis.call('HSET', token_bucket_key, 'lastRefillTime', now)
    -- 尝试获取令牌
    if tokens >= required then
        tokens = tokens - required
        redis.call('HSET', token_bucket_key, 'tokens', tokens)
        redis.call('EXPIRE', token_bucket_key, expireTimeSeconds) -- 刷新过期时间
        return 1
    else
        redis.call('EXPIRE', token_bucket_key, expireTimeSeconds) -- 刷新过期时间
        return 0

    end
end

-- 主函数
local main = function()
    -- 检查令牌桶是否存在
    if not redis.call('EXISTS', token_bucket_key) then
        -- 创建令牌桶
        createBucket()
    end

    -- 获取令牌
    return getTokens()
end

return main()