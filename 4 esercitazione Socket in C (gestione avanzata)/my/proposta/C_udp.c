
#include <netdb.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include "Myutils.h"

const char *ARGV0;

int main(int argc, char **argv)
{
    ARGV0 = argv[0];
    struct sockaddr_in caddr, saddr;
    struct hostent *host;
    socklen_t len;
    int sd;
    int port;
    // init
    {
        // manage params
        {
            // controll parameter number
            if (argc != 3)
            {
                printf("Usage: %s <serverIP> <serverPort>\n", ARGV0);
                exit(1);
            }
            // get port form personalized function
            if ((port = getportfromstring(argv[2])) == -1)
                exit(1);
            // get host. First from etc/hosts then DNS ...
            host = gethostbyname(argv[1]);
            if (host == NULL)
            {
                perror("Host Not Founded\n");
                exit(1);
            }
        }
        // memset of addresss
        {
            // memset of server address
            memset(&saddr, 0, sizeof(struct sockaddr_in));
            saddr.sin_family = AF_INET;
            saddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr; /*h_addr is h_addr_list[0] */
            saddr.sin_port = htons(port);
            // memset of client address
            memset(&caddr, 0, sizeof(struct sockaddr_in));
            caddr.sin_family = AF_INET;
            caddr.sin_addr.s_addr = INADDR_ANY;
            caddr.sin_port = 0;
        }
    } // init

    prompt("Start\n");
    // creat bind client socket
    {
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
        if (bind(sd, (struct sockaddr *)&caddr, sizeof(caddr)) < 0)
        {
            perror("Bind socket");
            exit(3);
        }
        prompt("Bind successfull\n");
    }

    int nread;
    char word[128], filename[FILENAME_LENGTH];
    int result = 0;
    Request p;

    prompt("Insert <word> which you want eliminate from remote <filename>, EOF to end.\n");
    // get first param from stdin
    while (gets(p.word))
    {
        // get second param from stdin
        prompt("Insert <filename>, EOF to end.\n");
        if (!gets(p.filename))
        {
            break;
        }
        printf("Request : \nword :<%s> \nfilename :<%s>\n", p.word, p.filename);

        // sendto
        len = sizeof(struct sockaddr_in);
        if (sendto(sd, &p, sizeof(p), 0, (struct sockaddr *)&saddr, len) < 0)
        {
            perror("Cannot send");
            continue;
        }
        prompt("Send OK\n");

        // recvfrom
        if (recvfrom(sd, &result, sizeof(result), 0, (struct sockaddr *)&saddr, &len) < 0)
        {
            perror("Cannot receive");
            continue;
        }
        prompt("Receive OK\n");

        prompt("");
        if (result >= 0)
        {
            printf("Elminate <%d> words\n", result);
        }
        else if (result == -1)
        {
            printf("!! Cannot open the file\n");
        }
        else
        {
            printf("Unknown error\n");
        }
        prompt("Insert <word> which you want eliminate from remote <filename>, EOF to end.\n");

    } // while
    close(sd);
    prompt("Termino...\n");
    exit(0);
} // main