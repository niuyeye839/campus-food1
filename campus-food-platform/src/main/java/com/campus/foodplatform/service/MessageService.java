package com.campus.foodplatform.service;

import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.entity.Message;
import com.campus.foodplatform.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;

    public PageResult<Message> list(Long userId, String type, int page, int size) {
        int offset = (page - 1) * size;
        List<Message> list = messageMapper.selectByType(userId, type, offset, size);
        return PageResult.of(list.size(), list);
    }

    public int unreadCount(Long userId) {
        return messageMapper.countUnread(userId);
    }

    public void markRead(Long userId, String type) {
        messageMapper.markReadByType(userId, type);
    }
}
