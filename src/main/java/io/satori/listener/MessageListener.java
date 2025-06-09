package io.satori.listener;

import io.satori.SpringBootActiveMQApplication;
import io.satori.domain.Product;
import io.satori.repositories.ProductRepository;
import io.satori.services.ProductService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the queue listener class, its receiveMessage() method ios invoked with the
 * message as the parameter.
 */
@Component
public class MessageListener {

    private ProductRepository productRepository;
    private AtomicInteger errorCount = new AtomicInteger(0); // For simulating errors
    private ProductService productService;
    private static final Logger log = LogManager.getLogger(MessageListener.class);

    public MessageListener(ProductRepository productRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }
    public static final String PRODUCT_MESSAGE_QUEUE = "product-message-queue";
    /**
     * This method is invoked whenever any new message is put in the queue.
     * See {@link SpringBootActiveMQApplication} for more details
     * @param message
     */
    @JmsListener(destination = PRODUCT_MESSAGE_QUEUE, containerFactory = "jmsFactory")
    public void receiveMessage(Map<String, String> message) {
        log.info("Received <" + message + ">");
        Long id = Long.valueOf(message.get("id"));
        Product product = productRepository.findById(id).orElse(null);
        product.setMessageReceived(true);
        product.setMessageCount(product.getMessageCount() + 1);
        productRepository.save(product);
        log.info("Message processed...");

    }
    @JmsListener(destination = PRODUCT_MESSAGE_QUEUE)
    public void anotherreceiveMessage(@Payload Map<String, String> message) throws InterruptedException {
        log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++");
        log.info("anotherreceiveMessage - Received: {}", message);
        Long id = Long.valueOf(message.get("id"));

        // Simulate an error every 3rd message for a specific product ID to test retries/DLQ
        if (id == 1 && errorCount.incrementAndGet() % 3 != 0) {
            log.warn("Simulating processing error for message with id: {}. Error count: {}", id, errorCount.get());
            throw new RuntimeException("Simulated processing error for product ID: " + id);
        }
        // Reset error count after successful processing or if not the target ID for error simulation
        if (id == 1) {
            errorCount.set(0);
            log.info("Error count for ID 1 reset.");
        }


        Product product = productService.getById(id);
        if (product == null) {
            log.warn("Product not found for id: {} in anotherreceiveMessage. Skipping further processing.", id);
            log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++");
            return;
        }

        log.info("Starting complex processing for product ID: {}", id);

        // 1. Simulate generating a detailed report
        log.info("Simulating: Generating detailed report for product ID: {}...", id);
        Thread.sleep(1500L); // Simulate 1.5 seconds
        log.info("Simulating: Detailed report generated for product ID: {}.", id);

        // 2. Simulate sending a series of notifications
        log.info("Simulating: Sending notifications for product ID: {}...", id);
        Thread.sleep(1000L); // Simulate 1 second for first notification
        log.info("Simulating: Email notification sent for product ID: {}.", id);
        Thread.sleep(500L);  // Simulate 0.5 seconds for second notification
        log.info("Simulating: SMS notification sent for product ID: {}.", id);

        // 3. Simulate integrating with a slow third-party system
        log.info("Simulating: Integrating with slow third-party system for product ID: {}...", id);
        Thread.sleep(2000L); // Simulate 2 seconds
        log.info("Simulating: Integration with third-party system complete for product ID: {}.", id);

        // Update product status after all simulated tasks
        product.setMessageReceived(true);
        product.setMessageCount(product.getMessageCount() + 1);
        productService.saveOrUpdate(product);

        log.info("Message Processed by anotherreceiveMessage: {}", product);
        log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }
}
