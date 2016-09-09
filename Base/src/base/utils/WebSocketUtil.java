package base.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketUtil
{
	// private final static String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
	// private final static String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
	// /* websocket版本号：草案8到草案12版本号都是8，草案13及以后的版本号都和草案号相同 */
	// private final static String Sec_WebSocket_Version = "Sec-WebSocket-Version";

	/** 
	 * 判断是否是WebSocket连接请求
	 * @param receiveText 请求连接时的握手信息 
	 * @return 如为websocket返回true,否则返回false
	 */
	public static boolean isWebSocket(String receiveText)
	{
		if (receiveText == null)
			return false;
		receiveText = receiveText.toLowerCase();
		Pattern pattern = Pattern.compile("upgrade:(.*?)" + System.lineSeparator());
		Matcher matcher = pattern.matcher(receiveText);
		if (matcher.find())
		{
			String matcherStr = matcher.group(0);
			String[] matcherStrs = matcherStr.split(":");
			if (matcherStrs != null && matcherStrs.length > 1)
			{
				return matcherStrs[1].replace(System.lineSeparator(), "").trim().equals("websocket");
			}
		}
		return false;
	}

	/** 
	 * 格式化Websocket数据
	 * @param desBytes 目标数据
	 * @param maskCode 掩码
	 */
	public static void formatWebSocketBytes(byte[] desBytes, byte[] maskCode)
	{
		int len = desBytes.length;
		for (int i = 0; i < len; i++)
		{
			desBytes[i] = (byte) (desBytes[i] ^ maskCode[i % 4]);
		}
	}

	public static byte[] buildFrameHead(int payloadLen)
	{
		int byteLen = 0;
		byte maskAndPayloadLen = 0;
		if (payloadLen <= 125)
		{
			byteLen = 2;
			maskAndPayloadLen = (byte) payloadLen;
		}
		else if (payloadLen <= Short.MAX_VALUE)
		{
			byteLen = 4;
			maskAndPayloadLen = 126;
		}
		else
		{
			byteLen = 10;
			maskAndPayloadLen = 127;
		}
		byte[] frameBytes = new byte[byteLen];

		frameBytes[0] = ByteUtil.bitToByte("10000010");// FIN(1)+RSV(000)+Opcode(0x2(0010))
		// Mask、Payload len处理
		// byte[] bits = ByteUtil.byteToBit((byte)maskAndPayloadLen);
		// bits[0]=0;//Mask位为二进制码0，表示无Mask掩码，即不增加掩码
		// String strBits = ByteUtil.bytesToString(bits, 0, 8);
		// frameBytes[1]=ByteUtil.bitToByte(strBits);//Mask(1)+Payload len(根据数据长度及规则得出来的二进制码)
		frameBytes[1] = (byte) maskAndPayloadLen;// Mask、Payload len处理，当前处理为Mask位置为0，即不增加掩码

		// Extended payload length长度处理
		if (maskAndPayloadLen == 126)
		{// Extended payload length 长度为二字节长度
			byte[] bts = ByteUtil.getBytes((short) payloadLen);
			frameBytes[2] = bts[0];
			frameBytes[3] = bts[1];
		}
		else if (maskAndPayloadLen == 127)
		{// Extended payload length 长度为8字节长度
			byte[] bts = ByteUtil.getBytes((long) payloadLen);
			frameBytes[2] = bts[0];
			frameBytes[3] = bts[1];
			frameBytes[4] = bts[2];
			frameBytes[5] = bts[3];
			frameBytes[6] = bts[4];
			frameBytes[7] = bts[5];
			frameBytes[8] = bts[6];
			frameBytes[9] = bts[7];
		}
		return frameBytes;
	}

	// /**
	// * 根据WebSocket请求，判断不同的握手形式，并返回相应版本的握手结果
	// * @param req
	// * @return
	// */
	// public HttpResponse buildWebSocketRes(HttpRequest req) {
	// String reasonPhrase = "";
	// boolean isThirdTypeHandshake = Boolean.FALSE;
	// int websocketVersion = 0;
	// if (req.getHeader(Sec_WebSocket_Version) != null) {
	// websocketVersion = Integer.parseInt(req.getHeader(Sec_WebSocket_Version));
	// }
	// /**
	// * 在草案13以及其以前，请求源使用http头是Origin，是草案4到草案10，请求源使用http头是Sec-WebSocket-Origin，而在草案11及以后使用的请求头又是Origin了，
	// * 不知道这些制定WEBSOCKET标准的家伙在搞什么东东，一个请求头有必要变名字这样变来变去的吗。<br>
	// * 注意，这里还有一点需要注意的就是"websocketVersion >= 13"这个条件，并不一定适合以后所有的草案，不过这也只是一个预防，有可能会适应后面的草案， 如果不适合还只有升级对应的websocket协议。<br>
	// */
	// if (websocketVersion >= 13
	// || (req.containsHeader(Names.SEC_WEBSOCKET_ORIGIN) && req.containsHeader(SEC_WEBSOCKET_KEY))) {
	// isThirdTypeHandshake = Boolean.TRUE;
	// }
	//
	// // websocket协议草案7后面的格式，可以参看wikipedia上面的说明，比较前后版本的不同：http://en.wikipedia.org/wiki/WebSocket
	// if (isThirdTypeHandshake = Boolean.FALSE) {
	// reasonPhrase = "Switching Protocols";
	// } else {
	// reasonPhrase = "Web Socket Protocol Handshake";
	// }
	// HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(101, reasonPhrase));
	// res.addHeader(HttpHeaders.Names.UPGRADE, HttpHeaders.Values.WEBSOCKET);
	// res.addHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.UPGRADE);
	// // Fill in the headers and contents depending on handshake method.
	// if (req.containsHeader(Names.SEC_WEBSOCKET_KEY1) && req.containsHeader(Names.SEC_WEBSOCKET_KEY2)) {
	// // New handshake method with a challenge:
	// res.addHeader(Names.SEC_WEBSOCKET_ORIGIN, req.getHeader(Names.ORIGIN));
	// res.addHeader(Names.SEC_WEBSOCKET_LOCATION, getWebSocketLocation(req));
	// String protocol = req.getHeader(Names.SEC_WEBSOCKET_PROTOCOL);
	// if (protocol != null) {
	// res.addHeader(Names.SEC_WEBSOCKET_PROTOCOL, protocol);
	// }
	// // Calculate the answer of the challenge.
	// String key1 = req.getHeader(Names.SEC_WEBSOCKET_KEY1);
	// String key2 = req.getHeader(Names.SEC_WEBSOCKET_KEY2);
	// int a = (int) (Long.parseLong(getNumeric(key1)) / getSpace(key1).length());
	// int b = (int) (Long.parseLong(getNumeric(key2)) / getSpace(key2).length());
	// long c = req.getContent().readLong();
	// ChannelBuffer input = ChannelBuffers.buffer(16);
	// input.writeInt(a);
	// input.writeInt(b);
	// input.writeLong(c);
	// ChannelBuffer output = null;
	// try {
	// output = ChannelBuffers.wrappedBuffer(MessageDigest.getInstance("MD5").digest(input.array()));
	// } catch (NoSuchAlgorithmException e) {
	// }
	//
	// res.setContent(output);
	// } else if (isThirdTypeHandshake = Boolean.FALSE) {
	// String protocol = req.getHeader(Names.SEC_WEBSOCKET_PROTOCOL);
	// if (protocol != null) {
	// res.addHeader(Names.SEC_WEBSOCKET_PROTOCOL, protocol);
	// }
	// res.addHeader(SEC_WEBSOCKET_ACCEPT, getSecWebSocketAccept(req));
	// } else {
	// // Old handshake method with no challenge:
	// if (req.getHeader(Names.ORIGIN) != null) {
	// res.addHeader(Names.WEBSOCKET_ORIGIN, req.getHeader(Names.ORIGIN));
	// }
	// res.addHeader(Names.WEBSOCKET_LOCATION, getWebSocketLocation(req));
	// String protocol = req.getHeader(Names.WEBSOCKET_PROTOCOL);
	// if (protocol != null) {
	// res.addHeader(Names.WEBSOCKET_PROTOCOL, protocol);
	// }
	// }
	//
	// return res;
	// }
	//
	// private String getWebSocketLocation(HttpRequest req) {
	// return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + req.getUri();
	// }
	//
	// private String getSecWebSocketAccept(HttpRequest req) {
	// // CHROME WEBSOCKET VERSION 8中定义的GUID，详细文档地址：http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-10
	// String guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	// String key = "";
	// key = req.getHeader(SEC_WEBSOCKET_KEY);
	// key += guid;
	// try {
	// MessageDigest md = MessageDigest.getInstance("SHA-1");
	// md.update(key.getBytes("iso-8859-1"), 0, key.length());
	// byte[] sha1Hash = md.digest();
	// key = base64Encode(sha1Hash);
	// } catch (NoSuchAlgorithmException e) {
	// } catch (UnsupportedEncodingException e) {
	// }
	// return key;
	// }

	// private String base64Encode(byte[] input)
	// {
	// sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
	// return encoder.encode(input);
	// }
	//
	// // 去掉传入字符串的所有非数字
	// private String getNumeric(String str) {
	// return str.replaceAll("\\D", "");
	// }
	//
	// // 返回传入字符串的空格
	// private String getSpace(String str) {
	// return str.replaceAll("\\S", "");
	// }
}
