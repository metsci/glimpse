#include <windows.h>
#include <winuser.h>

// Even if WINVER isn't set high enough for H-WHEEL to be
// defined, the way we use it (to check event-type) is still
// safe -- so just define it ourselves
#ifndef WM_MOUSEHWHEEL
#define WM_MOUSEHWHEEL 0x020E
#endif

#include <atomic>
#include <unordered_set>
#include "com_metsci_glimpse_platformFixes_WindowsFixes.h"



//#define DEBUG



#ifdef DEBUG
std::atomic<HANDLE> _logMutex;
std::atomic<FILE *> _log;
void log( const char *fmt, ... )
{
    va_list args;
    va_start( args, fmt );

    WaitForSingleObject( _logMutex, INFINITE );
    {
        vfprintf( _log, fmt, args );
        fflush( _log );
    }
    ReleaseMutex( _logMutex );

    va_end( args );
}
#endif



jstring getErrorString( JNIEnv *env, jint errorCode );
jclass findClassGlobal( JNIEnv *env, const char *classname );
JNIEnv *getJavaEnv( );
BOOL CALLBACK setHooksIfNeeded( HWND hwnd, LPARAM lParam );
LRESULT CALLBACK getMsgProc( int nCode, WPARAM wParam, LPARAM lParam );
LRESULT CALLBACK callWndProc( int nCode, WPARAM wParam, LPARAM lParam );



std::atomic<DWORD> _pid;
std::atomic<JavaVM *> _jvm;

std::atomic<jclass> _windowsFixesClass;
std::atomic<jmethodID> _handleVerticalMaximizeMID;

std::atomic<HANDLE> _threadsWithFixMutex;
std::unordered_set<DWORD> _threadsWithFix;




JNIEXPORT jint JNICALL JNI_OnLoad( JavaVM *jvm, void *reserved )
{
    #ifdef DEBUG
    _logMutex = CreateMutex( NULL, FALSE, NULL );
    _log = fopen( "log_windowsFixes.txt", "w" );
    #endif

    _pid = GetCurrentProcessId( );
    _jvm = jvm;
    _threadsWithFixMutex = CreateMutex( NULL, FALSE, NULL );

    JNIEnv *env;
    jvm->GetEnv( ( void ** ) &env, JNI_VERSION_1_6 );
    if ( env )
    {
        _windowsFixesClass = findClassGlobal( env, "com/metsci/glimpse/platformFixes/WindowsFixes" );
        _handleVerticalMaximizeMID = env->GetStaticMethodID( _windowsFixesClass, "handleVerticalMaximize", "(J)V" );
    }
    else
    {
        _windowsFixesClass = NULL;
        _handleVerticalMaximizeMID = NULL;
    }

    return JNI_VERSION_1_6;
}



