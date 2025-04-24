
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
    struct sockaddr_in caddr, saddr;
    struct hostent *clienthost;
    const int on = 1;
    Calc *calc = (Calc *)malloc(sizeof(Calc));
    Calc cal;
    int port;
    socklen_t len=sizeof(caddr);
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
    memset((char *)&saddr, 0, sizeof(saddr));
    saddr.sin_family = AF_INET;
    saddr.sin_addr.s_addr = INADDR_ANY; /*h_addr is h_addr_list[0] */
    saddr.sin_port = htons(port);

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
    if (bind(sd, (struct sockaddr *)&saddr, sizeof(saddr)) < 0)
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
        if (recvfrom(sd, calc, sizeof(Calc), 0, (struct sockaddr *)&caddr, &len) < 0)//len need be set in local
        {
            perror("Cannot receive");
            continue;
        }
        cal = *calc;
        // get client host info
        clienthost = gethostbyaddr((char *)&caddr.sin_addr, sizeof(caddr.sin_addr), AF_INET);
        if (clienthost == NULL)
        {
            prompt("Cannot found client host\n");
        }
        else
        {
            prompt("");
            printf("Request from : %s %s:%i\n", clienthost->h_name, clienthost->h_addr, ntohs(caddr.sin_port));
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
        if (sendto(sd, &risultato, sizeof(risultato), 0, (struct sockaddr *)&caddr, len) < 0)
        {
            perror("Cannot send");
            continue;
        }
    }
} // main