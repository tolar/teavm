package org.teavm.classlib.java.lang.reflect;

public abstract interface TParameterizedType
  extends TType
{
  public abstract TType[] getActualTypeArguments();
  
  public abstract TType getRawType();
  
  public abstract TType getOwnerType();
}


/* Location:              D:\programs\fernflower\rt.jar!\java\lang\reflect\ParameterizedType.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */