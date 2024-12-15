package de.uniba.rz.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.stream.Collectors;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ticket")
@XmlType(propOrder = {"id", "reporter", "topic","description","type","priority", "status"})
public class TicketDto {

    private int id;
    private String reporter;
    private String topic;
    private String description;
    private Type type;
    private Priority priority;
    private Status status;

    public TicketDto() {
    }

    public TicketDto(Ticket ticket) {
        this.id = ticket.getId();
        this.reporter = ticket.getReporter();
        this.topic = ticket.getTopic();
        this.description = ticket.getDescription();
        this.type = ticket.getType();
        this.priority = ticket.getPriority();
        this.status = ticket.getStatus();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static List<TicketDto> marshall(final List<Ticket> ticketList) {
        return ticketList.stream().map(TicketDto::new).collect(Collectors.toList());
    }

    public Ticket unmarshall(){
        return new Ticket(this.id, this.reporter, this.topic, this.description, this.type, this.priority, this.status);
    }


    @Override
    public String toString() {
        return "TicketDto{" +
                "id=" + id +
                ", reporter='" + reporter + '\'' +
                ", topic='" + topic + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", priority=" + priority +
                ", status=" + status +
                '}';
    }
}
