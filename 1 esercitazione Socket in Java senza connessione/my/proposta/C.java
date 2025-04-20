import java.io.*;
import java.net.*;

public class C {

	public static void main(String[] args) {
		if (args.length != 3) {
			usage();
		}
		My my = new My();
		String filename = args[2];
		int port = -1;
		InetAddress addr = null;
		DatagramSocket ds = null;
		DatagramPacket dp = null;
		byte[] buf = null;
		byte[] data = null;
		String req = null, res = null;
		try {
			port = Integer.parseInt(args[1]);
			if (!My.checkPort(port)) throw new NumberFormatException();
			addr = InetAddress.getByName(args[0]);
			ds = new DatagramSocket();
			ds.setSoTimeout(10000);
			buf = new byte[256];
			dp = new DatagramPacket(buf, buf.length, addr, port);
			req = filename;
			data = my.DBOutputStream(req);
			dp.setData(data);
			ds.send(dp);
		} catch (UnknownHostException uhe) {
			System.err.println("Cannot resolve IP");
			ds.close();
			System.exit(2);
		} catch (NumberFormatException nfe) {
			System.err.println("Error convert number");
			nfe.printStackTrace();
			ds.close();
			System.exit(2);
		} catch (SocketException se) {
			System.err.println("Error nella creazione socket or set timeout");
			se.printStackTrace();
			ds.close();
			System.exit(2);
		} catch (IOException ioe) {
			System.err.println("Error in send packet");
			ioe.printStackTrace();
			ds.close();
			System.exit(2);
		}
		System.out.println("C avviato");
		dp.setData(buf);
		try {
			ds.receive(dp);
			System.out.println("Received packet from: " + dp.getAddress() + ":" + dp.getPort());
			res = my.BDInputStream(dp.getData());
			System.out.println("res: " + res);
			dp = new DatagramPacket(buf, buf.length, addr, Integer.parseInt(res));
		} catch (Exception e) {
			System.err.println("Receive or parse problem");
			e.printStackTrace();
			System.exit(3);
		}
		System.out.println("Port to rowswap:" + res);
		System.out.println("Write first row.(Ctrl+D(Unix)/Ctrl+Z(Win)+Enter to END)");

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String stdin, row1, row2;
		try {
			while ((stdin = in.readLine()) != null) {
				row1 = stdin;
				System.out.println("Write second row.(Ctrl+D(Unix)/Ctrl+Z(Win)+Enter to END)");
				row2 = in.readLine();
				if (row2 == null) break;
				try {
					req = Integer.parseInt(row1) + "-" + Integer.parseInt(row2);
				} catch (NumberFormatException nfe) {
					System.err.println("Error convert number");
					nfe.printStackTrace();
					System.out.println("Write second row.(Ctrl+D(Unix)/Ctrl+Z(Win)+Enter to END)");
					continue;
				}

				dp.setData(my.DBOutputStream(req));
				try {
					ds.send(dp);
				} catch (IOException ioe) {
					System.err.println("Error in send swap row");
					ioe.printStackTrace();
					System.out.println("Write first row.(Ctrl+D(Unix)/Ctrl+Z(Win)+Enter to END)");
					continue;
				}
				dp.setData(buf);
				try {
					ds.receive(dp);
					System.out.println("Risultato :" + My.BDInputStream(dp.getData()));
				} catch (IOException ioe) {
					System.err.println("Error in receive responce of RowSwap");
					ioe.printStackTrace();
					System.out.println("Write first row.(Ctrl+D(Unix)/Ctrl+Z(Win)+Enter to END)");
					continue;
				}
				System.out.println("Write first row.(Ctrl+D(Unix)/Ctrl+Z(Win)+Enter to END)");
			}
		} catch (Exception e) {
			System.err.println("Other Error detected in while");
			e.printStackTrace();
		}
		System.err.println("Termino...");

		ds.close();
	} //main

	private static void usage() {
		System.out.println("Usage: java C <S_IP> <S_Port> <filename>");
		System.exit(1);
	}
}
