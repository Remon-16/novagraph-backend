package com.tech.novagraphbackendmodel.vo.user;

import com.tech.novagraphbackendmodel.user.entity.UserFavoriteFolder;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class UserFavoriteFolderVO {
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 收藏夹名称
     */
    private String folderName;

    /**
     * 创建时间
     */
    private Date createTime;

    public static UserFavoriteFolderVO objToVo(UserFavoriteFolder userFavoriteFolder){
        UserFavoriteFolderVO userFavoriteFolderVO = new UserFavoriteFolderVO();
        BeanUtils.copyProperties(userFavoriteFolder, userFavoriteFolderVO);
        return userFavoriteFolderVO;
    }

    public static List<UserFavoriteFolderVO> listObjToVo(List<UserFavoriteFolder> userFavoriteFolderList){
        if(userFavoriteFolderList == null || userFavoriteFolderList.isEmpty()){
            return new ArrayList<>();
        }
        return userFavoriteFolderList.stream().map(UserFavoriteFolderVO::objToVo).toList();
    }

    public static UserFavoriteFolder voToObj(UserFavoriteFolderVO userFavoriteFolderVO){
        UserFavoriteFolder userFavoriteFolder = new UserFavoriteFolder();
        BeanUtils.copyProperties(userFavoriteFolderVO, userFavoriteFolder);
        return userFavoriteFolder;
    }
}
