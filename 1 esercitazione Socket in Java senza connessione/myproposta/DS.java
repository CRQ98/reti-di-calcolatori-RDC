//args: DSport, filename1, Sport1, filename2, Sport2...
/*
 * TARGET:
 * active all Server
 * receive REQ from Client
 * if exist 
 * 		RES with Sport
 * else 
 * 		ERROR MSG
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class DS {

	public static void main(String[] args) {
		// args control
		int npair = -1;
		if (args.length % 2 == 1) {
			npair = args.length / 2;
		} else {
			System.err.println("args.length error, it must be singular");
			System.out.println("Usage: java DS DSport filename1 Serverport1 filename2 Serverport2...");
			System.exit(1);
		}
		int DSport = -1;
		DSport = Integer.parseInt(args[0]);
		if (DSport <= 1024 || DSport > 65535) {
			System.out.println("The discovery server port is not valid: " + args[0]);
			System.exit(2);
		}
		File file = null;
		String filename = null;
		int Sport = -1;
		Map<String, Integer> fileport = new HashMap<>();
		S server;
		// cycle to active Servers
		for (int i = 0; i < npair; i++) {
			System.out.println("\n[File: " + args[i * 2 + 1] + "] \n[Port: " + args[i * 2 + 2]+"]");
			filename = args[i * 2 + 1];
			file = new File(filename);
			try {
				Sport = Integer.parseInt(args[i * 2 + 2]);

			} catch (NumberFormatException e) {
				System.out.println("Port: " + args[i * 2 + 2] + " is not a number");
				continue;
			}
			if (file.exists()) {
				System.out.println("File: " + file.getName() + " -> exist");
			} else {
				System.out.println("File: " + file.getName() + " -> not exist");
				continue;
			}
			if (Sport < 1024 || Sport > 65535 || fileport.containsValue(Sport)) {
				System.out.println("The SwapServer port " + Sport + " is NOT valid");
				continue;
			} else {
				System.out.println("The SwapServer port " + Sport + " is valid");
			}
			Sport = Integer.parseInt(args[i * 2 + 2]);
			fileport.put(file.getName(), Sport);
			server = new S(file.getName(), Sport);
			server.start();
		} // for npair

		// prepare Socket
		DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];

		try {
			socket = new DatagramSocket(DSport);
			packet = new DatagramPacket(buf, buf.length);
			System.out.println("\nDiscoveryServer avviato con socket port: " + socket.getLocalPort());
		} catch (SocketException e) {
			System.out.println("Create socket problem: ");
			e.printStackTrace();
			System.out.println("DS: exit...");
			System.exit(4);
		}

		// prepare stream to receive req from client
		ByteArrayOutputStream bostream = null;
		DataOutputStream dostream = null;
		ByteArrayInputStream bistream = null;
		DataInputStream distream = null;
		byte[] data = null;
		String res = null;
		String req = null;

		// cycle to manage reqs
		try {
			while (true) {
				try {
					// prepare to receive
					packet.setData(buf);
					socket.receive(packet);
					bistream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					distream = new DataInputStream(bistream);
					req = distream.readUTF();
					System.out.println("\nRichiesta per nome file: " + req);

					if (fileport.containsKey(req)) {
						res = "" + fileport.get(req);
					} else {
						res = "File non trovato";
					}

					// prepare to send
					bostream = new ByteArrayOutputStream();
					dostream = new DataOutputStream(bostream);
					dostream.writeUTF(res);
					data = bostream.toByteArray();
					packet.setData(data);
					socket.send(packet);
				} catch (IOException e) {
					System.out.println("Problem during process request: ");
					e.printStackTrace();
					continue;
				}
			} // while
		}catch(Exception e ) {
			System.err.println("Other exception:");
			e.printStackTrace();
		}
		System.out.println("DS is closing...");
		socket.close();
	}// main
}
