package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // --- Бронирования конкретного пользователя как арендатора (booker) ---

    List<Booking> findByBooker_IdOrderByStartDesc(Long bookerId);

    List<Booking> findByBooker_IdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findByBooker_IdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findByBooker_IdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime now1, LocalDateTime now2);

    // --- Бронирования вещей, которыми владеет пользователь (owner) ---
    // Item_Owner_Id - переход через связь Booking -> Item -> User(owner)

    List<Booking> findByItem_Owner_IdOrderByStartDesc(Long ownerId);

    List<Booking> findByItem_Owner_IdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);

    List<Booking> findByItem_Owner_IdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findByItem_Owner_IdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime now1, LocalDateTime now2);

    // --- Для отображения дат бронирования у вещи (lastBooking/nextBooking) ---
    // Отдельный метод для одной вещи и batch-версия для списка вещей -
    // чтобы при просмотре GET /items не получить N+1 запросов на каждую вещь владельца.

    List<Booking> findByItem_IdAndStatusOrderByStartAsc(Long itemId, BookingStatus status);

    List<Booking> findByItem_IdInAndStatusOrderByStartAsc(List<Long> itemIds, BookingStatus status);

    // --- Для проверки права оставить отзыв: пользователь брал вещь в аренду,
    // бронирование подтверждено и срок аренды уже закончился ---

    boolean existsByBooker_IdAndItem_IdAndStatusAndEndBefore(
            Long bookerId, Long itemId, BookingStatus status, LocalDateTime now);
}
