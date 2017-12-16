import coppelia.CharWA;
import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;



/**
 *
 * @author leandro
 */
public class VisionSensor_test {

    public static remoteApi vrep;
    
    @SuppressWarnings({"UnusedAssignment", "ConvertToTryWithResources"})
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println("Program started");
        vrep = new remoteApi();
        vrep.simxFinish(-1); // just in case, close all opened connections
        int clientID = vrep.simxStart("127.0.0.1",25000,true,true,5000,5);
        if (clientID!=-1)
        {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
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
                ret = vrep.simxSynchronousTrigger(clientID);
            
            float dt;
            dt = (float) 1;
            
            vrep.simxSetFloatingParameter(clientID, remoteApi.sim_floatparam_simulation_time_step, dt,
                                           remoteApi.simx_opmode_oneshot);
            
            vrep.simxStartSimulation(clientID,remoteApi.simx_opmode_blocking);
            
            int res = 256;
            CharWA image = new CharWA(res*res*3);
            System.out.println("image size "+image.getLength());
            IntWA res_arr = new IntWA(2);
            
            IntW vision_sensor_h = new IntW(0);
            //IntW passive_vision_sensor_h = new IntW(0);
            
            vrep.simxGetObjectHandle(clientID, "Vision_sensor", vision_sensor_h,remoteApi.simx_opmode_oneshot_wait);
            //vrep.simxGetObjectHandle(clientID, "PassiveVision_sensor", passive_vision_sensor_h,vrep.simx_opmode_oneshot_wait);
            vrep.simxGetVisionSensorImage(clientID, vision_sensor_h.getValue(), res_arr, image, 0, remoteApi.simx_opmode_streaming);
            
            while(vrep.simxGetConnectionId(clientID) != -1){
                if(vrep.simxGetVisionSensorImage(clientID, vision_sensor_h.getValue(), res_arr, image, 0, remoteApi.simx_opmode_buffer) ==
                   remoteApi.simx_error_noerror){
                    //vrep.simxSetVisionSensorImage(clientID, passive_vision_sensor_h.getValue(), image, res*res*3, 0, vrep.simx_opmode_oneshot);
                }
            }
            
            vrep.simxFinish(clientID);
            
            System.out.println("image size "+image.getLength());
            System.out.println("res_arr 0 e 1 "+res_arr.getArray()[0]+" "+res_arr.getArray()[1]);
            
            char[] img = image.getArray();
            
            System.out.println("size img "+img.length);
            
            try {
                PrintWriter pr = new PrintWriter("byteMatrix.txt");

                for(int k = 0; k < 3; k++){
                    for(int i = 0; i < res; i++){
                        for(int j = 0; j < res; j++){
                            pr.print((int)img[k*2*2 + i*2 + j]+",");
                        }
                        pr.println();
                    }
                    pr.println();
                }
                pr.close();
                
                PrintWriter pr2 = new PrintWriter("byteArray.txt");
                
                for(int i = 0; i < img.length;i++){
                    pr2.println((int)img[i]);
                }
            
            } catch (FileNotFoundException ex) {
                Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            byte[] bytes = new byte[res*res*3];
            
            for(int i = 0; i < res*res*3; i++){
                bytes[i] =  (byte) (img[i] & 0x00FF);
            }
           
            Mat mat = new Mat(res,res,CvType.CV_8UC3);
            
            mat.put(0, 0, bytes);
            
            boolean imwrite = Imgcodecs.imwrite("teste.jpg", mat);
            
            if(imwrite == false){
                System.out.println("Error");
            }            
        }
    
    }
}
