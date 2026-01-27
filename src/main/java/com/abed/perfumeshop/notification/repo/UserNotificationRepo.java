package com.abed.perfumeshop.notification.repo;

import com.abed.perfumeshop.common.enums.UserType;
import com.abed.perfumeshop.notification.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepo extends JpaRepository<UserNotification, Long> {

    @Query("SELECT un FROM UserNotification un " +
            "JOIN FETCH un.notification n " +
            "JOIN FETCH n.order o " +
            "WHERE un.userId = :userId " +
            "AND un.userType = :userType " +
            "ORDER BY un.receivedAt DESC")
    List<UserNotification> findByUserIdAndUserTypeOrderByReceivedAtDesc(
            @Param("userId") Long userId,
            @Param("userType") UserType userType
    );

    boolean existsByUserIdAndUserTypeAndSeenFalse(Long userId, UserType userType);

    @Modifying
    @Query("UPDATE UserNotification un SET un.seen = true, un.seenAt = :seenAt " +
            "WHERE un.userId = :userId AND un.userType = :userType AND un.seen = false")
    void markAllAsSeen(
            @Param("userId") Long userId,
            @Param("userType") UserType userType,
            @Param("seenAt") LocalDateTime seenAt
    );

    Optional<UserNotification> findByIdAndUserIdAndUserType(Long id, Long userId, UserType userType);

    void deleteByUserIdAndUserType(Long userId, UserType userType);
}
