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
/*    */ public class TShortSignature
/*    */   implements TBaseType
/*    */ {
/* 32 */   private static final TShortSignature singleton = new TShortSignature();
/*    */   
/*    */   public static TShortSignature make()
/*    */   {
/* 36 */     return singleton;
/*    */   }
/*    */   
/* 39 */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) { paramTypeTreeVisitor.visitShortSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\ShortSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */