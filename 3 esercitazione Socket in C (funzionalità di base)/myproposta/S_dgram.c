// args: server_port
/*
TARGET:
receive FILENAME
process
send RESULT
*/
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <fcntl.h>
#include <unistd.h>

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
    int sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0)
    {
        perror("apertura socket");
        exit(3);
    }
    printf("Server: creata la socket sd=%d\n", sd);
    // set reuse addr
    int const on = 1;
    if (setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
    {
        perror("set opzioni socket ");
        exit(3);
    }
    printf("Server: set opzioni socket ok\n");
    // BIND SOCKET, a una porta scelta dal sistema
    if (bind(sd, (struct sockaddr_in *)&serveraddr, sizeof(serveraddr)) < 0)
    {
        perror("bind socket ");
        exit(3);
    }
    printf("Server: bind socket ok, alla porta %i\n", ntohs(serveraddr.sin_port));

    /* CORPO DEL server: ciclo di ricezione di richieste da utente -----------------*/

    while (1)
    {
        printf("\nWaiting for a new request\n");
        char filename[FILENAME_MAX + 1];
        int len = sizeof(struct sockaddr_in);
        if (recvfrom(sd, &filename, sizeof(filename), 0, (struct sockaddr_in *)&clientaddr, &len) < 0)
        {
            perror("recvfrom");
            continue;
        }
        printf("Filename: %s\n", filename);
        int fd = open(filename, O_RDONLY);
        int res;
        int wordcount = 0, longest = 0, nread;
        if (fd < 0)
        {
            perror("open file sorgente");
            res = -1;
        }
        else
        {
            /* ************************************** */
            // PROCESS
            char c;
            while ((nread = read(fd, &c, 1)) != 0)
            {
                if (nread < 0)
                {
                    printf("(PID %d) impossibile leggere dal file\n", getpid());
                    perror("read file");
                    res = -1;
                    break;
                }

                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
                {
                    wordcount++;
                }
                else
                {
                    if (wordcount > longest)
                    {
                        longest = wordcount;
                    }
                    wordcount = 0;
                }
            }
            res = longest;
        } // else
        printf("Send back result: %d\n", res);
        
        // send back response
        res = htonl(res);
        if (sendto(sd, &res, sizeof(res), 0, (struct sockaddr_in *)&clientaddr, len) < 0)
        {
            perror("sendto");
            continue;
        }
    } // while 1

    exit(0);
}

