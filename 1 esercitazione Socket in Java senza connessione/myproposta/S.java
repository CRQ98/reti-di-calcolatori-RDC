

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;

public class S extends Thread {
	private String filename = null;
	private int port = -1;
	private DatagramSocket socket = null;
	private DatagramPacket packet = null;

	public S(String filename, int port) {
		super();
		this.filename = filename;
		this.port = port;
	}

	public void run() {
        byte[] buf = new byte[256];
		try {
			socket = new DatagramSocket(port);
			packet = new DatagramPacket(buf, buf.length);
			System.out
					.println("SwapServer per file [" + filename + "] avviato con socket port: [" + socket.getLocalPort()+"]");
		} catch (SocketException e) {
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.exit(1);
		}

		ByteArrayOutputStream bostream = null;
		DataOutputStream dostream = null;
		ByteArrayInputStream bistream = null;
		DataInputStream distream = null;
		byte[] data = null;
		String req = null;
		String res = null;
		try {
			while (true) {
				try {
                    //buf = new byte[256];
                    packet.setData(buf);
					socket.receive(packet);
					bistream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					distream = new DataInputStream(bistream);
					req = distream.readUTF();
					StringTokenizer st = new StringTokenizer(req);
					int row1 = Integer.parseInt(st.nextToken());
					int row2 = Integer.parseInt(st.nextToken());
					System.out.println("FirstRow: " + row1 + " SecondRow: " + row2);
					File file = new File(filename);
					File temp = new File("temp_" + filename);
					BufferedReader br = new BufferedReader(new FileReader(file));
					String line, line1, line2;
					line = line1 = line2 = null;
					int counter = 0;
					System.out.println("Swapping rows...");
					while ((line = br.readLine()) != null) {
						counter++;
						if (counter == row1) {
							line1 = line;
						} else if (counter == row2) {
							line2 = line;
						}
					}
					br.close();
					counter = 0;
					br = new BufferedReader(new FileReader(file));
					BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
					while ((line = br.readLine()) != null) {
						counter++;
						if (counter == row1) {
							bw.write(line2 + "\n");
						} else if (counter == row2) {
							bw.write(line1 + "\n");
						} else {
							bw.write(line + "\n");
						}
					}
					br.close();
					bw.close();
					temp.renameTo(file);
					System.out.println("Operation success");
					//res = "0";
                    int rescode=0;
					bostream = new ByteArrayOutputStream();
					dostream = new DataOutputStream(bostream);
					dostream.writeInt(rescode);
					data = bostream.toByteArray();
					packet.setData(data);
					socket.send(packet);
				} catch (Exception e) {
					System.out.println("Problem during process request: ");
					e.printStackTrace();
					continue;
				}
			} // while
		} catch (Exception e) {
			System.err.println("Other exception:");
			e.printStackTrace();
		}
		System.out.println("S is closing...");
		socket.close();

	}// run()
}
