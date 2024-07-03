package org.radio.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CConnection {
    private static final Logger logger = Logger.getLogger(CConnection.class.getName());
    private static final String URL = "jdbc:mysql://localhost:3306/radiodb";
    public static Connection connection() throws SQLException{
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            //return DriverManager.getConnection(URL, "root", "root");
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE,"No se encontro la clase com.mysql.cj.jdbc.Driver");
        }
        Connection conn = DriverManager.getConnection(URL, "root", "root");
        return conn;
    }
}
