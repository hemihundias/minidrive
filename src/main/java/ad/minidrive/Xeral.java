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
        operaciones lj = new operaciones();                              
        File d = new File(System.getProperty("user.home"));
        lj.cargaDatos();        
        lj.crearTablas();        
        lj.existe();
        lj.listar(d);
        
        
        
        
        
    }
}
