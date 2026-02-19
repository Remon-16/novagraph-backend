package com.tech.novagraphbackendmodel.vo.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.cache.bean.BaseCommentVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserPostCommentRootVO extends BaseCommentVO {
    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 剧本 id
     */
    private Long postId;

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
    Page<UserPostCommentVO> userPostCommentVOPage;
}
