package com.metsci.glimpse.util.io;

import java.io.IOException;
import java.io.RandomAccessFile;

public interface FileMapper
{

    long map( RandomAccessFile raf, long size, boolean writable ) throws IOException;

    Runnable createUnmapper( long address, long size, RandomAccessFile raf );

}
