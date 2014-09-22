package com.tgx.queen.db.templ;

import com.tgx.queen.db.jdbc.JDBCUtils;


public class TgxBindTempl
{
	
	private String              schema_oauth;
	private String              tName_bind;
	
	private static TgxBindTempl _instance;
	
	public TgxBindTempl() {
		this.schema_oauth = JDBCUtils.getInstance().schema_oauth;
		this.tName_bind = JDBCUtils.getInstance().tName_bind;
	}
	
	public static TgxBindTempl getInstance() {
		return _instance == null ? _instance = new TgxBindTempl() : _instance;
	}
	
	public String getBindUsrByBindIndex(long bindIndex) {
		return "select * from " + this.schema_oauth + "." + this.tName_bind + " where  bind_index = " + bindIndex;
	}
	
	public String getBindsByUsrIndex(long usrIndex) {
		return "select * from " + this.schema_oauth + "." + this.tName_bind + " where  im_usr_index = " + usrIndex;
	}
	
	public String insertBind(long bind_index, long im_usr_index, String app_id) {
		return "insert into	" + this.schema_oauth + "." + this.tName_bind + " ( bind_index,im_usr_index,app_id ) " + "values " + "( " + bind_index + "," + im_usr_index + ",'" + app_id + "' )";
	}
	
	public String deleteBind(long serial, long bind_index, long im_usr_index, String app_id) {
		String query = "delete from " + this.schema_oauth + "." + this.tName_bind + " where id = " + serial;
		query += (im_usr_index != -1) ? " OR im_usr_index = " + im_usr_index : "";
		query += (bind_index != -1) ? " AND bind_index = " + bind_index : "";
		query += (app_id != null) ? " AND app_id = '" + app_id + "'" : "";
		
		return query;
	}
	
	public String createBindTable() {
		return "create table if not exists " + this.schema_oauth + "." + this.tName_bind + "(" + "id 				bigserial, " + "bind_index	 	bigint 		default -1, " + "im_usr_index    bigint 		default -1, " + "app_id  		char(64) 	not null, " + "c_time 			timestamp 	default now(), " + "primary	key " + "( bind_index, im_usr_index, app_id )" + ")";
	}
	
	public String createBindSchema() {
		return "create schema if not exists " + this.schema_oauth;
	}
}
