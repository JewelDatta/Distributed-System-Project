package de.uniba.rz.backend;


import java.util.List;

import de.uniba.rz.entities.Priority;
import de.uniba.rz.entities.Status;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.Type;

public interface TicketStore {

    Ticket storeNewTicket(String reporter, String topic, String description,
                        Type type, Priority priority);

    Ticket storeNewTicket(Ticket ticket);

//    void updateTicketStatus(int ticketId, Status newStatus) throws UnknownTicketException, IllegalStateException;

    Ticket updateTicketStatus(Ticket ticket) throws UnknownTicketException, IllegalStateException;

    List<Ticket> getAllTickets();

    Ticket getTicketById(int id);

    List<Ticket> getTicketsByName(String name);

    List<Ticket> getTicketsByNameAndType(String name, Type type);

}
