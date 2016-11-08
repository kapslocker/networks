import java.io.*;
import java.net.*;

public class receiver{
  static int x = 0;                                    // cumulative ACK#
  static int receiverPort;
  static DatagramSocket receiverSocket;
  static int senderPort;
  static InetAddress senderAddress;

  private static Packet extractPacketInfo(ByteArrayInputStream receivedBytes) throws Exception{
    ObjectInputStream iStream = new ObjectInputStream(receivedBytes);
    Packet packet = (Packet) iStream.readObject();
    iStream.close();
    return packet;
  }
  public static void main(String[] args) throws Exception {
    receiverPort = Integer.parseInt(args[0]);
    receiverSocket = new DatagramSocket(receiverPort);

    byte[] receivedData = new byte[10000];
    DatagramPacket receivePacket = new DatagramPacket(receivedData,receivedData.length);
    receiverSocket.receive(receivePacket);

    receivedData = receivePacket.getData();
    senderPort = receivePacket.getPort();
    senderAddress = receivePacket.getAddress();

    Packet rcv = extractPacketInfo(new ByteArrayInputStream(receivedData));
    x = rcv.seqNo;
    byte[] dataToSend = new byte[1];
    dataToSend[0] = (byte) x;

    DatagramPacket packetToSend = new DatagramPacket(dataToSend,dataToSend.length,senderAddress,senderPort);
    receiverSocket.send(packetToSend);                          //TODO: Debug if the right ack is being sent.
  }
}
