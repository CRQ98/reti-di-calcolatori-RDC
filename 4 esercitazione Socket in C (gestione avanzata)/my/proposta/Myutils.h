#ifndef MYUTILS_H
#define MYUTILS_H
#define FILENAME_LENGTH 128
#define DIM_BUFF 1024
#define max(a, b) ((a) > (b) ? (a) : (b))
typedef struct
{
    char word[128];
    char filename[FILENAME_LENGTH];
} Request;
void prompt(char *s);
int get_port_from_string(const char *portstr);
void consumptionstdin();
int readc(char *c, int fin);
int writec(char *c, int fout);
void inputoutput(const int fd_in, const int fd_out);
void inputoutput_withpattern(int fd_in, int fd_out);
off_t get_filesize(const char *filename);
void gestore(const int signo);
int is_directory(const char *path);
int is_regularfile(const char *path);
int fileCounter(char *dirname);
#endif