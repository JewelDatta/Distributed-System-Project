package de.uniba.rz.app;

import de.uniba.rz.entities.*;
import de.uniba.rz.io.rpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GrpcTicketManagementBackend implements TicketManagementBackend{

    private ManagedChannel channel;
    private TicketServiceGrpc.TicketServiceBlockingStub syncStub;
    private TicketServiceGrpc.TicketServiceStub asyncStub;

    public GrpcTicketManagementBackend(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    public GrpcTicketManagementBackend(ManagedChannelBuilder<?> channelBuilder) {
        this.channel = channelBuilder.build();

        // stubs are generated by the "protoc" tool (in our case during the gradle build)
        this.syncStub = TicketServiceGrpc.newBlockingStub(this.channel);
        this.asyncStub = TicketServiceGrpc.newStub(this.channel);


        this.asyncStub.startAutoUpdating(AutoUpdatingRequest.getDefaultInstance(), new StreamObserver<>() {
            @Override
            public void onNext(TicketResponse value) {
                System.out.println("Received the newly created ticket "+ value);
                //todo: refresh the table view
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    @Override
    public void triggerShutdown() {
        if (!channel.isShutdown()) {
            System.out.println("Trying to shut down the client . . .");
            while (true) {
                try {
                    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                    System.out.println("Client successfully shutdown!");
                } catch (InterruptedException e) {
                    // no handling of the InterruptedException needed
                }
                break;
            }
        }
    }

    @Override
    public Ticket createNewTicket(String reporter, String topic, String description, Type type, Priority priority)
            throws TicketException {
        try {
            TicketRequest request = TicketRequest.newBuilder()
                    .setReporter(reporter)
                    .setTopic(topic)
                    .setDescription(description)
                    .setType(TicketType.valueOf(type.name()))
                    .setPriority(TicketPriority.valueOf(priority.name()))
                    .setStatus(TicketStatus.valueOf(Status.NEW.name()))
                    .build();

            TicketResponse response = this.syncStub.post(request);
            return getTicketFromResponse(response);
        } catch (StatusRuntimeException e) {
            System.err.println("The server is unresponsive.");
            this.triggerShutdown();
            throw new TicketException("Could not create ticket " + e.getMessage());
        }

    }

    @Override
    public List<Ticket> getAllTickets() throws TicketException {
        System.out.println("this method is called");
        try {
            List<Ticket> tickets = new ArrayList<>();
            Empty request = Empty.newBuilder().build();
            final boolean[] receiveCompleted = {false};

            StreamObserver<TicketResponse> observer = new StreamObserver<>() {
                /**
                 * Executed when the client recognizes an element in the observed stream
                 *
                 * @param response
                 */
                @Override
                public void onNext(TicketResponse response) {
                    Ticket newTicket = getTicketFromResponse(response);
                    tickets.add(newTicket);
                }

                /**
                 * Executed when the client recognizes an error in the observed stream
                 *
                 * @param t
                 */
                @Override
                public void onError(Throwable t) {
                    io.grpc.Status status = io.grpc.Status.fromThrowable(t);
                    System.err.println("Greeting failed: " + status);
                    triggerShutdown();
                }

                /**
                 * Executed, when the server finished its job. Might be a good location to shutdown the channel
                 * (depending on the business logic of your program).
                 */
                @Override
                public void onCompleted() {
                    receiveCompleted[0] = true;
                    System.out.println("The server send all the necessary information. Communication completed successfully.");
                }
            };

            // execution of the asynchronous call
            this.asyncStub.getAll(request, observer);
            while (!receiveCompleted[0]){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return tickets;
        } catch (StatusRuntimeException e) {
            System.err.println("The server is unresponsive.");
            this.triggerShutdown();
            throw new TicketException("Exception while receiving tickets");
        }
    }


    @Override
    public Ticket getTicketById(int id) throws TicketException {
        try {
            TicketIdRequest request = TicketIdRequest.newBuilder().setId(id).build();
            TicketResponse response = syncStub.getTicketById(request);
            return getTicketFromResponse(response);
        } catch (StatusRuntimeException e) {
            System.err.println("The server is unresponsive.");
            this.triggerShutdown();
            throw new TicketException("Could not get ticket "+ e.getMessage());
        }
    }

    @Override
    public Ticket acceptTicket(int id) throws TicketException {
        return updateTicketStatus(id, Status.ACCEPTED);
    }

    @Override
    public Ticket rejectTicket(int id) throws TicketException {
        return updateTicketStatus(id, Status.REJECTED);
    }

    @Override
    public Ticket closeTicket(int id) throws TicketException {
        return updateTicketStatus(id, Status.CLOSED);
    }

    @Override
    public List<Ticket> getTicketsByName(String name) throws TicketException {
        return null;
    }

    @Override
    public List<Ticket> getTicketsByNameAndType(String name, Type type) throws TicketException {
        return null;
    }

    private Ticket updateTicketStatus(int id, Status status) throws TicketException {
        try {
            UpdateStatusRequest request = UpdateStatusRequest.newBuilder()
                    .setId(id)
                    .setStatus(TicketStatus.valueOf(status.name()))
                    .build();
            TicketResponse response = syncStub.updateStatus(request);
            return getTicketFromResponse(response);
        } catch (StatusRuntimeException e) {
            System.err.println("The server is unresponsive.");
            this.triggerShutdown();
            throw new TicketException("Could not update ticket "+ e.getMessage());
        }
    }

    private Ticket getTicketFromResponse(TicketResponse response) {
        return new Ticket(
                response.getId(),
                response.getReporter(),
                response.getTopic(),
                response.getDescription(),
                Type.valueOf(response.getType().name()),
                Priority.valueOf(response.getPriority().name()),
                Status.valueOf(response.getStatus().name())
        );
    }
}