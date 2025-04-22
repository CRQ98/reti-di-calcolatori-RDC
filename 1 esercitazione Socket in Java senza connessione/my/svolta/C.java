
//C
import java.io.*;
import java.net.*;

public class C {

  public static void main(String[] args) {
    InetAddress Saddr = null;
    int port = -1;
    // controllo input
    try {
      if (args.length != 2) {
        usage();
      } else {
        Saddr = InetAddress.getByName(args[0]);
        port = Integer.parseInt(args[1]);
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
    // ini
    DatagramPacket dp = null;
    DatagramSocket ds = null;
    byte[] bt = new byte[256];
    try {
      ds = new DatagramSocket();
      System.out.println("Creata la socket: " + ds);
      ds.setSoTimeout(30000);
      dp = new DatagramPacket(bt, bt.length, Saddr, port);
      System.out.println("Creato il packet: " + dp);
    } catch (SocketException e) {
      System.out.println("Impossibile creare la socket, Interrompo...");
      e.printStackTrace();
      System.exit(3);
    }

    ByteArrayOutputStream bos = null;
    DataOutputStream dos = null;
    ByteArrayInputStream bis = null;
    DataInputStream dis = null;
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    String line = null, filename = null, richiesta = null;
    int riga = -1;
    String[] content = null;

    System.out.println(
        "Inserire nomeFile e numero di riga che vuoi ottenere separato con ':', o terminare con EOF");

    try {
      // get input
      while ((line = in.readLine()) != null) {
        try {
          content = line.split(":");
          if (content.length != 2) {
            System.out.println("Errore, input non accetabile");
            System.out.println(
                "Inserire nomeFile e numero di riga che vuoi ottenere separato con ':', o terminare con EOF");
            continue;
          }
          // prepare to send
          for (int i = 0; i < content.length; i++) {
            content[i] = content[i].trim();
          }
          filename = content[0];
          riga = Integer.parseInt(content[1]);
          richiesta = filename + " " + riga;
          bos = new ByteArrayOutputStream();
          dos = new DataOutputStream(bos);
          dos.writeUTF(richiesta);
          dp.setData(bos.toByteArray());
          ds.send(dp);
          System.out.println("Richiesta inviata");
        } catch (NumberFormatException nfe) {
          System.out.println("Errore, input non accetabile");
          System.out.println(
              "Inserire nomeFile e numero di riga che vuoi ottenere separato con ':', o terminare con EOF");
          continue;
        } catch (IOException e) {
          System.out.println("Errore nel inviare il socket");
          System.out.println(
              "Inserire nomeFile e numero di riga che vuoi ottenere separato con ':', o terminare con EOF");
          continue;
        }
        // receive
        try {
          // clean buf
          dp.setData(bt);
          ds.receive(dp);
        } catch (IOException e) {
          System.out.println("Errore nel ricevere il socket");
          System.out.println(
              "Inserire nomeFile e numero di riga che vuoi ottenere separato con ':', o terminare con EOF");
          continue;
        }
        // stamp
        try {
          bis = new ByteArrayInputStream(dp.getData());
          dis = new DataInputStream(bis);
          line = dis.readUTF();
          System.out.println("Risposta ricevuto: " + line);
        } catch (IOException e) {
          System.out.println("Errore nel lettura della packet");
          System.out.println(
              "Inserire nomeFile e numero di riga che vuoi ottenere separato con ':', o terminare con EOF");
          continue;
        }
      }
    } catch (IOException e) {
      System.out.println("Errore nel lettura del input");
      System.exit(4);
    }
    System.out.println("Termino...");
    ds.close();
  }

  private static void usage() {
    System.out.println("Usage: java C serverIP serverPort");
    System.exit(1);
  }
}
