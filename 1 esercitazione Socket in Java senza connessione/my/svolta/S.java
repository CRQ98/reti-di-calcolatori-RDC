//S
import java.io.*;
import java.net.*;

public class S {

  //port range 1024-65535
  // dichiarata come statica perch� caratterizza il server
  private static final int PORT = 4445;

  public static void main(String[] args) {
    DatagramSocket ds = null;
    DatagramPacket dp = null;
    //used to set packet buf
    byte[] buf = new byte[256];
    int port = -1;

    if (args.length == 0) port = PORT; else if (args.length == 1) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException nfe) {
        System.out.println("Port non valido");
        usage();
      }

      if (port >= 1024 || port <= 65535) {} else {
        System.out.println("Port non valido");
        usage();
      }
    } else usage();
    System.out.println("Server avviato");
    try {
      //creation socket, rem here could generate socket exception
      ds = new DatagramSocket(port);
      System.out.println("Creato socket: " + ds + " in port: " + port);

      //Constructs a DatagramPacket for receiving packets
      dp = new DatagramPacket(buf, buf.length);
    } catch (SocketException se) {
      System.out.println("Creazione socket fallito");
      se.printStackTrace();
      System.exit(2);
    }
    String filename = null, line = null, req = null, res = null;
    String[] tokens;
    DataOutputStream dos = null;
    DataInputStream dis = null;
    ByteArrayOutputStream bos = null;
    ByteArrayInputStream bis = null;
    //used to store responce in bytearray
    byte[] data = new byte[256];
    int nline = -1;

    //run forever
    try {
      while (true) {
        System.out.println("In attesa di richieste...");
        //clean data
        dp.setData(buf);
        try {
          ds.receive(dp);
        } catch (IOException ioe) {
          System.out.println("Receive packet problem");
          ioe.printStackTrace();
          continue;
        }

        //received packet now estabilish data flow
        bis = new ByteArrayInputStream(dp.getData());
        dis = new DataInputStream(bis);

        try {
          req = dis.readUTF();
          System.out.println(req);
          tokens = req.split(" ");
          System.out.println(
            "REQ: filename-> " + tokens[0] + " line-> " + tokens[1]
          );
          //set params
          filename = tokens[0];
          nline = Integer.parseInt(tokens[1]);
        } catch (IOException ioe) {
          System.out.println("Cannot read form packet");
          ioe.printStackTrace();
          continue;
        } catch (NumberFormatException nfe) {
          System.out.println("Cannot invert number of line");
          nfe.printStackTrace();
          continue;
        }

        line = LineUtility.getLine(filename, nline);
        //prepare output to send to client
        res = line;
        bos = new ByteArrayOutputStream();
        dos = new DataOutputStream(bos);

        try {
          dos.writeUTF(res);
        } catch (IOException ioe) {
          System.out.println("Cannot write responce");
          ioe.printStackTrace();
          continue;
        }
        data = bos.toByteArray();
        dp.setData(data);
        try {
          ds.send(dp);
        } catch (IOException ioe) {
          System.out.println("Cannot send packet");
          ioe.printStackTrace();
          continue;
        }
      } //while
    } catch (Exception e) { //other exception
      System.out.println("Other exception detected");
      e.printStackTrace();
    }

    System.out.println("Termino...");
    ds.close();
  }

  private static void usage() {
    System.out.println(
      "Usage: java S <serverPort>(port range 1024-65535) OR java S"
    );
    System.exit(1);
  }
}
