package com.tech.novagraphbackendcommon.cache;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.cache.bean.BaseZSetVO;
import com.tech.novagraphbackendcommon.cache.bean.ZSetQueryBean;
import com.tech.novagraphbackendcommon.cache.bean.ZSetSaveBean;
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
public class ZSetPageCacheTemplate extends PageCacheTemplate {

    @Resource
    private CacheManager cacheManager;

    public <R, E, V extends BaseZSetVO> Page<V> zSetQuery(R request, ZSetQueryBean ZSetQueryBean,
                                                          ZSetSaveBean zsetSaveBean,
                                                          Supplier<Page<E>> dbLoader, Function<List<E>, List<V>> converter){
        return super.baseQuery(
                // 参数1: Request 对象
                request,
                // 参数2: 锁 Key 生成器
                CacheUtils::getHexLockString,
                // 参数3: 查缓存逻辑
                () -> queryCache(ZSetQueryBean),
                // 参数4: 查数据库逻辑
                dbLoader,
                // 参数5: 转换逻辑
                converter,
                // 参数6: 写缓存逻辑
                voPage -> this.putEntityVOPage2Cache(voPage, zsetSaveBean)
        );
    }

    private <V> Page<V> queryCache(ZSetQueryBean ZSetQueryBean) {
        String sortedKey = ZSetQueryBean.getSortedKey();
        String sortedTotalKey = ZSetQueryBean.getSortedTotalKey();
        String keyHead = ZSetQueryBean.getValueKeyHead();
        String order = ZSetQueryBean.getOrder();
        Long page = ZSetQueryBean.getPage();
        Long size = ZSetQueryBean.getSize();
        Class VOClass = ZSetQueryBean.getVOClass();

        ThrowUtils.throwIf(StringUtils.isEmpty(sortedKey), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isEmpty(sortedTotalKey), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isEmpty(keyHead), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isEmpty(order), ErrorCode.PARAMS_ERROR);

        SortedCacheResult sortedCacheResult = cacheManager.querySortedValues(sortedKey, sortedTotalKey, keyHead, order, page, size);
        if(sortedCacheResult == null){
            return null;
        }
        Map<Object, Object> queryValueMap = sortedCacheResult.getValueMap();
        List<V> valueVOList = new ArrayList<>();
        for(Object value : queryValueMap.values()){
            V valueVo = (V) JSONUtil.toBean((String)value, VOClass);
            valueVOList.add(valueVo);
        }
        Page<V> valueVoPage = new Page<>();
        valueVoPage.setRecords(valueVOList);
        valueVoPage.setCurrent(page);
        valueVoPage.setSize(size);
        valueVoPage.setTotal(sortedCacheResult.getTotal());
        return valueVoPage;
    }

    private <V extends BaseZSetVO> void putEntityVOPage2Cache(Page<V> valueVoPage, ZSetSaveBean zsetSaveBean){
        String sortedKey = zsetSaveBean.getSortedKey();
        String sortedTotalKey = zsetSaveBean.getSortedTotalKey();
        String valueKeyHead = zsetSaveBean.getValueKeyHead();
        Long total = zsetSaveBean.getTotal();
        valueVoPage.getRecords().forEach(valueVo -> {
            Double score = (double)valueVo.getCreateTime().getTime();
            cacheManager.zSetAdd(sortedKey, valueVo.getId(), score);
            cacheManager.putValueToCache(CacheUtils.getCacheKey(valueKeyHead, valueVo.getId().toString()),
                    JSONUtil.toJsonStr(valueVo), cacheManager.getRedisZSetExpireTime());
        });
        cacheManager.putValueToCache(sortedTotalKey, total, cacheManager.getRedisZSetExpireTime());
    }
}
