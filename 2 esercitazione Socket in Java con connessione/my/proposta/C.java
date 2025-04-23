//C
import java.io.*;
import java.net.*;

public class C {

	private static final String NEXT = "next";
	private static final String START = "start";

	public static void main(String[] args) {
		InetAddress addr = null;
		int port = -1;
		int minSize = 0;
		//controllo input
		try {
			if (args.length != 3) {
				usage();
			} else {
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
				minSize = Integer.parseInt(args[2]);
			}
		} catch (UnknownHostException e1) {
			errprintln("Impossibile determinare ServerIP, Interrompo...", e1);
			System.exit(2);
		} catch (NumberFormatException e2) {
			errprintln("Numero di Port or minSize non valido, Interrompo...", e2);
			System.exit(2);
		}
		if (!My.checkPort(port)) {
			println("Port no valid.");
			System.exit(3);
		}
		println("Started.");
		//core----------------------------------------------------------------------------
		Socket socket = null;
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		DataInputStream insock = null;
		DataOutputStream outsock = null;

		try {
			//create socket
			try {
				socket = new Socket(addr, port);
				socket.setSoTimeout(20000);
				println("Creato socket: " + socket);
			} catch (SocketException se) {
				errprintln("Error nella set SoTimeout", se);
				socket.close();
			} catch (IOException ioe) {
				errprintln("Error nella creazione socket", ioe);
				socket.close();
			}
			//create streams
			try {
				insock = new DataInputStream(socket.getInputStream());
				outsock = new DataOutputStream(socket.getOutputStream());
			} catch (IOException ioe) {
				System.err.println("Error nel creazione degli stream di socket");
				ioe.printStackTrace();
				socket.close();
			}
			prompt();
			while ((line = stdin.readLine()) != null) {
				String dirName = line;
				File dir = new File(dirName);
				if (!dir.isDirectory()) {
					println("Is not a directory!");
					prompt();
					continue;
				}

				File[] files = dir.listFiles();

				for (File f : files) {
					String filename = f.getName();
					long filesize = f.length();
					String res = null;

					println("File: " + filename + " FileSize: " + filesize);
					if (f.isDirectory()) {
						println(filename + " Is a directory, go next");
						continue;
					}
					if (!f.isFile()) {
						println(filename + " Is not a file, go next");
						continue;
					}
					if (f.length() > minSize) {
						println(
							"File <" +
							filename +
							"> lenght <" +
							filesize +
							"> over the minSize <" +
							minSize +
							">"
						);
						continue;
					}
					try {
						outsock.writeUTF(filename);
						res = insock.readUTF();
					} catch (IOException ioe) {
						errprintln("Error write filename or read responce ", ioe);
					}
					if (res.equals(NEXT)) {
						println("Serve says go next");
						continue;
					} else if (res.equals(START)) {
						println("Serve says go let's go");

						try {
							println("Start transmission");
							outsock.writeLong(filesize);
							DataInputStream fin = new DataInputStream(
								new FileInputStream(f)
							);
							My.transferFileBinary(fin, outsock, filesize);
							fin.close();
						} catch (IOException ioe) {
							errprintln("Error in send file", ioe);
						}
					}
				} //for
				prompt();
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
		System.out.println("Usage: java C <serverIP> <serverPort> <minSize>");
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
		println("Insert the Directory that you want upload to server, ^D(Unix)/^Z(Win)+Enter to END");
	}
}
