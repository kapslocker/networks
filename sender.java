import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
public class sender{

  private static class Node{
    int dataSize;
    int seqNo;
    long T;
    public Node(int s,int d,long t){
      dataSize = d;
      seqNo = s;
      T = t;
    }
  }

  static final int DATA_LENGTH = 100000;             // 'flow' that needs to be sent across.
  static final int MSS = 1000;                       // Maximum size of data, EXCLUDING headers.
  static int W = 1*MSS;                                // Window size.
  static byte[] data = new byte[DATA_LENGTH];
  static InetAddress receiverAddress;
  static int receiverPort ;
  static DatagramSocket senderSocket;
  static DatagramSocket senderReceiveSocket;
  static int senderPort = 1123;           //TODO: check if this needs to be done.
  static int senderReceivePort = 1124;
  static long initTime;
  static int receivedAck = 0;
  static LinkedList<Node> q;
  static int LOSS_FLAG = 0;
/*
* Generates a Packet to send over the network.
* The packet contains bytes from seq# upto seq# + dataSize.
*/
  private static DatagramPacket getPacket(int seqNo,int dataSize) throws Exception{
    int start = seqNo;int end = start + dataSize - 1;
    byte[] packetData = new byte[dataSize];
    for(int i=0; i<dataSize; i++){
      if(start + i >= DATA_LENGTH){
        break;
      }
      packetData[i] = data[start + i];
    }
    Packet packet = new Packet(seqNo,dataSize,packetData);

    ByteArrayOutputStream opStream = new ByteArrayOutputStream();
    ObjectOutput objectOutput = new ObjectOutputStream(opStream);
    objectOutput.writeObject(packet);
    objectOutput.close();
    byte[] data = opStream.toByteArray();
    DatagramPacket sendPacket = new DatagramPacket(data, data.length, receiverAddress, receiverPort);

    return sendPacket;
  }
  public static void main(String[] args) throws Exception {
    receiverAddress = InetAddress.getByName(args[0]);
    receiverPort = Integer.parseInt(args[1]);
    new Random().nextBytes(data);                        // initialize random data to the data array.
    q = new LinkedList<Node>();
    if(args.length >=3){
      int t = Integer.parseInt(args[2]);
      LOSS_FLAG = t;
    }
    initTime = System.nanoTime();
    senderSocket = new DatagramSocket(senderPort);
    senderReceiveSocket = new DatagramSocket(senderReceivePort);


    SendThread s = new SendThread();
    s.start();
    ReceiveThread r = new ReceiveThread();
    r.start();
  }

  private static class SendThread extends Thread{
    public void run(){
      try{
        int currSent = 0;                                                         // i.e. I've sent upto this point.
        while(receivedAck < DATA_LENGTH){
          int limit = min(receivedAck + W,DATA_LENGTH);                                            // This is the max I'm allowed to send.

          while(currSent < limit){                                                // Send all packets I'm allowed to send, until window closes.

            int size = min(MSS,limit - currSent);
            if(currSent < DATA_LENGTH){
              DatagramPacket packet = getPacket(currSent,size);
              long currTime = System.nanoTime();
              if(LOSS_FLAG > 0)
              {
                if(ThreadLocalRandom.current().nextInt(1,21) != 1)
                    senderSocket.send(packet);
              }
              else
              {
                  senderSocket.send(packet);
              }
              printValues(currSent,currTime,0);                    // print seqNo
              q.addFirst(new Node(currSent,size,currTime));                           // put this packet in queue for timer check.
              currSent = currSent + size;
            }
          }
          if(q.size() > 0){                                                    // Check for lost packets.
            Node node = q.getLast();
            if(node != null  && System.nanoTime() - node.T > 1000000000){
              W = MSS;                                                             // drop Window size
              currSent = receivedAck;
              q.clear();
            }
          }
        }
      }
      catch(Exception e){
        System.out.println("Exception: " + e.getMessage());
      }
    }
    public void start(){
      Thread thread;
      thread = new Thread(this);
      thread.start();
    }
  }



  private static class ReceiveThread extends Thread{
    public void run(){
      try{
        while(receivedAck < DATA_LENGTH){
          byte[] receivedData = new byte[10];
          DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
          senderReceiveSocket.receive(receivedPacket);
          String str =  new String(receivedPacket.getData());
          int ack = Integer.parseInt(str.trim());
          printValues(ack, System.nanoTime(), 1);
          receivedAck = ack;
          W = W + (MSS*MSS)/W;
          if(q.size()>0)
          {
            Node n = q.getLast();
            while(n!=null && n.seqNo + n.dataSize - 1 < ack){
              q.removeLast();
              if(q.size()>0)
                n = q.getLast();
              else
                n = null;
            }
          }
        }
      } catch(Exception e)
      {
        System.out.println("Exception:" + e.getLocalizedMessage());
      }
    }
    public void start(){
      Thread thread;
      thread = new Thread(this);
      thread.start();
    }

  }

  private static void printValues(int number, long currTime, int flag){
    if(flag == 0) {
      System.out.println("Seq#: " + String.valueOf(number) + "\t"+"Time elapsed: " + String.valueOf( (double)(currTime - initTime)/1000000000 ) + " s");
    }
    else {
      System.out.println("Ack#: " + String.valueOf(number) + "\t"+"Time elapsed: " + String.valueOf( (double)(currTime - initTime)/1000000000) + " s");
    }
  }
  private static int min(int a,int b){
    return a>b?b:a;
  }
}
