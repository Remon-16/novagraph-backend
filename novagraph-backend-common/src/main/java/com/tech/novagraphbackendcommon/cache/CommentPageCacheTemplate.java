package com.tech.novagraphbackendcommon.cache;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.cache.bean.BaseCommentVO;
import com.tech.novagraphbackendcommon.cache.bean.CommentQueryBean;
import com.tech.novagraphbackendcommon.cache.bean.CommentSaveBean;
import com.tech.novagraphbackendcommon.common.SortedCacheResult;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
@ConditionalOnClass(RedisTemplate.class)
public class CommentPageCacheTemplate extends PageCacheTemplate {

    @Resource
    private CacheManager cacheManager;

    public <R, E, V extends BaseCommentVO> Page<V> commentQuery(R request, CommentQueryBean commentQueryBean,
                                                                CommentSaveBean commentSaveBean,
                                                                Supplier<Page<E>> dbLoader, Function<List<E>, List<V>> converter){
        return super.baseQuery(
                // 参数1: Request 对象
                request,
                // 参数2: 锁 Key 生成器
                CacheUtils::getHexLockString,
                // 参数3: 查缓存逻辑
                () -> queryCache(commentQueryBean),
                // 参数4: 查数据库逻辑
                dbLoader,
                // 参数5: 转换逻辑
                converter,
                // 参数6: 写缓存逻辑
                voPage -> this.putEntityVOPage2Cache(voPage, commentSaveBean)
        );
    }

    private <V> Page<V> queryCache(CommentQueryBean commentQueryBean) {
        String sortedKey = commentQueryBean.getSortedKey();
        String sortedTotalKey = commentQueryBean.getSortedTotalKey();
        String keyHead = commentQueryBean.getKeyHead();
        String order = commentQueryBean.getOrder();
        Long page = commentQueryBean.getPage();
        Long size = commentQueryBean.getSize();
        Class VOClass = commentQueryBean.getVOClass();

        ThrowUtils.throwIf(StringUtils.isEmpty(sortedKey), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isEmpty(sortedTotalKey), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isEmpty(keyHead), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isEmpty(order), ErrorCode.PARAMS_ERROR);

        SortedCacheResult sortedCacheResult = cacheManager.querySortedValues(sortedKey, sortedTotalKey, keyHead, order, page, size);
        if(sortedCacheResult == null){
            return null;
        }
        Map<Object, Object> queryValueMap = sortedCacheResult.getValueMap();
        List<V> commentVOList = new ArrayList<>();
        for(Object value : queryValueMap.values()){
            V commentVo = (V) JSONUtil.toBean((String)value, VOClass);
            commentVOList.add(commentVo);
        }
        Page<V> commentVoPage = new Page<>();
        commentVoPage.setRecords(commentVOList);
        commentVoPage.setCurrent(page);
        commentVoPage.setSize(size);
        commentVoPage.setTotal(sortedCacheResult.getTotal());
        return commentVoPage;
    }

    private <V extends BaseCommentVO> void putEntityVOPage2Cache(Page<V> commentVoPage, CommentSaveBean commentSaveBean){
        String sortedKey = commentSaveBean.getSortedKey();
        String sortedTotalKey = commentSaveBean.getSortedTotalKey();
        String commentKeyHead = commentSaveBean.getCommentKeyHead();
        Long total = commentSaveBean.getTotal();
        commentVoPage.getRecords().forEach(commentVo -> {
            Double score = (double)commentVo.getCreateTime().getTime();
            cacheManager.zSetAdd(sortedKey, commentVo.getId(), score);
            cacheManager.putValueToCache(CacheUtils.getCacheKey(commentKeyHead, commentVo.getId().toString()),
                    JSONUtil.toJsonStr(commentVo), cacheManager.getRedisZSetExpireTime());
        });
        cacheManager.putValueToCache(sortedTotalKey, total, cacheManager.getRedisZSetExpireTime());
    }
}
