package com.tech.novagraphbackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.user.entity.UserPostStatistics;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserPostStatisticsRepository;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserPostStatisticsMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【user_post_statistics(动态统计数据)】的数据库操作Service实现
* @createDate 2026-01-26 17:25:18
*/
@Service
public class UserPostStatisticsRepositoryImpl extends ServiceImpl<UserPostStatisticsMapper, UserPostStatistics>
    implements UserPostStatisticsRepository {

}




