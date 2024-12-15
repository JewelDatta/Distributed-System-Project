package de.uniba.rz.backend.common;

import de.uniba.rz.entities.Ticket;

import java.util.Comparator;

public class TicketComparator implements Comparator<Ticket> {

    @Override
    public int compare(Ticket o1, Ticket o2) {
        return Integer.compare(getPriority(o1), getPriority(o2));
    }

    private int getPriority(Ticket ticket) {
        switch (ticket.getPriority()) {
            case CRITICAL:
                return 1;
            case MAJOR:
                return 2;
            case MINOR:
                return 3;
            default:
                return Integer.MAX_VALUE;
        }
    }

}
