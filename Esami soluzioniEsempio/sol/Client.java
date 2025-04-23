
/**
 * ClientCongresso.java
 *
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;

class ClientCongresso {

	public static void main(String[] args) {
		final int REGISTRYPORT = 1099;
		String registryHost = null; // host remoto con registry
		String serviceName = "";
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

		// Controllo dei parametri della riga di comando
		if (args.length != 2) {
			System.out.println("Sintassi: RMI_Registry_IP ServiceName");
			System.exit(1);
		}
		registryHost = args[0];
		serviceName = args[1];

		System.out.println("Invio richieste a " + registryHost + " per il servizio di nome " + serviceName);

		// Connessione al servizio RMI remoto
		try {
			String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/" + serviceName;
			RemOp serverRMI = (RemOp) Naming.lookup(completeName);
			System.out.println("ClientRMI: Servizio \"" + serviceName + "\" connesso");

			System.out.println("\nRichieste di servizio fino a fine file");

			String service;
			System.out.print("Servizio (A=aggiungi una stanza, E=elimina un utente): ");

			/* ciclo accettazione richieste utente */
			while ((service = stdIn.readLine()) != null) {

				if (service.equals("A")) {
					boolean ok = false; // stato [VALID|INVALID] della richiesta --> come un flag
					System.out.println("inserire un nome per la creazione della stanza");
					String nomeStanza = stdIn.readLine();
					System.out.println("inserire tipo di stanza vuoi creare P=peer to peer, M=multicast");
					String tipo=stdIn.readLine();
					while(!tipo.equals("P")&&tipo.equals("M")){
						System.out.println("tipo inserito non valido");
						System.out.println("inserire tipo di stanza vuoi creare P=peer to peer, M=multicast");
						tipo=stdIn.readLine();
					}
					if (serverRMI.aggiungi_stanza(serviceName, 0) == 0)
						System.out.println(
								"Creazione di " + nomeStanza + " con tipo " + tipo +"è andata buonfine");
					else
						System.out.println("Creazione di " + nomeStanza + " con tipo " + tipo +"NON è andata buonfine");
				} // A

				else if (service.equals("E")) {
					boolean ok = false;
					String nomeU;
					System.out.print("inserire nome utente da eliminare");
					nomeU=stdIn.readLine();
					Stanza [] stanza;
					stanza=serverRMI.elimina_utente(service);
					for (Stanza stanza2 : stanza) {
						if(!stanza2.nomeStanza.equals("L"))
						stanza2.stampaStanza();
					}
				} // E

				else
					System.out.println("Servizio non disponibile");

				System.out.print("Servizio (R=Registrazione, P=Programma del congresso): ");
			} // while (!EOF), fine richieste utente

		} catch (NotBoundException nbe) {
			System.err.println("ClientRMI: il nome fornito non risulta registrato; " + nbe.getMessage());
			nbe.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			System.err.println("ClientRMI: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}