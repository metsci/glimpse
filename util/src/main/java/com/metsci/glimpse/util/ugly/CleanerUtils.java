package com.metsci.glimpse.util.ugly;

import static com.metsci.glimpse.util.ugly.UglyUtils.findClass;
import static com.metsci.glimpse.util.ugly.UglyUtils.firstSuccessfulReturnValue;
import static com.metsci.glimpse.util.ugly.UglyUtils.requireDeclaredMethod;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadFactory;

/**
 * Cleaners serve the same purpose as finalize() methods, with 2 subtle differences:
 * <ol>
 * <li>finalize() methods are better when resource disposal is non-trivial and/or slow
 * <li>The JVM does a better job of running Cleaners promptly
 * </ol>
 * <p>
 * When a registered Object becomes eligible to be GC-ed, its corresponding Cleaner gets
 * magically triggered via the JVM's PhantomReference mechanism.
 * <p>
 * It is easy to write code that uses Java 8 Cleaners, and it is easy to write code that
 * uses Java 9+ Cleaners. However, it is painful to write code that can use either Java 8
 * or Java 9+ Cleaners. The goal of this class is to encapsulate and hide that pain.
 * <p>
 * Java 9+ supports creation of Cleaners that use specific {@link ThreadFactory}s. That
 * capability is not supported by Java 8, and therefore not supported by this class.
 */
public class CleanerUtils
{

    /**
     * Like {@code java.lang.ref.Cleaner.Cleanable}, but available in Java 8.
     */
    public static interface Cleanable
    {
        void clean( );
    }

    protected static interface CleanerImpl
    {
        Cleanable register( Object obj, Runnable action );
    }

    /**
     * Cleaner impl for Java 8 JVMs.
     * <p>
     * Also works for Java 9+ JVMs when {@link CleanerUtils} is on the classpath (not the modulepath).
     */
    protected static class CleanerImpl1 implements CleanerImpl
    {
        protected final Method Cleaner_create;
        protected final Method Cleaner_clean;

        public CleanerImpl1( ) throws Exception
        {
            Class<?> Cleaner_class = findClass( "jdk.internal.ref.Cleaner", "sun.misc.Cleaner" );
            this.Cleaner_create = requireDeclaredMethod( Cleaner_class, "create", Object.class, Runnable.class );
            this.Cleaner_clean = requireDeclaredMethod( Cleaner_class, "clean" );
        }

        @Override
        public Cleanable register( Object obj, Runnable action )
        {
            try
            {
                Object cleaner = this.Cleaner_create.invoke( null, obj, action );
                return ( ) ->
                {
                    try
                    {
                        this.Cleaner_clean.invoke( cleaner );
                    }
                    catch ( ReflectiveOperationException e )
                    {
                        throw new RuntimeException( e );
                    }
                };
            }
            catch ( ReflectiveOperationException e )
            {
                throw new RuntimeException( e );
            }
        }
    }

    /**
     * Cleaner impl for Java 9+ JVMs.
     */
    protected static class CleanerImpl2 implements CleanerImpl
    {
        protected final Object cleaner;
        protected final Method Cleaner_register;
        protected final Method Cleanable_clean;

        public CleanerImpl2( ) throws Exception
        {
            Class<?> Cleaner_class = Class.forName( "java.lang.ref.Cleaner" );
            Method Cleaner_create = requireDeclaredMethod( Cleaner_class, "create" );
            this.cleaner = Cleaner_create.invoke( null );
            this.Cleaner_register = requireDeclaredMethod( Cleaner_class, "register", Object.class, Runnable.class );

            Class<?> Cleanable_class = Class.forName( "java.lang.ref.Cleaner$Cleanable" );
            this.Cleanable_clean = requireDeclaredMethod( Cleanable_class, "clean" );
        }

        @Override
        public Cleanable register( Object obj, Runnable action )
        {
            try
            {
                Object cleanable = this.Cleaner_register.invoke( this.cleaner, obj, action );
                return ( ) ->
                {
                    try
                    {
                        this.Cleanable_clean.invoke( cleanable );
                    }
                    catch ( ReflectiveOperationException e )
                    {
                        throw new RuntimeException( e );
                    }
                };
            }
            catch ( ReflectiveOperationException e )
            {
                throw new RuntimeException( e );
            }
        }
    }

    protected static final CleanerImpl cleanerImpl = firstSuccessfulReturnValue( CleanerImpl2::new,
                                                                                 CleanerImpl1::new );

    /**
     * <strong>IMPORTANT:</strong> {@code action} MUST NOT refer to {@code obj} ... if
     * {@code action} does hold a reference to {@code obj}, it will prevent {@code obj}
     * from becoming eligible for cleaning.
     * <p>
     * A good way to avoid accidental references to {@code obj} is to avoid using a lambda,
     * or even an inner class, for {@code action}. Use a static class instead:
     * <pre>
     * public class SomeClass
     * {
     *     // Static class -- not inner class, not lambda
     *     private static class CleaningAction implements Runnable
     *     {
     *         private final Object thingNeededDuringCleanup1;
     *         private final Object thingNeededDuringCleanup2;
     *
     *         public CleaningAction( Object thingNeededDuringCleanup1, Object thingNeededDuringCleanup2 )
     *         {
     *             this.thingNeededDuringCleanup1 = thingNeededDuringCleanup1;
     *             this.thingNeededDuringCleanup2 = thingNeededDuringCleanup2;
     *         }
     *
     *         {@literal @}Override
     *         public void run( )
     *         {
     *             // Do cleanup
     *         }
     *     }
     *
     *     private final Cleanable cleanable;
     *
     *     public SomeClass( )
     *     {
     *         this.cleanable = UglyUtils.registerCleaner( this, new CleaningAction( ... ) );
     *     }
     * }
     * </pre>
     */
    public static Cleanable registerCleaner( Object obj, Runnable action )
    {
        return cleanerImpl.register( obj, action );
    }

}
