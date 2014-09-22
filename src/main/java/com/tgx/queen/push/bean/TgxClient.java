/**
 * -
 * Copyright (c) 2013 Zhang Zhuo
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote
 * products derived from this software without specific written permission.
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.tgx.queen.push.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

import com.tgx.queen.base.inf.IDisposable;
import com.tgx.queen.base.util.CryptUtil;
import com.tgx.queen.base.util.IoUtil;


public class TgxClient
        implements
        IDisposable,
        Serializable
{
	private static final long serialVersionUID = 9174194101246733501L;
	
	private long              id;
	private String            client_id;
	private double            c_time;
	private String            client_type;
	private int[]             tags;
	private String            thirdparty_push_token;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result + id);
		result = prime * result + +((client_id == null) ? 0 : client_id.hashCode());
		result = (int) (prime * result + +c_time);
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
	
	public double getC_time() {
		return this.c_time;
	}
	
	public void setC_time(double c_time) {
		this.c_time = c_time;
	}
	
	public String getClient_type() {
		return client_type;
	}
	
	// -------------------------------------------------
	@Override
	public void dispose() {
		client_id = null;
	}
	
	@Override
	public boolean isDisposable() {
		return true;
	}
	
	/* _____________________________________________ */
	
	public long getClientIndex() {
		return id;
	}
	
	public String getClientIdHex() {
		return client_id;
	}
	
	public TgxClient setClientId(CryptUtil crypt, byte[] hash_info) {
		if (crypt == null) return this;
		if (hash_info == null)
		{
			hash_info = new byte[16];
			Random r = new Random();
			r.nextBytes(hash_info);
		}
		client_id = IoUtil.bin2Hex(crypt.sha256(hash_info));
		return this;
	}
	
	public void setClientType(String client_type) {
		this.client_type = client_type;
	}
	
	public long getCreateTime() {
		return (long) (c_time * 1000L);
	}
	
	public int[] getTags() {
		return tags;
	}
	
	public void setTags(Integer[] tags) {
		int length = tags.length;
		this.tags = new int[tags.length];
		for (int i = 0; i < length; i++)
			this.tags[i] = tags[i].intValue();
	}
	
	public void setNativeTags(int[] tags) {
		this.tags = tags;
	}
	
	public TgxClient parseClient(TCmClientEntity e) {
		this.id = e.getId();
		this.c_time = e.getC_time().getTime();
		this.client_id = e.getClient_id();
		this.client_type = e.getClient_type();
		int[] t = e.getTags();
		if (t != null && t.length > 0)
		{
			
			Integer[] d = new Integer[t.length];
			for (int i = 0; i < t.length; i++)
			{
				d[i] = t[i];
			}
			this.setTags(d);
		}
		this.thirdparty_push_token = e.getThirdparty_push_token();
		
		return this;
	}
	
	public TCmClientEntity parseCMClient(TgxClient e) {
		TCmClientEntity c = new TCmClientEntity();
		c.setC_time(new Date((long) this.c_time));
		c.setClient_id(e.client_id);
		c.setClient_type(e.client_type);
		c.setId(e.id);
		c.setTags(e.tags);
		c.setThirdparty_push_token(thirdparty_push_token);
		
		return c;
	}
	
	public String getThirdparty_push_token() {
		return thirdparty_push_token;
	}
	
	public void setThirdparty_push_token(String thirdparty_push_token) {
		this.thirdparty_push_token = thirdparty_push_token;
	}
	
}
