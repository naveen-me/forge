package com.svb.common.dba;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.microsoft.jdbc.sqlserver.SQLServerDriver;

public class SQLDBConnection {

	public SQLDBConnection() {
	}

	public void destroy() {
	}

	public void free(Connection connection) throws Exception {
		if (connection != null)
			connection.close();
	}

	public Connection getConnection() throws Exception {
		Connection connection = null;
		try {
			Properties properties = new Properties();
			properties.load(new DataInputStream(new BufferedInputStream(
					new FileInputStream("C:\\config\\sqlavon1prop.txt"))));
			String s = properties.getProperty("username");
			String s1 = properties.getProperty("password");
			String s2 = properties.getProperty("url");
			String s3 = properties.getProperty("logfile");
			String s4 = properties.getProperty("driver");
			int i = Integer.parseInt(properties.getProperty("mincon"));
			int j = Integer.parseInt(properties.getProperty("maxcon"));
			float f = Float.parseFloat(properties.getProperty("days"));
			DriverManager.registerDriver(new SQLServerDriver());
			connection = DriverManager.getConnection(s2, s, s1);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return connection;
	}
}