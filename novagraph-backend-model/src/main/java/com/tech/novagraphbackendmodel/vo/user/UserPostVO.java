package com.tech.novagraphbackendmodel.vo.user;

import com.tech.novagraphbackendmodel.user.entity.UserPost;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
public class UserPostVO {
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 文本内容
     */
    private String content;

    /**
     * 类型：text-文字，screenplay-剧本，post-动态...
     */
    private String postType;

    /**
     * 引用 id
     */
    private Long quotedId;

    /**
     * 可见性：1-公开，2-私密...
     */
    private Integer visibility;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 用户是否对该内容点赞
     */
    private Boolean hasThumb;

    public static UserPost voToObj(UserPostVO userPostVO){
        UserPost userPost = new UserPost();
        BeanUtils.copyProperties(userPostVO, userPost);
        return userPost;
    }

    public static UserPostVO objToVo(UserPost userPost){
        UserPostVO userPostVO = new UserPostVO();
        BeanUtils.copyProperties(userPost, userPostVO);
        return userPostVO;
    }
}
