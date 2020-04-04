/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ad.minidrive;

import java.io.File;
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
public class notificaAr extends Thread{
    private Connection conn;
    private operaciones lj;
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
                    for(int i=0;i < notificationAr.length;i++){

                        int id = Integer.parseInt(notificationAr[i].getParameter());
                        
                        String sqlNotAr = "SELECT a.nombre, a.iddirectorio FROM archivos AS a WHERE a.id = ?;";
        
                        PreparedStatement ps = conn.prepareStatement(sqlNotAr);
                        ps.setInt(1, id);
                        ResultSet rs = ps.executeQuery();
                        rs.next();
                        String nAr = rs.getString(1);
                        int idd = rs.getInt(2);
                        System.out.println("Nuevo archivo añadido a la base de datos: " + rs.getString(1));
                        rs.close();
                        
                        String sqlDir = "SELECT d.nombre FROM directorios AS d WHERE d.id = ?;";
        
                        PreparedStatement ps2 = conn.prepareStatement(sqlDir);
                        ps.setInt(1, idd);
                        ResultSet rs2 = ps2.executeQuery();
                        rs2.next();
                        String nDir = rs2.getString(1);
                        File file = new File(lj.getPath() + nDir.replace(".", ""));
                        rs2.close();
                        
                        if(!lj.existeAr(nAr,idd)){
                            lj.recuperarAr(nAr,id,file);
                        }
                    } 
                }    
                //conn.close();
                Thread.sleep(16000);
            }        
        } catch (InterruptedException ex) {
            Logger.getLogger(listenner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(notificaAr.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(notificaAr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
}      
