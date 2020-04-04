/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

//Esta clase 
public class notificaDir extends Thread{
    private Connection conn;
    private operaciones lj;
    public notificaDir(Connection conn){
        this.conn = conn;
    }

    @Override
    public void run(){
        try{            
            while (true){                   
                PGConnection pgconn = conn.unwrap(PGConnection.class);
                Statement stmt = conn.createStatement();
                stmt.execute("LISTEN nuevo_directorio");
                stmt.close();
            
                PGNotification notificationDir[] = pgconn.getNotifications();

                if(notificationDir != null){
                    for(int i=0;i < notificationDir.length;i++){

                        int id = Integer.parseInt(notificationDir[i].getParameter());
                        
                        String sqlNotDir = "SELECT d.nombre FROM directorios AS d WHERE d.id = ?;";
        
                        PreparedStatement ps = conn.prepareStatement(sqlNotDir);
                        ps.setInt(1, id);
                        ResultSet rs = ps.executeQuery();
                        rs.next();
                        String nDir = rs.getString(1);
                        System.out.println("Nuevo directorio añadido a la base de datos: " + rs.getString(1));
                        rs.close();
                        
                        if(!lj.existeDir(nDir)){
                            lj.recuperarDir(nDir);
                        }
                    } 
                }    
                //conn.close();
                Thread.sleep(15000);
            }        
        } catch (InterruptedException ex) {
            Logger.getLogger(listenner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(notificaAr.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(notificaDir.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
}      