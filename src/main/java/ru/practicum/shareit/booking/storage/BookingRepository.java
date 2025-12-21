package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker " +
            "WHERE b.id = :bookingId AND i.owner.id = :userId")
    Optional<Booking> findByIdAndOwnerId(
            @Param("bookingId") Long bookingId,
            @Param("userId") Long userId);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker bk " +
            "WHERE b.id = :bookingId " +
            "AND (bk.id = :userId OR i.owner.id = :userId)")
    Optional<Booking> findByIdAndBookerIdOrOwnerId(
            @Param("bookingId") Long bookingId,
            @Param("userId") Long userId);

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(
            Long bookerId, BookingStatus status);

    // Текущие бронирования пользователч
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker bk " +
            "WHERE bk.id = :bookerId " +
            "AND b.start <= :currentTime AND b.end >= :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentByBooker(
            @Param("bookerId") Long bookerId,
            @Param("currentTime") LocalDateTime currentTime);

    // Прошлые бронирования пользователя
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker bk " +
            "WHERE bk.id = :bookerId " +
            "AND b.end < :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findPastByBooker(
            @Param("bookerId") Long bookerId,
            @Param("currentTime") LocalDateTime currentTime);

    // Будущие бронирования пользователя
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker bk " +
            "WHERE bk.id = :bookerId " +
            "AND b.start > :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureByBooker(
            @Param("bookerId") Long bookerId,
            @Param("currentTime") LocalDateTime currentTime);

    //Все брони владельца
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker " +
            "WHERE i.owner.id = :ownerId " +
            "ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdOrderByStartDesc(
            @Param("ownerId") Long ownerId);

    //По статусу
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker " +
            "WHERE i.owner.id = :ownerId AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(
            @Param("ownerId") Long ownerId,
            @Param("status") BookingStatus status);

    // Текущие
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.start <= :currentTime AND b.end >= :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentByItemOwner(
            @Param("ownerId") Long ownerId,
            @Param("currentTime") LocalDateTime currentTime);

    // Прошлые
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker " +
            "WHERE i.owner.id = :ownerId AND b.end < :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findPastByItemOwner(
            @Param("ownerId") Long ownerId,
            @Param("currentTime") LocalDateTime currentTime);

    // Будущие
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker " +
            "WHERE i.owner.id = :ownerId AND b.start > :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureByItemOwner(
            @Param("ownerId") Long ownerId,
            @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker bk " +
            "WHERE i.id = :itemId " +
            "AND b.end < :currentTime " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.end DESC " +
            "LIMIT 1")
    Optional<Booking> findLastBookingForItem(
            @Param("itemId") Long itemId,
            @Param("currentTime") LocalDateTime currentTime);


    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker bk " +
            "WHERE i.id = :itemId " +
            "AND b.start > :currentTime " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start ASC " +
            "LIMIT 1")
    Optional<Booking> findNextBookingForItem(
            @Param("itemId") Long itemId,
            @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND b.item.id = :itemId " +
            "AND b.end < :currentTime " +
            "AND b.status = 'APPROVED' ")
    boolean existsByBookerIdAndItemIdAndEndBefore(
            @Param("bookerId") Long bookerId,
            @Param("itemId") Long itemId,
            @Param("currentTime") LocalDateTime currentTime);

}