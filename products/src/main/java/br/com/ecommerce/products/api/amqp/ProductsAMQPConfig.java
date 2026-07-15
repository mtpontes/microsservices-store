package br.com.ecommerce.products.api.amqp;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class ProductsAMQPConfig {

	@Bean
	RabbitAdmin createRabbitAdmin(ConnectionFactory conn) {
		return new RabbitAdmin(conn);
	}

	@Bean
	ApplicationListener<ApplicationReadyEvent> initializeRabbitadmin(RabbitAdmin admin) {
		return event -> admin.initialize();
	}

	@Bean
	Jackson2JsonMessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	RabbitTemplate createTemplate(ConnectionFactory conn, Jackson2JsonMessageConverter messageConverter){
		RabbitTemplate template = new RabbitTemplate(conn);
		template.setMessageConverter(messageConverter);
		return template;
	}

	// Receiver configs
	@Configuration
	static class Receiver {
		
		@Bean
		DirectExchange discoverExchangeOrders() {
			return ExchangeBuilder.directExchange("orders.create.ex").build();
		}

		@Bean
		Queue queueStockProducts() {
			return QueueBuilder.durable("products.stock-orders").build();
		}
		
		@Bean
		Binding bindOrders() {
			return BindingBuilder
				.bind(this.queueStockProducts())
				.to(this.discoverExchangeOrders())
				.with("stock");
		}
	}
}