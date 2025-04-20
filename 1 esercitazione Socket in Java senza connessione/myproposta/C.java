import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

//args: DSIP, DSport, filename

/*
 * TARGET:
 * send REQ to DS
 * if success 
 * 		receive port of S
 * 		send number of row to SWAP
 * 		receive RES
 * else 
 * 		ERROR MSG and C exit
 */
public class C {

	public static void main(String[] args) {
		InetAddress DSaddr = null;
		int DSport = -1;

		// control args
		if (args.length != 3) {
            System.out.println("Usage: java C ipDiscoveryServer portDiscoveryServer fileName");
			System.exit(1);
		} else {
			try {
				DSaddr = InetAddress.getByName(args[0]);
				DSport = Integer.parseInt(args[1]);
			} catch (UnknownHostException e) {
				System.out.println("Host problem: ");
				e.printStackTrace();
				System.exit(1);
			} catch (NumberFormatException nfe) {
				System.out.println("Port problem: ");
				nfe.printStackTrace();
				System.exit(1);
			}
		} // else
		System.out.println("\nC: avviato");

		String filename = args[2];
		System.out.println("Interrogo il discovery server:\nIndirizzo: " + DSaddr.getHostAddress() + "\nporta: "
				+ DSport + "\nfile: " + filename);
		// creazione e inizializzazione socket,packet
		DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];

		try {
			socket = new DatagramSocket();
			//socket.setSoTimeout(2 * 1000);
		} catch (SocketException e) {
			System.out.println("Create socket problem: ");
			e.printStackTrace();
			System.out.println("C: exit...");
			System.exit(2);
		}
		System.out.println("Creata la socket: " + socket);
		packet = new DatagramPacket(buf, buf.length, DSaddr, DSport);

		// prepare stream to send req to DS
		ByteArrayOutputStream bostream = null;
		DataOutputStream dostream = null;
		ByteArrayInputStream bistream = null;
		DataInputStream distream = null;
		byte[] data = null;
		String res = null;
		String req = null;

		// send req to DS
		req = filename;
		bostream = new ByteArrayOutputStream();
		dostream = new DataOutputStream(bostream);
		try {
			dostream.writeUTF(req);
			data = bostream.toByteArray();
			packet.setData(data);
			socket.send(packet);
		} catch (IOException e) {
			System.out.println("Send socket problem: ");
			e.printStackTrace();
			System.exit(3);
		}

		// receive res from DS
		packet.setData(buf);
		try {
			socket.receive(packet);
		} catch (IOException e) {
			System.out.println("Receive socket problem: ");
			e.printStackTrace();
			System.exit(3);
		}

		// get data from bytes
		bistream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
		distream = new DataInputStream(bistream);
		try {
			res = distream.readUTF();
		} catch (IOException e) {
			System.out.println("Read DS response problem: ");
			e.printStackTrace();
			System.exit(3);
		}
		int Sport = -1;
		if (res.equals("File non trovato")) {
			System.out.println(res + "\nExit...");
			socket.close();
			System.exit(4);
		} else{
            System.out.println("Response of DS: " + res);
            Sport = Integer.parseInt(res);
        }
			

		// req to S
		buf = new byte[256];
		packet = new DatagramPacket(buf, buf.length, DSaddr, Sport);

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int row1, row2;
		row1 = row2 = -1;
		String useranswer = null;
		System.out.println("\n^D(Unix)/^Z(Win)+invio per uscire\nInserire row 1: ");
		try {
			while ((useranswer = br.readLine()) != null) {
				try {
					// interact
					row1 = Integer.parseInt(useranswer);
					System.out.println("Inserire row 2: ");
					row2 = Integer.parseInt(br.readLine());
					req = row1 + " " + row2;
					System.out.println("Richiesta row swap: " + req);

					// prepare to send
                    bostream = new ByteArrayOutputStream();
		            dostream = new DataOutputStream(bostream);
					dostream.writeUTF(req);
					data = bostream.toByteArray();
					packet.setData(data);
					socket.send(packet);

					// prepare to receive
					packet.setData(buf);
					socket.receive(packet);

					// prepare stream to get data from bytes
					bistream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					distream = new DataInputStream(bistream);
					int outcomecode = distream.readInt();
					System.out.println("Risposta da ServerSwap: " + outcomecode);

				} catch (Exception e) {
					System.out.println("Problemi incontrati: ");
					e.printStackTrace();
					System.out.println("\n^D(Unix)/^Z(Win)+invio per uscire\nInserire row 1: ");
					continue;
				}
				System.out.println("\n^D(Unix)/^Z(Win)+invio per uscire\nInserire row 1: ");
			}//while
		} catch (Exception e) {
			System.out.println("Problemi che causa la chiusura: ");
			e.printStackTrace();
		}
		System.out.println("Closing");
		socket.close();

	}

}
