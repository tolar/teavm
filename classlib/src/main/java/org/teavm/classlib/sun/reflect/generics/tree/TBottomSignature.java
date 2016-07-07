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
/*    */ public class TBottomSignature
/*    */   implements TFieldTypeSignature
/*    */ {
/* 31 */   private static final TBottomSignature singleton = new TBottomSignature();
/*    */   
/*    */ 
/*    */ 
/* 35 */   public static TBottomSignature make() { return singleton; }
/*    */   
/* 37 */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) { paramTypeTreeVisitor.visitBottomSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\BottomSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */