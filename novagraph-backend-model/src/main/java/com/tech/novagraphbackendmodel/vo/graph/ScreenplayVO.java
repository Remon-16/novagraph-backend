package com.tech.novagraphbackendmodel.vo.graph;

import cn.hutool.json.JSONUtil;
import com.tech.novagraphbackendmodel.graph.entity.Screenplay;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayWithStats;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

@Data
public class ScreenplayVO {

    private Long id;

    /**
     * 剧本名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;

    /**
     * 剧本封面连接
     */
    private String cover;

    /**
     * 剧情树的JSON字符串
     */
    private String plotTree;

    /**
     * 播放数量
     */
    private Long playCount;

    /**
     * 点赞数量
     */
    private Long thumbCount;

    /**
     * 收藏数量
     */
    private Long favoriteCount;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 用户是否对该内容点赞
     */
    private Boolean hasThumb;
    /**
     * 用户是否对该内容收藏
     */
    private Boolean hasFavorite;

    /**
     * 可见性：1-公开，2-私密...
     */
    private Integer visibility;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    public static Screenplay voToObj(ScreenplayVO screenplayVO){
        Screenplay screenplay = new Screenplay();
        BeanUtils.copyProperties(screenplayVO, screenplay);
        screenplay.setTags(JSONUtil.toJsonStr(screenplayVO.getTags()));
        return screenplay;
    }

    public static ScreenplayVO objToVo(Screenplay screenplay){
        ScreenplayVO screenplayVO = new ScreenplayVO();
        BeanUtils.copyProperties(screenplay, screenplayVO);
        // 类型不同，需要转换
        if(screenplay.getTags() != null && !screenplay.getTags().isEmpty()) {
            screenplayVO.setTags(JSONUtil.toList(screenplay.getTags(), String.class));
        }else{
            screenplayVO.setTags(List.of());
        }
        return screenplayVO;
    }

    public static List<ScreenplayVO> listObjToVo(List<Screenplay> screenplayList){
        return screenplayList.stream().map(ScreenplayVO::objToVo).toList();
    }

    public static ScreenplayVO objWithStatsToVo(ScreenplayWithStats screenplay){
        ScreenplayVO screenplayVO = new ScreenplayVO();
        BeanUtils.copyProperties(screenplay, screenplayVO);
        // 类型不同，需要转换
        if(screenplay.getTags() != null && !screenplay.getTags().isEmpty()) {
            screenplayVO.setTags(JSONUtil.toList(screenplay.getTags(), String.class));
        }else{
            screenplayVO.setTags(List.of());
        }
        return screenplayVO;
    }

    public static List<ScreenplayVO> listObjWithStatsToVo(List<ScreenplayWithStats> screenplayList){
        return screenplayList.stream().map(ScreenplayVO::objWithStatsToVo).toList();
    }
}
