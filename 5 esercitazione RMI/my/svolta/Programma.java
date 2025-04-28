import java.io.Serializable;

class Programma implements Serializable {
    private static final int ROW = 12;
    private static final int COL = 5;

    public String[][] speaker = new String[ROW][COL];

    public Programma() {
        for (int i = 0; i < ROW; i++)
            for (int j = 0; j < COL; j++)
                speaker[i][j] = "";
    }

    public synchronized int register(int section, String name) {
        System.out.println("Programma: registrazione di " + name + " per la sessione " + section);
        for (int i = 0; i < COL; i++)
            if (speaker[section - 1][i].equals("")) {
                speaker[section - 1][i] = name;
                return 0;
            }
        return 1;
    }

    public void print(Programma p) {
        System.out.print("Sessione");
        for (int i = 0; i < COL; i++)
            System.out.print("\tIntervento " + i);
        System.out.print("\n");
        
        for (int i = 0; i < ROW; i++) {
            System.out.print("S" + (i + 1));
            for (int j = 0; j < COL; j++)
                System.out.print("\t" + speaker[i][j]);
            System.out.print("\n");
        }

    }
}