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
/*    */ public class TWildcard
/*    */   implements TTypeArgument
/*    */ {
/*    */   private TFieldTypeSignature[] upperBounds;
/*    */   private TFieldTypeSignature[] lowerBounds;
/*    */   
/*    */   private TWildcard(TFieldTypeSignature[] paramArrayOfFieldTypeSignature1, TFieldTypeSignature[] paramArrayOfFieldTypeSignature2)
/*    */   {
/* 35 */     this.upperBounds = paramArrayOfFieldTypeSignature1;
/* 36 */     this.lowerBounds = paramArrayOfFieldTypeSignature2;
/*    */   }
/*    */   
/* 39 */   private static final TFieldTypeSignature[] emptyBounds = new TFieldTypeSignature[0];
/*    */   
/*    */   public static TWildcard make(TFieldTypeSignature[] paramArrayOfFieldTypeSignature1, TFieldTypeSignature[] paramArrayOfFieldTypeSignature2)
/*    */   {
/* 43 */     return new TWildcard(paramArrayOfFieldTypeSignature1, paramArrayOfFieldTypeSignature2);
/*    */   }
/*    */   
/*    */   public TFieldTypeSignature[] getUpperBounds() {
/* 47 */     return this.upperBounds;
/*    */   }
/*    */   
/*    */   public TFieldTypeSignature[] getLowerBounds() {
/* 51 */     if ((this.lowerBounds.length == 1) && 
/* 52 */       (this.lowerBounds[0] == TBottomSignature.make())) {
/* 53 */       return emptyBounds;
/*    */     }
/* 55 */     return this.lowerBounds;
/*    */   }
/*    */   
/* 58 */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) { paramTypeTreeVisitor.visitWildcard(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\Wildcard.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */