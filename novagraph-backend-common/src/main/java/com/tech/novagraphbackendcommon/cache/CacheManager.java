package com.tech.novagraphbackendcommon.cache;

import cn.hutool.core.util.RandomUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tech.novagraphbackendcommon.algorithm.AddResult;
import com.tech.novagraphbackendcommon.algorithm.HeavyKeeper;
import com.tech.novagraphbackendcommon.algorithm.TopK;
import com.tech.novagraphbackendcommon.common.SortedCacheResult;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnClass(RedisTemplate.class)
public class CacheManager {

    /**
     * 初始容量
     */
    @Getter
    @Setter
    private String localInitialCapacity = "1024";
    /**
     * 最大容量
     */
    @Getter
    @Setter
    private String localMaximumSize = "10000";
    /**
     * 过期时间
     */
    @Getter
    @Setter
    private String localExpireMinutes = "5";

    @Getter
    @Setter
    private Integer redisValueExpireTime = 60 * 30;

    @Getter
    @Setter
    private Integer redisZSetExpireTime = 24 * 60 * 60;

    @Getter
    private final Long defaultPage = 1L;
    @Getter
    private final Long defaultSize = 10L;

    @Getter
    private final Long ZERO = 0L;

    @Getter
    private final Integer Entry_DELETE_FLAG = 1;

    private static final String DESC = "descend";

    private static final String ASC = "ascend";
    /**
     * 用户是否做出了点赞、关注等
     */
    @Getter
    private static final String USER_ACTION_HAS = "::1";
    @Getter
    private static final String USER_NO_ACTION_HAS = "::0";


    private final TopK hotKeyDetector = new HeavyKeeper(
            // 监控 Top 100 Key
            100,
            // 哈希表宽度
            100000,
            // 哈希表深度
            5,
            // 衰减系数
            0.92,
            // 最小出现 10 次才记录
            10
    );


    private final Cache<String, Object> localCache = Caffeine.newBuilder()
            .initialCapacity(Integer.parseInt(localInitialCapacity))
            .maximumSize(Long.parseLong(localMaximumSize))
            // 缓存 5 分钟移除
            .expireAfterWrite(Long.parseLong(localExpireMinutes), TimeUnit.MINUTES)
            .build();


    private final RedisTemplate<String, Object> redisTemplate;

    public CacheManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 定时清理过期的热 Key 检测数据
    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.SECONDS)
    public void cleanHotKeys() {
        hotKeyDetector.fading();
    }

    // 拼接 Key
    private String buildCacheKeyByHead(String keyHead, String KeyBody){
        return keyHead + ":" + KeyBody;
    }

    /**
     * 拼接 redis 的 Key 用于分布式 Redis 区分不同服务
     */
    private String buildRedisKey(String key){
        return CacheUtils.APP_NAME + ":" + key;
    }

    // 辅助方法：构造复合 key
    private String buildCacheKey(String hashKey, String key) {
        return hashKey + ":" + key;
    }

    public Object getValueCache(String key){
        // 1. 先查本地缓存
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            log.info("本地缓存获取到数据 {} = {}", key, value);
            // 记录访问次数（每次访问计数 +1）
            hotKeyDetector.add(key, 1);
            return value;
        }

        // 2. 本地缓存未命中，查询 Redis
        Object redisValue = redisTemplate.opsForValue().get(buildRedisKey(key));
        if (redisValue == null) {
            return null;
        }

        // 3. 记录访问（计数 +1）
        AddResult addResult = hotKeyDetector.add(key, 1);

        // 4. 如果是热 Key 且不在本地缓存，则缓存数据
        if (addResult.isHotKey()) {
            localCache.put(key, redisValue);
        }

        return redisValue;
    }

    public void removeValueCache(String key){
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            localCache.invalidate(key);
        }
        if (redisTemplate.hasKey(buildRedisKey(key))) {
            redisTemplate.delete(key);
        }
    }

    public void putValueToCache(String key, Object value){
        this.putValueToCache(key, value, redisValueExpireTime);
    }


    public void putValueToCache(String key,Object value, Integer expireTime){
        // 1. 记录访问（计数 +1）
        AddResult addResult = hotKeyDetector.add(key, 1);
        if (addResult.isHotKey()) {
            // 2. 存本地缓存
            localCache.put(key, value);
        }
        // 3. 存 Redis
        int redisCacheExpireTime = expireTime +  RandomUtil.randomInt(0, expireTime);
        redisTemplate.opsForValue().set(buildRedisKey(key),
                value, redisCacheExpireTime, TimeUnit.SECONDS);
    }

    public void putIfPresentLocalHash(String hashKey, String key, Integer value) {
        String compositeKey = buildCacheKey(hashKey, key);
        Object object = localCache.getIfPresent(compositeKey);
        if (object == null) {
            return;
        }
        Integer oldValue = (Integer) object;
        localCache.put(compositeKey, oldValue + value);
    }

    /**
     * 获取Hash类型多级缓存
     * @param hashKey
     * @param key
     * @return
     */
    public Object getHashCache(String hashKey, String key) {
        // 构造唯一的 composite key
        String compositeKey = buildCacheKey(hashKey, key);

        // 1. 先查本地缓存
        Object value = localCache.getIfPresent(compositeKey);
        if (value != null) {
            log.info("本地缓存获取到数据 {} = {}", compositeKey, value);
            // 记录访问次数（每次访问计数 +1）
            hotKeyDetector.add(key, 1);
            return value;
        }

        // 2. 本地缓存未命中，查询 Redis
        Object redisValue = redisTemplate.opsForHash().get(buildRedisKey(hashKey), key);
        if (redisValue == null) {
            return null;
        }

        // 3. 记录访问（计数 +1）
        AddResult addResult = hotKeyDetector.add(key, 1);

        // 4. 如果是热 Key 且不在本地缓存，则缓存数据
        if (addResult.isHotKey()) {
            localCache.put(compositeKey, redisValue);
        }

        return redisValue;
    }

    public void zSetAdd(String key, Object value, Double score) {
        String redisKey = buildRedisKey(key);
        Boolean f = redisTemplate.hasKey(redisKey);
        redisTemplate.opsForZSet().addIfAbsent(redisKey, value, score);
        // 不存在就设置一个过期时间
        if (!f) {
            int redisCacheExpireTime = redisZSetExpireTime +  RandomUtil.randomInt(0, redisZSetExpireTime);
            redisTemplate.expire(redisKey, redisCacheExpireTime, TimeUnit.SECONDS);
        }
    }

    public void zSetRemove(String key, Object value) {
        String redisKey = buildRedisKey(key);
        Boolean f = redisTemplate.hasKey(redisKey);
        if (f){
            redisTemplate.opsForZSet().remove(redisKey, value);
        }
    }

    public Set<Object> zSetPageQuery(String key, Long page, Long size){
        return this.zSetPageQuery(key, page, size, ASC);
    }

    public Set<Object> zSetPageQuery(String key, Long page, Long size, String order){
        Set<Object> ValueSet = null;
        if(DESC.equals(order)){
            ValueSet = redisTemplate.opsForZSet().reverseRange(key, (page - 1) * size, page * size - 1);
        }else{
            // 默认升序
            ValueSet = redisTemplate.opsForZSet().range(key, (page - 1) * size, page * size - 1);
        }
        return ValueSet;
    }

    public Boolean zSetUserActionHas(String key, Object member){
        if(StringUtils.isEmpty(key) || member == null){
            throw new RuntimeException("key == null || member == null");
        }
        if (!redisTemplate.hasKey(key)){
            return null;
        }
        Double scoreHas = redisTemplate.opsForZSet().score(key, member + USER_ACTION_HAS);
        Double scoreNoHas = redisTemplate.opsForZSet().score(key, member + USER_NO_ACTION_HAS);
        if(scoreHas == null && scoreNoHas == null){
            return null;
        }
        return scoreHas != null;
    }


    public void insertSortedValue(String sortedKey, Object valueId, Double score, String valueKey, Object value) {
        this.zSetAdd(sortedKey, valueId, score);
        this.putValueToCache(valueKey, value, redisZSetExpireTime);
    }

    public Long getTotal(String totalKey){
        // 1. 查询 total
        Object totalValue = this.getValueCache(totalKey);
        if (totalValue == null) {
            return null;
        }

        return Long.parseLong(String.valueOf(totalValue));
    }

    public SortedCacheResult querySortedValues(String sortedKey, String sortedTotalKey, String keyHead, String sortOrder, Long page, Long size) {
        Long total = getTotal(sortedTotalKey);
        if (total == null) {
            return null;
        }
        SortedCacheResult sortedCacheResult = new SortedCacheResult();
        sortedCacheResult.setTotal(total);
        // 总数为 0 也算命中
        if(ZERO.equals(total)){
            sortedCacheResult.setValueMap(new LinkedHashMap<>());
            return sortedCacheResult;
        }
        // 2. 从zSet里查询id
        String zSetKey = buildRedisKey(sortedKey);
        Set<Object> idSet = this.zSetPageQuery(zSetKey, page, size, sortOrder);
        // 满足下列条件，直接返回一个空
        if (idSet == null || idSet.isEmpty() ||
                (idSet.size() != size && !ZERO.equals(total - (page - 1) * size - idSet.size()))) {
            return null;
        }

        // 3. 从id里获取值
        Map<Object, Object> valueMap = new LinkedHashMap<>();
        for (Object objId : idSet) {
            Object value = this.getValueCache(buildCacheKeyByHead(keyHead, objId.toString()));
            // 里面如果有值过期了，也返回空
            if(value == null){
                return null;
            }
            valueMap.put(objId, value);
        }
        sortedCacheResult.setValueMap(valueMap);
        return sortedCacheResult;
    }

    /**
     * 批量获取点赞状态
     * @param hashKey
     * @param objectIdList
     * @return
     */
    public Map<Long, Boolean> getThumbMapCaffeine(String hashKey, List<String> objectIdList){
        Map<Long, Boolean> objIdHasThumbMap = new HashMap<>();

        List<String> compositeKeyList = objectIdList.stream()
                .map(id -> buildCacheKey(hashKey, id))
                .toList();
        for (int i = 0; i < objectIdList.size(); i++) {
            String compositeKey = compositeKeyList.get(i);
            Object value = localCache.getIfPresent(compositeKey);
            if (value != null) {
                log.info("本地缓存获取到数据 {} = {}", compositeKey, value);
                // 记录访问次数（每次访问计数 +1）
                hotKeyDetector.add(objectIdList.get(i), 1);
                Long thumbId = (Long) value;
                if(thumbId.equals(1L)){
                    objIdHasThumbMap.put(Long.parseLong(compositeKey), Boolean.TRUE);
                }
            }
        }
        return objIdHasThumbMap;
    }

    /**
     * Redis批量获取点赞状态
     * @param hashKey
     * @param objectIdList
     * @return
     */
    public Map<Long, Boolean> getThumbMapRedis(String hashKey, List<String> objectIdList){
        Map<Long, Boolean> objIdHasThumbMap = new HashMap<>();
        List<Object> objList = new ArrayList<>(objectIdList);
        // 获取点赞
        List<Object> thumbList = redisTemplate.opsForHash().multiGet(
                buildRedisKey(hashKey), objList);
        for (int i = 0; i < thumbList.size(); i++) {
            if (thumbList.get(i) == null) {
                continue;
            }
            objIdHasThumbMap.put(Long.valueOf(objList.get(i).toString()), Boolean.TRUE);
        }
        return objIdHasThumbMap;
    }

    /**
     * 批量获取点赞状态
     * @param hashKey
     * @param objectIdList
     * @return
     */
    public Map<Long, Boolean> getThumbMapCache(String hashKey, List<String> objectIdList){
        Map<Long, Boolean> objIdHasThumbMap = getThumbMapCaffeine(hashKey, objectIdList);
        List<String> missKeys = new ArrayList<>();
        for (String objId : objectIdList) {
            if (!objIdHasThumbMap.containsKey(Long.parseLong(objId))) {
                missKeys.add(objId);
            }
        }
        if (!missKeys.isEmpty()) {
            objIdHasThumbMap.putAll(getThumbMapRedis(hashKey, missKeys));
        }
        return objIdHasThumbMap;
    }

    public Map<Long, Long> getThumbCountCaffeine(List<String> objKeyList){
        Map<Long, Long> objIdThumbCountMap = new HashMap<>();
        for (String key : objKeyList) {
            Object value = localCache.getIfPresent(key);
            if (value != null) {
                log.info("本地缓存获取到数据 {} = {}", key, value);
                // 记录访问次数（每次访问计数 +1）
                hotKeyDetector.add(key, 1);
                objIdThumbCountMap.put(Long.parseLong(key), (Long) value);
            }
        }
        return objIdThumbCountMap;
    }

    private Long objKey2objId(String objKey, String head){
        if (objKey == null || head == null || !objKey.startsWith(head)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, objKey);
        }
        if (objKey.length() <= head.length()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, objKey);
        }
        String idPart = objKey.substring(head.length());
        return Long.parseLong(idPart);
    }

    public Map<Long, Long> getThumbCountRedis(List<String> objKeyList, String head){
        Map<Long, Long> objIdThumbCountMap = new HashMap<>();
        List<String> objRedisKeyList = new ArrayList<>();
        for (String key : objKeyList) {
            objRedisKeyList.add(buildRedisKey(key));
        }

        // 获取点赞
        List<Object> thumbList = redisTemplate.opsForValue().multiGet(objRedisKeyList);
        if(thumbList == null || thumbList.isEmpty()){
            return objIdThumbCountMap;
        }
        for (int i = 0; i < thumbList.size(); i++) {
            Object thumbCount = thumbList.get(i);
            if (thumbCount == null) {
                continue;
            }
            Long value = ((Number) thumbCount).longValue();
            objIdThumbCountMap.put(objKey2objId(objKeyList.get(i), head), value);
        }
        return objIdThumbCountMap;
    }

    public Map<Long, Long> getThumbCountCache(List<String> objKeyList, String head){
        // 1. 查本地缓存
        Map<Long, Long> objIdThumbCountMap = getThumbCountCaffeine(objKeyList);
        List<String> missKeys = new ArrayList<>();
        // 2. 收集 Miss Key
        for (String key : objKeyList) {
            if (!objIdThumbCountMap.containsKey(objKey2objId(key, head))) {
                missKeys.add(key);
            }
        }
        // 3. 查 Redis
        objIdThumbCountMap.putAll(getThumbCountRedis(missKeys, head));
        return objIdThumbCountMap;
    }

    public void putThumbCountIfPresentLocal(String key, Integer count) {
        Object object = localCache.getIfPresent(key);
        if (object == null) {
            return;
        }
        Integer oldValue = (Integer) object;
        localCache.put(key, oldValue + count);
    }

}
