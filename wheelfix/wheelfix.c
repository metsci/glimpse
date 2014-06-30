#include <windows.h>
#include "com_metsci_glimpse_WheelFix.h"


/*
JNIEnv *_env;

void jprintln( char *message )
{
    jclass systemClass = (*_env)->FindClass( _env, "java/lang/System" );
    jfieldID systemErrField = (*_env)->GetStaticFieldID( _env, systemClass, "err", "Ljava/io/PrintStream;" );
    jobject systemErr = (*_env)->GetStaticObjectField( _env, systemClass, systemErrField );
    jclass systemErrClass = (*_env)->GetObjectClass( _env, systemErr );
    jmethodID printlnMethod = (*_env)->GetMethodID( _env, systemErrClass, "println", "(Ljava/lang/String;)V" );

    jstring s = (*_env)->NewStringUTF( _env, message );

    (*_env)->CallVoidMethod( _env, systemErr, printlnMethod, s );

    (*_env)->DeleteLocalRef( _env, s );
    (*_env)->DeleteLocalRef( _env, systemErrClass );
    (*_env)->DeleteLocalRef( _env, systemErr );
    (*_env)->DeleteLocalRef( _env, systemClass );
}

void jprintlnLong( jlong longVal )
{
    jclass systemClass = (*_env)->FindClass( _env, "java/lang/System" );
    jfieldID systemErrField = (*_env)->GetStaticFieldID( _env, systemClass, "err", "Ljava/io/PrintStream;" );
    jobject systemErr = (*_env)->GetStaticObjectField( _env, systemClass, systemErrField );
    jclass systemErrClass = (*_env)->GetObjectClass( _env, systemErr );
    jmethodID printlnMethod = (*_env)->GetMethodID( _env, systemErrClass, "println", "(J)V" );

    (*_env)->CallVoidMethod( _env, systemErr, printlnMethod, longVal );

    (*_env)->DeleteLocalRef( _env, systemErrClass );
    (*_env)->DeleteLocalRef( _env, systemErr );
    (*_env)->DeleteLocalRef( _env, systemClass );
}
*/

LRESULT CALLBACK hookProc( int nCode, WPARAM wParam, LPARAM lParam )
{
    if ( nCode >= 0 && wParam == WM_MOUSEWHEEL )
    {
        MOUSEHOOKSTRUCT *info = ( MOUSEHOOKSTRUCT * ) lParam;
        HWND hwndOrig = info->hwnd;
        HWND hwndHovered = WindowFromPoint( info->pt );
        if ( hwndOrig != hwndHovered )
        {
            // Post an equivalent message to the hovered window
            if ( hwndHovered != NULL )
            {
                DWORD pidCurrent = GetCurrentProcessId( );
                DWORD pidHovered;
                GetWindowThreadProcessId( hwndHovered, &pidHovered );
                if ( pidHovered == pidCurrent )
                {
                    MOUSEHOOKSTRUCTEX *infoEx = ( MOUSEHOOKSTRUCTEX * ) lParam;
                    PostMessage( hwndHovered, WM_MOUSEWHEEL, infoEx->mouseData, MAKELPARAM( info->pt.x, info->pt.y ) );
                }
            }
            
            // Drop the original message
            return 1;
        }
    }
    return CallNextHookEx( NULL, nCode, wParam, lParam );
}


jint JNICALL Java_com_metsci_glimpse_WheelFix__1activateWheelFix( JNIEnv *env, jclass this, jlongArray result )
{
    // XXX: Hack for println
    //_env = env;

    HHOOK hHook = SetWindowsHookEx( WH_MOUSE, hookProc, NULL, GetCurrentThreadId( ) );
    ( *env )->SetLongArrayRegion( env, result, 0, 1, ( jlong * ) &hHook );
    return ( hHook == NULL ? GetLastError( ) : 0 );
}


jint JNICALL Java_com_metsci_glimpse_WheelFix__1deactivateWheelFix( JNIEnv *env, jclass this, jlong hHook )
{
    BOOL success = UnhookWindowsHookEx( ( HHOOK ) hHook );
    return ( success ? 0 : GetLastError( ) );
}

jstring JNICALL Java_com_metsci_glimpse_WheelFix__1getErrorString( JNIEnv *env, jclass this, jint errorCode )
{
    LPVOID buf;
    FormatMessage( FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
                   NULL,
                   errorCode,
                   MAKELANGID( LANG_NEUTRAL, SUBLANG_DEFAULT ),
                   ( LPTSTR ) &buf,
                   0,
                   NULL );
    jstring errorString = ( *env )->NewStringUTF( env, buf );
    LocalFree( buf );
    return errorString;
}
