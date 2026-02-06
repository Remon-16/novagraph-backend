package com.tech.novagraphbackendcommon.handler;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 通用数据同步处理器
 * @param <T> 具体的实体类，如 ThumbPicture, ThumbMoment
 */
public interface IUserActionSyncHandler<T> {

    /**
     * 获取 Redis Key 前缀
     * 例如："sp:thumb:temp:"
     */
    String getRedisKeyPrefix();

    /**
     * 根据用户 ID 和业务目标 ID 构建实体
     */
    T buildEntity(Long userId, Long targetId);

    /**
     * 批量插入
     */
    void batchInsert(List<T> entityList);

    /**
     * 单独插入
     */
    void singleInsert(T entity);

    /**
     * 批量删除
     */
    void batchRemove(List<Long> userIds, List<Long> targetIds);

    /**
     * 单独删除
     */
    void singleDelete(Long userId, Long targetId);

    /**
     * 更新目标实体的统计数据
     * @param countMap key: targetId, value: 增量
     */
    void updateStatistics(Map<Long, Long> countMap);

    /**
     * 更新自身的统计数据
     * @param countMap key: targetId, value: 增量
     */
    void updateOwnerStatistics(Map<Long, Long> countMap);

    // --- 以下是处理副作用（积分、消息）所需的抽象方法 ---

    /**
     * 根据 targetId 获取该内容所属的作者 ID
     * 通用模板需要这个 ID 来给谁加积分、给谁发消息
     */
    Long getTargetOwnerId(Long targetId);

    /**
     * 获取该操作对应的积分配置值
     */
    default Long getScoreValue(String bizType) {
        return 10L;
    }

    /**
     * 获取消息类型
     */
    default String getMessageType(String bizType) {
        return "thumb";
    }

    /**
     * 获取消息内容常量
     */
    default String getMessageContent(String bizType) {
        return "赞了你的内容";
    }

    void handleSideEffects(List<T> insertedList, Set<Long> targetIds);
}