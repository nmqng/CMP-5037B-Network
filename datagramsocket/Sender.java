package datagramsocket;
import CMPC3M06.AudioRecorder;
import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.nio.ByteBuffer;

public class Sender {
    static DatagramSocket sending_socket;
    public static void main(String[] args) throws Exception{
        int PORT = 55555;
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("139.222.202.6");
        } catch (UnknownHostException e) {
            System.out.println("ERROR: AudioSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }

        try {
            sending_socket = new DatagramSocket();
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
        int recordTime = Integer.MAX_VALUE;
        int key = 12;
        short authenticationKey = 10;
        // key encryption
//        int p = 5;
//        int q = 7;
//        int n = p*q;
////        int z = (p-1)*(q-1);
//        int e1 = 5;
//        int encryptedKey = (int) ( Math.pow(authenticationKey, e1) % n);

        boolean running = true;
        while (running) {
            try {
                System.out.println("Recording Audio...");
                for (int i = 0; i < Math.ceil(recordTime / 0.032); i++) {
                    byte[] block = recorder.getBlock();
                    // encrypt data from here
                    ByteBuffer unwrapEncrypt = ByteBuffer.allocate(block.length);
                    ByteBuffer plainText = ByteBuffer.wrap(block);
                    for(int j = 0; j < block.length/4; j++) {
                        int fourByte = plainText.getInt();
                        fourByte = fourByte ^ key;
                        unwrapEncrypt.putInt(fourByte);
                    }
                    byte[] encryptedBlock = unwrapEncrypt.array();
                    // This line authentication (payload with header) VoIPpacket
                    ByteBuffer VoIPpacket = ByteBuffer.allocate(encryptedBlock.length +8);
                    // put the block into the VoIPpacket
                    VoIPpacket.putInt(key);
                    VoIPpacket.putShort(authenticationKey);
                    VoIPpacket.put(encryptedBlock);
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
