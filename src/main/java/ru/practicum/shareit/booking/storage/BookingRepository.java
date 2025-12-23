package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByItemIdIn(List<Long> itemIds);

    List<Booking> findByBookerOrderByStartDesc(User booker);

    List<Booking> findByBookerAndStatusOrderByStartDesc(
            User booker, BookingStatus status);

    List<Booking> findByItemOwnerOrderByStartDesc(User owner);

    List<Booking> findByItemOwnerAndStatusOrderByStartDesc(User owner, BookingStatus status);

    // Текущие бронирования пользователч
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item " +
            "JOIN FETCH b.booker " +
            "WHERE b.booker = :booker " +
            "AND b.start <= :currentTime AND b.end >= :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentByBooker(
            @Param("booker") User booker,
            @Param("currentTime") LocalDateTime currentTime);

    // Прошлые бронирования пользователя
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item " +
            "JOIN FETCH b.booker " +
            "WHERE b.booker = :booker " +
            "AND b.end < :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findPastByBooker(
            @Param("booker") User booker,
            @Param("currentTime") LocalDateTime currentTime);

    // Будущие бронирования пользователя
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item " +
            "JOIN FETCH b.booker " +
            "WHERE b.booker = :booker " +
            "AND b.start > :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureByBooker(
            @Param("booker") User booker,
            @Param("currentTime") LocalDateTime currentTime);


    // Текущие
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "WHERE b.item.owner = :owner " +
            "AND b.start <= :currentTime " +
            "AND b.end >= :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentByItemOwner(
            @Param("owner") User owner,
            @Param("currentTime") LocalDateTime currentTime);


    // Прошлые
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "WHERE b.item.owner = :owner " +
            "AND b.end < :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findPastByItemOwner(
            @Param("owner") User owner,
            @Param("currentTime") LocalDateTime currentTime);

    // Будущие
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.booker " +
            "WHERE b.item.owner = :owner " +
            "AND b.start > :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureByItemOwner(
            @Param("owner") User owner,
            @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.booker " +
            "WHERE b.item = :item " +
            "AND b.end < :currentTime " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.end DESC " +
            "LIMIT 1")
    Optional<Booking> findLastBookingForItem(
            @Param("item") Item item,
            @Param("currentTime") LocalDateTime currentTime);


    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.booker " +
            "WHERE b.item = :item " +
            "AND b.start > :currentTime " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start ASC " +
            "LIMIT 1")
    Optional<Booking> findNextBookingForItem(
            @Param("item") Item item,
            @Param("currentTime") LocalDateTime currentTime);

    boolean existsByBookerAndItemAndEndBeforeAndStatus(
            User booker,
            Item item,
            LocalDateTime end,
            BookingStatus status
    );

}