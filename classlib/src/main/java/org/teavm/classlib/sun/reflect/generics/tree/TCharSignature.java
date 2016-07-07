/*    */ package org.teavm.classlib.sun.reflect.generics.tree;

import org.teavm.classlib.sun.reflect.generics.visitor.TTypeTreeVisitor;

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
/*    */ public class TCharSignature
/*    */   implements TBaseType
/*    */ {
/* 32 */   private static final TCharSignature singleton = new TCharSignature();
/*    */   
/*    */   public static TCharSignature make()
/*    */   {
/* 36 */     return singleton;
/*    */   }
/*    */   
/* 39 */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) { paramTypeTreeVisitor.visitCharSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\CharSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */