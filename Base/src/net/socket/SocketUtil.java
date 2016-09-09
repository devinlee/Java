package base.net.socket;

import java.nio.ByteBuffer;

import base.Base;
import base.net.socket.packet.ISocketReceivablePacket;
import base.net.socket.packet.ISocketSendablePacketBase;
import base.net.socket.packet.SocketReceivablePacket;
import base.net.socket.packet.SocketSendablePacketClient;
import base.net.socket.packet.SocketSendablePacketServer;
import base.types.SocketDataStructureType;
import base.types.SocketType;

public class SocketUtil
{
	/**
	 * 创建SocketReceivablePacket
	 * @param SocketType 数据包Socket传输类型
	 * @param byteBuffer 数据源
	 * @param packetCode 数据包协议号
	 */
	public static ISocketReceivablePacket socketReceivablePacket(SocketType socketType, ByteBuffer byteBuffer, short packetCode)
	{
		ISocketReceivablePacket socketReceivablePacket = Base.newClass(SocketReceivablePacket.class);
		socketReceivablePacket.init(socketType, byteBuffer, packetCode);
		return socketReceivablePacket;
	}

	/**
	 * 创建SocketReceivablePacket
	 * @param SocketType 数据包Socket传输类型
	 * @param byteBuffer 数据源
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public static ISocketReceivablePacket socketReceivablePacket(SocketType socketType, ByteBuffer byteBuffer, short packetCode, long commandID)
	{
		ISocketReceivablePacket socketReceivablePacket = Base.newClass(SocketReceivablePacket.class);
		socketReceivablePacket.init(socketType, byteBuffer, packetCode, commandID);
		return socketReceivablePacket;
	}

	/**
	 * 创建SocketReceivablePacket
	 * @param SocketType 数据包Socket传输类型
	 * @param socketDataStructureType Socket数据包结构标识类型
	 * @param byteBuffer 数据源
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public static ISocketReceivablePacket socketReceivablePacket(SocketType socketType, byte socketDataStructureType, ByteBuffer byteBuffer, short packetCode, long commandID)
	{
		ISocketReceivablePacket socketReceivablePacket = Base.newClass(SocketReceivablePacket.class);
		socketReceivablePacket.init(socketType, socketDataStructureType, byteBuffer, packetCode, commandID);
		return socketReceivablePacket;
	}
	
	/**
	 * 创建SocketSendablePacketClient
	 * @param packetCode 数据包协议号
	 */
	public static ISocketSendablePacketBase socketSendablePacketClient(short packetCode)
	{
		return socketSendablePacketClient(SocketType.ALL, SocketDataStructureType.DATA, packetCode, -1);
	}
	
//	/**
//	 * 创建SocketSendablePacketClient
//	 * @param socketConnection 连接
//	 * @param packetCode 数据包协议号
//	 */
//	public static ISocketSendablePacketBase socketSendablePacketClient(ISocketConnection socketConnection, short packetCode)
//	{
//		SocketType socketType;
//		if(socketConnection.getHandshakeCompleteUdp())
//		{//如果udp已握手，优先udp
//			socketType = SocketType.UDP;
//		}
//		else
//		{
//			socketType = SocketType.TCP;
//		}
//		return socketSendablePacketClient(socketType, SocketDataStructureType.DATA, packetCode, -1);
//	}

	/**
	 * 创建SocketSendablePacketClient
	 * @param socketType 数据包类型
	 * @param socketDataStructureType Socket数据包结构标识类型
	 */
	public static ISocketSendablePacketBase socketSendablePacketClient(SocketType socketType, byte socketDataStructureType)
	{
		return socketSendablePacketClient(socketType, socketDataStructureType, (short) 0, -1);
	}

//	/**
//	 * 创建SocketSendablePacketClient
//	 * @param socketType 数据包类型
//	 * @param packetCode 数据包协议号
//	 */
//	public static ISocketSendablePacketBase socketSendablePacketClient(SocketType socketType, short packetCode)
//	{
//		return socketSendablePacketClient(socketType, SocketDataStructureType.DATA, packetCode, -1);
//	}

//	/**
//	 * 创建SocketSendablePacketClient
//	 * @param socketType 数据包类型
//	 * @param socketDataStructureType Socket数据包结构标识类型
//	 * @param packetCode 数据包协议号
//	 */
//	public static ISocketSendablePacketBase socketSendablePacketClient(SocketType socketType, byte socketDataStructureType, short packetCode)
//	{
//		return socketSendablePacketClient(socketType, socketDataStructureType, packetCode, -1);
//	}

//	/**
//	 * 创建SocketSendablePacketClient
//	 * @param socketType 数据包类型
//	 * @param packetCode 数据包协议号
//	 * @param commandID 命令ID标识
//	 */
//	public static ISocketSendablePacketBase socketSendablePacketClient(SocketType socketType, short packetCode, long commandID)
//	{
//		return socketSendablePacketClient(socketType, SocketDataStructureType.DATA, packetCode, commandID);
//	}

	/**
	 * 创建SocketSendablePacketClient
	 * @param socketType 数据包类型
	 * @param socketDataStructureType Socket数据包结构标识类型
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public static ISocketSendablePacketBase socketSendablePacketClient(SocketType socketType, byte socketDataStructureType, short packetCode, long commandID)
	{
		ISocketSendablePacketBase socketSendablePacketClient = Base.newClass(SocketSendablePacketClient.class);
		socketSendablePacketClient.init(0, socketType, socketDataStructureType, packetCode, commandID);
		return socketSendablePacketClient;
	}
	
	/**
	 * 创建SocketSendablePacketServer
	 * @param packetCode 数据包协议号
	 */
	public static ISocketSendablePacketBase socketSendablePacketServer(short packetCode)
	{
		return socketSendablePacketServer(SocketType.TCP, SocketDataStructureType.DATA, packetCode, -1);
	}

	/**
	 * 创建SocketSendablePacketServer
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public static ISocketSendablePacketBase socketSendablePacketServer(short packetCode, long commandID)
	{
		return socketSendablePacketServer(SocketType.TCP, SocketDataStructureType.DATA, packetCode, commandID);
	}

	/**
	 * 创建SocketSendablePacketServer
	 * @param socketType 数据包类型
	 * @param socketDataStructureType Socket数据包结构标识类型
	 */
	public static ISocketSendablePacketBase socketSendablePacketServer(SocketType socketType, byte socketDataStructureType)
	{
		return socketSendablePacketServer(socketType, socketDataStructureType, (short) 0, -1);
	}

	/**
	 * 创建SocketSendablePacketServer
	 * @param socketType 数据包类型
	 * @param packetCode 数据包协议号
	 */
	public static ISocketSendablePacketBase socketSendablePacketServer(SocketType socketType, short packetCode)
	{
		return socketSendablePacketServer(socketType, SocketDataStructureType.DATA, packetCode, -1);
	}

	/**
	 * 创建SocketSendablePacketServer
	 * @param socketType 数据包类型
	 * @param socketDataStructureType Socket数据包结构标识类型
	 * @param packetCode 数据包协议号
	 */
	public static ISocketSendablePacketBase socketSendablePacketServer(SocketType socketType, byte socketDataStructureType, short packetCode)
	{
		return socketSendablePacketServer(socketType, socketDataStructureType, packetCode, -1);
	}

	/**
	 * 创建SocketSendablePacketServer
	 * @param socketType 数据包类型
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public static ISocketSendablePacketBase socketSendablePacketServer(SocketType socketType, short packetCode, long commandID)
	{
		return socketSendablePacketServer(socketType, SocketDataStructureType.DATA, packetCode, commandID);
	}

	/**
	 * 创建SocketSendablePacketServer
	 * @param socketType 数据包类型
	 * @param socketDataStructureType Socket数据包结构标识类型
	 * @param packetCode 数据包协议号
	 * @param commandID 命令ID标识
	 */
	public static ISocketSendablePacketBase socketSendablePacketServer(SocketType socketType, byte socketDataStructureType, short packetCode, long commandID)
	{
		ISocketSendablePacketBase socketSendablePacketServer = Base.newClass(SocketSendablePacketServer.class);
		socketSendablePacketServer.init(1, socketType, socketDataStructureType, packetCode, commandID);
		return socketSendablePacketServer;
	}
}