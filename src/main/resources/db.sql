drop Database if EXISTS spring_onlineChat;

create Database spring_onlineChat;
use spring_onlineChat;

/* 用户数据表 */
drop table if EXISTS user;
create table user(
                     userId int AUTO_INCREMENT PRIMARY key,
                     username varchar(50) NOT null unique,
                     password varchar(100) NOT null,
    photo varchar(100) not null default 'waiting.jpg'
);
/* 插入测试用例 */
INSERT into user VALUES(null, 'zhangsan', '111', 'waiting.jpg');
INSERT into user VALUES(null, 'lisi', '111', 'waiting.jpg');
INSERT into user VALUES(null, 'wangwu', '111', 'waiting.jpg');
INSERT into user VALUES(null, 'an', '111', 'waiting.jpg');
INSERT into user VALUES(null, 'zhang', '111', 'waiting.jpg');
INSERT into user VALUES(null, 'is', '111', 'waiting.jpg');

/* 好友数据表，使用用户ID关联*/
drop table if EXISTS friend;
create table friend(
                       userId int NOT null,
                       friendId int NOT null,
    state int not null
);
/* 插入测试用例 */
INSERT into friend VALUES(1, 2, 1);
INSERT into friend VALUES(2, 1, 1);
INSERT into friend VALUES(1, 3, 1);
INSERT into friend VALUES(3, 1, 1);

/* 会话数据表 */
drop table if EXISTS message_session;
create table message_session(
                         sessionId int NOT null auto_increment primary key ,
                         lastTime datetime
);
/* 插入测试用例 */
INSERT into message_session VALUES(1, now());

/* 会话与用户之间的关系表 */
drop table if EXISTS message_session_user;
create table message_session_user
(
    sessionId int,
    userId int
);
/* 插入测试用例 */
INSERT into message_session_user VALUES(1, 1);
INSERT into message_session_user VALUES(1, 2);

/* 消息记录数据表 */
drop table if EXISTS message;
create table message(
                        messageId int primary key auto_increment,
                        fromId int,
                        sessionId int,
                        content varchar(2048),
                        postTime datetime
);
/* 插入测试用例 */
INSERT into message VALUES(null, 1, 1, '你好，在干嘛', now());
INSERT into message VALUES(null, 2, 1, '你好，在学习', now());