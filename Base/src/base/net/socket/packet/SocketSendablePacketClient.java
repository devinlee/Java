package base.net.socket.packet;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Vector;

import base.net.NetFactory;
import base.net.socket.SocketUtil;
import base.types.SocketType;
import base.utils.ByteUtil;

public class SocketSendablePacketClient extends SocketSendablePacketBase
{
	/**
	 * 未改变数据储存方式的原包数据
	 */
	private ByteBuffer oldData = null;

	/**
	 * 发送数据包，为TCP包类型
	 * @param packetCode 数据包协议号
	 */
	public SocketSendablePacketClient()
	{
	}

	/**
	 * 发送数据包
	 * @param socketType 数据包类型
	 * @param socketDataStructureType Socket数据包结构标识类型
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public void init(SocketType socketType, byte socketDataStructureType, short packetCode, long commandID)
	{
		init(0, socketType, socketDataStructureType, packetCode, commandID);
	}

	/**
	 * 检查当前包数据是否大于包容量，如果大，则进行数据存储方式改变
	 * @return
	 */
	private boolean checkPacketCapacity(int addLength)
	{
		if(packetType!=1 && dataBytesLength+addLength>data.capacity())
		{//如果插入的数据大于容量，则更改数据容量方式
			packetType=1;
			dataBytes=new Vector<byte[]>();
			//进行数据转移
			byte[] dst = new byte[dataBytesLength];
			data.position(0);
			data.get(dst, 0, dataBytesLength);
			dataBytes.add(dst);
			oldData=data;
			return true;
		}
		return false;
	}

	/**
	 * 写入ByteBuffer
	 * @param byteBuffer 源ByteBuffer
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
		checkPacketCapacity(remaining);
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
		if(packetType==1)
		{
			dataBytes.add(values);
		}
		else
		{
			data.put(values);
		}
		dataBytesLength += remaining;
	}

	/**
	 * 写入Byte
	 * @param value 值
	 */
	public final void put(byte value)
	{
		checkPacketCapacity(1);
		if(packetType==1)
		{
			byte[] values = new byte[1];
			values[0] = value;
			dataBytes.add(values);
		}
		else
		{
			data.put(value);
		}
		dataBytesLength += 1;
	}

	/**
	 * 写入Byte
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void put(int index, byte value)
	{
		if(packetType==1)
		{
			byte[] values = new byte[1];
			values[0] = value;
			dataBytes.set(index, values);
		}
		else
		{
			data.put(index, value);
		}
	}

	/**
	 * 写入Byte[]
	 * @param value 值
	 */
	public final void put(byte[] value)
	{
		checkPacketCapacity(value.length);
		if(packetType==1)
		{
			dataBytes.add(value);
		}
		else
		{
			data.put(value);
		}
		dataBytesLength += value.length;
	}

	/**
	 * 写入boolean
	 * @param value 值
	 */
	public final void putBoolean(boolean value)
	{
		checkPacketCapacity(1);
		if(packetType==1)
		{
			byte[] values = new byte[1];
			values[0] = (byte) (value == true ? 1 : 0);
			dataBytes.add(values);
		}
		else
		{
			data.put((byte) (value == true ? 1 : 0));
		}
		dataBytesLength += 1;
	}

	/**
	 * 写入boolean
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putBoolean(int index, boolean value)
	{
		if(packetType==1)
		{
			byte[] values = new byte[1];
			values[0] = (byte) (value == true ? 1 : 0);
			dataBytes.set(index, values);
		}
		else
		{
			data.put(index, (byte) (value == true ? 1 : 0));
		}
	}

	/**
	 * 写入Short
	 * @param value 值
	 */
	public final void putShort(short value)
	{
		checkPacketCapacity(2);
		if(packetType==1)
		{
			dataBytes.add(ByteUtil.getBytes(value));
		}
		else
		{
			data.putShort(value);
		}
		dataBytesLength += 2;
	}

	/**
	 * 写入Short
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putShort(int index, short value)
	{
		if(packetType==1)
		{
			byte[] values = ByteUtil.getBytes(value);
			dataBytes.set(index, values);
		}
		else
		{
			data.putShort(index, value);
		}
	}

	/**
	 * 写入Int
	 * @param value 值
	 */
	public final void putInt(int value)
	{
		checkPacketCapacity(4);
		if(packetType==1)
		{
			dataBytes.add(ByteUtil.getBytes(value));
		}
		else
		{
			data.putInt(value);
		}
		dataBytesLength += 4;
	}

