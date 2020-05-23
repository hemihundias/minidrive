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
 * @author David Pardo
 */

public class operaciones {
    private jsonConfig conf = new jsonConfig();
    private final File datos = new File("config.json");;
    private Connection conn = null;
    private String nombreDir = ".";
    
    //En este método cogemos los datos que se nos proporciona en el archivo config.json 
    //para poder configurar nuestra conexión a la base de datos
    public void confConexion(){
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
    
    //usando los datos extraidos mediante el método "confConexión", configuramos 
    //nuestra conexión a la base de datos
    public Connection connect(){
        
        String url = conf.getDbConnection().getAddress();
        String db = conf.getDbConnection().getName();
        
        Properties props = new Properties();
        props.setProperty("user", conf.getDbConnection().getUser());
        props.setProperty("password", conf.getDbConnection().getPassword());

        String postgres = "jdbc:postgresql://"+url+"/"+db;
        
        try {    
            conn = DriverManager.getConnection(postgres,props);                       
            return conn;
        }catch (SQLException ex) {
            System.err.println("No se ha podido conectar a la base de datos\n" + ex.getMessage());
            return null;    
        }           
    }
    
    //Aquí creamos las tablas que vamos a usar, las funciones y trigger para las 
    //notificaciones y además se introduce al final el directorio raíz en la base 
    //de datos mediante el método "insertarDir"
    public void crearTablas() throws SQLException, IOException{
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
            
            notificaDir();
            notificaAr();            
            insertarDir(nombreDir);
            
        } catch (SQLException ex) {
            System.err.println("Error: " + ex.toString());
        }    
        conn.close();
                
    }
        
    //Con este método se recorre recursivamente el directorio que se le pasa como 
    //parámetro llamando a los distintos métodos encargados de insertar los archivos y directorios
    public void listar(File d) throws SQLException, IOException{
        String nom;
        if (d.exists() && d.isDirectory()){            
            
            for(File f:d.listFiles()){
                if (f.isDirectory()){                    
                    nom = f.getAbsolutePath().replace(conf.getApp().getDirectory(), ".");
                    insertarDir(nom);
                    listar(f);
                }else {
                    nom = f.getParentFile().toString().replace(getPath(), ".");
                    insertarArchivo(f.getName(),nom,f.getAbsoluteFile());
                }
            }            
        }else{
            System.out.println("El directorio a listar no existe.");
        }        
    }
    
    //Método para la inserción de los archivos, se le pasa el nombre del archivo 
    //a insertar, el nombre del directorio en el que se encuentra, y el path completo 
    //del archivo. En caso de haber un nombre de archivo igual, no insertará nada
    public void insertarArchivo(String nombreAr, String nomDir, File d) throws FileNotFoundException, SQLException, IOException{
        connect();
        int IdDir = 0;        
                
        String sqlId = "SELECT id FROM directorios WHERE nombre = '" + nomDir + "';";
        
        PreparedStatement ps = conn.prepareStatement(sqlId);

        ResultSet rs = ps.executeQuery();
        
        while(rs.next()){
            IdDir = rs.getInt(1);
        }
        ps.close();
        if(!existeAr(nombreAr,IdDir)){
            FileInputStream fis = new FileInputStream(d);
            String sqlInsertAr = "INSERT INTO archivos(nombre,dato,iddirectorio)"
                + " VALUES (?,?,?)"
                + " ON CONFLICT (nombre) DO NOTHING;";
        
            PreparedStatement ps2 = conn.prepareStatement(sqlInsertAr);

            ps2.setString(1, nombreAr);                                      

            ps2.setBinaryStream(2, fis, (int)d.length());

            ps2.setInt(3, IdDir);

            try{
                ps2.executeUpdate();
            }catch (SQLException e){
                System.err.println(e.getMessage());
            }

            ps2.close();
            fis.close();
        }
        
        conn.close();
    }
    
    //Método para la inserción de los directorios, se le pasa el nombre del directorio 
    //a insertar. En caso de haber otro directorio con el mismo nombre, no insertará nada.
    public void insertarDir(String nombreD) throws SQLException, IOException{
        connect();
        if(!existeDir(nombreD)){
            String sqlInsertDir = "INSERT INTO directorios(nombre)"
                + "   VALUES (?)"
                + "   ON CONFLICT (nombre) DO NOTHING;";
        
            PreparedStatement ps = conn.prepareStatement(sqlInsertDir);

            ps.setString(1, nombreD);

            try{
                ps.executeUpdate();
            }catch (SQLException e){
                System.err.println(e.getMessage());
            }
            ps.close();
        }
                  
        conn.close();
    }
    
    //Método supletorio para recuperar el directorio raíz
    public String getPath(){        
        confConexion();
        return conf.getApp().getDirectory();
    }
    
    //Mediante este método comprobamos al inicio de la aplicación si los directorios, 
    //primero, y los archivos, después, de nuestra base de datos existen en el directorio. 
    //En caso contrario, los descarga llamando a los métodos correspondientes.
    public void existe() throws SQLException, IOException{
        connect();
        String nom,nomD;
        int idD;
          
        String sqled = "SELECT d.nombre FROM directorios AS d;"; 

        PreparedStatement ps = conn.prepareStatement(sqled);

        ResultSet rsq = ps.executeQuery();
        
        while(rsq.next()){
            nomD = rsq.getString(1);
            
            File dirDir = new File(getPath() + nomD.replace(".",""));
            
            if(!dirDir.exists()){
                recuperarDir(nomD);
            }            
        }
        
        String sqlex = "SELECT a.nombre,d.nombre,a.iddirectorio FROM archivos AS a INNER JOIN directorios AS d ON a.iddirectorio = d.id;"; 

        PreparedStatement ps3 = conn.prepareStatement(sqlex);

        ResultSet rs = ps3.executeQuery();

        while(rs.next()){
            nom = rs.getString(1);
            nomD = rs.getString(2);
            idD = rs.getInt(3);            
            File dirAr = new File(getPath() + nomD.replace(".","") + File.separator + nom);
            
            if(!dirAr.exists()){
                recuperarAr(nom,idD,dirAr);
            }
        }
        conn.close();        
    }
    
    //Método para la recuperación de archivos, al que se le pasa nombre de archivo, 
    //id de directorio y path completo del archivo.
    public void recuperarAr(String s,int i,File f) throws SQLException, FileNotFoundException, IOException{
        
        String sqlGet = "SELECT a.dato FROM archivos AS a WHERE (a.nombre = ? AND iddirectorio = ?);";
        PreparedStatement ps = conn.prepareStatement(sqlGet); 

        ps.setString(1, s); 

        ps.setInt(2, i);

        ResultSet rs = ps.executeQuery();

        byte[] arch = null;
        while (rs.next()){ 
            arch = rs.getBytes(1); 
        }

        rs.close(); 
        ps.close();
 
        FileOutputStream flujo = new FileOutputStream(f);

        if(arch != null){
            flujo.write(arch);
        }

        flujo.close();  
    }
    
    //Método para la recuperación de directorios, al que se le pasa nombre del directorio. 
    public void recuperarDir(String s) throws SQLException, FileNotFoundException, IOException{
        File f = new File(s.replace(".", getPath()));
        f.mkdir();        
    }
    
    //Con este método se comprueba si un directorio listado ya existe en la BD
    public boolean existeDir(String s) throws SQLException{
        String sqlId = "SELECT d.nombre FROM directorios AS d WHERE d.nombre = ?;";
        
        PreparedStatement ps = conn.prepareStatement(sqlId);

        ps.setString(1, s);
        
        ResultSet rs = ps.executeQuery();
        
        return rs.next();    
    }
    
    //Con este método se comprueba si un archivo listado ya existe en la BD
    public boolean existeAr(String s,int i) throws SQLException{              
        String sqlAr = "SELECT a.nombre FROM archivos AS a WHERE (a.nombre = ? AND a.iddirectorio = ?);";
        
        PreparedStatement ps2 = conn.prepareStatement(sqlAr);

        ps2.setString(1, s); 

        ps2.setInt(2, i);
    
        ResultSet rs2 = ps2.executeQuery();
        
        return rs2.next();
    }
    
    //Se crea la función y el trigger asociado para las notificaciones de nuevos 
    //archivos añadidos a la BD
    public void notificaAr() throws SQLException{
        String sqlTableCreation = "CREATE OR REPLACE FUNCTION notificaAr()"
                + "    RETURNS trigger AS "
                + "    $BODY$"
                + "    BEGIN" 
                + "    PERFORM pg_notify('nuevo_archivo',NEW.id::text);" 
                + "    RETURN NEW;" 
                + "    END;" 
                + "    $BODY$"
                + "    LANGUAGE plpgsql;";

        CallableStatement createFunction = conn.prepareCall(sqlTableCreation);
        createFunction.execute();
        createFunction.close();
                    
        String sqlTableCreation2 = "DROP TRIGGER IF EXISTS nuevo_archivo ON archivos;"
                + "    CREATE TRIGGER nuevo_archivo "
                + "    AFTER INSERT "
                + "    ON archivos " 
                + "    FOR EACH ROW " 
                + "    EXECUTE PROCEDURE notificaAr();";

        createFunction = conn.prepareCall(sqlTableCreation2);
        createFunction.execute();
        createFunction.close();                
               
    }
    
    //Se crea la función y el trigger asociado para las notificaciones de nuevos 
    //directorios añadidos a la BD
    public void notificaDir() throws SQLException{
        String sqlTableCreation = "CREATE OR REPLACE FUNCTION notificaDir()"
                + "    RETURNS trigger AS "
                + "    $BODY$"
                + "    BEGIN" 
                + "    PERFORM pg_notify('nuevo_directorio',NEW.id::text);" 
                + "    RETURN NEW;" 
                + "    END;" 
                + "    $BODY$"
                + "    LANGUAGE plpgsql;";

        CallableStatement createFunction = conn.prepareCall(sqlTableCreation);
        createFunction.execute();
        createFunction.close();
                    
        String sqlTableCreation3 = "DROP TRIGGER IF EXISTS nuevo_directorio ON directorios;"
                + "    CREATE TRIGGER nuevo_directorio "
                + "    AFTER INSERT "
                + "    ON directorios " 
                + "    FOR EACH ROW " 
                + "    EXECUTE PROCEDURE notificaDir();";

        createFunction = conn.prepareCall(sqlTableCreation3);
        createFunction.execute();
        createFunction.close();               
    }        
}
