/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.hydra.helpers;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class MySQLUtils {
    // Logger object
    private final static Logger m_log = Logger.getLogger(MySQLUtils.class);

    private final static Pattern safeTableIdentifier_re =
        Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]{0,63}");

	public static boolean isValidTableIdentifier(String tableName) {
		m_log.debug("Entered isValidTableIdentifier.");
		
        // Check safe characters and the length
        if (!safeTableIdentifier_re.matcher(tableName).matches()) {
            return false;
        }
		
		// Check for reserved words
		if(Arrays.binarySearch(reservedWords, tableName.toUpperCase()) >= 0) {
			return false;
		}
		
		return true;
	}
	
    private final static Pattern safeColumnIdentifier_re =
        Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]{0,63}");

	public static boolean isValidColumnIdentifier(String columnName) {
		m_log.debug("Entered isValidColumnIdentifier.");
		
        // Check safe characters and the length
        if (!safeColumnIdentifier_re.matcher(columnName).matches()) {
            return false;
        }

		// Check for reserved words
		if(Arrays.binarySearch(reservedWords, columnName.toUpperCase()) >= 0) {
			return false;
		}
		
		
		return true;
	}

    // List of MySQL reservedWords
	private static String[] reservedWords = new String[] {
			"ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "ASENSITIVE",
			"BEFORE", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOTH", "BY",
			"CALL", "CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER", "CHECK",
			"COLLATE", "COLUMN", "COLUMNS", "CONDITION", "CONNECTION", "CONSTRAINT",
			"CONTINUE", "CONVERT", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME",
			"CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR",
			"DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE",
			"DAY_SECOND", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELAYED", "DELETE",
			"DESC", "DESCRIBE", "DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV",
			"DOUBLE", "DROP", "DUAL",
			"EACH", "ELSE", "ELSEIF", "ENCLOSED", "ESCAPED", "EXISTS", "EXIT", "EXPLAIN",
			"FALSE", "FETCH", "FIELDS", "FLOAT", "FLOAT4", "FLOAT8", "FOR", "FORCE",
            "FOREIGN", "FROM", "FULLTEXT",
			"GOTO", "GRANT", "GROUP",
			"HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND",
			"IF", "IGNORE", "IN", "INDEX", "INFILE", "INNER", "INOUT", "INSENSITIVE",
			"INSERT", "INT", "INT1", "INT2", "INT3", "INT4", "INT8", "INTEGER", "INTERVAL",
            "INTO", "IS", "ITERATE",
			"JOIN",
			"KEY", "KEYS", "KILL",
			"LABEL", "LEADING", "LEAVE", "LEFT", "LIKE", "LIMIT", "LINES", "LOAD", "LOCALTIME",
			"LOCALTIMESTAMP", "LOCK", "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY",
			"MATCH", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT", 
			"MINUTE_MICROSECOND", "MINUTE_SECOND", "MOD", "MODIFIES",
			"NATURAL", "NOT", "NO_WRITE_TO_BINLOG", "NULL", "NUMERIC",
			"ON", "OPTIMIZE", "OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE",
			"PRECISION", "PRIMARY", "PRIVILEGES", "PROCEDURE", "PURGE",
			"READ", "READS", "REAL", "REFERENCES", "REGEXP", "RELEASE", "RENAME", "REPEAT",
            "REPLACE", "REQUIRE", "RESTRICT", "RETURN", "REVOKE", "RIGHT", "RLIKE",
			"SCHEMA", "SCHEMAS", "SECOND_MICROSECOND", "SELECT", "SENSITIVE", "SEPARATOR",
			"SET", "SHOW", "SMALLINT", "SONAME", "SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION",
			"SQLSTATE", "SQLWARNING", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", 
			"SQL_SMALL_RESULT", "SSL", "STARTING", "STRAIGHT_JOIN",
			"TABLE", "TABLES", "TERMINATED", "THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO",
			"TRAILING", "TRIGGER", "TRUE",
			"UNDO", "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "UPGRADE", "USAGE",
			"USE", "USING", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP",
			"VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING",
			"WHEN", "WHERE", "WHILE", "WITH", "WRITE",
			"XOR",
			"YEAR_MONTH",
			"ZEROFILL"
		};
	
	// List of valid column types
	
}
