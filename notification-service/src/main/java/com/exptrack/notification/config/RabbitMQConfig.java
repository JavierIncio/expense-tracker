package com.exptrack.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "expense.events";

    public static final String BUDGET_EXCEEDED_RK = "budget.exceeded";
    public static final String TRANSACTION_CREATED_RK = "transaction.created";
    public static final String TRANSACTION_DELETED_RK = "transaction.deleted";

    public static final String TRANSACTION_CREATED_QUEUE = "notification.transaction-created";
    public static final String TRANSACTION_DELETED_QUEUE = "notification.transaction-deleted";
    public static final String BUDGET_EXCEEDED_QUEUE = "notification.budget-exceeded";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue budgetExceededQueue() {
        return new Queue(BUDGET_EXCEEDED_QUEUE, true);
    }

    @Bean
    public Binding budgetExceededBinding() {
        return BindingBuilder.bind(budgetExceededQueue()).to(topicExchange()).with(BUDGET_EXCEEDED_RK);
    }

    @Bean
    public Queue transactionCreatedQueue() {
        return new Queue(TRANSACTION_CREATED_QUEUE, true);
    }

    @Bean
    public Binding transactionCreatedBinding() {
        return BindingBuilder.bind(transactionCreatedQueue()).to(topicExchange()).with(TRANSACTION_CREATED_RK);
    }

    @Bean
    public Queue transactionDeletedQueue() {
        return new Queue(TRANSACTION_DELETED_QUEUE, true);
    }

    @Bean
    public Binding transactionDeletedBinding() {
        return BindingBuilder.bind(transactionDeletedQueue()).to(topicExchange()).with(TRANSACTION_DELETED_RK);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter("com.exptrack.notification.events");
    }
}
