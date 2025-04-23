import java.io.*;

public class My {

	/**
	 * Function to validate port
	 * @param port
	 * @return true or false
	 */
	static boolean checkPort(int port) {
		boolean flag = false;
		if (port >= 1024 && port <= 65535) flag = true;
		return flag;
	}

	/**
	 * This function create input stream dataflow from ByteArray to Data
	 * @param data[]
	 * @return String read from data[]
	 */
	static String BtoDInputStream(byte[] data) {
		String dataUTF = null;
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bis);
		try {
			dataUTF = dis.readUTF();
		} catch (IOException e) {
			System.out.println("Error from readUTF");
			e.printStackTrace();
			return dataUTF;
		}

		return dataUTF;
	}

	/**
	 * This function create output stream dataflow from Data to ByteArray
	 * @param String
	 * @return ByteArray converted from dataUTF
	 */
	static byte[] DtoBOutputStream(String s) {
		byte[] data = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeUTF(s);
		} catch (IOException e) {
			System.out.println("Error from writeUFT");
			e.printStackTrace();
			return data;
		}
		data = bos.toByteArray();
		return data;
	}

	/**
	 * metodo per recuperare la linea successiva di un file aperto in precedenza
	 * @param in
	 * @return linea or null
	 */
	static String getNextLine(BufferedReader in) {
		String linea = null;
		try {
			if ((linea = in.readLine()) == null) {
				in.close();
			}
		} catch (FileNotFoundException e) {
			System.out.println("File non trovato: ");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Problemi nell'estrazione della linea: ");
			e.printStackTrace();
		}
		return linea;
	} //getNextLine

	/**
	 * metodo per recuperare una certa linea di un certo file
	 * @param nomeFile
	 * @param numLinea
	 * @return linea letta o null
	 */
	static String getLine(String nomeFile, int numLinea) {
		String linea = null;
		BufferedReader in = null;

		if (numLinea <= 0) return linea;
		// associazione di uno stream di input al file da cui estrarre le linee
		try {
			in = new BufferedReader(new FileReader(nomeFile));
			System.out.println("File aperto: " + nomeFile);
		} catch (FileNotFoundException e) {
			System.out.println("File non trovato: ");
			e.printStackTrace();
			return linea;
		}
		try {
			for (int i = 1; i <= numLinea; i++) {
				linea = in.readLine();
				if (linea == null) {
					in.close();
					return linea;
				}
			}
		} catch (IOException e) {
			System.out.println("Linea non trovata: ");
			e.printStackTrace();
			return linea;
		}
		System.out.println("Linea selezionata: " + linea);

		try {
			in.close();
		} catch (IOException e) {
			System.out.println("Errore nella chiusura del reader");
			e.printStackTrace();
		}
		return linea;
	} // getLine

	/**
	 * @param src BufferedReader
	 * @param dest BufferedWriter
	 */
	static void transferFileText(BufferedReader src, BufferedWriter dest) throws IOException {
		String buffer;
		try {
			while ((buffer = src.readLine()) != null) {
				dest.write(buffer + "\n");
			}
			dest.flush();
		} catch (EOFException e) {
			System.err.println("Problemi, i seguenti: ");
			e.printStackTrace();
		}
	} //transferFileText

	/**
	 * @param src DataInputStream
	 * @param dest DataOutputStream
	 */
	static void transferFileBinary(DataInputStream src, DataOutputStream dest) throws IOException {
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
			System.err.println("Problemi, i seguenti: ");
			e.printStackTrace();
		}
	} //transferFileBinary

	/**
	 * @param src DataInputStream
	 * @param dest DataOutputStream
	 * @param size size of file
	 */
	static void transferFileBinary(DataInputStream src, DataOutputStream dest, long size)
		throws IOException {
		// ciclo di lettura da sorgente e scrittura su destinazione
		int counter = 0;
		try {
			// esco dal ciclo all lettura di un valore negativo -> EOF
			// N.B.: la funzione consuma l'EOF
			while (counter++ < size) {
				dest.write(src.read());
			}
			dest.flush();
		} catch (EOFException e) {
			System.err.println("Problemi, i seguenti: ");
			e.printStackTrace();
		}
	} //transferFileBinary
}
//Exception part
//common exception
/*

catch(SocketException se){
    errprintln("Error nella creazione socket",se);
}

catch(IOException ioe){
    errprintln("Error ",ioe);
}

catch(NumberFormatException nfe){
    errprintln("Error convert number",nfe);
}

catch(Exception e){
    errprintln("Other Error ",e);
}

catch(FileNotFoundException fnfe){
  errprintln("Error non trova file "+filename,fnfe);
}

catch (SocketTimeoutException ste) {
  errprintln("socket Timeout",ste);
}

 */
