package base.net.socket.packet;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Vector;

import base.net.NetFactory;
import base.net.socket.SocketConfig;
import base.types.SocketDataStructureType;
import base.types.SocketType;

public class SocketSendablePacketBase extends SocketPacketBase implements ISocketSendablePacketBase
{
	/**
	 * 发送数据包类型:0=定长包容量(用与服务器发向客户端的包);1=不限包容量(用与服务器之间的包)
	 */
	protected int packetType = 0;

	/**
	 * 数据包头长度
	 */
	protected int packetHeadLength = 0;

	/**
	 * 数据包尾长度
	 */
	protected int packetEndLength = 0;

	/**
	 * 包数据的总byte[]长度
	 */
	protected int dataBytesLength = 0;

	/**
	 * 包数据的总byte[]长度
	 */
	public void setDataBytesLength(int dataBytesLength)
	{
		this.dataBytesLength=dataBytesLength;
	}

	/**
	 * 包体数据的长度
	 */
	protected int packetBodyLength = 0;

	public int getPacketBodyLength()
	{
		return packetBodyLength; 
	}

	/**
	 * 包数据的Vector格式
	 */
	protected Vector<byte[]> dataBytes;

	/**
	 * 发送数据包
	 */
	public SocketSendablePacketBase()
	{

	}

	/**
	 * 初始化
	 * @param packetType 发送包类型:0=定长包容量(用与服务器发向客户端的包);1=不限包容量(用与服务器之间的包)
	 * @param socketType 数据包类型
	 * @param socketDataStructureType Socket数据包结构标识类型
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public void init(int packetType, SocketType socketType, byte socketDataStructureType, short packetCode, long commandID)
	{
		super.init(socketType, socketDataStructureType, packetCode, commandID);
		this.packetType = packetType;
		if (packetType == 0)
		{// 为用与服务器发向客户端的包类型时，需先创建data的值
			data = NetFactory.socketController().getSendablePacketClientByteBuffer();
		}
		else
		{
			dataBytes = new Vector<byte[]>();
		}

		packetHeadLength = SocketConfig.PACKET_STRUCTURE_TYPE_LENGTH;// 包头容量＝已计算容量大小+Socket数据包结构标识类型长度
		switch (socketDataStructureType)
		{
			case SocketDataStructureType.DATA:
				if (packetType == 0)
				{
					//					data.put((byte)0);//写入Socket数据包结构标识类型占位
					//					data.putInt(0);// 写入数据包序号占位
					//					data.putInt(0);// 先写入一个包体长度占位，包体结束后会替换为包真实长度，此长度仅为包体长度，不包括包头及包尾部分
					//					data.putShort((short)0);// 写入数据包协议号占位
					//					data.putLong(0);//写入命令ID标识占位
					data.position(SocketConfig.PACKET_STRUCTURE_TYPE_LENGTH + SocketConfig.PACKET_NUMBER_LENGTH + SocketConfig.PACKET_BODY_LENGTH + SocketConfig.PACKET_PROTOCOL_NUMBE_LENGTH + SocketConfig.COMMAND_LENGTH);
				}
				//				if (socketType == SocketType.TCP)
				//				{
				//					packetHeadLength += SocketConfig.PACKET_NUMBER_LENGTH;// 包头容量＝已计算容量大小+数据包序号长度
				//					packetEndLength = SocketConfig.PACKET_END_LENGTH;
				//					putInt(0);// 写入数据包序号占位
				//				}
				//				packetHeadLength += SocketConfig.PACKET_BODY_LENGTH;// 包头容量＝已计算容量大小+数据包体长度
				//				putInt(0);// 先写入一个包体长度占位，包体结束后会替换为包真实长度，此长度仅为包体长度，不包括包头及包尾部分
				//				putShort(packetCode);// 写入数据包协议号
				//				if (commandID >= 0)
				//					putLong(commandID);// 写入命令ID标识(如果值大于等于0)
				break;
			case SocketDataStructureType.HANDSHAKE_TCP:
			case SocketDataStructureType.HANDSHAKE_TCP_ACK:
				put(socketDataStructureType);// 写入Socket数据包结构标识类型
				packetHeadLength = packetHeadLength + SocketConfig.PACKET_BODY_LENGTH;// 包头容量＝已计算容量大小+数据包体长度
				putInt(0);// 先写入一个包体长度占位，包体结束后会替换为包真实长度，此长度仅为包体长度，不包括包头及包尾部分
				break;
			case SocketDataStructureType.HANDSHAKE_UDP:
				put(socketDataStructureType);// 写入Socket数据包结构标识类型
				packetHeadLength = packetHeadLength + SocketConfig.CONNECTION_ID_LENGTH + SocketConfig.CONNECTION_CHECK_CODE_LENGTH;// 包头容量＝已计算容量大小+连接ID长度+连接校验码长度
				packetEndLength = SocketConfig.PACKET_END_LENGTH;
				putInt(0);// 写入连接ID占位
				putInt(0);// 写入连接校验码占位
				break;
			case SocketDataStructureType.ACK://发往发送端的ACK(说明当前包是由接收端(服务器端)发送的)
				put(socketDataStructureType);// 写入Socket数据包结构标识类型
				packetEndLength = SocketConfig.PACKET_END_LENGTH;
				break;
			default:
				put(socketDataStructureType);// 写入Socket数据包结构标识类型
				break;
		}
	}

	/**
	 * 写入ByteBuffer
	 * @param byteBuffer ByteBuffer
	 */
	public void putByteBuffer(ByteBuffer byteBuffer)
	{
	}

