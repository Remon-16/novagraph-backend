package com.tech.novagraphbackendmodel.vo.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tech.novagraphbackendmodel.user.entity.UserWithStats;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
public class LoginUserVO {

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * token
     */
    private String token;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 积分余额
     */
    private Long userScore;

    /**
     * 关注数量
     */
    private Long followingCount;

    /**
     * 粉丝数量
     */
    private Long followerCount;

    private Long playHistoryCount;

    private Long userFavoriteCount;

    public static LoginUserVO objWithStateToVO(UserWithStats user) {
        if (user == null) {
            return null;
        }
        LoginUserVO vo = new LoginUserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
