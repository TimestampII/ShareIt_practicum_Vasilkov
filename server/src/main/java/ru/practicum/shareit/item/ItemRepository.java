package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwnerId(Long ownerId);

    List<Item> findAllByRequest_Id(Long requestId);

    List<Item> findAllByRequest_IdIn(List<Long> requestIds);

    // Запросный метод не подходит: нужно искать по двум полям через "или",
    // без учёта регистра, поэтому запрос описан вручную через JPQL
    @Query("select i from Item i " +
            "where i.available = true " +
            "and (upper(i.name) like upper(concat('%', :text, '%')) " +
            "or upper(i.description) like upper(concat('%', :text, '%')))")
    List<Item> search(@Param("text") String text);
}
