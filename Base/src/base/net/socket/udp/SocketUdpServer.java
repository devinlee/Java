package base.net.socket.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.net.socket.ISocketConnection;
import base.net.socket.SocketBase;
import base.net.socket.SocketSelectorItem;
import base.net.socket.SocketServer;
import base.net.socket.thread.SocketReadThread;
import base.types.SocketDataStructureType;
import base.types.SocketType;

/**
 * Socket UDP 服务
 * @author Devin
 *
 */
public class SocketUdpServer extends SocketBase
{
	private SocketServer socketServer;

	/**
	 * DatagramChannel
	 */
	private DatagramChannel datagramChannel;

	/**
	 * Socket UDP 服务
	 * @param serverName Socket服务名称
	 * @param ipAddress 连接IP地址
	 * @param port 端口
	 * @param dataTransfer 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID。
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 * @throws Exception
	 */
	public SocketUdpServer(SocketServer socketServer, String ipAddress, int port, int clientPoolSize, boolean dataTransfer, boolean isCrypto) throws Exception
	{
		super(SocketType.UDP, ipAddress, port, dataTransfer, isCrypto);
		this.socketServer = socketServer;
		this.selector = socketServer.getSocketSelector().getSelector();
	}

	/**
	 * 开始服务
	 */
	public void startServer()
	{
		super.startServer();
		try
		{
			datagramChannel = DatagramChannel.open();
			datagramChannel.socket().bind(new InetSocketAddress(ipAddress, port));
			datagramChannel.configureBlocking(false);
			datagramChannel.register(selector, SelectionKey.OP_READ, new SocketSelectorItem(this));
			isAvailable = true;
			System.out.println(socketServer.getSocketName() + " SocketUdpServer 启动完成！");
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpServer.class.getName()).log(Level.SEVERE, "SocketUdpServer " + socketServer.getSocketName() + " 运行错误。", e);
		}
	}

	/**
	 * 处理 SelectionKey
	 * @param selectionKey SelectionKey
	 * @throws IOException
	 */
	public void handleSelectionKey(SelectionKey selectionKey, ISocketConnection socketConnection)
	{
		try
		{
			if (selectionKey.isValid() && selectionKey.isReadable())
			{// 有可读信息
				DatagramChannel channel = (DatagramChannel) selectionKey.channel();
				ByteBuffer byteBuffer = socketByteBufferPool.getByteBuffer();
				byteBuffer.clear();
				SocketAddress socketAddress = channel.receive(byteBuffer);
				if (socketAddress != null)
				{
					byteBuffer.flip();
					if (byteBuffer.hasRemaining())
					{
						byte socketDataStructureType = byteBuffer.get();// 取得由一个字节组成的标识
						switch(socketDataStructureType)
						{
							case SocketDataStructureType.CONNECTION_UDP:
							case SocketDataStructureType.HANDSHAKE_UDP:
							case SocketDataStructureType.HANDSHAKE_UDP_ACK_ACK:
								socketServerHandshake.handshake(channel, socketAddress, socketDataStructureType, byteBuffer);
								break;
							default:
								int connectionID = byteBuffer.getInt();// 取得标识ID(每个标识ID，对应一个SocketConnectionBase对象，与tcp共用)
								int connectionCheckCode = byteBuffer.getInt();// 取得SocketConnectionBase连接校验码
								socketConnection = socketServer.getSocketConnectionBaseById(connectionID, connectionCheckCode);
								if (socketConnection != null && socketConnection.getHandshakeCompleteUdp())
								{
									socketConnection.setSocketChannel(channel, socketAddress);
									SocketReadThread socketReadThread = new SocketReadThread(SocketType.UDP, socketConnection, byteBuffer);
									connectionReadPools.execute(socketReadThread);
								}
								break;
						}
					}
					else
					{
						socketByteBufferPool.releaseByteBuffer(byteBuffer);
						byteBuffer = null;
					}
				}
				else
				{
					socketByteBufferPool.releaseByteBuffer(byteBuffer);
					byteBuffer = null;
				}
			}
			else if (selectionKey.isValid() && selectionKey.isWritable())
			{
				System.out.println("selectionKey.isValid() && selectionKey.isWritable()");
			}
		}
		catch (Exception e)
		{
			selectionKey.attach(null);
			selectionKey.cancel();
			Logger.getLogger(SocketUdpServer.class.getName()).log(Level.SEVERE, "SocketUdpServer " + socketServer.getSocketName() + " 运行错误。", e);
		}
		finally
		{
			if (!isAvailable)
			{
				close();
			}
		}
	}

	/**
	 *关闭服务 
	 */
	public synchronized void close()
	{
		super.close();
		try
		{
			if (datagramChannel != null)
			{
				SelectionKey key = datagramChannel.keyFor(selector);
				if (key != null)
					key.cancel();
				datagramChannel.close();
				datagramChannel = null;
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpServer.class.getName()).log(Level.SEVERE, "SocketUdpServer " + socketServer.getSocketName() + " 运行错误。", e);
		}
	}
}
