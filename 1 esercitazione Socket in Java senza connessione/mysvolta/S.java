import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;

//args: 
/*
 * TARGET: 
 * receive FILENAME form client
 * verify existence of FILE
 * if exist response client with LINE wished 
 * else ERROR MSG
 */
/*
 * ADDITIONAL:
 * server sequential
 */
public class S {
	
	// porta nel range consentito 1024-65535!
	// dichiarata come statica perch� caratterizza il server
	private static final int PORT = 4445;
	
	public static void main(String[] args) {
		System.out.println("Server : Started");
		
		int port=-1;
		
		//controll args
		//1 arg
		if(args.length == 1) {
			try {
				port = Integer.parseInt(args[0]);
			}catch(NumberFormatException e) {
				System.out.println("Usage: java LineServer [serverPort>1024 && <65535]");
		        System.exit(1);
			}
			if(port > 1024 && port < 65535) {
				//do nothing
			}else {
				System.out.println("Usage: java S [serverPort>1024 && <65535]");
				System.exit(1);
			}
		//no args
		}else if(args.length == 0) {
			port = PORT;
		//n agrs
		}else{
			System.out.println("Incorrect number of arguments");
			System.out.println("Usage: java S [serverPort>1024 && <65535]");
			System.exit(1);
		}
		
		//prepare socket to listen 
		DatagramPacket packet = null;
		DatagramSocket socket = null;
		byte[]buf=new byte[256];
		
		try {
			//socket listen mode
			socket = new DatagramSocket(port);
			//packet only for listen
			packet = new DatagramPacket(buf,buf.length);
		} catch (SocketException e) {
		      System.out.println("Problemi nella creazione della socket: ");
		      e.printStackTrace();
		      System.exit(1);
		}
		
		//ini vars 
		String filename=null;
		String req=null;
		String res=null;
		String line=null;
		int nline=-1;
		ByteArrayInputStream bistream = null;
		ByteArrayOutputStream bostream = null;
		DataInputStream distream = null;
		DataOutputStream dostream = null;
		byte[]data=new byte[256];
		StringTokenizer st = null;
		
		//here start service
		try {
			while(true) {
				System.out.println("\nIn attesa di richieste...");
				
				//receive datagram request
				try {
					packet.setData(buf);
					socket.receive(packet);
				}catch(IOException e) {
					System.err.println(
							"Problemi nella ricezione del datagramma: " + e.getMessage()
							);
					e.printStackTrace();
					continue;
				}
				
				//received request string, translate bytes to data
				try {
					bistream = new ByteArrayInputStream(packet.getData(),0,packet.getLength());
					distream = new DataInputStream(bistream);
					req = distream.readUTF();
					st = new StringTokenizer(req);
					filename = st.nextToken();
					nline = Integer.parseInt(st.nextToken());
					System.out.println(
							"Richiesta linea " + nline + " del file " + filename
							);
				}catch(Exception e) {
					System.err.println(
				            "Problemi nella lettura della richiesta: " +
				            filename + " " + nline
				            );
					e.printStackTrace();
					continue;
				}
				
				//elaborate request
				try {
					line = LineUtility.getLine(filename, nline);
					bostream = new ByteArrayOutputStream();
					dostream = new DataOutputStream(bostream);
					
					res = line;
					dostream.writeUTF(res);
					data = bostream.toByteArray();
					packet.setData(data,0,data.length);
					socket.send(packet);
				}catch(Exception e) {
					System.err.println(
							"Problemi nell'invio della risposta: " + e.getMessage()
							);
					e.printStackTrace();
					continue;
				}
				
			}//while
		//here catch other exceptions, that cannot catch in while
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("S: termino...");
	    socket.close();
	}

}
