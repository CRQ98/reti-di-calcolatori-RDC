
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
void inputoutput_local(int in,int out){
    int nread;
    char c;
    while((nread=read(in,&c,1))>0){
        write(out,&c,nread);
        write(1,&c,nread);
    }

}
int main(int argc, char **argv)
{
    ARGV0 = argv[0];
    struct sockaddr_in clientaddr, servaddr;
    struct hostent *host;
    int nread, num;
    char c;
    int sd,fin,fout;
    char *output;

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
        exit(2);
    }

    // get port form personalized function
    int port;
    if ((port = getportfromstring(argv[2])) == -1)
        exit(1);


    // clear mem of server address
    // 测试没有char*
    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr; /*h_addr is h_addr_list[0] */
    servaddr.sin_port = htons(port);

    prompt("Start\n");

    char filename[256]
    ,filename_sorted[256];
    
    printf("Insert filename to sort or EOF to END\n");
    while((nread=scanf("%s",filename))!=EOF){
        //consumptioninput();
        fin=open(filename,O_RDONLY);
        if(fin<0){
            prompt("");
            perror("Open file sorg failed\n");
            printf("Insert filename or EOF to END\n");
            continue;
        }
        prompt("Insert file dest name\n");
        if((nread=scanf("%s",filename_sorted))<0)
            break;
        fout=open(filename_sorted,O_WRONLY|O_CREAT,0644);
        if(fout<0){
            prompt("");
            perror("Open file dest failed\n");
            printf("Insert filename or EOF to END\n");
            continue;
        }

        // create socket
        sd = socket(AF_INET, SOCK_STREAM, 0);
        if (sd < 0)
        {
            perror(ARGV0);
            perror(" : Cannot create socket");
            exit(3);
        }
        prompt("");
        printf("Created socket in fd %d\n", sd);
        
        /* BIND implicit in connect */
        if (connect(sd, (struct sockaddr *)&servaddr, sizeof(struct sockaddr)) < 0) {
            prompt("");
            perror("connect");
            exit(4);
        }
        prompt("Connected to server\n");

        inputoutput_local(fin,sd);
        close(fin);
        shutdown(sd,1);
        prompt("File inviato\n");

        prompt("Receive and print on STDOUT the file sorted content\n");
        inputoutput_local(sd,fout);
        close(fout);
        shutdown(sd,0);
        prompt("End of transfer\n");
        printf("Insert filename to sort or EOF to END\n");
    }
    prompt("Termino...");
    exit(0);
}