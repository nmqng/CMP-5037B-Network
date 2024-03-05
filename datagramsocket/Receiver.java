package datagramsocket;

import CMPC3M06.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class Receiver {
    static DatagramSocket receiving_socket;
    public static void main(String[] args) {
        {
            int PORT = 55555;
            try {
                receiving_socket = new DatagramSocket(PORT);
            } catch (SocketException e){
                System.out.println("ERROR: AudioReceiver: Could not open UDP socket to receive from.");
                e.printStackTrace();
                System.exit(0);
            }
            boolean running = true;
            byte[] buffer = new byte[514];
            int key = 15;
            DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
            AudioPlayer player = null;
            try {
                player = new AudioPlayer();
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }
            while (running){
                try{
                    receiving_socket.receive(packet);
                    // decrypt data from here
                    ByteBuffer unwrapDecrypt = ByteBuffer.allocate(packet.getLength()); // allocate block length
                    ByteBuffer cipherText = ByteBuffer.wrap(packet.getData()); // wrap the encryptedBlock
                    short authenticationKey = cipherText.getShort();
                    if (authenticationKey == 10){
                        for(int j = 0; j < packet.getLength()/4; j++) {
                            int fourByte = cipherText.getInt();
                            fourByte = fourByte ^ key; // XOR decrypt
                            unwrapDecrypt.putInt(fourByte);
                        }
                        byte[] decryptedBlock = unwrapDecrypt.array();
                        player.playBlock(decryptedBlock);
                    } else {
                        System.out.println("Authentication key not match");
                    }
                } catch (Exception e){
                    System.out.println("ERROR: AudioReceiver: Some error occurred!");
                    e.printStackTrace();
                }
            }
            player.close();
            receiving_socket.close();
        }
    }
}
