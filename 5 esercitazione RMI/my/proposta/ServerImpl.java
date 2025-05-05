import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;

public class ServerImpl extends UnicastRemoteObject implements RemOp {
    public ServerImpl() throws RemoteException {
        super();
    }

    @Override
    public int conta_righe(String filename, int nWord) throws RemoteException {
        System.out.println("Request <conta_righe>, filename: <" + filename + ">, number of line: <" + nWord + ">");
        String line;
        String words[];
        BufferedReader br;
        int nLine = 0;
        try {
            br = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException fnfe) {
            throw new RemoteException(fnfe.toString());
        }

        try {
            while ((line = br.readLine()) != null) {
                words = line.split("\\s+");
                if (words.length > nWord)
                    nLine++;
            }
            br.close();
        } catch (Exception e) {
            throw new RemoteException(e.toString());
        }
        System.out.println("Found <" + nLine + "> lines exceeding maximum words");
        return nLine;
    }

    @Override
    public int elimina_riga(String filename, int nLine) throws RemoteException {
        System.out.println("Request <elimina_riga>, filename: <" + filename + ">, number of line: <" + nLine + ">");
        String line;
        BufferedReader br;
        BufferedWriter bw;
        int result = 1;
        int lineCount = 0;
        try {
            br = new BufferedReader(new FileReader(filename));
            bw = new BufferedWriter(new FileWriter("temp_" + filename));
            while ((line = br.readLine()) != null) {
                lineCount++;
                if (lineCount != nLine)
                    bw.write(line + "\n");
                else {
                    System.out.println("Eliminated line : " + nLine);
                    System.out.println(line);
                    result = 0;
                }
            }
            br.close();
            bw.close();
            new File(filename).delete();
            new File("temp_" + filename).renameTo(new File(filename));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException();
        }
    }

    public static void main(String[] args) {
        final int REGISTRYPORT = 1099;
        String registryHost = "localhost";
        String serviceName = "RemOp";
        System.out.println("Server RMI: Started");

        try {
            ServerImpl serverRMI = new ServerImpl();
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