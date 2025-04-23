import java.io.*;
import java.net.*;

public class S_seq {

	public static final int PORT = 54321; // porta default per server
	private static final String NEXT = "next";
	private static final String START = "start";

	public static void main(String[] args) {
		int port = -1;
		//controllo input
		try {
			if (args.length > 1) {
				usage();
			} else if (args.length == 1) {
				port = Integer.parseInt(args[1]);
			} else {
				port = PORT;
			}
		} catch (NumberFormatException e2) {
			errprintln("Numero di Port non valido, Interrompo...", e2);
			System.exit(2);
		}
		if (!My.checkPort(port)) {
			println("Port no valid.");
			System.exit(3);
		}

		println("Started.");
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			//cuz when we close it the system still will keep port for a while (TIME_WAIT)
			server.setReuseAddress(true);
			println("Created server socket: " + server);
		} catch (Exception e) {
			errprintln("Problemi nella creazione della server socket: " + e.getMessage(), e);
			System.exit(2);
		}
		//core----------------------------------------------------------------------------
		Socket client = null;
		try {
			try {
				println("Waiting for connection");
				client = server.accept();
				println("Accept connection :" + client);
				//client.setSoTimeout(50000);
			} catch (SocketException se) {
				errprintln("Error nella set timeout", se);
				server.close();

				System.exit(3);
			} catch (IOException ioe) {
				errprintln("Error in accept socket", ioe);
				server.close();
				System.exit(3);
			}

			DataInputStream insock = null;
			DataOutputStream outsock = null;
			String filename = null;
			try {
				insock = new DataInputStream(client.getInputStream());
				outsock = new DataOutputStream(client.getOutputStream());
			} catch (IOException ioe) {
				errprintln("Error in create data streams", ioe);
				client.close();
				server.close();
				System.exit(3);
			}

			while ((filename = insock.readUTF()) != null) {
				println("Filename received: " + filename);
				File file = new File(filename);
				if (file.exists()) {
					println("File <" + filename + "> already exists");
					outsock.writeUTF(NEXT);
				} else {
					outsock.writeUTF(START);
					long filesize = insock.readLong();
					println("FileSize: " + filesize);
					DataOutputStream fout = new DataOutputStream(
						new FileOutputStream(file)
					);
					println("Start transmission");
					My.transferFileBinary(insock, fout, filesize);
					fout.close();
				}
			}
		} catch (EOFException eofe) {
			println("Received EOF, now closing");
		} catch (IOException ioe) {
			errprintln("Error close socket", ioe);
		} catch (Exception e) {
			errprintln("Error detected in while", e);
		}
		try {
			server.close();
			client.close();
		} catch (IOException ioe) {
			errprintln("Error close server socket", ioe);
		}
		//core----------------------------------------------------------------------------

		println("Termino...");
	}

	private static void usage() {
		System.out.println("Usage: java S_con <serverPort> or java S_con");
		System.exit(1);
	}

	private static void errprintln(String s, Exception e) {
		System.err.println(Thread.currentThread().getName() + ": " + s);
		e.printStackTrace();
	}

	private static void println(String s) {
		System.out.println(Thread.currentThread().getName() + ": " + s);
	}
}
