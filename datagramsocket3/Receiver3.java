package datagramsocket3;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

import CMPC3M06.AudioPlayer;
import uk.ac.uea.cmp.voip.DatagramSocket3;

public class Receiver3 {
    static DatagramSocket3 receiving_socket;
    static byte[] lastReceivedPacket = new byte[512]; // Store the last received packet
    static int TIMEOUT = 5000; // Timeout in milliseconds

    public static void main(String[] args) {
        int PORT = 55555;
        try {
            receiving_socket = new DatagramSocket3(PORT); // Initialize the DatagramSocket3 for receiving data
            AudioPlayer player = new AudioPlayer(); // Initialize the audio player
            int expectedSequenceNumber = 0; // Initialize the expected sequence number for packet ordering

            // Main loop for receiving audio data
            while (true) {
                byte[] buffer = new byte[516]; // Create a buffer to hold received data
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length); // Create a DatagramPacket to receive data

                // Set timeout for receiving socket to handle potential packet loss
                receiving_socket.setSoTimeout(TIMEOUT);

                try {
                    receiving_socket.receive(packet); // Receive a packet
                } catch (SocketTimeoutException e) {
                    System.out.println("ERROR: Receiver: Timeout occurred while waiting for packet.");
                    continue; // Continue waiting for the next packet on timeout
                }

                ByteBuffer wrappedBuffer = ByteBuffer.wrap(packet.getData()); // Wrap the received data in a ByteBuffer
                byte[] audioData = new byte[512]; // Create a buffer to hold the audio data
                byte[] sequenceNumberBytes = new byte[4]; // Create a buffer to hold the sequence number

                wrappedBuffer.get(audioData); // Extract the audio data from the received buffer
                wrappedBuffer.get(sequenceNumberBytes); // Extract the sequence number from the received buffer
                int receivedSequenceNumber = ByteBuffer.wrap(sequenceNumberBytes).getInt(); // Convert the sequence number bytes to integer

                if (receivedSequenceNumber == expectedSequenceNumber) { // Check if received packet is in order
                    lastReceivedPacket = Arrays.copyOf(packet.getData(), packet.getLength()); // Update the last received packet
                    player.playBlock(audioData); // Play the received audio block
                    expectedSequenceNumber++; // Increment expected sequence number for next packet
                } else {
                    System.out.println("WARNING: Receiver: Received out-of-sequence packet, expected: " + expectedSequenceNumber + ", received: " + receivedSequenceNumber);
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: Receiver: Some error occurred!");
            e.printStackTrace();
        } finally {
            receiving_socket.close(); // Close the receiving socket on error or completion
        }
    }
}