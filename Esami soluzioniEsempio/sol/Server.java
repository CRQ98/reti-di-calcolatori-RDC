
/**
 * ServerCongressoImpl.java
 * 		Implementazione del server
 * */

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Server extends UnicastRemoteObject implements RemOp {
	static Stanza stanza[];
	final static int N = 100;
	// Costruttore
	public Server() throws RemoteException {
		super();
	}

	// Richiede una prenotazione
	public boolean aggiungi_stanza(String nomeStanza, char tipoComunicazione) throws RemoteException {
		boolean esito=false;
		for(int i =0;i < N;i++){
			if(stanza[i].nomeStanza.equals("L")){
				stanza[i].nomeStanza=nomeStanza;
				stanza[i].stato=tipoComunicazione;
				esito=true;
				break;
			}
		}
		return esito;
	}

	public Stanza[] elimina_utente(String nomeUtente) throws RemoteException{
		int couter=0;
		Stanza [] ris = new Stanza[N];
		for (int i = 0; i < N; i++)
			ris[i] = new Stanza();
		for(int i =0;i<N;i++){
			if(stanza[i].elimina_utente(nomeUtente)){
				ris[couter++].nomeStanza=stanza[i].nomeStanza;
			}
		}
		return ris;
	}


	// Avvio del Server RMI
	public static void main(String[] args) {

		// creazione programmi per le tre giornate di congresso.s
		stanza = new Stanza[N];
		for (int i = 0; i < N; i++)
			stanza[i] = new Stanza();
		final int REGISTRYPORT = 1099;
		String registryHost = "localhost";
		String serviceName = "Server"; // lookup name...

		// Registrazione del servizio RMI
		String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/" + serviceName;
		try {
			Server serverRMI = new Server();
			Naming.rebind(completeName, serverRMI);//bind url con serverRMI
			System.out.println("Server RMI: Servizio \"" + serviceName + "\" registrato");
		} catch (Exception e) {
			System.err.println("Server RMI \"" + serviceName + "\": " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}