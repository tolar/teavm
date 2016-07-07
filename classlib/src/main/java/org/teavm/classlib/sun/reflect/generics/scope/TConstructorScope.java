/*    */ package org.teavm.classlib.sun.reflect.generics.scope;
/*    */ 
/*    */

import java.lang.reflect.Constructor;

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
/*    */   extends TAbstractScope<Constructor<?>>
/*    */ {
/*    */   private TConstructorScope(Constructor<?> paramConstructor)
/*    */   {
/* 39 */     super(paramConstructor);
/*    */   }
/*    */   
/*    */ 
/*    */   private Class<?> getEnclosingClass()
/*    */   {
/* 45 */     return ((Constructor)getRecvr()).getDeclaringClass();
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
/*    */   public static TConstructorScope make(Constructor<?> paramConstructor)
/*    */   {
/* 65 */     return new TConstructorScope(paramConstructor);
/*    */   }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\scope\ConstructorScope.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */