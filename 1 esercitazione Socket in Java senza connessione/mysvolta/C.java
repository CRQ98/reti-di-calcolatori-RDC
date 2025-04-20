
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

//args: IP, PORT

/* 
 * TARGET:
 * nel service chiede a l'utente di inserire un FILENAME e N_LINE, 
 * poi manda DATAGRAM a server e riceve la LINE richiesta oppure msg di ERROR
 * 
 * ADDTIONAL REQUIREMENTS:
 * timeout 30s
 * 
 */

public class C {
	public static void main(String[] args) {
        //init
		InetAddress addr = null;
		int port = -1;
		
		//controll args
		if (args.length == 2) {
			try {
				//set addr
				addr=InetAddress.getByName(args[0]);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.out.println("Cannot determinate servers endpoint");
				System.out.println("Closing Client");
				closing(2);
			}
			//set port
			port=Integer.parseInt(args[1]);
			
		}else{
			System.out.println("Usage: java LineClient serverIP serverPort");
			closing(1);
		}
		
		//creating datagram
		DatagramSocket socket=null;
		DatagramPacket packet=null;
		byte[] buff=new byte[256];
		
		try {
			socket=new DatagramSocket();
			socket.setSoTimeout(30*1000);
			packet=new DatagramPacket(buff, buff.length, addr, port);
			System.out.println("Client: Started");
			System.out.println("Socket: " + socket);
		} catch (SocketException e) {
			e.printStackTrace();
			System.out.println("Failed to create socket");
			closing(3);
		}
		
		
	    //init vars, fuori while perche' a ogni ciclo si deve creare nuovamente
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		ByteArrayOutputStream bostream =null;
		DataOutputStream dostream = null;
		ByteArrayInputStream bistream =null;
		DataInputStream distream = null;
		byte[]bytedata=null;
		String filename = null;
		int nline=-1;
		String req = null;
		String res = null;
		//interact with client
	    System.out.println(
	    	      "\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): "
	    	    );
	    
		//while
	    try {
			while((filename = stdin.readLine())!= null) {
				try {
					System.out.println("Number of line: ");
					nline = Integer.parseInt(stdin.readLine());
					req=filename+" "+nline;
				} catch (IOException e) {
					System.out.println("Problemi nell'interazione da console: ");
					e.printStackTrace();
					 System.out.println(
		          		"\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): "
		        		);
					 continue;
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}
				
				//datastream to bytestream and send packet
				try {
					bostream = new ByteArrayOutputStream();
					dostream = new DataOutputStream(bostream);
					dostream.writeUTF(req);
					bytedata = bostream.toByteArray();
					packet.setData(bytedata);
					socket.send(packet);
					System.out.println("Richiesta inviata a " + addr + ", " + port);
				}catch(IOException e) {
					System.out.println("Problemi nell'invio della richiesta: ");
					e.printStackTrace();
					System.out.println(
						"\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): "
						);
					continue;
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}
				
				//receiving packet
				try {
					packet.setData(buff);
					socket.receive(packet); //bloccante, con timeout
				}catch(IOException e){
					System.out.println("Problemi nella ricezione del datagramma: ");
					e.printStackTrace();
			        System.out.println(
		          		"\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): "
		        		);
			        continue;
			       // il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}
				
				//bytestream to datastream
				try {
					bistream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					distream = new DataInputStream(bistream);
					res = distream.readUTF();
					System.out.println("Risposta: '" + res +"'");
				}catch(IOException e) {
					System.out.println("Problemi nella lettura della risposta: ");
					e.printStackTrace();
			        System.out.println(
		          		"\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): "
		        	);
			        continue;
			        // il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}
				
		        // tutto ok, pronto per nuova richiesta
		        System.out.println(
		          	"\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): "
		        	);
			}//while
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	    //User terminate cicle
	    System.out.println("Client: termino...");
	    //close socket after all
	    socket.close();
	}

	private static void closing(int exitcode) {
		System.out.println("Client is closing ...");
		System.exit(exitcode);
	}
	
}