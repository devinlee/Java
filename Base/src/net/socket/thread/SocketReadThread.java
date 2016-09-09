package base.net.socket.thread;

import java.nio.ByteBuffer;

import base.net.socket.ISocketConnection;
import base.types.SocketType;

/**
 * Socket读取处理线程
 * @author Devin
 *
 */
public class SocketReadThread implements Runnable
{
	/**
	 * Socket类型
	 */
	private SocketType socketType;

	/**
	 * SocketConnection
	 */
	private ISocketConnection socketConnection;
	
	/**
	 * 接收的数据
	 */
	private ByteBuffer receiveByteBuffer;

	/**
	 * Socket读取处理线程
	 * @param socketType SocketType
	 * @param socketConnection SocketConnection
	 * @param receiveByteBuffer 收到的数据
	 */
	public SocketReadThread(SocketType socketType, ISocketConnection socketConnection)
	{
		this(socketType, socketConnection, null);
	}
	
	/**
	 * Socket读取处理线程
	 * @param socketType SocketType
	 * @param socketConnection SocketConnection
	 * @param receiveByteBuffer 收到的数据
	 */
	public SocketReadThread(SocketType socketType, ISocketConnection socketConnection, ByteBuffer receiveByteBuffer)
	{
		this.socketType = socketType;
		this.socketConnection = socketConnection;
		this.receiveByteBuffer = receiveByteBuffer;
	}

	@Override
	public void run()
	{
		if (socketType == SocketType.TCP)
		{
			socketConnection.receiveDataTcpUdp(socketType);
		}
		else if(socketType == SocketType.UDP)
		{
			socketConnection.getSocketUdpConnection().receiveData(receiveByteBuffer);
		}
	}
}