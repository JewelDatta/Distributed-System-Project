package de.uniba.rz.backend;

import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.Type;
import de.uniba.rz.entities.PaginatedTickets;
import de.uniba.rz.entities.PaginationHelper;
import de.uniba.rz.entities.TicketDto;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("tickets")
public class TicketResource {


    @GET
    public List<TicketDto> getAll() {
        return TicketDto.marshall(TicketService.instance.getAll());
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") int id) {
        Ticket ticket = TicketService.instance.getById(id);

        if (ticket == null) {
            throw new WebApplicationException("No Ticket found with id: " + id, Response.Status.NOT_FOUND);
        }
        return Response.ok().entity(new TicketDto(ticket)).build();
    }


    @POST
    public Response create(Ticket newTicket, @Context UriInfo uriInfo) {
        Ticket ticket = TicketService.instance.create(newTicket);

        if (ticket == null) {
            throw new WebApplicationException("Invalid request body", Response.Status.BAD_REQUEST);
        }

        UriBuilder path = uriInfo.getAbsolutePathBuilder();
        path.path(Integer.toString(ticket.getId()));
        return Response.created(path.build()).build();
    }

    @PUT
    @Path("{id}")
    public Response updateTicket(@PathParam("id") int id, Ticket updatedTicket) {
        System.out.println("updateTicket id: " + id);
        Ticket ticket = TicketService.instance.getById(id);
        if (ticket == null) {
            throw new WebApplicationException("No ticket found with id: " + id, Response.Status.NOT_FOUND);
        }
        if (updatedTicket == null) {
            throw new WebApplicationException("Bad request exception ", Response.Status.BAD_REQUEST);
        }
        Ticket resultTicket = TicketService.instance.update(updatedTicket);
        System.out.println("result ticket: " + resultTicket.getStatus());
        return Response.ok().entity(new TicketDto(resultTicket)).build();
    }

    @GET
    @Path("/query")
    public List<TicketDto> getTickets(@Context final UriInfo info,
                                      @QueryParam("name") final String name,
                                      @QueryParam("type") final String type,
                                      @QueryParam("offset") @DefaultValue("1") final int offset,
                                      @QueryParam("limit") @DefaultValue("10") final int limit
    ) {

        System.out.println("getTicketsByNameAndType name: " + name + " type: " + type);

        // Parameter validation
        if (offset < 1 || limit < 1) {
            throw new WebApplicationException("Bad request exception, PageLimit or page is less than 1. " +
                    "Read the documentation for a proper handling!", Response.Status.BAD_REQUEST);
        }

        try {
            List<Ticket> tickets;
            if (type == null) {
                tickets = TicketService.instance.getTicketsByName(name);
            } else {
                Type ticketType = Type.valueOf(type);
                tickets = TicketService.instance.getTicketsByNameAndType(name, ticketType);
            }
            System.out.println("getTickets size: " + tickets.size());

            PaginationHelper<Ticket> helper = new PaginationHelper<>(tickets);
            PaginatedTickets response = new PaginatedTickets(helper.getPagination(info, offset, limit),
                    TicketDto.marshall(helper.getPaginatedList()), info.getRequestUri());
            return response.getTickets();

        } catch (Exception exception) {
            System.out.println("Exception in search" + exception.getLocalizedMessage());
            throw new WebApplicationException("Bad request exception", Response.Status.BAD_REQUEST);
        }
    }

}
