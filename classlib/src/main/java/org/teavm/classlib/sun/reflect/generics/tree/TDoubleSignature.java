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
/*    */ public class TDoubleSignature
/*    */   implements TBaseType
/*    */ {
/* 32 */   private static final TDoubleSignature singleton = new TDoubleSignature();
/*    */   
/*    */ 
/*    */ 
/* 36 */   public static TDoubleSignature make() { return singleton; }
/*    */   
/* 38 */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) { paramTypeTreeVisitor.visitDoubleSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\DoubleSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */