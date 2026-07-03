package com.ballsaas.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendSubscribeMessage(Long userId, String scene, String content) {
        log.info("mock subscribe message userId={}, scene={}, content={}", userId, scene, content);
    }
}

