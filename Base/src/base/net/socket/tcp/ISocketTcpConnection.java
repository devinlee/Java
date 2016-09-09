package base.net.socket.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import base.event.IEventListener;

public interface ISocketTcpConnection
{
	/**
	 * Socket TCP通道
	 */
	public SocketChannel getSocketChannel();

	/**
	 * 当前连接对象是否可用
	 * @return
	 */
	public boolean getIsAvailable();

	/**
	 * Socket连接
	 * @param socketChannel SocketChannel
	 * @param byteBufferCapacity 字节缓冲大小
	 * @throws IOException
	 */
	public void init(SocketChannel socketChannel, int byteBufferCapacity);

	/**
	 * 发送Tcp数据
	 * @param byteBuffer 数据
	 * @return 成功发送的字节数
	 */
	public int send(ByteBuffer byteBuffer);
	
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