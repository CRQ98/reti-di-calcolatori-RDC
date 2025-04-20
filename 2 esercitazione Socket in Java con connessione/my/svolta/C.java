import java.io.*;
import java.net.*;

public class C {

	public static void main(String[] args) {
		InetAddress addr = null;
		int port = -1;
		//controllo input
		try {
			if (args.length != 2) {
				usage();
			} else {
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
			}
		} catch (UnknownHostException e1) {
			errprintln("Impossibile determinare ServerIP, Interrompo...", e1);
			System.exit(2);
		} catch (NumberFormatException e2) {
			errprintln("Numero di Port non valido, Interrompo...", e2);
			System.exit(2);
		}
		if (!My.checkPort(port)) {
			println("Port no valid.");
			System.exit(3);
		}
		println("Started.");

		Socket socket = null;
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		DataInputStream insock = null;
		DataOutputStream outsock = null;

		println("Insert the filename that you want upload to server, ^D(Unix)/^Z(Win)+Enter to END");
		try {
			while ((line = stdin.readLine()) != null) {
				String filename = line;
				if (!new File(filename).exists()) {
					println("File doesn't exists");
					prompt();
					continue;
				}
				//create socket
				try {
					socket = new Socket(addr, port);
					socket.setSoTimeout(20000);
					println("Creato socket: " + socket);
				} catch (SocketException se) {
					errprintln("Error nella set SoTimeout", se);
					socket.close();
					prompt();
					continue;
				} catch (IOException ioe) {
					errprintln("Error nella creazione socket", ioe);
					socket.close();
					prompt();
					continue;
				}
				//create streams
				try {
					insock = new DataInputStream(socket.getInputStream());
					outsock = new DataOutputStream(socket.getOutputStream());
				} catch (IOException ioe) {
					System.err.println("Error nel creazione degli stream di socket");
					ioe.printStackTrace();
					socket.close();
					prompt();
					continue;
				}
				DataInputStream infile = new DataInputStream(new FileInputStream(filename));
				try {
					outsock.writeUTF(filename);
					My.transferFileBinary(infile, outsock);
					infile.close();
					socket.shutdownOutput();
				} catch (IOException ioe) {
					errprintln("Error send filename or send file", ioe);
					infile.close();
					socket.close();
					prompt();
					continue;
				}
				String outcome = null;
				try {
					outcome = insock.readUTF();
					println("Risultato: " + outcome);
					socket.shutdownInput();
				} catch (IOException ioe) {
					errprintln("Error read result", ioe);
					prompt();
					socket.close();
					continue;
				}
			} //while
			socket.close();
		} catch (IOException e) {
			errprintln("Errore nel read form stdin or chiusura socket", e);
		} catch (Exception e) {
			errprintln("Other Error in while", e);
		}
		println("Termino...");
	}

	private static void usage() {
		System.out.println("Usage: java C serverIP serverPort");
		System.exit(1);
	}

	private static void errprintln(String s, Exception e) {
		System.err.println(Thread.currentThread().getStackTrace()[1].getClassName() + ": " + s);
		e.printStackTrace();
	}

	private static void println(String s) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getClassName() + ": " + s);
	}

	private static void prompt() {
		println("Insert the filename that you want upload to server, ^D(Unix)/^Z(Win)+Enter to END");
	}
}
