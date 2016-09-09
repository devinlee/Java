package base.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProgramClassLoader
{
	/**
	 * 线程读写锁
	 */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	/**
	 * 已载入类列表容器
	 */
	private final ConcurrentHashMap<String, Class<?>> classList = new ConcurrentHashMap<String, Class<?>>();

	private ProgramClassLoader()
	{

	}

	/**
	 * 新创建一个类实例
	 * @param rootDirPath 类文件根目录路径
	 * @param packageName 类包名
	 * @param className 类名
	 * @return 如果存在指定路径的类，则返回实例后的类，否则返回null
	 */
	@SuppressWarnings("unchecked")
	public <T> T newClass(String rootDirPath, String packageName, String className)
	{
		T classInstance = null;
		lock.writeLock().lock();
		try
		{
			Class<?> cls = null;
			packageName = packageName.replace('.', File.separatorChar);
			StringBuffer sb = new StringBuffer(rootDirPath);
			sb.append(packageName);
			sb.append(File.separator + className + ".class");
			String classPath = sb.toString();
			if (!classList.containsKey(classPath))
			{
				cls = loadProgramClass(classPath, className);
			}
			else
			{
				cls = classList.get(classPath);
			}

			if (cls == null)
			{
				Logger.getLogger(ProgramClassLoader.class.getName()).log(Level.SEVERE, null, new ClassNotFoundException(classPath));
			}
			else
			{
				classInstance = (T) cls.newInstance();
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(ProgramClassLoader.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
		return classInstance;
	}

	/**
	 * 移除一个类
	 * @param rootDirPath 类文件根目录路径
	 * @param packageName 类包名
	 * @param className 类名
	 * @return 移出成功，返回true，否则返回false
	 */
	public boolean removeClass(String rootDirPath, String packageName, String className)
	{
		lock.writeLock().lock();
		try
		{
			packageName = packageName.replace('.', File.separatorChar);
			StringBuffer sb = new StringBuffer(rootDirPath);
			sb.append(packageName);
			sb.append(File.separator + className + ".class");
			String classPath = sb.toString();
			if (classList.containsKey(classPath))
			{
				classList.remove(classPath);
			}
			return true;
		}
		catch (Exception e)
		{
			Logger.getLogger(ProgramClassLoader.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 移除所有类
	 * @return 移出成功，返回true，否则返回false
	 */
	public boolean removeAllClass()
	{
		lock.writeLock().lock();
		try
		{
			if(classList!=null)
			{
				classList.clear();
			}
			return true;
		}
		catch (Exception e)
		{
			Logger.getLogger(ProgramClassLoader.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * 载入一个类
	 * @param classPath 类文件路径
	 * @param className 类名
	 */
	private Class<?> loadProgramClass(String classPath, String className)
	{
		Class<?> cls = null;
		try
		{
			if (!classList.containsKey(classPath))
			{
				File classFile = new File(classPath);
				if (classFile.exists())
				{
					cls = defineProgramClass(className, new FileInputStream(classFile), classFile.length());
					if (cls != null)
					{
						classList.put(classPath, cls);
					}
				}
			}
			else
			{
				cls = classList.get(classPath);
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(ProgramClassLoader.class.getName()).log(Level.SEVERE, null, e);
		}
		return cls;
	}

	/**
	 * 定义一个类
	 * @param className 类名
	 * @param inputStream 输入流
	 * @param length 输入流长度
	 * @return 如果成功定义返回定义的类，否则返回null
	 */
	private Class<?> defineProgramClass(String className, InputStream inputStream, long length)
	{
		Class<?> cls = null;
		try
		{
			byte[] raw = new byte[(int) length];
			inputStream.read(raw);
			inputStream.close();
			cls = new ProgramClassLoaderBase().defindProgramClass(className, raw, 0, raw.length);
		}
		catch (Exception e)
		{
			Logger.getLogger(ProgramClassLoader.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			try
			{
				inputStream.close();
				inputStream = null;
			}
			catch (IOException e)
			{
				Logger.getLogger(ProgramClassLoader.class.getName()).log(Level.SEVERE, null, e);
			}
		}
		return cls;
	}

	private volatile static ProgramClassLoader instance;

	/**
	 * 类载入
	 */
	public static ProgramClassLoader getInstance()
	{
		if (instance == null)
		{
			synchronized (ProgramClassLoader.class)
			{
				if (instance == null)
				{
					instance = new ProgramClassLoader();
				}
			}
		}
		return instance;
	}
}
