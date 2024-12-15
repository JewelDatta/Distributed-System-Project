package de.uniba.rz.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import de.uniba.rz.entities.RequestResponseDto;
import de.uniba.rz.entities.TicketException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class ComplexAmqpClient{

    private Connection connection;
    private Channel channel;
    private final String hostname;
    private final String routingKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ConnectionFactory connFactory = new ConnectionFactory();

    public ComplexAmqpClient(String hostname, String routingKey) {
        this.hostname = hostname;
        this.routingKey = routingKey;
        connFactory.setHost(this.hostname);
        try {
            connection = connFactory.newConnection();
            channel = connection.createChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RequestResponseDto sendMessage(RequestResponseDto requestResponseDto) throws TicketException {
        try {
            String correlationId = UUID.randomUUID().toString();
            String replyToQueue = channel.queueDeclare().getQueue();
            AMQP.BasicProperties basicProperties = new AMQP.BasicProperties().builder()
                    .correlationId(correlationId)
                    .replyTo(replyToQueue)
                    .deliveryMode(2)
                    .build();
            channel.basicPublish("", routingKey, basicProperties, serializeObject(requestResponseDto));

            return pushQueueReceiver(replyToQueue, channel, correlationId);

        } catch (IOException e) {
            throw new TicketException("Could not create ticket " + e.getMessage());
        }
    }

    private RequestResponseDto pushQueueReceiver(String replyToQueue, Channel channel, String correlationId) throws IOException {
        final BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(1, true);

        channel.basicQos(1);

        String ctag = channel.basicConsume(replyToQueue, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                try {
                    // After receiving, add the message to a thread-safe data structure, like a BlockingQueue
                    blockingQueue.put(new String(delivery.getBody(), StandardCharsets.UTF_8));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // Preserve the interrupt for the caller (channel's thread pool)
                    Thread.currentThread().interrupt();
                }
            }
        }, consumerTag -> {

        });

        // The program does never terminate if no message arrives
        while (true) {
            try {
                String content  = blockingQueue.take();
                System.out.println("\t [RECEIVER IN CLIENT]: >Received: "+ content);

                RequestResponseDto requestResponseDto = objectMapper.readValue(content, RequestResponseDto.class);
                System.out.println(requestResponseDto);

                return requestResponseDto;
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }finally {
                channel.basicCancel(ctag);
            }
        }
    }

    private byte[] serializeObject(Object o) throws IOException {
        return objectMapper.writeValueAsBytes(o);
    }

    public void close() {
        if(connection.isOpen()) {
            if(channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException e){
                    e.printStackTrace();
                } catch (TimeoutException e){
                    e.printStackTrace();
                }
            }
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
