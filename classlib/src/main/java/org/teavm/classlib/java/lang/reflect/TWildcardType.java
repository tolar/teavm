package org.teavm.classlib.java.lang.reflect;

public abstract interface TWildcardType
  extends TType
{
  public abstract TType[] getUpperBounds();
  
  public abstract TType[] getLowerBounds();
}


/* Location:              D:\programs\fernflower\rt.jar!\java\lang\reflect\WildcardType.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */