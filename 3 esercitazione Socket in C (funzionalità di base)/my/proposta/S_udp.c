
#include <netdb.h>
#include <stdio.h>
#include <fcntl.h>
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
    struct hostent *clienthost;
    socklen_t len;
    int port;
    const int on = 1;
    int sd;

    // controll parameter number
    if (argc != 2)
    {
        prompt("Usage: <Port>\n");
        exit(1);
    }

    // get port form personalized function
    if ((port = getportfromstring(argv[1])) == -1)
    {
        exit(1);
    }

    // clear mem of server address
    memset(&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY; /*h_addr is h_addr_list[0] */
    servaddr.sin_port = htons(port);

    prompt("Start\n");

    // create socket
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0)
    {
        perror(" : Cannot create socket");
        exit(2);
    }
    prompt("");
    printf("Created socket in fd %d\n", sd);

    // set socket opt
    if (setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
    {
        perror("Set opzioni socket failed");
        exit(2);
    }

    // bind socket
    if (bind(sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0)
    {
        perror("Cannot bind socket");
        exit(2);
    }
    prompt("Bind successfull\n");

    char filename[256];
    for (;;)
    {
        prompt("Waiting for new connection\n");
        // recvfrom
        len = sizeof(clientaddr);
        if (recvfrom(sd, filename, sizeof(filename), 0, (struct sockaddr *)&clientaddr, &len) < 0)
        {
            perror("Cannot receive");
            continue;
        }
        prompt("Receive OK\n");
        printf("filename to read : %s\n", filename);

        int nread, fd_in, cout, maxcout;
        char c;
        cout = 0;
        maxcout = 0;
        if ((fd_in = open(filename, O_RDONLY)) < 0)
        {
            maxcout = -1;
            perror("Open file");
        }
        else
        {
            while ((nread = read(fd_in, &c, 1)) != 0)
            {
                // if (c != ' ' && c != '\n' && c != '\t' && ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')))
                //^ matchs real word a-z A-Z
                if (nread < 0)
                {
                    perror("Read file");
                    exit(1);
                }

                if (c == ' ' || c == '\n' || c == '\r' || c == '\t')
                {
                    if (cout > maxcout)
                    {
                        maxcout = cout;
                    }
                    cout = 0;
                }
                else
                {
                    cout++;
                }
            }
            close(fd_in);
        }

        int result = maxcout;
        // send
        if (sendto(sd, &result, sizeof(result), 0, (struct sockaddr *)&clientaddr, len) < 0)
        {
            perror("Cannot send");
            continue;
        }
    }
} // main