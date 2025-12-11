package com.tech.novagraphbackendmodel.dto.graph;

import com.tech.novagraphbackendmodel.graph.entity.ScreenplaySection;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class ScreenplaySectionAddRequest {

    private Long id;

    /**
     * 章节名称
     */
    private String sectionName;

    /**
     * 内容内容
     */
    private String content;

    /**
     * 剧本Id
     */
    private Long screenplayId;

    /**
     * 用户 id
     */
    private Long userId;

    public static ScreenplaySection dtoToObj(ScreenplaySectionAddRequest screenplaySectionAddRequest){
        ScreenplaySection screenplaySection = new ScreenplaySection();
        BeanUtils.copyProperties(screenplaySectionAddRequest, screenplaySection);
        return screenplaySection;
    }
}
