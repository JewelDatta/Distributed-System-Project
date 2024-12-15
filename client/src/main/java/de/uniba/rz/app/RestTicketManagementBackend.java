package de.uniba.rz.app;

import de.uniba.rz.app.configs.Configuration;
import de.uniba.rz.entities.*;
import de.uniba.rz.entities.TicketDto;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Properties;

public class RestTicketManagementBackend implements TicketManagementBackend{

    private final Client client;
    private final String uri;

    public RestTicketManagementBackend() {
        Properties properties = Configuration.loadProperties();
        assert properties != null;
        uri = properties.getProperty("serverUri");
        client = ClientBuilder.newClient();
    }

    @Override
    public void triggerShutdown() {

    }

    @Override
    public Ticket createNewTicket(String reporter, String topic, String description, Type type, Priority priority) throws TicketException {
        Ticket newTicket = new Ticket();
        newTicket.setReporter(reporter);
        newTicket.setTopic(topic);
        newTicket. setDescription(description);
        newTicket.setType(type);
        newTicket.setPriority(priority);

        Response response = client.target(uri)
                .path("tickets")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(newTicket));

        if (response.getStatus() == 201) {
            System.out.println(response.getLocation());
            TicketDto ticketDto =  client.target(response.getLocation())
                    .request(MediaType.APPLICATION_JSON).get()
                    .readEntity(TicketDto.class);
            return ticketDto.unmarshall();
        } else {
            throw new TicketException("Unknown response code " + response.getStatus());
        }
    }

    @Override
    public List<Ticket> getAllTickets() throws TicketException {
        Response response = client.target(uri)
                .path("tickets")
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == 200) {
            return response.readEntity(new GenericType<>() {});
        } else {
            throw new TicketException("Unknown response code " + response.getStatus());
        }
    }

    @Override
    public Ticket getTicketById(int id) throws TicketException {
        Response response = client.target(uri)
                .path("/tickets")
                .path("/"+id)
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == 200) {
            TicketDto ticketDto = response.readEntity(TicketDto.class);
            return ticketDto.unmarshall();
        } else {
            throw new TicketException("Unknown response code " + response.getStatus());
        }
    }

    @Override
    public Ticket acceptTicket(int id) throws TicketException {
        return updateTicketStatus(id, Status.ACCEPTED);
    }

    @Override
    public Ticket rejectTicket(int id) throws TicketException {
        return updateTicketStatus(id, Status.REJECTED);
    }

    @Override
    public Ticket closeTicket(int id) throws TicketException {
        return updateTicketStatus(id, Status.CLOSED);
    }

    private Ticket updateTicketStatus(int id, Status status) throws TicketException {
        System.out.println("udpateTicketStatus "+id + " status: "+status.toString());
        Ticket oldTicket = getTicketById(id);
        oldTicket.setStatus(status);

        Response response = client.target(uri)
                .path("/tickets")
                .path("/"+id)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(oldTicket));

        if (response.getStatus() == 200) {
            return client.target(uri)
                    .path("/tickets")
                    .path("/"+id)
                    .request(MediaType.APPLICATION_JSON)
                    .get()
                    .readEntity(Ticket.class);
        } else {
            throw new TicketException("Unknown response code " + response.getStatus());
        }
    }

    @Override
    public List<Ticket> getTicketsByName(String name, int offset, int limit) throws TicketException {
        System.out.println("getTicketsByName name: "+name);
        Response response = client.target(uri)
                .path("/tickets")
                .path("/query")
                .queryParam("name",name)
                .queryParam("offset", offset)
                .queryParam("limit", limit)
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == 200 ) {
            return  response.readEntity(new GenericType<>(){});
        } else {
            throw new TicketException("Unknown response code " + response.getStatus());
        }
    }

    @Override
    public List<Ticket> getTicketsByNameAndType(String name, Type type, int offset, int limit) throws TicketException {
        System.out.println("getTicketsByName name: "+name);
        Response response = client.target(uri)
                .path("/tickets")
                .path("/query")
                .queryParam("name",name)
                .queryParam("type",type.toString())
                .queryParam("offset", offset)
                .queryParam("limit", limit)
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == 200 ) {
            return  response.readEntity(new GenericType<>(){});
        } else {
            throw new TicketException("Unknown response code " + response.getStatus());
        }
    }

}
