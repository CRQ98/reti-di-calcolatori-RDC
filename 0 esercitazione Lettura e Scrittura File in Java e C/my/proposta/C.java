import java.io.*;

public class C {

  public static void main(String[] args) {
    BufferedReader br = null;
    String filename = null, prefix = null;
    int n;
    char c;
    boolean same = false;
    //controllo argomento
    if (args.length != 1 && args.length != 2) usage(); else prefix = args[0];
    try {
      if (args.length == 2) {
        filename = args[1];
        br = new BufferedReader(new FileReader(filename));
        System.out.println("Apertura file riuscito");

      } else {
        br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Inizi a inserire");
      }
      //read fino a -1 means EOF
      while ((n = br.read()) >= 0) {
        c = (char) n;
        same = false;
        for (int i = 0; i < prefix.length(); i++) {
          if (prefix.charAt(i) == c) same = true;
        }
        if (!same) System.out.print(c);
      }
      System.out.println("\nTermino...");

      br.close();
    } catch (FileNotFoundException fne) {
      fne.printStackTrace();
      System.exit(1);
    } catch (IOException ioe) {
      ioe.printStackTrace();
      System.exit(2);
    }
  }

  private static void usage() {
    System.out.println(
      "Usage: C <filterprefix> <filename> or C <filterprefix>"
    );
    System.exit(0);
  }
}
