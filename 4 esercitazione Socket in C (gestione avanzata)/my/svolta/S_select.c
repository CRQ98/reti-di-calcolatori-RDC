#include <errno.h>
#include <fcntl.h>
#include <netdb.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <dirent.h>
#include "Myutils.h"

const char *ARGV0;
void prompt(char *s)
{
    printf("%s : ", ARGV0);
    printf("%s", s);
}

/********************************************************/
int fileCounter(char *dirname)
{
    int nfile = 0;
    DIR *dir;
    struct dirent *dd;
    dir = opendir(dirname);
    if (dir == NULL)
        return -1;
    while ((dd = readdir(dir)) != NULL)
    {
        if (strcmp(dd->d_name, ".") == 0 || strcmp(dd->d_name, "..") == 0)
            continue;
        printf("Find file :<%s>\n", dd->d_name);
        nfile++;
    }
    closedir(dir);
    return nfile;
}
/********************************************************/

int main(int argc, char **argv)
{
    ARGV0 = argv[0];
    struct sockaddr_in caddr, saddr;
    struct hostent *host;
    const int on = 1;
    socklen_t len;
    int tcpfd, udpfd, cfd;
    int port;
    len = sizeof(caddr);

    // controll params -------------------------------------
    {
        if (argc != 2)
        {
            printf("Usage: %s <serverPort>\n", ARGV0);
            exit(1);
        }
        if ((port = getportfromstring(argv[1])) == -1)
            exit(1);
    }

    // clear mem of server address -------------------------
    memset((char *)&saddr, 0, sizeof(struct sockaddr_in));
    saddr.sin_family = AF_INET;
    saddr.sin_addr.s_addr = INADDR_ANY;
    saddr.sin_port = htons(port);

    // Create TCP socket -----------------------------------
    {
        printf("***************+**********************************\n");
        prompt("create TCP socket\n");
        // create socket
        tcpfd = socket(AF_INET, SOCK_STREAM, 0);
        if (tcpfd < 0)
        {
            perror("Create socket");
            exit(3);
        }
        prompt("");
        printf("Created socket in fd %d\n", tcpfd);
        // set socket opt
        if (setsockopt(tcpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
        {
            perror("Set option socket failed");
            exit(2);
        }
        // bind socket
        if (bind(tcpfd, (struct sockaddr *)&saddr, sizeof(saddr)) < 0)
        {
            perror("Bind TCP socket");
            exit(2);
        }
        prompt("Bind successfull\n");
        // Listen
        // set backlog = 5
        if (listen(tcpfd, 5) < 0)
        {
            perror("Listen");
            exit(2);
        }
        prompt("Listen OK\n");
        printf("***************+**********************************\n");
    }

    // Create UDP socket -----------------------------------
    {
        printf("***************+**********************************\n");
        prompt("create UDP socket\n");
        udpfd = socket(AF_INET, SOCK_DGRAM, 0);
        if (udpfd < 0)
        {
            perror("Cannot create socket");
            exit(2);
        }
        prompt("");
        printf("Created socket in fd %d\n", udpfd);
        // set socket opt
        if (setsockopt(udpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
        {
            perror("Set option socket failed");
            exit(2);
        }
        // bind socket
        if (bind(udpfd, (struct sockaddr *)&saddr, sizeof(saddr)) < 0)
        {
            perror("Cannot bind socket");
            exit(2);
        }
        prompt("Bind successfull\n");
        printf("***************+**********************************\n");
    }

    prompt("Start\n");
    signal(SIGCHLD, gestore);
    fd_set rset;
    FD_ZERO(&rset);
    int maxfd = max(tcpfd, udpfd);
    int nready;
    // Cicle of select -------------------------------------
    while (1)
    {
        // set list every cicle, cuz select will modify rset
        FD_SET(tcpfd, &rset);
        FD_SET(udpfd, &rset);

        if ((nready = select(maxfd + 1, &rset, NULL, NULL, NULL)) < 0)
        {
            if (errno == EINTR)
                continue;
            else
            {
                perror("Select");
                exit(3);
            }
        }

        // Manage UDP ------------------------------------
        if (FD_ISSET(udpfd, &rset))
        {
            prompt("UDP part\n");
            char dirname[256];
            // recvfrom
            if (recvfrom(udpfd, dirname, sizeof(dirname), 0, (struct sockaddr *)&caddr, &len) < 0) // len need be set in local
            {
                perror("Cannot receive");
                continue;
            }
            int result;
            result = fileCounter(dirname);
            // send
            if (sendto(udpfd, &result, sizeof(result), 0, (struct sockaddr *)&caddr, len) < 0)
            {
                perror("Cannot send");
                continue;
            }
        }

        // Manage TCP ------------------------------------
        if (FD_ISSET(tcpfd, &rset))
        {
            prompt("TCP part\n");
            if ((cfd = accept(tcpfd, (struct sockaddr *)&caddr, &len)) < 0)
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
            char filename[FILENAME_LENGTH];
            int nread, finfd;
            char response = 'S';
            if ((nread = read(cfd, filename, sizeof(filename)) < 0))
            {
                perror("Read");
                exit(3);
            }
            if ((finfd = open(filename, O_RDONLY)) < 0)
            {
                perror("Open");
                response = 'N';
            }
            if (write(cfd, &response, 1) < 0)
            {
                perror("write");
            }
            else
            {
                inputoutput(finfd, cfd);
            }
            close(finfd);
            close(cfd);
        }
    }
} // main