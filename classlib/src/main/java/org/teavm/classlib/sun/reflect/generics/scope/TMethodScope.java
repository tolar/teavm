/*    */ package org.teavm.classlib.sun.reflect.generics.scope;
/*    */ 
/*    */

import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.reflect.TMethod;

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
/*    */ public class TMethodScope
/*    */   extends TAbstractScope<TMethod>
/*    */ {
/*    */   private TMethodScope(TMethod paramMethod)
/*    */   {
/* 39 */     super(paramMethod);
/*    */   }
/*    */   
/*    */ 
/*    */   private TClass<?> getEnclosingClass()
/*    */   {
/* 45 */     return ((TMethod)getRecvr()).getDeclaringClass();
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
/*    */   public static TMethodScope make(TMethod paramMethod)
/*    */   {
/* 65 */     return new TMethodScope(paramMethod);
/*    */   }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\scope\MethodScope.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */