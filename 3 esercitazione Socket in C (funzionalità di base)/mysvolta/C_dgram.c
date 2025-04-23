// args: server_addr, server_port
/*
TARGET:
ask cliente OPERATION
send OPERATION to server
receive RESULT
print RESULT
*/
#include <fcntl.h>
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

// Struct of request
/********************************************************/
typedef struct
{
    int op1;
    int op2;
    char op;
} Req;
/********************************************************/

int main(int argc, char const **argv)
{
    // init vars--------------------------------------------------
    struct hostent *host;
    struct sockaddr_in clientaddr, serveraddr;
    int port;
    Req req;

    // args control------------------------------------------------
    if (argc != 3)
    {
        printf("Error args!");
        printf("\nUsage: %s server_addr, server_port\n", argv[0]);
        exit(1);
    }
    // verify host(addr)
    host = gethostbyname(argv[1]);
    if (host == NULL)
    {
        printf("%s not found in /etc/hosts\n", argv[1]);
        exit(2);
    }
    // verify port
    int counter = 0;
    while (argv[2][counter] != '\0')
    {
        if (argv[2][counter] < '0' || argv[2][counter] > '9')
        {
            printf("Error args!");
            printf("\nUsage: %s server_addr, server_port\n", argv[0]);
            printf("PORT: %s is not number", argv[2]);
            exit(2);
        }
        counter++;
    }
    port = atoi(argv[2]);
    if (port < 1024 || port > 65535)
    {
        printf("Error args!");
        printf("\nUsage: %s server_addr, server_port\n", argv[0]);
        printf("PORT: %d is not a valid port", port);
        exit(2);
    }
    // INIZIALIZZAZIONE INDIRIZZO CLIENT E SERVER and sets
    memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
    clientaddr.sin_family = AF_INET;
    clientaddr.sin_addr.s_addr = INADDR_ANY;
    clientaddr.sin_port =0;

    memset((char *)&serveraddr, 0, sizeof(struct sockaddr_in));
    serveraddr.sin_family = AF_INET;
    serveraddr.sin_addr.s_addr = ((struct in_addr *)host->h_addr)->s_addr;
    serveraddr.sin_port = htons(port);

    // create and bind datagram socket--------------------------------------------------------
    int sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0)
    {
        perror("apertura socket");
        exit(3);
    }
    printf("Client: creata la socket sd=%d\n", sd);
    // BIND SOCKET, a una porta scelta dal sistema
    if (bind(sd, (struct sockaddr_in *)&clientaddr, sizeof(clientaddr)) < 0)
    {
        perror("bind socket ");
        exit(3);
    }
    printf("Client: bind socket ok");

    /* CORPO DEL CLIENT: ciclo di accettazione di richieste da utente -----------------*/
    printf("\nPrimo operando (intero), EOF per terminare: ");
    int op1, op2, nread;
    char op;
    while ((nread = scanf("%i", &op1)) != EOF)
    {
        if (nread != 1)
        {
            do
            {
                /* Problema nell'implementazione della scanf. Se l'input contiene PRIMA
                 * dell'intero altri caratteri la testina di lettura si blocca sul primo carattere
                 * (non intero) letto. Ad esempio: ab1292\n
                 *				  ^     La testina si blocca qui
                 * Bisogna quindi consumare tutto il buffer in modo da sbloccare la testina.
                 */
            } while (getchar() != '\n');
            printf("\nPrimo operando (intero), EOF per terminare: ");
            continue;
        }

        req.op1 = htonl(op1);
        char buf256[256];
        gets(buf256); // consuma '\n' ed evetuale altri caratteri

        printf("Inserire secondo operando (intero): ");
        while (scanf("%i", &op2) != 1)
        {
            while (getchar() != '\n')
                ;
            printf("Inserire secondo operando (intero): ");
            continue;
        }
        req.op2 = htonl(op2);
        gets(buf256); // consumo resto linea

        do
        {
            printf("Operazione (+ = addizione, - = sottrazione, * = moltiplicazione, / = "
                   "divisione): ");
            op = getchar();
        } while (op != '+' && op != '-' && op != '*' && op != '/');
        req.op = op;
        gets(buf256); // consumo resto linea

        printf("Operazione richiesta: %d %c %d \n", ntohl(req.op1), req.op, ntohl(req.op2));

        // send request to server
        int len = sizeof(serveraddr);
        if (sendto(sd, &req, sizeof(Req), 0, (struct sockaddr_in *)&serveraddr, len) < 0)
        {
            perror("sendto");
            printf("\nPrimo operando (intero), EOF per terminare: ");
            continue;
        }

        // receive response from server
        printf("Attesa del risultato...\n");
        int ris;
        // l'unica diff rispetto sendto() e' qui si mette &len per ricevere len di serveraddr
        if (recvfrom(sd, &ris, sizeof(ris), 0, (struct sockaddr_in *)&serveraddr, &len) < 0)
        {
            perror("recvfrom");
            printf("\nPrimo operando (intero), EOF per terminare: ");
            continue;
        }

        // print res
        printf("Esito dell'operazione: %d\n", ntohl(ris));
        printf("\nPrimo operando (intero), EOF per terminare: ");
    } // while nread!=EOF

    // clean and exit
    close(sd);
    printf("\nClient: termino...\n");
    exit(0);
} // main
