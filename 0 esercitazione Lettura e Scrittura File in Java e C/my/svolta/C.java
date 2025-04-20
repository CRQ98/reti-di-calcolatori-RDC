import java.io.*;

public class C {

  public static void main(String[] args) {
    FileReader fr = null;
    String filename = null;
    int n;
    //controllo argomento
    if (args.length != 1) usage(); else filename = args[0];

    try {
      fr = new FileReader(filename);
      while ((n = fr.read()) >= 0) {
        //-1 means EOF
        System.out.print((char) n);
      }
      fr.close();
    } catch (FileNotFoundException fne) {
      fne.printStackTrace();
      System.exit(1);
    } catch (IOException ioe) {
      ioe.printStackTrace();
      System.exit(2);
    }
  }

  private static void usage() {
    System.out.println("Usage: C <filename>");
    System.exit(0);
  }
}
