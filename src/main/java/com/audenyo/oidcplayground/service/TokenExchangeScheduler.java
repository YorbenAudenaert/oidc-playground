package com.audenyo.oidcplayground.service;

import com.audenyo.oidcplayground.model.dto.TokenExchangeResultDto;
import com.audenyo.oidcplayground.repository.KnownUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenExchangeScheduler {

    private static final Logger log = LoggerFactory.getLogger(TokenExchangeScheduler.class);

    private final KnownUserRepository knownUserRepository;
    private final TokenExchangeService tokenExchangeService;
    private final Map<String, TokenExchangeResultDto> resultStore = new ConcurrentHashMap<>();

    public TokenExchangeScheduler(KnownUserRepository knownUserRepository,
                                  TokenExchangeService tokenExchangeService) {
        this.knownUserRepository = knownUserRepository;
        this.tokenExchangeService = tokenExchangeService;
    }

    @Scheduled(cron = "${token-exchange.cron:0 * * * * *}")
    public void run() {
       knownUserRepository.findAll().forEach(user -> {
            try {
                TokenExchangeResultDto result = tokenExchangeService.exchangeForUser(user);
                resultStore.put(user.getSub(), result);
                log.info("Token exchange OK for sub={}, act={}", user.getSub(),
                        result.decodedClaims() != null ? result.decodedClaims().get("act") : "n/a");
            } catch (Exception e) {
                log.error("Token exchange failed for sub={}: {}", user.getSub(), e.getMessage());
            }
        });
    }

    public Collection<TokenExchangeResultDto> getResults() {
        return resultStore.values();
    }
}
