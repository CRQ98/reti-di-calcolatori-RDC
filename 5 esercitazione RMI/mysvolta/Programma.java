import java.io.Serializable;

/**
 * Programma.java Serializable -> deve essere restituita da un metodo remoto.
 * Costruttore = inizializza (a "") tutto il programma. Stampa = metodo della
 * classe di appoggio per visualizzare il programma. Registra = metodo per
 * registrare un nome in una sessione. Ritorna 0 se OK.
 */

public class Programma implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * String [day] [session] [intervento]
	 */
	public String speaker[][][] = new String[3][12][5];

	// init
	public Programma() {
		for (int i = 0; i < speaker.length; i++) {
			for (int j = 0; j < speaker[i].length; j++) {
				for (int j2 = 0; j2 < speaker[i][j].length; j2++) {
					speaker[i][j][j2] = "";
				}
			}
		}
	}

	public synchronized int register(int day, int session, String name) {
		System.out.println("Programma: registrazione SPEAKER: " + name + " per la SESSIONE: " + session);
		// per corretto funzionamento
		day = day - 1;
		session = session - 1;
		for (int i = 0; i < speaker[day][session].length; i++) {
			if (speaker[day][session][i].equals("")) {
				speaker[day][session][i] = name;
				return 0;
			}
		}
		return 1;
	}

	public void toPrint() {
		for (int i = 0; i < speaker.length; i++) {
			System.out.println(
				"GIORNO " +
				(i + 1) +
				": \tIntervento 1\tIntervento 2\tIntervento 3\tIntervento 4\tIntervento 5"
			);
			for (int j = 0; j < speaker[i].length; j++) {
				System.out.print("Sessione " + (j + 1) + ": ");
				for (int j2 = 0; j2 < speaker[i][j].length; j2++) {
					System.out.print("\t" + speaker[i][j][j2]);
				}
				System.out.println();
			}
			System.out.println();
		}
	}
}
