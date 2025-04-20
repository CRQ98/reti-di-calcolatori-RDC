import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MultiC {

  public static void main(String[] args) {
    InetAddress addr = null;
    int port = -1;
    //controllo input
    try {
      if (args.length != 2) usage(); else {
        addr = InetAddress.getByName(args[0]);
        port = Integer.parseInt(args[1]);
      }
      if (port >= 1024 && port <= 65535) {} else {
        System.out.println("Port non valido");
        usage();
      }
    } catch (UnknownHostException e1) {
      System.out.println("Impossibile determinare ServerIP, Interrompo...");
      e1.printStackTrace();
      System.exit(2);
    } catch (NumberFormatException e2) {
      System.out.println("Numero di Port non valido, Interrompo...");
      e2.printStackTrace();
      System.exit(2);
    }
    //prepare socket
    DatagramPacket dp = null;
    MulticastSocket ms = null;
    byte[] bt = new byte[256];
    //creation socket packet
    try {
      ms = new MulticastSocket(port);
      System.out.println("Creata la socket: " + ms);
      ms.setSoTimeout(20000);
      dp = new DatagramPacket(bt, bt.length);

      System.out.println("Creato il packet: " + dp);
      //InetAddress netInterface = InetAddress.getByName("localhost");
      //ms.setInterface(netInterface);
      ms.joinGroup(addr);
    } catch (SocketException e) {
      System.out.println("Impossibile creare la socket, Interrompo...");
      e.printStackTrace();
      System.exit(3);
    } catch (IOException ioe) {
      System.out.println(
        "Impossibile fare creare la socket o entrare nel gruppo, Interrompo..."
      );
      ioe.printStackTrace();
      System.exit(3);
    }
    //ini dataflow
    ByteArrayInputStream bis = null;
    DataInputStream dis = null;
    String msg = null;

    System.out.println("Waiting for server msg");
    int count = 0;
    try {
      //count till 20, reveive 20 times msg from server
      while (count++ < 20) {
        try {
          dp.setData(bt);
          ms.receive(dp);
          bis = new ByteArrayInputStream(dp.getData());
          dis = new DataInputStream(bis);
          msg = dis.readUTF();
        } catch (IOException ioe) {
          System.out.println(
            "Errore nel receive packet or read data from packet"
          );
          ioe.printStackTrace();
          continue;
        }
        System.out.println("Msg: " + msg);
      }
    } catch (Exception e) {
      System.out.println("Other error detected");
      e.printStackTrace();
      System.exit(4);
    }

    try {
      ms.leaveGroup(addr);
    } catch (IOException e) {
      System.out.println("Cannot leave from group");
      e.printStackTrace();
    }
    System.out.println("Termino...");
    ms.close();
  }

  private static void usage() {
    System.out.println(
      "Usage: java C MulticastIP(224.0.0.0 ~ 239.255.255.255) Port"
    );
    System.exit(1);
  }
}
