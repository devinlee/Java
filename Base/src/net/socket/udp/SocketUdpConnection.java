package base.net.socket.udp;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.event.Event;
import base.event.EventDispatcher;
import base.event.IEventListener;
import base.net.socket.events.SocketConnectionEvent;
import base.types.SocketType;

public class SocketUdpConnection extends EventDispatcher implements IEventListener
{
	/**
	 * 通讯数据包加密密码
	 * @param cryptoKeys 要设置的通讯数据包加密密码
	 */
	public void setCryptoKeys(int[] cryptoKeys)
	{
		this.socketUdpSendable.setCryptoKeys(cryptoKeys);
		this.socketUdpReceivable.setCryptoKeys(cryptoKeys);
	}

	/**
	 * Socket UDP通道
	 */
	private DatagramChannel datagramChannel;

	/**
	 * Udp通道的SocketAddress
	 */
	private SocketAddress socketAddress;

	/**
	 * Udp通道的SocketAddress
	 * @return socketAddress
	 */
	public SocketAddress getSocketAddress()
	{
		return socketAddress;
	}

	/**
	 * Socket Udp发送
	 */
	private SocketUdpSendable socketUdpSendable;

	/**
	 * Socket Udp发送
	 * @return socketUdpSendable
	 */
	public SocketUdpSendable getSocketUdpSendable()
	{
		return socketUdpSendable;
	}

	/**
	 * Socket Udp接收
	 */
	private SocketUdpReceivable socketUdpReceivable;

	/**
	 * 当前连接对象是否可用
	 */
	private boolean isAvailable = false;

	/**
	 * 当前连接对象是否可用
	 * @return
	 */
	public boolean getIsAvailable()
	{
		return isAvailable;
	}

	/**
	 * Socket连接
	 * @param datagramChannel DatagramChannel
	 * @param socketAddress SocketAddress
	 * @param byteBufferCapacity 字节缓冲大小
	 * @param cryptoKeys 数据包体加密key
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 * @throws IOException
	 */
	public SocketUdpConnection(DatagramChannel datagramChannel, SocketAddress socketAddress, int byteBufferCapacity, int[] cryptoKeys, boolean isCrypto)
	{
		this.datagramChannel = datagramChannel;
		this.socketAddress = socketAddress;
		socketUdpSendable = new SocketUdpSendable(datagramChannel, socketAddress, (byte)10, byteBufferCapacity, cryptoKeys, isCrypto);
		socketUdpSendable.addEventListener(SocketConnectionEvent.CLOSE_CONNECTION, this);
		socketUdpReceivable = new SocketUdpReceivable(cryptoKeys, isCrypto);
		socketUdpReceivable.addEventListener(SocketConnectionEvent.RECEIVABLE_BYTE_BUFFER_UDP, this);
		socketUdpReceivable.addEventListener(SocketConnectionEvent.RECEIVABLE_KEEP_ALIVE, this);
		socketUdpReceivable.addEventListener(SocketConnectionEvent.RECEIVABLE_ACK, this);
		socketUdpReceivable.addEventListener(SocketConnectionEvent.SENDABLE_ACK, this);
		socketUdpReceivable.addEventListener(SocketConnectionEvent.CLOSE_CONNECTION, this);
		isAvailable = true;
	}

	/**
	 * 加入待发送Udp数据
	 * @param byteBuffer
	 * @param socketAddress SocketAddress
	 * @return 成功发送的字节数
	 */
	public void addSendData(byte socketDataStructureType, ByteBuffer byteBuffer)
	{
		if (isAvailable && datagramChannel != null)
		{
			socketUdpSendable.addByteBuffer(socketDataStructureType, byteBuffer);
		}
	}

	/**
	 * 发送Udp数据
	 */
	public void send()
	{
		socketUdpSendable.send();
	}

	/**
	 * 读取到Socket UDP通道中的数据
	 * @param reByteBuffer 读取到的数据
	 */
	public void receiveData(ByteBuffer reByteBuffer)
	{
		if (isAvailable && socketUdpReceivable!=null)
		{
			socketUdpReceivable.receive(reByteBuffer);
		}
	}

	@Override
	public void handleEvent(Event event)
	{
		switch (event.getType())
		{
			case SocketConnectionEvent.RECEIVABLE_BYTE_BUFFER_UDP:// 从UDP通道中收到数据
				dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.RECEIVABLE_BYTE_BUFFER_UDP, (ByteBuffer) event.getData()));
				break;
			case SocketConnectionEvent.RECEIVABLE_KEEP_ALIVE:// 心跳
				dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.RECEIVABLE_KEEP_ALIVE, SocketType.UDP));
				socketUdpSendable.ackKeepAliveSend();
				break;
			case SocketConnectionEvent.SENDABLE_ACK:
				socketUdpSendable.ackSend((int) event.getData());
				break;
			case SocketConnectionEvent.RECEIVABLE_ACK:
				socketUdpSendable.ackReceive((int) event.getData());
				break;
			case SocketConnectionEvent.CLOSE_CONNECTION:
				byte closeType = (byte)event.getData();
				close(closeType);
				break;
		}
	}

	/**
	 * 设置Udp通道
	 * @param datagramChannel DatagramChannel
	 * @param socketAddress Udp通道的socketAddress
	 */
	public void setSocketChannel(DatagramChannel datagramChannel, SocketAddress socketAddress)
	{
		this.datagramChannel = datagramChannel;
		this.socketAddress = socketAddress;
		socketUdpSendable.setDatagramChannel(datagramChannel);
		socketUdpSendable.setSocketAddress(socketAddress);
	}
	
	/**
	 * 设置连接信息
	 * @param id 标识ID
	 * @param connectionCheckCode 连接校验码
	 */
	public void setConnectionInfo(int id, int connectionCheckCode)
	{
		socketUdpSendable.setConnectionInfo(id, connectionCheckCode);
		socketUdpReceivable.setConnectionInfo(id, connectionCheckCode);
	}

	/**
	 * 关闭连接
	 * @param closeType 关闭类型：0=服务器内部主动断开关闭;1=外部强制关闭(比如手动离开一个玩家时，从玩家类调用关闭);2=客户端主动断开网络关闭
	 */
	private void close(byte closeType)
	{
		isAvailable = false;
		try
		{
			datagramChannel = null;
			dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.CLOSE_CONNECTION, closeType));
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpConnection.class.getName()).log(Level.SEVERE, "关闭Socket通道错误。", e);
		}
	}

	public void dispose()
	{
		try
		{
			if (socketUdpReceivable != null)
			{
				socketUdpReceivable.removeEventListener(SocketConnectionEvent.RECEIVABLE_BYTE_BUFFER_UDP, this);
				socketUdpReceivable.removeEventListener(SocketConnectionEvent.RECEIVABLE_KEEP_ALIVE, this);
				socketUdpReceivable.removeEventListener(SocketConnectionEvent.RECEIVABLE_ACK, this);
				socketUdpReceivable.removeEventListener(SocketConnectionEvent.SENDABLE_ACK, this);
				socketUdpReceivable.removeEventListener(SocketConnectionEvent.CLOSE_CONNECTION, this);
				socketUdpReceivable.dispose();
				socketUdpReceivable = null;
			}
			if (socketUdpSendable != null)
			{
				socketUdpSendable.removeEventListener(SocketConnectionEvent.CLOSE_CONNECTION, this);
				socketUdpSendable.dispose();
				socketUdpSendable = null;
			}
			datagramChannel = null;
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpConnection.class.getName()).log(Level.SEVERE, "关闭Socket通道错误。", e);
		}
	}
}
