package com.audenyo.oidcplayground.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenRefreshJob {

    private final SilentTokenRefreshService silentTokenRefreshService;

    public TokenRefreshJob(SilentTokenRefreshService silentTokenRefreshService) {
        this.silentTokenRefreshService = silentTokenRefreshService;
    }

    @Scheduled(cron = "${adfs.refresh-cron}")
    public void run() {
        silentTokenRefreshService.refreshAll();
    }
}
