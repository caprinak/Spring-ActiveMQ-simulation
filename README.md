

# Spring Boot ActiveMQ Example

To run this example, you will need to do the following:

*   Update the application.properties file with your MySQL username and password
*   Create a new database schema with name springboot_activemq_example
*   If you wish to used a different database / schema, you will need to override values in application.properties
*   Example uses an embedded ActiveMQ Broker. If you wish to use an external broker, you will need to override the appropriate Spring Boot Properties.

## Application flow:

*   Once the application is started, open the same in a browser http://localhost:8080
*   Create a new Product record
*   Notice that the value for "Are Messages Sent" in the http://localhost:8080/product/show/1 is set to false.
*   There is a link at the bottom of this record "Send message for product through queue listener"
*   When this link is clicked, a message is put in an activemq queue, the browser is redirected back to the product show page without getting blocked.
*   This message is read and processed by MessageListener class.
*   On the browser, refreshing the page: http://localhost:8080/product/show/1
*   You will see the message sent and message count increase (if you click the send link more than once)
*   Expect a short delay in processing the messages.

## Why Use Asynchronous Messaging (JMS)?

This project demonstrates asynchronous processing using a message queue (ActiveMQ with JMS). In real life, this pattern solves several problems:

1.  **Offloading Long-Running Tasks:** Imagine the "Send message for product" action triggers a complex, time-consuming process like generating a detailed report, sending a series of notifications, or integrating with a slow third-party system. You wouldn't want the user to wait for this to complete. By sending a message, the user gets an immediate response, and the work happens in the background.
2.  **Decoupling Systems:** The part of the application that initiates the action (the web controller) is decoupled from the part that processes it (the message listener). They only need to agree on the message format. This allows them to be developed, deployed, and scaled independently.
3.  **Improving Responsiveness and User Experience:** The browser redirects immediately after the message is sent to the queue, not after the message is fully processed. This makes the application feel faster.
4.  **Ensuring Reliability for Certain Operations:** If the message processing fails, the message can remain in the queue and be retried, or moved to a dead-letter queue for investigation. This is more robust than a direct synchronous call that might fail and lose the intended operation.

### JMS got involved to update the product after it's stored in the DB

In this specific example, JMS isn't strictly *updating* the core product data that was initially saved. Instead, it's used to trigger a *secondary, asynchronous action* related to that product:

*   The initial save to the database is a quick, synchronous operation. The product exists with "Are Messages Sent" as `false`.
*   Clicking "Send message for product through queue listener" initiates an asynchronous task. The message sent via JMS tells a separate part of the system (the `MessageListener`) to perform an additional action related to that product.
*   This action, in this case, is to simulate processing and then update the product's "message sent" status and count in the database.

This pattern is common for tasks that can happen "eventually" without blocking the primary user flow.
