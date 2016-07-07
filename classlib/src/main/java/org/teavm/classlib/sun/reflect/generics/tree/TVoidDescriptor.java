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
/*    */ 
/*    */ 
/*    */ public class TVoidDescriptor
/*    */   implements TReturnType
/*    */ {
/* 33 */   private static final TVoidDescriptor singleton = new TVoidDescriptor();
/*    */   
/*    */   public static TVoidDescriptor make()
/*    */   {
/* 37 */     return singleton;
/*    */   }
/*    */   
/*    */   public void accept(TTypeTreeVisitor<?> paramTypeTreeVisitor) {
/* 41 */     paramTypeTreeVisitor.visitVoidDescriptor(this);
/*    */   }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\tree\VoidDescriptor.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */