import java.io.*;
import java.rmi.*;

class ClientCongress {
    private static String constructCompleteName(String host, int port, String serviceName) {
        return "//" + host + ":" + port + "/" + serviceName;
    }

    public static void main(String[] args) {
        final int REGISTRYPORT = 1099;// cuz we need get Remote Object from registery
        String registryHost = null; // host remoto con registry
        String serviceName = "";
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        if (args.length != 1) {
            System.out.println("Usage: RMI_Registry_IP");
            System.exit(1);
        }
        registryHost = args[0];
        serviceName = "ServerCongress";

        System.out.println("Connecting to : <" + registryHost + "> \nservice name : <" + serviceName + ">");
        String completeName = constructCompleteName(registryHost, REGISTRYPORT, serviceName);
        try {
            ServerCongress serverRMI = (ServerCongress) Naming.lookup(completeName);// need cast cuz lookup() returns a
                                                                                    // // generic Remote Object
            System.out.println("Service <" + completeName + "> connected");

            System.out.println("Service (R=Register to congresso, P=Program del congresso),EOF to end ");
            String in;
            while ((in = stdIn.readLine()) != null) {

                if (in.equals("R")) {
                    int gg;
                    String session;
                    String speakerName;
                    boolean valid = false;

                    // get DAY
                    do {
                        System.out.println("Insert <Day>");
                        String day = stdIn.readLine();
                        gg = Integer.parseInt(day);
                        if (gg < 1 || gg > 3) {
                            System.out.println("Day non available");
                            continue;
                        } else
                            valid = true;
                    } while (!valid);

                    // get SESSION
                    valid = false;
                    do {
                        System.out.println("Insert <Session> (<S1>,<S2>,...)");
                        session = stdIn.readLine();
                        if (!session.equals("S1") && !session.equals("S2") && !session.equals("S3")
                                && !session.equals("S4")
                                && !session.equals("S5") && !session.equals("S6") && !session.equals("S7")
                                && !session.equals("S8") && !session.equals("S9") && !session.equals("S10")
                                && !session.equals("S11") && !session.equals("S12")) {
                            System.out.println("Session non available");
                            continue;
                        } else
                            valid = true;
                    } while (!valid);

                    // get SPEAKER NAME
                    System.out.println("Insert <Speaker Name>");
                    speakerName = stdIn.readLine();

                    // call Remote Service
                    int result = serverRMI.register(gg, session, speakerName);
                    if (result == 0)
                        System.out.println("Success");
                    else
                        System.out.println("Failed");

                } // service == R
                else if (in.equals("P")) {
                    int gg;
                    boolean valid = false;

                    // get DAY
                    do {
                        System.out.println("Insert <Day>");
                        String day = stdIn.readLine();
                        gg = Integer.parseInt(day);
                        if (gg < 1 || gg > 3) {
                            System.out.println("Day non available");
                            continue;
                        } else
                            valid = true;
                    } while (!valid);
                    System.out.println("");
                    serverRMI.getProgram(gg).print();
                } // service == P
                else {
                    System.out.println("This service is not available");
                }

                System.out.println("Service (R=Register to congresso, P=Program del congresso),EOF to end ");
            } // while stdIN
        } catch (NotBoundException nbe) {
            System.err.println("ClientRMI: No found this service; " + nbe.getMessage());
            nbe.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("ClientRMI: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

    }
}