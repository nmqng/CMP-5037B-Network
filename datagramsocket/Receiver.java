package datagramsocket;

import CMPC3M06.AudioPlayer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class Receiver {
    static DatagramSocket receiving_socket;
    public static void main (String[] args) throws Exception {
        int PORT = 55555;
        try {
            receiving_socket = new DatagramSocket(PORT);
        } catch (SocketException e){
            System.out.println("ERROR: AudioReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }
        boolean running = true;
        byte[] buffer = new byte[516];
        DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
        AudioPlayer player = new AudioPlayer();

        int[] key = {2,0,3,1};
        int sharedKey = 402;
        // Inverse permutation key
        int[] inverseKey = new int[key.length];
        for (int i = 0; i < key.length; i++) {
            inverseKey[key[i]] = i;
        }

        while (running){
            try{
                receiving_socket.receive(packet);
                ByteBuffer cipherText = ByteBuffer.wrap(packet.getData()); // wrap the encryptedBlock
                int encryptedAuthKey = cipherText.getInt();
                int decryptedAuthKey = encryptedAuthKey ^ sharedKey;
                byte[] decryptedBlock = new byte[buffer.length-4];

                if (decryptedAuthKey == 331){
                    for (int i = 0; i < buffer.length-4; i += inverseKey.length) {
                        for (int j = 0; j < inverseKey.length; j++) {
                            decryptedBlock[i + j] = buffer[i + 4 + inverseKey[j]];
                        }
                    }
                    player.playBlock(decryptedBlock);
                } else {
                    System.out.println("Authentication key doesnt match");
                }
                // uncomment next line for receiving the packet without decryption
//                player.playBlock(packet.getData());
            } catch (Exception e){
                System.out.println("ERROR: AudioReceiver: Some error occurred!");
                e.printStackTrace();
            }
        }
        player.close();
        receiving_socket.close();
    }
}
