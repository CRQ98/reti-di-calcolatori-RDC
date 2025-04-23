
#include <netdb.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
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
    socklen_t len;
    int sd;

    // controll parameter number
    if (argc != 3)
    {
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
    memset(&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr; /*h_addr is h_addr_list[0] */
    servaddr.sin_port = htons(port);

    // clear mem of client address
    memset(&clientaddr, 0, sizeof(struct sockaddr_in));
    clientaddr.sin_family = AF_INET;
    clientaddr.sin_addr.s_addr = INADDR_ANY;
    clientaddr.sin_port = 0;

    prompt("Start\n");

    // create socket
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0)
    {
        perror("Create socket");
        exit(3);
    }
    prompt("");
    printf("Created socket in fd %d\n", sd);

    // bind socket
    if (bind(sd, (struct sockaddr *)&clientaddr, sizeof(clientaddr)) < 0)
    {
        perror("Bind socket");
        exit(3);
    }
    prompt("Bind successfull\n");

    int nread;
    char dirname[256];
    int result = 0;

    prompt("Insert remote <directory> which you want get number of files, EOF per end.\n");
    while ((nread = scanf("%s", dirname)) != EOF)
    {
        consumptioninput();
        if (nread != 1)
        {
            printf("Again pls!\n");
            prompt("Insert remote <directory> which you want get number of files, EOF per end.\n");
            continue;
        }

        // sendto
        len = sizeof(struct sockaddr_in);
        if (sendto(sd, dirname, sizeof(dirname), 0, (struct sockaddr *)&servaddr, len) < 0)
        {
            perror("Cannot send");
            continue;
        }
        prompt("Send OK\n");

        // recvfrom
        if (recvfrom(sd, &result, sizeof(result), 0, (struct sockaddr *)&servaddr, &len) < 0)
        {
            perror("Cannot receive");
            continue;
        }
        prompt("Receive OK\n");

        prompt("");
        if (result >= 0)
        {
            printf("In <%s> there <%d> files\n", dirname, result);
        }
        else
        {
            printf("!! Cannot get the number of files\n");
        }
        prompt("Insert remote filename which you want get longest word, EOF per end.\n");

    } // while
    close(sd);
    prompt("Termino...\n");
    exit(0);
} // main