
#include <netdb.h>
#include <errno.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <fcntl.h>
#include <unistd.h>
#include "Myutils.h"

const char *ARGV0;

/********************************************************/
void gestore(int signo)
{
    int stato;
    printf("SIGCHLD\n");
    wait(&stato);
}
/********************************************************/

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
    const int on = 1;
    int sd;

    // controll parameter number ---------------------------
    if (argc != 2)
    {
        printf("Usage: %s <serverPort>\n", ARGV0);
        exit(1);
    }

    // get port --------------------------------------------
    int port;
    if ((port = getportfromstring(argv[1])) == -1)
        exit(1);

    // clear mem of server address -------------------------
    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port = htons(port);

    prompt("Start\n");

    // Create socket for server -----------------------------
    sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd < 0)
    {
        perror("Create socket\n");
        exit(2);
    }
    prompt("");
    printf("Create socket in fd=%d\n", sd);

    // set socket opt ---------------------------------------
    if (setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
    {
        perror("set opt socket ");
        exit(1);
    }
    prompt("Set socket opt OK\n");

    // bind -------------------------------------------------
    if (bind(sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0)
    {
        printf("Bind ");
        exit(2);
    }
    prompt("Bind OK\n");

    // Listen -----------------------------------------------
    // set backlog = 5
    if (listen(sd, 5) < 0)
    {
        perror("Listen");
        exit(2);
    }
    prompt("Listen list set OK\n");

    signal(SIGCHLD, gestore);

    socklen_t len;
    int cfd;
    while (1)
    {
        // Accept ------------------------------------------
        len = sizeof(clientaddr);
        if ((cfd = accept(sd, (struct sockaddr *)&clientaddr, &len)) < 0)
        {
            if (errno == EINTR)
            {
                continue;
            }
            else
            {
                perror("Accept ");
                exit(3);
            }
        }
        prompt("");
        printf("Get connection client fd = %d\n", cfd);

        // fork -------------------------------------------
        if (fork() == 0)
        { // is child
            prompt("Child\n");
            // close fd no in use
            close(sd);

            prompt("Read number of line\n");
            int nline;
            if (read(cfd, &nline, sizeof(nline)) < 0)
            {
                perror("Read number of line");
            }
            printf("Line request to eliminate : %d\n", nline);

            int lineCount, nread;
            char c;
            lineCount = 1;
            while ((nread = read(cfd, &c, 1)) > 0)
            {
                if (lineCount != nline)
                {
                    if (write(cfd, &c, nread) < 0)
                        perror("Write");
                }
                if (c == '\n')
                    lineCount++;
            }
            close(cfd);
            exit(0);
        }
        else
        { // is father
            // close fd no in use
            close(cfd);
        }
    }
}