/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ad.minidrive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Hemihundias
 */
public class operacionesBD {  
    private static final jsonConfig conf = new jsonConfig();
    
    public static Connection connect() throws SQLException{
        
        //URL y base de datos a la que nos conectamos
        String url = conf.getDbConnection().getAddress();
        String db = conf.getDbConnection().getName();
        
        //Indicamos las propiedades de la conexión
        Properties props = new Properties();
        props.setProperty("user", conf.getDbConnection().getUser());
        props.setProperty("password", conf.getDbConnection().getPassword());

        //Dirección de conexión a la base de datos
        String postgres = "jdbc:postgresql://"+url+"/"+db;
        
        //Conexión
        try {    
            Connection conn = DriverManager.getConnection(postgres,props);
            
            return conn;
            
        }catch (SQLException ex) {
            System.err.println("No se ha podido conectar a la base de datos\n"+ex.getMessage());
            return null;
        }            
    }  
    
    public void crearTablas(){
        
        try {
            Connection conn = connect();
            
            String sqlTableCreation1 = "CREATE TABLE IF NOT EXISTS directorios (\n"
                    + "    id SERIAL,\n"
                    + "    nombre VARCHAR(255) NOT NULL,\n"
                    + "PRIMARY KEY(id)"
                    + ");"; 
                        
            CallableStatement createFunction = conn.prepareCall(sqlTableCreation1);
            createFunction.execute();
            createFunction.close();
            
            String sqlTableCreation2 = "CREATE TABLE IF NOT EXISTS archivos (\n"                
                    + "    nombre VARCHAR(255) NOT NULL,\n"
                    + "    dato BYTEA NOT NULL,\n"
                    + "    id SERIAL,\n"
                    + "    id_directorio INTEGER REFERENCES directorios(id),\n"
                    + "PRIMARY KEY(id)\n"
                    + ");";
            
            createFunction = conn.prepareCall(sqlTableCreation2);
            createFunction.execute();
            createFunction.close();
            
        } catch (SQLException ex) {
            System.err.println("Error: " + ex.toString());
        }        
    }
    
    public void insertarArchivo(String nombreAr, File f) throws FileNotFoundException, SQLException, IOException{
        Connection conn = connect();
        String nombreArchivo = nombreAr;
        int IdDir = 0;
        
        File file = new File(f.toString() + File.separator + nombreArchivo);
        FileInputStream fis = new FileInputStream(file);
        String sqlId = "SELECT id FROM directorios WHERE nombre=" + f.toString().replaceAll(conf.getApp().getDirectory(), ".");
        
        PreparedStatement ps2 = conn.prepareStatement(sqlId);

        ResultSet rs = ps2.executeQuery();
        
        while(rs.next()){
            IdDir = rs.getInt(1);
        }



        //Creamos la consulta para insertar el archivo en la base de datos
        String sqlInsertAr = "INSERT INTO archivos(nombre,dato,id_directorio)\n"
                + "   VALUES (?,?,?);";
        
        PreparedStatement ps = conn.prepareStatement(sqlInsertAr);

        //Añadimos nombre archivo
        ps.setString(1, nombreArchivo);                                      

        //Añadimos el archivo
        ps.setBinaryStream(2, fis, (int)file.length());
        
        //Añadimos nombre archivo
        ps.setInt(3, IdDir);
        
        //Ejecutamos la consulta
        ps.executeUpdate();

        //Cerrramos la consulta y el archivo abierto
        ps.close();
        fis.close();
    }
    
    public void insertarDir(String nombreD) throws SQLException, IOException{
        Connection conn = connect();
        //Creamos la consulta para insertar el archivo en la base de datos
        String sqlInsertDir = "INSERT INTO directorios(nombre)\n"
                + "   VALUES (?);";
        
        PreparedStatement ps = conn.prepareStatement(sqlInsertDir);

        //Añadimos nombre archivo
        ps.setString(1, nombreD);                                      


        //Ejecutamos la consulta
        ps.executeUpdate();

        //Cerrramos la consulta
        ps.close();  
        
    }
}
