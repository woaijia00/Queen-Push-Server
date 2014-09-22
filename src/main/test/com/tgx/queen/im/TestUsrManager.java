package com.tgx.queen.im;

import java.io.IOException;

import org.junit.Test;

import com.tgx.queen.db.impl.im.TgxUsrDao;
import com.tgx.queen.im.bean.TgxImUsr;
import com.tgx.queen.im.bean.TgxUsr;


public class TestUsrManager
{
	
	//@Test
	public void testLoginIn() {
		UsrStateManager usm = UsrStateManager.getInstance();
		int loop = 2;
		for (int i = 1; i <= loop; i++)
		{
			TgxUsr usr = TgxUsrDao.getInstance().getImUsr(i);
			long t = System.currentTimeMillis();
			usm.loginUsr(usr, i);
			long cast = System.currentTimeMillis() - t;
			System.out.println("nano: " + cast);
		}
	}
	
	// @Test
	public void testSkipSet() throws IOException {
		TgxUsr usr = TgxUsrDao.getInstance().getImUsr(1);
		int loop = 16;
		for (int i = 0; i < loop; i++)
		{
			long t = System.nanoTime();
			new TgxImUsr(usr);
			long cast = System.nanoTime() - t;
			System.out.println("ss: " + (cast));
		}
		
	}
	
	@Test
	public void x() {
		int x = 0;
		System.out.println(++x & 7);
	}
}
