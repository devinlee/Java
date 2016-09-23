package base.net.socket.thread;

import java.math.BigInteger;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.net.NetFactory;
import base.net.socket.ISocketConnection;
import base.net.socket.SocketByteBufferPool;
import base.net.socket.SocketServer;
import base.net.socket.SocketServerHandshake;
import base.net.socket.SocketUtil;
import base.net.socket.packet.ISocketSendablePacketBase;
import base.types.SocketDataStructureType;
import base.types.SocketType;
import base.utils.RC4Util;

public class SocketHandshakeReadThread implements Runnable
{
	/**
	 * Socket类型
	 */
	private SocketType socketType;

	private SocketServer socketServer;

	/**
	 * 接收的数据
	 */
	private ByteBuffer receiveByteBuffer;

	private DatagramChannel datagramChannel;

	private SocketAddress socketAddress;
	
	private byte socketDataStructureType;

	/**
	 * ByteBuffer池
	 */
	protected SocketByteBufferPool socketByteBufferPool;
	
	private SocketServerHandshake socketServerHandshake;

	/**
	 * Socket读取处理线程
	 * @param socketType SocketType
	 * @param socketConnection SocketConnection
	 * @param receiveByteBuffer 收到的数据
	 */
	public SocketHandshakeReadThread(SocketServer socketServer, SocketServerHandshake socketServerHandshake, DatagramChannel datagramChannel, SocketAddress socketAddress, SocketByteBufferPool socketByteBufferPool, byte socketDataStructureType, ByteBuffer receiveByteBuffer)
	{
		this.socketServer=socketServer;
		this.datagramChannel=datagramChannel;
		this.socketAddress=socketAddress;
		this.socketType=SocketType.UDP;
		this.socketByteBufferPool=socketByteBufferPool;
		this.socketDataStructureType=socketDataStructureType;
		this.receiveByteBuffer = receiveByteBuffer;
		this.socketServerHandshake=socketServerHandshake;
	}

