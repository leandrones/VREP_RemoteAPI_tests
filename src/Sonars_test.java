import coppelia.IntW;
import coppelia.BoolW;
import coppelia.FloatWA;
import coppelia.remoteApi;

// Make sure to have the server side running in V-REP: 
// in a child script of a V-REP scene, add following command
// to be executed just once, at simulation start:
//
// simExtRemoteApiStart(25000)
//
// then start simulation, and run this program.
//
// IMPORTANT: for each successful call to simxStart, there
// should be a corresponding call to simxFinish at the end!

public class Sonars_test
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

            IntW[] sonar_handles = new IntW[16];

            for(int i = 1; i <= 16; i++){
                String proximity_sensors_name = "Pioneer_p3dx_ultrasonicSensor"+ i;
                //proximity_sensors_name.setValue("Pioneer_p3dx_ultrasonicSensor"+ i);

                sonar_handles[i-1] = new IntW(-1);

                //System.out.println(proximity_sensors_name);

                vrep.simxGetObjectHandle(clientID,proximity_sensors_name, sonar_handles[i-1],remoteApi.simx_opmode_blocking);
                if(sonar_handles[i-1].getValue() == -1)
                    System.out.println("Error on connenting to sensor");
                else
                    System.out.println("Connected to sensor "+i);
                
            }
            
            float dt;
            dt = (float) 1;
            
            vrep.simxSetFloatingParameter(clientID, remoteApi.sim_floatparam_simulation_time_step, dt,
                                           remoteApi.simx_opmode_oneshot);
            
            vrep.simxStartSimulation(clientID,remoteApi.simx_opmode_blocking);

            for(int i = 0; i< 16; i++){
                ret = vrep.simxReadProximitySensor(clientID,sonar_handles[i].getValue(),null,null,null,null,remoteApi.simx_opmode_streaming);
                if(ret == 1){
                    System.out.println("init ok i = "+i);
                }
                else {
                    System.exit(1);
                }
            }
            
            float[] sonar_readings = new float[16];
            BoolW[] detect_state = new BoolW[16];
            float speed;
            readSensors(clientID,sonar_handles,sonar_readings,detect_state);
            
            for(int i = 0; i < 16; i++){
                System.out.print(sonar_readings[i]+ " ");
            }
            System.out.println();
            
            speed = 2;
            vrep.simxSetJointTargetVelocity(clientID,left_motor_h.getValue(),speed,remoteApi.simx_opmode_streaming);
            vrep.simxSetJointTargetVelocity(clientID,right_motor_h.getValue(),speed,remoteApi.simx_opmode_streaming);               
            
            while(sonar_readings[4] > 0.5 || detect_state[4].getValue() == false){
                System.out.println(sonar_readings[4]);
                System.out.println(detect_state[4].getValue());
                readSensors(clientID,sonar_handles,sonar_readings,detect_state);
            }
            speed = 0;
            vrep.simxSetJointTargetVelocity(clientID,left_motor_h.getValue(),speed,remoteApi.simx_opmode_streaming);
            vrep.simxSetJointTargetVelocity(clientID,right_motor_h.getValue(),speed,remoteApi.simx_opmode_streaming);               
            
            try
            {
                Thread.sleep(100);
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }
           
            /**
            while (sonar_readings[4] > 0.5 || detect_state[3].getValue() == false ||
                    detect_state[2].getValue() == false || sonar_readings[5] > 0.5 ||
                    detect_state[6].getValue() == false || detect_state[5].getValue() == false ||
                    detect_state[7].getValue() == false)
            {
                //System.out.println(sonar_readings[4]);
                vrep.simxSetJointTargetVelocity(clientID,left_motor_h.getValue(),speed,vrep.simx_opmode_streaming);
                vrep.simxSetJointTargetVelocity(clientID,right_motor_h.getValue(),speed,vrep.simx_opmode_streaming);
                readSensors(clientID,sonar_handles,sonar_readings,detect_state);
            }
            
            speed = 0;
            vrep.simxSetJointTargetVelocity(clientID,left_motor_h.getValue(),speed,vrep.simx_opmode_streaming);
            vrep.simxSetJointTargetVelocity(clientID,right_motor_h.getValue(),speed,vrep.simx_opmode_streaming);               
            
                        
            */
            vrep.simxFinish(clientID);
            
        }
        else
            System.out.println("Failed connecting to remote API server");
        System.out.println("Program ended");
    }
}
            
