
#include <netdb.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <fcntl.h>
#include <unistd.h>
#include "Myutils.h"

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

    // controll parameter number
    if (argc != 3)
    {
        printf("Usage: %s <serverIP> <serverPort>\n", ARGV0);
        exit(1);
    }

    // get host. First from etc/hosts then DNS ...
    host = gethostbyname(argv[1]);
    if (host == NULL)
    {
        prompt("Host Not Founded\n");
        exit(2);
    }

    // get port form personalized function
    int port;
    if ((port = getportfromstring(argv[2])) == -1)
        exit(1);

    // clear mem of server address
    memset(&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr; /*h_addr is h_addr_list[0] */
    servaddr.sin_port = htons(port);

    prompt("Start\n");

    char filename[256];
    int nread, fin, fout, nline, sd;
    char *request;

    printf("Insert <filename> which you want eliminate line, EOF to END.\n");
    while ((nread = scanf("%s", filename)) != EOF)
    {
        consumptioninput();
        fin = open(filename, O_RDONLY);
        if (fin < 0)
        {
            perror("Open file");
            printf("Insert <filename> which you want eliminate line, EOF to END.\n");
            continue;
        }
        printf("Insert <number of line> that you want eliminate\n");
        if ((nread = scanf("%d", &nline)) < 0)
        {
            perror("Read number of line");
            printf("Insert <filename> which you want eliminate line, EOF to END.\n");
            continue;
        }
        consumptioninput();

        // create socket
        sd = socket(AF_INET, SOCK_STREAM, 0);
        if (sd < 0)
        {
            perror("Create socket");
            exit(3);
        }
        prompt("");
        printf("Created socket in fd %d\n", sd);

        /* BIND implicit in connect */
        if (connect(sd, (struct sockaddr *)&servaddr, sizeof(struct sockaddr)) < 0)
        {
            prompt("");
            perror("Connect");
            exit(4);
        }
        prompt("Connected to server\n");

        prompt("Send number of line\n");
        write(sd, &nline, sizeof(nline));

        prompt("Send content of file\n");
        inputoutput(fin, sd);
        close(fin);
        shutdown(sd, 1);

        prompt("Receive response\n");
        inputoutput(sd, 1);
        close(sd);

        printf("\n");
        printf("Insert <filename> which you want eliminate line, EOF to END.\n");
    }
    prompt("Termino...");
    exit(0);
}