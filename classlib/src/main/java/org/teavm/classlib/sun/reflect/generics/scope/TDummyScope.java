/*    */ package sun.reflect.generics.scope;
/*    */ 
/*    */ import java.lang.reflect.TypeVariable;
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
/*    */ public class TDummyScope
/*    */   implements Scope
/*    */ {
/* 41 */   private static final TDummyScope singleton = new TDummyScope();
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public static TDummyScope make()
/*    */   {
/* 51 */     return singleton;
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public TypeVariable<?> lookup(String paramString)
/*    */   {
/* 60 */     return null;
/*    */   }
/*    */ }


/* Location:              D:\programs\fernflower\rt.jar!\sun\reflect\generics\scope\DummyScope.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */