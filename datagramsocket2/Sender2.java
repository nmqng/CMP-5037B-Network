package datagramsocket2;
import java.util.Vector;
import java.net.*;

//Uncomment the following if using JAR package:
import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket2;

public class Sender2 {
    static DatagramSocket2 sending_socket;

    public static void main(String[] args) throws Exception{
        int PORT = 55555;
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("localhost");
        } catch (UnknownHostException e){
            System.out.println("ERROR: AudioSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }

        try{
            sending_socket = new DatagramSocket2();
        } catch (SocketException e){
            System.out.println("ERROR: AudioSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        //Initialise AudioPlayer and AudioRecorder objects
        AudioRecorder recorder = new AudioRecorder();
        int recordTime = Integer.MAX_VALUE;

        // Main loop
        boolean running = true;
        while (running){
            try {
                System.out.println("Recording Audio...");
                byte[] buffer = new byte[512];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);
                for (int i = 0; i < Math.ceil(recordTime / 0.032); i++) {
                    byte[] block = recorder.getBlock();
                    packet.setData(block);
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
