import java.io.*;
import java.rmi.*;

class Client {
    public static void main(String[] args) {
        int registryPort = 1099;

        // Controllo parametri
        if (args.length != 1 && args.length != 2) {
            System.out.println("Usage: Client RegistryHost [registryPort]");
            System.exit(1);
        } else {
            if (args.length == 2) {
                try {
                    registryPort = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    System.out.println("<registryPort> non valid");
                    System.exit(1);
                }
            }
        }
        String registryHost = args[0];
        String serviceName = "RemOp";
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        // Connessione al servizio RMI remoto
        System.out.println("Connecting to : <" + registryHost + "> \nservice name : <" + serviceName + ">");
        String completeName = "//" + registryHost + ":" + registryPort + "/" + serviceName;
        try {
            RemOp serverRMI = (RemOp) Naming.lookup(completeName);
            System.out.println("Service <" + completeName + "> connected");

            System.out.println("\nRequest for service, EOF to end");

            String service;
            System.out.print("Servizio (C=Conta, E=Elimina): ");

            while ((service = stdIn.readLine()) != null) {
                // service == Conta
                if (service.equals("C")) {
                    System.out.print("Enter <filename>:");
                    String filename = stdIn.readLine();

                    System.out.print("Enter max <nWords> per line:");
                    String num = stdIn.readLine();
                    int nWords;
                    try {
                        nWords = Integer.parseInt(num);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number format. Please enter an integer.");
                        System.out.print("Servizio (C=Conta, E=Elimina): ");
                        continue;
                    }
                    try {
                        int result = serverRMI.conta_righe(filename, nWords);
                        if (result < 0) {
                            System.out.println("Error: Invalid result from server.");
                        } else {
                            System.out.println("Found " + result + " line(s) exceeding the maximum number of words.");
                        }
                    } catch (RemoteException e) {
                        System.out.println("Remote call failed: " + e.getMessage());
                        e.printStackTrace();
                    }

                } // Conta
                  // service == Elimina
                else if (service.equals("E")) {
                    System.out.print("Enter <filename>:");
                    String filename = stdIn.readLine();

                    System.out.print("Enter <nLine> to eliminate from file:");
                    String num = stdIn.readLine();
                    int nLine;
                    try {
                        nLine = Integer.parseInt(num);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number format. Please enter an integer.");
                        System.out.print("Servizio (C=Conta, E=Elimina): ");
                        continue;
                    }

                    try {
                        int result = serverRMI.elimina_riga(filename, nLine);
                        if (result == 0)
                            System.out.println("The file doesnt cotain <" + nLine + "> line");
                        else if (result == 1)
                            System.out.println("Eliminated line <" + nLine + "> from file.");
                        else
                            System.out.println("Result unacceptable.");

                    } catch (RemoteException e) {
                        System.out.println("Remote call failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                } // Elimina
                else
                    System.out.println("Servizio non disponibile");
                System.out.print("Servizio (C=Conta, E=Elimina): ");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
