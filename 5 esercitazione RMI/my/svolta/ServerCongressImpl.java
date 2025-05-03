import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class ServerCongressImpl extends UnicastRemoteObject implements ServerCongress {
    static Program prog[];

    public ServerCongressImpl() throws RemoteException {
        super();
    }

    private void controlDayValidity(int gg) throws RemoteException {
        if (gg < 1 || gg > 3)
            throw new RemoteException("Invalid day value. Day must be between 1 and 3.");

    }

    public int register(int gg, String session, String speaker) throws RemoteException {
        int sess = -1;
        System.out.println("Server RMI: richiesta registrazione con parametri");
        System.out.println("giorno   = " + gg);
        System.out.println("sessione = " + session);
        System.out.println("speaker  = " + speaker);
        controlDayValidity(gg);

        if (session.equals("S1"))
            sess = 0;
        if (session.equals("S2"))
            sess = 1;
        if (session.equals("S3"))
            sess = 2;
        if (session.equals("S4"))
            sess = 3;
        if (session.equals("S5"))
            sess = 4;
        if (session.equals("S6"))
            sess = 5;
        if (session.equals("S7"))
            sess = 6;
        if (session.equals("S8"))
            sess = 7;
        if (session.equals("S9"))
            sess = 8;
        if (session.equals("S10"))
            sess = 9;
        if (session.equals("S11"))
            sess = 10;
        if (session.equals("S12"))
            sess = 11;

        if (sess == -1)
            throw new RemoteException();

        return prog[gg - 1].register(sess, speaker);
    }

    public Program getProgram(int gg) throws RemoteException {
        System.out.println("Server RMI: richiesto programma del giorno " + gg);
        controlDayValidity(gg);
        return prog[gg - 1];
    }

    public static void main(String[] args) {
        prog = new Program[3];
        for (int i = 0; i < 3; i++) {
            prog[i] = new Program();
        }
        final int REGISTRYPORT = 1099;
        String registryHost = "localhost";
        String serviceName = "ServerCongress";
        System.out.println("Server RMI: Started");

        try {
            ServerCongressImpl serverRMI = new ServerCongressImpl();
            String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/" + serviceName;
            Naming.rebind(completeName, serverRMI);
            System.out.println("Server RMI: Service <" + completeName + "> registed");
        } catch (Exception e) {
            System.err.println("Server RMI \"" + serviceName + "\": " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}