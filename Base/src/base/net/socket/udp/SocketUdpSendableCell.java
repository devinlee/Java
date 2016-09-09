package base.net.socket.udp;

import java.nio.ByteBuffer;

import base.event.EventDispatcher;
import base.net.NetFactory;
import base.net.socket.events.SocketConnectionEvent;
import base.timer.TimerTask;
import base.types.SocketType;
import base.util.CryptoUtil;

/**
 * Socket Udp 发送窗口序列数据单元
 * @author Devin
 *
 */
public class SocketUdpSendableCell extends EventDispatcher
{
	/**
	 * 数据
	 */
	private ByteBuffer data;

	/**
	 * 数据
	 * @param data 要设置的 data
	 */
	public void setData(ByteBuffer data)
	{
		this.data = data;
	}

	/**
	 * 数据
	 * @return data
	 */
	public ByteBuffer getData()
	{
		return data;
	}

	/**
	 * 数据位置
	 */
	private int dataPosition;
	/**
	 * 数据位置
	 */
	public void setDataPosition(int dataPosition)
	{
		this.dataPosition = dataPosition;
	}
	/**
	 * 数据位置
	 */
	public int getDataPosition()
	{
		return dataPosition;
	}

	/**
	 * 数据限制位置
	 */
	private int dataLimit;
	/**
	 * 数据限制位置
	 */
	public void setDataLimit(int dataLimit)
	{
		this.dataLimit = dataLimit;
	}
	/**
	 * 数据限制位置
	 */
	public int getDataLimit()
	{
		return dataLimit;
	}

	/**
	 * 数据包种类
	 */
	private byte socketDataStructureType;

	/**
	 * 数据包种类
	 * @return socketDataStructureType
	 */
	public byte getSocketDataStructureType()
	{
		return socketDataStructureType;
	}

	/**
	 * 序号
	 */
	private int sequenceNumber;

	/**
	 * 序号
	 * @return sequenceNumber
	 */
	public int getSequenceNumber()
	{
		return sequenceNumber;
	}
	
	/**
	 * ACK应答超时计时器
	 */
	private TimerTask timerTask;

	/**
	 * 真实包体数据
	 */
	private byte[] bodyBytes;
	
	/**
	 * 发送次数
	 */
	private volatile short sendNumber=0;
	
	/**
	 * 发送次数
	 */
	public short getSendNumber()
	{
		return sendNumber;
	}
	
	/**
	 * 发送次数
	 */
	public void setSendNumber(short sendNumber)
	{
		this.sendNumber=sendNumber;
	}
	
	/**
	 * 是否已应答
	 */
	private volatile boolean isAck;
	
	/**
	 * 是否已应答
	 */
	public boolean getIsAck()
	{
		return isAck;
	}
	
	/**
	 * 是否已应答
	 */
	public void setIsAck(boolean isAck)
	{
		if(this.isAck==isAck)return;
		this.isAck=isAck;
		if(this.isAck)
		{
			if (timerTask != null)
			{
				timerTask.cancel();
				timerTask = null;
			}
		}
	}
	
	/**
	 * Socket Udp 滑动窗口序列数据单元
	 * @param sequenceNumber 序号
	 * @param data 单元数据
	 */
	public SocketUdpSendableCell(byte socketDataStructureType, ByteBuffer byteBuffer, byte[] bodyBytes)
	{
		this.socketDataStructureType = socketDataStructureType;
		this.data = byteBuffer;
		this.bodyBytes=bodyBytes;
		this.dataPosition=byteBuffer.position();
		this.dataLimit=byteBuffer.limit();
	}

	/**
	 * 应答计时开始
	 */
	public TimerTask ackTimerStart()
	{
		if (timerTask != null)
		{
			timerTask.cancel();
			timerTask = null;
		}
		timerTask = new TimerTask()
		{
			public void run()
			{
				if(isAck)
				{//如果已应答
					cancel();
					return;
				}
				dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.SENDABLE_PACKET_ACK_TIME_OUT, sequenceNumber));
			}
		};
		return timerTask;
	}

	/**
	 * 结束窗口序列数据单元
	 * @param connectionID 连接ID
	 * @param connectionCheckCode 连接校验码
	 * @param isCrypto 包体数据是否加密
	 * @param sequenceNumber 序列数据单元位于窗口的序号
	 */
	public void end(int connectionID, int connectionCheckCode, int sequenceNumber, boolean isCrypto, int[] cryptoKeys)
	{
		this.sequenceNumber=sequenceNumber;
		if(bodyBytes!=null)
		{
			data.putInt(sequenceNumber);// 包位与窗体的序号
			if (isCrypto)
			{// 如果需要对数据包进行加密及校验，则进行相应处理
				byte[] encryptEndCode = CryptoUtil.encryptPacket(socketDataStructureType, connectionID, connectionCheckCode, sequenceNumber, bodyBytes, cryptoKeys);
				data.put(bodyBytes);// 重置加入包体加密后数据
				data.put(encryptEndCode);// 加入包尾校验
			}
			else
			{// 不需要加密及校验，则直接加入数据
				data.put(bodyBytes);// 加入包体数据
			}
			data.flip();
		}
	}

	public synchronized void dispose()
	{
		if (data != null)
		{
			NetFactory.socketController().releaseSendablePacketClientByteBuffer(data, SocketType.UDP);
			data = null;
		}
		if (timerTask != null)
		{
			timerTask.cancel();
			timerTask = null;
		}
	}
}
