/*    */ package org.teavm.classlib.sun.reflect.generics.scope;
/*    */ 
/*    */

import org.teavm.classlib.java.lang.reflect.TConstructor;

/*    */
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class TConstructorScope
/*    */   extends TAbstractScope<TConstructor<?>>
/*    */ {
/*    */   private TConstructorScope(TConstructor<?> paramConstructor)
/*    */   {
/* 39 */     super(paramConstructor);
/*    */   }
/*    */   
/*    */ 
/*    */   private Class<?> getEnclosingClass()
/*    */   {
/* 45 */     return ((TConstructor)getRecvr()).getDeclaringClass();
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   protected TScope computeEnclosingScope()
/*    */   {
/* 55 */     return TClassScope.make(getEnclosingClass());
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public static TConstructorScope make(TConstructor<?> paramConstructor)
/*    */   {
/* 65 */     return new TConstructorScope(paramConstructor);
/*    */   }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\scope\ConstructorScope.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */