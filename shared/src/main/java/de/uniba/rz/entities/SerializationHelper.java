package de.uniba.rz.entities;

import java.io.*;

public class SerializationHelper {

    public static RequestResponseDto deserializeData(byte[] receivedData) throws TicketException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(receivedData);
             ObjectInput in = new ObjectInputStream(bis);) {
            return (RequestResponseDto) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new  TicketException("Could not create ticket " + e.getMessage());
        }
    }

    public static byte[] serialize(RequestResponseDto requestResponseDto) throws TicketException{
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ObjectOutputStream os = new ObjectOutputStream(out);) {
            os.writeObject(requestResponseDto);
            os.flush();
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new  TicketException("Could not create ticket " + e.getMessage());
        }
    }
}
