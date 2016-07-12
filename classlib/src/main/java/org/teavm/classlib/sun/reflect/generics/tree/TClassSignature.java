/*    */ package org.teavm.classlib.sun.reflect.generics.tree;

import org.teavm.classlib.sun.reflect.generics.visitor.TVisitor;

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
/*    */ public class TClassSignature
/*    */   implements TSignature
/*    */ {
/*    */   private final TFormalTypeParameter[] formalTypeParams;
/*    */   private final TClassTypeSignature superclass;
/*    */   private final TClassTypeSignature[] superInterfaces;
/*    */   
/*    */   private TClassSignature(TFormalTypeParameter[] paramArrayOfTFormalTypeParameter, TClassTypeSignature paramClassTypeSignature, TClassTypeSignature[] paramArrayOfClassTypeSignature)
/*    */   {
/* 38 */     this.formalTypeParams = paramArrayOfTFormalTypeParameter;
/* 39 */     this.superclass = paramClassTypeSignature;
/* 40 */     this.superInterfaces = paramArrayOfClassTypeSignature;
/*    */   }
/*    */   
/*    */ 
/*    */   public static TClassSignature make(TFormalTypeParameter[] paramArrayOfTFormalTypeParameter, TClassTypeSignature paramClassTypeSignature, TClassTypeSignature[] paramArrayOfClassTypeSignature)
/*    */   {
/* 46 */     return new TClassSignature(paramArrayOfTFormalTypeParameter, paramClassTypeSignature, paramArrayOfClassTypeSignature);
/*    */   }
/*    */   
/*    */ 
/* 50 */   public TFormalTypeParameter[] getFormalTypeParameters() { return this.formalTypeParams; }
/*    */   
/* 52 */   public TClassTypeSignature getSuperclass() { return this.superclass; }
/* 53 */   public TClassTypeSignature[] getSuperInterfaces() { return this.superInterfaces; }
/*    */   
/* 55 */   public void accept(TVisitor<?> paramVisitor) { paramVisitor.visitClassSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\ClassSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */