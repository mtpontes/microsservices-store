package br.com.ecommerce.orders.api.amqp;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
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
public class OrderAMQPConfig {

	@Bean 
	RabbitAdmin createRabbitAdmin(ConnectionFactory conn){
		return new RabbitAdmin(conn);
	}

	@Bean
	ApplicationListener<ApplicationReadyEvent> inicializaRabbitadmin(RabbitAdmin admin) {
		return event -> admin.initialize();
	}

	@Bean
	Jackson2JsonMessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	RabbitTemplate criaTemplate(ConnectionFactory conn, Jackson2JsonMessageConverter messageConverter){
		RabbitTemplate template = new RabbitTemplate(conn);
		template.setMessageConverter(messageConverter);
		
		return template;
	}


	// Sender configs
	@Configuration
	static class Sender {
		@Bean
		DirectExchange directExchangeCreateOrder() {
			return ExchangeBuilder.directExchange("orders.create.ex").build();
		}
		
		@Bean
		Queue queueProductsStock() {
			return QueueBuilder.durable("products.stock-orders").build();
		}
		
		@Bean
		Binding bindOrdersWithStock() {
			return BindingBuilder
				.bind(this.queueProductsStock())
				.to(this.directExchangeCreateOrder())
				.with("stock");
		}
		
		@Bean
		Queue queuePayments() {
			return QueueBuilder.durable("payments.details-order").build();
		}

		@Bean
		Binding bindOrdersWithPayments() {
			return BindingBuilder
				.bind(this.queuePayments())
				.to(this.directExchangeCreateOrder())
				.with("payment");
		}
		
		
		@Bean
		DirectExchange directExchangeCancelOrder() {
			return ExchangeBuilder.directExchange("orders.cancel.ex").build();
		}

		@Bean
		Queue queueCancelOrder() {
			return QueueBuilder.durable("payments.cancel-order").build();
		}

		@Bean
		Binding bindingCancelOrder() {
			return BindingBuilder
				.bind(this.queueCancelOrder())
				.to(this.directExchangeCancelOrder())
				.with("cancellation");
		}
	}

	// Receiver configs
	@Configuration
	static class Receiver {
		@Bean
		FanoutExchange discoverExchangePayments() {
			return ExchangeBuilder.fanoutExchange("payments.ex").build();
		}

		@Bean
		Queue queueStatusPayment() {
			return QueueBuilder.durable("orders.status-payment").build();
		}

		@Bean
		Binding bindPayments() {
			return BindingBuilder.bind(this.queueStatusPayment()).to(this.discoverExchangePayments());
		}
	}
}