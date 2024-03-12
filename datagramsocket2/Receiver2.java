package datagramsocket2;

import CMPC3M06.AudioPlayer;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class Receiver2 {
    static DatagramSocket2 receiving_socket;
    static byte[] lastReceivedPacket = new byte[512]; // Store the last received packet

    public static void main(String[] args) {
        int PORT = 55555;
        try {
            receiving_socket = new DatagramSocket2(PORT);
            AudioPlayer player = new AudioPlayer();
            int expectedSequenceNumber = 0;
            byte [] sequence_number_byte = new byte[4];
            byte [] audio_data = new byte[512];
            boolean running = true;

            while (running) {
                byte[] buffer = new byte[516];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                receiving_socket.receive(packet);

                ByteBuffer wrappedBuffer = ByteBuffer.wrap(packet.getData());
                int receivedSequenceNumber;

                System.arraycopy(packet.getData(),0,audio_data,0,audio_data.length);
                System.arraycopy(packet.getData(),audio_data.length,sequence_number_byte,0,4);
                receivedSequenceNumber = ByteBuffer.wrap(sequence_number_byte).getInt();

                if (receivedSequenceNumber == expectedSequenceNumber) {
//                    System.out.println("Received packet with sequence number: " + receivedSequenceNumber);
                    lastReceivedPacket = audio_data; // Update the last received packet
                } else {
                    // Report missing sequence numbers
//                    System.out.println("Missing sequence number(s) from " + expectedSequenceNumber + " to " + (receivedSequenceNumber - 1));
                    for (int i = expectedSequenceNumber; i < receivedSequenceNumber; i++) {
                        player.playBlock(lastReceivedPacket); // Play the last received packet for each missing packet
                    }
                }
                // Extract audio data from packet
                byte[] audioData = new byte[512];
                wrappedBuffer.get(audioData);

                player.playBlock(audioData); // Play the received audio block
                expectedSequenceNumber = receivedSequenceNumber + 1;
            }
        } catch (Exception e) {
            System.out.println("ERROR: Receiver: Some error occurred!");
            e.printStackTrace();
        }
        receiving_socket.close();
    }
}