JNIEXPORT jstring JNICALL Java_com_metsci_glimpse_platformFixes_WindowsFixes__1applyFixes( JNIEnv *env, jclass jthis )
{
    BOOL success = EnumWindows( setHooksIfNeeded, ( LPARAM ) NULL );
    return ( success ? NULL : getErrorString( env, GetLastError( ) ) );
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



jclass findClassGlobal( JNIEnv *env, const char *classname )
{
    jclass classLocal = env->FindClass( classname );
    if ( classLocal )
    {
        jclass classGlobal = ( jclass ) env->NewGlobalRef( classLocal );
        env->DeleteLocalRef( classLocal );
        return classGlobal;
    }
    else
    {
        return NULL;
    }
}



JNIEnv *getJavaEnv( )
{
    JNIEnv *env = NULL;
    JavaVM *jvm = _jvm;
    if ( jvm )
    {
        if ( jvm->GetEnv( ( void ** ) &env, JNI_VERSION_1_6 ) == JNI_EDETACHED )
        {
            jvm->AttachCurrentThreadAsDaemon( ( void ** ) &env, NULL );
        }
    }
    return env;
}



BOOL CALLBACK setHooksIfNeeded( HWND hwnd, LPARAM lParam )
{
    DWORD pidHwnd;
    DWORD tidHwnd = GetWindowThreadProcessId( hwnd, &pidHwnd );
    if ( pidHwnd == _pid )
    {
        WaitForSingleObject( _threadsWithFixMutex, INFINITE );
        {
            if ( _threadsWithFix.find( tidHwnd ) == _threadsWithFix.end( ) )
            {
                HHOOK hGetMsgHook = SetWindowsHookEx( WH_GETMESSAGE, getMsgProc, NULL, tidHwnd );
                if ( hGetMsgHook == NULL )
                {
                    ReleaseMutex( _threadsWithFixMutex );
                    return FALSE;
                }

                HHOOK hCallWndHook = SetWindowsHookEx( WH_CALLWNDPROC, callWndProc, NULL, tidHwnd );
                if ( hCallWndHook == NULL )
                {
                    // XXX: This unhook probably trashes the last-error code
                    UnhookWindowsHookEx( hGetMsgHook );
                    ReleaseMutex( _threadsWithFixMutex );
                    return FALSE;
                }

                _threadsWithFix.insert( tidHwnd );
            }
        }
        ReleaseMutex( _threadsWithFixMutex );
    }
    return TRUE;
}



LRESULT CALLBACK getMsgProc( int nCode, WPARAM wParam, LPARAM lParam )
{
    // Special case -- handle up here so it doesn't get messed with accidentally
    if ( nCode < 0 )
    {
        return CallNextHookEx( NULL, nCode, wParam, lParam );
    }

    MSG *msg = ( MSG * ) lParam;
    if ( nCode == HC_ACTION && ( msg->message == WM_MOUSEWHEEL || msg->message == WM_MOUSEHWHEEL ) )
    {
        HWND hwndDest = NULL;

        // When dragging, wheel events go to the capture window
        DWORD tid = GetWindowThreadProcessId( msg->hwnd, NULL );
        if ( tid == GetCurrentThreadId( ) )
        {
            hwndDest = GetCapture( );
        }
        else
        {
            GUITHREADINFO guiThreadInfo;
            guiThreadInfo.cbSize = sizeof( GUITHREADINFO );
            if ( GetGUIThreadInfo( tid, &guiThreadInfo ) )
            {
                hwndDest = guiThreadInfo.hwndCapture;
            }
        }

        // Otherwise, wheel events go to the hovered window
        if ( !hwndDest )
        {
            hwndDest = WindowFromPoint( msg->pt );
        }

        if ( msg->hwnd != hwndDest )
        {
            // Post an equivalent message to the destination window
            if ( hwndDest )
            {
                DWORD pidHovered;
                GetWindowThreadProcessId( hwndDest, &pidHovered );
                if ( pidHovered == _pid )
                {
                    PostMessage( hwndDest, msg->message, msg->wParam, msg->lParam );
                }
            }

            // Squash the original message
            msg->message = WM_NULL;
        }
    }
    return CallNextHookEx( NULL, nCode, wParam, lParam );
}


LRESULT CALLBACK callWndProc( int nCode, WPARAM wParam, LPARAM lParam )
{
    // Special case -- handle up here so it doesn't get messed with accidentally
    if ( nCode < 0 )
    {
        return CallNextHookEx( NULL, nCode, wParam, lParam );
    }

    CWPSTRUCT *msg = ( CWPSTRUCT * ) lParam;
    if ( nCode == HC_ACTION && msg->message == WM_EXITSIZEMOVE )
    {
        WINDOWPLACEMENT wndPlacement;
        GetWindowPlacement( msg->hwnd, &wndPlacement );

        RECT snappedRect;
        GetWindowRect( msg->hwnd, &snappedRect );

        if ( !EqualRect( &snappedRect, &( wndPlacement.rcNormalPosition ) ) )
        {
            JNIEnv *env = getJavaEnv( );
            if ( env )
            {
                env->CallStaticVoidMethod( _windowsFixesClass, _handleVerticalMaximizeMID, msg->hwnd );
            }
        }
    }
    return CallNextHookEx( NULL, nCode, wParam, lParam );
}