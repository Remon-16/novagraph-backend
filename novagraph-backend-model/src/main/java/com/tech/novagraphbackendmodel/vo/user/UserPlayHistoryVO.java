package com.tech.novagraphbackendmodel.vo.user;

import com.tech.novagraphbackendcommon.cache.bean.BaseZSetVO;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserPlayHistoryVO extends BaseZSetVO {

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 剧本 id
     */
    private Long screenplayId;

    /**
     * 剧本
     */
    private ScreenplayVO screenplayVo;

}
