package de.uniba.rz.app;

import de.uniba.rz.ui.swing.MainFrame;
import de.uniba.rz.ui.swing.SwingMainController;
import de.uniba.rz.ui.swing.SwingMainModel;

/**
 * Main class to start the TicketManagement5000 client application Currently
 * only a local backend implementation is registered.<br>
 * <p>
 * To add additional implementations modify the method
 * <code>evaluateArgs(String[] args)</code>
 *
 * @see #evaluateArgs(String[])
 */
public class Main {

    public static void main(String[] args) {
        TicketManagementBackend backendToUse = evaluateArgs(args);

        SwingMainController control = new SwingMainController(backendToUse);
        SwingMainModel model = new SwingMainModel(backendToUse);
        MainFrame mf = new MainFrame(control, model);

        control.setMainFrame(mf);
        control.setSwingMainModel(model);

        control.start();
    }

    
    private static TicketManagementBackend evaluateArgs(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("No arguments passed. Using local backend implementation.");
            return new LocalTicketManagementBackend();
        }

        switch (args[0]) {
            case "local":
                return new LocalTicketManagementBackend();
            case "udp":
                if (args.length < 3) {
                    throw new IllegalArgumentException("UDP requires host and port. Usage: udp <host> <port>");
                }
                String udpHost = args[1];
                int udpPort = Integer.parseInt(args[2]);
                return new UdpTicketManagementBackend(udpHost, udpPort);
            case "amqp":
                if (args.length < 3) {
                    throw new IllegalArgumentException("AMQP requires host and routing key. Usage: amqp <host> <routingKey>");
                }
                String amqpHost = args[1];
                String routingKey = args[2];
                return new AmqpTicketManagementBackend(amqpHost, routingKey);
            case "grpc":
                if (args.length < 3) {
                    throw new IllegalArgumentException("gRPC requires host and port. Usage: grpc <host> <port>");
                }
                String grpcHost = args[1];
                int grpcPort = Integer.parseInt(args[2]);
                return new GrpcTicketManagementBackend(grpcHost, grpcPort);
            case "rest":
                return new RestTicketManagementBackend();
            default:
                System.out.println("Unknown backend type. Using local backend implementation.");
                return new LocalTicketManagementBackend();
        }
    }
}
