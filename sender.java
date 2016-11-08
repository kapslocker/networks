import java.io.*;
import java.net.*;
import java.util.Random;
public class sender{
  static final int DATA_LENGTH = 100000;             // 'flow' tat needs to be sent across.
  static final int MSS = 1000;                       // Maximum size of data, EXCLUDING headers.
  static final int W = 1*MSS;                                // Window size.
  static byte[] data = new byte[DATA_LENGTH];
/*
* Generates a Packet to send over the network.
* The packet contains bytes from seq# upto seq# + dataSize.
*/
  private static Packet getPacket(int seqNo,int dataSize) {
    int start = seqNo;int end = start + dataSize - 1;
    byte[] packetData = new byte[dataSize];
    for(int i=0;i<dataSize;i++){
      packetData[i] = data[start + i];
    }
    Packet packet = new Packet(seqNo,dataSize,packetData);
    return packet;
  }
  static InetAddress receiverAddress;
  static int receiverPort ;
  static DatagramSocket senderSocket;
  static int senderPort = 4357;           //TODO: check if this needs to be done.

  public static void main(String[] args)throws Exception {
    receiverAddress = InetAddress.getByName(args[0]);
    receiverPort = Integer.parseInt(args[1]);
    new Random().nextBytes(data);                        // initialize random data to the data array.

    senderSocket = new DatagramSocket(senderPort);

  }
}
