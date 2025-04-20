
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
    struct hostent *clienthost;
    const int on = 1;
    Calc *calc = (Calc *)malloc(sizeof(Calc));
    Calc cal;
    int port;
    int len=sizeof(clientaddr);
    int sd;

    // controll parameter number
    if (argc != 2)
    {
        prompt("Usage: <Port>\n");
        exit(1);
    }

    // get port form personalized function
    if ((port = getportfromstring(argv[1])) == -1)
        exit(1);
    // clear mem of server address
    // 测试没有char*
    memset((char *)&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY; /*h_addr is h_addr_list[0] */
    servaddr.sin_port = htons(port);

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

    // set socket opt
    if (setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
    {
        perror(ARGV0);
        perror("Set opzioni socket failed");
        exit(4);
    }

    // bind socket
    if (bind(sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0)
    {
        perror(ARGV0);
        perror(" : Cannot bind socket");
        exit(5);
    }
    prompt("Bind successfull\n");

    for (;;)
    {
        prompt("Waiting for new connection\n");
        // recvfrom
        if (recvfrom(sd, calc, sizeof(Calc), 0, (struct sockaddr *)&clientaddr, &len) < 0)//len need be set in local
        {
            perror("Cannot receive");
            continue;
        }
        cal = *calc;
        // get client host info
        clienthost = gethostbyaddr((char *)&clientaddr.sin_addr, sizeof(clientaddr.sin_addr), AF_INET);
        if (clienthost == NULL)
        {
            prompt("Cannot found client host\n");
        }
        else
        {
            prompt("");
            printf("Request from : %s %s:%i\n", clienthost->h_name, clienthost->h_addr, ntohs(clientaddr.sin_port));
        }

        prompt("");
        printf("Request calc: [%d %c %d]\n", cal.op1, cal.op, cal.op2);

        // calculation
        int risultato = 0;
        if (cal.op == '+')
            risultato = cal.op1 + cal.op2;
        if (cal.op == '-')
            risultato = cal.op1 - cal.op2;
        if (cal.op == '*')
            risultato = cal.op1 * cal.op2;
        if (cal.op == '/')
            risultato = cal.op1 / cal.op2;
        if (cal.op != '+' && cal.op != '-' && cal.op != '*' && cal.op != '/')
        {
            risultato = 0;
            prompt("");
            printf("Cannot calculate witn <%c> ", cal.op);
        }

        // send
        if (sendto(sd, &risultato, sizeof(risultato), 0, (struct sockaddr *)&clientaddr, len) < 0)
        {
            perror("Cannot send");
            continue;
        }
    }
} // main