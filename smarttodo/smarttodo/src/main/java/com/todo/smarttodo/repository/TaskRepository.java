package com.todo.smarttodo.repository;

import com.todo.smarttodo.entity.RecurrenceMode;
import com.todo.smarttodo.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.todo.smarttodo.entity.TaskStatus;
import java.util.List;
import java.time.LocalDateTime;
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByUserId(Long userId, Pageable pageable);

    @Query("""
    SELECT t FROM Task t
    WHERE t.deadline < :now
    AND t.status <> 'COMPLETED'
    AND t.overdueAlertSent = false
    """)
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);

    @Query("""
    SELECT t FROM Task t
    WHERE t.deadline BETWEEN :from AND :to
    AND t.status = 'PENDING'
    """)
    List<Task> findTasksBetweenDeadlines(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    List<Task> findByStatus(TaskStatus status);
    // ✅ Find the latest recurring task for a user
    Task findTopByUserIdAndRecurringOrderByDeadlineDesc(Long userId, boolean recurring);
    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, TaskStatus status);
    @Query("""
    SELECT COUNT(t)
    FROM Task t
    WHERE t.user.id = :userId
    AND t.deadline < CURRENT_TIMESTAMP
    AND t.status <> 'COMPLETED'
    """)
    long countOverdue(Long userId);
    @Query("""
    SELECT t.priority, COUNT(t)
    FROM Task t
    WHERE t.user.id = :userId
    GROUP BY t.priority
    """)
    List<Object[]> countByPriority(Long userId);
    @Query("""
    SELECT DATE(t.completedAt), COUNT(t)
    FROM Task t
    WHERE t.user.id = :userId
    AND t.status = 'COMPLETED'
    AND t.completedAt >= :startDate
    GROUP BY DATE(t.completedAt)
    ORDER BY DATE(t.completedAt)
    """)
    List<Object[]> completedTrend(Long userId, LocalDateTime startDate);
    Page<Task> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);
    Page<Task> findByUserIdAndDeletedTrue(Long userId, Pageable pageable);
    void deleteByDeletedTrueAndDeletedAtBefore(LocalDateTime time);
    void deleteByUserIdAndDeletedTrue(Long userId);








}

//Interfaces, no implementation needed.
//
//Inherits methods like save(), findById(), findAll().
//
//You can add custom queries using method names (like findByUserId).
//taskRepository.save(task) → Hibernate generates SQL INSERT.
//
//taskRepository.findById(id) → Hibernate generates SQL SELECT.
//
//Foreign keys and relationships are automatically handled (like user_id in Task).  check this is correct or not