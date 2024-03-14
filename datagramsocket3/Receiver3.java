package datagramsocket3;

import CMPC3M06.AudioPlayer;
import uk.ac.uea.cmp.voip.DatagramSocket3;

import java.net.DatagramPacket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Receiver3 {
    static DatagramSocket3 receiving_socket;
    public static void main (String[] args) throws Exception {
        int PORT = 55550;
        try {
            receiving_socket = new DatagramSocket3(PORT);
        } catch (SocketException e){
            System.out.println("ERROR: AudioReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }
        boolean running = true;
//        byte[] buffer = new byte[512];
//        DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
        AudioPlayer player = new AudioPlayer();
        int expectedSequenceNumber = 0;
        byte [] sequence_number_byte = new byte[4];
        byte [] audio_data = new byte[512];
        Map<Integer, byte[]> buff =  new HashMap<>();

        while (running){
            try{
                byte[] buffer = new byte[516];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                receiving_socket.receive(packet);

                ByteBuffer wrappedBuffer = ByteBuffer.wrap(packet.getData());
                int receivedSequenceNumber;

                System.arraycopy(packet.getData(),0,audio_data,0,audio_data.length);
                System.arraycopy(packet.getData(),audio_data.length,sequence_number_byte,0,4);
                receivedSequenceNumber = ByteBuffer.wrap(sequence_number_byte).getInt();

                if (receivedSequenceNumber == expectedSequenceNumber) {
                    System.out.printf("Packet received" + "\n");
                    player.playBlock(audio_data);
                    expectedSequenceNumber++;

                    while (buff.containsKey(expectedSequenceNumber)){
                        audio_data = buff.remove(expectedSequenceNumber);
                        System.out.println("recieved packet with sequence number" + expectedSequenceNumber + "\n");
                        player.playBlock(audio_data);
                        expectedSequenceNumber++;
                    }
                } else {
                    buff.put(receivedSequenceNumber,audio_data);
                    System.out.printf("Packet stored in buff with sequence:  " + receivedSequenceNumber + "\n");
                }
                byte[] audioData = new byte[512];
                wrappedBuffer.get(audioData);

                player.playBlock(audioData);
                expectedSequenceNumber = receivedSequenceNumber + 1;

            } catch (Exception e){
                System.out.println("ERROR: AudioReceiver: Some error occurred!" + "\n");
                e.printStackTrace();
            }
        }
        player.close();
        receiving_socket.close();
    }
}


