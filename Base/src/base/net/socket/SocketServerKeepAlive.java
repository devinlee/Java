package base.net.socket;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import base.timer.TimerTask;

public class SocketServerKeepAlive extends TimerTask
{
	/**
	 * SocketConnectionBase列表容器
	 */
	private ConcurrentHashMap<Integer, ISocketConnection> socketConnections;

	public SocketServerKeepAlive(ConcurrentHashMap<Integer, ISocketConnection> socketConnections)
	{
		this.socketConnections=socketConnections;
	}

	@Override
	public void run()
	{
		start();
	}

	private void start()
	{
		if(socketConnections!=null && socketConnections.size()>0)
		{
			Iterator<Entry<Integer, ISocketConnection>> iterator = socketConnections.entrySet().iterator();
			while(iterator.hasNext())
			{
				Entry<Integer, ISocketConnection> socketConnections = iterator.next();
				ISocketConnection socketConnection = socketConnections.getValue();
				if(!socketConnection.checkKeepAlive(15000))
				{//15秒为最大间隔时间，如果超过时间，则直接断开其连接
					socketConnection.close((byte)2);
					System.out.println("客户端连接超过心跳最大时间被断开");
				}
			}
		}
	}
}