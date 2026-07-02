// /backend/src/main/java/edu/nslk/imylm/mq/TaskProducer.java
// 职责描述：发送任务到 RabbitMQ 执行队列

package edu.nslk.imylm.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TaskProducer {

    private final RabbitTemplate rabbitTemplate;

    public TaskProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // 发送任务到执行队列
    // @param message 任务消息
    public void sendTask(TaskMessage message) {
        rabbitTemplate.convertAndSend("task.exchange", "task.execute", message);
    }
}
