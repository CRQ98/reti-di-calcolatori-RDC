#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#define MAX_STRING_LENGTH 256
void usage()
{
    printf("Usage: C <filename>\n");
    exit(0);
}
int main(int argc, char **argv)
{
    int fd, nc;
    char *filename;
    char ch;
    // controllo paramentri
    if (argc != 2)
        usage();
    else
        filename = argv[1];
    fd = open(filename, O_RDONLY);
    if (fd < 0)
    {
        perror("Apertura file fallita");
        return EXIT_FAILURE;
    }
    while ((nc = read(fd, &ch, 1)) > 0)
    {
        if (nc == 1)
            putchar(ch);
        else
        {
            printf("PID %d: Errore nel read file %s", getpid(), filename);
            perror("Errore!");
            close(fd);
        }
    }
    close(fd);
    return EXIT_SUCCESS;
}
