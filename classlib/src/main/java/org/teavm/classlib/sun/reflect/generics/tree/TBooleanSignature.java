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
/*    */ public class TBooleanSignature
/*    */   implements TBaseType
/*    */ {
/* 32 */   private static final TBooleanSignature singleton = new TBooleanSignature();
/*    */   
/*    */   public static TBooleanSignature make()
/*    */   {
/* 36 */     return singleton;
/*    */   }
/*    */   
/* 39 */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) { paramTypeTreeVisitor.visitBooleanSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\BooleanSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */