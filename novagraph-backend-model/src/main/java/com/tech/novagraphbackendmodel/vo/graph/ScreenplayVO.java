package com.tech.novagraphbackendmodel.vo.graph;

import cn.hutool.json.JSONUtil;
import com.tech.novagraphbackendmodel.graph.entity.Screenplay;
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
     * 点赞数量
     */
    private Long thumbCount;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 用户是否对该内容点赞
     */
    private Boolean hasThumb;

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
}