	@Override
	public void run()
	{
		try
		{
		if (socketType == SocketType.TCP)
		{
			
		}
		else if(socketType == SocketType.UDP)
		{
			if (datagramChannel!=null && socketAddress != null)
			{
				switch(socketDataStructureType)
				{
					case SocketDataStructureType.CONNECTION_UDP:
						int[] idCheckCodes = socketServer.getConnectionIDCheckCode();//取得一组连接信息
						if(idCheckCodes!=null)
						{
							//发送连接应答
							ISocketSendablePacketBase socketSendablePacketClient = SocketUtil.socketSendablePacketClient(SocketType.UDP, SocketDataStructureType.CONNECTION_UDP_ACK);
							socketSendablePacketClient.putInt(idCheckCodes[0]);//连接ID
							socketSendablePacketClient.putInt(idCheckCodes[1]);//连接校验码
							socketSendablePacketClient.end(false);
							socketServerHandshake.addConnection(idCheckCodes[0], idCheckCodes[1]);
							datagramChannel.send(socketSendablePacketClient.getData(), socketAddress);
							socketSendablePacketClient.dispose();
							socketSendablePacketClient=null;
						}
						socketByteBufferPool.releaseByteBuffer(receiveByteBuffer);
						receiveByteBuffer=null;
						break;
					case SocketDataStructureType.HANDSHAKE_UDP:
						byte type = receiveByteBuffer.get();//握手方式:0=udp自行握手;1=通过tcp握手
						int connectionID = receiveByteBuffer.getInt();// 取得标识ID(每个标识ID，对应一个SocketConnectionBase对象，与tcp共用)
						int connectionCheckCode = receiveByteBuffer.getInt();// 取得SocketConnectionBase连接校验码
						if(type==1)
						{//通过Tcp连接握手
							ISocketConnection socketConnection = socketServer.getSocketConnectionBaseById(connectionID, connectionCheckCode);
							if (socketConnection != null && !socketConnection.getHandshakeCompleteUdp())
							{
								socketConnection.setSocketChannel(datagramChannel, socketAddress);// Udp通道与SocketConnectionBase建立绑定
								ISocketSendablePacketBase socketSendablePacketClient = SocketUtil.socketSendablePacketClient(SocketType.UDP, SocketDataStructureType.HANDSHAKE_UDP_ACK);
								socketSendablePacketClient.put(type);
								socketSendablePacketClient.end(false);
								datagramChannel.send(socketSendablePacketClient.getData(), socketAddress);
								socketSendablePacketClient.dispose();
								socketSendablePacketClient=null;
							}
						}
						else
						{//Udp连接自行握手
							if(socketServerHandshake.containsConnection(connectionID, connectionCheckCode))
							{//如果找到并通过检查，说明为合法的握手信息
								ISocketConnection socketConnection = socketServer.getSocketConnectionBaseById(connectionID, connectionCheckCode);//以防客户端重复发握手消息，先查找是否有对应的连接
								if(socketConnection==null)
								{
									//创建连接
									socketConnection = NetFactory.socketController().createSocketConnectionBase(socketServer.isDataTransfer(), socketServer.isCrypto());
									socketServer.addSocketConnectionBase(socketConnection, connectionID, connectionCheckCode);
								}
								socketConnection.setSocketChannel(datagramChannel, socketAddress);// Udp通道与SocketConnectionBase建立绑定

								ISocketSendablePacketBase socketSendablePacketClient = SocketUtil.socketSendablePacketClient(SocketType.UDP, SocketDataStructureType.HANDSHAKE_UDP_ACK);
								socketSendablePacketClient.put(type);
								BigInteger bigIntegerModulus = socketConnection.getPublicKey().getModulus();
								BigInteger bigIntegerPublicExponent = socketConnection.getPublicKey().getPublicExponent();
								byte[] modulusBytes = null;
								byte[] publicExponentBytes = null;
								modulusBytes = bigIntegerModulus.toByteArray();
								publicExponentBytes = bigIntegerPublicExponent.toByteArray();

								socketSendablePacketClient.putInt(connectionID);// 连接ID
								socketSendablePacketClient.putInt(connectionCheckCode);// 连接校验码
								socketSendablePacketClient.putInt(modulusBytes.length);
								socketSendablePacketClient.put(modulusBytes);
								socketSendablePacketClient.putInt(publicExponentBytes.length);
								socketSendablePacketClient.put(publicExponentBytes);
								socketSendablePacketClient.end(false);
								datagramChannel.send(socketSendablePacketClient.getData(), socketAddress);
								socketSendablePacketClient.dispose();
								socketSendablePacketClient=null;
							}
						}
						socketByteBufferPool.releaseByteBuffer(receiveByteBuffer);
						receiveByteBuffer=null;
						break;
					case SocketDataStructureType.HANDSHAKE_UDP_ACK_ACK:
						type = receiveByteBuffer.get();//握手方式:0=udp自行握手;1=通过tcp握手
						connectionID = receiveByteBuffer.getInt();// 取得标识ID(每个标识ID，对应一个SocketConnectionBase对象，与tcp共用)
						connectionCheckCode = receiveByteBuffer.getInt();// 取得SocketConnectionBase连接校验码
						ISocketConnection socketConnection = socketServer.getSocketConnectionBaseById(connectionID, connectionCheckCode);
						if (socketConnection != null && !socketConnection.getHandshakeCompleteUdp())
						{
							if(type==0)
							{//为udp自行握手
								byte[] rsaCryptoKeys = new byte[receiveByteBuffer.remaining()];
								receiveByteBuffer.get(rsaCryptoKeys);
								int[] cryptoKeys = RC4Util.getKey(rsaCryptoKeys);
								socketConnection.setCryptoKeys(cryptoKeys);
							}
							socketConnection.setSocketChannel(datagramChannel, socketAddress);// Udp通道与SocketConnectionBase建立绑定
							socketConnection.setHandshakeCompleteUdp(true);

							ISocketSendablePacketBase socketSendablePacketClient = SocketUtil.socketSendablePacketClient(SocketType.UDP, SocketDataStructureType.HANDSHAKE_UDP_ACK_ACK_ACK);
							socketSendablePacketClient.end(false);
							datagramChannel.send(socketSendablePacketClient.getData(), socketAddress);
							socketSendablePacketClient.dispose();
							socketSendablePacketClient=null;
						}
						socketByteBufferPool.releaseByteBuffer(receiveByteBuffer);
						receiveByteBuffer=null;
						break;
				}
			}
			else
			{
				socketByteBufferPool.releaseByteBuffer(receiveByteBuffer);
				receiveByteBuffer=null;
			}
		}
		}
		catch (Exception e)
		{
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
	}
}
