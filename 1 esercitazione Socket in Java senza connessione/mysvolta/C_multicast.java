

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

public class C_multicast {

	public static void main(String[] args) {
		InetAddress group = null;
		int port = -1;
		
		//control args
		try {
			if(args.length == 2) {
				group = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
				System.out.println("Mi pongo in ascolto su:\nMCastAddr: " + args[0]
		    			+ "\nMCastPort: " + port);
			}else {
				System.out.println("Usage: java C_multicast MultiCastAddr MultiCastPort");
	    		System.exit(1);//Incorrect args
			}
		}//try
		catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
	    	e.printStackTrace();
	    	System.exit(2);
		}
		
		//create multicast socket
		MulticastSocket socket = null;
		byte []buf= new byte[256];
		DatagramPacket packet = null;
		System.out.println("\nC_multicast: avviato");
		
		try {
			socket = new MulticastSocket(port);
			socket.setSoTimeout(20*1000);
			packet = new DatagramPacket(buf,buf.length);
	    	System.out.println("Creata la socket: " + socket);
		}catch(Exception e) {
			System.out.println("Problemi nella creazione della socket: ");
	    	e.printStackTrace();
	    	System.exit(3);
		}
		
		//join to the group
		try {
			/* Per evitare problemi, con computer non connessi in rete,
	    	 * bisogna impostare l'interfaccia di rete PRIMA di fare la
	    	 * join al gruppo.
	    	 *
	    	 * Il problema viene quindi risolto settando l'interfaccia di rete sull'oggetto socket, 
			 * perciò prima di invocare:
			 * 
			 * multicastSocket.joinGroup(group);
			 * 
			 * bisogna impostare l'interfaccia di rete:
			 * 
			 * // mi procuro l'inetaddress locale
			 * 
			 * InetAddress netInterface= InetAddress.getByName("localhost");
			 * 
			 * // imposto l'interfaccia di rete
			 * 
			 * socket.setInterface(netInterface);
	    	 */
			socket.joinGroup(group);
			System.out.println("Adesione al gruppo " + group);
		}catch(Exception e) {
	    	System.out.println("Problemi nell'adesione al gruppo: ");
	    	e.printStackTrace();
	    	System.exit(4);		
		}
		
		//prepare stream
		ByteArrayInputStream bistream = null;
		DataInputStream distream = null;
		String line = null;
		
		//cycle, receive
		for(int i = 0; i < 20; i++) {
			System.out.println("\nIn attesa di un datagramma... ");
			
			try {
				//packet.setData(buf);
				socket.receive(packet);
			}catch(SocketTimeoutException ste) {
				System.out.println("Non ho ricevuto niente per 20 secondi, chiudo!");
	    		System.exit(5);
			}catch(IOException e) {
				System.out.println("Problemi nella ricezione del datagramma: ");
	    		e.printStackTrace();
	    		continue;
			}
			
			//get data form bytes received recently
			try {
				bistream = new ByteArrayInputStream(packet.getData(),0,packet.getLength());
				distream = new DataInputStream(bistream);
				line = distream.readUTF();
				System.out.println("Linea ricevuta: " + line);
			}catch(IOException e) {
				System.out.println("Problemi nella lettura del datagramma: ");
	    		e.printStackTrace();
	    		continue;
			}
		}//for 20
		System.out.println("\nUscita dal gruppo");
		
		//leave group and close
		try {
			socket.leaveGroup(group);
		}catch(IOException e) {
			System.out.println("Problemi nell'uscita dal gruppo: ");
	    	e.printStackTrace();
		}
		System.out.println("C_multicast: termino...");
		socket.close();
	}//main
}
