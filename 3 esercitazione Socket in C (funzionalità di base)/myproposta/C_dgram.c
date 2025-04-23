// args: server_addr, server_port
/*
TARGET:
Client is a filter
ask client FILENAME(remote)
send to server FILENAME
receive form server the LENGTH of longest word
print LENGTH or ERROR MSG
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

int main(int argc, char const **argv)
{
    // init vars--------------------------------------------------
    struct hostent *host;
    struct sockaddr_in clientaddr, serveraddr;
    int port;

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
    clientaddr.sin_port = 0;

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
    printf("Client: bind socket ok\n");

    /* CORPO DEL CLIENT: ciclo di accettazione di richieste da utente -----------------*/
    printf("\nInsert filename or EOF to terminate: ");
    int nread;
    char filename[FILENAME_MAX + 1];
    while (gets(filename))
    {
        printf("Filename: %s\n", filename);
        // send request to server
        int len = sizeof(serveraddr);
        if (sendto(sd, &filename, sizeof(filename), 0, (struct sockaddr_in *)&serveraddr, len) < 0)
        {
            perror("sendto");
            printf("\nInsert filename or EOF to terminate: ");
            continue;
        }

        // receive response from server
        printf("Attesa del risultato...\n");
        int ris;
        if (recvfrom(sd, &ris, sizeof(ris), 0, (struct sockaddr_in *)&serveraddr, &len) < 0)
        {
            perror("recvfrom");
            printf("\nInsert filename or EOF to terminate: ");
            continue;
        }
        // ris is number
        ris = ntohl(ris);
        // print res
        if (ris == -1)
        {
            printf("Received an ERROR MESSAGE\n");
        }
        else
            printf("Risultato ricevuto: %d\n", ris);

        // restart
        printf("\nInsert filename or EOF to terminate: ");

    } // while
    
    //  clean and exit
    close(sd);
    printf("\nClient: termino...\n");
    exit(0);
} // main
