package base.net.socket;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.Base;
import base.net.socket.packet.SocketSendablePacketClientPool;
import base.types.SocketType;

/**
 * Socket控制器
 * @author Devin
 *
 */
public class SocketController
{
	/**
	 * SocketSendablePacketClient的ByteBuffer池
	 */
	private SocketSendablePacketClientPool socketSendablePacketClientPool = null;

	/**
	 * SocketSendablePacketClient的ByteBuffer池
	 */
	public SocketSendablePacketClientPool getSocketSendablePacketClientPool()
	{
		return this.socketSendablePacketClientPool;
	}

	private SocketSelector socketSelectorClient;
	private SocketSelector socketSelectorServer;

	public SocketController()
	{
		socketSendablePacketClientPool = new SocketSendablePacketClientPool();
		socketSelectorClient = new SocketSelector();
		socketSelectorServer = new SocketSelector();
	}

	public void startSocketSelectorServer()
	{
		if (!socketSelectorServer.getIsAvailable())
			socketSelectorServer.start();
	}

	public void startSocketSelectorClient()
	{
		if (!socketSelectorClient.getIsAvailable() && !socketSelectorClient.isAlive())
			socketSelectorClient.start();
	}

	/**
	 * 创建Socket服务
	 * @param serverName 服务名称
	 * @param socketType Socket通讯模式
	 * @param ipAddress 地址
	 * @param port 端口
	 * @param clientPoolSize 客户端线程池大小
	 * @param dataTransfer 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket连接ID，否则不读取初始来源的Socket连接ID。
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 * @param isCheckKeepAlive 是否检测心跳时间
	 */
	public SocketServer createSocketServer(String serverName, SocketType socketType, String ipAddress, int port, int clientPoolSize, boolean dataTransfer, boolean isCrypto, boolean isCheckKeepAlive)
	{
		SocketServer socketServer = null;
		try
		{
			socketServer = new SocketServer(socketSelectorServer, serverName, socketType, ipAddress, port, clientPoolSize, dataTransfer, isCrypto, isCheckKeepAlive);
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketController.class.getName()).log(Level.SEVERE, "创建SocketServer错误。", e);
		}
		return socketServer;
	}

	/**
	 * 创建SocketClient
	 * @param clientName 名称
	 * @param socketType Socket通讯模式
	 * @param ipAddress 地址
	 * @param port 端口
	 * @param dataTransfer 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 */
	public SocketClient createSocketClient(String clientName, SocketType socketType, String ipAddress, int port, boolean dataTransfer, boolean isCrypto, boolean isSendKeepAlive)
	{
		SocketClient socketClient = null;
		try
		{
			socketClient = new SocketClient(socketSelectorClient, clientName, socketType, ipAddress, port, dataTransfer, isCrypto, isSendKeepAlive);
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketController.class.getName()).log(Level.SEVERE, "创建SocketClient错误。", e);
		}
		return socketClient;
	}

	/**
	 * 创建SocketConnectionBase
	 * @param dataTransfer 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID。
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 * @param isWebsocket 是否为Websocket连接
	 * @return 返回成功创建的SocketConnectionBase
	 */
	public ISocketConnection createSocketConnectionBase(boolean dataTransfer, boolean isCrypto)
	{
		return createSocketConnectionBase(dataTransfer, isCrypto, false);
	}

	/**
	 * 创建SocketConnectionBase
	 * @param dataTransfer 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID。
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 * @param isWebsocket 是否为Websocket连接
	 * @return 返回成功创建的SocketConnectionBase
	 */
	public ISocketConnection createSocketConnectionBase(boolean dataTransfer, boolean isCrypto, boolean isWebsocket)
	{
		ISocketConnection socketConnection = Base.newClass(SocketConnection.class);
		socketConnection.init(dataTransfer, isCrypto);
		socketConnection.setIsWebSocket(isWebsocket);
		return socketConnection;
	}

	/**
	 * 从SendablePacketClient的ByteBuffer池中取得一个ByteBuffer
	 * @return ByteBuffer
	 */
	public ByteBuffer getSendablePacketClientByteBuffer()
	{
		return getSendablePacketClientByteBuffer(SocketType.TCP);
	}
	
	/**
	 * 从SendablePacketClient的ByteBuffer池中取得一个ByteBuffer
	 * @return ByteBuffer
	 */
	public ByteBuffer getSendablePacketClientByteBuffer(SocketType socketType)
	{
		return socketSendablePacketClientPool.getByteBuffer(socketType);
	}

	/**
	 * 回收一个SendablePacketClient的ByteBuffer池至池中
	 */
	public void releaseSendablePacketClientByteBuffer(ByteBuffer byteBuffer)
	{
		releaseSendablePacketClientByteBuffer(byteBuffer, SocketType.TCP);
	}
	
	/**
	 * 回收一个SendablePacketClient的ByteBuffer池至池中
	 */
	public void releaseSendablePacketClientByteBuffer(ByteBuffer byteBuffer, SocketType socketType)
	{
		socketSendablePacketClientPool.releaseByteBuffer(byteBuffer, socketType);
	}
}
