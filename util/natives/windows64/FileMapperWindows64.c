#include "jni.h"
#include <windows.h>

jstring getErrorMessage( JNIEnv *env, const wchar_t *prefix, DWORD code )
{
    jstring s;
    {
        wchar_t *detail;
        {
            wchar_t *charsPrelim = NULL;
            DWORD numCharsPrelim = FormatMessageW( FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
                                              NULL,
                                              code,
                                              MAKELANGID( LANG_NEUTRAL, SUBLANG_DEFAULT ),
                                              ( LPWSTR ) &charsPrelim,
                                              0,
                                              NULL );
            if ( numCharsPrelim > 0 )
            {
                // Trim trailing whitespace
                while ( numCharsPrelim > 0 )
                {
                    if ( !iswspace( charsPrelim[ numCharsPrelim - 1 ] ) )
                    {
                        break;
                    }
                    numCharsPrelim--;
                }

                size_t numChars = numCharsPrelim + 1;
                detail = malloc( numChars * sizeof( wchar_t ) );
                _snwprintf_s( detail, numChars, _TRUNCATE, L"%s", charsPrelim );

                LocalFree( charsPrelim );
            }
            else
            {
                size_t numChars = 1024;
                detail = malloc( numChars * sizeof( wchar_t ) );
                _snwprintf_s( detail, numChars, _TRUNCATE, L"%d", code );
            }
        }

        const wchar_t *separator = L": ";

        wchar_t *message;
        {
            size_t numChars = wcslen( prefix ) + wcslen( separator ) + wcslen( detail ) + 1;
            message = malloc( numChars * sizeof( wchar_t ) );
            _snwprintf_s( message, numChars, _TRUNCATE, L"%s%s%s", prefix, separator, detail );
        }

        s = ( *env )->NewString( env, ( const jchar * ) message, wcslen( message ) );

        free( message );
        free( detail );
    }
    return s;
}

jthrowable newIOException( JNIEnv *env, jstring message )
{
    jthrowable e;
    {
        jclass clazz = ( *env )->FindClass( env, "java/io/IOException" );
        jmethodID init = ( *env )->GetMethodID( env, clazz, "<init>", "(Ljava/lang/String;)V" );

        e = ( jthrowable ) ( *env )->NewObject( env, clazz, init, message );

        ( *env )->DeleteLocalRef( env, clazz );
    }
    return e;
}

void throwIOException( JNIEnv *env, const wchar_t *prefix )
{
    jthrowable e;
    {
        jstring message = getErrorMessage( env, prefix, GetLastError( ) );
        e = newIOException( env, message );
        ( *env )->DeleteLocalRef( env, message );
    }

    if ( e != NULL )
    {
        ( *env )->Throw( env, e );
    }
}

JNIEXPORT jlong JNICALL Java_com_metsci_glimpse_util_io_FileMapperWindows64__1map( JNIEnv *env,
                                                                                   jclass clazz,
                                                                                   jobject fileDescriptor,
                                                                                   jlong size,
                                                                                   jboolean writable )
{
    void *mappingPtr;
    {
        // Borrow from fileDescriptor
        HANDLE fileHandle;
        {
            jclass clazz = ( *env )->FindClass( env, "java/io/FileDescriptor" );
            jfieldID handle = ( *env )->GetFieldID( env, clazz, "handle", "J" );
            fileHandle = ( HANDLE ) ( *env )->GetLongField( env, fileDescriptor, handle );
            ( *env )->DeleteLocalRef( env, clazz );
        }

        HANDLE mappingHandle;
        {
            DWORD fileProtect = ( writable == JNI_TRUE ? PAGE_READWRITE : PAGE_READONLY );

            LARGE_INTEGER size64;
            size64.QuadPart = size;
            DWORD sizeHi32 = size64.HighPart;
            DWORD sizeLo32 = size64.LowPart;

            mappingHandle = CreateFileMapping( fileHandle, NULL, fileProtect, sizeHi32, sizeLo32, NULL );
        }
        if ( mappingHandle == NULL )
        {
            throwIOException( env, L"CreateFileMapping failed" );
            return -1L;
        }

        DWORD mappingAccess = ( writable == JNI_TRUE ? FILE_MAP_WRITE : FILE_MAP_READ );

        mappingPtr = MapViewOfFile( mappingHandle, mappingAccess, 0, 0, size );
        if ( mappingPtr == NULL )
        {
            BOOL closeResult = CloseHandle( mappingHandle );
            if ( closeResult == 0 )
            {
                throwIOException( env, L"MapViewOfFile and CloseHandle both failed" );
                return -1L;
            }
            else
            {
                throwIOException( env, L"MapViewOfFile failed" );
                return -1L;
            }
        }

        BOOL closeResult = CloseHandle( mappingHandle );
        if ( closeResult == 0 )
        {
            throwIOException( env, L"CloseHandle failed" );
            return -1L;
        }
    }

    jlong mappingAddr = ( jlong ) mappingPtr;
    return mappingAddr;
}

JNIEXPORT void JNICALL Java_com_metsci_glimpse_util_io_FileMapperWindows64__1unmap( JNIEnv *env,
                                                                                    jclass clazz,
                                                                                    jlong mappingAddr )
{
    void *mappingPtr = ( void * ) mappingAddr;
    BOOL result = UnmapViewOfFile( mappingPtr );
    if ( result == 0 )
    {
        throwIOException( env, L"UnmapViewOfFile failed" );
        return;
    }
}

