import java.io.*;

public class P {

  public static void main(String args[]) {
    BufferedReader br;
    FileWriter fw;
    String line,filename=null;
    //controllo argomento
    if (args.length != 1)
      usage();
      else 
      filename=args[0];
    int nline = 0;
    
    br = new BufferedReader(new InputStreamReader(System.in));
    try {
        //controllo del parametro inserito
        while (nline <= 0) {
        System.out.println("Quante righe vuoi inserire?");
        line = br.readLine();
        nline = Integer.parseInt(line);
        if (nline <= 0) System.out.println("Numero inserito non valido");
      }
      fw = new FileWriter(filename);
        System.out.println("Adesso puoi iniziare a scrivere nel "+filename);

      for (int i = 0; i < nline; i++) {
        System.out.println("Inserisci la riga");
        fw.write(br.readLine()+"\n");
      }
      fw.close();
    } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      System.exit(1);
    } catch (IOException ioe) {
        ioe.printStackTrace();
        System.exit(2);
    }
        System.out.println("Termino...");
    
  }

  private static void usage() {
    System.out.println("Usage: P <filename>");
    System.exit(0);
  }
}
