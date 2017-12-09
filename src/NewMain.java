/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author leandro
 */
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        float[] a = new float[16];
        
        for(int i = 0; i < 16; i++){
            a[i] = i;
        }
        System.out.println(a[4]);
        
        try {
            PrintWriter pr = new PrintWriter("a.txt");
            
            for(int k = 0; k < 3; k++){
                for(int i = 0; i < 2; i++){
                    for(int j = 0; j < 2; j++){
                        pr.print(a[k*2*2 + i*2 + j]+",");
                    }
                    pr.println();
                }
                pr.println();
                
            }
            pr.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
