package de.uniba.rz.entities;


import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;
import java.util.List;

@XmlRootElement
@XmlType(propOrder = {"pagination", "tickets", "href"})
public class PaginatedTickets {

    private Pagination pagination;
    private List<TicketDto> tickets;
    private URI href;

    public PaginatedTickets() {

    }

    public PaginatedTickets(final Pagination pagination, final List<TicketDto> tickets, final URI href) {
        this.pagination = pagination;
        this.tickets = tickets;
        this.href = href;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public List<TicketDto> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketDto> tickets) {
        this.tickets = tickets;
    }

    public URI getHref() {
        return this.href;
    }

    public void setHref(final URI href) {
        this.href = href;
    }

}
