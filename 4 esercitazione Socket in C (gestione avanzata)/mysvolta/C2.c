// args: server_addr, server_port
/*
TARGET:
adk FILENAME
send GET request to server(stream)
receive FILE from server
*/
#include <netdb.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>
#define DIM_BUFF 4096

int main(int argc, char const *argv[])
{
    // init vars--------------------------------------------------
    struct hostent *host;
    struct sockaddr_in serveraddr;
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
    // INIZIALIZZAZIONE INDIRIZZO SERVER and sets
    memset((char *)&serveraddr, 0, sizeof(struct sockaddr_in));
    serveraddr.sin_family = AF_INET;
    serveraddr.sin_addr.s_addr = ((struct in_addr *)host->h_addr)->s_addr;
    serveraddr.sin_port = htons(port);

    /* CORPO DEL CLIENT: ciclo di accettazione di richieste da utente ------- */
    printf("Client: Ciclo di richieste fino a EOF\n");
    printf("\nNome del file oppure EOF per terminare: ");
    char filename[FILENAME_MAX + 1];
    while (gets(filename))
    {
        // open socket
        int sd = socket(AF_INET, SOCK_STREAM, 0);
        if (sd < 0)
        {
            perror("apertura socket");
            exit(1);
        }
        printf("Client: creata la socket sd= %d\n", sd);

        // connction
        /* Operazione di BIND implicita nella connect */
        if (connect(sd, (struct sockaddr_in *)&serveraddr, sizeof(serveraddr)) < 0)
        {
            perror("connect");
            exit(1);
        }
        printf("Client: connect ok\n");

        // process
        printf("Client: File name: %s\n", filename);
        write(sd, &filename, sizeof(filename));
        printf("Client: Inviata la richiesta di file\n");
        printf("Client: ricevo e stampo file ...\n\n");
        /*RICEZIONE File*/
        int nread;
        char buf[DIM_BUFF];
        while ((nread = read(sd, buf, sizeof(buf))) > 0)
        {
            write(1, buf, nread);
        }
        
        if (nread < 0)
        {
            perror("Errore lettura file dal server");
            printf("\nNome del file oppure EOF per terminare: ");
            continue;
        }
        printf("\n\nClient: Ricezione terminato\n");

        // close socket
        printf("Client: Closing connection\n");
        close(sd);

        // restart
        printf("\nNome del file oppure EOF per terminare: ");
    } // while
      // close resource

} // main
