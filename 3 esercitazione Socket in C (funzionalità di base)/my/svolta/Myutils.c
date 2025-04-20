#include "Myutils.h"
#include <stdio.h>
#include <stdlib.h>

extern char *ARGV0;
/*personalized printf*/
void pprintf(char *s)
{
    printf("%s : %s", ARGV0, s);
}

/*Convert port from a string and check its validity*/
int getportfromstring(char *portstr)
{
    int n = 0;
    int port = -1;
    while (portstr[n] != '\0')
    {
        if (!(portstr[n] >= '0' && portstr[n] <= '9'))
        {
            pprintf("Port is not a number\n");
            return port;
        }
        n++;
    }
    port = atoi(portstr);
    if (port < 1024 || port > 65535)
    {
        pprintf("Port non valid\n");
        pprintf("Port range : (1024 - 65535)\n");
        return -1;
    }
    return port;
}

void consumptioninput()
{
    while (getchar() != '\n')
        ;
}