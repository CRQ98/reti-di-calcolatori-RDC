#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#define MAX_STRING_LENGTH 256
void usage()
{
    printf("Usage: C <prefix> OR C <prefix> <filename>\n");
    exit(0);
}
int main(int argc, char **argv)
{
    int fd, nc, flag = 0;
    char *filename, *prefix;
    char ch;
    // controllo paramentri
    if (argc != 2 && argc != 3){ usage();
        return EXIT_FAILURE;}
       
    else
        prefix = argv[1];
    if (argc == 3)
    {
        filename = argv[2];
        fd = open(filename, O_RDONLY);
        if (fd < 0)
        {
            perror("Apertura file fallita");
            return EXIT_FAILURE;
        }
        else
            printf("Apertura file riuscita\n");
    }
    else
    {
        // 0 Standard input (stdin)
        fd = 0;
        printf("Pronto a leggere da STDIN\n");
    }
    printf("Leggo fino a EOF\n");

    while ((nc = read(fd, &ch, 1)) > 0)
    {
        // clean flag
        flag = 0;
        if (nc == 1)
        {
            for (int i = 0; i < strlen(prefix); i++)
            {
                if (ch == prefix[i])
                    flag = 1;
            }
            if (!flag)
                putchar(ch);
        }
        else
        {
            printf("PID %d: Errore nel read file %s", getpid(), filename);
            perror("Errore!");
            close(fd);
        }
    }
    printf("Termino...\n");

    close(fd);
    return EXIT_SUCCESS;
}
