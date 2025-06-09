package io.satori.config;
import javax.jms.ConnectionFactory; // or javax.jms.ConnectionFactory for older Spring Boot
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class JmsConfig {

    // This bean customizes the default factory used by @JmsListener
    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        // Configure retries with backoff
        // Retry 3 times with a 5-second interval between retries
        factory.setBackOff(new FixedBackOff(5000L, 2)); // interval, maxAttempts (total 3 attempts: 1 initial + 2 retries)
        factory.setErrorHandler(t -> {
            // Log the error after retries are exhausted
            System.err.println("Error in listener, max retries reached for message: " + t.getMessage());
            // Further DLQ logic could be implemented here if not handled by broker
        });
        // If you want to ensure transactions for message processing:
        // factory.setSessionTransacted(true);
        return factory;
    }

    // If you need to customize the JmsTemplate (e.g., for message converters)
    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory){
        CachingConnectionFactory ccf = new CachingConnectionFactory(connectionFactory);
        JmsTemplate jmsTemplate = new JmsTemplate(ccf);
        // jmsTemplate.setMessageConverter(yourMessageConverter()); // If using custom converter
        jmsTemplate.setDestinationResolver(new DynamicDestinationResolver());
        return jmsTemplate;
    }
}