import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

//args: RMI_Registry_IP ServiceName
/*
 * TARGET:
 * ask user which SERVICE
 * 		SERVICE: register, getProgramma
 * use REMOTE OBJ to get service
 */
public class Client {

	public static void main(String[] args) {
		final int port = 1099; //default rmiregistry port
		String registryHost = null; // host remoto con registry
		String serviceName = "";
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

		// Control args
		if (args.length != 2) {
			System.out.println();
			System.out.println("Usage: RMI_Registry_IP ServiceName");
			System.exit(1);
		}
		registryHost = args[0];
		serviceName = args[1];

		System.out.println("Invio richieste a " + registryHost + " per il servizio di nome " + serviceName);
		String completeName = "//" + registryHost + ":" + port + "/" + serviceName;
		try {
			// get remote obj
			Server serverRMI = (Server) Naming.lookup(completeName);
			System.out.println("ClientRMI: Servizio \"" + serviceName + "\" connesso");

			// process
			System.out.println("\nRichieste di servizio fino a EOF");
			String req;
			System.out.println("Servizio (R=Registrazione, P=Programma del congresso): ");
			int day, session;
			String speaker;
			while ((req = stdIn.readLine()) != null) {
				// register
				if (req.equals("R")) {
					System.out.println("Giorno(1-3)?");
					day = Integer.parseInt(stdIn.readLine());
					if (day < 1 || day > 3) {
						System.out.println("Giorno non valido");
						System.out.println("\nServizio (R=Registrazione, P=Programma del congresso): ");
						continue;
					}
					System.out.println("Sessione(1-12)?");
					session = Integer.parseInt(stdIn.readLine());
					if (session < 1 || session > 12) {
						System.out.println("Sessione non valido");
						System.out.println("\nServizio (R=Registrazione, P=Programma del congresso): ");
						continue;
					}
					System.out.println("Speaker?");
					speaker = stdIn.readLine();
					System.out.println(
						"\nRichiesta registrazione SPEAKER: " +
						speaker +
						" in GIORNO: " +
						day +
						" nel SESSIONE: " +
						session
					);

					if (serverRMI.register(day, session, speaker) == 0) {
						System.out.println("Registrazione con successo");
					} else {
						System.out.println("Registrazione NON successo");
					}
				} else // getProgramma
				if (req.equals("P")) {
					System.out.println(
						"PROGRAMMA ------------------------------------------------------------\n"
					);
					serverRMI.getProgramma().toPrint();
					System.out.println(
						"THE END --------------------------------------------------------------\n"
					);
				} else {
					System.out.println("Service not available");
				}
				System.out.println("\nServizio (R=Registrazione, P=Programma del congresso): ");
			}
		} catch (MalformedURLException e) {
			System.out.println("ClientRMI: Bad Url");
			e.printStackTrace();
			System.exit(2);
		} catch (RemoteException e) {
			System.err.println("ClientRMI: " + e.getMessage());
			e.printStackTrace();
			System.exit(2);
		} catch (NotBoundException e) {
			System.err.println("ClientRMI: il nome fornito non risulta registrato; " + e.getMessage());
			e.printStackTrace();
			System.exit(2);
		} catch (Exception e) {
			System.err.println("ClientRMI: Other exception" + e.getMessage());
			e.printStackTrace();
			System.exit(2);
		}
		System.exit(0);
	}
}
