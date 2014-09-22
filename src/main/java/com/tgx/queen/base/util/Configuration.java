/**
 * -
 * Copyright (c) 2013 Mark Cai
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
package com.tgx.queen.base.util;

import java.util.ResourceBundle;


public class Configuration
{
	
	/**
	 * 读取配置文件信息
	 * 
	 * @param name
	 *            读取节点名
	 * @param fileName
	 *            文件名
	 * @return 读取的节点值
	 */
	public static String readConfigString(String name, String fileName) {
		String result = "";
		try
		{
			ResourceBundle rb = ResourceBundle.getBundle(fileName);
			result = rb.getString(name);
		}
		catch (Exception e)
		{
			// log.error("从" + fileName + "读取" + name + "出错:" + e.getMessage());
			System.out.println("从" + fileName + "读取" + name + "出错:" + e.getMessage());
		}
		return result;
	}
	
	/**
	 * 读取配置文件信息
	 * 
	 * @param name
	 *            读取节点名
	 * @param fileName
	 *            文件名
	 * @return 读取的节点值
	 */
	public static int readConfigInteger(String name, String fileName) {
		int result = 0;
		try
		{
			ResourceBundle rb = ResourceBundle.getBundle(fileName);
			result = Integer.parseInt(rb.getString(name));
		}
		catch (Exception e)
		{
			// log.error("从" + fileName + "读取" + name + "出错:" + e.getMessage());
			System.out.println("从" + fileName + "读取" + name + "出错:" + e.getMessage());
		}
		return result;
	}
	
	public static long readConfigLong(String name, String fileName) {
		long result = 0;
		try
		{
			ResourceBundle rb = ResourceBundle.getBundle(fileName);
			result = Long.parseLong(rb.getString(name));
		}
		catch (Exception e)
		{
			// log.error("从" + fileName + "读取" + name + "出错:" + e.getMessage());
			System.out.println("从" + fileName + "读取" + name + "出错:" + e.getMessage());
		}
		return result;
	}
	
	public static double readConfigDouble(String name, String fileName) {
		double result = 0;
		try
		{
			ResourceBundle rb = ResourceBundle.getBundle(fileName);
			result = Double.parseDouble(rb.getString(name));
		}
		catch (Exception e)
		{
			// log.error("从" + fileName + "读取" + name + "出错:" + e.getMessage());
			System.out.println("从" + fileName + "读取" + name + "出错:" + e.getMessage());
		}
		return result;
	}
	
	public static float readConfigFloat(String name, String fileName) {
		float result = 0.0000000000f;
		try
		{
			ResourceBundle rb = ResourceBundle.getBundle(fileName);
			String s = rb.getString(name);
			result = Float.parseFloat(s);
		}
		catch (Exception e)
		{
			// log.error("从" + fileName + "读取" + name + "出错:" + e.getMessage());
			System.out.println("从" + fileName + "读取" + name + "出错:" + e.getMessage());
		}
		return result;
	}
	
}
