package base.net.socket.packet;

import java.nio.ByteBuffer;
import java.util.Date;

import base.types.SocketType;
import base.utils.ByteUtil;

public class SocketSendablePacketServer extends SocketSendablePacketBase
{
	/**
	 * 发送数据包
	 */
	public SocketSendablePacketServer()
	{
	}

	/**
	 * 发送数据包
	 * @param socketType 数据包类型
	 * @param socketDataStructureType Socket数据包结构标识类型
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public SocketSendablePacketServer(SocketType socketType, byte socketDataStructureType, short packetCode, long commandID)
	{
		init(1, socketType, socketDataStructureType, packetCode, commandID);
	}

	/**
	 * 结束包体
	 */
	public void end(boolean isCrypto)
	{
		super.end(isCrypto);
	}

	/**
	 * 写入ByteBuffer
	 * @param byteBuffer ByteBuffer
	 */
	public final void putByteBuffer(ByteBuffer byteBuffer)
	{
		putByteBuffer(byteBuffer, false);
	}
	
	/**
	 * 写入ByteBuffer
	 * @param byteBuffer 源ByteBuffer
	 * @param isResetPosition 源ByteBuffer读取后，是否复位至读取前位置，即读取前的“byteBuffer.position()”
	 */
	public final void putByteBuffer(ByteBuffer byteBuffer, boolean isResetPosition)
	{
		int remaining = byteBuffer.remaining();
		byte[] values = new byte[remaining];
		int p = 0;
		if(isResetPosition)
		{//如果复位
			p = byteBuffer.position();//记录下读取前时的位置
		}
		byteBuffer.get(values);
		if(isResetPosition)
		{//如果复位
			byteBuffer.position(p);//复位至读取前位置
		}
		put(values);
	}

	/**
	 * 写入Byte
	 * @param value 值
	 */
	public final void put(byte value)
	{
		byte[] values = new byte[1];
		values[0] = value;
		put(values);
	}

	/**
	 * 写入Byte
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void put(int index, byte value)
	{
		byte[] values = new byte[1];
		values[0] = value;
		dataBytes.set(index, values);
	}

	/**
	 * 写入Byte[]
	 * @param value 值
	 */
	public final void put(byte[] value)
	{
		dataBytes.add(value);
		dataBytesLength += value.length;
	}

	/**
	 * 写入Byte[]
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void put(int index, byte[] value)
	{
		dataBytes.set(index, value);
	}

	/**
	 * 写入boolean
	 * @param value 值
	 */
	public final void putBoolean(boolean value)
	{
		byte[] values = new byte[1];
		values[0] = (byte) (value == true ? 1 : 0);
		put(values);
	}

	/**
	 * 写入boolean
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putBoolean(int index, boolean value)
	{
		byte[] values = new byte[1];
		values[0] = (byte) (value == true ? 1 : 0);
		dataBytes.set(index, values);
	}

	/**
	 * 写入Short
	 * @param value 值
	 */
	public final void putShort(short value)
	{
		put(ByteUtil.getBytes(value));
	}

	/**
	 * 写入Short
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putShort(int index, short value)
	{
		byte[] values = ByteUtil.getBytes(value);
		dataBytes.set(index, values);
	}

	/**
	 * 写入Int
	 * @param value 值
	 */
	public final void putInt(int value)
	{
		put(ByteUtil.getBytes(value));
	}

	/**
	 * 写入Int
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putInt(int index, int value)
	{
		byte[] values = ByteUtil.getBytes(value);
		dataBytes.set(index, values);
	}

	/**
	 * 写入Long
	 * @param value 值
	 */
	public final void putLong(long value)
	{
		put(ByteUtil.getBytes(value));
	}

	/**
	 * 写入Long
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putLong(int index, long value)
	{
		byte[] values = ByteUtil.getBytes(value);
		dataBytes.set(index, values);
	}

	/**
	 * 写入Float
	 * @param value 值
	 */
	public final void putFloat(float value)
	{
		put(ByteUtil.getBytes(value));
	}

	/**
	 * 写入Float
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putFloat(int index, float value)
	{
		byte[] values = ByteUtil.getBytes(value);
		dataBytes.set(index, values);
	}

	/**
	 * 写入Double
	 * @param value 值
	 */
	public final void putDouble(double value)
	{
		put(ByteUtil.getBytes(value));
	}

	/**
	 * 写入Double
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putDouble(int index, double value)
	{
		byte[] values = ByteUtil.getBytes(value);
		dataBytes.set(index, values);
	}

	/**
	 * 写入Date
	 * @param value 值
	 */
	public final void putDate(Date value)
	{
		put(ByteUtil.getBytes(value.getTime()));
	}

	/**
	 * 写入Date
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putDate(int index, Date value)
	{
		byte[] values = ByteUtil.getBytes(value.getTime());
		dataBytes.set(index, values);
	}

	/**
	 * 写入String
	 * @param value 值
	 */
	public final void putString(String value)
	{
		if (value == null)
			value = "";
		final byte[] values = ByteUtil.getBytes(value);
		putShort((short) values.length);
		put(values);
	}

	/**
	 * 写入String
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putString(int index, String value)
	{
		if (value == null)
			value = "";
		final byte[] values = ByteUtil.getBytes(value);
		putShort(index, (short) values.length);
		dataBytes.set(index + 1, values);
	}

	/**
	 * 在指定的Vector索引位置插入数据
	 * @param index 指定的Vector索引位置
	 * @param value 插入的数据
	 */
	public <T> void insert(int index, T value)
	{
		if (dataBytes != null && dataBytes.size() > index)
		{
			byte[] bytesValue = null;
			String simpleName = value.getClass().getSimpleName().toLowerCase();
			switch (simpleName)
			{
				case "string":
					bytesValue = ByteUtil.getBytes(String.valueOf(value));
					break;
				case "boolean":
					bytesValue = ByteUtil.getBytes((boolean) value);
					break;
				case "byte":
					bytesValue = ByteUtil.getBytes((byte) value);
					break;
				case "short":
					bytesValue = ByteUtil.getBytes((short) value);
					break;
				case "integer":
					bytesValue = ByteUtil.getBytes((int) value);
					break;
				case "long":
					bytesValue = ByteUtil.getBytes((long) value);
					break;
				case "float":
					bytesValue = ByteUtil.getBytes((float) value);
					break;
				case "double":
					bytesValue = ByteUtil.getBytes((double) value);
					break;
				case "date":
					bytesValue = ByteUtil.getBytes((java.util.Date) value);
					break;
			}
			dataBytes.insertElementAt(bytesValue, index);
			dataBytesLength += bytesValue.length;
		}
	}

	public synchronized void dispose()
	{
		super.dispose();
		if (dataBytes != null)
		{
			dataBytes.clear();
			dataBytes = null;
		}
	}
}
