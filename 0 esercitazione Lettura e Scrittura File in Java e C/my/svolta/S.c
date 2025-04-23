#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#define MAX_STRING_LENGTH 256
void usage()
{
    printf("Usage: S <filename>\n");
    exit(0);
}
int main(int argc, char **argv)
{
    int fd, nline;
    char *filename;
    char line[MAX_STRING_LENGTH];
    // controllo paramentri
    if (argc != 2)
        usage();
    else
        filename = argv[1];

    printf("Quante righe vuoi inserire?\n");
    while (scanf("%d", &nline) != 1)
    {
        perror("Numero inserito non valido");
        // consuma riga dopo scanf
        gets(line);
    }
    // consuma riga dopo scanf
    gets(line);
    fd = open(filename, O_WRONLY | O_TRUNC | O_CREAT, 0640);
    if (fd < 0)
    {
        perror("P0:Creazione o Apertura file fallita");
        return EXIT_FAILURE;
    }
    printf("Aperta file %s\n", filename);
    for (int i = 0; i < nline; i++)
    {
        printf("Inserire la riga\n");
        gets(line);
        line[strlen(line)] = '\n';
        line[strlen(line) + 1] = '\0';
        if (write(fd, line, strlen(line)) < 0)
        {
            perror("Errore nella scrittura");
            return EXIT_FAILURE;
        }
    }
    printf("Termino\n");
    close(fd);
    return EXIT_SUCCESS;
}
