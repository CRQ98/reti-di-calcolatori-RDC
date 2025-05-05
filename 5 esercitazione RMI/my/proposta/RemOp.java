import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemOp extends Remote {
    int conta_righe(String filename, int nWord) throws RemoteException;

    int elimina_riga(String filename, int nLine) throws RemoteException;
}