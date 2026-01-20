package com.tech.novagraphbackendmodel.vo.graph;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.vo.user.UserVO;
import lombok.Data;

import java.util.Date;

@Data
public class ScreenplayCommentRootVO {
    /**
     * id
     */
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
     * 评论内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 用户信息
     */
    private UserVO user;

    /**
     * 子评论
     */
    Page<ScreenplayCommentVO> screenplayCommentVoPage;
}
