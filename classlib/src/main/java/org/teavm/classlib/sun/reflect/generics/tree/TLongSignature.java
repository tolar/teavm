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
/*    */ public class TLongSignature
/*    */   implements TBaseType
/*    */ {
/* 32 */   private static final TLongSignature singleton = new TLongSignature();
/*    */   
/*    */ 
/*    */ 
/* 36 */   public static TLongSignature make() { return singleton; }
/*    */   
/* 38 */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) { paramTypeTreeVisitor.visitLongSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\LongSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */