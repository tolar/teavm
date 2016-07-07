/*    */ package org.teavm.classlib.sun.reflect.generics.tree;
/*    */ 
/*    */ import sun.reflect.generics.visitor.Visitor;
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
/*    */ public class TClassTSignature
/*    */   implements TSignature
/*    */ {
/*    */   private final TFormalTypeParameter[] formalTypeParams;
/*    */   private final TClassTypeSignature superclass;
/*    */   private final TClassTypeSignature[] superInterfaces;
/*    */   
/*    */   private TClassTSignature(TFormalTypeParameter[] paramArrayOfTFormalTypeParameter, TClassTypeSignature paramClassTypeSignature, TClassTypeSignature[] paramArrayOfClassTypeSignature)
/*    */   {
/* 38 */     this.formalTypeParams = paramArrayOfTFormalTypeParameter;
/* 39 */     this.superclass = paramClassTypeSignature;
/* 40 */     this.superInterfaces = paramArrayOfClassTypeSignature;
/*    */   }
/*    */   
/*    */ 
/*    */   public static TClassTSignature make(TFormalTypeParameter[] paramArrayOfTFormalTypeParameter, TClassTypeSignature paramClassTypeSignature, TClassTypeSignature[] paramArrayOfClassTypeSignature)
/*    */   {
/* 46 */     return new TClassTSignature(paramArrayOfTFormalTypeParameter, paramClassTypeSignature, paramArrayOfClassTypeSignature);
/*    */   }
/*    */   
/*    */ 
/* 50 */   public TFormalTypeParameter[] getFormalTypeParameters() { return this.formalTypeParams; }
/*    */   
/* 52 */   public TClassTypeSignature getSuperclass() { return this.superclass; }
/* 53 */   public TClassTypeSignature[] getSuperInterfaces() { return this.superInterfaces; }
/*    */   
/* 55 */   public void accept(Visitor<?> paramVisitor) { paramVisitor.visitClassSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\ClassSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */