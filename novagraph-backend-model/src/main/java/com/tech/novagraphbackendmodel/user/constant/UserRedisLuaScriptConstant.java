package com.tech.novagraphbackendmodel.user.constant;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public class UserRedisLuaScriptConstant {

    /**
     * 动态点赞 Lua 脚本
     * KEYS[1]       -- 临时计数键
     * KEYS[2]       -- 用户点赞状态键
     * ARGV[1]       -- 用户 ID
     * ARGV[2]       -- 动态 ID
     * 返回:
     * -1: 已点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> POST_THUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]       -- 临时计数键（如 novagraph:thumb:temp:{timeSlice}）
            local userThumbKey = KEYS[2]       -- 用户点赞状态键（如 novagraph:thumb:{userId}）
            local postKey = KEYS[3]       -- 动态Id键（如 novagraph:sp:{postId}）
            local userId = ARGV[1]             -- 用户 ID
            local postId = ARGV[2]             -- 动态 ID
            
            -- 1. 检查是否已点赞（避免重复操作）
            if redis.call('HEXISTS', userThumbKey, postId) == 1 then
               return -1  -- 已点赞，返回 -1 表示失败
            end
            
            -- 2. 获取旧值（不存在则默认为 0）
            local hashKey = userId .. ':' .. postId
            local oldNumber = tonumber(redis.call('HGET', tempThumbKey, hashKey) or 0)
            local oldThumbCount = tonumber(redis.call('GET', postKey) or 0)
            
            -- 3. 计算新值
            local newNumber = oldNumber + 1
            local newThumbCount = oldThumbCount + 1
            
            -- 4. 原子性更新：写入临时计数 + 标记用户已点赞
            redis.call('HSET', tempThumbKey, hashKey, newNumber)
            redis.call('SET', postKey, newThumbCount)
            redis.call('HSET', userThumbKey, postId, 1)
            return 1  -- 返回 1 表示成功
            """, Long.class);

    /**
     * 动态取消点赞 Lua 脚本
     * 参数同上
     * 返回：
     * -1: 未点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> POST_UNTHUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]      -- 临时计数键（如 novagraph:thumb:temp:{timeSlice}）
            local userThumbKey = KEYS[2]      -- 用户点赞状态键（如 novagraph:thumb:{userId}）
            local postKey = KEYS[3]     -- 动态Id键（如 novagraph:sp:{postId}）
            local userId = ARGV[1]            -- 用户 ID
            local postId = ARGV[2]      -- 动态 ID
            -- 1. 检查用户是否已点赞（若未点赞，直接返回失败）
            if redis.call('HEXISTS', userThumbKey, postId) ~= 1 then
               return -1  -- 未点赞，返回 -1 表示失败
            end
            
            -- 2. 获取当前临时计数（若不存在则默认为 0）
            local hashKey = userId .. ':' .. postId
            local oldNumber = tonumber(redis.call('HGET', tempThumbKey, hashKey) or 0)
            local oldThumbCount = tonumber(redis.call('GET', postKey) or 0)
            -- 3. 计算新值并更新
            local newNumber = oldNumber - 1
            local newThumbCount = oldThumbCount - 1
            
            -- 4. 原子性操作：更新临时计数 + 删除用户点赞标记
            redis.call('HSET', tempThumbKey, hashKey, newNumber)
            redis.call('SET', postKey, newThumbCount)
            redis.call('HDEL', userThumbKey, postId)
            
            return 1  -- 返回 1 表示成功
            """, Long.class);


    /**
     * 添加历史记录
     * KEYS[1]       -- 临时计数键
     * KEYS[2]       -- 用户点赞状态键
     * ARGV[1]       -- 用户 ID
     * ARGV[2]       -- 剧本 ID
     * 返回:
     * -1: 已点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> SP_HIS_ADD_SCRIPT = new DefaultRedisScript<>("""
            local tempHisKey = KEYS[1]       -- 临时计数键（如 novagraph:his:temp:{timeSlice}）
            local userHisKey = KEYS[2]       -- 用户浏览历史状态键（如 novagraph:his:{userId}）
            local screenplayKey = KEYS[3]       -- 动态Id键（如 novagraph:sp:{postId}）
            local userId = ARGV[1]             -- 用户 ID
            local screenplayId = ARGV[2]             -- 动态 ID
            
            -- 1. 检查是否已点赞（避免重复操作）
            if redis.call('HEXISTS', userThumbKey, postId) == 1 then
               return -1  -- 已点赞，返回 -1 表示失败
            end
            
            -- 2. 获取旧值（不存在则默认为 0）
            local hashKey = userId .. ':' .. postId
            local oldNumber = tonumber(redis.call('HGET', tempHisKey, hashKey) or 0)
            local oldPlayCount = tonumber(redis.call('GET', postKey) or 0)
            
            -- 3. 计算新值
            local newNumber = oldNumber + 1
            local newPlayCount = oldPlayCount + 1
            
            -- 4. 原子性更新：写入临时计数 + 标记用户已点赞
            redis.call('HSET', tempHisKey, hashKey, newNumber)
            redis.call('SET', postKey, newPlayCount)
            redis.call('HSET', userThumbKey, postId, 1)
            return 1  -- 返回 1 表示成功
            """, Long.class);


    /**
     * 用户关注 Lua 脚本
     * 返回:
     * -1: 已关注
     * 1: 操作成功
     */
    public static final RedisScript<Long> USER_FOLLOW_SCRIPT = new DefaultRedisScript<>("""
            local tempFollowKey = KEYS[1]       -- 临时计数键（如 novagraph:fl:temp:{timeSlice}）
            local userFollowingKey = KEYS[2]    -- 关注Hash（如 novagraph:fl:{userId}）
            local userFollowerKey = KEYS[3]     -- 粉丝Hash novagraph:fler:{userId}<UNK>
            local followingKey = KEYS[4]        -- 关注数量（如 novagraph:flc:{userId}）
            local followerKey = KEYS[5]         -- 粉丝数量（如 novagraph:flerc:{userId}）
            local userId = ARGV[1]              -- 用户 ID
            local targetUserId = ARGV[2]        -- 目标用户 ID
            
            -- 1. 检查是否已关注（避免重复操作）
            if redis.call('HEXISTS', userFollowingKey, targetUserId) == 1 then
               return -1  -- 已关注，返回 -1 表示失败
            end
            
            -- 2. 获取旧值（不存在则默认为 0）
            local hashKey = userId .. ':' .. targetUserId
            local oldNumber = tonumber(redis.call('HGET', tempFollowKey, hashKey) or 0)
            local oldFollowingCount = tonumber(redis.call('GET', followingKey) or 0)
            local oldFollowerCount = tonumber(redis.call('GET', followerKey) or 0)
            
            -- 3. 计算新值
            local newNumber = oldNumber + 1
            local newFollowingCount = oldFollowingCount + 1
            local newFollowerCount = oldFollowerCount + 1
            
            -- 4. 原子性更新：写入临时计数 + 标记用户已点赞
            redis.call('HSET', tempFollowKey, hashKey, newNumber)
            redis.call('SET', followingKey, newFollowingCount)
            redis.call('HSET', userFollowingKey, targetUserId, 1)
            redis.call('SET', followerKey, newFollowingCount)
            redis.call('HSET', userFollowerKey, userId, 1)
            return 1  -- 返回 1 表示成功
            """, Long.class);

    /**
     * 用户取消关注 Lua 脚本
     * 参数同上
     * 返回：
     * -1: 未点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> USER_UNFOLLOW_SCRIPT = new DefaultRedisScript<>("""
            local tempFollowKey = KEYS[1]       -- 临时计数键（如 novagraph:fl:temp:{timeSlice}）
            local userFollowingKey = KEYS[2]    -- 关注Hash（如 novagraph:fl:{userId}）
            local userFollowerKey = KEYS[3]     -- 粉丝Hash novagraph:fler:{userId}<UNK>
            local followingKey = KEYS[4]        -- 关注数量（如 novagraph:flc:{userId}）
            local followerKey = KEYS[5]         -- 粉丝数量（如 novagraph:flerc:{userId}）
            local userId = ARGV[1]              -- 用户 ID
            local targetUserId = ARGV[2]        -- 目标用户 ID
            
            -- 1. 检查用户是否已点赞（若未点赞，直接返回失败）
            if redis.call('HEXISTS', userFollowingKey, targetUserId) ~= 1 then
               return -1  -- 未点赞，返回 -1 表示失败
            end
            
            -- 2. 获取当前临时计数（若不存在则默认为 0）
            local hashKey = userId .. ':' .. targetUserId
            local oldNumber = tonumber(redis.call('HGET', tempFollowKey, hashKey) or 0)
            local oldFollowingCount = tonumber(redis.call('GET', followingKey) or 0)
            local oldFollowerCount = tonumber(redis.call('GET', followerKey) or 0)
            
            -- 3. 计算新值并更新
            local newNumber = oldNumber - 1
            local newFollowingCount = oldFollowingCount - 1
            local newFollowerCount = oldFollowerCount - 1
            
            -- 4. 原子性操作：更新临时计数 + 删除用户点赞标记
            redis.call('HSET', tempFollowKey, hashKey, newNumber)
            redis.call('SET', followingKey, newFollowingCount)
            redis.call('HDEL', userFollowingKey, targetUserId)
            redis.call('SET', followerKey, newFollowerCount)
            redis.call('HDEL', userFollowerKey, userId)
            
            return 1  -- 返回 1 表示成功
            """, Long.class);


    /**
     * 剧本收藏 Lua 脚本
     * KEYS[1]       -- 临时计数键
     * KEYS[2]       -- 用户点赞状态键
     * ARGV[1]       -- 用户 ID
     * ARGV[2]       -- 动态 ID
     * 返回:
     * -1: 已点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> SP_FAVORITE_SCRIPT = new DefaultRedisScript<>("""
            local tempKey = KEYS[1]       -- 临时计数键
            local userFavoriteKey = KEYS[2]    -- 用户收藏状态键
            local spFavoriteKey = KEYS[3]      -- 动态Id键
            local userId = ARGV[1]             -- 用户 ID
            local screenplayId = ARGV[2]       -- 动态 ID
            local folderId = ARGV[3]           -- 收藏夹 ID
            
            local userHashKey = screenplayId  .. ':' .. folderId   -- 用户把剧本收藏到了哪个收藏夹
            
            
            -- 1. 检查是否已点赞（避免重复操作）
            if redis.call('HEXISTS', userFavoriteKey, userHashKey) == 1 then
               return -1  -- 已点赞，返回 -1 表示失败
            end
            
            -- 2. 获取旧值（不存在则默认为 0）
            local hashKey = userId .. ':' .. userHashKey
            local oldNumber = tonumber(redis.call('HGET', tempKey, hashKey) or 0)
            local oldFavoriteCount = tonumber(redis.call('GET', spFavoriteKey) or 0)
            
            -- 3. 计算新值
            local newNumber = oldNumber + 1
            local newFavoriteCount = oldFavoriteCount + 1
            
            -- 4. 原子性更新：写入临时计数 + 标记用户已点赞
            redis.call('HSET', tempKey, hashKey, newNumber)
            redis.call('SET', spFavoriteKey, newFavoriteCount)
            redis.call('HSET', userFavoriteKey, userHashKey, 1)
            return 1  -- 返回 1 表示成功
            """, Long.class);

    /**
     * 剧本取消收藏 Lua 脚本
     * 参数同上
     * 返回：
     * -1: 未收藏
     * 1: 操作成功
     */
    public static final RedisScript<Long> SP_UNFAVORITE_SCRIPT = new DefaultRedisScript<>("""
            local tempKey = KEYS[1]       -- 临时计数键
            local userFavoriteKey = KEYS[2]    -- 用户收藏状态键
            local spFavoriteKey = KEYS[3]      -- 动态Id键
            local userId = ARGV[1]             -- 用户 ID
            local screenplayId = ARGV[2]       -- 动态 ID
            local folderId = ARGV[3]           -- 收藏夹 ID
            
            local userHashKey = screenplayId  .. ':' .. folderId   -- 用户把剧本收藏到了哪个收藏夹
            
            -- 1. 检查用户是否已收藏（若未点赞，直接返回失败）
            if redis.call('HEXISTS', userFavoriteKey, userHashKey) ~= 1 then
               return -1  -- 未收藏，返回 -1 表示失败
            end
            
            -- 2. 获取当前临时计数（若不存在则默认为 0）
            local hashKey = userId .. ':' .. userHashKey
            local oldNumber = tonumber(redis.call('HGET', tempKey, hashKey) or 0)
            local oldFavoriteCount = tonumber(redis.call('GET', spFavoriteKey) or 0)
            -- 3. 计算新值并更新
            local newNumber = oldNumber - 1
            local newFavoriteCount = oldFavoriteCount - 1
            
            -- 4. 原子性操作：更新临时计数 + 删除用户点赞标记
            redis.call('HSET', tempKey, hashKey, newNumber)
            redis.call('SET', spFavoriteKey, newFavoriteCount)
            redis.call('HDEL', userFavoriteKey, userHashKey)
            
            return 1  -- 返回 1 表示成功
            """, Long.class);
}
