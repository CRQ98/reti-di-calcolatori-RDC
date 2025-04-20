// args: server_port

#include <errno.h>
#include <dirent.h>
#include <netdb.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <stdio.h>
#include <signal.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <sys/select.h>
#define max(a, b) ((a) > (b) ? (a) : (b))
#define DIM_BUFF 4096
/*Funzione conteggio file in un direttorio*/
/********************************************************/
int count_file_in_dir(char *name)
{
    DIR *dir;
    struct dirent *dd;
    int count = 0;
    dir = opendir(name);
    if (dir == NULL)
        return -1;
    while ((dd = readdir(dir)) != NULL)
    {
        printf("Trovato il file %s\n", dd->d_name);
        count++;
    }
    /*Conta anche direttorio stesso e padre*/
    printf("Numero totale di file %d\n", count);
    closedir(dir);
    return count;
}
/********************************************************/
void gestore(int signo)
{
    int stato;
    printf("esecuzione gestore di SIGCHLD\n");
    wait(&stato);
}
/********************************************************/

int main(int argc, char const *argv[])
{
    // init vars--------------------------------------------------
    struct hostent *host;
    struct sockaddr_in clientaddr, serveraddr;
    int port;

    // args control------------------------------------------------
    if (argc != 2)
    {
        printf("Wrong quantity args!\n");
        printf("Usage: %s server_port\n", argv[0]);
        exit(1);
    }
    // verify port
    int counter = 0;
    while (argv[1][counter] != '\0')
    {
        if (argv[1][counter] < '0' || argv[1][counter] > '9')
        {
            printf("PORT: %s is not number", argv[1]);
            printf("\nUsage: %s server_port\n", argv[0]);
            exit(1);
        }
        counter++;
    }
    port = atoi(argv[1]);
    if (port < 1024 || port > 65535)
    {
        printf("PORT: %d is not a valid port", port);
        printf("\nUsage: %s server_port\n", argv[0]);
        exit(1);
    }

    // INIZIALIZZAZIONE INDIRIZZO SERVER and sets
    memset((char *)&serveraddr, 0, sizeof(struct sockaddr_in));
    serveraddr.sin_family = AF_INET;
    serveraddr.sin_addr.s_addr = INADDR_ANY;
    serveraddr.sin_port = htons(port);

    // CREATE and bind STREAM SOCKET--------------------------------------------------------
    int tcpsd = socket(AF_INET, SOCK_STREAM, 0);
    if (tcpsd < 0)
    {
        perror("apertura socket tcp");
        exit(3);
    }
    printf("Server: creata la socket tcp tcpsd=%d\n", tcpsd);
    // set reuse addr
    int const on = 1;
    if (setsockopt(tcpsd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
    {
        perror("set opzioni socket tcp ");
        exit(3);
    }
    printf("Server: set opzioni socket tcp ok\n");
    // BIND SOCKET, a una porta scelta dal sistema
    if (bind(tcpsd, (struct sockaddr_in *)&serveraddr, sizeof(serveraddr)) < 0)
    {
        perror("bind socket d'ascolto");
        exit(3);
    }
    printf("Server: bind socket d'ascolto ok, alla porta %i\n", ntohs(serveraddr.sin_port));

    if (listen(tcpsd, 5) < 0) // create code
    {
        perror("listen");
        exit(1);
    }
    printf("Server: listen ok\n");

    // CREATE and bind DATAGRAM SOCKET--------------------------------------------------------
    int udpsd = socket(AF_INET, SOCK_DGRAM, 0);
    if (udpsd < 0)
    {
        perror("apertura socket udp");
        exit(3);
    }
    printf("Server: creata la socket udp udpsd=%d\n", udpsd);
    // set reuse addr
    if (setsockopt(udpsd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
    {
        perror("set opzioni socket udp ");
        exit(3);
    }
    printf("Server: set opzioni socket udp ok\n");
    // BIND SOCKET, a una porta scelta dal sistema
    if (bind(udpsd, (struct sockaddr_in *)&serveraddr, sizeof(serveraddr)) < 0)
    {
        perror("bind socket udp");
        exit(3);
    }
    printf("Server: bindsocket  udp ok, alla porta %i\n", ntohs(serveraddr.sin_port));

    /* AGGANCIO GESTORE PER EVITARE FIGLI ZOMBIE*/
    signal(SIGCHLD, gestore);

    /* PULIZIA E SETTAGGIO MASCHERA DEI FILE DESCRIPTOR ------------------------- */
    fd_set rset;
    FD_ZERO(&rset);
    int nfd = max(tcpsd, udpsd) + 1;

    /* CICLO DI RICEZIONE EVENTI DALLA SELECT ----------------------------------- */
    int nread;
    int len = sizeof(struct sockaddr_in);
    int connsd;
    char filename[FILENAME_MAX];
    char buf[DIM_BUFF];
    int fd;
    while (1)
    {
        printf("\nServer: Waiting for a new request or connection\n");
        FD_SET(tcpsd, &rset);
        FD_SET(udpsd, &rset);

        // select
        nread = select(nfd, &rset, NULL, NULL, NULL);
        if (nread < 0)
        {
            if (errno == EINTR)
                continue;
            else
            {
                perror("select");
                exit(4);
            }
        }

        /* GESTIONE RICHIESTE TCP------------------------------------- */
        if (FD_ISSET(tcpsd, &rset))
        {
            printf("Ricevuta richiesta da SOCK_STREAM\n");
            connsd = accept(tcpsd, (struct sockaddr *)&clientaddr, &len);
            if (connsd < 0)
            {
                if (errno == EINTR)
                    continue;
                else
                {
                    perror("accept");
                    exit(5);
                }
            }
            // GET
            if (fork() == 0)
            {
                close(tcpsd);
                // get client host info
                host = gethostbyaddr((char *)&clientaddr.sin_addr, sizeof(clientaddr.sin_addr), AF_INET);
                if (host == NULL)
                {
                    printf("Server (figlio): client host information not found\n");
                }
                else
                {
                    printf("Server (figlio): Operazione richiesta da: \t%s \tport: %i\n", host->h_name,
                           ntohs(clientaddr.sin_port));
                }
                // process
                if (read(connsd, filename, sizeof(filename)) <= 0)
                {
                    perror("TCP read");
                    break;
                }
                shutdown(connsd, 0);
                printf("Richiesto file %s\n", filename);
                fd = open(filename, O_RDONLY);
                if (fd < 0)
                {
                    printf("File inesistente\n");
                    write(connsd, "N", 1);
                }
                else
                {
                    printf("Leggo e invio il file richiesto\n");
                    while ((nread = read(fd, buf, sizeof(buf))) > 0)
                    {
                        if (write(connsd, buf, nread) < 0)
                        {
                            perror("write");
                            break;
                        }
                    }
                    printf("Terminato invio file\n");
                    close(fd);
                }
                printf("Figlio %: termino\n", getpid());
                shutdown(connsd, 1);
                exit(0);
            }
            else
            {
                // padre
                close(connsd);
            }

        } // tcp

        /* GESTIONE RICHIESTE UDP ------------------------------------------ */
        if (FD_ISSET(udpsd, &rset))
        {
            printf("Ricevuta richiesta da SOCK_DGRAM\n");

            if (recvfrom(udpsd, filename, sizeof(filename), 0, (struct sockaddr *)&clientaddr, &len) <
                0)
            {
                perror("recvfrom");
                continue;
            }
            else
            {
                // get client host info
                host = gethostbyaddr((char *)&clientaddr.sin_addr, sizeof(clientaddr.sin_addr), AF_INET);
                if (host == NULL)
                {
                    printf("Server: client host information not found\n");
                }
                else
                {
                    printf("Server: Operazione richiesta da: \t%s \tport: %i\n", host->h_name,
                           ntohs(clientaddr.sin_port));
                }
            }
            printf("Richiesto conteggio dei file in %s\n", filename);
            int num = count_file_in_dir(filename);
            num = htonl(num);
            if (sendto(udpsd, &num, sizeof(int), 0, (struct sockaddr *)&clientaddr, len) < 0)
            {
                perror("sendto");
                continue;
            }
        } // udp

    } // while
    // never arrives here
    exit(0);
} // main

/*
TARGET:
offer 2 services : COUNT, GET
    COUNT: number of files in a dir (work with DGRAM)
    GET: send a FILE to client (work with STREAM)
*/