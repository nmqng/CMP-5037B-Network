package datagramsocket4;

import CMPC3M06.AudioPlayer;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import java.net.DatagramPacket;
import java.net.SocketException;

public class Receiver4 {
    static DatagramSocket4 receiving_socket;
    public static void main (String[] args) throws Exception {
        int PORT = 55555;
        try {
            receiving_socket = new DatagramSocket4(PORT);
        } catch (SocketException e){
            System.out.println("ERROR: AudioReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }
        boolean running = true;
        byte[] buffer = new byte[512];
        DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
        AudioPlayer player = new AudioPlayer();

        while (running){
            try{
                receiving_socket.receive(packet);
                player.playBlock(buffer);
            } catch (Exception e){
                System.out.println("ERROR: AudioReceiver: Some error occurred!");
                e.printStackTrace();
            }
        }
        player.close();
        receiving_socket.close();
    }
}


