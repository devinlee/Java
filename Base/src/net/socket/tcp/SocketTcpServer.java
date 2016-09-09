package base.net.socket.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.net.NetFactory;
import base.net.socket.SocketBase;
import base.net.socket.ISocketConnection;
import base.net.socket.SocketSelectorItem;
import base.net.socket.SocketServer;
import base.types.SocketType;

public class SocketTcpServer extends SocketBase
{
	/**
	 * SocketServer
	 */
	private SocketServer socketServer;
	/**
	 * ServerSocketChannel
	 */
	private ServerSocketChannel serverSocketChannel;

	/**
	 * Socket TCP 服务
	 * @param socketServer ISocketServer
	 * @param ipAddress 连接IP地址
	 * @param port 端口
	 * @param clientPoolSize 连接处理线程最大数
	 * @param dataTransfer 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID。
	 * @throws Exception
	 */
	public SocketTcpServer(SocketServer socketServer, String ipAddress, int port, int clientPoolSize, boolean dataTransfer, boolean isCrypto) throws Exception
	{
		super(SocketType.TCP, ipAddress, port, dataTransfer, isCrypto);
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
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(ipAddress, port));
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, new SocketSelectorItem(this));
			isAvailable = true;
			System.out.println(socketServer.getSocketName() + " SocketTcpServer 启动完成！");
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketTcpServer.class.getName()).log(Level.SEVERE, "SocketTcpServer " + socketServer.getSocketName() + " 运行错误。", e);
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
	 * 处理 SelectionKey
	 * @param selectionKey SelectionKey
	 * @throws Exception
	 * @throws IOException
	 */
	public void handleSelectionKey(SelectionKey selectionKey, ISocketConnection socketConnection)
	{
		try
		{
			if (selectionKey.isValid() && selectionKey.isAcceptable())
			{// 有新连接
				try
				{
//					System.out.println("新连接1");
					SocketChannel socketChannel = serverSocketChannel.accept();
					if (socketChannel != null)
					{
						socketChannel.configureBlocking(false);
						selectionKey.interestOps(SelectionKey.OP_ACCEPT);
						socketConnection = NetFactory.socketController().createSocketConnectionBase(dataTransfer, isCrypto);
						boolean isWebSocket = false;// 是否为WebSocket连接
//						System.out.println("新连接2 LocalAddressHost:"+socketChannel.socket().getLocalAddress().toString() + " InetAddress:"+socketChannel.socket().getInetAddress().toString() + " "+socketChannel.socket().getInetAddress() + " Por:"+socketChannel.socket().getPort() + " LocalPort:" + socketChannel.socket().getLocalPort());
						// String receiveText=null;
						// ByteBuffer receiveByteBuffer = socketConnection.getTransferTcpByteBuffer();
						// receiveByteBuffer.clear();
						// int len = socketChannel.read(receiveByteBuffer);
						// if(len>0)
						// {
						// byte[] bytes=new byte[len];
						// receiveByteBuffer.flip();
						// receiveByteBuffer.get(bytes, 0, len);
						// receiveText = new String(bytes, 0, len, "UTF-8");
						// bytes=null;
						// isWebSocket=WebSocketUtil.isWebSocket(receiveText);
						// }
						// receiveByteBuffer.clear();
						// receiveByteBuffer=null;
						int[] connectionIDCheckCodes = socketServer.getConnectionIDCheckCode();
						if(connectionIDCheckCodes!=null)
						{
							selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
							socketConnection.setIsWebSocket(isWebSocket);
							socketConnection.setSocketChannel((SocketChannel) selectionKey.channel());
							selectionKey.attach(new SocketSelectorItem(this, socketConnection));
							socketServer.addSocketConnectionBase(socketConnection, connectionIDCheckCodes[0], connectionIDCheckCodes[1]);
							socketConnection.sendSendablePacketQueue();
						}
						else
						{
							socketConnection.close();
						}
//						System.out.println("新连接3 LocalAddressHost:"+socketChannel.socket().getLocalAddress().toString() + " InetAddress:"+socketChannel.socket().getInetAddress().toString() + " "+socketChannel.socket().getInetAddress() + " Por:"+socketChannel.socket().getPort() + " LocalPort:" + socketChannel.socket().getLocalPort());

						// if(isWebSocket)
						// {//如果为WebSocket连接，则执行连接握手
						// socketConnection.webSocketToWebSocketHandshake(receiveText);
						// }
					}
				}
				catch (IOException e)
				{
					Logger.getLogger(SocketTcpServer.class.getName()).log(Level.SEVERE, "SocketTcpServer " + socketServer.getSocketName() + " 运行错误。", e);
				}
			}
		}
		catch (Exception e)
		{
			selectionKey.attach(null);
			selectionKey.cancel();
			Logger.getLogger(SocketTcpServer.class.getName()).log(Level.SEVERE, "SocketTcpServer " + socketServer.getSocketName() + " 运行错误。", e);
		}
		super.handleSelectionKey(selectionKey, socketConnection);
	}

	/**
	 * 移除一个SocketConnection
	 * @param socketConnection SocketConnection对象
	 */
	protected void removeSocketConnection(ISocketConnection socketConnection)
	{
		super.removeSocketConnection(socketConnection);
		socketServer.removeSocketConnection(socketConnection);
	}

	/**
	 *关闭服务 
	 */
	public synchronized void close()
	{
		super.close();
		try
		{
			if (serverSocketChannel != null)
			{
				SelectionKey key = serverSocketChannel.keyFor(selector);
				if (key != null)
					key.cancel();
				serverSocketChannel.close();
				serverSocketChannel = null;
			}
		}
		catch (IOException e)
		{
			Logger.getLogger(SocketTcpServer.class.getName()).log(Level.SEVERE, "关闭 SocketTcpServer " + socketServer.getSocketName() + " 时错误。", e);
		}
	}
}
