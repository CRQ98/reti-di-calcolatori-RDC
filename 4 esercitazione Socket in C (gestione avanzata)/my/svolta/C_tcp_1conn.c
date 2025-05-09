
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
    struct sockaddr_in saddr;
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
        exit(1);
    }

    // get port form personalized function
    int port;
    if ((port = getportfromstring(argv[2])) == -1)
        exit(1);

    // clear mem of server address
    memset(&saddr, 0, sizeof(struct sockaddr_in));
    saddr.sin_family = AF_INET;
    saddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr; /*h_addr is h_addr_list[0] */
    saddr.sin_port = htons(port);

    prompt("Start\n");

    char filename[FILENAME_LENGTH];
    int nread, fin, fout, nline, sd;
    char *request;

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
    if (connect(sd, (struct sockaddr *)&saddr, sizeof(struct sockaddr)) < 0)
    {
        prompt("");
        perror("Connect");
        exit(2);
    }
    prompt("Connected to server\n");

    printf("Insert <filename> which you want get form Server, EOF to END.\n");
    while ((fgets(filename, sizeof(filename), stdin)) != NULL)
    {
        filename[strcspn(filename, "\n")] = '\0';
        char response;
        printf("Send filename <%s> \n", filename);
        if (write(sd, filename, sizeof(filename)) < 0)
        {
            perror("Write filename");
            printf("Insert <filename> which you want get form Server, EOF to END.\n");
            continue;
        }
        if (read(sd, &response, 1) < 0)
        {
            perror("Read response");
            close(sd);
            exit(2);
        }

        if (response == 'N')
        {
            printf("Cannot find this file in remote\n");
        } // response == 'N'
        else if (response == 'S')
        {
            fout = open(filename, O_WRONLY | O_CREAT | O_TRUNC, 0644);
            if (fout < 0)
            {
                perror("Open file");
                printf("Insert <filename> which you want get form Server, EOF to END.\n");
                continue;
            }
            prompt("receive file\n");
            inputoutputwithpattern(sd, fout);
            close(fout);
        } // response == 'S'
        else
        {
            printf("Error when get response\n");
        }
        prompt("Done\n");
        printf("Insert <filename> which you want get form Server, EOF to END.\n");
    }
    shutdown(sd, SHUT_RDWR);
    prompt("Termino...");
    exit(0);
}