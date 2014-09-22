package com.tgx.queen.im.bean;

import java.io.Serializable;
import java.util.Date;


/**
 * IM 用户表
 * 
 * @author flybug
 */
public class TIMUserEntity
        implements
        Serializable
{
	
	private static final long serialVersionUID = 9174194101246733503L;
	
	private long              id;
	private String            im_usr_id;
	private long[]            c_bind;
	private Long[]            c_bind_l;
	private int               bind_type;                              //0:正常用户；1：群聊；2：企业用户
	private Date              c_time;
	private Date              m_time;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result + id);
		result = prime * result + +((im_usr_id == null) ? 0 : im_usr_id.hashCode());
		result = (int) (prime * result + +c_time.hashCode());
		result = (int) (prime * result + +m_time.hashCode());
		result = prime * result + +((c_bind == null) ? 0 : c_bind.hashCode());
		result = prime * result + +bind_type;
		return result;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getIm_usr_id() {
		return im_usr_id;
	}
	
	public void setIm_usr_id(String im_usr_id) {
		this.im_usr_id = im_usr_id;
	}
	
	public long[] getC_bind() {
		return c_bind;
	}
	
	public void setC_bind(long[] c_bind) {
		this.c_bind = c_bind;
	}
	
	public int getBind_type() {
		return bind_type;
	}
	
	public void setBind_type(int bind_type) {
		this.bind_type = bind_type;
	}
	
	public Date getC_time() {
		return c_time;
	}
	
	public void setC_time(Date c_time) {
		this.c_time = c_time;
	}
	
	public Date getM_time() {
		return m_time;
	}
	
	public void setM_time(Date m_time) {
		this.m_time = m_time;
	}
	
	public Long[] getC_bind_l() {
		return c_bind_l;
	}
	
	public void setC_bind_i(Long[] c_bind_l) {
		this.c_bind_l = c_bind_l;
		if (c_bind_l != null && c_bind_l.length > 0)
		{
			long[] c = new long[c_bind_l.length];
			for (int i = 0; i < c_bind_l.length; i++)
			{
				c[i] = c_bind_l[i];
			}
			this.c_bind = c;
		}
	}
}
