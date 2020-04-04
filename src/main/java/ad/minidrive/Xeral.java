
package ad.minidrive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author David pardo
 */
public class Xeral { 
    private static operaciones lj = new operaciones();     
    private static File d;
    public static void main(String args[]) throws FileNotFoundException, SQLException, IOException{      
        lj.confConexion();        
        lj.crearTablas();        
        d = new File(lj.getPath());
        lj.existe();
        lj.listar(d);
        
        Connection conNotAr = lj.connect();
        listenner listen = new listenner(d);
        notificaAr notifAr = new notificaAr(conNotAr);
        notificaDir notifDir = new notificaDir(lj.connect());
        
        listen.start();
        notifAr.start();
        notifDir.start();
    }
}
