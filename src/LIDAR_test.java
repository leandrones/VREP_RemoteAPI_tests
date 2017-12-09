import coppelia.IntW;
import coppelia.CharWA;
import coppelia.BoolW;
import coppelia.FloatWA;
import coppelia.remoteApi;

// Make sure to have the server side running in V-REP: 
// in a child script of a V-REP scene, add following command
// to be executed just once, at simulation start:
//
// simExtRemoteApiStart(19999)
//
// then start simulation, and run this program.
//
// IMPORTANT: for each successful call to simxStart, there
// should be a corresponding call to simxFinish at the end!

public class LIDAR_test
{
    public static remoteApi vrep;
    
    public static void readSensors(int clientID,IntW sonar_handles[],float sonar_readings[], BoolW detect_state[]){
        FloatWA coord = new FloatWA(3);
        for (int i = 0; i < 16; i++){
            float[] temp;
            detect_state[i] = new BoolW(false);
            int ret;
            ret = vrep.simxReadProximitySensor(clientID,sonar_handles[i].getValue(),detect_state[i],coord,null,null,remoteApi.simx_opmode_buffer);
            //System.out.println("RET = "+ret);
            if(ret  == 0){
                if(detect_state[i].getValue() == true){
                    //System.out.println("OIIIIIEEEEEEEEE");                    
                }                
            }
            else
                sonar_readings[i] = -1;
            temp = coord.getArray();
            sonar_readings[i] = temp[2];
        }
    }
    
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String[] args)
    {
        System.out.println("Program started");
        vrep = new remoteApi();
        vrep.simxFinish(-1); // just in case, close all opened connections
        int clientID = vrep.simxStart("127.0.0.1",25000,true,true,5000,5);
        if (clientID!=-1)
        {
            System.out.println("Connected to remote API server");   

            // Now try to retrieve data in a blocking fashion (i.e. a service call):
            int ret;

            try
            {
                Thread.sleep(2000);
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }
                        
            ret = vrep.simxSynchronous(clientID,true);
            if(ret == remoteApi.simx_return_ok)
                vrep.simxSynchronousTrigger(clientID);
    
            IntW p3dx_handle = new IntW(-1);
            vrep.simxGetObjectHandle(clientID,"Pioneer_p3dx",p3dx_handle, remoteApi.simx_opmode_blocking);
            
            IntW left_motor_h = new IntW(-1);
            vrep.simxGetObjectHandle(clientID,"Pioneer_p3dx_leftMotor", left_motor_h,remoteApi.simx_opmode_blocking);
            IntW right_motor_h = new IntW(-1);
            vrep.simxGetObjectHandle(clientID,"Pioneer_p3dx_rightMotor", right_motor_h,remoteApi.simx_opmode_blocking);

            //IntW[] sonar_handles = new IntW[16];
            
            float dt;
            dt = (float) 1;
            
            vrep.simxSetFloatingParameter(clientID, remoteApi.sim_floatparam_simulation_time_step, dt,
                                           remoteApi.simx_opmode_oneshot);
            
            vrep.simxStartSimulation(clientID,remoteApi.simx_opmode_blocking);
            
            CharWA signal_laser_value = new CharWA("");
            
            vrep.simxGetStringSignal(clientID, "LaserSignal", signal_laser_value, remoteApi.simx_opmode_streaming);
            
            while(vrep.simxGetConnectionId(clientID) != -1){
                if(vrep.simxGetStringSignal(clientID, "LaserSignal", signal_laser_value, remoteApi.simx_opmode_buffer) == remoteApi.simx_error_noerror){
                    int no_of_floats = signal_laser_value.getLength() / 4;
                    float[] laser_data = new float[no_of_floats];
                    char[] values_char = signal_laser_value.getArray();

                    System.out.println("no_of_floats = "+no_of_floats);

                    for(int i = 0; i < no_of_floats; i++){
                        laser_data[i] = (float) values_char[4*i];
                    }
                    System.out.println("laser_data = "+laser_data[50]);
                }
                try
                {
                    Thread.sleep(500);
                }
                catch(InterruptedException ex)
                {
                    Thread.currentThread().interrupt();
                }
            }
            vrep.simxFinish(clientID);
            
        }
        else
            System.out.println("Failed connecting to remote API server");
        System.out.println("Program ended");
    }
}
            
