import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
public class receiver{
  static int x = 0;                                    // cumulative ACK#
  static int receiverPort = 4358;
  static DatagramSocket receiverSocket;
  static int senderPort;
  static int senderReceivePort = 1124;
  static InetAddress senderAddress;

  private static Packet extractPacketInfo(ByteArrayInputStream receivedBytes) throws Exception{
    ObjectInputStream iStream = new ObjectInputStream(receivedBytes);
    Packet packet = (Packet) iStream.readObject();
    iStream.close();
    return packet;
  }

  public static void main(String[] args) throws Exception {
    if(args.length>0)
      receiverPort = Integer.parseInt(args[0]);
    receiverSocket = new DatagramSocket(receiverPort);
    byte[] receivedData = new byte[10000];
    DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
    while(x < 99999){
      receiverSocket.receive(receivePacket);
      receivedData = receivePacket.getData();
      senderPort = receivePacket.getPort();
      senderAddress = receivePacket.getAddress();

      Packet rcv = extractPacketInfo(new ByteArrayInputStream(receivedData));

      if(x == rcv.seqNo){
        int temp = rcv.seqNo + rcv.dataSize;
        x = temp;
      }
      String str = String.valueOf(x);
      byte[] ackBytes = str.getBytes();
      DatagramPacket packetToSend = new DatagramPacket(ackBytes, ackBytes.length, senderAddress, senderReceivePort);
      receiverSocket.send(packetToSend);                          //TODO: Debug if the right ack is being sent.
      printValues(x);
    }
  }
  private static void printValues(int number){
      System.out.println("Ack#: " + String.valueOf(number) );
  }
}
