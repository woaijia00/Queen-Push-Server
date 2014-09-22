package com.tgx.queen.push.bean;

import java.io.Serializable;
import java.util.Date;


/**
 * 客户端账号信息
 * 
 * @author flybug
 */
public class TCmClientEntity
        implements
        Serializable
{
	
	private static final long serialVersionUID = 9174194101246733501L;
	
	private long              id;
	private String            client_id;
	private Date              c_time;
	private int[]             tags;
	private Integer[]         tags_;
	private String            client_type;
	private String            thirdparty_push_token;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result + id);
		result = prime * result + +((client_id == null) ? 0 : client_id.hashCode());
		result = (int) (prime * result + +c_time.hashCode());
		result = prime * result + +((client_type == null) ? 0 : client_type.hashCode());
		result = prime * result + +((tags == null) ? 0 : tags.hashCode());
		result = prime * result + +((thirdparty_push_token == null) ? 0 : thirdparty_push_token.hashCode());
		return result;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getClient_id() {
		return client_id;
	}
	
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	
	public Date getC_time() {
		return c_time;
	}
	
	public void setC_time(Date c_time) {
		this.c_time = c_time;
	}
	
	public String getClient_type() {
		return client_type;
	}
	
	public void setClient_type(String client_type) {
		this.client_type = client_type;
	}
	
	public int[] getTags() {
		return tags;
	}
	
	public void setTags(int[] tags) {
		this.tags = tags;
	}
	
	public Integer[] getTags_() {
		return tags_;
	}
	
	public void setTags_(Integer[] tags_) {
		this.tags_ = tags_;
		if (tags_ != null && tags_.length > 0)
		{
			tags = new int[tags_.length];
			for (int i = 0; i < tags_.length; i++)
			{
				tags[i] = tags_[i];
			}
		}
	}
	
	public String getThirdparty_push_token() {
		return thirdparty_push_token;
	}
	
	public void setThirdparty_push_token(String thirdparty_push_token) {
		this.thirdparty_push_token = thirdparty_push_token;
	}
	
}
