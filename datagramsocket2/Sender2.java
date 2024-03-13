package datagramsocket2;
import java.nio.ByteBuffer;
import java.net.*;

import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket2;

public class Sender2 {
    static DatagramSocket2 sending_socket;
    static byte[] lastSentPacket = new byte[516];

    public static void main(String[] args) throws Exception{
        int PORT = 55555;
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("139.222.202.6");
        } catch (UnknownHostException e){
            System.out.println("ERROR: AudioSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }

        try{
            sending_socket = new DatagramSocket2();
        } catch (SocketException e){
            System.out.println("ERROR: AudioSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        AudioRecorder recorder = new AudioRecorder();
        int recordTime = Integer.MAX_VALUE;
        int sequenceNumber = 0;
        byte [] sequence_number_byte;

        // Main loop
        boolean running = true;
        while (running){
            try {
                System.out.println("Recording Audio...");
                byte[] buffer = new byte[516];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);
                for (int i = 0; i < Math.ceil(recordTime / 0.032); i++) {
                    byte[] block = recorder.getBlock();
                    sequence_number_byte = ByteBuffer.allocate(4).putInt(sequenceNumber).array();
                    System.arraycopy(block,0,buffer,0,block.length);
                    System.arraycopy(sequence_number_byte,0,buffer,block.length, sequence_number_byte.length);
                    packet.setData(buffer);
                    sending_socket.send(packet);
                    sequenceNumber++;
                    // Update the last sent packet
                    lastSentPacket = buffer;
                }
                running = false;
            } catch (Exception e){
                System.out.println("ERROR: Audio: Some error occurred!");
                e.printStackTrace();
            }
        }
        sending_socket.close();
    }
}
