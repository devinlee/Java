package base.net.socket.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.net.socket.SocketBase;
import base.net.socket.SocketClient;
import base.net.socket.ISocketConnection;
import base.net.socket.SocketSelectorItem;
import base.thread.ThreadsFactory;
import base.types.SocketType;

public class SocketTcpClient extends SocketBase
{
	/**
	 * SocketChannel
	 */
	private SocketChannel socketChannel;

	private SocketClient socketClient;

	private SocketSelectorItem socketSelectorItem;

	/**
	 * Socket 客户端 TCP 服务
	 * @param socketServer ISocketServer
	 * @param ipAddress 连接IP地址
	 * @param port 端口
	 * @param dataTransfer 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID。
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 * @throws Exception
	 */
	public SocketTcpClient(SocketClient socketClient, String ipAddress, int port, boolean dataTransfer, boolean isCrypto) throws Exception
	{
		super(SocketType.TCP, ipAddress, port, dataTransfer, isCrypto);
		this.socketClient = socketClient;
		this.selector = socketClient.getSocketSelector().getSelector();
		this.connectionReadPools = ThreadsFactory.threadPoolController().create("SocketTcpClientConnectionReadPools", Runtime.getRuntime().availableProcessors() + 5);
	}

	/**
	 * 开始服务
	 */
	public void startServer()
	{
		super.startServer();
		try
		{
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			socketSelectorItem = new SocketSelectorItem(this, socketClient.getSocketConnectionBase());
			socketChannel.register(selector, SelectionKey.OP_CONNECT, socketSelectorItem);
			socketChannel.connect(new InetSocketAddress(ipAddress, port));
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketTcpClient.class.getName()).log(Level.SEVERE, "SocketTcpServer " + socketClient.getSocketName() + " 运行错误。", e);
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
			if (selectionKey.isValid() && selectionKey.isConnectable())
			{// 已成功连接
				SocketChannel channel = null;
				try
				{
					channel = (SocketChannel) selectionKey.channel();
					if (finishConnect(channel))
					{
						channel.configureBlocking(false);
						channel.register(selector, SelectionKey.OP_READ);
						socketClient.getSocketConnectionBase().setSocketChannel(channel);
						selectionKey.attach(socketSelectorItem);
						isAvailable = true;
						System.out.println(socketClient.getSocketName() + " SocketTcpClient 连接完成！");
						socketClient.connectable(SocketType.TCP);
						socketClient.getSocketConnectionBase().sendSendablePacketQueue();
					}
					else
					{
						Thread.sleep(5000);// 延迟5000毫秒后，以准备重新创建连接服务
					}
				}
				catch (Exception e)
				{
					isAvailable = false;
					if (!isAvailable)
					{// 如果连接失败，将重新连接
						socketChannel.connect(new InetSocketAddress(ipAddress, port));
					}
					Logger.getLogger(SocketTcpClient.class.getName()).log(Level.SEVERE, "新的SocketClient连接处理错误。", e);
				}
			}
		}
		catch (Exception e)
		{
			selectionKey.attach(null);
			selectionKey.cancel();
			Logger.getLogger(SocketTcpClient.class.getName()).log(Level.SEVERE, "SocketTcpClient " + socketClient.getSocketName() + " 运行错误。", e);
		}
		super.handleSelectionKey(selectionKey, socketConnection);
	}

	/**
	 * 完成连接
	 * @param channel SocketChannel
	 * @return 连接完成立即返回true,否则返回false
	 */
	private boolean finishConnect(SocketChannel channel)
	{
		boolean isFinish = false;
		if (channel.isConnectionPending())
		{
			try
			{
				isFinish = channel.finishConnect();
				Thread.sleep(100);
			}
			catch (Exception e)
			{
				isFinish = false;
			}
		}
		return isFinish;
	}

	/**
	 *关闭服务 
	 */
	public synchronized void close()
	{
		super.close();
		socketClient = null;
		socketChannel = null;
		if (socketSelectorItem != null)
		{
			socketSelectorItem.dispose();
			socketSelectorItem = null;
		}
	}
}
