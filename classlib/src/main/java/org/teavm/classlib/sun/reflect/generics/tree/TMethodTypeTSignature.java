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
/*    */ 
/*    */ public class TMethodTypeTSignature
/*    */   implements TSignature
/*    */ {
/*    */   private final TFormalTypeParameter[] formalTypeParams;
/*    */   private final TypeSignature[] parameterTypes;
/*    */   private final ReturnType returnType;
/*    */   private final TFieldTypeSignatureT[] exceptionTypes;
/*    */   
/*    */   private TMethodTypeTSignature(TFormalTypeParameter[] paramArrayOfTFormalTypeParameter, TypeSignature[] paramArrayOfTypeSignature, ReturnType paramReturnType, TFieldTypeSignatureT[] paramArrayOfFieldTypeSignature)
/*    */   {
/* 40 */     this.formalTypeParams = paramArrayOfTFormalTypeParameter;
/* 41 */     this.parameterTypes = paramArrayOfTypeSignature;
/* 42 */     this.returnType = paramReturnType;
/* 43 */     this.exceptionTypes = paramArrayOfFieldTypeSignature;
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */   public static TMethodTypeTSignature make(TFormalTypeParameter[] paramArrayOfTFormalTypeParameter, TypeSignature[] paramArrayOfTypeSignature, ReturnType paramReturnType, TFieldTypeSignatureT[] paramArrayOfFieldTypeSignature)
/*    */   {
/* 50 */     return new TMethodTypeTSignature(paramArrayOfTFormalTypeParameter, paramArrayOfTypeSignature, paramReturnType, paramArrayOfFieldTypeSignature);
/*    */   }
/*    */   
/*    */ 
/* 54 */   public TFormalTypeParameter[] getFormalTypeParameters() { return this.formalTypeParams; }
/*    */   
/* 56 */   public TypeSignature[] getParameterTypes() { return this.parameterTypes; }
/* 57 */   public ReturnType getReturnType() { return this.returnType; }
/* 58 */   public TFieldTypeSignatureT[] getExceptionTypes() { return this.exceptionTypes; }
/*    */   
/* 60 */   public void accept(Visitor<?> paramVisitor) { paramVisitor.visitMethodTypeSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\MethodTypeSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */