void winnow(int w /*size of window*/ ){   //size of window should less than total number of hash values
   int *h=new int[w];   //a loop window that holds hash values
     int min=0;      //records the position of the current minimum in the window
    for(int i=0;i<w-1;i++){
        h[i]=next_hash();
        if(h[i]==-1){       //Executed when the window value is greater than the total number of fingerprints
             printf("[%d, %d]\n",h[min],pos);
             delete h;
             return;
        }
    }
   int r=w-1;       //Iterate through groups of H, adding the hash value of the original sequence to the array in order
    int pos=min;     //Record the position where the minimum appears in the original sequence
   while(true){
          r=(r+1)%w;
          h[r]=next_hash();   //Add the next hash value
          if(h[r]==-1)break;  //Pretend end up with -1
          if(min==r){        //If this is true, the minimum value of the previous window will be removed
              pos+=w;
              for(int i=(r-1+w)%w; i!=r; i=(i-1+w)%w){   //Selects the smallest and rightmost hash value in the window
                  if(h[i]<h[min]){
                    pos-=(min+w-i)%w;
                    min=i;
                }
            }
              printf("[%d, %d]\n",h[min],pos);
         }
          else{                             //min!=r indicates that the current minimum hash value is still contained in the window
            if(h[r]<=h[min]){      //We only need to compare the newly added hash value
                  pos+=(r+w-min)%w;
                  min=r;
                  printf("[%d, %d]\n",h[min],pos);
            }
          }
   }
   delete h;
}
