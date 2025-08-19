package com.kt.api_messaging_svc.repository;

import com.kt.api_messaging_svc.entity.MessageRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRecipientRepository extends JpaRepository<MessageRecipient, Long> {}
