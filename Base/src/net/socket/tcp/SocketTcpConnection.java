package base.net.socket.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.event.EventDispatcher;
import base.net.socket.events.SocketConnectionEvent;

public class SocketTcpConnection extends EventDispatcher implements ISocketTcpConnection
{
	/**
	 * Socket TCP通道
	 */
	private SocketChannel socketChannel;
	/**
	 * Socket TCP通道
	 */
	public SocketChannel getSocketChannel()
	{
		return socketChannel;
	}

	/**
	 * 当前连接对象是否可用
	 */
	private boolean isAvailable = true;

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
	 */
	public SocketTcpConnection()
	{
	}
	
	/**
	 * Socket连接
	 * @param socketChannel SocketChannel
	 * @param byteBufferCapacity 字节缓冲大小
	 */
	public void init(SocketChannel socketChannel, int byteBufferCapacity)
	{
		this.socketChannel = socketChannel;
	}

	/**
	 * 发送Tcp数据
	 * @param byteBuffer 数据
	 * @return 成功发送的字节数
	 */
	public synchronized int send(ByteBuffer byteBuffer)
	{
//		System.out.println("SocketTcpConnection.send 1");
		int result = 0;
		try
		{
			if (isAvailable && socketChannel != null)
			{
				while (isAvailable && byteBuffer.hasRemaining())
				{
					result = socketChannel.write(byteBuffer);
//					System.out.println("SocketTcpConnection.send result:"+result);
				}
			}
		}
		catch (NotYetConnectedException e)
		{
			close((byte)0);
			Logger.getLogger(ISocketTcpConnection.class.getName()).log(Level.SEVERE, "尚未连接此通道。", e);
		}
		catch (ClosedByInterruptException e)
		{
			close((byte)0);
			Logger.getLogger(ISocketTcpConnection.class.getName()).log(Level.SEVERE, "正在进行写入操作时另一个线程中断了当前线程，因此关闭了该通道并将当前线程的状态设置为中断。", e);
		}
		catch (AsynchronousCloseException e)
		{
			close((byte)0);
			Logger.getLogger(ISocketTcpConnection.class.getName()).log(Level.SEVERE, "正在进行写入操作时另一个线程关闭了此通道。", e);
		}
		catch (ClosedChannelException e)
		{
			close((byte)0);
			Logger.getLogger(ISocketTcpConnection.class.getName()).log(Level.SEVERE, "此通道已关闭。", e);
		}
		catch (IOException e)
		{
			close((byte)0);
			Logger.getLogger(ISocketTcpConnection.class.getName()).log(Level.SEVERE, "发送Socket Tcp数据时错误，可能socket连接已断开。", e);
		}
		catch (Exception e)
		{
			close((byte)0);
			Logger.getLogger(ISocketTcpConnection.class.getName()).log(Level.SEVERE, "发送Socket Tcp数据时错误，", e);
		}
		return result;
	}
	
	/**
	 * 关闭连接
	 * @param closeType 关闭类型：0=服务器内部主动断开关闭;1=外部强制关闭(比如手动离开一个玩家时，从玩家类调用关闭);2=客户端主动断开网络关闭
	 */
	private synchronized void close(byte closeType)
	{
		isAvailable = false;
		try
		{
			if (socketChannel != null)
			{
				socketChannel.close();
				socketChannel = null;
			}
			dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.CLOSE_CONNECTION, closeType));
		}
		catch (IOException e)
		{
			Logger.getLogger(ISocketTcpConnection.class.getName()).log(Level.SEVERE, "关闭Socket通道错误。", e);
		}
	}

	public synchronized void dispose()
	{
		isAvailable = false;
		try
		{
			if (socketChannel != null)
			{
				socketChannel.close();
				socketChannel = null;
			}
		}
		catch (IOException e)
		{
			Logger.getLogger(ISocketTcpConnection.class.getName()).log(Level.SEVERE, "关闭Socket通道错误。", e);
		}
	}
}
