package com.example.loans.repository;

import com.example.loans.model.ApprovalRequest;
import com.example.loans.model.Decision;
import com.example.loans.model.EventKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Configuration
public class RepositoryConfiguration {

    @Bean
    public ConcurrentSkipListMap<EventKey, UUID> events() {
        return new ConcurrentSkipListMap<>();
    }

    @Bean
    public ConcurrentHashMap<ApprovalRequest, ConcurrentHashMap<String, Decision>> storage() {
         return new ConcurrentHashMap<>();
    }
}
