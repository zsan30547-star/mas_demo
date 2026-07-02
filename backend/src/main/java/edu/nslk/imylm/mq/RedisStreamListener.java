package edu.nslk.imylm.mq;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import edu.nslk.imylm.service.SseService;
import java.nio.charset.StandardCharsets;

@Component
public class RedisStreamListener implements MessageListener {

    private final SseService sseService;

    public RedisStreamListener(SseService sseService) {
        this.sseService = sseService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 频道名称，例如: task_stream:123
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        
        if (channel.startsWith("task_stream:")) {
            try {
                Long taskId = Long.parseLong(channel.split(":")[1]);
                // 把从 Python 拿到的 chunk 立即通过 SSE 推送给前端
                sseService.sendEventToTask(taskId, "stream_chunk", body);
            } catch (Exception e) {
                // 解析错误忽略
            }
        }
    }
}