/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.common.helpers;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;


/**
 * Class to manage database connection for the given pool.
 */
public class DBManager {
    /* Log4j logger for class  */
    private final static Logger m_log = Logger.getLogger("org.glite.data.common.helpers.DBManager");
    protected final String m_db_pool_name;

    // The datasource from which to fetch the connections
    protected final DataSource m_dataSource;

    /**
     * Create a manager for the given poolname. Multiple managers can be active on any one pool. Just like the real
     * thing.
     *
     * @param pool
     * @throws DBException
     */
    public DBManager(String pool) throws DBException {
        m_log.debug("Created new DBManager");

        try {
            m_db_pool_name = pool;

            Context initCtx = new InitialContext();
            m_log.debug("Fetched initial context");

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            m_log.debug("Fetched envirnoment context");

            // Look up our data source
            m_log.debug("Looking up JNDI datasource: " + m_db_pool_name);

            m_dataSource = (DataSource) envCtx.lookup(m_db_pool_name);
        } catch (NamingException e) {
            m_log.error("Got naming error trying to fetch pool: " + pool, e);
            throw new DBException();
        }
    }

    /**
     * Fetch a cvonnection from the pool.
     *
     * @param autocommit Boolean flag for autocommit
     * @return the connection.
     * @throws org.glite.data.common.helpers.DBException
     *          if a connection cannot be fetched.
     */
    public Connection getConnection(boolean autocommit)
        throws DBException {
        Connection conn;

        try {
            m_log.debug("Trying to get connection wo sync"); 
            conn = m_dataSource.getConnection();
            m_log.debug("Setting AutoCommit " + (autocommit ? "true" : "false"));

            conn.setAutoCommit(autocommit);
        } catch (SQLException e) {
            m_log.error("Could not get connection to the database from pool: " + m_db_pool_name, e);
            throw new DBException();
        }

        return conn;
    }

    /**
     * Create a basic statement object from the connection.
     *
     * @param conn
     * @return
     * @throws DBException
     */
    public Statement createStatement(Connection conn)
        throws DBException {
        if (conn == null) {
            m_log.error("Trying to create statement on closed connection from pool: " + m_db_pool_name);
            throw new DBException();
        }

        Statement statement;

        try {
            statement = conn.createStatement();
        } catch (SQLException e) {
            m_log.error("Failed to get a statement object from db connection on pool: " + m_db_pool_name, e);
            throw new DBException();
        }

        return statement;
    }

    /**
     * Create a basic statement object from the connection.
     *
     * @param conn
     * @return
     * @throws DBException
     */
    public PreparedStatement prepareStatement(Connection conn, String sqlQuery)
        throws DBException {
        if (conn == null) {
            m_log.error("Trying to create statement on closed connection from pool: " + m_db_pool_name);
            throw new DBException();
        }

        PreparedStatement statement;

        try {
            statement = conn.prepareStatement(sqlQuery);
        } catch (SQLException e) {
            m_log.error("Failed to get a statement object from db connection on pool: " + m_db_pool_name, e);
            throw new DBException();
        }

        return statement;
    }

    /**
     * Rollback a connectiopn and throw an excpetion if it fails.
     *
     * @param conn
     * @throws DBException
     */
    public  void rollback(Connection conn)
        throws DBException {
        try {
            conn.rollback();
        } catch (SQLException e) {
            m_log.error("Error rolling back connection from pool: " + m_db_pool_name, e);
            throw new DBException();
        }
    }

    /**
     * Rollback a connection, returning true on success, false on failure.
     *
     * @param conn
     * @return
     */
    public  boolean rollbackWithResult(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            m_log.error("Error rolling back connection from pool: " + m_db_pool_name, e);

            return false;
        }

        return true;
    }

    /**
     * Rollback, but do not throw any exceptions if the rollback fails. This method should be used sparingly in only
     * cases where you don't care or can't actually do anything if the rollback failed...
     *
     * @param conn
     */
    public  void rollbackRegardless(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            m_log.error("Error reolling back connection from pool: " + m_db_pool_name, e);
        }
    }

    /**
     * Commit a connection, throwing an exception if it fails.
     *
     * @param conn
     * @throws DBException
     */
    public  void commit(Connection conn) throws DBException {
        try {
            conn.commit();
        } catch (SQLException e) {
            m_log.error("Error comitting connection from pool: " + m_db_pool_name, e);
            throw new DBException();
        }
    }

    /**
     * Commit a connection, returning true if it suceeded and false if it failed.
     *
     * @param conn
     * @return
     */
    public  boolean commitWithResult(Connection conn) {
        try {
            conn.commit();
        } catch (SQLException e) {
            m_log.error("Error comitting connection from pool: " + m_db_pool_name, e);

            return false;
        }

        return true;
    }

    /**
     * Commit a connection, but do not throw any exceptions if the commit fails. This method should be used sparingly in
     * only cases where you don't care or can't actually do anything if the commit failed...
     *
     * @param conn
     */
    public  void commitRegardless(Connection conn) {
        try {
            conn.commit();
        } catch (SQLException e) {
            m_log.error("Error comitting connection from pool: " + m_db_pool_name, e);
        }
    }

    /**
     * Cleanup a connection and return to pool. You should cleanup ResultSets and Statements belonging to the Connection
     * first.
     *
     * @param conn
     */
    public  void cleanupResources(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            m_log.warn("Error closing Connection on pool: " + m_db_pool_name, e);
        }
    }

    /**
     * Cleanup a Statement. You should cleanup ResultSets belonging to the Statement first.
     *
     * @param st
     */
    public  void cleanupResources(Statement st) {
        try {
            if (st != null) {
                st.close();
                st = null;
            }
        } catch (SQLException e) {
            m_log.warn("Error closing Statement on pool: " + m_db_pool_name, e);
        }
    }

    /**
     * Cleanup ResultSet.
     *
     * @param rs
     */
    public  void cleanupResources(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
                rs = null;
            }
        } catch (SQLException e) {
            m_log.warn("Error closing ResultSet on pool: " + m_db_pool_name, e);
        }
    }
}
