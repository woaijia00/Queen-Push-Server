package com.tgx.queen.db.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import com.tgx.queen.base.util.Configuration;


/**
 * @author cann
 */
public class JDBCUtils
{
	ArrayList<Connection>    Connections          = new ArrayList<Connection>();
	final int                poolsize             = 1;
	private static JDBCUtils _instance;
	
	String                   driver               = "org.postgresql.Driver";
	String                   remote               = "jdbc:postgresql://localhost:5432/tgx_im";
	String                   username             = "tgx_jdbc";
	String                   password             = "1";
	public String            schema_cm            = "tgx_cm_test";
	public String            schema_im            = "tgx_test";
	public String            schema_oauth         = "tgx_oauth_test";
	public String            tName_cm_master      = "t_cm_client";
	public String            tName_im_master      = "t_im_usr";
	public String            tName_bind           = "t_im_cm_bind";
	public String            tName_cm_hash_global = "t_cm_hash_global";
	public String            tName_cm_tag_define  = "t_cm_tag_def";
	public String            tName_cm_tag_data    = "t_cm_tag_data";
	public boolean           jdbc_use             = false;
	
	public static JDBCUtils getInstance() {
		return _instance == null ? _instance = new JDBCUtils() : _instance;
	}
	
	public Connection getConnection() {
		return this.Connections.get((int) (Math.random() * this.poolsize));
	}
	
	private void readConfig() {
		this.driver = Configuration.readConfigString("driver", "config");
		this.remote = Configuration.readConfigString("remote", "config");
		this.username = Configuration.readConfigString("username", "config");
		this.password = Configuration.readConfigString("password", "config");
		this.schema_cm = Configuration.readConfigString("schema_cm", "config");
		this.schema_im = Configuration.readConfigString("schema_im", "config");
		this.schema_oauth = Configuration.readConfigString("schema_oauth", "config");
		this.tName_cm_master = Configuration.readConfigString("tName_cm_master", "config");
		this.tName_im_master = Configuration.readConfigString("tName_im_master", "config");
		this.tName_bind = Configuration.readConfigString("tName_bind", "config");
		this.tName_cm_hash_global = Configuration.readConfigString("tName_cm_hash_global", "config");
		this.tName_cm_tag_define = Configuration.readConfigString("tName_cm_tag_define", "config");
		this.tName_cm_tag_data = Configuration.readConfigString("tName_cm_tag_data", "config");
		String jdbcUsing = Configuration.readConfigString("jdbc_use", "config");
		this.jdbc_use = jdbcUsing.equals("true") ? true : false;
		
	}
	
	public JDBCUtils() {
		readConfig();
		
		try
		{
			Class.forName(driver);
		}
		catch (Exception e)
		{
			System.out.println("no postgresql jdbc driver found..");
			e.printStackTrace();
		}
		for (int i = 0; i < poolsize; i++)
		{
			try
			{
				Connection con = DriverManager.getConnection(remote, username, password);
				Connections.add(con);
			}
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
