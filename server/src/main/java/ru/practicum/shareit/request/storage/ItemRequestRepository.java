package ru.practicum.shareit.request.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(Long requesterId);

    @Query("SELECT DISTINCT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.items i " +
            "LEFT JOIN FETCH i.owner " +
            "LEFT JOIN FETCH ir.requester " +
            "WHERE ir.requester.id = :requesterId " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findAllByRequesterIdWithItems(@Param("requesterId") Long requesterId);

    @Query("SELECT DISTINCT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.items i " +
            "LEFT JOIN FETCH i.owner " +
            "WHERE ir.id = :requestId")
    Optional<ItemRequest> findByIdWithItems(@Param("requestId") Long requestId);

    @Query("SELECT DISTINCT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.items i " +
            "LEFT JOIN FETCH i.owner " +
            "LEFT JOIN FETCH ir.requester " +
            "WHERE ir.requester.id != :userId " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findAllByRequesterIdNotWithItems(@Param("userId") Long userId,
                                                       Pageable pageable);
}
