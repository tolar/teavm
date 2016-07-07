/*    */ package org.teavm.classlib.sun.reflect.generics.tree;
/*    */ 
/*    */ import org.teavm.classlib.sun.reflect.generics.visitor.TTypeTreeVisitor;
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
/*    */ public class TArrayTypeSignature
/*    */   implements TFieldTypeSignature
/*    */ {
/*    */   private final TTypeSignature componentType;
/*    */   
/*    */   private TArrayTypeSignature(TTypeSignature paramTypeSignature)
/*    */   {
/* 33 */     this.componentType = paramTypeSignature;
/*    */   }
/*    */   
/* 36 */   public static TArrayTypeSignature make(TTypeSignature paramTypeSignature) { return new TArrayTypeSignature(paramTypeSignature); }
/*    */   
/*    */   public TTypeSignature getComponentType() {
/* 39 */     return this.componentType;
/*    */   }
/*    */   
/* 42 */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) { paramTypeTreeVisitor.visitArrayTypeSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\ArrayTypeSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */