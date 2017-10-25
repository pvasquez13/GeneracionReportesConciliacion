package com.globokas.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 * @author pvasquez
 */
public class SqlConection {

    private static final Logger LOG = Logger.getLogger(SqlConection.class);

    // TODO Auto-generated method stub
    public Connection SQLServerConnection(String database) throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException cnfe) {
            LOG.debug("No encuentra driver");
            cnfe.printStackTrace();
        }
        Connection c = null;

        try {

            String databaseNombre = "";
            String userDatabase = "";
            String passDatabase = "";
            String ipDataBase = ConfigApp.getValue("ip_bd_produccion");

            if (database.equalsIgnoreCase("G")) {
                databaseNombre = ConfigApp.getValue("databasename_gestion");
                userDatabase = ConfigApp.getValue("user_gestion");
                passDatabase = "PassControl$";
//                passDatabase = ConfigApp.getValue("pass_gestion");
            } else if (database.equalsIgnoreCase("R")) {
                databaseNombre = ConfigApp.getValue("databasename_repositorio");
                userDatabase = ConfigApp.getValue("user_repositorio");
                passDatabase = "PassControl$";
//                passDatabase = ConfigApp.getValue("pass_repositorio");
            }

            c = DriverManager.getConnection("jdbc:sqlserver://" + ipDataBase + ";databaseName=" + databaseNombre + ";user=" + userDatabase + ";password=" + passDatabase + ";");

        } catch (SQLException se) {
            se.printStackTrace();
        }

        if (c == null) {
            LOG.debug("No Conecto!");
        }
        return c;

    }

}
