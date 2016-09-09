package base.net.socket.packet;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Date;

import base.types.SocketType;

public interface ISocketSendablePacketBase
{
	public int getPacketBodyLength();
	
	/**
	 * 包数据的总byte[]长度
	 */
	public void setDataBytesLength(int dataBytesLength);

	/**
	 * 发送数据包
	 * @param packetType 发送包类型:0=定长包容量(用与服务器发向客户端的包);1=不限包容量(用与服务器之间的包)
	 * @param socketType 数据包类型
	 * @param socketDataStructureType Socket数据包结构标识类型
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public void init(int packetType, SocketType socketType, byte socketDataStructureType, short packetCode, long commandID);
	
	/**
	 * 写入ByteBuffer
	 * @param byteBuffer ByteBuffer
	 */
	public void putByteBuffer(ByteBuffer byteBuffer);
	
	/**
	 * 写入ByteBuffer
	 * @param byteBuffer 源ByteBuffer
	 * @param isResetPosition 源ByteBuffer读取后，是否复位至读取前位置，即读取前的“byteBuffer.position()”
	 */
	public void putByteBuffer(ByteBuffer byteBuffer, boolean isResetPosition);

	/**
	 * 写入Byte
	 * @param value 值
	 */
	public void put(byte value);

	/**
	 * 写入Byte
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void put(int index, byte value);

	/**
	 * 写入Byte[]
	 * @param value 值
	 */
	public void put(byte[] value);

	/**
	 * 写入Byte[]
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void put(int index, byte[] value);

	/**
	 * 写入boolean
	 * @param value 值
	 */
	public void putBoolean(boolean value);

	/**
	 * 写入boolean
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void putBoolean(int index, boolean value);

	/**
	 * 写入Short
	 * @param value 值
	 */
	public void putShort(short value);

	/**
	 * 写入Short
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void putShort(int index, short value);

	/**
	 * 写入Int
	 * @param value 值
	 */
	public void putInt(int value);

	/**
	 * 写入Int
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void putInt(int index, int value);

	/**
	 * 写入Long
	 * @param value 值
	 */
	public void putLong(long value);

	/**
	 * 写入Long
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void putLong(int index, long value);

	/**
	 * 写入Float
	 * @param value 值
	 */
	public void putFloat(float value);

	/**
	 * 写入Float
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void putFloat(int index, float value);

	/**
	 * 写入Double
	 * @param value 值
	 */
	public void putDouble(double value);

	/**
	 * 写入Double
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void putDouble(int index, double value);

	/**
	 * 写入Date
	 * @param value 值
	 */
	public void putDate(Date value);

	/**
	 * 写入Date
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void putDate(int index, Date value);

	/**
	 * 写入String
	 * @param value 值
	 */
	public void putString(String value);

	/**
	 * 结束包体
	 * @param isCrypto 是否加密
	 */
	public void end(boolean isCrypto);
	
	/**
	 * 结束包体
	 * @param isCrypto 是否加密
	 * @param packetNumber 包编号(只有TCP包才需要设置)
	 */
	public void end(boolean isCrypto, int packetNumber);
	
	/**
	 * 如果之前有设定标记(通过方法markPosition指定)，应用其位置标记至data，使data当前位置为标记位置
	 */
	public void applyMarkPosition();
	
	/**
	 * 指定位置为标记(注：指定了位置标记后，需要在外部手动进行数据清除)
	 * @param position 位置
	 */
	public void markPosition(int position);
	
	/**
	 * 将数据当前位置为标记(注：指定了位置标记后，需要在外部手动进行数据清除)
	 */
	public void markPosition();
	
	/**
	 * 取得数据当前位置为标记
	 */
	public int getMarkPosition();
	
	/**
	 * 包数据
	 * @return data 包数据
	 */
	public ByteBuffer getData();
	
	/**
	 * 包数据
	 * @return data 包数据
	 */
	public void setData(ByteBuffer data);
	
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
	
	/**
	 * Socket数据包结构标识类型
	 * @return socketDataStructureType 数据包结构标识类型，见SocketDataStructureType.xxx
	 */
	public byte getSocketDataStructureType();
	
	/**
	 * @param socketAddress 要设置的 socketAddress
	 */
	public void setSocketAddress(SocketAddress socketAddress);

	/**
	 * SocketAddress
	 * @return socketAddress
	 */
	public SocketAddress getSocketAddress();
	
	/**
	 * 数据包类型
	 * @return socketType
	 */
	public SocketType getSocketType();
	
	/**
	 * 数据包类型
	 */
	public void setSocketType(SocketType socketType);
	
	/**
	 * 复制新数据包
	 */
	public ISocketSendablePacketBase duplicate();

	public void dispose();
}