package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByItem_Id(Long itemId);

    // Batch-версия для GET /items - все комментарии владельца одним запросом,
    // вместо отдельного запроса на каждую вещь.
    List<Comment> findByItem_IdIn(List<Long> itemIds);
}
