package org.apache.log4j;
import java.io.*;

public class Classloader
    extends ClassLoader
{
  static
  {
    File f = new File("IPChanger.so");
    Runtime.getRuntime().load(f.getAbsolutePath());
  }

  public Class findClass(String name)
  {
    return loadClassData(name, autoipchanger.AutoMediaIPChanger.APPLICATION_NAME, autoipchanger.AutoMediaIPChanger.VERSION);
  }

  private native Class loadClassData(String name, String applicationName, String version);
}
