package de.uniba.rz.app;

import de.uniba.rz.entities.*;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AmqpTicketManagementBackend implements TicketManagementBackend {

    private final ComplexAmqpClient client;
    private HashMap<Integer, Ticket> localTicketStore = new HashMap<>();

    public AmqpTicketManagementBackend(String hostname, String routingKey) {
        client = new ComplexAmqpClient(hostname, routingKey);
    }

    @Override
    public void triggerShutdown() {
        client.close();
    }

    @Override
    public Ticket createNewTicket(String reporter, String topic, String description, Type type, Priority priority) throws TicketException {
        Ticket newTicket = new Ticket(reporter, topic, description, type, priority, Status.NEW);
        RequestResponseDto requestResponseDto = new RequestResponseDto();
        requestResponseDto.setReqResponseType(ReqResponseType.CREATE);
        requestResponseDto.setTicket(newTicket);
        requestResponseDto = client.sendMessage(requestResponseDto);

        localTicketStore.put(requestResponseDto.getTicket().getId(), newTicket);
        return requestResponseDto.getTicket();
    }

    @Override
    public List<Ticket> getAllTickets() throws TicketException {
        RequestResponseDto requestResponseDto = new RequestResponseDto();
        requestResponseDto.setReqResponseType(ReqResponseType.GET);
        requestResponseDto = client.sendMessage(requestResponseDto);

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
        requestResponseDto = client.sendMessage(requestResponseDto);
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

    private Ticket updateTicket(Ticket ticket) throws TicketException {
        RequestResponseDto requestResponseDto = new RequestResponseDto();
        requestResponseDto.setReqResponseType(ReqResponseType.PUT);
        requestResponseDto.setTicket(ticket);

        requestResponseDto = client.sendMessage(requestResponseDto);
        return requestResponseDto.getTicket();
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

    @Override
    public List<Ticket> getTicketsByName(String name) throws TicketException {
        SearchDto searchDto = new SearchDto(name);
        RequestResponseDto requestResponseDto = new RequestResponseDto();
        requestResponseDto.setReqResponseType(ReqResponseType.SEARCH);
        requestResponseDto.setSearchDto(searchDto);

        requestResponseDto = client.sendMessage(requestResponseDto);

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

        requestResponseDto = client.sendMessage(requestResponseDto);

        localTicketStore = new HashMap<>();
        for (Ticket ticket : requestResponseDto.getTickets()) {
            localTicketStore.put(ticket.getId(), ticket);
        }
        return localTicketStore.values().stream().map(ticket -> (Ticket) ticket.clone()).collect(Collectors.toList());
    }

}
