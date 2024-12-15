package de.uniba.rz.backend;



import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.Type;

import java.util.List;

public class TicketService {

    public static final TicketService instance = new TicketService();
    private final TicketStore ticketStore;

    private TicketService() {
        this.ticketStore = new SimpleTicketStore();
    }

    public List<Ticket> getAll() {
        return ticketStore.getAllTickets();
    }


    public Ticket getById(int id)  {
        return ticketStore.getTicketById(id);
    }

    public Ticket create(Ticket newTicket) {
        if (newTicket == null) {
            return null;
        }
        return ticketStore.storeNewTicket(
                newTicket.getReporter(),
                newTicket.getTopic(),
                newTicket.getDescription(),
                newTicket.getType(),
                newTicket.getPriority()
        );
    }

    public Ticket update(Ticket newTicket) {
        if (newTicket == null) {
            return null;
        }
        try {
            return ticketStore.updateTicketStatus(newTicket);
        } catch (Exception exception) {
            return null;
        }
    }

    public List<Ticket> getTicketsByName(String name) {
        return ticketStore.getTicketsByName(name);
    }
    public List<Ticket> getTicketsByNameAndType(String name, Type type) {
        return ticketStore.getTicketsByNameAndType(name, type);
    }
}
