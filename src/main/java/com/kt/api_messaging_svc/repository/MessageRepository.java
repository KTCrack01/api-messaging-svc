package com.kt.api_messaging_svc.repository;

import com.kt.api_messaging_svc.entity.Messages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Messages, Long> {
    List<Messages> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
