/*    */ package sun.reflect.generics.scope;
/*    */ 
/*    */

import java.lang.reflect.Method;

import org.teavm.classlib.sun.reflect.generics.scope.TAbstractScope;
import org.teavm.classlib.sun.reflect.generics.scope.TScope;

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
/*    */   extends TAbstractScope<Method>
/*    */ {
/*    */   private TMethodScope(Method paramMethod)
/*    */   {
/* 39 */     super(paramMethod);
/*    */   }
/*    */   
/*    */ 
/*    */   private Class<?> getEnclosingClass()
/*    */   {
/* 45 */     return ((Method)getRecvr()).getDeclaringClass();
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   protected TScope computeEnclosingScope()
/*    */   {
/* 55 */     return sun.reflect.generics.scope.TClassScope.make(getEnclosingClass());
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public static TMethodScope make(Method paramMethod)
/*    */   {
/* 65 */     return new TMethodScope(paramMethod);
/*    */   }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\scope\MethodScope.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */