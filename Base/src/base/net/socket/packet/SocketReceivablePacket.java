package base.net.socket.packet;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.types.SocketDataStructureType;
import base.types.SocketType;
import base.util.ByteUtil;

/**
 * 接收数据包
 * @author Devin
 *
 */
public class SocketReceivablePacket extends SocketPacketBase implements ISocketReceivablePacket
{
	/**
	 * 是否自动释放
	 * @return
	 */
	protected boolean isAutoDispose=true;
	/**
	 * 是否自动释放
	 * @return
	 */
	public boolean getIsAutoDispose()
	{
		return isAutoDispose;
	}
	/**
	 * 是否自动释放
	 * @return
	 */
	public void setIsAutoDispose(boolean isAutoDispose)
	{
		this.isAutoDispose=isAutoDispose;
	}
	
	/**
	 * 传递数据(不为包中的协议数据)
	 * @return
	 */
	protected Object transmitData=null;
	/**
	 * 是否自动释放
	 * @return
	 */
	public Object getTransmitData()
	{
		return transmitData;
	}
	/**
	 * 是否自动释放
	 * @return
	 */
	public void setTransmitData(Object transmitData)
	{
		this.transmitData=transmitData;
	}
	
	/**
	 * 接收数据包
	 */
	public SocketReceivablePacket()
	{
	}
	
	/**
	 * 接收数据包
	 * @param SocketType 数据包Socket传输类型
	 * @param byteBuffer 数据源
	 * @param packetCode 数据包协议号
	 */
	public void init(SocketType socketType, ByteBuffer byteBuffer, short packetCode)
	{
		init(socketType, SocketDataStructureType.DATA, packetCode, 0);
		data = byteBuffer;
	}

	/**
	 * 接收数据包
	 * @param SocketType 数据包Socket传输类型
	 * @param byteBuffer 数据源
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public void init(SocketType socketType, ByteBuffer byteBuffer, short packetCode, long commandID)
	{
		init(socketType, SocketDataStructureType.DATA, packetCode, commandID);
		data = byteBuffer;
	}

	/**
	 * 接收数据包
	 * @param SocketType 数据包Socket传输类型
	 * @param socketDataStructureType Socket数据包结构标识类型
	 * @param byteBuffer 数据源
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public void init(SocketType socketType, byte socketDataStructureType, ByteBuffer byteBuffer, short packetCode, long commandID)
	{
		init(socketType, socketDataStructureType, packetCode, commandID);
		data = byteBuffer;
	}

	/**
	 * 取得byte类型数据
	 * @return byte类型数据
	 */
	public final byte getByte()
	{
		return data.get();
	}

	/**
	 * 取得当前位置到限制之间的所有字节
	 * @return byte[]类型数据
	 */
	public final byte[] getBytes()
	{
		return getBytes(data.remaining());
	}

	/**
	 * 取得byte[]类型数据
	 * @return byte[]类型数据
	 */
	public final byte[] getBytes(int length)
	{
		byte[] dst = new byte[length];
		data.get(dst, 0, length);
		return dst;
	}

	/**
	 * 取得boolean类型数据
	 * @return boolean类型数据
	 */
	public final boolean getBoolean()
	{
		return data.get() == 1;
	}

	/**
	 * 取得short类型数据
	 * @return short类型数据
	 */
	public final short getShort()
	{
		return data.getShort();
	}

	/**
	 * 取得char类型数据
	 * @return char类型数据
	 */
	public final char getChar()
	{
		return data.getChar();
	}

	/**
	 * 取得int类型数据
	 * @return int类型数据
	 */
	public final int getInt()
	{
		return data.getInt();
	}

	/**
	 * 取得long类型数据
	 * @return long类型数据
	 */
	public final long getLong()
	{
		return data.getLong();
	}

	/**
	 * 取得float类型数据
	 * @return float类型数据
	 */
	public final float getFloat()
	{
		return data.getFloat();
	}

	/**
	 * 取得double类型数据
	 * @return double类型数据
	 */
	public final double getDouble()
	{
		return data.getDouble();
	}

	/**
	 * 取得Date类型数据
	 * @return Date类型数据
	 */
	public final Date getDate()
	{
		return new Date(data.getLong());
	}

	/**
	 * 取得String类型数据
	 * @return String类型数据
	 */
	public final String getString()
	{
		int len = getShort();
		String str = getString(len);
		return str;
	}

	/**
	 * 取得String类型数据
	 * @return String类型数据
	 */
	public final String getString(int length)
	{
		String str = null;
		try
		{
			byte[] bytes = new byte[length];
			data.get(bytes, 0, length);
			str = ByteUtil.getString(bytes);
			bytes = null;
		}
		catch (Exception e)
		{
			Logger.getLogger(ISocketReceivablePacket.class.getName()).log(Level.SEVERE, null, e);
		}
		return str;
	}

	public synchronized void dispose()
	{
		transmitData=null;
		super.dispose();
	}
}
