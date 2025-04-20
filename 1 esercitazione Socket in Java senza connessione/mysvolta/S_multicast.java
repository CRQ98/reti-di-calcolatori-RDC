

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class S_multicast {
	private static final String FILE = "test";
	private static final long WAIT = 2;
	private static BufferedReader in = null;
	private static boolean moreLines = true;
	
	public static void main(String[] args) {
		InetAddress group = null;
		int port = -1;
		
		//control args
		if(args.length == 1) {
			try {
				group = InetAddress.getByName("230.0.0.1");
				port = Integer.parseInt(args[0]);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.out.println("S_multicast: cannot find the host 230.0.0.1");
				System.out
				.println("Usage: \"java MulticastServer MCastPort\" or \"java MulticastServer MCastAddr MCastPort\"");
				System.exit(1);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.out.println("S_multicast: need a port");
				System.out
				.println("Usage: \"java S_multicast MultiCastPort\" or \"java S_multicast MultiCastAddr MultiCastPort\"");
				System.exit(1);
			}
		}else if(args.length == 2) {
			try {
				group = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.out.println("S_multicast: cannot find the host 230.0.0.1");
				System.out
				.println("Usage: \"java MulticastServer MCastPort\" or \"java MulticastServer MCastAddr MCastPort\"");
				System.exit(1);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.out.println("S_multicast: need a port");
				System.out
				.println("Usage: \"java S_multicast MultiCastPort\" or \"java S_multicast MultiCastAddr MultiCastPort\"");
				System.exit(1);
			}
		}else{
			
			System.out
			.println("Usage: \"java S_multicast MultiCastPort\" or \"java S_multicast MultiCastAddr MultiCastPort\"");
			System.exit(1);
		}
		
		//create socket
		MulticastSocket socket = null;
		byte[] buf = new byte[256];
		DatagramPacket packet = new DatagramPacket(buf,0,buf.length,group,port) ;
		
		
		try {
			socket = new MulticastSocket();
			//if not work set net interface
			socket.joinGroup(group);
			System.out.println("Socket: " + socket);
		} catch (Exception e) {
			System.out.println("Problemi nella creazione della socket: ");
	    	e.printStackTrace();
	    	System.exit(2);
		}
		
		System.out.println("MulticastServer: avviato\nMCastAddr: " + group
        + "\nport: " + socket.getLocalPort());//packet.getPort(); 
		int count = -1; //contatore debug
		ByteArrayOutputStream bostream = null;
	    DataOutputStream dostream = null;
	    String line=null;

	    try {
		    //cycle multicasting
		    while(true) {
		    	count = 0;
		    	moreLines = true;
		    	
		    	try {
					in = new BufferedReader(new FileReader(FILE));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.out
	    			.println("MulticastServer, errore durante il reset del file reader, il seguente: "
	    					+ e + "\nEsco.");
					System.exit(3);
				}
		    	
		    	while(moreLines) {
		    		count++;
		    		line=LineUtility.getNextLine(in);
		    		if(line.equals("Nessuna linea disponibile")) {
		    			moreLines = false;
		    			break;
		    		}
		    		//DEBUG
					System.out.println("Estratta linea # " + count + " : " + line);
					
					//prepare to send it, data to bytes
					bostream = new ByteArrayOutputStream();
					dostream = new DataOutputStream(bostream);
					try {
						dostream.writeUTF(line);
					} catch (IOException e) {
						System.out.println("Problemi nel preparare byte stream da inviare: ");
						e.printStackTrace();
						continue;
					}
					buf = bostream.toByteArray();
					packet.setData(buf);
					try {
						socket.send(packet);
					} catch (IOException e) {
						System.out.println("Problemi nell'invio del datagramma: ");
						e.printStackTrace();
						continue;
					}
					
					//sleep 2 sec
						try {
							Thread.sleep(WAIT*1000);
						} catch (InterruptedException e) {
							System.out
					      	.println("MulticastServer, errore durante la sleep, il seguente: " + e);
						continue;
						}
		    	}//while moreLines
		    }//while true
	    }catch (Exception e) {
	    	//here catch other exception
	    	e.printStackTrace();
	    }
	    
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
