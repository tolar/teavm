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
/*    */ public class TByteSignature
/*    */   implements TBaseType
/*    */ {
/* 32 */   private static final TByteSignature singleton = new TByteSignature();
/*    */   
/*    */   public static TByteSignature make()
/*    */   {
/* 36 */     return singleton;
/*    */   }
/*    */   
/* 39 */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) { paramTypeTreeVisitor.visitByteSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\ByteSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */