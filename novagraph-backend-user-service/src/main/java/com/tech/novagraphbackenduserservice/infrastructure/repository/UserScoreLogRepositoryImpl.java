package com.tech.novagraphbackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.user.entity.UserScoreLog;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserScoreLogRepository;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserScoreLogMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【user_score_log(用户积分变动)】的数据库操作Service实现
* @createDate 2026-01-26 17:25:27
*/
@Service
public class UserScoreLogRepositoryImpl extends ServiceImpl<UserScoreLogMapper, UserScoreLog>
    implements UserScoreLogRepository {

}




