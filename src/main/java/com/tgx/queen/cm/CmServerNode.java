/**
 * - Copyright (c) 2013 Zhang Zhuo All rights reserved.
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
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.tgx.queen.cm;

import java.io.IOException;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.tgx.queen.base.bean.BaseMessage;
import com.tgx.queen.base.classic.task.AbstractListener;
import com.tgx.queen.base.classic.task.TaskService;
import com.tgx.queen.base.classic.task.inf.ITaskListener;
import com.tgx.queen.base.classic.task.inf.ITaskResult;
import com.tgx.queen.base.classic.task.timer.TimerTask;
import com.tgx.queen.base.disruptor.CM_3XTCore;
import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.handler.aio.operations.AioOpConnect;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpCmPush;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpCmWrite;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpSessionCreate;
import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.base.util.TimeUtil;
import com.tgx.queen.io.filter.websocket.WebSocketProtocolFilter;
import com.tgx.queen.io.filter.websocket.WebSocketServerProtocolFilter;
import com.tgx.queen.push.PushRouter;
import com.tgx.queen.socket.aio.impl.AioAcceptor;
import com.tgx.queen.socket.aio.impl.AioConnector;
import com.tgx.queen.socket.aio.impl.AioProcessor;
import com.tgx.queen.socket.aio.impl.AioReadHandler;
import com.tgx.queen.socket.aio.impl.AioSession;
import com.tgx.queen.socket.aio.inf.IConnectCallBack;


/**
 * @author william
 */
public class CmServerNode
        extends
        AioProcessor
        implements
        IConnectCallBack
{
	private final static ResourceBundle         BUNDLE        = ResourceBundle.getBundle("CmServerNode");
	private final static int                    LISTEN_PORT   = Integer.parseInt(BUNDLE.getString("ListenPort"));
	private final static String                 LISTEN_ADDR   = BUNDLE.getString("ListenAddr");
	private final static int                    RETRY_GAP     = Integer.parseInt(BUNDLE.getString("RetryGap"));        ;
	private final static String                 MASTER        = BUNDLE.getString("Master"), SLAVE = BUNDLE.getString("Slave");
	private final static boolean                NO_IM_SERVICE = Boolean.parseBoolean(BUNDLE.getString("NoImService"));
	private final WebSocketProtocolFilter       imFilter      = new WebSocketProtocolFilter();
	private final WebSocketServerProtocolFilter cmFilter      = new WebSocketServerProtocolFilter();
	
	public CmServerNode() {
		super(new CM_3XTCore(3), new HashMap<Long, AioSession>(1 << 21));
		AioAcceptor.INSTANCE.setFilter(cmFilter);
		TgxOpSessionCreate.INSTANCE_CLIENT.setReadHandler(AioReadHandler.IM_CLIENT).setNoHandshake();
		TgxOpSessionCreate.INSTANCE_SERVER.setReadHandler(AioReadHandler.CM_INSTANCE).setHandshake();
		AioOpConnect.INSTANCE.setCallBack(this);
		AioOpConnect.ERROR.setCallBack(this);
		TgxOpCmWrite.INSTANCE.regSessionManager(this);
		TgxOpCmPush.INSTANCE.setBroadCaster(new PushRouter());
	}
	
	public void connectImServer(String url, String localEthIp, int localPort) throws IOException {
		newConnect(new AioConnector(this, url, localEthIp, localPort, imFilter));
	}
	
	public void connectImServer(String url) throws IOException {
		connectImServer(url, null, 0);
	}
	
	@Override
	public void onSessionCreated(Event event) {
		//TODO 连接 IM_SERVER 成功
		AioSession session = event.session;
		String remote = session.getRemoteUrl();//"ip:port"
		if (MASTER.contains(remote)) master = event.session;
		else if (SLAVE.contains(remote)) slave = event.session;
		else System.err.println("Remote: " + remote + " M: " + MASTER + " S: " + SLAVE);
		if (master != null || slave != null)
		{
			iPv4Local = IoUtil.parseInetAddress(session.localDec())[0];
			startServer();
		}
	}
	
	private static int iPv4Local;
	
	public static int getCmIndex() {
		return iPv4Local;
	}
	
	public AioSession           master;
	public AioSession           slave;
	private final ITaskListener tgxHandler = new CmHandler();
	
	@Override
	public void onConnectFaild(AioConnector connector) {
		//TODO 连接 IM_SERVER 失败
		reConnectIm();
	}
	
	public void reConnectIm() {
		liteLock.lock();
		try
		{
			retryCondition.signal();
		}
		finally
		{
			liteLock.unlock();
		}
	}
	
	private class TimerWheel
	        extends
	        TimerTask
	{
		public final static int SerialNum = SerialDomain + 1;
		
		@Override
		public int getSerialNum() {
			return SerialNum;
		}
		
		@Override
		protected boolean doTimeMethod() {
			TimeUtil.CURRENT_TIME_CACHE = BaseMessage.CURRENT_TIME_CACHE = TIME_UNPRECISE = System.currentTimeMillis();
			return false;
		}
		
		public TimerWheel() {
			super(1);
		}
	}
	
	private class CmHandler
	        extends
	        AbstractListener
	{
		
		@Override
		public boolean handleResult(ITaskResult taskOrResult, TaskService service) {
			return false;
		}
		
		@Override
		public boolean exCaught(ITaskResult task, TaskService service) {
			return false;
		}
		
	}
	
	//主线程需要进行守护操作
	public static void main(String[] args) {
		CmServerNode node = new CmServerNode();
		try
		{
			node.init();
			node.bindServer(LISTEN_PORT, LISTEN_ADDR);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		node.tgxHandler.setBindSerial(node.tgxHandler.hashCode());
		taskCore.addListener(node.tgxHandler);
		taskCore.startService();
		taskCore.requestService(node.new TimerWheel(), 0);
		
		if (NO_IM_SERVICE) node.startServer();
		else
		{
			
			for (int i = 0;; i++)
			{
				
				node.liteLock.lock();
				try
				{
					node.connectImServer((i & 1) == 0 ? MASTER : SLAVE);
					node.retryCondition.await();
					Thread.sleep(TimeUnit.SECONDS.toMillis(RETRY_GAP));
				}
				catch (InterruptedException | IOException e)
				{
					e.printStackTrace();
				}
				finally
				{
					node.liteLock.unlock();
				}
			}
		}
	}
	
	public static long               TIME_UNPRECISE;
	private final static TaskService taskCore             = TaskService.getInstance(true);
	private final ReentrantLock      liteLock             = new ReentrantLock();
	private final Condition          retryCondition       = liteLock.newCondition();
	
	public final static int          BLANK                = 1 << 19;
	public final static int          BLANK_REDIRECT_LIMIT = BLANK << 1;
	public final static int          BLANK_MAX_LIMIT      = BLANK * 3;
	
}
