package base.net.socket;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import base.net.socket.thread.SocketHandshakeReadThread;

public class SocketServerHandshake
{
	private SocketServer socketServer;
	
	/**
	 * ByteBuffer池
	 */
	private SocketByteBufferPool socketByteBufferPool;
	
	/**
	 * 读取数据处理线程池
	 */
	private ExecutorService connectionReadPools;
	
	/**
	 * 待握手的连接信息,数据格式：HashMap<连接ID, 连接校验码>
	 */
	private ConcurrentHashMap<Integer, Integer> connectionIDCheckCodes = new ConcurrentHashMap<Integer, Integer>();
	
	public SocketServerHandshake(SocketServer socketServer, ExecutorService connectionReadPools, SocketByteBufferPool socketByteBufferPool)
	{
		this.socketServer=socketServer;
		this.connectionReadPools=connectionReadPools;
		this.socketByteBufferPool=socketByteBufferPool;
	}
	
	/**
	 * 握手
	 * @param datagramChannel Udp通道
	 * @param socketAddress udp SocketAddress
	 * @param socketDataStructureType Socket数据包结构标识类型
	 * @param receiveByteBuffer 收到的数据
	 */
	public void handshake(DatagramChannel datagramChannel, SocketAddress socketAddress, byte socketDataStructureType, ByteBuffer receiveByteBuffer)
	{
		SocketHandshakeReadThread socketHandshakeReadThread = new SocketHandshakeReadThread(socketServer, this, datagramChannel, socketAddress, socketByteBufferPool, socketDataStructureType, receiveByteBuffer);
		connectionReadPools.execute(socketHandshakeReadThread);
	}
	
	/**
	 * 增加一个连接
	 * @param connectionID 连接ID
	 * @param connectionCheckCode 连接校验码
	 */
	public void addConnection(int connectionID, int connectionCheckCode)
	{
		connectionIDCheckCodes.put(connectionID, connectionCheckCode);
	}
	
	/**
	 * 检查是否存在连接
	 * @param connectionID 连接ID
	 * @param connectionCheckCode 连接校验码
	 */
	public boolean containsConnection(int connectionID, int connectionCheckCode)
	{
		if(!connectionIDCheckCodes.containsKey(connectionID))return false;
		int checkCode = connectionIDCheckCodes.get(connectionID);
		if(connectionCheckCode==checkCode)
		{
			return true;
		}
		return false;
	}
}