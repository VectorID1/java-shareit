package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByItem_IdIn(List<Long> itemIds);

    List<Booking> findByBookerOrderByStartDesc(User booker);

    List<Booking> findByBookerAndStatusOrderByStartDesc(
            User booker, BookingStatus status);

    List<Booking> findByItemOwnerOrderByStartDesc(User owner);

    List<Booking> findByItemOwnerAndStatusOrderByStartDesc(User owner, BookingStatus status);

    // Текущие бронирования пользователч
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker bk " +
            "WHERE bk.id = :bookerId " +
            "AND b.start <= :currentTime " +
            "AND b.end >= :currentTime " +
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


    // Текущие
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.start <= :currentTime AND " +
            "b.end >= :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentByItemOwner(
            @Param("ownerId") Long ownerId,
            @Param("currentTime") LocalDateTime currentTime);


    // Прошлые
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker " +
            "WHERE i.owner.id = :ownerId AND " +
            "b.end < :currentTime " +
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
            "AND b.status = 'APPROVED'")
    boolean existsByBookerIdAndItemIdAndEndBefore(
            @Param("bookerId") Long bookerId,
            @Param("itemId") Long itemId,
            @Param("currentTime") LocalDateTime currentTime);


//    boolean existsByBookerAndItemAndStatusAndEndBefore(
//            User booker,
//            Item item,
//            BookingStatus status,
//            LocalDateTime end
//
//    );

}