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
package com.tgx.queen.im.bean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import com.tgx.queen.base.inf.IDisposable;
import com.tgx.queen.base.util.CryptUtil;
import com.tgx.queen.base.util.IoUtil;


public class TgxUsr
        implements
        IDisposable,
        Serializable
{
	
	private static final long serialVersionUID = 9174194101246733503L;
	
	private String            im_usr_id;
	private long              id;
	
	public void setId(long id) {
		this.id = id;
	}
	
	private long[] c_bind = {
		                      -1
	                      };
	private double c_time;
	
	private double m_time;
	
	private byte   bind_type;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result + id);
		result = prime * result + +((im_usr_id == null) ? 0 : im_usr_id.hashCode());
		result = (int) (prime * result + +c_time);
		result = (int) (prime * result + +m_time);
		result = prime * result + +((c_bind == null) ? 0 : c_bind.hashCode());
		result = prime * result + +bind_type;
		return result;
	}
	
	@Override
	public void dispose() {
		im_usr_id = null;
	}
	
	public String getImUsrId() {
		return im_usr_id;
	}
	
	public TgxUsr(long usrIndex) {
		id = usrIndex;
	}
	
	public TgxUsr(CryptUtil crypt, byte[] hash_info) {
		setUsrId(crypt, hash_info);
	}
	
	public TgxUsr() {
	}
	
	public TgxUsr setUsrId(CryptUtil crypt, byte[] hash_info) {
		if (crypt == null) return this;
		if (hash_info == null)
		{
			hash_info = new byte[16];
			Random r = new Random();
			r.nextBytes(hash_info);
		}
		im_usr_id = IoUtil.bin2Hex(crypt.sha256(hash_info));// TODO
		                                                    // 考虑使用PG的内部函数实现，需要比较效能差异
		return this;
	}
	
	public long getUsrIndex() {
		return id;
	}
	
	public boolean contain(long bindIndex) {
		return Arrays.binarySearch(c_bind, bindIndex) < 0 ? false : true;
	}
	
	public boolean isNoBind() {
		boolean noBind = true;
		for (long c : c_bind)
			if (c != -1) return false;
		return noBind;
	}
	
	public long[] getC_bind() {
		return c_bind;
	}
	
	public void setC_bind(Long[] bind) {
		if (bind == null || bind.length == 0) return;
		int length = bind.length;
		c_bind = new long[length];
		for (int i = 0; i < length; i++)
		{
			if (bind[i].longValue() == 0) System.err.println("bind clientIndex 0!check!");
			c_bind[i] = bind[i].longValue();
		}
		// Arrays.sort(c_bind); //调整为数据库内容变更时必须保持顺序结构。
	}
	
	public void setNativeC_bind(long[] bind) {
		c_bind = bind;
	}
	
	public long getCreateTime() {
		return (long) (c_time * 1000L);
	}
	
	public long getModifyTime() {
		return (long) (m_time * 1000L);
	}
	
	public byte getBind_type() {
		return bind_type;
	}
	
	public void setBind_type(byte bind_type) {
		this.bind_type = bind_type;
	}
	
	public boolean isNormalBind() {
		return bind_type == NORMAL_BIND;
	}
	
	public void setGroupBind() {
		bind_type = GROUP_BIND;
	}
	
	public void setCircleBind() {
		bind_type = CIRCLE_BIND;
	}
	
	public boolean isGroupBind() {
		return bind_type == GROUP_BIND;
	}
	
	public boolean isCircleBind() {
		return bind_type == CIRCLE_BIND;
	}
	
	private final static byte NORMAL_BIND = 0, GROUP_BIND = 1, CIRCLE_BIND = 2;
	
	public TgxUsr parseIMUserToTgxUsr(TIMUserEntity u) {
		this.bind_type = (byte) u.getBind_type();
		this.c_time = u.getC_time().getTime();
		this.id = u.getId();
		this.im_usr_id = u.getIm_usr_id();
		this.m_time = u.getM_time().getTime();
		long[] d = u.getC_bind();
		if (d != null && d.length > 0)
		{
			this.c_bind = new long[d.length];
			for (int i = 0; i < d.length; i++)
			{
				this.c_bind[i] = d[i];
			}
		}
		return this;
	}
	
	public TIMUserEntity parseTgxUsrToIMusr() {
		TIMUserEntity u = new TIMUserEntity();
		u.setId(this.id);
		u.setBind_type(this.bind_type);
		u.setC_time(new Date((long) this.c_time));
		u.setIm_usr_id(this.im_usr_id);
		u.setM_time(new Date((long) this.m_time));
		if (this.c_bind != null && this.c_bind.length > 0)
		{
			long[] d = new long[this.c_bind.length];
			for (int i = 0; i < this.c_bind.length; i++)
			{
				d[i] = this.c_bind[i];
			}
			u.setC_bind(d);
		}
		return u;
	}
	
	public TgxUsr resetInstance(long id, String im_usr_id, double c_time, double m_time, Long[] c_bind, byte bind_type) {
		this.id = id;
		this.im_usr_id = im_usr_id;
		this.c_time = c_time;
		this.m_time = m_time;
		this.setC_bind(c_bind);
		this.bind_type = bind_type;
		return this;
	}
	
	@Override
	public boolean isDisposable() {
		return true;
	}
}
