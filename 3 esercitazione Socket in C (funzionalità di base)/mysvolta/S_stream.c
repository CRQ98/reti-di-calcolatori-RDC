// args: server_port
/*
TARGET:
receive FILE
sort FILE -> FILE_sorted
send back FILE_sorted
*/
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
#define DIM_BUFF 4096

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

    // create and bind socket--------------------------------------------------------
    int listen_sd = socket(AF_INET, SOCK_STREAM, 0);
    if (listen_sd < 0)
    {
        perror("apertura socket");
        exit(3);
    }
    printf("Server: creata la socket sd=%d\n", listen_sd);
    // set reuse addr
    int const on = 1;
    if (setsockopt(listen_sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
    {
        perror("set opzioni socket ");
        exit(3);
    }
    printf("Server: set opzioni socket ok\n");
    // BIND SOCKET, a una porta scelta dal sistema
    if (bind(listen_sd, (struct sockaddr_in *)&serveraddr, sizeof(serveraddr)) < 0)
    {
        perror("bind socket d'ascolto");
        exit(3);
    }
    printf("Server: bind socket d'ascolto ok, alla porta %i\n", ntohs(serveraddr.sin_port));

    if (listen(listen_sd, 5) < 0) // create code
    {
        perror("listen");
        exit(1);
    }
    printf("Server: listen ok\n");

    /* AGGANCIO GESTORE PER EVITARE FIGLI ZOMBIE,
     * Quali altre primitive potrei usare? E' portabile su tutti i sistemi?
     * Pregi/Difetti?
     * Alcune risposte le potete trovare nel materiale aggiuntivo!
     */
    signal(SIGCHLD, gestore);

    /* CORPO DEL server: ciclo di ricezione di richieste da utente -----------------*/
    int len;
    int conn_sd;
    while (1)
    {
        printf("\nWaiting for a new connection\n");

        // accept connection
        len = sizeof(clientaddr);
        if ((conn_sd = accept(listen_sd, (struct sockaddr_in *)&clientaddr, &len)) < 0)
        {
            /* La accept puo' essere interrotta dai segnali inviati dai figli alla loro
             * teminazione. Tale situazione va gestita opportunamente. Vedere nel man a cosa
             * corrisponde la costante EINTR!*/
            if (errno == EINTR)
            {
                perror("Forzo la continuazione della accept");
                continue;
            }
            else
                exit(1);
        }

        // fork
        if (fork() == 0)
        {
            // figlio
            /*Chiusura FileDescr non utilizzati e ridirezione STDIN/STDOUT*/
            close(listen_sd);
            host = gethostbyaddr((char *)&clientaddr.sin_addr, sizeof(clientaddr.sin_addr), AF_INET);
            if (host == NULL)
            {
                printf("Server (figlio): client host information not found\n");
            }
            else
            {
                printf("Server (figlio): Operazione richiesta da: %s \tport: %i\n", host->h_name,
                       ntohs(clientaddr.sin_port));
            }
            /* PROCESS REQUEST ------------------------------------------------------------------------- */
            printf("Server (figlio): eseguo l'ordinamento\n");
            close(1);
            close(0);
            dup(conn_sd);
            dup(conn_sd);
            execl("/usr/bin/sort", "sort", (char *)0);
            //                             (char *)0 means last arg its here 
            
            // Chiudo la socket
            close(conn_sd);
            exit(0);
        }
        else
        {
            // padre
            close(conn_sd);
        }
    }//while
}//main
