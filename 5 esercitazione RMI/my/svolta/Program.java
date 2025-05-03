import java.io.Serializable;

public class Program implements Serializable {
    private static final int ROW = 12;
    private static final int COL = 5;

    public String[][] speaker = new String[ROW][COL];

    public Program() {
        for (int i = 0; i < ROW; i++)
            for (int j = 0; j < COL; j++)
                speaker[i][j] = "";
    }

    public synchronized int register(int session, String name) {
        System.out.println("Program: registrazione di " + name + " per la sessione " + session);
        for (int i = 0; i < COL; i++)
            if (speaker[session][i].equals("")) {
                speaker[session][i] = name;
                return 0;
            }
        return 1;
    }

    public void print() {
        System.out.print("Session");
        for (int i = 0; i < COL; i++)
            System.out.print("\tIntervento " + i);
        System.out.print("\n\n");
        
        for (int i = 0; i < ROW; i++) {
            System.out.print("S" + (i + 1));
            for (int j = 0; j < COL; j++)
                System.out.print("\t" + speaker[i][j]);
            System.out.print("\n");
        }

    }   
}