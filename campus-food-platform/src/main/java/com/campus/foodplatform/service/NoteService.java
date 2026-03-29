/** 探店笔记服务，处理笔记发布、草稿存储、查询和审核业务逻辑 */
package com.campus.foodplatform.service;

import com.campus.foodplatform.common.BusinessException;
import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.dto.NoteDTO;
import com.campus.foodplatform.entity.Note;
import com.campus.foodplatform.mapper.NoteMapper;
import com.campus.foodplatform.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteMapper noteMapper;
    private final ShopMapper shopMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DRAFT_KEY = "note:draft:";

    public Note publish(Long userId, NoteDTO dto) {
        if (dto.getShopId() == null || shopMapper.selectById(dto.getShopId()) == null) {
            throw new BusinessException("所选店铺不存在或已下线，请重新选择");
        }
        Note note = buildNote(userId, dto);
        note.setStatus(dto.isPublish() ? 2 : 0);  // 直接发布，无需审核
        noteMapper.insert(note);
        if (dto.isPublish()) {
            try {
                redisTemplate.delete(DRAFT_KEY + userId);
            } catch (Exception e) {
                log.warn("草稿清除失败（Redis不可用）: {}", e.getMessage());
            }
        }
        return note;
    }

    public void saveDraft(Long userId, NoteDTO dto) {
        try {
            redisTemplate.opsForValue().set(DRAFT_KEY + userId, dto, 30, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("草稿保存失败（Redis不可用）: {}", e.getMessage());
        }
    }

    public NoteDTO getDraft(Long userId) {
        try {
            return (NoteDTO) redisTemplate.opsForValue().get(DRAFT_KEY + userId);
        } catch (Exception e) {
            log.warn("草稿读取失败（Redis不可用）: {}", e.getMessage());
            return null;
        }
    }

    public Note update(Long userId, Long noteId, NoteDTO dto) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) throw new BusinessException("笔记不存在");
        if (!note.getUserId().equals(userId)) throw new BusinessException(403, "无权操作");

        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setMediaUrls(dto.getMediaUrls());
        note.setTags(dto.getTags());
        note.setStatus(dto.isPublish() ? 1 : 0);
        noteMapper.update(note);
        return note;
    }

    public void delete(Long userId, Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) throw new BusinessException("笔记不存在");
        if (!note.getUserId().equals(userId)) throw new BusinessException(403, "无权操作");
        noteMapper.deleteById(noteId);
    }

    public Note getById(Long id) {
        Note note = noteMapper.selectById(id);
        if (note == null || note.getStatus() != 2) throw new BusinessException("笔记不存在");
        noteMapper.incrementViewCount(id);
        return note;
    }

    public PageResult<Note> list(Long shopId, Long userId, Integer status, String keyword, int page, int size) {
        int offset = (page - 1) * size;
        List<Note> list = noteMapper.selectList(shopId, userId, status, keyword, offset, size);
        long total = noteMapper.countList(shopId, userId, status, keyword);
        return PageResult.of(total, list);
    }

    public void adminReview(Long noteId, Integer status) {
        noteMapper.updateStatus(noteId, status);
    }

    private Note buildNote(Long userId, NoteDTO dto) {
        Note note = new Note();
        note.setUserId(userId);
        note.setShopId(dto.getShopId());
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setType(dto.getType());
        note.setMediaUrls(dto.getMediaUrls());
        note.setTags(dto.getTags());
        return note;
    }
}
