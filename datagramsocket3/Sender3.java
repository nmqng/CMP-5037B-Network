package datagramsocket3;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket3;

import javax.sound.sampled.LineUnavailableException;

public class Sender3 {
    static DatagramSocket3 sending_socket;
    static byte[] lastSentPacket = new byte[516]; // Store the last sent packet
    static int MAX_RETRY = 5; // Maximum number of retries for packet transmission
    static int TIMEOUT = 1000; // Timeout in milliseconds

    public static void main(String[] args) throws LineUnavailableException {
        int PORT = 55555;
        InetAddress clientIP = null;

        try {
            clientIP = InetAddress.getLocalHost(); // Get the local host IP address
        } catch (UnknownHostException e) {
            System.out.println("ERROR: Sender: Could not find client IP");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            sending_socket = new DatagramSocket3(); // Initialize the DatagramSocket3 for sending data
        } catch (SocketException e) {
            System.out.println("ERROR: Sender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(1);
        }

        AudioRecorder recorder = new AudioRecorder(); // Initialize the audio recorder
        int sequenceNumber = 0; // Initialize the sequence number for packet ordering

        // Main loop for sending audio data
        boolean running = true;
        while (running) {
            try {
                byte[] block = recorder.getBlock(); // Get a block of audio data
                byte[] sequenceNumberBytes = ByteBuffer.allocate(4).putInt(sequenceNumber).array(); // Convert the sequence number to bytes

                byte[] buffer = new byte[block.length + sequenceNumberBytes.length]; // Create a buffer to hold the audio data and sequence number
                System.arraycopy(block, 0, buffer, 0, block.length); // Copy the audio data into the buffer
                System.arraycopy(sequenceNumberBytes, 0, buffer, block.length, sequenceNumberBytes.length); // Copy the sequence number into the buffer

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT); // Create a DatagramPacket to send the data

                // Send packet with retry mechanism
                boolean sent = false;
                int retryCount = 0;
                while (!sent && retryCount < MAX_RETRY) { // Retry sending packet until successful or max retries reached
                    try {
                        sending_socket.send(packet); // Send the packet
                        lastSentPacket = Arrays.copyOf(buffer, buffer.length); // Update the last sent packet
                        sent = true;
                    } catch (Exception e) {
                        System.out.println("ERROR: Sender: Error sending packet, retrying...");
                        e.printStackTrace();
                        retryCount++;
                    }
                }

                if (!sent) { // Check if packet failed to send after maximum retries
                    System.out.println("ERROR: Sender: Max retries reached, unable to send packet.");
                    running = false; // Stop the sender if unable to send packet
                }

                sequenceNumber++; // Increment sequence number for next packet
            } catch (Exception e) {
                System.out.println("ERROR: Sender: Some error occurred!");
                e.printStackTrace();
                running = false; // Stop the sender in case of any error
            }
        }
        sending_socket.close(); // Close the sending socket after sending completes or on error
    }
}