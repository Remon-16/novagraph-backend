package com.tech.novagraphbackendmodel.graph.constant;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public class RedisLuaScriptConstant {

    /**
     * 剧本点赞 Lua 脚本
     * KEYS[1]       -- 临时计数键
     * KEYS[2]       -- 用户点赞状态键
     * ARGV[1]       -- 用户 ID
     * ARGV[2]       -- 剧本 ID
     * 返回:
     * -1: 已点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> SCREENPLAY_THUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]       -- 临时计数键（如 novagraph:thumb:temp:{timeSlice}）
            local userThumbKey = KEYS[2]       -- 用户点赞状态键（如 novagraph:thumb:{userId}）
            local screenplayKey = KEYS[3]       -- 剧本Id键（如 novagraph:sp:{screenplayId}）
            local userId = ARGV[1]             -- 用户 ID
            local screenplayId = ARGV[2]             -- 剧本 ID
            
            -- 1. 检查是否已点赞（避免重复操作）
            if redis.call('HEXISTS', userThumbKey, screenplayId) == 1 then
               return -1  -- 已点赞，返回 -1 表示失败
            end
            
            -- 2. 获取旧值（不存在则默认为 0）
            local hashKey = userId .. ':' .. screenplayId
            local oldNumber = tonumber(redis.call('HGET', tempThumbKey, hashKey) or 0)
            local oldThumbCount = tonumber(redis.call('GET', screenplayKey) or 0)
            
            -- 3. 计算新值
            local newNumber = oldNumber + 1
            local newThumbCount = oldThumbCount + 1
            
            -- 4. 原子性更新：写入临时计数 + 标记用户已点赞
            redis.call('HSET', tempThumbKey, hashKey, newNumber)
            redis.call('SET', screenplayKey, newThumbCount)
            redis.call('HSET', userThumbKey, screenplayId, 1)
            return 1  -- 返回 1 表示成功
            """, Long.class);

    /**
     * 剧本取消点赞 Lua 脚本
     * 参数同上
     * 返回：
     * -1: 未点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> SCREENPLAY_UNTHUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]      -- 临时计数键（如 novagraph:thumb:temp:{timeSlice}）
            local userThumbKey = KEYS[2]      -- 用户点赞状态键（如 novagraph:thumb:{userId}）
            local screenplayKey = KEYS[3]     -- 剧本Id键（如 novagraph:sp:{screenplayId}）
            local userId = ARGV[1]            -- 用户 ID
            local screenplayId = ARGV[2]      -- 剧本 ID
            -- 1. 检查用户是否已点赞（若未点赞，直接返回失败）
            if redis.call('HEXISTS', userThumbKey, screenplayId) ~= 1 then
               return -1  -- 未点赞，返回 -1 表示失败
            end
            
            -- 2. 获取当前临时计数（若不存在则默认为 0）
            local hashKey = userId .. ':' .. screenplayId
            local oldNumber = tonumber(redis.call('HGET', tempThumbKey, hashKey) or 0)
            local oldThumbCount = tonumber(redis.call('GET', screenplayKey) or 0)
            -- 3. 计算新值并更新
            local newNumber = oldNumber - 1
            local newThumbCount = oldThumbCount - 1
            
            -- 4. 原子性操作：更新临时计数 + 删除用户点赞标记
            redis.call('HSET', tempThumbKey, hashKey, newNumber)
            redis.call('SET', screenplayKey, newThumbCount)
            redis.call('HDEL', userThumbKey, screenplayId)
            
            return 1  -- 返回 1 表示成功
            """, Long.class);
}
