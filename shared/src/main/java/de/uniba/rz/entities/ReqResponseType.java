package de.uniba.rz.entities;

import java.io.Serializable;

/**
 * Enumeration to describe the Type of a {@link Ticket} or
 * {@link TransferTicket}.
 * 
 * Possible Values:
 * <ul>
 * <li>{@code CREATE}</li>
 * <li>{@code PUT}</li>
 * <li>{@code DELETE}</li>
 * <li>{@code GET}</li>
 * </ul>
 * 
 */
public enum ReqResponseType implements Serializable {

    CREATE, PUT, SEARCH, GET

}
