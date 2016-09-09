package base.types;

/**
 * Socket数据包结构标识类型
 *
 */
public class SocketDataStructureType
{
	/**
	 * 数据包
	 */
	public final static byte DATA = 0;

	/**
	 * 控制包-TCP协议连接(发送端(客户端)TCP向接收端(服务端)TCP发送)
	 */
	public final static byte CONNECTION_TCP = 10;

	/**
	 * 控制包-TCP协议握手(由TCP接收端(服务端)发送握手数据给TCP发送端(客户端))
	 */
	public final static byte HANDSHAKE_TCP = 11;

	/**
	 * 控制包-TCP协议握手(发送端(客户端)TCP向接收端(服务端)TCP发送)应答
	 */
	public final static byte HANDSHAKE_TCP_ACK = 12;

	/**
	 * 控制包-TCP协议握手(接收端(服务端)TCP向发送端(客户端)TCP发送)应答的应答
	 */
	public final static byte HANDSHAKE_TCP_ACK_ACK = 13;
	
	/**
	 * 控制包-UDP协议进行连接(发送端(客户端)UDP向接收端(服务端)UDP发送)
	 */
	public final static byte CONNECTION_UDP = 20;
	
	/**
	 * 控制包-UDP协议进行连接(接收端(服务端)UDP向发送端(客户端)UDP发送)
	 */
	public final static byte CONNECTION_UDP_ACK = 21;
	
//	/**
//	 * 控制包-UDP协议连接成功(发送端(客户端)UDP向接收端(服务端)UDP发送)
//	 */
//	public final static byte CONNECTION_SUCCESS_UDP = 22;

	/**
	 * 控制包-UDP协议握手(由UDP发送端(客户端)发送握手数据给UDP接收端(服务端))
	 */
	public final static byte HANDSHAKE_UDP = 23;

	/**
	 * 控制包-UDP协议握手成功应答(接收端(服务端)UDP向发送端(客户端)UDP发送)
	 */
	public final static byte HANDSHAKE_UDP_ACK = 24;
	
	/**
	 * 控制包-UDP协议握手(发送端(客户端)UDP向接收端(服务端)UDP发送)应答的应答
	 */
	public final static byte HANDSHAKE_UDP_ACK_ACK = 25;

	/**
	 * 控制包-UDP协议握手(接收端(服务端)UDP向发送端(客户端)UDP发送)应答的应答的应答
	 */
	public final static byte HANDSHAKE_UDP_ACK_ACK_ACK = 26;
	
	/**
	 * 控制包-应答ACK
	 */
	public final static byte ACK = 29;

	/**
	 * 控制包-协议心跳保活
	 */
	public final static byte KEEP_ALIVE = 30;
	
	/**
	 * 控制包-协议心跳保活(接收端(服务端)UDP向发送端(客户端)UDP发送)应答
	 */
	public final static byte KEEP_ALIVE_ACK = 31;
	
	/**
	 * 控制包-关闭协议
	 */
	public final static byte CLOSE = 40;
}
