#include <windows.h>
#include <unordered_set>
#include "com_metsci_glimpse_support_wheelfix_WheelFix.h"


DWORD _pid = GetCurrentProcessId( );
HANDLE _mutex = CreateMutex( NULL, FALSE, NULL );
std::unordered_set<DWORD> _threadsWithFix;


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


LRESULT CALLBACK hookProc( int code, WPARAM wParam, LPARAM lParam )
{
    MSG *msg = ( MSG * ) lParam;
    if ( code >= 0 && msg->message == WM_MOUSEWHEEL )
    {
        HWND hwndHovered = WindowFromPoint( msg->pt );
        if ( msg->hwnd != hwndHovered )
        {
            // Post an equivalent message to the hovered window
            if ( hwndHovered != NULL )
            {
                DWORD pidHovered;
                GetWindowThreadProcessId( hwndHovered, &pidHovered );
                if ( pidHovered == _pid )
                {
                    PostMessage( hwndHovered, WM_MOUSEWHEEL, msg->wParam, msg->lParam );
                }
            }
            
            // Squash the original message
            msg->message = WM_NULL;
        }
    }
    return CallNextHookEx( NULL, code, wParam, lParam );
}


BOOL CALLBACK setHookIfNeeded( HWND hwnd, LPARAM lParam )
{
    DWORD pidHwnd;
    DWORD tidHwnd = GetWindowThreadProcessId( hwnd, &pidHwnd );
    if ( pidHwnd == _pid )
    {
        WaitForSingleObject( _mutex, INFINITE );
        {
            if ( _threadsWithFix.find( tidHwnd ) == _threadsWithFix.end( ) )
            {
                HHOOK hHook = SetWindowsHookEx( WH_GETMESSAGE, hookProc, NULL, tidHwnd );
                if ( hHook == NULL )
                {
                    ReleaseMutex( _mutex );
                    return FALSE;
                }
                
                _threadsWithFix.insert( tidHwnd );
            }
        }
        ReleaseMutex( _mutex );
    }
    return TRUE;
}


jstring JNICALL Java_com_metsci_glimpse_support_wheelfix_WheelFix__1activateWheelFix( JNIEnv *env, jclass jthis )
{
    BOOL success = EnumWindows( setHookIfNeeded, ( LPARAM ) NULL );
    return ( success ? NULL : getErrorString( env, GetLastError( ) ) );
}
