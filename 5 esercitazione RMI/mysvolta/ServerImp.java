import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerImp extends UnicastRemoteObject implements Server {

	private static final long serialVersionUID = 1L;
	static Programma prog;

	public ServerImp() throws RemoteException {
		super();
	}

	@Override
	public int register(int day, int session, String speaker) throws RemoteException {
		System.out.println("Server RMI: richiesta registrazione con parametri");
		System.out.println("giorno   = " + day);
		System.out.println("sessione = " + session);
		System.out.println("speaker  = " + speaker);
		return prog.register(day, session, speaker);
	}

	@Override
	public Programma getProgramma() throws RemoteException {
		return prog;
	}

	public static void main(String[] args) {
		prog = new Programma();
		final int registryPort = 1099; //default rmiregistry port
		String registryHost = "localhost";
		String serviceName = "Server"; // lookup name...

		// Registrazione del servizio RMI
		String completeName = "//" + registryHost + ":" + registryPort + "/" + serviceName;
		try {
			ServerImp sImp = new ServerImp();
			Naming.rebind(completeName, sImp);
			System.err.println("Server RMI servizio \"" + serviceName + "\" -> Registrato in rmiregistry");
		} catch (Exception e) {
			System.err.println("Server RMI \"" + serviceName + "\": " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
