package com.tech.novagraphbackendmodel.vo.user;

import com.tech.novagraphbackendmodel.user.entity.UserFavorite;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class UserFavoriteVO {

    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 收藏夹 id
     */
    private Long folderId;

    /**
     * 剧本 id
     */
    private Long screenplayId;

    /**
     * 创建时间
     */
    private Date createTime;

    public static UserFavoriteVO objToVo(UserFavorite userFavorite){
        UserFavoriteVO vo = new UserFavoriteVO();
        BeanUtils.copyProperties(userFavorite, vo);
        return vo;
    }

    public static List<UserFavoriteVO> listObjToVo(List<UserFavorite> userFavoriteList){
        return userFavoriteList.stream().map(UserFavoriteVO::objToVo).toList();
    }
}
