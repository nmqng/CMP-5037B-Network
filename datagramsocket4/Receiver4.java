package datagramsocket4;

import CMPC3M06.AudioPlayer;
import uk.ac.uea.cmp.voip.DatagramSocket4;

//import javax.sound.sampled.LineUnavailableException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class Receiver4 {
    static DatagramSocket4 receiving_socket;

    static AudioPlayer player;

    public static void main(String[] args) {

        int PORT = 55555;
        try {
            receiving_socket = new DatagramSocket4(PORT);
            player = new AudioPlayer();
            Set<Integer> receivedPacketNumbers = new HashSet<>();


            boolean running = true;

            while (running) {
                byte[] buffer = new byte[516];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                //Receive Packet
                receiving_socket.receive(packet);

                int packetNumber = packet.hashCode(); // Using packet hashcode as random number
                if (!receivedPacketNumbers.contains(packetNumber)) {
                    receivedPacketNumbers.add(packetNumber);
                    byte[] audioData = packet.getData();
                    player.playBlock(audioData);
                }
            }

        } catch(Exception e){
            System.out.println("ERROR: AudioReceiver: Some error occurred!");
            e.printStackTrace();
        }

        //player.close();
        receiving_socket.close();
    }
}
