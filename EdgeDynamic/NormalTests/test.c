#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include<sys/stat.h>


int main () {
	   char command[50];

   strcpy( command, "dir" );
   system(command);
   int NodeNumber[5] = {100, 200, 300, 400, 500};
   double changerate[1] = {0.1};//0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09,
   double density[9] = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
   int i, j, k, l;
   FILE *fp = NULL;
   printf("Hello");
   for (i = 0 ; i < 5; i++){
	   for (j = 0 ; j < 1; j++){
		   for (k = 0 ; k < 9; k++){
					char FileName[500];
					//sprintf(FileName,"NodeDynamic_%d_%.2f_%.2f.txt", NodeNumber[i], changerate[j], density[k]);
			   	   	sprintf(FileName,"mv %d/EdgeDynamic_%d_%.2f_%.1f.txt %d/EdgeDynamic_%d_%.1f_%.1f.txt",NodeNumber[i], NodeNumber[i], changerate[j], density[k], NodeNumber[i], NodeNumber[i], changerate[j], density[k]);					
			   	   	//sprintf(FileName,"cp %d/testNewDPBEA2_%.1f_%.1f_%d-* %d/EdgeDynamic_%d_%.2f_%.2f.txt",NodeNumber[i], changerate[j], density[k], NodeNumber[i], NodeNumber[i], NodeNumber[i], changerate[j], density[k]);
					system(FileName);
					/*printf(FileName);
					printf("\n");
					fp = fopen(FileName ,"w+");
					if (fp == NULL) {
						perror("fopen");
					}*/
					//fclose(fp);
		   }
	   }

   }
   return 0;
   
 }