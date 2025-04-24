#ifndef MYUTILS_H
#define MYUTILS_H
#define FILENAME_LENGTH 128
#define max(a, b)        ((a) > (b) ? (a) : (b))
#define DIM_BUFF         256
int getportfromstring(const char *portstr);
void consumptioninput();
void inputoutput(const int fd_in,const int fd_out);
void inputoutputwithpattern(int fd_in, int fd_out);
void gestore(const int signo);
int is_directory(const char *path);
int is_regularfile(const char *path);
#endif