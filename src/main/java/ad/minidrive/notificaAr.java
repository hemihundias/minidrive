package ad.minidrive;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

/**
 *
 * @author David Pardo
 */

//Mediante esta clase se nos notifica si se ha añadido un nuevo archivo a nuestra BD. 
//Además una vez detectado un nuevo archivo, comprueba si ese archivo existe en el 
//directorio y de no ser así lo descarga.
public class notificaAr extends Thread{
    private Connection conn;
    private operaciones lj = new operaciones();
    public notificaAr(Connection conn){
        this.conn = conn;
    }

    @Override
    public void run(){
        try{            
            while (true){                   
                PGConnection pgconn = conn.unwrap(PGConnection.class);
                Statement stmt = conn.createStatement();
                stmt.execute("LISTEN nuevo_archivo");
                stmt.close();
            
                PGNotification notificationAr[] = pgconn.getNotifications();

                if(notificationAr != null){
                    for (PGNotification notificationAr1 : notificationAr) {
                        int id = Integer.parseInt(notificationAr1.getParameter());
                        String sqlNotAr = "SELECT a.nombre, a.iddirectorio FROM archivos AS a WHERE a.id = ?;";
                        PreparedStatement ps = conn.prepareStatement(sqlNotAr);
                        ps.setInt(1, id);
                        ResultSet rs = ps.executeQuery();
                        rs.next();
                        
                        System.out.println("Nuevo archivo añadido a la base de datos: " + rs.getString(1));
                        rs.close();
                        
                        lj.confConexion();
                        lj.existe();
                    } 
                }                    
                Thread.sleep(16000);
            }        
        } catch (InterruptedException ex) {
            Logger.getLogger(listenner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException | IOException ex) {
            Logger.getLogger(notificaAr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
}      
