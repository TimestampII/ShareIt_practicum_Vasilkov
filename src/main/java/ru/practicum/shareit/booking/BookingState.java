package ru.practicum.shareit.booking;

// Отдельное перечисление от BookingStatus: это значения параметра "state"
// в запросе (что показать пользователю), а не хранимый статус бронирования.
// ALL/CURRENT/PAST/FUTURE описывают выборку по датам, а не поле в БД.
public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED
}
