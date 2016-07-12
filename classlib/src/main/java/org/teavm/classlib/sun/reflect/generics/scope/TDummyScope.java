/*    */ package org.teavm.classlib.sun.reflect.generics.scope;
/*    */ 
/*    */

import org.teavm.classlib.java.lang.reflect.TTypeVariable;

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
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class TDummyScope
/*    */   implements TScope
/*    */ {
/* 41 */   private static final TDummyScope singleton = new TDummyScope();
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public static TDummyScope make()
/*    */   {
/* 51 */     return singleton;
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public TTypeVariable<?> lookup(String paramString)
/*    */   {
/* 60 */     return null;
/*    */   }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\scope\DummyScope.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */