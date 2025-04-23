import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MultiS {

  static final int PORT = 1234;
  static final String FILE = "test";

  public static void main(String[] args) {
    MulticastSocket ms = null;
    DatagramPacket dp = null;
    //used to set packet buf
    byte[] buf = new byte[256];
    String porta = null, address = "230.0.0.1";//by default
    int port = -1;
    InetAddress addr = null;
    if (args.length == 1) {
      porta = args[0];
    } else if (args.length == 2) {
      address = args[0];
      porta = args[1];
    } else usage();
    try {
      port = Integer.parseInt(porta);
      addr = InetAddress.getByName(address);
    } catch (NumberFormatException nfe) {
      System.out.println("Cannot determine the address");
      nfe.printStackTrace();
      usage();
    } catch (UnknownHostException uhe) {
      System.out.println("Cannot convert port");
      uhe.printStackTrace();
      usage();
    }
    if (port >= 1024 && port <= 65535) {} else {
      System.out.println("Port non valido");
      usage();
    }
    System.out.println("Server avviato");
    try {
      //creation multicastsocket, rem here could generate IO exception
      ms = new MulticastSocket(PORT);
      System.out.println("Creato socket con IP: " + addr.getHostName() + " in port: " + PORT);

      //Constructs a DatagramPacket for send
      dp = new DatagramPacket(buf, buf.length, addr, port);
    } catch (IOException ioe) {
      System.out.println("Creazione socket fallito");
      ioe.printStackTrace();
      System.exit(2);
    }

    String filename = FILE, line = null;
    DataOutputStream dos = null;
    ByteArrayOutputStream bos = null;
    byte[] data = new byte[256];
    BufferedReader in = null;
    boolean hasnext = true;
    //run forever
    try {
      while (true) {
        in = new BufferedReader(new FileReader(filename));
        hasnext=true;
        while (hasnext) {
          line = LineUtility.getNextLine(in);
          if (line.equals("Nessuna linea disponibile")) {
            hasnext = false;
          } else {
            System.out.println("Linea letta: "+line);
            try {
              //clean
              dp.setData(buf);
              bos = new ByteArrayOutputStream();
              dos = new DataOutputStream(bos);
              dos.writeUTF(line);
              data = bos.toByteArray();
              dp.setData(data);
              ms.send(dp);
            System.out.println("Msg inviata");

            } catch (IOException ioe) {
              System.out.println("Write or send packet problem");
              ioe.printStackTrace();
              continue;
            }
          }
          Thread.sleep(2000);
        } //while hasnext
      } //while true
    } catch (Exception e) { //other exception
      System.out.println("Other exception detected");
      e.printStackTrace();
    }
    try {
      ms.leaveGroup(addr);
    } catch (IOException e) {
      System.out.println("Cannot leave the group");
      e.printStackTrace();
    }
    System.out.println("Termino...");
    ms.close();
  }

  private static void usage() {
    System.out.println(
      "Usage: \njava S <MulticatIP> (224.0.0.0~239.255.255.255) <destPort> (port range 1024-65535) \nOR\njava S <destPort>"
    );
    System.exit(1);
  }
}
