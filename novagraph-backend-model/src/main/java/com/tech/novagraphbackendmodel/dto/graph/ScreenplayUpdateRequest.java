package com.tech.novagraphbackendmodel.dto.graph;

import cn.hutool.json.JSONUtil;
import com.tech.novagraphbackendmodel.graph.entity.Screenplay;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
public class ScreenplayUpdateRequest {

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
     * 用户 id
     */
    private Long userId;

    public static Screenplay resToObj(ScreenplayUpdateRequest screenplayUpdateRequest){
        Screenplay screenplay = new Screenplay();
        BeanUtils.copyProperties(screenplayUpdateRequest, screenplay);
        List<String> tags = screenplayUpdateRequest.getTags();
        screenplay.setTags(JSONUtil.toJsonStr(tags));
        return screenplay;
    }
}
