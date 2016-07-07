package org.teavm.classlib.sun.reflect.generics.scope;

import org.teavm.classlib.java.lang.reflect.TTypeVariable;

public abstract interface TScope
{
  public abstract TTypeVariable<?> lookup(String paramString);
}


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\scope\Scope.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */