/*    */ package org.teavm.classlib.sun.reflect.generics.scope;
/*    */ 
/*    */

import org.teavm.classlib.java.lang.reflect.TGenericDeclaration;
import org.teavm.classlib.java.lang.reflect.TTypeVariable;

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
/*    */ public abstract class TAbstractScope<D extends TGenericDeclaration>
/*    */   implements TScope
/*    */ {
/*    */   private final D recvr;
/*    */   private volatile TScope enclosingScope;
/*    */   
/*    */   protected TAbstractScope(D paramD)
/*    */   {
/* 55 */     this.recvr = paramD;
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */   protected D getRecvr()
/*    */   {
/* 62 */     return this.recvr;
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   protected abstract TScope computeEnclosingScope();
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */   protected TScope getEnclosingScope()
/*    */   {
/* 76 */     TScope localScope = this.enclosingScope;
/* 77 */     if (localScope == null) {
/* 78 */       localScope = computeEnclosingScope();
/* 79 */       this.enclosingScope = localScope;
/*    */     }
/* 81 */     return localScope;
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public TTypeVariable<?> lookup(String paramString)
/*    */   {
/* 92 */     TTypeVariable[] arrayOfTypeVariable1 = getRecvr().getTypeParameters();
/* 93 */     for (TTypeVariable localTypeVariable : arrayOfTypeVariable1) {
/* 94 */       if (localTypeVariable.getName().equals(paramString)) return localTypeVariable;
/*    */     }
/* 96 */     return getEnclosingScope().lookup(paramString);
/*    */   }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\scope\AbstractScope.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */