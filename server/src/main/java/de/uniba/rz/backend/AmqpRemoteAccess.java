package de.uniba.rz.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import de.uniba.rz.entities.RequestResponseDto;
import de.uniba.rz.entities.TicketException;

import java.nio.charset.StandardCharsets;

public class AmqpRemoteAccess extends RemoteAccess{

    private Connection connection;
    private Channel channel;
    final Object monitor;
    private final String hostname;
    private final String queueName;
    private final ConnectionFactory connFactory = new ConnectionFactory();

    public AmqpRemoteAccess(String hostname, String queueName) {
        this.hostname = hostname;
        this.queueName = queueName;
        monitor = new Object();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void prepareStartup(TicketStore ticketStore) {
        this.ticketStore = ticketStore;
        connFactory.setHost(hostname);
        try {
            connection = connFactory.newConnection();
            channel = connection.createChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        if(connection.isOpen()) {
            try{
                connection.close();
                if(channel.isOpen()) {
                    channel.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            channel.queueDeclare(queueName, false, false, false, null);
            channel.queuePurge(this.queueName);
            channel.basicQos(1);
            System.out.println("Waiting for messages...");

            DeliverCallback deliverCallback = ((consumerTag, delivery) -> {

                // Read replyTo-queue and the correlation id
                String replyTo = delivery.getProperties().getReplyTo();
                String correlationId = delivery.getProperties().getCorrelationId();

                // Append the correlation id to support to match request and response
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(correlationId)
                        .build();

                RequestResponseDto requestResponseDto = null;
                try{
                    byte[] receivedData = delivery.getBody();
                    String content = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    System.out.println("\t [RECEIVER]: >Received: " + content);

                    requestResponseDto = processReceivedData(receivedData);

                }catch (RuntimeException e){
                    e.printStackTrace();
                } catch (UnknownTicketException e) {
                    e.printStackTrace();
                } catch (TicketException e) {
                    e.printStackTrace();
                } finally {
                    channel.basicPublish("", replyTo, replyProps,
                            objectMapper.writeValueAsBytes(requestResponseDto));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    // RabbitMq consumer worker thread notifies the RPC server owner thread
                    synchronized (monitor) {
                        monitor.notify();
                    }
                }

            });

            channel.basicConsume(queueName, false, deliverCallback,consumerTag -> {});

            // Wait and be prepared to consume the message from client.
            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
