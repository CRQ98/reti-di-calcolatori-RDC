//DiscoveryServer
import java.io.*;
import java.net.*;
import java.util.*;

public class S {

  private static int PORT = -1;

  public static void main(String[] args) {
    //controll input
    if (((args.length - 1) % 2) != 0 || args.length < 3) usage();
    Map<String, Integer> map = new HashMap<>();
    int nargs = (args.length - 1) / 2;
    int port = -1;

    for (int i = 1; i <args.length; i=i+2) {
      try {
        port = Integer.parseInt(args[i  + 1]);
      } catch (NumberFormatException nfe) {
        System.err.println("port: " + args[i  + 1] + " cannot be converted");
        usage();
      }

      if (My.checkPort(port)) {
        if (new File(args[i]).exists()) {
          if (map.containsKey(args[i]) || map.containsValue(port)) {
            System.out.println("The data is repeated");
            usage();
          } else {
            map.put(args[i], port);
          }
        } else {
          System.out.println("This file " + args[i] + " doesn't exists");
          usage();
        }
      } else {
        System.out.println("This port cannot be used");
        usage();
      }
    }
    System.out.println("S avviato");
    System.out.println(map);

    //prepare thread
    map.forEach((k, v) -> new Thread(new RowSwap(k, v)).start());

    DatagramSocket ds = null;
    DatagramPacket dp = null;
    byte[] buf = new byte[256];
    byte[] data = null;
    String req = null;
    /* 
    ByteArrayInputStream bis = null;
    DataInputStream dis = null;
    ByteArrayOutputStream bos = null;
    DataOutputStream dos = null;
    */
    String res = null;

    try {
      PORT = Integer.parseInt(args[0]);
      ds = new DatagramSocket(PORT);
      //for receive
      dp = new DatagramPacket(buf, buf.length);
    } catch (SocketException se) {
      System.err.println("Error nella creazione socket");
      se.printStackTrace();
      System.exit(2);
    } catch (NumberFormatException nfe) {
      System.err.println("Error convert number");
      nfe.printStackTrace();
      System.exit(2);
    }
    try {
      while (true) {
        try {
          //data = new byte[256];
          dp.setData(buf);
          System.out.println("S: Waiting for UDP");
          ds.receive(dp);
          /* 
          data = dp.getData();
          bis = new ByteArrayInputStream(data);
          dis = new DataInputStream(bis);
          req = dis.readUTF();
          */
          req = My.BDInputStream(dp.getData());
          System.out.println("S: REQ: " + req);
        } catch (IOException ioe) {
          System.err.println("Error nel ricezione or lettura packet");
          ioe.printStackTrace();
          continue;
        }
        if (map.containsKey(req)) res = "" + map.get(req); else res = "-1"; //no file
        System.out.println("S: RES: " + res);
        /* 
        bos = new ByteArrayOutputStream();
        dos = new DataOutputStream(bos);
        */
        try {
          /* 
          dos.writeUTF(res);
          data = bos.toByteArray();
          */
          data = My.DBOutputStream(res);
          dp.setData(data);
          ds.send(dp);
        } catch (IOException ioe) {
          System.err.println(
            "Error nel send packet or nella scrittura nel dataflow"
          );
          ioe.printStackTrace();
          continue;
        }
      } //while true
    } catch (Exception e) {
      System.err.println("Other Error ");
      e.printStackTrace();
    }
    System.out.println("Termino...");
    ds.close();
  }

  private static void usage() {
    System.out.println(
      "Usage: java S <S_Port> <filename1> <port1> <filename2> <port2> ..."
    );
    System.exit(1);
  }
}
