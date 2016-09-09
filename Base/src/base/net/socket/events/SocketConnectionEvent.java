package base.net.socket.events;

import base.event.Event;

/**
 * Socket事件
 *
 */
public class SocketConnectionEvent extends Event
{
	/**
	 * 连接成功
	 */
	public final static String CONNECTABLE = "connectable";

	/**
	 * 新连接
	 */
	public final static String NEW_CONNECTION = "newConnection";
	/**
	 * 移除连接
	 */
	public final static String REMOVE_CONNECTION = "removeConnection";

	/**
	 * 关闭连接
	 */
	public final static String CLOSE_CONNECTION = "closeConnection";

	/**
	 * 收到数据包
	 */
	public final static String RECEIVABLE_PACKET = "receivablePacket";

	/**
	 * 收到协议心跳数据包
	 */
	public final static String RECEIVABLE_KEEP_ALIVE = "receivableKeepAlive";

	/**
	 * 收到协议应答ACK数据包
	 */
	public final static String RECEIVABLE_ACK = "receivableAck";

	/**
	 * 发出协议应答ACK数据包
	 */
	public final static String SENDABLE_ACK = "sendableAck";

	/**
	 * 协议握手完成
	 */
	public final static String HANDSHAKE_COMPLETE = "handshakeComplete";
	/**
	 * 从UDP通道中收到包体数据
	 */
	public final static String RECEIVABLE_BYTE_BUFFER_UDP = "receivableByteBufferUdp";

	/**
	 * 数据发送后ACK应答超时
	 */
	public final static String SENDABLE_PACKET_ACK_TIME_OUT = "sendablePacketAckTimeOut";

	// /**
	// * ByteBuffer
	// */
	// private ByteBuffer byteBuffer;
	//
	// /**
	// * ByteBuffer
	// * @return byteBuffer
	// */
	// public ByteBuffer getByteBuffer() {
	// return byteBuffer;
	// }
	//
	// /**
	// * Socket数据包
	// */
	// private SocketReceivablePacket socketReceivablePacket;
	//
	// /**
	// * Socket数据包
	// * @return socketReceivablePacket Socket数据包
	// */
	// public SocketReceivablePacket getSocketReceivablePacket() {
	// return socketReceivablePacket;
	// }

	public SocketConnectionEvent(String type)
	{
		super(type);
	}

	public SocketConnectionEvent(String type, Object data)
	{
		super(type, data);
	}

	// public SocketConnectionEvent(String type, ByteBuffer byteBuffer)
	// {
	// super(type);
	// this.byteBuffer=byteBuffer;
	// }
	//
	// public SocketConnectionEvent(String type, SocketReceivablePacket socketReceivablePacket)
	// {
	// super(type);
	// this.socketReceivablePacket=socketReceivablePacket;
	// }
}
