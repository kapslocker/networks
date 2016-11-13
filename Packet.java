import java.io.*;

/* This is a packet that is sent 
 * between the sender and the receiver
 *
 * It implements the Serializable interface 
 *
 **/
public class Packet implements Serializable{
  int seqNo;                          // The sequence number.
  int dataSize;                     // The size of data contained in the packet.
  byte[] data;
  public Packet(int seqNo,int dataSize, byte[] data){
    this.seqNo = seqNo;
    this.dataSize = dataSize;
    this.data = data;
  }
}
