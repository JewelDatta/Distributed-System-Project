package de.uniba.rz.backend;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import de.uniba.rz.backend.common.TicketComparator;
import de.uniba.rz.entities.Priority;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.Type;

/**
 * This is a basic implementation of the <code>TicketStore</code> interface for
 * testing purposes only.
 * <p>
 * Caution: This class is neither thread-safe nor does it perform any checks in
 * the updateTicketStatus method
 * <p>
 * Do not use this class in the assignment solution but provide an own
 * implementation of <code>TicketStore</code>!
 */
public class SimpleTicketStore implements TicketStore {

    private final AtomicInteger nextTicketId = new AtomicInteger(0);
    private final List<Ticket> ticketList = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Ticket storeNewTicket(String reporter, String topic, String description, Type type, Priority priority) {
        System.out.println("Creating new Ticket from Reporter: " + reporter + " with the topic \"" + topic + "\"");
        Ticket newTicket = new Ticket(nextTicketId.incrementAndGet(), reporter, topic, description, type, priority);
        ticketList.add(newTicket);
        return newTicket;
    }

    @Override
    public Ticket storeNewTicket(Ticket ticket) {
        System.out.println("Creating new Ticket from Reporter: " + ticket.getReporter()
                + " with the topic \"" + ticket.getTopic() + "\"");
        ticket.setId(nextTicketId.incrementAndGet());
        ticketList.add(ticket);
        return ticket;
    }

    @Override
    public Ticket updateTicketStatus(Ticket updateTicket) throws UnknownTicketException, IllegalStateException {
        synchronized (this) {
            boolean valid = false;
            for (Ticket ticket : ticketList) {
                if (ticket.getId() == updateTicket.getId()) {
                    ticket.setStatus(updateTicket.getStatus());
                    updateTicket = ticket;
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                throw new UnknownTicketException("Ticket id is not valid " + updateTicket.getId());
            }
            return updateTicket;
        }
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketList;
    }

    @Override
    public Ticket getTicketById(int id) {
        System.out.println("Getting ticket by id "+ id);
        return this.ticketList.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    @Override
    public List<Ticket> getTicketsByName(String name) {
        return ticketList.stream().filter(d -> d.getTopic().contains(name) ||
                d.getReporter().contains(name) || d.getDescription().contains(name))
                .sorted(new TicketComparator()).collect(Collectors.toList());
    }

    @Override
    public List<Ticket> getTicketsByNameAndType(String name, Type type) {
        return ticketList.stream().filter(d -> d.getTopic().contains(name) && d.getType().equals(type))
                .collect(Collectors.toList());
    }
}
