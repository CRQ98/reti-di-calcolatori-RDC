import java.io.*;
import java.net.*;

public class S_con {

	private static final int PORT = 54321; // porta default per server

	public static void main(String[] args) {
		InetAddress addr = null;
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

		ServerSocket servers = null;
		try {
			servers = new ServerSocket(port);
			//cuz when we close it the system still will keep port for a while (TIME_WAIT)
			servers.setReuseAddress(true);
			println("Created server socket: " + servers);
		} catch (Exception e) {
			errprintln("Problemi nella creazione della server socket: " + e.getMessage(), e);
			System.exit(4);
		}
		while (true) {
			Socket soc = null;
			try {
				println("Waiting for req");
				soc = servers.accept();
				println("Accept connection :" + soc);
				soc.setSoTimeout(10000);
			} catch (SocketException se) {
				errprintln("Error nella set timeout", se);
				continue;
			} catch (IOException ioe) {
				errprintln("Error in accept socket", ioe);
				continue;
			}
			try {
				new Thread(new S(soc)).start();
			} catch (Exception e) {
				errprintln("Error in S_con thread", e);
			}
		}
	}

	private static void usage() {
		System.out.println("Usage: java S_con <serverPort> or java S_con");
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

class S implements Runnable {

	private Socket sock = null;

	public S(Socket sock) {
		this.sock = sock;
	}

	public void run() {
		DataInputStream insock = null;
		DataOutputStream outsock = null;
		String filename = null;
		try {
			try {
				insock = new DataInputStream(sock.getInputStream());
				outsock = new DataOutputStream(sock.getOutputStream());
				filename = insock.readUTF();
			} catch (SocketTimeoutException ste) {
				errprintln("socket Timeout", ste);
				sock.close();
				return;
			} catch (IOException ioe) {
				errprintln("Error in create data streams", ioe);
				sock.close();
				return;
			}
			if (filename == null) {
				println("Lettura filename failed");
				sock.close();
				return;
			}
			println("Filename received: " + filename);
			FileOutputStream fout = null;
			File file = new File(filename);
			String outcome = null;
			outcome = file.exists() ? "Overwrite file" : "Created new file";
			fout = new FileOutputStream(file);
			try {
				My.transferFileBinary(insock, new DataOutputStream(fout));
				fout.close();
				sock.shutdownInput();
				outsock.writeUTF(outcome);
				sock.shutdownOutput();
				sock.close();
			} catch (SocketTimeoutException ste) {
				errprintln("Client socket timeout", ste);
				sock.close();
				return;
			} catch (IOException ioe) {
				errprintln("Error in transfer file", ioe);
				sock.close();
				return;
			}
		} catch (Exception e) {
			errprintln("Other Error ", e);
			System.exit(5);
		}
	}

	private static void errprintln(String s, Exception e) {
		System.err.println(Thread.currentThread().getStackTrace()[1].getClassName() + ": " + s);
		e.printStackTrace();
	}

	private static void println(String s) {
		System.out.println(Thread.currentThread().getStackTrace()[1].getClassName() + ": " + s);
	}
}
