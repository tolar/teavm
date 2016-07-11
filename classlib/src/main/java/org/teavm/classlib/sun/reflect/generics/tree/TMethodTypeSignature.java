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
/*    */ 
/*    */ public class TMethodTypeSignature
/*    */   implements TSignature
/*    */ {
/*    */   private final TFormalTypeParameter[] formalTypeParams;
/*    */   private final TTypeSignature[] parameterTypes;
/*    */   private final TReturnType returnType;
/*    */   private final TFieldTypeSignature[] exceptionTypes;
/*    */   
/*    */   private TMethodTypeSignature(TFormalTypeParameter[] paramArrayOfTFormalTypeParameter, TTypeSignature[] paramArrayOfTypeSignature, TReturnType paramReturnType, TFieldTypeSignature[] paramArrayOfFieldTypeSignature)
/*    */   {
/* 40 */     this.formalTypeParams = paramArrayOfTFormalTypeParameter;
/* 41 */     this.parameterTypes = paramArrayOfTypeSignature;
/* 42 */     this.returnType = paramReturnType;
/* 43 */     this.exceptionTypes = paramArrayOfFieldTypeSignature;
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */   public static TMethodTypeSignature make(TFormalTypeParameter[] paramArrayOfTFormalTypeParameter, TTypeSignature[] paramArrayOfTypeSignature, TReturnType paramReturnType, TFieldTypeSignature[] paramArrayOfFieldTypeSignature)
/*    */   {
/* 50 */     return new TMethodTypeSignature(paramArrayOfTFormalTypeParameter, paramArrayOfTypeSignature, paramReturnType, paramArrayOfFieldTypeSignature);
/*    */   }
/*    */   
/*    */ 
/* 54 */   public TFormalTypeParameter[] getFormalTypeParameters() { return this.formalTypeParams; }
/*    */   
/* 56 */   public TTypeSignature[] getParameterTypes() { return this.parameterTypes; }
/* 57 */   public TReturnType getReturnType() { return this.returnType; }
/* 58 */   public TFieldTypeSignature[] getExceptionTypes() { return this.exceptionTypes; }
/*    */   
/* 60 */   public void accept(TVisitor<?> paramVisitor) { paramVisitor.visitMethodTypeSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\MethodTypeSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */