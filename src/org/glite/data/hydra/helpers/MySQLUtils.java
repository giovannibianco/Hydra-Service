/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.hydra.helpers;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class MySQLUtils {
    // Logger object
    private final static Logger m_log = Logger.getLogger("org.glite.data.catalog.service.meta.helpers.MySQLUtil");

	public static boolean isValidTableIdentifier(String tableName) {
		m_log.debug("Entered isValidTableIdentifier.");
		
		if((tableName.getBytes().length > 64) // check maximum size
				|| (tableName.indexOf('\\') != -1) // check 
				|| (tableName.indexOf('/') != -1)  // invalid
				|| (tableName.indexOf('.') != -1)  // characters
				) {
			return false;
		}		
		
		// Check for reserved words
		ArrayList reservedList = new ArrayList();
		if(reservedList.contains(tableName.toUpperCase())) {
			return false;
		}
		
		return true;
	}
	
	public static boolean isValidColumnIdentifier(String columnName) {
		m_log.debug("Entered isValidColumnIdentifier.");
		
		 // check maximum size
		if(columnName.getBytes().length > 64) {
			return false;
		}
		
		return true;
	}

    // List of MySQL reservedWords
	private static String[] reservedWords = new String[] {
			"ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "ASENSITIVE",
			"BEFORE", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOTH", "BY",
			"CALL", "CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER", "CHECK",
			"COLLATE", "COLUMN", "CONDITION", "CONNECTION", "CONSTRAINT",
			"CONTINUE", "CONVERT", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME",
			"CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR",
			"DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE",
			"DAY_SECOND", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELAYED", "DELETE",
			"DESC", "DESCRIBE", "DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV",
			"DOUBLE", "DROP", "DUAL",
			"EACH", "ELSE", "ELSEIF", "ENCLOSED", "ESCAPED", "EXISTS", "EXIT", "EXPLAIN",
			"FALSE", "FETCH", "FLOAT", "FOR", "FORCE", "FOREIGN", "FROM", "FULLTEXT",
			"GOTO", "GRANT", "GROUP",
			"HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND",
			"IF", "IGNORE", "IN", "INDEX", "INFILE", "INNER", "INOUT", "INSENSITIVE",
			"INSERT", "INT", "INTEGER", "INTERVAL", "INTO", "IS", "ITERATE",
			"JOIN",
			"KEY", "KEYS", "KILL",
			"LEADING", "LEAVE", "LEFT", "LIKE", "LIMIT", "LINES", "LOAD", "LOCALTIME",
			"LOCALTIMESTAMP", "LOCK", "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY",
			"MATCH", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT", 
			"MINUTE_MICROSECOND", "MINUTE_SECOND", "MOD", "MODIFIES",
			"NATURAL", "NOT", "NO_WRITE_TO_BINLOG", "NULL", "NUMERIC",
			"ON", "OPTIMIZE", "OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE",
			"PRECISION", "PRIMARY", "PROCEDURE", "PURGE",
			"READ", "READS", "REAL", "REFERENCES", "REGEXP", "RENAME", "REPEAT", "REPLACE",
			"REQUIRE", "RESTRICT", "RETURN", "REVOKE", "RIGHT", "RLIKE",
			"SCHEMA", "SCHEMAS", "SECOND_MICROSECOND", "SELECT", "SENSITIVE", "SEPARATOR",
			"SET", "SHOW", "SMALLINT", "SONAME", "SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION",
			"SQLSTATE", "SQLWARNING", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", 
			"SQL_SMALL_RESULT", "SSL", "STARTING", "STRAIGHT_JOIN",
			"TABLE", "TERMINATED", "THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO",
			"TRAILING", "TRIGGER", "TRUE",
			"UNDO", "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE",
			"USE", "USING", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP",
			"VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING",
			"WHEN", "WHERE", "WHILE", "WITH", "WRITE",
			"XOR",
			"YEAR_MONTH",
			"ZEROFILL"
		};
	
	// List of valid column types
	
}
