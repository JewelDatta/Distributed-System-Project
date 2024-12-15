package de.uniba.rz.backend.configs;

public class ServerConfig {
    public static final String HOST = "localhost";
    public static final  String USER = "guest";
    public static final String PASSWORD = "guest";
    public static final String QUEUE_NAME = "amqp_backend_queue";
    public static final String EXCHANGE_NAME = "amqp_exchange";
    public static final String AMQP_URL = "amqp://guest:guest@localhost:15672";
}
