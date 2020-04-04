package ad.minidrive;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author David Pardo
 */

//Esta clase es la encargada de monitorear el directorio en busca de nuevos directorios 
//o archivos. A su constructor se pasa el directorio raíz y llama al método listar(). 
public class listenner extends Thread{
    private File d;
    private operaciones lj = new operaciones();
    
    listenner(File d) throws SQLException{
        this.d = d;        
    }

    @Override
    public void run(){
        try{
            while (true){                   
                System.out.println("Comprobando...");
                
                lj.listar(d);
                
                Thread.sleep(10000);
            }        
        } catch (IOException | InterruptedException | SQLException ex) {
            Logger.getLogger(listenner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
