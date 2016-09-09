package base.net.socket.packet;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import base.types.SocketType;

/**
 * 数据包基类
 * @author Devin
 *
 */
public abstract class SocketPacketBase
{
	/**
	 * 数据包类型
	 */
	protected SocketType socketType;

	/**
	 * 数据包类型
	 * @return socketType
	 */
	public SocketType getSocketType()
	{
		return socketType;
	}
	
	/**
	 * 数据包类型
	 */
	public void setSocketType(SocketType socketType)
	{
		this.socketType=socketType;
	}

	/**
	 * Socket数据包结构标识类型，见SocketDataStructureType.xxx
	 */
	protected byte socketDataStructureType;

	/**
	 * Socket数据包结构标识类型
	 * @return socketDataStructureType 数据包结构标识类型，见SocketDataStructureType.xxx
	 */
	public byte getSocketDataStructureType()
	{
		return socketDataStructureType;
	}

	/**
	 * 命令ID标识
	 */
	protected long commandID;

	/**
	 * 命令ID标识
	 * @return connectionID
	 */
	public long getCommandID()
	{
		return commandID;
	}

	/**
	 * SocketAddress
	 */
	protected SocketAddress socketAddress;

	/**
	 * @param socketAddress 要设置的 socketAddress
	 */
	public void setSocketAddress(SocketAddress socketAddress)
	{
		this.socketAddress = socketAddress;
	}

	/**
	 * SocketAddress
	 * @return socketAddress
	 */
	public SocketAddress getSocketAddress()
	{
		return socketAddress;
	}

	/**
	 * 数据包协议编号
	 */
	protected short packetCode = 0;

	/**
	 * 数据包协议编号
	 * @return packetCode
	 */
	public final short getPacketCode()
	{
		return packetCode;
	}

	/**
	 * 包数据
	 */
	protected ByteBuffer data = null;

	/**
	 * 包数据
	 * @return data 包数据
	 */
	public final ByteBuffer getData()
	{
		return data;
	}
	
	/**
	 * 包数据
	 * @return data 包数据
	 */
	public final void setData(ByteBuffer data)
	{
		this.data=data;
	}
	
	/**
	 * 将数据当前位置为标记(注：指定了位置标记后，需要在外部手动进行数据清除)
	 */
	protected int markPosition = -1;
	
	/**
	 * 将数据当前位置为标记(注：指定了位置标记后，需要在外部手动进行数据清除)
	 */
	public void markPosition()
	{
		markPosition = data.position();
	}
	
	/**
	 * 指定位置为标记(注：指定了位置标记后，需要在外部手动进行数据清除)
	 * @param position 位置
	 */
	public void markPosition(int position)
	{
		markPosition = position;
	}
	
	/**
	 * 数据当前位置为标记
	 */
	public int getMarkPosition()
	{
		return this.markPosition;
	}

	/**
	 * 数据包基类
	 */
	public SocketPacketBase()
	{
	}
	
	/**
	 * 数据包基类
	 * @param socketType 数据包类型
	 * @param socketDataStructureType Socket数据包结构标识类型
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public void init(SocketType socketType, byte socketDataStructureType, short packetCode, long commandID)
	{
		this.socketType = socketType;
		this.socketDataStructureType = socketDataStructureType;
		this.packetCode = packetCode;
		this.commandID = commandID;
	}
	
	/**
	 * 如果之前有设定标记(通过方法markPosition指定)，应用其位置标记至data，使data当前位置为标记位置
	 */
	public void applyMarkPosition()
	{
		if(markPosition>-1)
		{
			data.position(markPosition);
		}
	}

	public synchronized void dispose()
	{
		if (data != null)
		{
			data.clear();
			data = null;
		}
	}
}
