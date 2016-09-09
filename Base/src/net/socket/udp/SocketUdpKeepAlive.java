package base.net.socket.udp;

import base.net.socket.SocketClient;
import base.net.socket.SocketUtil;
import base.net.socket.packet.ISocketSendablePacketBase;
import base.timer.TimerTask;
import base.types.SocketDataStructureType;
import base.types.SocketType;

public class SocketUdpKeepAlive extends TimerTask
{
	private SocketClient socketClient;

	public SocketUdpKeepAlive(SocketClient socketClient)
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
			System.out.println("UDP心跳包发送开始");
			// Udp心跳
			ISocketSendablePacketBase socketSendablePacketServer = SocketUtil.socketSendablePacketServer(SocketType.UDP, SocketDataStructureType.KEEP_ALIVE);
			socketSendablePacketServer.putInt(socketClient.getSocketConnectionBase().getId());
			socketSendablePacketServer.putInt(socketClient.getSocketConnectionBase().getConnectionCheckCode());
			socketClient.send(socketSendablePacketServer);
		}
	}
}