	/**
	 * 写入ByteBuffer
	 * @param byteBuffer 源ByteBuffer
	 * @param isResetPosition 源ByteBuffer读取后，是否复位至读取前位置，即读取前的“byteBuffer.position()”
	 */
	public void putByteBuffer(ByteBuffer byteBuffer, boolean isResetPosition)
	{
	}

	/**
	 * 写入Byte
	 * @param value 值
	 */
	public void put(byte value)
	{
	}

	/**
	 * 写入Byte
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void put(int index, byte value)
	{
	}

	/**
	 * 写入Byte[]
	 * @param value 值
	 */
	public void put(byte[] value)
	{
	}

	/**
	 * 写入Byte[]
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void put(int index, byte[] value)
	{
	}

	/**
	 * 写入boolean
	 * @param value 值
	 */
	public void putBoolean(boolean value)
	{
	}

	/**
	 * 写入boolean
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void putBoolean(int index, boolean value)
	{
	}

	/**
	 * 写入Short
	 * @param value 值
	 */
	public void putShort(short value)
	{
	}

	/**
	 * 写入Short
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void putShort(int index, short value)
	{
	}

	/**
	 * 写入Int
	 * @param value 值
	 */
	public void putInt(int value)
	{
	}

	/**
	 * 写入Int
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void putInt(int index, int value)
	{
	}

	/**
	 * 写入Long
	 * @param value 值
	 */
	public void putLong(long value)
	{
	}

	/**
	 * 写入Long
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void putLong(int index, long value)
	{
	}

	/**
	 * 写入Float
	 * @param value 值
	 */
	public void putFloat(float value)
	{
	}

	/**
	 * 写入Float
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void putFloat(int index, float value)
	{
	}

	/**
	 * 写入Double
	 * @param value 值
	 */
	public void putDouble(double value)
	{
	}

	/**
	 * 写入Double
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void putDouble(int index, double value)
	{
	}

	/**
	 * 写入Date
	 * @param value 值
	 */
	public void putDate(Date value)
	{
	}

	/**
	 * 写入Date
	 * @param index 位于Vector的索引位置，Vector必须存在，且其值必须小于Vector当前的长度
	 * @param value 值
	 */
	public void putDate(int index, Date value)
	{
	}

	/**
	 * 写入String
	 * @param value 值
	 */
	public void putString(String value)
	{
	}

	/**
	 * 结束包体
	 * @param isCrypto 是否加密
	 */
	public void end(boolean isCrypto)
	{
		end(isCrypto, 0);
	}

