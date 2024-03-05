package datagramsocket3;

import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket3;

import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.nio.ByteBuffer;

public class Sender3 {
    static DatagramSocket3 sending_socket;
    public static void main(String[] args) throws Exception{
        {
            int PORT = 55555;
            InetAddress clientIP = null;
            try {
                clientIP = InetAddress.getByName("localhost");
            } catch (UnknownHostException e) {
                System.out.println("ERROR: AudioSender: Could not find client IP");
                e.printStackTrace();
                System.exit(0);
            }

            try {
                sending_socket = new DatagramSocket3();
            } catch (SocketException e) {
                System.out.println("ERROR: AudioSender: Could not open UDP socket to send from.");
                e.printStackTrace();
                System.exit(0);
            }

            AudioRecorder recorder = null;
            try {
                recorder = new AudioRecorder();
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }
            int recordTime = 10;
            int key = 15;
            // Main loop
            boolean running = true;
            while (running) {
                try {
                    System.out.println("Recording Audio...");
                    for (int i = 0; i < Math.ceil(recordTime / 0.032); i++) {
                        byte[] block = recorder.getBlock();
                        short authenticationKey = 10;
                        // encrypt data from here
                        ByteBuffer unwrapEncrypt = ByteBuffer.allocate(block.length);
                        ByteBuffer plainText = ByteBuffer.wrap(block);
                        for( int j = 0; j < block.length/4; j++) {
                            int fourByte = plainText.getInt();
                            fourByte = fourByte ^ key; // XOR operation with key
                            unwrapEncrypt.putInt(fourByte);
                        }
                        byte[] encryptedBlock = unwrapEncrypt.array();
                        // This line authentication (payload with header) VoIPpacket
                        ByteBuffer VoIPpacket = ByteBuffer.allocate(encryptedBlock.length + 2);
                        // put the block into the VoIPpacket
                        VoIPpacket.put(encryptedBlock);
                        VoIPpacket.putShort(authenticationKey);
                        DatagramPacket packet = new DatagramPacket(VoIPpacket.array(), VoIPpacket.array().length, clientIP, PORT);
                        sending_socket.send(packet);
                    }
                    running = false;
                } catch (Exception e) {
                    System.out.println("ERROR: AudioSender: Some error occurred!");
                    e.printStackTrace();
                }
            }
            sending_socket.close();
        }
    }
}
