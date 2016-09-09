package base;

import base.loader.ProgramClassLoader;

public class Base
{
	/**
	 * Data项目包类文件根目录
	 * @param rootDir
	 */
	private static String classRootDir;

	/**
	 * 设置Data项目包类文件根目录
	 * @param rootDir
	 */
	public static void setClassRootDir(String rootDir)
	{
		classRootDir = rootDir;
	}

	/**
	 * 实例化类
	 * @param classObject 类对象
	 * @return 实例成功，返回已实例的类对象，否则返回null
	 */
	public static <T> T newClass(Class<?> classObject)
	{
		return ProgramClassLoader.getInstance().newClass(classRootDir, classObject.getPackage().getName(), classObject.getSimpleName());
	}

	/**
	 * 移除实例化的类
	 * @param classObject 类对象
	 * @return 移出成功，返回true，否则返回false
	 */
	public static boolean removeClass(Class<?> classObject)
	{
		return ProgramClassLoader.getInstance().removeClass(classRootDir, classObject.getPackage().getName(), classObject.getSimpleName());
	}
}