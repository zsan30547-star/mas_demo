package edu.nslk.imylm.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {

    // 存储 Task ID 和对应的 SseEmitter 列表
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 注册一个新的 SSE 连接
    public SseEmitter subscribe(Long taskId) {
        // 设置超时时间为 30 分钟 (1800000ms)
        SseEmitter emitter = new SseEmitter(1800000L);
        emitters.put(taskId, emitter);

        emitter.onCompletion(() -> emitters.remove(taskId));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(taskId);
        });
        emitter.onError(e -> {
            emitter.completeWithError(e);
            emitters.remove(taskId);
        });
        
        try {
            // 发送连接成功事件
            emitter.send(SseEmitter.event().name("connected").data("SSE Connection established for task " + taskId));
        } catch (IOException e) {
            emitter.completeWithError(e);
            emitters.remove(taskId);
        }

        return emitter;
    }

    // 向指定的 Task 发送事件
    public void sendEventToTask(Long taskId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(taskId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(taskId);
            }
        }
    }
}
