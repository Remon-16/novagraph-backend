package com.tech.novagraphbackendmodel.vo.user;

import com.tech.novagraphbackendmodel.user.entity.Message;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayCommentVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class MessageVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 消息状态
     */
    private String messageState;

    /**
     * 发送者 id
     */
    private Long senderId;

    /**
     * 剧本 id
     */
    private Long screenplayId;

    /**
     * 评论 id
     */
    private Long commentId;

    /**
     * 评论
     */
    private ScreenplayCommentVO screenplayCommentVO;


    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;

    public static MessageVO objToVo(Message message){
        if(message == null){
            return null;
        }
        MessageVO messageVO = new MessageVO();
        BeanUtils.copyProperties(message, messageVO);
        return messageVO;
    }
}