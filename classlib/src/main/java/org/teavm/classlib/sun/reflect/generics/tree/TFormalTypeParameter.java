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
/*    */ 
/*    */ 
/*    */ public class TFormalTypeParameter
/*    */   implements TTypeTree
/*    */ {
/*    */   private final String name;
/*    */   private final TFieldTypeSignature[] bounds;
/*    */   
/*    */   private TFormalTypeParameter(String paramString, TFieldTypeSignature[] paramArrayOfFieldTypeSignature)
/*    */   {
/* 36 */     this.name = paramString;
/* 37 */     this.bounds = paramArrayOfFieldTypeSignature;
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public static TFormalTypeParameter make(String paramString, TFieldTypeSignature[] paramArrayOfFieldTypeSignature)
/*    */   {
/* 48 */     return new TFormalTypeParameter(paramString, paramArrayOfFieldTypeSignature);
/*    */   }
/*    */   
/* 51 */   public TFieldTypeSignature[] getBounds() { return this.bounds; }
/* 52 */   public String getName() { return this.name; }
/*    */   
/* 54 */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) { paramTypeTreeVisitor.visitFormalTypeParameter(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\FormalTypeParameter.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */