
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
typedef struct
{
    int length;
    char filename[][FILENAME_LENGTH];
} filenames;

int main(int argc, char **argv)
{
    ARGV0 = argv[0];
    struct sockaddr_in saddr;
    struct hostent *host;
    int sd;
    // init
    {
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
            exit(1);
        }
        // get port form personalized function
        int port;
        if ((port = get_port_from_string(argv[2])) == -1)
            exit(1);
        // clear mem of server address
        memset(&saddr, 0, sizeof(struct sockaddr_in));
        saddr.sin_family = AF_INET;
        saddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr; /*h_addr is h_addr_list[0] */
        saddr.sin_port = htons(port);
    }

    prompt("Start\n");

    char dirname[FILENAME_LENGTH];
    int nread, fin, fout, nline;
    char *request;
    // Creat socket and conn
    {
        // create socket
        sd = socket(AF_INET, SOCK_STREAM, 0);
        if (sd < 0)
        {
            perror("Create socket");
            exit(2);
        }
        printf("Created socket in fd %d\n", sd);
        /* BIND implicit in connect */
        if (connect(sd, (struct sockaddr *)&saddr, sizeof(struct sockaddr)) < 0)
        {
            perror("Connect");
            exit(2);
        }
    }

    prompt("Connected to server\n");
    printf("Insert remote <dirname> which you want get list of files, EOF to END.\n");
    while (gets(dirname))
    {
        char response;
        printf("Send dirname <%s> \n", dirname);
        if (write(sd, dirname, sizeof(dirname)) < 0)
        {
            perror("Send dirname");
            printf("Insert remote <dirname> which you want get list of files, EOF to END.\n");
            continue;
        }
        if (read(sd, &response, 1) < 0)
        {
            perror("Read response");
            close(sd);
            exit(3);
        }
        if (response == 'N')
        {
            printf("Cannot find this directory in remote\n");
        } // response == 'N'
        else if (response == 'Y')
        {
            prompt("Receiving list file\n");
            inputoutput_withpattern(sd, 1);
        } // response == 'Y'
        else
        {
            printf("Error when get response\n");
        }
        prompt("Done\n");
        printf("Insert remote <dirname> which you want get list of files, EOF to END.\n");
    }
    shutdown(sd, SHUT_RDWR);
    prompt("Termino...");
    exit(0);
}