/*    */ package org.teavm.classlib.sun.reflect.generics.scope;
/*    */

import org.teavm.classlib.java.lang.TClass;


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
/*    */   extends TAbstractScope<TClass<?>>
/*    */   implements TScope
/*    */ {
/*    */   private TClassScope(TClass<?> paramClass)
/*    */   {
/* 40 */     super(paramClass);
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */   protected TScope computeEnclosingScope()
/*    */   {
return null;
}
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public static TClassScope make(TClass<?> paramClass)
/*    */   {
/* 81 */     return new TClassScope(paramClass);
/*    */   }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\scope\ClassScope.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */