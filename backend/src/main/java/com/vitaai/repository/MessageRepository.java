package com.vitaai.repository;

import com.vitaai.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Message> findByStatusOrderByCreatedAtDesc(Message.Status status, Pageable pageable);
    long countByStatus(Message.Status status);
}
