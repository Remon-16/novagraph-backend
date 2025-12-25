package com.tech.novagraphbackendmodel.vo.graph;

import com.tech.novagraphbackendmodel.graph.entity.ScreenplayComment;
import com.tech.novagraphbackendmodel.vo.user.UserVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
public class ScreenplayCommentVo {
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

    /**
     * 创建时间
     */
    private Date createTime;

    public static ScreenplayCommentVo objToVo(ScreenplayComment screenplayComment) {
        ScreenplayCommentVo screenplayCommentVo = new ScreenplayCommentVo();
        BeanUtils.copyProperties(screenplayComment, screenplayCommentVo);
        return screenplayCommentVo;
    }
}
