package com.tgx.queen.test.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Random;
import java.util.ResourceBundle;

import com.lmax.disruptor.RingBuffer;
import com.tgx.queen.base.disruptor.IM_CM_Client_3XTCore;
import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.bean.TgxEvent;
import com.tgx.queen.base.disruptor.handler.aio.operations.AioOpClosed;
import com.tgx.queen.base.disruptor.handler.aio.operations.AioOpConnect;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpSessionCreate;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpWrite;
import com.tgx.queen.base.util.CryptUtil;
import com.tgx.queen.base.util.MixUtil;
import com.tgx.queen.io.bean.protocol.impl.TestFactory;
import com.tgx.queen.io.bean.protocol.impl.X06_RequestClientID;
import com.tgx.queen.io.bean.protocol.impl.X100_HandShake;
import com.tgx.queen.io.bean.protocol.impl.X101_Close;
import com.tgx.queen.io.bean.protocol.impl.X102_Ping;
import com.tgx.queen.io.bean.protocol.impl.X10_Login;
import com.tgx.queen.io.filter.WSCommandFilter;
import com.tgx.queen.io.filter.websocket.WebSocketProtocolFilter;
import com.tgx.queen.io.inf.IQoS;
import com.tgx.queen.socket.aio.impl.AioClient;
import com.tgx.queen.socket.aio.impl.AioConnector;
import com.tgx.queen.socket.aio.impl.AioReadHandler;
import com.tgx.queen.socket.aio.impl.AioSession;
import com.tgx.queen.socket.aio.inf.IConnectCallBack;


