package com.tech.novagraphbackendmodel.vo.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.cache.bean.BaseZSetVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserPostZSetRootVO extends BaseZSetVO {
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
    Page<UserPostZSetVO> userPostCommentVOPage;
}
