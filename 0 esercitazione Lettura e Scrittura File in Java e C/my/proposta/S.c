#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
void usage()
{
    printf("Usage: S <filename>\n");
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
    fd = open(filename, O_WRONLY | O_TRUNC | O_CREAT, 0640);
    if (fd < 0)
    {
        perror("P0:Creazione o Apertura file fallita");
        return EXIT_FAILURE;
    }
    printf("Pronta per scrivere nel file %s\n", filename);
    printf("Inizi a inserire, si termina fino alla lettura di EOF\n");
    while ((nc = read(1, &ch, 1)) == 1)
    {
        if (write(fd, &ch, 1) < 0)
        {
            perror("Errore nella scrittura");
            close(fd);
            return EXIT_FAILURE;
        }
    }
    printf("Termino...\n");
    close(fd);
    return EXIT_SUCCESS;
}
