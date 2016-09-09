package base.net.socket.packet;

import java.nio.ByteBuffer;
import java.util.Date;

import base.types.SocketType;

public interface ISocketReceivablePacket
{
	/**
	 * 是否自动释放
	 * @return
	 */
	public boolean getIsAutoDispose();
	/**
	 * 是否自动释放
	 * @return
	 */
	public void setIsAutoDispose(boolean isAutoDispose);
	/**
	 * 是否自动释放
	 * @return
	 */
	public Object getTransmitData();
	/**
	 * 是否自动释放
	 * @return
	 */
	public void setTransmitData(Object transmitData);
	
	/**
	 * 接收数据包
	 * @param SocketType 数据包Socket传输类型
	 * @param byteBuffer 数据源
	 * @param packetCode 数据包协议号
	 */
	public void init(SocketType socketType, ByteBuffer byteBuffer, short packetCode);

	/**
	 * 接收数据包
	 * @param SocketType 数据包Socket传输类型
	 * @param byteBuffer 数据源
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public void init(SocketType socketType, ByteBuffer byteBuffer, short packetCode, long commandID);

	/**
	 * 接收数据包
	 * @param SocketType 数据包Socket传输类型
	 * @param socketDataStructureType Socket数据包结构标识类型
	 * @param byteBuffer 数据源
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public void init(SocketType socketType, byte socketDataStructureType, ByteBuffer byteBuffer, short packetCode, long commandID);

	/**
	 * 取得byte类型数据
	 * @return byte类型数据
	 */
	public byte getByte();

	/**
	 * 取得当前位置到限制之间的所有字节
	 * @return byte[]类型数据
	 */
	public byte[] getBytes();

	/**
	 * 取得byte[]类型数据
	 * @return byte[]类型数据
	 */
	public byte[] getBytes(int length);

	/**
	 * 取得boolean类型数据
	 * @return boolean类型数据
	 */
	public boolean getBoolean();

	/**
	 * 取得short类型数据
	 * @return short类型数据
	 */
	public short getShort();

	/**
	 * 取得char类型数据
	 * @return char类型数据
	 */
	public char getChar();

	/**
	 * 取得int类型数据
	 * @return int类型数据
	 */
	public int getInt();

	/**
	 * 取得long类型数据
	 * @return long类型数据
	 */
	public long getLong();

	/**
	 * 取得float类型数据
	 * @return float类型数据
	 */
	public float getFloat();

	/**
	 * 取得double类型数据
	 * @return double类型数据
	 */
	public double getDouble();

	/**
	 * 取得Date类型数据
	 * @return Date类型数据
	 */
	public Date getDate();

	/**
	 * 取得String类型数据
	 * @return String类型数据
	 */
	public String getString();

	/**
	 * 取得String类型数据
	 * @return String类型数据
	 */
	public String getString(int length);
	
	/**
	 * 如果之前有设定标记(通过方法markPosition指定)，应用其位置标记至data，使data当前位置为标记位置
	 */
	public void applyMarkPosition();
	
	/**
	 * 指定位置为标记
	 * @param position 位置
	 */
	public void markPosition(int position);
	
	/**
	 * 将数据当前位置为标记
	 */
	public void markPosition();
	
	/**
	 * 包数据
	 * @return data 包数据
	 */
	public ByteBuffer getData();
	
	/**
	 * 数据包协议编号
	 * @return packetCode
	 */
	public short getPacketCode();
	
	/**
	 * 命令ID标识
	 * @return connectionID
	 */
	public long getCommandID();

	public void dispose();
}