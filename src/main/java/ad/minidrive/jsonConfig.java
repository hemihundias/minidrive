/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ad.minidrive;

import java.io.File;

/**
 *
 * @author Hemihundias
 */
public class jsonConfig {
    private dbConnection dbConnection;
    private app app;

    public jsonConfig() {
    }

    jsonConfig(dbConnection dbConnection, app app) {
        this.dbConnection = dbConnection;
        this.app = app;
    }

    dbConnection getDbConnection() {
        return dbConnection;
    }

    void setDbConnection(dbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    app getApp() {
        return app;
    }

    void setApp(app app) {
        this.app = app;
    }    
    
    class dbConnection{
        private String address,name,user,password;

        public dbConnection() {
        }

        public dbConnection(String address, String name, String user, String password) {
            this.address = address;
            this.name = name;
            this.user = user;
            this.password = password;            
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }        
        
    }
    
    class app{
        private String directory;

        public app() {
        }

        public app(String directory) {
            this.directory = directory;
        }

        public String getDirectory() {
            return System.getProperty("user.home") + File.separator + directory;
            
        }
      
        
    }
}
