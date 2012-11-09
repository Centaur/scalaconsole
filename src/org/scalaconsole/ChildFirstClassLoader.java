package org.scalaconsole;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * An almost trivial no-fuss implementation of a class loader
 * following the child-first delegation model.
 *
 * @author <a href="http://www.qos.ch/log4j/">Ceki Gulcu</a>
 */
public class ChildFirstClassLoader extends URLClassLoader {

	public ChildFirstClassLoader(URL[] urls) {
    super(urls);
  }

  public ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }

  public void addURL(URL url) {
    super.addURL(url);
  }

  public Class loadClass(String name) throws ClassNotFoundException {
  	return loadClass(name, false);
  }

  /**
   * We override the parent-first behavior established by
   * java.lang.Classloader.
   * <p>
   * The implementation is surprisingly straightforward.
   */
  protected Class loadClass(String name, boolean resolve)
    throws ClassNotFoundException {

    //System.out.println("ChildFirstClassLoader("+name+", "+resolve+")");

    // First, check if the class has already been loaded
    Class c = findLoadedClass(name);

    // if not loaded, search the local (child) resources
    if (c == null) {
    	try {
        c = findClass(name);
    	} catch(ClassNotFoundException cnfe) {
    	  // ignore
    	} catch(SecurityException se) {
            // ignore
        }
    }

    // if we could not find it, delegate to parent
    // Note that we don't attempt to catch any ClassNotFoundException
    if (c == null) {
      if (getParent() != null) {
        c = getParent().loadClass(name);
      } else {
        c = getSystemClassLoader().loadClass(name);
      }
    }

    if (resolve) {
      resolveClass(c);
    }

    return c;
  }

  /**
   * Override the parent-first resource loading model established by
   * java.lang.Classloader with child-first behavior.
   */
  public URL getResource(String name) {
    URL url = findResource(name);

    // if local search failed, delegate to parent
    if(url == null) {
      url = getParent().getResource(name);
    }
    return url;
   }
}
