#include <windows.h>
#include <TlHelp32.h>
#include <unordered_set>
#include "com_metsci_glimpse_support_wheelfix_WheelFix.h"


JNIEnv *_env;

void jprintln( char *message )
{
    jclass systemClass = _env->FindClass( "java/lang/System" );
    jfieldID systemErrField = _env->GetStaticFieldID( systemClass, "err", "Ljava/io/PrintStream;" );
    jobject systemErr = _env->GetStaticObjectField( systemClass, systemErrField );
    jclass systemErrClass = _env->GetObjectClass( systemErr );
    jmethodID printlnMethod = _env->GetMethodID( systemErrClass, "println", "(Ljava/lang/String;)V" );

    jstring s = _env->NewStringUTF( message );

    _env->CallVoidMethod( systemErr, printlnMethod, s );

    _env->DeleteLocalRef( s );
    _env->DeleteLocalRef( systemErrClass );
    _env->DeleteLocalRef( systemErr );
    _env->DeleteLocalRef( systemClass );
}

void jprintlnLong( jlong longVal )
{
    jclass systemClass = _env->FindClass( "java/lang/System" );
    jfieldID systemErrField = _env->GetStaticFieldID( systemClass, "err", "Ljava/io/PrintStream;" );
    jobject systemErr = _env->GetStaticObjectField( systemClass, systemErrField );
    jclass systemErrClass = _env->GetObjectClass( systemErr );
    jmethodID printlnMethod = _env->GetMethodID( systemErrClass, "println", "(J)V" );

    _env->CallVoidMethod( systemErr, printlnMethod, longVal );

    _env->DeleteLocalRef( systemErrClass );
    _env->DeleteLocalRef( systemErr );
    _env->DeleteLocalRef( systemClass );
}


jstring getErrorString( JNIEnv *env, jint errorCode )
{
    LPVOID buf;
    FormatMessage( FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
                   NULL,
                   errorCode,
                   MAKELANGID( LANG_NEUTRAL, SUBLANG_DEFAULT ),
                   ( LPTSTR ) &buf,
                   0,
                   NULL );
    jstring errorString = env->NewStringUTF( ( LPTSTR ) buf );
    LocalFree( buf );
    return errorString;
}


/*
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
*/

LRESULT CALLBACK hookProc( int code, WPARAM wParam, LPARAM lParam )
{
/*
    MSG *msg = ( MSG * ) lParam;
    if ( code >= 0 && msg->message == WM_MOUSEWHEEL )
    {
        HWND hwndHovered = WindowFromPoint( msg->pt );
        if ( msg->hwnd != hwndHovered )
        {
            // Post an equivalent message to the hovered window
            if ( hwndHovered != NULL )
            {
                DWORD pidCurrent = GetCurrentProcessId( );
                DWORD pidHovered;
                GetWindowThreadProcessId( hwndHovered, &pidHovered );
                if ( pidHovered == pidCurrent )
                {
                    MOUSEHOOKSTRUCTEX *infoEx = ( MOUSEHOOKSTRUCTEX * ) msg->lParam;
                    PostMessage( hwndHovered, WM_MOUSEWHEEL, infoEx->mouseData, MAKELPARAM( msg->pt.x, msg->pt.y ) );
                }
            }
            
            // Squash the original message
            msg->message = WM_NULL;
        }
    }
    */
    return CallNextHookEx( NULL, code, wParam, lParam );
}



HANDLE _mutex = CreateMutex( NULL, FALSE, NULL );
std::unordered_set<DWORD> _threadsWithFix;


jstring JNICALL Java_com_metsci_glimpse_support_wheelfix_WheelFix__1activateWheelFix( JNIEnv *env, jclass jthis )
{
    // XXX: Hack for println
    _env = env;

    DWORD pid = GetCurrentProcessId( );
    
    HANDLE hThreadSnap = CreateToolhelp32Snapshot( TH32CS_SNAPTHREAD, 0 );
    if ( hThreadSnap == INVALID_HANDLE_VALUE )
    {
        return env->NewStringUTF( "Failed to create thread snapshot" );
    }
    
    THREADENTRY32 entry;
    entry.dwSize = sizeof( THREADENTRY32 );
    
    if( !Thread32First( hThreadSnap, &entry ) ) 
    {
        jstring errorString = getErrorString( env, GetLastError( ) );
        CloseHandle( hThreadSnap );
        return errorString;
    }
    
    do
    {
        if ( entry.th32OwnerProcessID == pid )
        {
            WaitForSingleObject( _mutex, INFINITE );
            {
                DWORD threadID = entry.th32ThreadID;
                if ( _threadsWithFix.find( threadID ) == _threadsWithFix.end( ) )
                {
                    HHOOK hHook = SetWindowsHookEx( WH_GETMESSAGE, hookProc, NULL, threadID );
                    if ( hHook == NULL )
                    {
                        jstring errorString = getErrorString( env, GetLastError( ) );
                        ReleaseMutex( _mutex );
                        CloseHandle( hThreadSnap );
                        return errorString;
                    }
                    
                    _threadsWithFix.insert( threadID );
                }
            }
            ReleaseMutex( _mutex );
        }
    } while ( Thread32Next( hThreadSnap, &entry ) );
    
    CloseHandle( hThreadSnap );
    return NULL;
}
