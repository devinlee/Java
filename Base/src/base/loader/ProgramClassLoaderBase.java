package base.loader;

public class ProgramClassLoaderBase extends ClassLoader
{
	public ProgramClassLoaderBase()
	{
	}

	public Class<?> defindProgramClass(String name, byte[] b, int off, int len) throws ClassNotFoundException
	{
		return defineClass(null, b, 0, b.length);
	}
}
