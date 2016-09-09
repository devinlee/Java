package base.net.socket.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.net.socket.ISocketConnection;
import base.net.socket.SocketBase;
import base.net.socket.SocketClient;
import base.net.socket.SocketSelectorItem;
import base.thread.ThreadsFactory;
import base.types.SocketType;

public class SocketUdpClient extends SocketBase
{
	/**
	 * DatagramChannel
	 */
	private DatagramChannel datagramChannel;

	private SocketClient socketClient;

	private SocketSelectorItem socketSelectorItem;

	/**
	 * Socket 客户端 UDP 服务
	 * @param socketServer ISocketServer
	 * @param ipAddress 连接IP地址
	 * @param port 端口
	 * @param dataTransfer 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID。
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 * @throws Exception
	 */
	public SocketUdpClient(SocketClient socketClient, String ipAddress, int port, boolean dataTransfer, boolean isCrypto) throws Exception
	{
		super(SocketType.UDP, ipAddress, port, dataTransfer, isCrypto);
		this.socketClient = socketClient;
		this.selector = socketClient.getSocketSelector().getSelector();
		this.connectionReadPools = ThreadsFactory.threadPoolController().create("SocketUdpClientConnectionReadPools", Runtime.getRuntime().availableProcessors() + 5);
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
			datagramChannel.configureBlocking(false);
			socketSelectorItem = new SocketSelectorItem(this, socketClient.getSocketConnectionBase());
			datagramChannel.register(selector, SelectionKey.OP_READ, socketSelectorItem);
			datagramChannel.connect(new InetSocketAddress(ipAddress, port));
			socketClient.getSocketConnectionBase().setSocketChannel(datagramChannel, datagramChannel.getRemoteAddress());
			isAvailable = true;
			System.out.println(socketClient.getSocketName() + " SocketUdpClient 连接完成！");
			socketClient.connectable(SocketType.UDP);
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpClient.class.getName()).log(Level.SEVERE, "SocketUdpClient " + socketClient.getSocketName() + " 运行错误。", e);
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
//		super.handleSelectionKey(selectionKey, socketClient.getSocketConnectionBase());
		super.handleSelectionKey(selectionKey, socketConnection);
	}

	/**
	 *关闭服务 
	 */
	public synchronized void close()
	{
		super.close();
		try
		{
			if(datagramChannel!=null)
			{
				datagramChannel.close();
				datagramChannel=null;
			}
			socketSelectorItem=null;
			socketClient=null;
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpClient.class.getName()).log(Level.SEVERE, null, e);
		}
	}
}