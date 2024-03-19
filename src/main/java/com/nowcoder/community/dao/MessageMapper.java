package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    //查询当前用户的会话列表，针对每个会话只返回最新的私信
    List<Message> selectConversations(int userId,int offset,int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId,int offset,int limit);

    //查询某个会话包含的私信
    int selectLetterCount(String conversationId);

    //查询未读私信数量
    int selectLetterUnreadCount(int userId,String conversationId);

    //新增消息
    int insertMessage(Message message);

    //修改消息的状态
    int updateStatus(List<Integer> ids,int status);

    //查询某主题下最新通知
    Message selectLatestNotice(int userId,String topic);

    //查询某个主题所包含的数量
    int selectNoticeCount(int userId,String topic);

    //查询未读的通知数量
    int selectNoticeUnreadCount(int userId,String topic);

    //查询某个主题的通知列表
    List<Message> selectNotices(int userId,String topic,int offset,int limit);

}
