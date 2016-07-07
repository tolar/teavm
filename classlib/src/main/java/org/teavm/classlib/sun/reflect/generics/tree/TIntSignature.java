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
/*    */ public class TIntSignature
/*    */   implements TBaseType
/*    */ {
/* 32 */   private static final TIntSignature singleton = new TIntSignature();
/*    */   
/*    */ 
/*    */ 
/* 36 */   public static TIntSignature make() { return singleton; }
/*    */   
/* 38 */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) { paramTypeTreeVisitor.visitIntSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\IntSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */