package com.tgx.queen.db.templ;

import com.tgx.queen.db.jdbc.JDBCUtils;


public class TgxTagTempl
{
	
	private String             schema_cm;
	private String             tName_cm_tag_data;
	private String             tName_cm_tag_define;
	private static TgxTagTempl _instance;
	
	final String               AllTagColumn = "id,tag,app_id,domain";
	
	public static TgxTagTempl getInstance() {
		return _instance == null ? _instance = new TgxTagTempl() : _instance;
	}
	
	public TgxTagTempl() {
		this.schema_cm = JDBCUtils.getInstance().schema_cm;
		this.tName_cm_tag_data = JDBCUtils.getInstance().tName_cm_tag_data;
		this.tName_cm_tag_define = JDBCUtils.getInstance().tName_cm_tag_define;
	}
	
	public String getClientTags(int[] tagIds) {
		String tags = "(";
		for (int i = 0; i < tagIds.length - 1; i++)
			tags += tagIds[i] + ",";
		tags += tagIds[tagIds.length - 1] + ")";
		
		String query = "select " + this.AllTagColumn + " from " + this.schema_cm + "." + this.tName_cm_tag_define + " where id in " + tags;
		
		return query;
	}
	
	public String createTagSchema() {
		return "create schema if not exists " + this.schema_cm;
	}
	
	public String createTagDefineTable() {
		//formatter:off
		return "create table if not exists " 
				+ this.schema_cm 
				+ "." 
				+ this.tName_cm_tag_define 
			    + "(" 
			    + " id serial, "
			    + "tag varchar(64),"
				+ " app_id char(64),"
			    + " domain char(64) " 
				+ ");";
		//formatter:on
	}
	
	public String insertTagDefine(String tags, String app_id, String domain) {
		//formatter:off
		return "insert into "
				+ this.schema_cm 
				+ "." 
				+ this.tName_cm_tag_define 
				+ " (tag, app_id, domain) " 
				+ " values " 
				+ "('" 
				+ tags 
				+ "', '" 
				+ app_id 
				+ "', '" 
				+ domain 
				+ "');";
		//formatter:on
	}
	
	public String getTagDefine() {
		return "select " + this.AllTagColumn + " from " + this.schema_cm + "." + this.tName_cm_tag_define;
	}
	
	public String getTagData() {
		return "select " + this.AllTagColumn + " from " + this.schema_cm + "." + this.tName_cm_tag_data;
	}
	
}
