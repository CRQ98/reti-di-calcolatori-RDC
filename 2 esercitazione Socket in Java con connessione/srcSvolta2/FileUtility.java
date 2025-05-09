/* FileUtility.java */

import java.io.*;

public class FileUtility {

	/**
	 * Nota: sorgente e destinazione devono essere correttamente aperti e chiusi
	 * da chi invoca questa funzione.
	 *
	 */
	protected static void trasferisci_a_byte_file_binario(DataInputStream src, DataOutputStream dest)
		throws IOException {
		// ciclo di lettura da sorgente e scrittura su destinazione
		int buffer;
		try {
			// esco dal ciclo all lettura di un valore negativo -> EOF
			// N.B.: la funzione consuma l'EOF
			while ((buffer = src.read()) >= 0) {
				dest.write(buffer);
			}
			dest.flush();
		} catch (EOFException e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
		}
	}

	protected static void trasferisci_a_caratere_file_testo(BufferedReader src, BufferedWriter dest)
		throws IOException {
		String buffer;
		try {
			while ((buffer = src.readLine()) != null) {
				dest.write(buffer + "\n");
			}
			dest.flush();
		} catch (EOFException e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
		}
	}
}