	/**
	 * 写入Int
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putInt(int index, int value)
	{
		if(packetType==1)
		{
			byte[] values = ByteUtil.getBytes(value);
			dataBytes.set(index, values);
		}
		else
		{
			data.putInt(index, value);
		}
	}

	/**
	 * 写入Long
	 * @param value 值
	 */
	public final void putLong(long value)
	{
		checkPacketCapacity(8);
		if(packetType==1)
		{
			dataBytes.add(ByteUtil.getBytes(value));
		}
		else
		{
			data.putLong(value);
		}
		dataBytesLength += 8;
	}

	/**
	 * 写入Long
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putLong(int index, long value)
	{
		if(packetType==1)
		{
			byte[] values = ByteUtil.getBytes(value);
			dataBytes.set(index, values);
		}
		else
		{
			data.putLong(index, value);
		}
	}

	/**
	 * 写入Float
	 * @param value 值
	 */
	public final void putFloat(float value)
	{
		checkPacketCapacity(4);
		if(packetType==1)
		{
			dataBytes.add(ByteUtil.getBytes(value));
		}
		else
		{
			data.putFloat(value);
		}
		dataBytesLength += 4;
	}

	/**
	 * 写入Float
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putFloat(int index, float value)
	{
		if(packetType==1)
		{
			byte[] values = ByteUtil.getBytes(value);
			dataBytes.set(index, values);
		}
		else
		{
			data.putFloat(index, value);
		}
	}

	/**
	 * 写入Double
	 * @param value 值
	 */
	public final void putDouble(double value)
	{
		checkPacketCapacity(8);
		if(packetType==1)
		{
			dataBytes.add(ByteUtil.getBytes(value));
		}
		else
		{
			data.putDouble(value);
		}
		dataBytesLength += 8;
	}

	/**
	 * 写入Double
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putDouble(int index, double value)
	{
		if(packetType==1)
		{
			byte[] values = ByteUtil.getBytes(value);
			dataBytes.set(index, values);
		}
		else
		{
			data.putDouble(index, value);
		}
	}

	/**
	 * 写入Date
	 * @param value 值
	 */
	public final void putDate(Date value)
	{
		checkPacketCapacity(8);
		if(packetType==1)
		{
			dataBytes.add(ByteUtil.getBytes(value.getTime()));
		}
		else
		{
			data.putLong(value.getTime());
		}
		dataBytesLength += 8;
	}

	/**
	 * 写入Date
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public final void putDate(int index, Date value)
	{
		if(packetType==1)
		{
			byte[] values = ByteUtil.getBytes(value.getTime());
			dataBytes.set(index, values);
		}
		else
		{
			data.putLong(index, value.getTime());
		}
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
		checkPacketCapacity(values.length);
		putShort((short) values.length);
		if(packetType==1)
		{
			dataBytes.add(values);
		}
		else
		{
			put(values);
		}
	}

	/**
	 * 结束包体
	 */
	public void end(boolean isCrypto)
	{
//		if(packetType==1)
//		{
//			data = ByteBuffer.allocate(dataBytesLength + packetEndLength);
//			data.order(SocketConfig.byteOrder);
//			for (byte[] bytes : dataBytes)
//			{
//				data.put(bytes);
//			}
//		}
		super.end(isCrypto);
	}

	/**
	 * 复制新数据包
	 */
	public ISocketSendablePacketBase duplicate()
	{
		ISocketSendablePacketBase socketSendablePacketBase = SocketUtil.socketSendablePacketClient(socketType, socketDataStructureType, packetCode, commandID);
		socketSendablePacketBase.getData().position(0);
		int i=0;
		while(i<data.position())
		{
			socketSendablePacketBase.getData().put(data.get(i));
			i++;
		}
		socketSendablePacketBase.getData().position(data.position());
		socketSendablePacketBase.getData().limit(data.limit());
		socketSendablePacketBase.setDataBytesLength(dataBytesLength);
		return socketSendablePacketBase;
	}

	public synchronized void dispose()
	{
		if(packetType==1)
		{
			NetFactory.socketController().releaseSendablePacketClientByteBuffer(oldData);
			if(data!=null)
			{
				data.clear();
				data=null;
			}
		}
		else
		{
			NetFactory.socketController().releaseSendablePacketClientByteBuffer(data);
			data=null;
		}
		if (dataBytes != null)
		{
			dataBytes.clear();
			dataBytes = null;
		}
	}
}
