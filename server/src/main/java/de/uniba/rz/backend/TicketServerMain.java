package de.uniba.rz.backend;

public class TicketServerMain {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("No protocol specified. Usage: <protocol> [args...]");
            System.exit(1);
        }

        TicketStore ticketStore = new SimpleTicketStore();
        RemoteAccess server = null;
        String hostname = "localhost"; // Default hostname

        try {
            switch (args[0]) {
                case "udp":
                    if (args.length < 2) {
                        System.out.println("UDP requires port. Usage: udp <port>");
                        System.exit(1);
                    }
                    int udpPort = Integer.parseInt(args[1]);
                    server = new UdpRemoteAccess(udpPort);
                    break;
                    
                case "amqp":
                    if (args.length < 2) {
                        System.out.println("AMQP requires routing key. Usage: amqp <routingKey>");
                        System.exit(1);
                    }
                    String routingKey = args[1];
                    server = new AmqpRemoteAccess(hostname, routingKey);
                    break;
                    
                case "grpc":
                    if (args.length < 2) {
                        System.out.println("gRPC requires port. Usage: grpc <port>");
                        System.exit(1);
                    }
                    int grpcPort = Integer.parseInt(args[1]);
                    server = new GrpcRemoteAccess(grpcPort);
                    break;
                    
                case "rest":
                    if (args.length < 2) {
                        System.out.println("REST requires port. Usage: rest <port>");
                        System.exit(1);
                    }
                    server = new RestRemoteAccess();
                    break;
                    
                default:
                    System.out.println("Unknown protocol: " + args[0]);
                    System.exit(1);
            }

            if (server == null) {
                System.err.println("Failed to initialize server");
                System.exit(1);
            }

            server.prepareStartup(ticketStore);
            Thread serverThread = new Thread(server);
            serverThread.start();

            System.out.println("Server started. Press Enter to stop.");
            System.in.read();

            server.shutdown();
            serverThread.join();
            
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
