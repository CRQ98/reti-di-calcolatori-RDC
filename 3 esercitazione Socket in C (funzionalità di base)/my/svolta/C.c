
#include <netdb.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include "Myutils.h"

// def struct---------------------
typedef struct
{
    int op1;
    int op2;
    char op;
} Calc;

const char *ARGV0;
void prompt(char *s)
{
    printf("%s : ", ARGV0);
    printf("%s", s);
}

int main(int argc, char **argv)
{
    ARGV0 = argv[0];
    struct sockaddr_in clientaddr, servaddr;
    struct hostent *host;
    Calc cal;
    int nread, num;
    char c;
    int ris; 
    int len;
    int sd;
    char *output;

    // controll parameter number
    if (argc != 3)
    {
        prompt("");
        printf("Usage: %s <serverIP> <serverPort>\n", ARGV0);
        exit(1);
    }

    // get port form personalized function
    int port;
    if ((port = getportfromstring(argv[2])) == -1)
        exit(1);

    // get host. First from etc/hosts then DNS ...
    host = gethostbyname(argv[1]);
    if (host == NULL)
    {

        prompt("Host Not Founded\n");
        exit(2);
    }

    // clear mem of server address
    // 测试没有char*
    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr; /*h_addr is h_addr_list[0] */
    servaddr.sin_port = htons(port);

    // clear mem of client address
    // 测试没有char*
    memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
    clientaddr.sin_family = AF_INET;
    clientaddr.sin_addr.s_addr = INADDR_ANY;
    clientaddr.sin_port = 0;

    prompt("Start\n");

    // create socket
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0)
    {
        perror(ARGV0);
        perror(" : Cannot create socket");
        exit(3);
    }
    prompt("");
    printf("Created socket in fd %d\n", sd);

    // bind socket
    if (bind(sd, (struct sockaddr *)&clientaddr, sizeof(clientaddr)) < 0)
    {
        perror(ARGV0);
        perror(" : Cannot bind socket");
        exit(3);
    }
    prompt("Bind successfull\n");

    prompt("Inserisca primo operando (Un numero intero), EOF per end.\n");
    while ((nread = scanf("%d", &num)) != EOF)
    {
        if (nread != 1)
        {
            consumptioninput();
            prompt("Reinserisca primo operando (Un numero intero), EOF per end.\n");
            continue;
        }
        cal.op1 = num;
        consumptioninput();
        prompt("Inserisca secondo operando\n");
        while ((nread = scanf("%d", &num)) != 1)
        {
            consumptioninput();
            prompt("Reinserisca secondo operando (Un numero intero)\n");
            continue;
        }
        cal.op2 = num;
        do
        {
            consumptioninput();
            prompt("Inserisca operazione ( +, -,  *, / )\n");
            scanf("%c", &c);
        } while (c != '+' && c != '-' && c != '*' && c != '/');
        cal.op = c;
        prompt("");
        printf("Operazione richiesta: [%d %c %d]\n", cal.op1, cal.op, cal.op2);

        // sendto
        len = sizeof(struct sockaddr_in);
        if (sendto(sd, &cal, sizeof(Calc), 0, (struct sockaddr *)&servaddr, len) < 0)
        {
            perror("Cannot send");
            continue;
        }
        prompt("Send ok\n");

        // recvfrom
        if (recvfrom(sd, &ris, sizeof(ris), 0, (struct sockaddr *)&servaddr, &len) < 0)
        {
            perror("Cannot receive");
            continue;
        }

        prompt("");
        printf("Esito dell'operazione: %d\n", ris);
        prompt("Inserisca primo operando (Un numero intero), EOF per end.\n");
    } // while
    close(sd);
    prompt("Termino...\n");
    exit(0);
} // main