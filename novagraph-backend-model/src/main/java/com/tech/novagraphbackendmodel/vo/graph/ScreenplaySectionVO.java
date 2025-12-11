package com.tech.novagraphbackendmodel.vo.graph;

import com.tech.novagraphbackendmodel.graph.entity.ScreenplaySection;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
public class ScreenplaySectionVO {

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
    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    public static ScreenplaySectionVO ObjToVo(ScreenplaySection screenplaySection) {
        ScreenplaySectionVO screenplaySectionVO = new ScreenplaySectionVO();
        BeanUtils.copyProperties(screenplaySection, screenplaySectionVO);
        return screenplaySectionVO;
    }

    public static ScreenplaySection voToObj(ScreenplaySectionVO screenplaySectionVO) {
        ScreenplaySection screenplaySection = new ScreenplaySection();
        BeanUtils.copyProperties(screenplaySectionVO, screenplaySection);
        return screenplaySection;
    }
}
