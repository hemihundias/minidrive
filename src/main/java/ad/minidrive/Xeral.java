/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ad.minidrive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

/**
 *
 * @author Hemihundias
 */
public class Xeral { 
        
    public static void main(String args[]) throws FileNotFoundException, SQLException, IOException{        
        leerJson lj = new leerJson();
        operacionesBD obd = new operacionesBD();  
        jsonConfig conf = new jsonConfig();
        String nombreDir = "."; 
        String dir;               
        
        lj.cargaDatos();        
        obd.crearTablas();
        dir = conf.getApp().getDirectory();
        
        File f = new File(dir);
        if (f.exists()){
            obd.insertarDir(nombreDir);
            File[] ficheros = f.listFiles();
            
            for(int x=0;x<f.listFiles().length;x++){
                if (ficheros[x].isDirectory()){
                    //f = new File(f.toString() + File.separator + ficheros[x].getName());
                    //nombreDir = nombreDir + File.separator + ficheros[x].getName();
                    //listar();
                }else{                    
                    obd.insertarArchivo(ficheros[x].getName(),f);                    
                }
            }
        }else{
            System.out.println("El directorio a listar no existe.");
        }
        
    }
}