public class TestConsoleClient
        implements
        IConnectCallBack
{
	AioClient               client;
	ResourceBundle          bundle;
	IM_CM_Client_3XTCore    core;
	String[]                hosts      = {
		                                   "127.0.0.1"
	                                   };
	int                     port       = 5226;
	String                  cServer    = "127.0.0.1";
	int                     cPort      = 5227;
	int                     count      = 1024;
	int                     sleep      = 1000;
	int                     startIndex = 1;
	String[]                bindAddrs  = {
		                                   "127.0.0.1"
	                                   };
	WebSocketProtocolFilter filter     = new WebSocketProtocolFilter();
	CryptUtil               crypt      = new CryptUtil();
	
	public void init() throws InstantiationException, IllegalAccessException, IOException {
		client = new AioClient(core = new IM_CM_Client_3XTCore(3), new HashMap<Long, AioSession>(1 << 16));
		client.init();AioOpConnect.INSTANCE.setCallBack(this);
		AioOpConnect.ERROR.setCallBack(this);
		TgxOpSessionCreate.INSTANCE_CLIENT.setReadHandler(AioReadHandler.CM_CLIENT).setHandshake();
		WSCommandFilter wsCmdFilter = (WSCommandFilter) filter.getChainHead(WSCommandFilter.name);
		wsCmdFilter.regOther(new TestFactory());
		try
		{
			client.init();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
		TestConsoleClient test = new TestConsoleClient();
		test.bundle = ResourceBundle.getBundle("TestConsoleClient");
		test.hosts = test.bundle.getString("hosts").split(",");
		test.port = Integer.parseInt(test.bundle.getString("port"));
		test.cServer = test.bundle.getString("cServer");
		test.cPort = Integer.parseInt(test.bundle.getString("cPort"));
		test.count = Integer.parseInt(test.bundle.getString("count"));
		test.sleep = Integer.parseInt(test.bundle.getString("sleep"));
		test.startIndex = Integer.parseInt(test.bundle.getString("startIndex"));
		test.bindAddrs = test.bundle.getString("bindAddrs").split(";");
		test.init();
		System.out.println("waiting command input...");
		test.telnet(test.cServer, test.cPort);
		AioOpClosed.INSTANCE.name();
	}
	
	@Override
	public void onSessionCreated(Event event) {
		event.attach = new X100_HandShake();
	}
	
	@Override
	public void onConnectFaild(AioConnector connector) {
		System.err.println("Connect Failed!");
	}
	
	void sendCmd(AioSession session, IQoS content) {
		RingBuffer<TgxEvent> publisher = core.getSendBuffer();
		long sequence = publisher.next();
		TgxEvent event = publisher.get(sequence);
		event.produce(TgxOpWrite.INSTANCE, EventOp.Error.NO_ERROR, session, content, null);
		publisher.publish(sequence);
	}
	
	private void telnet(String host, int port) throws IOException {
		final AsynchronousServerSocketChannel server;
		server = AsynchronousServerSocketChannel.open();
		server.bind(new InetSocketAddress(host, port));
		server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>()
		{
			final ByteBuffer buffer  = ByteBuffer.allocate(1024);
			ByteBuffer       sendbuf = ByteBuffer.allocate(1024);
			
			@Override
			public void completed(AsynchronousSocketChannel result, Object attachment) {
				System.out.println("waiting for command....");
				buffer.clear();
				sendbuf.clear();
				try
				{
					result.read(buffer).get();
					buffer.flip();
					System.out.println("Echo " + new String(buffer.array()).trim() + " to " + result);
					String cmd = new String(buffer.array()).trim();
					switch (cmd) {
						case "connect":
							System.out.println("start connect...");
							testConnect();
							result.write(ByteBuffer.wrap(String.valueOf("connect start..").getBytes()));
							sendbuf.flip();
							break;
						case "encrypt":
							System.out.println("start encrypt...");
							result.write(ByteBuffer.wrap(String.valueOf("encrypt start..").getBytes()));
							sendbuf.flip();
							break;
						case "register":
							System.out.println("start register...");
							result.write(ByteBuffer.wrap(String.valueOf("register start..").getBytes()));
							sendbuf.flip();
							sendX06();
							System.out.println("send x06 count -- " + x06cnt);
							break;
						case "login":
							System.out.println("start login...");
							result.write(ByteBuffer.wrap(String.valueOf("login start..").getBytes()));
							sendbuf.flip();
							sendX10(startIndex);
							break;
						case "beat1":
							System.out.println("send ping");
							result.write(ByteBuffer.wrap(String.valueOf("ping start..").getBytes()));
							sendbuf.flip();
							sendX102();
							break;
						case "close":
							System.out.println("close");
							result.write(ByteBuffer.wrap(String.valueOf("close start..").getBytes()));
							sendbuf.flip();
							sendX101();
							break;
						default:
							break;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					try
					{
						result.close();
						server.accept(null, this);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void failed(Throwable exc, Object attachment) {
				System.out.print("Server failed...." + exc.getCause());
			}
		});
	}
	
	void testConnect() {
		new Thread(new Runnable()
		{
			@Override
			public void run() {
				int cnt = 0, index = 0;
				for (int bport = 1024; bport < 65535; bport++)
				{
					System.out.println("Connect -- " + cnt);
					try
					{
						if (isPortAvailable(bport))
						{
							for (int ii = 0; ii < bindAddrs.length; ii++)
							{
								String bhost = bindAddrs[ii];
								client.newConnect(new AioConnector(client, hosts[index++ % hosts.length], port, bhost, bport, filter));
								Thread.sleep(sleep);
							}
							cnt += bindAddrs.length;
							if (cnt + bindAddrs.length > count) break;
						}
					}
					catch (IOException | InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				
			}
		}).start();
	}
	
	void testRegister() {
		
	}
	
	private void bindPort(String host, int port) throws Exception {
		Socket s = new Socket();
		s.bind(new InetSocketAddress(host, port));
		s.close();
	}
	
	private boolean isPortAvailable(int port) {
		try
		{
			bindPort("0.0.0.0", port);
			for (int i = 0; i < bindAddrs.length; i++)
			{
				bindPort(bindAddrs[i], port);
			}
			
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public void sendCmd(IQoS content, AioSession session) {
		RingBuffer<TgxEvent> rb = core.getSendBuffer();
		long next = rb.next();
		TgxEvent e = rb.get(next);
		e.produce(TgxOpWrite.INSTANCE, session, content);
		rb.publish(next);
	}
	
	private Random r      = new Random();
	int            x06cnt = 0;
	
	public void sendX06() {
		byte[] xr = new byte[20];
		int cnt = 0;
		try
		{
			for (AioSession session : client.getSessions())
			{
				X06_RequestClientID x06 = new X06_RequestClientID();
				r.nextBytes(xr);
				x06.appId = crypt.sha256(xr);
				r.nextBytes(xr);
				x06.appKey = crypt.sha256(xr);
				x06.bluetooth_mac = r.nextLong();
				x06.domain = "tgxpush.com";
				r.nextBytes(x06.imsi);
				r.nextBytes(x06.imei);
				x06.wifi_mac = r.nextLong();
				x06.client_type = "1";
				sendCmd(session, x06);
				x06cnt++;
				
				cnt++;
				Thread.sleep(sleep);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void sendX10(int start) {
		long usrIndex = 0, clientIndex = start;
		int cnt = 0;
		try
		{
			for (AioSession session : client.getSessions())
			{
				X10_Login x10 = new X10_Login();
				x10.usr_token = MixUtil.mixToken(usrIndex);
				x10.client_token = MixUtil.mixToken(clientIndex++);
				sendCmd(session, x10);
				
				cnt++;
				Thread.sleep(sleep);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void sendX102() {
		try
		{
			System.out.println("s: " + client.sessionCount());
			for (AioSession session : client.getSessions())
			{
				System.out.println("Ping - -- -");
				for (int i = 0; i < 35; i++)
					sendCmd(session, new X102_Ping("ping -.-.-.-.-".getBytes()));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void sendX101() {
		try
		{
			System.out.println("s: " + client.sessionCount());
			for (AioSession session : client.getSessions())
			{
				System.out.println("close - -- -");
				sendCmd(session, new X101_Close("close - - - -".getBytes()));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
