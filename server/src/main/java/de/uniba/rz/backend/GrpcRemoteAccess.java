package de.uniba.rz.backend;

import de.uniba.rz.entities.Priority;
import de.uniba.rz.entities.Status;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.Type;
import de.uniba.rz.io.rpc.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GrpcRemoteAccess extends RemoteAccess {

    private final int port;
    private Server server;
    private final static BlockingQueue<Ticket> blockingQueue = new ArrayBlockingQueue<>(1, true);;
    private final static HashSet<StreamObserver<TicketResponse>> observers = new LinkedHashSet<>();

    public GrpcRemoteAccess(int port) {
        this.port = port;
    }

    @Override
    void prepareStartup(TicketStore ticketStore) {
        this.ticketStore = ticketStore;
        this.server = ServerBuilder.forPort(port).addService(new TicketImpl(this.ticketStore)).build();
    }

    @Override
    void shutdown() {
        try {
            blockUntilShutdown();
        } catch (InterruptedException e) {
            System.out.println("Error in shutdown method " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            server.start();
        } catch (IOException e) {
            System.out.println("Could not start server " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("gRPC Server started and listened on port " + this.port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            GrpcRemoteAccess.this.stop();
            System.err.println("*** gRPC Server shut down");
        }));

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Ticket ticket = blockingQueue.take();
                for (StreamObserver<TicketResponse> observer : observers) {
                    observer.onNext(ticketResponse(ticket));
                }
            } catch (InterruptedException ie) {
                System.err.println("Cannot broadcast created ticket " + ie.getMessage());
                ie.printStackTrace();
            }
        }
    }

    /**
     * Method to stop the server, if a server is present.
     */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Blocking method until the shutdown hock terminates the server.
     *
     * @throws InterruptedException
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Custom class for the implementation of the base service, which is an abstract class. The method must be
     * overridden since the default implementation has no implemented logic.
     */
    static class TicketImpl extends TicketServiceGrpc.TicketServiceImplBase {

        private final TicketStore ticketStore;

        public TicketImpl(TicketStore ticketStore) {
            this.ticketStore = ticketStore;
        }

        @Override
        public void post(TicketRequest request, StreamObserver<TicketResponse> responseObserver) {
            Ticket newTicket = ticketStore.storeNewTicket(
                    request.getReporter(),
                    request.getTopic(),
                    request.getDescription(),
                    Type.valueOf(request.getType().name()),
                    Priority.valueOf(request.getPriority().name())
            );
            responseObserver.onNext(ticketResponse(newTicket));
            responseObserver.onCompleted();

           try{
               blockingQueue.put(newTicket);
           }catch (InterruptedException ie){
               System.err.println("Canno put ticket to queue "+ ie.getMessage());
           }
        }

        @Override
        public void getAll(Empty request, StreamObserver<TicketResponse> responseObserver) {
            List<Ticket> tickets = ticketStore.getAllTickets();
            for (Ticket ticket : tickets) {
                responseObserver.onNext(ticketResponse(ticket));
            }
            responseObserver.onCompleted();
        }

        @Override
        public void getTicketById(TicketIdRequest request, StreamObserver<TicketResponse> responseObserver) {
            Ticket ticket = ticketStore.getTicketById(request.getId());
            if (ticket == null) {
                responseObserver.onError(new UnknownTicketException("Ticket not found " + request.getId()));
            }
            assert ticket != null;
            responseObserver.onNext(ticketResponse(ticket));
            responseObserver.onCompleted();
        }

        @Override
        public void updateStatus(UpdateStatusRequest request, StreamObserver<TicketResponse> responseObserver) {
            try {
                Ticket ticket = ticketStore.getTicketById(request.getId());
                ticket.setStatus(Status.valueOf(request.getStatus().name()));
                ticket = ticketStore.updateTicketStatus(ticket);
                responseObserver.onNext(ticketResponse(ticket));
                responseObserver.onCompleted();
            } catch (UnknownTicketException e) {
                e.printStackTrace();
                responseObserver.onError(new UnknownTicketException("Ticket id is invalid " + request.getId()));
            }
        }

        @Override
        public void startAutoUpdating(AutoUpdatingRequest request, StreamObserver<TicketResponse> responseObserver) {
            observers.add(responseObserver);
        }
    }

    private static TicketResponse ticketResponse(Ticket ticket) {
        return TicketResponse.newBuilder()
                .setId(ticket.getId())
                .setReporter(ticket.getReporter())
                .setTopic(ticket.getTopic())
                .setDescription(ticket.getDescription())
                .setType(TicketType.valueOf(ticket.getType().name()))
                .setPriority(TicketPriority.valueOf(ticket.getPriority().name()))
                .setStatus(TicketStatus.valueOf(ticket.getStatus().name()))
                .build();
    }
}
