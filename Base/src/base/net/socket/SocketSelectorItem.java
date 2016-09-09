package base.net.socket;

import java.nio.channels.SelectionKey;

public class SocketSelectorItem
{
	private SocketBase socketBase = null;

	public SocketBase getSocketBase()
	{
		return socketBase;
	}

	private ISocketConnection socketConnection = null;

	public ISocketConnection getSocketConnection()
	{
		return socketConnection;
	}

	public SocketSelectorItem(SocketBase socketBase)
	{
		this.socketBase = socketBase;
	}

	public SocketSelectorItem(SocketBase socketBase, ISocketConnection socketConnection)
	{
		this.socketBase = socketBase;
		this.socketConnection = socketConnection;
	}

	public void handle(SelectionKey selectionKey)
	{
		socketBase.handleSelectionKey(selectionKey, socketConnection);
	}

	public synchronized void dispose()
	{
		socketBase = null;
		socketConnection = null;
	}
}
