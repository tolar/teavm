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
/*    */ public class TFloatSignature
/*    */   implements TBaseType
/*    */ {
/* 32 */   private static final TFloatSignature singleton = new TFloatSignature();
/*    */   
/*    */ 
/*    */ 
/* 36 */   public static TFloatSignature make() { return singleton; }
/*    */   
/* 38 */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) { paramTypeTreeVisitor.visitFloatSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\FloatSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */