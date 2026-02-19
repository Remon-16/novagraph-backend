package com.tech.novagraphbackendmodel.vo.user;

import com.tech.novagraphbackendcommon.cache.bean.BaseCommentVO;
import com.tech.novagraphbackendmodel.user.entity.UserPostComment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserPostCommentVO extends BaseCommentVO {

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 剧本 id
     */
    private Long postId;

    /**
     * 目标 id 为空代表是直接评论在剧本上，不为空说明是多级评论
     */
    private Long targetId;

    /**
     * 二级目标 id 为空代表是二级评论，不为空说明是三级评论
     */
    private Long secondTargetId;

    /**
     * 目标用户的Id
     */
    private Long targetUserId;

    /**
     * 目标用户的昵称
     */
    private String targetUserName;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 用户信息
     */
    private UserVO user;

    public static UserPostCommentVO objToVo(UserPostComment userPostComment) {
        UserPostCommentVO userPostCommentVO = new UserPostCommentVO();
        BeanUtils.copyProperties(userPostComment, userPostCommentVO);
        return userPostCommentVO;
    }
}
