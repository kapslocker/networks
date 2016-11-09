import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.util.Random;
public class sender{
  static final int DATA_LENGTH = 100000;             // 'flow' tat needs to be sent across.
  static final int MSS = 1000;                       // Maximum size of data, EXCLUDING headers.
  static final int W = 1*MSS;                                // Window size.
  static byte[] data = new byte[DATA_LENGTH];
  static InetAddress receiverAddress;
  static int receiverPort ;
  static DatagramSocket senderSocket;
  static int senderPort = 4357;           //TODO: check if this needs to be done.
  static long initTime;
/*
* Generates a Packet to send over the network.
* The packet contains bytes from seq# upto seq# + dataSize.
*/
  private static DatagramPacket getPacket(int seqNo,int dataSize) throws Exception{
    int start = seqNo;int end = start + dataSize - 1;
    byte[] packetData = new byte[dataSize];
    for(int i=0;i<dataSize;i++){
      packetData[i] = data[start + i];
    }
    Packet packet = new Packet(seqNo,dataSize,packetData);

    ByteArrayOutputStream opStream = new ByteArrayOutputStream();
    ObjectOutput objectOutput = new ObjectOutputStream(opStream);
    objectOutput.writeObject(packet);
    objectOutput.close();
    byte[] data = opStream.toByteArray();
    DatagramPacket sendPacket = new DatagramPacket(data, data.length,receiverAddress,receiverPort);

    return sendPacket;
  }

  private static void sendPackets(int receivedAck) throws Exception{
    int temp = 0;
    while(temp<W){
      int size = min(MSS,W-temp);
      DatagramPacket packet = getPacket(receivedAck+temp,size);
      long currTime = System.nanoTime();
      printValues(receivedAck + temp,currTime,0);                    // print seqNo
      temp = temp + size;
      senderSocket.send(packet);
    }
  }
  public static void main(String[] args)throws Exception {
    receiverAddress = InetAddress.getByName(args[0]);
    receiverPort = Integer.parseInt(args[1]);
    new Random().nextBytes(data);                        // initialize random data to the data array.

    senderSocket = new DatagramSocket(senderPort);
    initTime = System.nanoTime();
    int receivedAck = 0;

    // send first packet here.
    sendPackets(0);

    while(receivedAck<DATA_LENGTH){
      byte[] receivedData = new byte[1024];
      DatagramPacket receivedPacket = new DatagramPacket(receivedData,receivedData.length);
      senderSocket.receive(receivedPacket);
      String str =  new String(receivedPacket.getData());
      int ack = Integer.parseInt(str.trim());
      printValues(ack,System.nanoTime(),1);
      receivedAck = ack;
      sendPackets(receivedAck);
    }
  }

  private static void printValues(int number, long currTime,int flag){
    if(flag == 0){
      System.out.println("Seq#: " + String.valueOf(number) + "\t"+"Time elapsed: " + String.valueOf( (double)(currTime - initTime)/1000000000 ) + " s");
    }
    else{
      System.out.println("Ack#: " + String.valueOf(number) + "\t"+"Time elapsed: " + String.valueOf( (double)(currTime - initTime)/1000000000) + " s");
    }
  }
  private static int min(int a,int b){
    return a>b?b:a;
  }
}
