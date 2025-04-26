#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <dirent.h>
#include <string.h>
#include "Myutils.h"

extern char *ARGV0;

void prompt(char *s)
{
    printf("%s : ", ARGV0);
    printf("%s", s);
}

/*Convert port from a string and check its validity*/
int get_port_from_string(const char *portstr)
{
    int n = 0;
    int port = -1;
    while (portstr[n] != '\0')
    {
        if (!(portstr[n] >= '0' && portstr[n] <= '9'))
        {
            printf("%s : Port is not a number\n", ARGV0);
            return port;
        }
        n++;
    }
    port = atoi(portstr);
    if (port < 1024 || port > 65535)
    {
        printf("%s : Port non valid\n", ARGV0);
        printf("%s : Port range : (1024 - 65535)\n", ARGV0);
        return -1;
    }
    return port;
}

void consumptionstdin()
{
    while (getchar() != '\n')
        ;
}

int readc(int fin, char *c)
{
    int nread;
    nread = read(fin, c, 1);
    if (nread < 0)
    {
        perror("Read char ");
        return -1;
    }
    return nread;
}

int writec(int fout, char *c)
{
    int nwrite;
    nwrite = write(fout, c, 1);
    if (nwrite < 0)
    {
        perror("Write char");
        return -1;
    }
    return nwrite;
}

void inputoutput(int fd_in, int fd_out)
{
    char c;
    while (readc(fd_in, &c) > 0)
    {
        if (writec(fd_out, &c) < 0)
            break;
    }
}

void inputoutput_withpattern(int fd_in, int fd_out)
{
    int nread;
    char c;

    while (readc(fd_in, &c) > 0)
    {
        if (c == 0x04)
        { // EOT = 0x04
            return;
        }
        if (writec(fd_out, &c) < 0)
        {
            break;
        }
    }
    c = 0x04;
    writec(fd_out, &c);
}

off_t get_filesize(const char *filename)
{
    struct stat file_info;
    if (stat(filename, &file_info) == -1)
    {
        perror("stat");
        return -1;
    }
    return file_info.st_size;
}

void gestore(int signo)
{
    int stato;
    printf("SIGCHLD\n");
    wait(&stato);
}

int is_directory(const char *path)
{
    struct stat path_stat;
    return (stat(path, &path_stat) == 0) && S_ISDIR(path_stat.st_mode);
}

int is_regularfile(const char *path)
{
    struct stat path_stat;
    return (stat(path, &path_stat) == 0) && S_ISREG(path_stat.st_mode);
}

// current directory file counter
int fileCounter(char *dirname)
{
    DIR *dir;
    struct dirent *dd;
    int nfile = 0;

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
