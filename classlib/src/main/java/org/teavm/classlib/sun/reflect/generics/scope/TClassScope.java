/*    */ package org.teavm.classlib.sun.reflect.generics.scope;
/*    */

import org.teavm.classlib.java.lang.reflect.TConstructor;
import org.teavm.classlib.java.lang.reflect.TMethod;

import sun.reflect.generics.scope.TDummyScope;

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
/*    */ public class TClassScope
/*    */   extends TAbstractScope<Class<?>>
/*    */   implements TScope
/*    */ {
/*    */   private TClassScope(Class<?> paramClass)
/*    */   {
/* 40 */     super(paramClass);
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */   protected TScope computeEnclosingScope()
/*    */   {
/* 48 */     Class localClass1 = (Class)getRecvr();
/*    */     
/* 50 */     TMethod localMethod = localClass1.getEnclosingMethod();
/* 51 */     if (localMethod != null)
/*    */     {
/*    */ 
/* 54 */       return sun.reflect.generics.scope.TMethodScope.make(localMethod);
/*    */     }
/* 56 */     TConstructor localConstructor = localClass1.getEnclosingConstructor();
/* 57 */     if (localConstructor != null)
/*    */     {
/*    */ 
/* 60 */       return TConstructorScope.make(localConstructor);
/*    */     }
/* 62 */     Class localClass2 = localClass1.getEnclosingClass();
/*    */     
/*    */ 
/* 65 */     if (localClass2 != null)
/*    */     {
/*    */ 
/* 68 */       return make(localClass2);
/*    */     }
/*    */     
/*    */ 
/* 72 */     return TDummyScope.make();
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public static TClassScope make(Class<?> paramClass)
/*    */   {
/* 81 */     return new TClassScope(paramClass);
/*    */   }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\scope\ClassScope.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */