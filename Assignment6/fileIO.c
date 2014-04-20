#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <sys/timeb.h>
#include <sys/types.h>
#include <string.h>
#include <signal.h>

#define SLEEPTIME 3000
#define RUNTIME 30
#define BUFFSIZE 512

#define STDIN 0
#define STDOUT 1

int timeUp = 0; // == 1 time out
struct itimerval parentTimer;
void getTime(int * sec, int * mSec, struct timeval * start) {
    struct timeval now;
    gettimeofday(&now, NULL);
    long int diff = (now.tv_usec + 1000000 * now.tv_sec) - (start->tv_usec + 1000000 * start->tv_sec);
    diff = diff/1000;// millisecond
    *sec = (int)diff/1000 ;
    *mSec = (int)diff%1000; 
}


int main(int argc,char* argv[]){
    printf("Start of Program\n");
    int fd [5][2]; 
    fd_set set;
    struct timeval timeout;
    /*init file descriptor set*/
    int id = 0;
    for (id = 0; id < 5; id++) {
        pipe(fd[id]);
    }
    /*init time stamp calculation*/
    struct timeval start;
    gettimeofday(&start, NULL); 
    /*init child no and counter*/
    int childNo = 0;
    int i = 0;
    pid_t process_id = 0;
    for (i=0; i<5;i++) {
	childNo++;
        process_id = fork();
        if (process_id < 0) {
            printf("fork failed - %d!\n",i);
            continue;
        }
	/*parent, read from pipe*/
        else if(process_id > 0) {
	    if (childNo < 5) {
		continue; /*fork 5 children before start reading from input*/
	    }	
	    int j = 0;
	    for (j = 0; j < 5; j++) {
	    	close(fd[j][1]);   /* parent does not write to pipe */
	    }
	    int out = open("output.log", O_RDWR|O_CREAT|O_APPEND, 0600);
	    int save_out = dup(fileno(stdout));
	    dup2(out, fileno(stdout));

	    while(!timeUp) {
		FD_ZERO(&set);
		int k = 0;
		int max = fd[0][0];
		for (k = 0; k < 5; k++) {
    		    FD_SET(fd[k][0], &set);
		    if (fd[k][0] > max) {
			max = fd[k][0];
		    } 
		}
		int ret = select(max+1, &set, NULL, NULL, NULL); 
		if (ret <= 0) {
			printf("error!\n");
		    }
		else {
		    int m = 0;
		    for (m = 0; m < 5; m++) {
			if (FD_ISSET(fd[m][0], &set) > 0) {
		    	    char buf[BUFFSIZE];
		    	    memset(buf, 0, BUFFSIZE);
		    	    read(fd[m][0], buf, BUFFSIZE);
		    	    if (buf[0] == '0') {
				timeUp = 1;
				break;
                            }
		    	    int sec, mSec;
		    	    getTime(&sec, &mSec, &start);
			    printf("Parent(%d) Received Time: 0:%02d.%03d: %s\n", getpid(), sec, mSec, buf);
			}
		    }
		}
	    }
	    int n = 0;
	    for(n = 0; n < 5; n++)
	        close(fd[n][0]);
	    fflush(stdout);
	    dup2(save_out, fileno(stdout));
	    close(save_out);
	    close(out);
	    printf("Time Up, please check output.log for result.\n");
	    printf("End of Program\n");
        }

        /*children, write to pipe*/
        else {
	    close(fd[i][0]);   /*child does not read from pipe*/
	    int messNo = 0;
	    while(1){
		int sec, mSec;
		getTime(&sec, &mSec, &start);
		if (sec < RUNTIME) {
		    if (childNo == 5) {/*fifith child prompt to the terminal*/
			char buffer[BUFFSIZE];
			memset(buffer, 0, BUFFSIZE);
			printf("Enter a message for child 5: \n");
			while( fgets(buffer, BUFFSIZE , stdin) ) { /* break with ^D or ^Z */
			    int sec, mSec;
			    getTime(&sec, &mSec, &start);
			    char str[] = "Sent Time: 0:%02d.%03d Message : Child %d Message %s";
  		    	    char str2[BUFFSIZE];
			    strtok(buffer, "\n");
  		    	    sprintf(str2,str,sec, mSec, childNo, buffer);
		    	    write(fd[i][1], str2, sizeof(str2));
			    memset(buffer, 0, BUFFSIZE);
			    printf("Enter a message for child 5: \n");
			}
             	    }
		    else {
			srand(time(NULL) ^ (getpid()<<16));
			usleep((rand() % SLEEPTIME) * 1000);
			//printf("random number %d\n", rand()%SLEEPTIME);
			messNo++;
			int sec, mSec;
			getTime(&sec, &mSec, &start);
		        char str[] = "Send Time: 0:%02d.%03d Message : Child %d message %d";
  		        char str2[BUFFSIZE];
  		        sprintf(str2,str,sec, mSec, childNo, messNo);
		        write(fd[i][1], str2, sizeof(str2));
		    }
		}
		else {
		    char end = '0';
		    write(fd[i][1], &end, sizeof(char));  
		    close(fd[i][1]);
		    return 0;
		}
	    }
        }
    }
    return 0;
}
