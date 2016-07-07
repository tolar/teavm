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
/*    */ public class TSimpleClassTypeSignature
/*    */   implements TFieldTypeSignature
/*    */ {
/*    */   private final boolean dollar;
/*    */   private final String name;
/*    */   private final TTypeArgument[] typeArgs;
/*    */   
/*    */   private TSimpleClassTypeSignature(String paramString, boolean paramBoolean, TTypeArgument[] paramArrayOfTTypeArgument)
/*    */   {
/* 36 */     this.name = paramString;
/* 37 */     this.dollar = paramBoolean;
/* 38 */     this.typeArgs = paramArrayOfTTypeArgument;
/*    */   }
/*    */   
/*    */ 
/*    */   public static TSimpleClassTypeSignature make(String paramString, boolean paramBoolean, TTypeArgument[] paramArrayOfTTypeArgument)
/*    */   {
/* 44 */     return new TSimpleClassTypeSignature(paramString, paramBoolean, paramArrayOfTTypeArgument);
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/* 53 */   public boolean getDollar() { return this.dollar; }
/* 54 */   public String getName() { return this.name; }
/* 55 */   public TTypeArgument[] getTypeArguments() { return this.typeArgs; }
/*    */   
/*    */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) {
/* 58 */     paramTypeTreeVisitor.visitSimpleClassTypeSignature(this);
/*    */   }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\SimpleClassTypeSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */