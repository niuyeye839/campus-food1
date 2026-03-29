package com.campus.foodplatform.service;

import com.campus.foodplatform.common.BusinessException;
import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.dto.CommentDTO;
import com.campus.foodplatform.entity.Comment;
import com.campus.foodplatform.entity.Message;
import com.campus.foodplatform.entity.Note;
import com.campus.foodplatform.mapper.CommentMapper;
import com.campus.foodplatform.mapper.MessageMapper;
import com.campus.foodplatform.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final NoteMapper noteMapper;
    private final MessageMapper messageMapper;

    @Transactional
    public Comment create(Long userId, CommentDTO dto) {
        Comment comment = new Comment();
        comment.setNoteId(dto.getNoteId());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId());
        comment.setReplyUserId(dto.getReplyUserId());
        comment.setContent(dto.getContent());
        commentMapper.insert(comment);
        noteMapper.updateCommentCount(dto.getNoteId(), 1);

        Note note = noteMapper.selectById(dto.getNoteId());
        if (note != null && !note.getUserId().equals(userId)) {
            Message msg = new Message();
            msg.setUserId(note.getUserId());
            msg.setType("INTERACTION");
            msg.setTitle("新评论");
            msg.setContent("有人评论了你的笔记：" + dto.getContent());
            msg.setRelatedId(dto.getNoteId());
            messageMapper.insert(msg);
        }
        return comment;
    }

    public PageResult<Comment> listByNote(Long noteId, int page, int size) {
        int offset = (page - 1) * size;
        List<Comment> topComments = commentMapper.selectTopByNoteId(noteId, offset, size);
        long total = commentMapper.countByNoteId(noteId);
        return PageResult.of(total, topComments);
    }

    public List<Comment> getReplies(Long parentId) {
        return commentMapper.selectReplies(parentId);
    }

    @Transactional
    public void delete(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException("评论不存在");
        if (!comment.getUserId().equals(userId)) throw new BusinessException(403, "无权操作");
        commentMapper.deleteById(commentId);
        noteMapper.updateCommentCount(comment.getNoteId(), -1);
    }
}
