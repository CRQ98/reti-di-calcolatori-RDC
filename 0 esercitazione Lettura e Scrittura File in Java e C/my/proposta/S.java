import java.io.*;

public class S {

  public static void main(String args[]) {
    BufferedReader br;
    FileWriter fw;
    String line, filename = null;
    //controllo argomento
    if (args.length != 1) usage(); else filename = args[0];
    int nline = 0;

    br = new BufferedReader(new InputStreamReader(System.in));
    try {
        fw = new FileWriter(filename);
      System.out.println("Adesso puoi iniziare a scrivere nel " + filename);
      System.out.println(
        "Inizi a inserire la riga, termino fino alla lettura di EOF (CTRL+D)"
      );
      while ((line = br.readLine()) != null) {
        fw.write(line+'\n');
      }
      fw.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      System.exit(2);
    }
    System.out.println("Termino...");
  }

  private static void usage() {
    System.out.println("Usage: S <filename>");
    System.exit(0);
  }
}
