package com.tech.novagraphbackenduserservice.infrastructure.mapper;

import com.tech.novagraphbackendmodel.user.entity.UserPlayHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Remon
* @description 针对表【user_play_history(用户播放历史)】的数据库操作Mapper
* @createDate 2026-01-26 17:25:06
* @Entity com.tech.novagraphbackendmodel.user.entity.UserPlayHistory
*/
public interface UserPlayHistoryMapper extends BaseMapper<UserPlayHistory> {
    void batchIgnoreInsert(@Param("userPlayHistories")List<UserPlayHistory> userPlayHistories);
}




