package de.uniba.rz.entities;

import java.io.Serializable;
import java.util.List;

public class RequestResponseDto implements Serializable {

    private static final long serialVersionUID = -6979364632920616224L;

    private ReqResponseType reqResponseType;
    private Ticket ticket;
    private List<Ticket> tickets;
    private SearchDto searchDto;

    public ReqResponseType getReqResponseType() {
        return reqResponseType;
    }

    public void setReqResponseType(ReqResponseType reqResponseType) {
        this.reqResponseType = reqResponseType;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public SearchDto getSearchDto() {
        return searchDto;
    }

    public void setSearchDto(SearchDto searchDto) {
        this.searchDto = searchDto;
    }

    @Override
    public String toString() {
        return "RequestResponseDto{" +
                "reqResponseType=" + reqResponseType +
                ", ticket=" + ticket +
                ", tickets=" + tickets +
                ", searchDto=" + searchDto +
                '}';
    }

}
