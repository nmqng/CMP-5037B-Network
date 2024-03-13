package datagramsocket;

import CMPC3M06.AudioRecorder;
import java.net.*;
import java.nio.ByteBuffer;

public class Sender {
    static DatagramSocket sending_socket;

    public static void main(String[] args) throws Exception{
        int PORT = 55555;
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("localhost"); //139.222.202.6
        } catch (UnknownHostException e){
            System.out.println("ERROR: AudioSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }

        try{
            sending_socket = new DatagramSocket();
        } catch (SocketException e){
            System.out.println("ERROR: AudioSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        AudioRecorder recorder = new AudioRecorder();
        int recordTime = Integer.MAX_VALUE;
        int[] key = {2,0,3,1};
        int authKey = 331;
        int sharedKey = 402;
        int encryptedAuthKey = authKey ^ sharedKey;

        // Main loop
        boolean running = true;
        while (running){
            try {
                System.out.println("Recording Audio...");
                for (int i = 0; i < Math.ceil(recordTime / 0.032); i++) {
                    byte[] block = recorder.getBlock();
                    byte[] encryptedBlock = new byte[block.length];
                    for (int j = 0; j < block.length; j += key.length) {
                        for (int k = 0; k < key.length; k++) {
                            encryptedBlock[j + k] = block[j + key[k]];
                        }
                    }

                    ByteBuffer VoIPpacket = ByteBuffer.allocate(516);
                    VoIPpacket.putInt(encryptedAuthKey);
                    VoIPpacket.put(encryptedBlock);
                    DatagramPacket packet = new DatagramPacket(VoIPpacket.array(), VoIPpacket.array().length, clientIP, PORT);
                    sending_socket.send(packet);
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
