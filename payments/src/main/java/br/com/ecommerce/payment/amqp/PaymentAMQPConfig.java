package br.com.ecommerce.payment.amqp;

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

@Configuration
public class PaymentAMQPConfig {
	
	@Bean 
	RabbitAdmin createRabbitAdmin(ConnectionFactory conn){
		return new RabbitAdmin(conn);
	}
	
	@Bean
	ApplicationListener<ApplicationReadyEvent> initialyzeRabbitAdmin(RabbitAdmin admin) {
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

	// Classe interna estática para configuração do receiver
	@Configuration
	static class Receiver {
		@Bean
		DirectExchange directExchangeCreateOrder() {
			return ExchangeBuilder.directExchange("orders.create.ex").build();
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
		Binding bindingCancelWithOrder() {
			return BindingBuilder
				.bind(this.queueCancelOrder())
				.to(this.directExchangeCancelOrder())
				.with("cancellation");
		}
	}

    // static internal class for sender configuration
    @Configuration
    static class Sender {
		@Bean
		FanoutExchange fanoutExchangePayments() {
			return ExchangeBuilder.fanoutExchange("payments.ex").build();
		}
	}
}