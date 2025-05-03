import java.rmi.Remote;
import java.rmi.RemoteException;
public interface ServerCongress extends Remote{
    int register (int gg,String session,String speaker) throws RemoteException;
    Program getProgram(int gg) throws RemoteException;
}