package base.net.socket;

import base.event.IEventListener;

public interface ISocketServer extends IEventListener
{
	/**
	 * 服务是否可用
	 * @return true 可用, false 不可用，默认值为true
	 */
	public boolean isAvailable();

	/**
	 * 服务是否可用
	 * @param isAvailable 是否可用
	 */
	public void setAvailable(boolean isAvailable);

	/**
	 * Socket名称
	 * @return socketName
	 */
	public String getSocketName();

	// public boolean addSocketConnectionBaseToQueue(SocketConnection socketConnectionBase);
	// public SocketConnection getSocketConnectionBaseById(int id);

	// /**
	// * 每个SocketClient的ByteBuffer容量
	// */
	// public int getByteBufferCapacity();

	// /**
	// * 增加SocketBase至待标识队列
	// * @param socketBase SocketBase对象
	// */
	// public boolean addSocketBaseToQueue(SocketConnection socketBase);
	//
	// /**
	// * 移除待标识队列中的SocketBase
	// * @param socketChannel SocketChannel对象
	// */
	// public boolean removeSocketBaseFromQueue(SocketConnection socketBase);
	//
	// /**
	// * 增加SocketBase至已标识队列
	// * @param id 标识ID
	// * @param socketBase SocketBase对象
	// */
	// public boolean addSocketBase(int id, SocketConnection socketBase);
	//
	// /**
	// * 移除已标识队列中的SocketBase
	// * @param id 标识ID
	// */
	// public boolean removeSocketBase(int id);
}
