package com.tech.novagraphbackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.user.entity.UserStatistics;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserStatisticsRepository;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserStatisticsMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【user_statistics(用户统计数据)】的数据库操作Service实现
* @createDate 2026-01-26 17:25:30
*/
@Service
public class UserStatisticsRepositoryImpl extends ServiceImpl<UserStatisticsMapper, UserStatistics>
    implements UserStatisticsRepository {

}




