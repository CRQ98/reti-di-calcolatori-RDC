// args: server_addr, server_port
/*
TARGET:
ask user FILENAME
send FILE to server
receive FILE_sorted
print FILE_sorted
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
    printf("Ciclo di richieste di ordinamento fino a EOF\n");
    printf("\nNome del file da ordinare, EOF per terminare: ");

    char filename[FILENAME_MAX + 1];
    while (gets(filename))
    {
        printf("File da ordinare: %s\n", filename);

        // open file
        int fd = open(filename, O_RDONLY);
        if (fd < 0)
        {
            perror("open file sorgente");
            printf("\nNome del file da ordinare, EOF per terminare: ");
            continue;
        }

        // open socket
        int sd = socket(AF_INET, SOCK_STREAM, 0);
        if (sd < 0)
        {
            perror("apertura socket");
            exit(1);
        }
        printf("Client: creata la socket sd= %d\n", sd);

        // bind socket
        /* Operazione di BIND implicita nella connect */
        if (connect(sd, (struct sockaddr_in *)&serveraddr, sizeof(serveraddr)) < 0)
        {
            perror("connect");
            exit(1);
        }
        printf("Client: connect ok\n");
        printf("Client: stampo e invio file da ordinare\n");

        // send file
        int nread;
        char buf[DIM_BUFF];
        while ((nread = read(fd, buf, DIM_BUFF)) > 0)
        {
            write(stdout, buf, nread);
            write(sd, buf, nread);
        }
        close(fd);
        printf("Client: file inviato\n");

        /* Chiusura socket in spedizione -> invio dell'EOF */
        shutdown(sd, 1);

        /*RICEZIONE File*/
        printf("Client: ricevo e stampo file ordinato\n");
        int fd_sorted;
        strcat(filename,"_sorted");
        fd_sorted = open(filename, O_WRONLY | O_CREAT, 0664);
        if (fd_sorted < 0)
        {
            perror("open file destinatario");
            printf("\nNome del file da ordinare, EOF per terminare: ");
            continue;
        }

        while ((nread = read(sd, buf, DIM_BUFF)))
        {
            write(1, buf, nread);
            write(fd_sorted, buf, nread);
        }
        printf("Client: Traspefimento terminato\n");
        printf("Client: File ordinato viene denominato: %s\n",filename);
        /* Chiusura socket in ricezione */
        shutdown(sd, 0);

        // close all resource
        close(fd_sorted);
        close(sd);

        // restart
        printf("\nNome del file da ordinare, EOF per terminare: ");
    } // while
    printf("\nClient: termino...\n");
    exit(0);
} // main
