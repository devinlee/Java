package base.util;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

public class RSAUtil
{
	/**
	 * 取得公钥、私钥
	 * @return 如果生成成功，返回生成的公钥匙与私钥，数据：公钥：hashMap.get("public")，私钥：hashMap.get("private")，如果失败则返回null
	 */
	public static HashMap<String, Object> getKeys()
	{
		try
		{
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(512);
			KeyPair keyPair = keyPairGenerator.genKeyPair();
			// 生成公钥
			RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
			// 生成私钥
			RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
			HashMap<String, Object> keys = new HashMap<String, Object>();
			keys.put("public", publicKey);
			keys.put("private", privateKey);
			return keys;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据指定的Key Bytes 取得公钥
	 * @param keyBytes 指定的Key Bytes
	 * @return 成功返回PublicKey，否则返回null
	 */
	public static PublicKey getPublicKey(byte[] keyBytes)
	{
		try
		{
			X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePublic(spec);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据指定的Key Bytes 取得私钥
	 * @param keyBytes 指定的Key Bytes
	 * @return 成功返回PublicKey，否则返回null
	 */
	public static PrivateKey getPrivateKey(byte[] keyBytes)
	{
		try
		{
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePrivate(spec);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 取得公钥
	 * @param modulus 系数
	 * @param publicExponent 公用指数
	 * @return
	 * @throws Exception
	 */
	public static PublicKey getPublicKey(byte[] modulus, byte[] publicExponent) throws Exception
	{
		BigInteger m = new BigInteger(modulus);
		BigInteger e = new BigInteger(publicExponent);
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(keySpec);
		return publicKey;
	}

	/**
	 * 取得公钥
	 * @param modulus 系数
	 * @param publicExponent 公用指数
	 * @return
	 * @throws Exception
	 */
	public static PublicKey getPublicKey(String modulus, String publicExponent) throws Exception
	{
		BigInteger m = new BigInteger(modulus);
		BigInteger e = new BigInteger(publicExponent);
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(keySpec);
		return publicKey;
	}

	/**取得私钥
	 * @param modulus 系数
	 * @param privateExponent 专用指数
	 * @return
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKey(byte[] modulus, byte[] privateExponent) throws Exception
	{
		BigInteger m = new BigInteger(modulus);
		BigInteger e = new BigInteger(privateExponent);
		RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;
	}

	/**
	 * 加密指定的数据
	 * @param publicKey 公钥
	 * @param sourceBytes 指定要加密的数据
	 * @return 成功返回加密后的数据，否则返回null
	 */
	public static byte[] encrypt(PublicKey publicKey, byte[] sourceBytes)
	{
		try
		{
			// Cipher cipher = Cipher.getInstance("RSA");
			// Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding", new org.bouncycastle.jce.provider.BouncyCastleProvider());
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			// Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(sourceBytes);
		}
		catch (Exception e)
		{
			Logger.getLogger(RSAUtil.class.getName()).log(Level.SEVERE, "RSA加密指定的数据时错误", e);
		}
		return null;
	}

	/**
	 * 解密指定的数据
	 * @param privateKey 私钥
	 * @param sourceBytes 指定要解密的数据
	 * @return 成功返回解密后的数据，否则返回null
	 */
	public static byte[] decryption(PrivateKey privateKey, byte[] sourceBytes)
	{
		try
		{
			// Cipher cipher = Cipher.getInstance("RSA");
			// Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", new org.bouncycastle.jce.provider.BouncyCastleProvider());
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			// Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			System.out.println("sourceBytes.length:" + sourceBytes.length);
			return cipher.doFinal(sourceBytes);
		}
		catch (Exception e)
		{
			Logger.getLogger(RSAUtil.class.getName()).log(Level.SEVERE, "RSA解密指定的数据时错误", e);
		}
		return null;
	}
}
