package base.net.socket;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;

import base.event.IEventListener;
import base.net.socket.packet.ISocketSendablePacketBase;
import base.net.socket.tcp.ISocketTcpConnection;
import base.net.socket.udp.SocketUdpConnection;
import base.types.SocketType;

public interface ISocketConnection
{
	/**
	 * 当前连接对象是否可用
	 * @return
	 */
	public boolean getIsAvailable();

	/**
	 * @param isWebSocket 要设置的 isWebSocket
	 */
	public void setIsWebSocket(boolean isWebSocket);

	/**
	 * 当前连接对象是否为WebSocket
	 * @return
	 */
	public boolean getIsWebSocket();

	/**
	 * 标识ID，全局唯一值
	 * @return id
	 */
	public int getId();

	/**
	 * SocketConnectionBase连接校验码
	 * @return connectionCheckCode
	 */
	public int getConnectionCheckCode();

	/**
	 * 通讯数据包加密密码
	 * @return cryptoKeys
	 */
	public int[] getCryptoKeys();
	
	/**
	 * 通讯数据包加密密码
	 */
	public void setCryptoKeys(int[] cryptoKeys);

	/**
	 * TCP连接是否握手完成
	 * @return handshakeCompleteTcp
	 */
	public boolean getHandshakeCompleteTcp();

	/**
	 * UDP连接是否握手完成
	 */
	public boolean getHandshakeCompleteUdp();
	
	/**
	 * UDP连接是否握手完成
	 */
	public void setHandshakeCompleteUdp(boolean handshakeCompleteUdp);

	/**
	 * Tcp连接
	 * @return socketTcpConnection
	 */
	public ISocketTcpConnection getSocketTcpConnection();

	/**
	 * Udp连接
	 * @return socketUdpConnection
	 */
	public SocketUdpConnection getSocketUdpConnection();

	/**
	 * 初始化
	 * @param dataTransfer 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID。
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 * @throws IOException
	 */
	public void init(boolean dataTransfer, boolean isCrypto);

	/**
	 * 取得接受的数据，并创建接收包予队列中
	 * @param socketType Socket通讯模式
	 * @param byteBuffer 从通道中读取到的ByteBuffer
	 */
	public void receiveData(SocketType socketType, ByteBuffer byteBuffer);

	/**
	 * 取得Tcp/Udp接受的数据，并创建接收包予队列中
	 * @param byteBuffer 从通道中读取到的ByteBuffer
	 */
	public void receiveDataTcpUdp(SocketType socketType);

	/**
	 * 向发送队列增加发送数据包
	 * @param socketSendablePacketBase 数据包
	 * @throws IOException 
	 */
	public void addQueueSendPacket(ISocketSendablePacketBase socketSendablePacketBase);

	/**
	 * 执行包发送
	 */
	public void sendPacket();

	/**
	 * 向发送队列增加发送数据包并执行发送
	 * @param socketSendablePacketBase 数据包
	 * @throws IOException 
	 */
	public void sendPacket(ISocketSendablePacketBase socketSendablePacketBase);

	/**
	 * 设置Tcp通道并创建Tcp连接
	 * @param socketChannel SocketChannel
	 */
	public void setSocketChannel(SocketChannel socketChannel);

	/**
	 * 设置Udp通道并创建Udp连接
	 * @param datagramChannel DatagramChannel
	 * @param socketAddress Udp通道的socketAddress
	 */
	public void setSocketChannel(DatagramChannel datagramChannel, SocketAddress socketAddress);
	
	/**
	 * 执行Tcp发送包队列发送
	 */
	public void sendSendablePacketQueue();

	/**
	 * 设置SocketConnectionBase标识
	 * @param id 标识ID，全局唯一
	 *  @param connectionCheckCode 校验码
	 */
	public void setMark(int id, int connectionCheckCode);

	/**
	 * 发送端(客户端)TCP向接收端(服务端)TCP发送连接成功
	 */
	public void clientToServerTcpConnecyionSuccess();

	/**
	 * 接收端(服务器端)TCP向发送端(客户端)TCP发送协议握手数据
	 */
	public void tcpToTcpHandshake();

	/**
	 * 发送端(客户端)TCP向接收端(服务端)TCP发送协议握手成功应答数据
	 * @param cryptoKeys 通讯加密密码
	 */
	public void tcpToTcpHandshakeAck(byte[] cryptoKeys);

	/**
	 * 接收端(服务端)TCP向发送端(客户端)TCP发送协议握手成功应答的应答数据
	 */
	public void tcpToTcpHandshakeAckAck();

	/**
	 * 发送端(客户端)UDP向接收端(服务器端)UDP发送UDP协议握手数据，数据内容就为包头中的连接ID与连接校验码
	 */
	public void udpToUdpHandshake();

//	/**
//	 * UDP协议连接握手成功，接收端(服务器端)TCP向发送端(客户端)TCP发送UDP成功握手消息
//	 * @param type 握手方式:0=udp自行握手;1=通过tcp握手
//	 */
//	public void udpToUdpHandshakeAck(byte type);
	
//	/**
//	 * UDP协议连接握手成功，接收端(服务器端)TCP向发送端(客户端)TCP发送UDP成功握手消息应答的应答
//	 * @param type 握手方式:0=udp自行握手;1=通过tcp握手
//	 */
//	public void udpToUdpHandshakeAckAck();

	/**
	 * 接收端(服务器端)WebSocket向发送端(客户端)WebSocket发送协议握手数据
	 */
	public void webSocketToWebSocketHandshake(String receiveText);

	/**
	 * ByteBuffer池
	 */
	public SocketByteBufferPool getSocketByteBufferPool();
	
	/**
	 * ByteBuffer池
	 */
	public void setSocketByteBufferPool(SocketByteBufferPool socketByteBufferPool);
	
	/**
	 * RSA密钥
	 */
	public void setRSAKeys(HashMap<String, Object> rsaKeys);
	
	/**
	 * RSA密钥
	 */
	public HashMap<String, Object> getRSAKeys();
	
	/**
	 * RSA公钥
	 */
	public RSAPublicKey getPublicKey();
	
	/**
	 * RSA私钥
	 */
	public RSAPrivateKey getPrivateKey();
	
	/**
	 * 关闭连接
	 * @param closeType 关闭类型：0=服务器内部主动断开关闭;1=外部强制关闭(比如手动离开一个玩家时，从玩家类调用关闭);2=客户端主动断开网络关闭
	 */
	public void close(byte closeType);
	
	/**
	 * 关闭连接
	 */
	public void close();
	
	/**
	 * 连接心跳判断
	 * @param keepAliveTimeMax 心跳最大间隔毫秒时间
	 * @return 如果最后一次收到的心跳时间超过了指定的最大间隔时间，返回false,否则返回true
	 */
	public boolean checkKeepAlive(long keepAliveTimeMax);
	
	/**
	 * 增加事件监听
	 * @param type 事件类型
	 * @param eventListener 事件接收者
	 * @return
	 */
	public void addEventListener(String type, IEventListener eventListener);

	/**
	 * 是否存在指定的事件监听
	 * @param type 事件类型
	 * @param eventListener 事件接收者
	 * @return
	 */
	public boolean hasEventListener(String type, IEventListener eventListener);

	public void removeEventListener(String type, IEventListener eventListener);

	public void dispose();
}