-- 创建库
create database if not exists novagraph_base;

-- 切换库
use novagraph_base;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(1024)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    roomCode     varchar(256)                           not null comment '房间号',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',

    UNIQUE KEY uk_userAccount (userAccount),
    UNIQUE KEY uk_roomCode (roomCode),
    INDEX idx_userName (userName),        -- 提升基于用户名的查询性能
    INDEX idx_roomCode (roomCode)         -- 提升基于用户房间号的查询性能
) comment '用户' collate = utf8mb4_unicode_ci;

-- 图片表
create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    category     varchar(64)                        null comment '分类',
    tags         varchar(512)                       null comment '标签（JSON 数组）',
    picSize      bigint                             null comment '图片体积',
    picWidth     int                                null comment '图片宽度',
    picHeight    int                                null comment '图片高度',
    picScale     double                             null comment '图片宽高比例',
    picFormat    varchar(32)                        null comment '图片格式',
    thumbnailUrl varchar(512)                       null comment '缩略图 url',
    userId       bigint                             not null comment '创建用户 id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',

    INDEX idx_name (name),                 -- 提升基于图片名称的查询性能
    INDEX idx_category (category),         -- 提升基于分类的查询性能
    INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
    INDEX idx_userId (userId)              -- 提升基于用户 ID 的查询性能
) comment '图片' collate = utf8mb4_unicode_ci;

-- 剧本表
create table if not exists screenplay
(
    id           bigint auto_increment comment 'id' primary key,
    name         varchar(256)                           null comment '剧本名称',
    introduction varchar(512)                           null comment '简介',
    category     varchar(64)                            null comment '分类',
    tags         varchar(512)                           null comment '标签（JSON 数组）',
    cover        varchar(1024)                          null comment '剧本封面链接',
    plotTree     longtext                               null comment '剧情树的JSON字符串',
    userId       bigint                                 not null comment '用户 id',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',

    INDEX idx_screenplayName (name),       -- 提升基于剧本名称的查询性能
    INDEX idx_category (category),         -- 提升基于分类的查询性能
    INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
    INDEX idx_userId (userId)              -- 提升基于用户 ID 的查询性能
) comment '剧本表' collate = utf8mb4_unicode_ci;

-- 剧本-章节
create table if not exists screenplay_section
(
    id           bigint auto_increment comment 'id'     primary key,
    sectionName  varchar(256)                           null comment '章节名称',
    content      longtext                               not null comment '内容',
    screenplayId bigint                                 not null comment '剧本Id',
    userId       bigint                                 not null comment '用户 id',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',

    INDEX idx_sectionName (sectionName),    -- 提升基于剧情章节的查询性能
    INDEX idx_userId (userId)               -- 提升基于用户 ID 的查询性能
) comment '剧本章节' collate = utf8mb4_unicode_ci;

-- 剧本统计数据 方便剧本和统计数据在缓存上使用不同方案
create table if not exists screenplay_statistics
(
    id             bigint auto_increment comment 'id' primary key,
    screenplayId   bigint                                 not null comment '剧本 id',
    playCount      bigint       default 0                 null comment '播放数量',
    thumbCount     bigint       default 0                 null comment '点赞数量',
    favoriteCount  bigint       default 0                 null comment '收藏数量',
    editTime       datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint      default 0                 not null comment '是否删除',

    INDEX idx_screenplayId (screenplayId)       -- 提升基于剧本的查询效率
) comment '剧本统计数据' collate = utf8mb4_unicode_ci;

-- 剧本点赞记录表
create table if not exists screenplay_thumb
(
    id           bigint auto_increment                  primary key,
    userId       bigint                                 not null comment '用户ID',
    screenplayId bigint                                 not null comment '剧本ID',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',

    INDEX screenplay_thumb (screenplayId)       -- 提升基于动态的查询效率
)comment '剧本点赞记录表' collate = utf8mb4_unicode_ci;

-- 剧本评论表
create table if not exists screenplay_comment(
    id           bigint auto_increment primary key,
    userId       bigint                                 not null comment '用户 id',
    screenplayId bigint                                 not null comment '剧本 id',
    targetId     bigint                                 null comment '目标 id 为空代表是直接评论在剧本上，不为空说明是多级评论',
    secondTargetId bigint                               null comment '二级目标评论Id',
    content      varchar(2048)                          not null comment '评论内容',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',

    INDEX idx_userId (userId),       -- 提升基于用户的查询效率
    INDEX idx_screenplayId_target (screenplayId, targetId), -- 优化直接评论查询
    INDEX idx_target_time (targetId, createTime), -- 优化子评论查询
    INDEX idx_screenplayId (screenplayId)       -- 提升基于用户的查询效率
)comment '剧本评论表' collate = utf8mb4_unicode_ci;

-- 消息表
create table if not exists message(
    id            bigint auto_increment primary key,
    userId        bigint                                 not null comment '用户 id',
    content       longtext                               not null comment '消息内容',
    messageType   varchar(128)                           not null comment '消息类型',
    messageState  varchar(128)                           not null comment '消息状态', -- 0 未读 1 已读
    senderId      bigint                                 not null comment '发送者 id', -- 系统消息用户： system001
    screenplayId  bigint                                 null  comment '剧本 id',
    commentId     bigint                                 null  comment '评论 id',
    createTime    datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    editTime      datetime default CURRENT_TIMESTAMP     not null comment '编辑时间',
    updateTime    datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint  default 0                     not null comment '是否删除',

    INDEX idx_userId (userId)       -- 提升基于用户的查询效率
)comment '消息表' collate = utf8mb4_unicode_ci;