	/**
	 * 结束包体
	 * @param isCrypto 是否加密
	 * @param packetNumber 包编号(只有TCP包才需要设置)
	 */
	public void end(boolean isCrypto, int packetNumber)
	{
		switch (socketDataStructureType)
		{
			case SocketDataStructureType.DATA:
				if (socketType == SocketType.TCP)
				{
					if(!isCrypto)
					{//如果不加密，包尾不需要校验码，即包尾长度为0
						packetEndLength=0;
					}
					else
					{
						packetEndLength = SocketConfig.PACKET_END_LENGTH;
					}

					int pos = 0;
					if (commandID >= 0)
					{
						packetBodyLength = dataBytesLength + SocketConfig.PACKET_PROTOCOL_NUMBE_LENGTH + SocketConfig.COMMAND_LENGTH;
						pos=0;
					}
					else
					{
						packetBodyLength = dataBytesLength + SocketConfig.PACKET_PROTOCOL_NUMBE_LENGTH;
						pos = SocketConfig.COMMAND_LENGTH;
					}

					if(packetType==1)
					{
						data = ByteBuffer.allocate(SocketConfig.PACKET_STRUCTURE_TYPE_LENGTH  + SocketConfig.PACKET_NUMBER_LENGTH + SocketConfig.PACKET_BODY_LENGTH +  packetBodyLength + packetEndLength);
						data.order(SocketConfig.byteOrder);
//						data.position(pos);
						data.put(socketDataStructureType);
						data.putInt(packetNumber);
						data.putInt(packetBodyLength);
						data.putShort(packetCode);
						if (commandID >= 0)
						{
							data.putLong(commandID);// 写入命令ID标识(如果值大于等于0)
						}
						for (byte[] bytes : dataBytes)
						{
							data.put(bytes);
						}
						data.flip();
//						data.position(pos);
					}
					else
					{
						data.flip();
						data.position(pos);
						data.put(data.position() + 0, socketDataStructureType);
						data.putInt(data.position() + 1, packetNumber);
						data.putInt(data.position() + 5, packetBodyLength);
						data.putShort(data.position() + 9, packetCode);
						if (commandID >= 0)
						{
							putLong(data.position() + 11, commandID);// 写入命令ID标识(如果值大于等于0)
						}
						
					}
				}
				else if(socketType == SocketType.UDP)
				{
					int pos = 0;
					if (commandID >= 0)
					{
						packetBodyLength = dataBytesLength + SocketConfig.PACKET_PROTOCOL_NUMBE_LENGTH + SocketConfig.COMMAND_LENGTH;
						pos=SocketConfig.PACKET_NUMBER_LENGTH;
					}
					else
					{
						packetBodyLength = dataBytesLength + SocketConfig.PACKET_PROTOCOL_NUMBE_LENGTH;
						pos = SocketConfig.PACKET_NUMBER_LENGTH + SocketConfig.COMMAND_LENGTH;
					}

					if(packetType==1)
					{
						data = ByteBuffer.allocate(SocketConfig.PACKET_STRUCTURE_TYPE_LENGTH  + SocketConfig.PACKET_BODY_LENGTH +  packetBodyLength);
						data.order(SocketConfig.byteOrder);
//						data.position(pos);
						data.put(socketDataStructureType);
						data.putInt(packetBodyLength);
						data.putShort(packetCode);
						if (commandID >= 0)
						{
							putLong(commandID);// 写入命令ID标识(如果值大于等于0)
						}
						for (byte[] bytes : dataBytes)
						{
							data.put(bytes);
						}
						data.flip();
//						data.position(pos);
					}
					else
					{
						data.flip();
						data.position(pos);
						data.put(data.position() + 0, socketDataStructureType);
						data.putInt(data.position() + 1, packetBodyLength);
						data.putShort(data.position() + 5, packetCode);
						if (commandID >= 0)
						{
							putLong(data.position() + 7, commandID);// 写入命令ID标识(如果值大于等于0)
						}
					}
				}
				break;
			case SocketDataStructureType.HANDSHAKE_TCP:
			case SocketDataStructureType.HANDSHAKE_TCP_ACK:
				data.flip();
				packetBodyLength = dataBytesLength - packetHeadLength;
				putInt(1, packetBodyLength);// 写入数据包体长度
				break;
			default:
				if(packetType==1)
				{
					if(!isCrypto)
					{//如果不加密，包尾不需要校验码，即包尾长度为0
						packetEndLength=0;
					}
					data = ByteBuffer.allocate(dataBytesLength + packetEndLength);
					data.order(SocketConfig.byteOrder);
					for (byte[] bytes : dataBytes)
					{
						data.put(bytes);
					}
				}
				data.flip();
				break;
		}
	}

	/**
	 * 复制新数据包
	 */
	public ISocketSendablePacketBase duplicate()
	{
		return null;
	}

	public synchronized void dispose()
	{
		if(data!=null && packetType==1)
		{
			data.clear();
		}
		super.dispose();
	}
}
