#include "Myutils.h"
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>

extern char *ARGV0;

/*Convert port from a string and check its validity*/
int getportfromstring(char *portstr)
{
    int n = 0;
    int port = -1;
    while (portstr[n] != '\0')
    {
        if (!(portstr[n] >= '0' && portstr[n] <= '9'))
        {
            printf("%s : Port is not a number\n",ARGV0);
            return port;
        }
        n++;
    }
    port = atoi(portstr);
    if (port < 1024 || port > 65535)
    {
        printf("%s : Port non valid\n",ARGV0);
        printf("%s : Port range : (1024 - 65535)\n",ARGV0);
        return -1;
    }
    return port;
}

void consumptioninput()
{
    while (getchar() != '\n')
        ;
}

void inputoutput(int fd_in,int fd_out){
    int nread;
    char c;
    while((nread=read(fd_in,&c,1))>0)
        if(write(fd_out,&c,nread)<0){
            perror("Write in inputoutput");
        }
}
