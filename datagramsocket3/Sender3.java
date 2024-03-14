package datagramsocket3;

import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket3;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Sender3 {
    static DatagramSocket3 sending_socket;

    public static void main(String[] args) throws Exception{
        int PORT = 55550;
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("localhost");
        } catch (UnknownHostException e){
            System.out.println("ERROR: AudioSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }

        try{
            sending_socket = new DatagramSocket3();
        } catch (SocketException e){
            System.out.println("ERROR: AudioSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        //Initialise AudioPlayer and AudioRecorder objects
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

                    int index = 0;
                    for ( int j = 0; j < block.length; j++){
                        buffer[index++] = block[j];
                    }
                    for ( int j = 0; j < sequence_number_byte.length; j++){
                        buffer[index++] = sequence_number_byte[j];
                    }

                    packet.setData(buffer);
                    sending_socket.send(packet);
                    sequenceNumber++;
                    System.arraycopy(block,0,buffer,0,block.length);
                    System.arraycopy(sequence_number_byte,0,buffer,block.length, sequence_number_byte.length);
                    packet.setData(buffer);
                    sending_socket.send(packet);
                    sequenceNumber++;
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
