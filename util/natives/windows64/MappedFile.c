#include "jni.h"
#include <windows.h>

jstring getErrorMessage( JNIEnv *env, const wchar_t *prefix, DWORD code )
{
    jstring s;
    {
        wchar_t *detail;
        {
            DWORD numCharsA = FormatMessageW( FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
                                              NULL,
                                              code,
                                              MAKELANGID( LANG_NEUTRAL, SUBLANG_DEFAULT ),
                                              ( LPWSTR ) &detail,
                                              0,
                                              NULL );
            if ( numCharsA == 0 )
            {
                size_t maxNumCharsB = 1024;
                detail = malloc( maxNumCharsB * sizeof( wchar_t ) );
                _snwprintf_s( detail, maxNumCharsB, _TRUNCATE, L"%d", code );
            }
        }

        const wchar_t *separator = L": ";

        wchar_t *message;
        {
            size_t maxNumChars = wcslen( prefix ) + wcslen( separator ) + wcslen( detail ) + 1;
            message = malloc( maxNumChars * sizeof( wchar_t ) );
            _snwprintf_s( message, maxNumChars, _TRUNCATE, L"%s%s%s", prefix, separator, detail );
        }

        s = ( *env )->NewString( env, ( const jchar * ) message, wcslen( message ) );

        LocalFree( message );
        LocalFree( detail );
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

JNIEXPORT jlong JNICALL Java_com_metsci_glimpse_util_io_MappedFile__1mapFile( JNIEnv *env,
                                                                              jclass clazz,
                                                                              jobject fileDescriptor,
                                                                              jlong position,
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
            jlong maxSize = position + size;
            DWORD fileProtect = ( writable == JNI_TRUE ? PAGE_READWRITE : PAGE_READONLY );
            mappingHandle = CreateFileMapping( fileHandle, NULL, fileProtect, HIWORD( maxSize ), LOWORD( maxSize ), NULL );
        }
        if ( mappingHandle == NULL )
        {
            throwIOException( env, L"CreateFileMapping failed" );
            return -1L;
        }

        DWORD mappingAccess = ( writable == JNI_TRUE ? FILE_MAP_WRITE : FILE_MAP_READ );
        mappingPtr = MapViewOfFile( mappingHandle, mappingAccess, HIWORD( position ), LOWORD( position ), size );
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

JNIEXPORT void JNICALL Java_com_metsci_glimpse_util_io_MappedFile__1unmapFile( JNIEnv *env,
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

