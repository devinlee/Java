package base.net.socket;

import java.nio.ByteOrder;

public class SocketConfig
{
	/**
	 * 包种类标识长度(1byte)
	 */
	public final static byte PACKET_STRUCTURE_TYPE_LENGTH = 1;

	/**
	 * 连接ID长度(int)
	 */
	public final static byte CONNECTION_ID_LENGTH = 4;

	/**
	 * 连接校验码长度(int)
	 */
	public final static byte CONNECTION_CHECK_CODE_LENGTH = 4;

	/**
	 * 数据包序号长度(int)
	 */
	public final static byte PACKET_NUMBER_LENGTH = 4;

	/**
	 * 包体长度(int)
	 */
	public final static byte PACKET_BODY_LENGTH = 4;

	/**
	 * 数据包协议号长度(short)
	 */
	public final static byte PACKET_PROTOCOL_NUMBE_LENGTH = 2;

	/**
	 * UDP分解包包头长度((标识(1byte)&连接ID(4byte)&连接校验码(4byte)&分解包序号(4byte))
	 */
	public final static byte PACKET_HEAD_LENGTH_UDP = 13;

	/**
	 * 包尾长度(校验码(4byte))
	 */
	public final static byte PACKET_END_LENGTH = 4;

	/**
	 * 命令ID标识长度(long)
	 */
	public final static byte COMMAND_LENGTH = 8;

	/**
	 * UDP数据包最大长度
	 */
	public final static short PACKET_UDP_LENGTN = 512;

	/**
	 * TCP数据包最大长度
	 */
	public final static short PACKET_TCP_LENGTN = 1024;
	
	/**
	 * 发送数据字节顺序类型
	 */
	public final static ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
}
