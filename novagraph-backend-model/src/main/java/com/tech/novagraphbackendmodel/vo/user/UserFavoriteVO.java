package com.tech.novagraphbackendmodel.vo.user;

import com.tech.novagraphbackendmodel.user.entity.UserFavorite;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
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
     * 收藏夹名称
     */
    private String folderName;

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

    public static UserFavoriteVO objToVo(UserFavorite userFavorite){
        UserFavoriteVO vo = new UserFavoriteVO();
        BeanUtils.copyProperties(userFavorite, vo);
        return vo;
    }

    public static List<UserFavoriteVO> listObjToVo(List<UserFavorite> userFavoriteList){
        return userFavoriteList.stream().map(UserFavoriteVO::objToVo).toList();
    }
}
