package base.net.socket.tcp;

import base.net.socket.SocketClient;
import base.net.socket.SocketUtil;
import base.net.socket.packet.ISocketSendablePacketBase;
import base.timer.TimerTask;
import base.types.SocketDataStructureType;
import base.types.SocketType;

//TCP心跳事件
public class SocketTcpKeepAlive extends TimerTask
{
	private SocketClient socketClient;

	public SocketTcpKeepAlive(SocketClient socketClient)
	{
		this.socketClient = socketClient;
	}

	@Override
	public void run()
	{
		start();
	}

	private void start()
	{
		if (socketClient.isAvailable() && socketClient.getSocketConnectionBase().getIsAvailable())
		{
//			System.out.println("TCP心跳包发送开始");
			ISocketSendablePacketBase socketSendablePacketServer = SocketUtil.socketSendablePacketServer(SocketType.TCP, SocketDataStructureType.KEEP_ALIVE);
			socketClient.send(socketSendablePacketServer);
		}
	}
}
