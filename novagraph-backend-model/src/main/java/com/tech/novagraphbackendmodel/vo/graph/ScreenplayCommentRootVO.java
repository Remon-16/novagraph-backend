package com.tech.novagraphbackendmodel.vo.graph;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.cache.bean.BaseZSetVO;
import com.tech.novagraphbackendmodel.vo.user.UserVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ScreenplayCommentRootVO extends BaseZSetVO {
    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 剧本 id
     */
    private Long screenplayId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 用户信息
     */
    private UserVO user;

    /**
     * 子评论
     */
    Page<ScreenplayCommentVO> screenplayCommentVoPage;
}
