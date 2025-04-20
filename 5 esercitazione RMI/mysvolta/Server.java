
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {

	int register(int day, int session, String speaker) throws RemoteException;

	Programma getProgramma() throws RemoteException;
}
