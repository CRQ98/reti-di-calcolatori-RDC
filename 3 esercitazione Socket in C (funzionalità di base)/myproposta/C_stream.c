// args: server_addr, server_port
/*
TARGET:
ask client FILENAME
ask client NLINE to eliminate
send to server NLINE
send FILE
receive FILE
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

    /* CORPO DEL CLIENT: ciclo di accettazione di richieste da utente ------- */
    printf("\nClient: Ciclo di richieste fino a EOF\n");
    printf("Nome del file oppure EOF per terminare: ");

    char filename[FILENAME_MAX + 1];
    char buf[DIM_BUFF];
    while (gets(filename))
    {
        printf("Client: File name: %s\n", filename);
        // open file
        int fd = open(filename, O_RDONLY);
        if (fd < 0)
        {
            perror("open file sorgente");
            printf("\nNome del file oppure EOF per terminare: ");
            continue;
        }
        // ask nline
        printf("Inserire numero di linea da eliminare: ");
        int nline;
        while (scanf("%d", &nline) != 1)
        {
            while (getchar() != '\n')
                ;
            printf("\nInserire numero di linea da eliminare: ");

            continue;
        }
        gets(buf);
        printf("Client: Numero di linea da eliminare: %d\n", nline);

        // send to server nline
        write(sd, &nline, sizeof(nline));

        /*send file*/
        int nread;
        char c;
        // in questo caso posso usare buff per velocizzare il trasferimento
        while ((nread = read(fd, buf, DIM_BUFF)) > 0)
        {
            write(sd, buf, nread);
        }
        // close resource
        close(fd);

        //protocoll EOF
        char eof='\0';
        write(sd, &eof, 1);
        printf("Client: file inviato\n");

        // prepare file to save
        printf("Client: ricevo e stampo file ...\n\n");
        int fd_mod;
        strcat(filename, "_mod");
        fd_mod = open(filename, O_WRONLY | O_CREAT | O_TRUNC, 0664);
        if (fd_mod < 0)
        {
            perror("open file destinatario");
            printf("\nNome del file oppure EOF per terminare: ");
            continue;
        }
        /*RICEZIONE File*/
        while ((nread = read(sd, &c, 1)) > 0)
        {
            if (c != '\0')
            {
                write(1, &c, nread);
                write(fd_mod, &c, nread);
            }
            else
                break;
        }
        if (nread < 0)
        {
            perror("Errore lettura file dal server");
            printf("\nNome del file oppure EOF per terminare: ");
            continue;
        }
        
        printf("\n\nClient: Traspefimento terminato\n");
        printf("Client: File modificato viene denominato: %s\n", filename);
        // close resource
        close(fd_mod);

        // restart
        printf("\nNome del file oppure EOF per terminare: ");

    } // while
    printf("\nClient: termino...\n");
    exit(0);
} // main
