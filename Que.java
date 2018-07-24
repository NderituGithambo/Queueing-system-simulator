
import java.io.*;
import java.util.Scanner;


public class Que {
 
//global variables definition for single server queuing system     
    
 public static final int Q_LIMIT = 100;
 public static final int BUSY = 1;
 public static final int IDLE = 0;
 
static int next_event_type, num_custs_delay,num_delay_req,num_events, num_in_q,server_status;
static float area_num_in_q,area_server_status,mean_interarrival,mean_service,time,
        time_last_event,total_of_delays;
static float [] time_arrival = new float [ Q_LIMIT + 1 ];
static float []  time_next_event = new float [3];

         
static FileReader in=null;
static FileWriter out=null;

//main function

   public static void main(String[] args) throws FileNotFoundException, IOException {
      //specify number of event for timing fuction 
        num_events=2;
     
     try{
          //opening input and output files
         in= new FileReader("mm1.txt");
         out= new FileWriter("mm2.txt");  
         
     //reading input parameters from the input file
     Scanner sc = new Scanner(in);
     String data;
      while (sc.hasNextLine()){
     data=sc.nextLine();
     
     String [] token =data.split(" ");
     mean_interarrival= Float.parseFloat(token[0]);
     mean_service=Float.parseFloat(token[1]);
     num_delay_req=Integer.parseInt(token[2]);
     
     //write report heading and input parameters to the output file
     
     out.write("SINGLE_SERVER QUEUEING SYSTEM \r\n");
     out.write(".............................\r\n");
     
     out.write(" Mean interarrival time is "+mean_interarrival+ " minutes\r\n");
     
     out.write(" Mean service time is "+mean_service+ " minutes\r\n");
   
     out.write(" Number of customers is "+num_delay_req);
     
     
     out.write("\r\n");
   
      }
     
     }
     catch(Exception e){
         
    e.printStackTrace();
      
      }
     
    
     //initialize the simulation
     initialize();
     
     //run the simulation while more delays are still needed
     while(num_custs_delay<num_delay_req){
     //determining the next event
         timing();
     
     //update time average statistical accumulators
         update_time_avg_stats();
         
     //invoke appropriate event function
         switch(next_event_type){
         
             case 1:
                 arrive();
                 break;
             case 2:
                 depart();
                 break;
         
         }
     
     }
    //invoke the report generator and end the simulation 
     report();
   
    }
    
  //initialization method
  static void initialize(){
    
    time=(float)0.0;
    
    //initialize the state variable
    server_status = IDLE;
    num_in_q = 0;
    time_last_event = (float) 0.0;
    
    //initialize statistical counters
    num_custs_delay = 0;
    total_of_delays = (float) 0.0;
    area_num_in_q = (float) 0.0;
    area_server_status = (float) 0.0;
    
    // initialize event list
    time_next_event [1] = (float) (time  + expon(mean_interarrival));
    time_next_event [2] = (float) (1.0e30);
    
    
    }
  
  
 //timing method 
  static void timing() throws IOException{
  int i;
 
float min_time_next_event = (float) 1.0e29;

// Determine the event type of the next event to occur. 

for (i = 1; i <= num_events; i++) {
    
if (time_next_event[i] < min_time_next_event){
    
min_time_next_event = time_next_event[i];
next_event_type = i;

}
}
/* Check to see whether the event list is empty.
The event list is empty, so stop the simulation. */

if(next_event_type==0){
    
out.write( "\r\nEvent list empty at time "+ time);
System.exit(i);

// The event list is not empty, so advance the simulation clock.
  }
else{
    
time = min_time_next_event;

}
  }

  
  
  //arrival event method
  static void arrive(){
  float delay;
  
  //schedule next arrival
   time_next_event [1] = (float) (time + expon(mean_interarrival));
  
  //check to see whether server is busy
   if(server_status==BUSY){
       
   //server is busy, so increment number of customers in the queue    
   ++num_in_q;
   
   //check to see whether an overflow condition exist
   if(num_in_q > Q_LIMIT){
   
       //the queue has overflowed, so stop the simulation
       try {
           out.write("\r\n Overflow of the array time_arrival at "+ time+ "time");
           System.exit(2);
       } 
       catch (IOException ex) {
           ex.printStackTrace();
       }
   }
   
   /*there is still room in the queue,so store the time of 
   arrival of the arriving customer at the (new)end of time_arrival
    */
   time_arrival[num_in_q]= time;
   
   }
   
   else{
       
   /*server is idle so the arriving customer has a delay of zero. the following 
     two statements are for program clarity and do not affect the results of simulation
      */
       
   delay = (float) 0.0;
   total_of_delays  = total_of_delays + delay;
   
   //increment the number of customers delayed and make the server busy
   ++ num_custs_delay;
   server_status = BUSY;
   
   //schedule a departure (service completion)
   time_next_event [2] = (float) (time + expon(mean_service));
   
   }
   
  }
  
  
  //departure event method
  static void depart(){
  int i;
  float delay;
  
  //check to see whether the queue is empty
  if(num_in_q==0){
      
    /*the queue is empty so make the server idle and eliminate the 
      departure (service completion) event from consideration 
     */
      server_status = IDLE;
      time_next_event [2] = (float) 1.0e30;
  }
  else{
      
  //the queue is non-empty, so decrement the number of customers in the queue
  -- num_in_q;
  
  /* compute the delay of the customer who is beginning 
  service and update the total delay accumulator
    */
  delay =  time - time_arrival[1];
  total_of_delays = total_of_delays + delay;
  
  //increment the number of customers delayed and schedule departure
  ++num_custs_delay;
  time_next_event [2] = (float) (time + expon(mean_service));
  
  //move each customer in queue (if any) up one place
  for(i=1; i<= num_in_q; i++){
  time_arrival [i] = time_arrival [i + 1];
  
  }
  }
  }
  
  
  //Report generator method
  static void report(){
  
     try {
         
         //compute and write estimates of desired measure of performance
         out.write("\r\n Average delay in queue is "+ (total_of_delays/num_custs_delay));
         out.write("\r\n Average number in queue is "+ (area_num_in_q/time));
         out.write("\r\n Server utilization is "+ (area_server_status/time));
         out.write("\r\n Time simulation ended at "+ time);
         
         
      out.close();
     }
     catch (IOException ex) {
        ex.printStackTrace();
       
     }
  }
  
  
  //update area accumulators for time-average statistics
  static void update_time_avg_stats(){
      
  float time_since_last_event;
  
  //compute time since last event, and update last-time-event marker
  time_since_last_event = time - time_last_event;
  time_last_event = time;
  
  //update area under number-in-queue fuction
  area_num_in_q = area_num_in_q + (num_in_q * time_since_last_event);
  
  //update area under server-busy indicator function
  area_server_status = area_server_status + (server_status * time_since_last_event);
  
  }
  
  
  
//Exponential variate generation function.
  static double expon(double mean) {
// Generate x[0,1) random variates
double x;
x=Math.random();
return (double) (-mean * Math.log(x));
 
}
}

    

