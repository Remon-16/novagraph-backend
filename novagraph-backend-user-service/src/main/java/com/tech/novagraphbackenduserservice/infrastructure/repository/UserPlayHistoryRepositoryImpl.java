package com.tech.novagraphbackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.user.entity.UserPlayHistory;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserPlayHistoryRepository;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserPlayHistoryMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【user_play_history(用户播放历史)】的数据库操作Service实现
* @createDate 2026-01-26 17:25:06
*/
@Service
public class UserPlayHistoryRepositoryImpl extends ServiceImpl<UserPlayHistoryMapper, UserPlayHistory>
    implements UserPlayHistoryRepository {

}




