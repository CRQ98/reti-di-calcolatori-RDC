//RowSwap server

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RowSwap implements Runnable {

  private String filename;
  private int port;

  public RowSwap(String filename, int port) {
    this.filename = filename;
    this.port = port;
    System.out.println(
      "RowSwap: filename <" + filename + "> port <" + port + ">"
    );
  }

  public void run() {
    if (!new File(filename).exists()) {
      System.err.println("RowSwap: doesn't exist this file");
      System.exit(1);
    }
    DatagramSocket ds = null;
    DatagramPacket dp = null;
    byte[] buf = new byte[256];
    byte[] data = new byte[256];

    String res = "OK", req = null;
    String row1 = null, row2 = null;
    int nrow1 = -1, nrow2 = -1;
    String[] strs = null;

    try {
      ds = new DatagramSocket(port);
      dp = new DatagramPacket(buf, buf.length);
    } catch (SocketException se) {
      System.err.println("Error nella creazione socket");
      se.printStackTrace();
      System.exit(2);
    }
    try {
      while (true) {
        dp.setData(buf);
        System.out.println("RowSwap: waiting UDP");
        try {
          ds.receive(dp);
        } catch (IOException ioe) {
          System.err.println("RowSwap: Error di ricevere packet");
          ioe.printStackTrace();
          continue;
        }
        req = My.BDInputStream(dp.getData());
        strs = req.split("-");
        //i controlli fatti in client
        try {
          nrow1 = Integer.parseInt(strs[0]);
          nrow2 = Integer.parseInt(strs[1]);
        } catch (NumberFormatException nfe) {
          System.err.println("Error convert number");
          nfe.printStackTrace();
          System.exit(3);
        }

        System.out.println(
          "RowSwap: REQ: swap line <" + nrow1 + "> <" + nrow2 + ">"
        );
        row1 = My.getLine(filename, nrow1);
        row2 = My.getLine(filename, nrow2);
        if (row1 == null || row2 == null) {
          System.err.println("RowSwap: " + row1 + " | " + row2);
          res = "NO";
        } else {
          BufferedReader in = null;
          BufferedWriter out = null;
          try {
            in = new BufferedReader(new FileReader(filename));
            out = new BufferedWriter(new FileWriter(filename + "_temp"));
          } catch (FileNotFoundException fnfe) {
            System.err.println("RowSwap: Error non trova file " + filename);
            fnfe.printStackTrace();
            res = "NO";
          } catch (IOException ioe) {
            System.err.println("RowSwap: Error scrittura su file");
            ioe.printStackTrace();
            res = "NO";
          }

          String line = null;
          int count = 0;
          try {
            while ((line = My.getNextLine(in)) != null) {
              count++;
              if (count == nrow1) {
                line = row2;
              } else if (count == nrow2) {
                line = row1;
              }
              out.write(line + "\n");
            }
            in.close();
            out.close();
          } catch (IOException ioe) {
            System.err.println(
              "Error scrittura nel file temp o non si chiude i file"
            );
            ioe.printStackTrace();
            res = "NO";
          }

          new File(filename).delete();
          new File(filename + "_temp").renameTo(new File(filename));
          new File(filename + "_temp").delete();
        }

        dp.setData(My.DBOutputStream(res));
        try {
          ds.send(dp);
        } catch (IOException ioe) {
          System.err.println("RowSwap: Error di send");
          ioe.printStackTrace();
        }
      } //while
    } catch (Exception e) {
      System.err.println("RowSwap: Other Error ");
      e.printStackTrace();
    }
    System.out.println("RowSwap: Termino...");
    ds.close();
  }
}
