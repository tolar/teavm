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
/*    */ public class TTypeVariableSignature
/*    */   implements TFieldTypeSignature
/*    */ {
/*    */   private final String identifier;
/*    */   
/*    */   private TTypeVariableSignature(String paramString)
/*    */   {
/* 33 */     this.identifier = paramString;
/*    */   }
/*    */   
/*    */   public static TTypeVariableSignature make(String paramString) {
/* 37 */     return new TTypeVariableSignature(paramString);
/*    */   }
/*    */   
/* 40 */   public String getIdentifier() { return this.identifier; }
/*    */   
/*    */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) {
/* 43 */     paramTypeTreeVisitor.visitTypeVariableSignature(this);
/*    */   }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\TypeVariableSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */