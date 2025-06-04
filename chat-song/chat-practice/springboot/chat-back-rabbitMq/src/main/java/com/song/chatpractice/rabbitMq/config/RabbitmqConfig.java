package com.song.chatpractice.rabbitMq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.port}")
    private int port;

    // 1. Exchange 구성
    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange("chat.exchange");
    }

    // 2. Queue 구성
    @Bean
    public Queue chatQueue() {
        return new Queue("chat.queue", true);
    }

    // 3. Binding ( Queue와 Exchange 바인딩) 구성
    @Bean
    public Binding chatBinding(DirectExchange chatExchange, Queue chatQueue){
        return BindingBuilder
                .bind(chatQueue)
                .to(chatExchange)
                .with("chat.routing.key");
    }

    // 4. ConnectionFactory 구성 - application.yml 파일과 연결
    @Bean
    public ConnectionFactory chatConnectionFactory(){

        CachingConnectionFactory chatConnectionFactory = new CachingConnectionFactory();
        chatConnectionFactory.setHost(host);
        chatConnectionFactory.setPort(port);
        chatConnectionFactory.setUsername(username);
        chatConnectionFactory.setPassword(password);

        return chatConnectionFactory;
    }

    // 5. Jackson2JsonMessageConverter 구성 - 데이터를 json 형태로 바꿔주는 메소드
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 6. RabbitMQ Template 구성 - chatConnectionFactory와 messageConveter 사용
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        // RabbitMQ 설정파일 연결
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory chatConnectionFactory){
        RabbitAdmin admin = new RabbitAdmin(chatConnectionFactory);
        admin.setAutoStartup(true);

        return admin;
    }

}