-- 用户关注和用户粉丝
create table if not exists user_follow
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint                                 not null comment '用户 id',
    following_id bigint                                 null comment '用户的关注',
    follower_id  bigint                                 null comment '用户的粉丝',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',

    INDEX idx_userId (userId)       -- 提升基于用户的查询效率
) comment '用户关注表' collate = utf8mb4_unicode_ci;

-- 用户统计数据 方便用户信息和用户统计数据在缓存上使用不同方案
create table if not exists user_statistics
(
    id             bigint auto_increment comment 'id' primary key,
    userId         bigint                                 not null comment '用户 id',
    userScore      bigint       default 0                 null comment '积分余额',
    thumbCount     bigint       default 0                 null comment '点赞数量',
    favoriteCount  bigint       default 0                 null comment '收藏数量',
    followingCount bigint       default 0                 null comment '关注数量',
    followerCount  bigint       default 0                 null comment '粉丝数量',
    editTime       datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint      default 0                 not null comment '是否删除',

    INDEX idx_userId (userId)       -- 提升基于用户的查询效率
) comment '用户统计数据' collate = utf8mb4_unicode_ci;

-- 用户积分变动
create table if not exists user_score_log
(
    id           bigint auto_increment primary key,
    userId       bigint                                 not null comment '用户 id',
    scoreAmount  bigint                                 not null comment '积分变动值',
    scoreType    varchar(128)                           not null comment '积分变动类型',
    createTime   datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP     not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                     not null comment '是否删除',

    INDEX idx_userId (userId)       -- 提升基于用户的查询效率
)comment '用户积分变动' collate = utf8mb4_unicode_ci;

-- 用户播放历史
create table if not exists user_play_history
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint                                 not null comment '用户 id',
    screenplayId bigint                                 not null comment '剧本 id',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',

    INDEX idx_userId (userId),                  -- 提升基于用户的查询效率
    INDEX idx_screenplayId (screenplayId),      -- 提升基于剧本的查询效率
    INDEX idx_createTime (createTime)           -- 提升基于创建时间的查询效率
) comment '用户播放历史' collate = utf8mb4_unicode_ci;

-- 用户收藏夹
create table if not exists user_favorite_folder
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint                                 not null comment '用户 id',
    folderName   varchar(256)                           not null comment '收藏夹名称',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',

    INDEX idx_userId (userId),                -- 提升基于用户的查询效率
    INDEX idx_createTime (createTime)         -- 提升基于创建时间的查询效率
) comment '用户收藏夹' collate = utf8mb4_unicode_ci;

-- 用户收藏
create table if not exists user_favorite
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint                                 not null comment '用户 id',
    folderId     bigint                                 not null comment '收藏夹 id',
    screenplayId bigint                                 not null comment '剧本 id',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',

    INDEX idx_userId (userId),                -- 提升基于用户的查询效率
    INDEX idx_folderId (folderId),            -- 提升基于收藏夹的查询效率
    INDEX idx_createTime (createTime)         -- 提升基于创建时间的查询效率
) comment '用户收藏' collate = utf8mb4_unicode_ci;

-- 用户动态
CREATE TABLE user_post (
     id           bigint auto_increment primary key,
     userId       bigint                                 not null comment '用户 id',
     content      text                                   not null comment '文本内容',
     postType     varchar(128)                          not null comment '类型：text-文字，screenplay-剧本，post-动态...',
     quotedId     bigint                                 null comment '引用 id',
     visibility   tinyint  default 0                     not null comment  '可见性：1-公开，2-私密...',

     createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
     editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
     updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
     isDelete     tinyint  default 0                 not null comment '是否删除',

     INDEX idx_user_id (userId),
     INDEX idx_quoted_id (quotedId),
     INDEX idx_create_time (createTime)
) comment '用户动态表' collate = utf8mb4_unicode_ci;

-- 动态评论
create table if not exists user_post_comment(
     id           bigint auto_increment primary key,
     userId       bigint                                 not null comment '用户 id',
     postId       bigint                                 not null comment '动态 id',
     targetId     bigint                                 null comment '目标 id 为空代表是直接评论在剧本上，不为空说明是多级评论',
     secondTargetId bigint                               null comment '二级目标评论Id',
     content      varchar(2048)                          not null comment '评论内容',
     createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
     editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
     updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
     isDelete     tinyint  default 0                 not null comment '是否删除',

     INDEX idx_userId (userId),       -- 提升基于用户的查询效率
     INDEX idx_postId_target (postId, targetId), -- 优化直接评论查询
     INDEX idx_target_time (targetId, createTime), -- 优化子评论查询
     INDEX idx_postId (postId)       -- 提升基于动态 ID 的查询效率
)comment '动态评论表' collate = utf8mb4_unicode_ci;

-- 动态统计数据 方便动态数据在缓存上使用不同方案
create table if not exists user_post_statistics
(
    id             bigint auto_increment comment 'id' primary key,
    postId         bigint                                 not null comment '动态 id',
    userScore      bigint       default 0                 null comment '积分余额',
    thumbCount     bigint       default 0                 null comment '点赞数量',
    commentCount   bigint       default 0                 null comment '评论数量',
    shareCount     bigint       default 0                 null comment '分享数量',
    quotedCount    bigint       default 0                 null comment '引用数量',
    editTime       datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint      default 0                 not null comment '是否删除',

    INDEX idx_userId (postId)       -- 提升基于动态的查询效率
) comment '动态统计数据' collate = utf8mb4_unicode_ci;

-- 动态点赞记录表
create table if not exists user_post_thumb
(
    id           bigint auto_increment                  primary key,
    userId       bigint                                 not null comment '用户ID',
    postId       bigint                                 not null comment '动态ID',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',

    INDEX user_post_thumb (postId)       -- 提升基于动态的查询效率
)comment '动态点赞记录表' collate = utf8mb4_unicode_ci;
