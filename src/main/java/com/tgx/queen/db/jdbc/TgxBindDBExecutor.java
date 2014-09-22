package com.tgx.queen.db.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tgx.queen.db.templ.TgxBindTempl;
import com.tgx.queen.im.bean.TgxBind;


public class TgxBindDBExecutor
{
	
	private Connection connection;
	
	public TgxBindDBExecutor() {
		this.connection = JDBCUtils.getInstance().getConnection();
	}
	
	public List<TgxBind> getBindUsrByBindIndex(long bindIndex) throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxBindTempl.getInstance().getBindUsrByBindIndex(bindIndex);
		
		ResultSet resultSet = statement.executeQuery(query);
		List<TgxBind> list = new ArrayList<TgxBind>();
		
		while (resultSet.next())
		{
			TgxBind bind = new TgxBind();
			bind.setSerial(resultSet.getLong(1));
			bind.setBindIndex(resultSet.getLong(2));
			bind.setImUsrIndex(resultSet.getLong(3));
			bind.setApp_id(resultSet.getString(4));
			list.add(bind);
		}
		
		statement.close();
		return list;
	}
	
	public List<TgxBind> getBindsByUsrIndex(long usrIndex) throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxBindTempl.getInstance().getBindsByUsrIndex(usrIndex);
		ResultSet resultSet = statement.executeQuery(query);
		List<TgxBind> list = new ArrayList<TgxBind>();
		
		while (resultSet.next())
		{
			TgxBind bind = new TgxBind();
			bind.setSerial(resultSet.getLong(1));
			bind.setBindIndex(resultSet.getLong(2));
			bind.setImUsrIndex(resultSet.getLong(3));
			bind.setApp_id(resultSet.getString(4));
			list.add(bind);
		}
		
		statement.close();
		return list;
	}
	
	public int insertBind(TgxBind binder) throws SQLException {
		Statement statement = this.connection.createStatement();
		long bind_index = binder.getBindIndex();
		long im_usr_index = binder.getImUsrIndex();
		String app_id = binder.getApp_id();
		
		String query = TgxBindTempl.getInstance().insertBind(bind_index, im_usr_index, app_id);
		int re = 0;
		re = statement.executeUpdate(query);
		
		return re;
	}
	
	public int deleteBind(TgxBind binder) throws SQLException {
		Statement statement = this.connection.createStatement();
		long serial = binder.getSerial();
		long im_usr_index = binder.getImUsrIndex();
		long bind_index = binder.getBindIndex();
		int re = 0;
		String app_id = binder.getApp_id();
		if (serial != 0)
		{
			String query = TgxBindTempl.getInstance().deleteBind(serial, bind_index, im_usr_index, app_id);
			re = statement.executeUpdate(query);
		}
		statement.close();
		return re;
	}
	
	public void createBindTable() throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxBindTempl.getInstance().createBindTable();
		statement.execute(query);
		statement.close();
	}
	
	public void createBindSchema() throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxBindTempl.getInstance().createBindSchema();
		statement.execute(query);
		statement.close();
	}
	
	public static void main(String[] agrs) {
		TgxBindDBExecutor tbe = new TgxBindDBExecutor();
		TgxBind bind = new TgxBind();
		bind.setSerial(3);
		bind.setBindIndex(2);
		bind.setImUsrIndex(2);
		bind.setApp_id("97738E1690FA90A1F2943E5499B7B725787F0FC2FDB99C818960B6DDC4B13F3E");
		
		try
		{
			List<TgxBind> list = tbe.getBindsByUsrIndex(1);
			for (TgxBind bb : list)
			{
				System.out.println(bb.getBindIndex() + ":" + bb.getImUsrIndex());
			}
			System.out.println(tbe.deleteBind(bind));
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
