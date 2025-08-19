package com.kt.api_messaging_svc.repository;

import com.kt.api_messaging_svc.entity.MessageRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRecipientRepository extends JpaRepository<MessageRecipient, Long> {
    Optional<MessageRecipient> findByProviderSid(String sid);
}
