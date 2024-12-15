package de.uniba.rz.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniba.rz.entities.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class UdpTicketManagementBackend implements TicketManagementBackend {

    private String hostname;
    private int port;
    private final ObjectMapper objectMapper;
    private HashMap<Integer, Ticket> localTicketStore = new HashMap<>();


    public UdpTicketManagementBackend(String hostname, int port) {
        this.port = port;
        this.hostname = hostname;
        objectMapper = new ObjectMapper();
    }

    @Override
    public void triggerShutdown() {
        //recourses are already closed as used with try-with-resources statement
    }

    @Override
    public Ticket createNewTicket(String reporter, String topic, String description, Type type, Priority priority) throws TicketException {
        Ticket newTicket = new Ticket(reporter, topic, description, type, priority, Status.NEW);
        RequestResponseDto requestResponseDto = new RequestResponseDto();
        requestResponseDto.setReqResponseType(ReqResponseType.CREATE);
        requestResponseDto.setTicket(newTicket);
        requestResponseDto = udpRequest(requestResponseDto);

        localTicketStore.put(requestResponseDto.getTicket().getId(), newTicket);
        return requestResponseDto.getTicket();
    }


    @Override
    public List<Ticket> getAllTickets() throws TicketException {
        RequestResponseDto requestResponseDto = new RequestResponseDto();
        requestResponseDto.setReqResponseType(ReqResponseType.GET);
        requestResponseDto = udpRequest(requestResponseDto);

        localTicketStore = new HashMap<>();
        for (Ticket ticket : requestResponseDto.getTickets()) {
            localTicketStore.put(ticket.getId(), ticket);
        }
        return localTicketStore.values().stream().map(ticket -> (Ticket) ticket.clone()).collect(Collectors.toList());
    }

    @Override
    public Ticket getTicketById(int id) throws TicketException {
        RequestResponseDto requestResponseDto = new RequestResponseDto();
        requestResponseDto.setReqResponseType(ReqResponseType.GET);

        Ticket ticket = new Ticket();
        ticket.setId(id);

        requestResponseDto.setTicket(ticket);
        requestResponseDto = udpRequest(requestResponseDto);
        return requestResponseDto.getTicket();
    }

    private Ticket getTicketByIdInteral(int id) throws TicketException {
        if (!localTicketStore.containsKey(id)) {
            throw new TicketException("Ticket ID is unknown");
        }

        return localTicketStore.get(id);
    }

    @Override
    public Ticket acceptTicket(int id) throws TicketException {
        Ticket ticketToModify = getTicketByIdInteral(id);
        if (ticketToModify.getStatus() != Status.NEW) {
            throw new TicketException(
                    "Can not accept Ticket as it is currently in status " + ticketToModify.getStatus());
        }
        ticketToModify.setStatus(Status.ACCEPTED);
        return updateTicket(ticketToModify);
    }

    @Override
    public Ticket rejectTicket(int id) throws TicketException {
        Ticket ticketToModify = getTicketByIdInteral(id);
        if (ticketToModify.getStatus() != Status.NEW) {
            throw new TicketException(
                    "Can not reject Ticket as it is currently in status " + ticketToModify.getStatus());
        }
        ticketToModify.setStatus(Status.REJECTED);
        return updateTicket(ticketToModify);
    }

    @Override
    public Ticket closeTicket(int id) throws TicketException {
        Ticket ticketToModify = getTicketByIdInteral(id);
        if (ticketToModify.getStatus() != Status.ACCEPTED) {
            throw new TicketException(
                    "Can not close Ticket as it is currently in status " + ticketToModify.getStatus());
        }
        ticketToModify.setStatus(Status.CLOSED);
        return updateTicket(ticketToModify);
    }

    private Ticket updateTicket(Ticket ticket) throws TicketException {
        RequestResponseDto requestResponseDto = new RequestResponseDto();
        requestResponseDto.setReqResponseType(ReqResponseType.PUT);
        requestResponseDto.setTicket(ticket);

        requestResponseDto = udpRequest(requestResponseDto);
        return requestResponseDto.getTicket();
    }

    @Override
    public List<Ticket> getTicketsByName(String name) throws TicketException {
        SearchDto searchDto = new SearchDto(name);
        RequestResponseDto requestResponseDto = new RequestResponseDto();
        requestResponseDto.setReqResponseType(ReqResponseType.SEARCH);
        requestResponseDto.setSearchDto(searchDto);

        requestResponseDto = udpRequest(requestResponseDto);

        localTicketStore = new HashMap<>();
        for (Ticket ticket : requestResponseDto.getTickets()) {
            localTicketStore.put(ticket.getId(), ticket);
        }
        return localTicketStore.values().stream().map(ticket -> (Ticket) ticket.clone()).collect(Collectors.toList());
    }

    @Override
    public List<Ticket> getTicketsByNameAndType(String name, Type type) throws TicketException {
        SearchDto searchDto = new SearchDto(name, type);
        RequestResponseDto requestResponseDto = new RequestResponseDto();
        requestResponseDto.setReqResponseType(ReqResponseType.SEARCH);
        requestResponseDto.setSearchDto(searchDto);

        requestResponseDto = udpRequest(requestResponseDto);

        localTicketStore = new HashMap<>();
        for (Ticket ticket : requestResponseDto.getTickets()) {
            localTicketStore.put(ticket.getId(), ticket);
        }
        return localTicketStore.values().stream().map(ticket -> (Ticket) ticket.clone()).collect(Collectors.toList());
    }

    private RequestResponseDto udpRequest(RequestResponseDto requestResponseDto) throws TicketException {
        System.out.println("\t [Sender]: Trying to send message '" + requestResponseDto.toString() + "'");
        // try-with-resources - automatically closes the socket sock
        try (DatagramSocket sock = new DatagramSocket(null)) {

            // create address of recipient
            SocketAddress serverAddress = new InetSocketAddress(hostname, port);

            // set timeout time to 5000ms
            sock.setSoTimeout(5000);

            //object to byte array for sending over the udp
            byte[] messageData  = objectMapper.writeValueAsBytes(requestResponseDto);

            //datagram packet to send
            DatagramPacket packet = new DatagramPacket(messageData, messageData.length, serverAddress);

            // send packet
            sock.send(packet);

            //preparing data holder for received packet
            byte[] receivedData = new byte[51002];

            //received datagram packet
            DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);

            //receive data
            sock.receive(receivePacket);
            System.out.println("The server replied with data "
                    + new String(receivePacket.getData()) + " on port "+ receivePacket.getPort());

            return objectMapper.readValue(receivedData, RequestResponseDto.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TicketException("Could not create ticket " + e.getMessage());
        }
    }
}
