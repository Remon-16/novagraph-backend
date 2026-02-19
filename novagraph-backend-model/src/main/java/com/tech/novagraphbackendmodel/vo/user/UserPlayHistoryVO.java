package com.tech.novagraphbackendmodel.vo.user;

import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;
import lombok.Data;

import java.util.Date;

@Data
public class UserPlayHistoryVO {
    private Long id;

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

    /**
     * 创建时间
     */
    private Date createTime;
}
