// args: server_port
/*
TARGET:
receive OPERATION
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

int main(int argc, char const *argv[])
{
    // init vars--------------------------------------------------
    struct hostent *clienthost;
    struct sockaddr_in clientaddr, serveraddr;
    int port;
    Req req;

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
    printf("Server: bind socket ok, alla porta %i\n", ntohs(serveraddr.sin_port) );

    /* CORPO DEL server: ciclo di ricezione di richieste da utente -----------------*/
    while (1)
    {
        printf("\nWaiting for a new request\n");
        int len = sizeof(struct sockaddr_in);
        if (recvfrom(sd, &req, sizeof(Req), 0, (struct sockaddr_in *)&clientaddr, &len)<0)
        {
            perror("recvfrom");
            continue;
        }
        // get data
        int op1, op2;
        char op;
        op1 = ntohl(req.op1);
        op2 = ntohl(req.op2);
        op = req.op;
        printf("Operazione richiesta: %i %c %i\n", op1, req.op, op2);
        clienthost = gethostbyaddr((char *)&clientaddr.sin_addr, sizeof(clientaddr.sin_addr), AF_INET);
        if (clienthost == NULL)
        {
            printf("client host information not found\n");
        }
        else
        {
            printf("Operazione richiesta da: %s \tport: %i\n", clienthost->h_name,
                   ntohs(clientaddr.sin_port));
        }
        // process
        int res;
        switch (op)
        {
        case '+':
            res = op1 + op2;
            break;
        case '-':
            res = op1 - op2;
            break;
        case '*':
            res = op1 * op2;
            break;
        case '/':
            if (op2 != 0)
            {
                res = op1 / op2;
            }
            else
            {
                printf("OP2 equals ZERO\n");
                res = 0;
            }
            break;
        default:
            printf("Operation request is NOT valid");
            res = 0;
            break;
        } // switch
        // send response
        printf("Send back result: %d\n",res);
        res=htonl(res);
        if (sendto(sd, &res, sizeof(res), 0, (struct sockaddr_in *)&clientaddr, len) < 0)
        {
            perror("sendto");
            continue;
        }
    } // while
} // main
