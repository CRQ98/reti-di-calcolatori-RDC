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

// udps job
int countwordeliminate(char *word, char *filenamein)
{
    printf("countwordeliminate\n");
    int counter = 0;
    char c;
    int i = 0;
    char wordtemp[128];
    char *filenameout = "temp_file.tmp";
    int fout, fin;

    if ((fin = open(filenamein, O_RDONLY)) < 0)
    {
        perror("Open file");
        return -1;
    }

    if ((fout = open(filenameout, O_WRONLY | O_TRUNC | O_CREAT, 0644)) < 0)
    {
        perror("Open file");
        return -1;
    }

    while (read(fin, &c, 1) > 0)
    {
        if (c == ' ' || c == '\n' || c == '\r' || c == ',' || c == '.')
        {
            wordtemp[i] = '\0';
            if (strlen(wordtemp) == strlen(word) && strcmp(wordtemp, word) == 0)
            {
                counter++;
            }
            else
            {
                if (write(fout, wordtemp, strlen(wordtemp)) < 0)
                    perror("Write");
                if (write(fout, &c, 1) < 0)
                    perror("Write");
            }
            i = 0;
            wordtemp[0] = '\0';
        }
        else
        {
            wordtemp[i++] = c;
        }
    }
    if (i > 0)
    {
        wordtemp[i] = '\0';
        if (strlen(wordtemp) == strlen(word) && strcmp(wordtemp, word) == 0)
        {
            counter++;
        }
        else
        {
            if (write(fout, wordtemp, strlen(wordtemp)) < 0)
                perror("Write");
        }
    }
    if (rename(filenameout, filenamein) < 0)
    {
        perror("Rename file");
    }
    close(fin);
    close(fout);
    return counter;
}

int main(int argc, char **argv)
{
    ARGV0 = argv[0];
    struct sockaddr_in caddr, saddr;
    struct hostent *host;
    socklen_t len;
    const int on = 1;
    int tcpfd, udpfd, cfd;
    int port;
    len = sizeof(caddr);
    // init
    {
        // manage params -------------------------------------
        {
            if (argc != 2)
            {
                printf("Usage: %s <serverPort>\n", ARGV0);
                exit(1);
            }
            if ((port = get_port_from_string(argv[1])) == -1)
                exit(1);
        }

        // memset of addresss -------------------------
        {
            memset((char *)&saddr, 0, sizeof(struct sockaddr_in));
            saddr.sin_family = AF_INET;
            saddr.sin_addr.s_addr = INADDR_ANY;
            saddr.sin_port = htons(port);
        }

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
    }

    prompt("Start\n");

    fd_set rset;
    FD_ZERO(&rset);
    int maxfd = max(tcpfd, udpfd);
    int nready;
    // Cicle  ------------------------------------------
    while (1)
    {
        // set list, select
        {
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
        }

        // Manage UDP ------------------------------------
        if (FD_ISSET(udpfd, &rset))
        {
            prompt("UDP part\n");
            Request req;

            int ffd;
            // recvfrom
            if (recvfrom(udpfd, &req, sizeof(Request), 0, (struct sockaddr *)&caddr, &len) < 0) // len need be set in local
            {
                perror("Cannot receive");
                continue;
            }
            printf("Request : \nword :<%s> \nfilename :<%s>\n", req.word, req.filename);

            int result = countwordeliminate(req.word, req.filename);
            printf("Eliminate <%d> words\n", result);

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

            char dirname[FILENAME_LENGTH];
            char fullpath[2048], filepath[2048];
            int nread, finfd;
            char response;

            signal(SIGCHLD, gestore);
            if (fork() == 0)
            {
                printf("Child : %d\n", getpid());
                close(tcpfd);
                DIR *dir, *dir1, *dir2;
                struct dirent *dd, *dd2;
                char eot = 0x04, nextline = '\n';
                while ((nread = read(cfd, dirname, sizeof(dirname))) > 0)
                {
                    if ((dir = opendir(dirname)) == NULL)
                    {
                        response = 'N';
                    }
                    else
                    {
                        response = 'Y';
                    }
                    write(cfd, &response, 1);
                    if (response == 'N')
                        continue;
                    while ((dd = readdir(dir)) != NULL)
                    {
                        if (strcmp(dd->d_name, ".") == 0 || strcmp(dd->d_name, "..") == 0)
                            continue;
                        snprintf(fullpath, sizeof(fullpath), "%s/%s", dirname, dd->d_name);
                        if (is_directory(fullpath))
                        {
                            dir1 = opendir(fullpath);
                            if (dir1 == NULL)
                            {
                                perror("Open dir");
                            }
                            else
                            {
                                while ((dd2 = readdir(dir1)) != NULL)
                                {
                                    if (strcmp(dd2->d_name, ".") == 0 || strcmp(dd2->d_name, "..") == 0)
                                        continue;
                                    snprintf(filepath, sizeof(filepath), "%s/%s", fullpath, dd2->d_name);
                                    if (is_regularfile(filepath))
                                    {
                                        if (write(cfd, dd2->d_name, strlen(dd2->d_name)) < 0)
                                        {
                                            perror("Write filename");
                                        }
                                        if (write(cfd, &nextline, 1) < 0)
                                        {
                                            perror("Write nextline");
                                        }
                                    }
                                } // while dd2
                                closedir(dir1);
                            }
                        } // is directory
                    } // while dd
                    closedir(dir);
                    write(cfd, &eot, 1);
                } // while nread
                shutdown(cfd, SHUT_RDWR);
                printf("Child END\n");
                exit(0);
            } // fork
            close(cfd);
        } // if isset tcpfd
    }
} // main