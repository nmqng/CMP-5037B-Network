package datagramsocket4;

import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket4;

//import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Random;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;


public class Sender4 {
    static DatagramSocket4 sending_socket;
    static Random random = new Random();

    public static void main(String[] args) throws Exception{

        int PORT = 55555;
        String clientIPAddress = "192.168.0.161";
        InetAddress clientIP = InetAddress.getByName(clientIPAddress);

        try {
            sending_socket = new DatagramSocket4();
        } catch (SocketException e) {
            System.out.println("ERROR: AudioSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        AudioRecorder recorder = new AudioRecorder();
        int recordTime = Integer.MAX_VALUE;
        // Main loop
        boolean running = true;
        while (running) {
            try {
                System.out.println("Recording Audio...");
                byte[] buffer = new byte[516];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);
                for (int i = 0; i < Math.ceil(recordTime / 0.032); i++) {
                    byte[] block = recorder.getBlock();
                    int randomNum = random.nextInt(1000);
                    packet.setData(block);
                    sending_socket.send(packet);
                }
                running = false;
            } catch(Exception e){
                System.out.println("ERROR: AudioSender: Some error occurred!");
                e.printStackTrace();
            }
        }
        sending_socket.close();
    }
}
