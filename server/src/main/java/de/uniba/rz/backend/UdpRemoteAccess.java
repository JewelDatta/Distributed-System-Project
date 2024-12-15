package de.uniba.rz.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniba.rz.entities.*;

import java.io.IOException;
import java.net.*;

public class UdpRemoteAccess extends RemoteAccess {

    private final int port;
    private boolean active = true;

    public UdpRemoteAccess(int port) {
        this.port = port;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void prepareStartup(TicketStore ticketStore) {
        this.ticketStore = ticketStore;
    }

    private void startServer() {
        System.out.println("\t [SERVER]: Start listening on port " + port + " for messages.");
        active = true;
        // try-with-resources, create unbound serverSocket
        try (DatagramSocket serverSocket = new DatagramSocket(null)) {
            // create socket address
            SocketAddress address = new InetSocketAddress(port);

            // and bind the socket to this address
            serverSocket.bind(address);

            // set timeout time to 5000ms
            serverSocket.setSoTimeout(5000);

            while (active) {
                // prepare packet to receive data
                byte[] data = new byte[51002];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                try {
                    // wait for packet
                    serverSocket.receive(packet);
                    // process received packet
                    byte[] receivedData = packet.getData();

                    String content = new String(receivedData, 0, packet.getLength());
                    System.out.println("\t [RECEIVER]: >Received: " + content);

                    RequestResponseDto requestResponseDto = processReceivedData(receivedData);

                    // create packet
                    byte[] messageData = objectMapper.writeValueAsBytes(requestResponseDto);
                    DatagramPacket responsePacket = new DatagramPacket(messageData, messageData.length,
                            packet.getSocketAddress());

                    // send packet
                    serverSocket.send(responsePacket);

                } catch (SocketTimeoutException e) {
                    // swallow timeout
                } catch (TicketException | UnknownTicketException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // dummy exception handling - do NOT do this in your Assignment!
            e.printStackTrace();
        }
        System.out.println("\t [SERVER]: Stopped.");
    }



    @Override
    public void shutdown() {
        active = false;
        System.out.println("\t [SERVER]: Stopping to listen for messages.");
    }

    @Override
    public void run() {
        startServer();
    }
}
