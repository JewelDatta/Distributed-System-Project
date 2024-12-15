package de.uniba.rz.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniba.rz.entities.ReqResponseType;
import de.uniba.rz.entities.RequestResponseDto;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.TicketException;

import java.io.IOException;
import java.util.List;

/**
 * Basic interface to enable remote access to the {@link TicketStore} managing the tickets internally
 *
 */
public abstract class RemoteAccess implements Runnable {

	protected ObjectMapper objectMapper;
	protected TicketStore ticketStore;

	/**
	 * Generic startup method which might be used to prepare the actual execution
	 * 
	 * @param ticketStore
	 * reference to the {@link TicketStore} which is used by the application
	 */
    abstract void prepareStartup(TicketStore ticketStore);

    /**
     * Triggers the graceful shutdown of the system.
     */
    abstract void shutdown();

	protected RequestResponseDto processReceivedData(byte[] receivedData) throws TicketException, IOException, UnknownTicketException {
		RequestResponseDto requestResponseDto = objectMapper.readValue(receivedData, RequestResponseDto.class);
		System.out.println(requestResponseDto);
		if(requestResponseDto.getReqResponseType()== ReqResponseType.CREATE){
			Ticket ticket = ticketStore.storeNewTicket(requestResponseDto.getTicket());
			requestResponseDto.setTicket(ticket);
		}else if(requestResponseDto.getReqResponseType()==ReqResponseType.GET){
			if(requestResponseDto.getTicket()!=null && requestResponseDto.getTicket().getId()>0){
				Ticket ticket = ticketStore.getTicketById(requestResponseDto.getTicket().getId());
				requestResponseDto.setTicket(ticket);
			}else {
				List<Ticket> allTickets = ticketStore.getAllTickets();
				requestResponseDto.setTickets(allTickets);
			}
		}else if(requestResponseDto.getReqResponseType()==ReqResponseType.PUT){
			Ticket ticket = ticketStore.updateTicketStatus(requestResponseDto.getTicket());
			requestResponseDto.setTicket(ticket);
		}else if(requestResponseDto.getReqResponseType()==ReqResponseType.SEARCH){
			List<Ticket> tickets;
			if(requestResponseDto.getSearchDto().getType()==null){
				tickets = ticketStore.getTicketsByName(requestResponseDto.getSearchDto().getName());
			}else {
				tickets = ticketStore.getTicketsByNameAndType(requestResponseDto.getSearchDto().getName(),
						requestResponseDto.getSearchDto().getType());
			}
			requestResponseDto.setTickets(tickets);
		}
		return requestResponseDto;
	}

}
