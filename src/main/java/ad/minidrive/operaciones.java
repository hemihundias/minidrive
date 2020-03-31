/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ad.minidrive;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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

public class operaciones {
    
    jsonConfig conf = new jsonConfig();
    File datos = new File("config.json");;
    Connection conn = null;
    String nombreDir = "."; 
    String dir;      
        
    public void cargaDatos(){
        if(datos.exists()){

            try{     
                FileReader fluxoDatos = new FileReader(datos);                
                BufferedReader buferEntrada = new BufferedReader(fluxoDatos);

                StringBuilder jsonBuilder = new StringBuilder();
                String linea;

                while ((linea=buferEntrada.readLine()) != null) {
                    jsonBuilder.append(linea).append("\n");                    
                }
                String json = jsonBuilder.toString();

                Gson gson = new Gson(); 

                conf = gson.fromJson(json, jsonConfig.class); 
                
            }catch (JsonSyntaxException | IOException e){
                System.err.println(e);
            }    

        }else {
            System.out.println("No existe el fichero de configuración de la conexión.");
        } 
    }    
    
    public void connect(){
        
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
            conn = DriverManager.getConnection(postgres,props);                       
            
        }catch (SQLException ex) {
            System.err.println("No se ha podido conectar a la base de datos\n"+ex.getMessage());            
        }   
        
    }
    
    public void crearTablas() throws SQLException{
        connect();       
        try {
                        
            String sqlTableCreation1 = "CREATE TABLE IF NOT EXISTS directorios ("
                    + "    id SERIAL,"
                    + "    nombre VARCHAR(255) UNIQUE,"
                    + "    PRIMARY KEY(id),"
                    + "    CONSTRAINT ux_nomd UNIQUE (nombre)"
                    + ");"; 
                        
            CallableStatement createFunction = conn.prepareCall(sqlTableCreation1);
            createFunction.execute();
            createFunction.close();
            
            String sqlTableCreation2 = "CREATE TABLE IF NOT EXISTS archivos ("                
                    + "    nombre VARCHAR(255) UNIQUE,"
                    + "    dato BYTEA,"
                    + "    id SERIAL,"
                    + "    iddirectorio INTEGER REFERENCES directorios(id),"
                    + "    PRIMARY KEY(id),"
                    + "    CONSTRAINT ux_nombre UNIQUE (nombre)"
                    + ");";
            
            createFunction = conn.prepareCall(sqlTableCreation2);
            createFunction.execute();
            createFunction.close();
            
        } catch (SQLException ex) {
            System.err.println("Error: " + ex.toString());
        }    
        conn.close();
        String dB = "\\" + "\\";
        dir = conf.getApp().getDirectory().replaceAll("\\",dB );
    }
    
    public void listar(String d) throws SQLException, IOException{
        connect();
        d = dir;        
        File f = new File(d);
        
        if (f.exists()){
            insertarDir(nombreDir);
            File[] ficheros = f.listFiles();
            
            for(int x=0;x<f.listFiles().length;x++){
                if (ficheros[x].isDirectory()){
                    
                }else{                    
                    insertarArchivo(ficheros[x].getName(),f);                    
                }
            }
            
            for(int x=0;x<f.listFiles().length;x++){
                if (ficheros[x].isDirectory()){
                    dir = ficheros[x].getAbsolutePath();
                    nombreDir = nombreDir + File.separator + ficheros[x].getName();
                    listar(d);
                }else{                    
                                        
                }
            }
        }else{
            System.out.println("El directorio a listar no existe.");
        }
        conn.close();
    }
    
    public void insertarArchivo(String nombreAr, File f) throws FileNotFoundException, SQLException, IOException{
        
        String nombreArchivo = nombreAr;
        int IdDir = 0;
        
        File file = new File(f.toString() + File.separator + nombreArchivo);
        FileInputStream fis = new FileInputStream(file);
        String sqlId = "SELECT id FROM directorios WHERE nombre = '" + nombreDir+ "';";
        
        PreparedStatement ps2 = conn.prepareStatement(sqlId);

        ResultSet rs = ps2.executeQuery();
        
        while(rs.next()){
            IdDir = rs.getInt(1);
        }

        //Creamos la consulta para insertar el archivo en la base de datos
        String sqlInsertAr = "INSERT INTO archivos(nombre,dato,iddirectorio)"
                + " VALUES (?,?,?)"
                + " ON CONFLICT (nombre) DO NOTHING;";
        
        PreparedStatement ps = conn.prepareStatement(sqlInsertAr);

        //Añadimos nombre archivo
        ps.setString(1, nombreArchivo);                                      

        //Añadimos el archivo
        ps.setBinaryStream(2, fis, (int)file.length());
        
        //Añadimos nombre archivo
        ps.setInt(3, IdDir);
        
        //Ejecutamos la consulta
        try{
            ps.executeUpdate();
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        

        //Cerrramos la consulta y el archivo abierto
        ps.close();
        fis.close();
    }
    
    public void insertarDir(String nombreD) throws SQLException, IOException{
        
        //Creamos la consulta para insertar el archivo en la base de datos
        String sqlInsertDir = "INSERT INTO directorios(nombre)"
                + "   VALUES (?)"
                + "   ON CONFLICT (nombre) DO NOTHING;";
        
        PreparedStatement ps = conn.prepareStatement(sqlInsertDir);

        //Añadimos nombre archivo
        ps.setString(1, nombreD);                                      


        //Ejecutamos la consulta
        try{
            ps.executeUpdate();
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }

        //Cerrramos la consulta
        ps.close();  
        
    }
    
    public void existe() throws SQLException, IOException{
        connect();       
        String nom,nomD;
        int idD;
                        
        String sqlex = "SELECT a.nombre,d.nombre,a.iddirectorio FROM archivos AS a INNER JOIN directorios AS d ON a.iddirectorio = d.id;"; 

        PreparedStatement ps3 = conn.prepareStatement(sqlex);

        ResultSet rs = ps3.executeQuery();

        while(rs.next()){
            nom = rs.getString(1);
            nomD = rs.getString(2);
            idD = rs.getInt(3);
            
            dir = nomD.replaceAll(".", conf.getApp().getDirectory());
            File fil = new File(dir + File.separator + nom);
            System.out.println(dir);
            if(fil.exists()){
                
            }else{
                recuperar(nom,idD,fil);
            }
            
            
        }
        conn.close();
    }
    
    public void recuperar(String s,int i,File f) throws SQLException, FileNotFoundException, IOException{
        //Creamos a consulta para recuperar a imaxe anterior
    String sqlGet = "SELECT a.dato FROM archivos AS a WHERE (nombre = ? AND id = ?);";
    PreparedStatement ps2 = conn.prepareStatement(sqlGet); 

    //Engadimos o nome da imaxe que queremos recuperar
    ps2.setString(1, s); 

    //Ponemos el id del archivo a recuperar
    ps2.setInt(2, i);
    
    //Executamos a consulta
    ResultSet rs = ps2.executeQuery();

    //Imos recuperando todos os bytes das imaxes
    byte[] arch = null;
    while (rs.next()){ 
        arch = rs.getBytes(1); 
    }

    //Cerramos a consulta
    rs.close(); 
    ps2.close();

    //Creamos o fluxo de datos para gardar o arquivo recuperado
    String archGen = f.getAbsolutePath();
    File fileOut = new File(archGen);
    FileOutputStream fluxoDatos = new FileOutputStream(fileOut);

    //Gardamos o arquivo recuperado
    if(arch != null){
        fluxoDatos.write(arch);
    }

    //cerramos o fluxo de datos de saida
    fluxoDatos.close();  
    }
}
