package org.teavm.classlib.java.lang.reflect;


public abstract interface TTypeVariable<D extends TGenericDeclaration>
  extends TType, TAnnotatedElement
{
  public abstract TType[] getBounds();
  
  public abstract D getGenericDeclaration();
  
  public abstract String getName();
  
  public abstract TAnnotatedType[] getAnnotatedBounds();
}


/* Location:              D:\programs\fernflower\rt.jar!\java\lang\reflect\TypeVariable.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */