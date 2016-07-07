/*    */ package org.teavm.classlib.sun.reflect.generics.tree;
/*    */ 
/*    */

import java.util.List;

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
/*    */ 
/*    */ 
/*    */ public class TClassTypeSignature
/*    */   implements TFieldTypeSignature
/*    */ {
/*    */   private final List<TSimpleClassTypeSignature> path;
/*    */   
/*    */   private TClassTypeSignature(List<TSimpleClassTypeSignature> paramList)
/*    */   {
/* 40 */     this.path = paramList;
/*    */   }
/*    */   
/*    */   public static TClassTypeSignature make(List<TSimpleClassTypeSignature> paramList) {
/* 44 */     return new TClassTypeSignature(paramList);
/*    */   }
/*    */   
/* 47 */   public List<TSimpleClassTypeSignature> getPath() { return this.path; }
/*    */   
/* 49 */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) { paramTypeTreeVisitor.visitClassTypeSignature(this); }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\ClassTypeSignature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */