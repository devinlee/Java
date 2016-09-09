package base.net;

import base.net.socket.SocketController;

/**
 * 网络服务工厂
 * @author Devin
 *
 */
public class NetFactory
{
	/**
	 * Socket控制器
	 */
	private final static SocketController socketController = new SocketController();

	private NetFactory()
	{
	}

	/**
	 * Socket控制器
	 * @return SocketController
	 */
	public static SocketController socketController()
	{
		return socketController;
	}
}
