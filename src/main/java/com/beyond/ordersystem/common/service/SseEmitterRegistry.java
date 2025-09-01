package com.beyond.ordersystem.common.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {

    // SseEmitter 연결된 사용자 정보(ip, mac address 정보 등)를 의미
    // ConcurrerntHashMap은  thread safe한 map (동시성 이슈 발생 X)
    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public void addSseEmitter(String email, SseEmitter sseEmitter) {
        emitterMap.put(email, sseEmitter);
    }

    public void removeSseEmitter(String email) {
        emitterMap.remove(email);
    }
    public SseEmitter getEmitter(String email) {
        return this.emitterMap.get(email);
    }
}
