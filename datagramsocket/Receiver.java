package datagramsocket;
import CMPC3M06.AudioPlayer;
import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.nio.ByteBuffer;

public class Receiver {
    static DatagramSocket receiving_socket;
    public static void main(String[] args) {
        int PORT = 55555;
        try {
            receiving_socket = new DatagramSocket(PORT);
        } catch (SocketException e){
            System.out.println("ERROR: AudioReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }
        boolean running = true;
        byte[] buffer = new byte[520];
//        int p = 5;
//        int q = 7;
//        int n = p*q;
//        int e1 = 5;
//        double d = 29;

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
                int key1 = cipherText.getInt();
                int key = 10;
                short authenticationKey = cipherText.getShort();

                if (authenticationKey == 10){
                    for(int j = 0; j < (packet.getLength()-8)/4; j++) {
                        int fourByte = cipherText.getInt();
                        fourByte = fourByte ^ key; // XOR decrypt
                        unwrapDecrypt.putInt(fourByte);
                    }
                    byte[] decryptedBlock = unwrapDecrypt.array();
                    player.playBlock(decryptedBlock);
                } else {
                    System.out.println("Authentication key not match ");
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
