// /backend/src/main/java/edu/nslk/imylm/config/RabbitMQConfig.java
// 职责描述：RabbitMQ 队列/交换机/绑定关系配置

package edu.nslk.imylm.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ==================== 交换机 ====================
    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange("task.exchange");
    }

    // ==================== 队列 ====================
    // 任务执行队列：Spring Boot 发 -> FastAPI 消费
    @Bean
    public Queue taskExecuteQueue() {
        return QueueBuilder.durable("task.execute.queue").build();
    }

    // 任务结果队列：FastAPI 发 -> Spring Boot 消费
    @Bean
    public Queue taskResultQueue() {
        return QueueBuilder.durable("task.result.queue").build();
    }

    // ==================== 绑定 ====================
    @Bean
    public Binding taskExecuteBinding() {
        return BindingBuilder.bind(taskExecuteQueue())
                .to(taskExchange())
                .with("task.execute");
    }

    @Bean
    public Binding taskResultBinding() {
        return BindingBuilder.bind(taskResultQueue())
                .to(taskExchange())
                .with("task.result");
    }

    // ==================== 消息转换器 ====================
    // 使用 JSON 序列化，让 Python FastAPI 能读取消息
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
