/*
 BSD 3-Clause License
 
 Copyright (c) 2019, Udaybhaskar Sarma Seetamraju
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 
 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 
 * Neither the name of the copyright holder nor the names of its
 contributors may be used to endorse or promote products derived from
 this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.ASUX.common;


import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;
//import java.util.regex.*;
import java.util.Set;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;

/**
 *  Utility functions that are quite generic
 */
public class IOUtils {

    public static final String CLASSNAME = IOUtils.class.getName();

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  This method aims to be a simpler interface to java.io.File's method to __CHECK__ whether the file permissions (Unix/Linux style) are limited to you/the owner of the file.
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _filePathStr a NotNull string representing a valid filepath (else you'll get IOException)
     *  @throws SecurityException if disallowed/improper access, etc..
     *  @throws IOException if anything goes wrong, including wrong paths, etc..
     */
    public static void checkIfFileSecure( final boolean _verbose, final String _filePathStr ) throws SecurityException, IOException
    {
        final String HDR = CLASSNAME + ": AWSCmdline(): ";
        // final String errmsg = CLASSNAME + ": deepClone(): ERROR deepCloning object of type "+ _orig.getClass().getName() +" of value=["+ ((_orig==null)?"null":_orig.toString()) +"]";
        try {
            final Path filePath = FileSystems.getDefault().getPath( _filePathStr );
            final Set<PosixFilePermission> perms = Files.getPosixFilePermissions( filePath );
            if ( perms.contains(PosixFilePermission.GROUP_READ) || perms.contains(PosixFilePermission.OTHERS_READ) )
                throw new SecurityException( "SECURITY-RISK! filepath '"+ filePath +"' is ACCESSIBLE to GROUP and OTHER user-groups."  );

        } catch(SecurityException se) {
            if ( _verbose ) se.printStackTrace(System.err);
            System.err.println( "\n"+ se +"\n\nUnable to ensure file(path) ["+ _filePathStr +"] is NOT ACCESSIBLE to GROUP and OTHER user-groups.\n\n" );
            throw se;
        } catch(java.io.IOException fe) {
            if ( _verbose ) fe.printStackTrace(System.err);
            System.err.println( "\n"+ fe +"\n\nUnable to ensure file(path) ["+ _filePathStr +"] is NOT ACCESSIBLE to GROUP and OTHER user-groups.\n\n" );
            throw fe;
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  This method aims to be a simpler interface to java.io.File's method to set the file permissions (Unix/Linux style).
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _filePathStr a NotNull string representing a valid filepath (else you'll get IOException)
     *  @param readable whether the file should be readable to you/the owner.
     *  @param writeable whether the file should be writeable to you/the owner.
     *  @param executable whether the file should be executable to you/the owner.
     *  @param ownerOnly whether the file should have __SAME__ permissions offered to GROUP and OTHERS (Linux/Unix concepts.  use 'true' for Windoze)
     *  @throws SecurityException if anything goes wrong, including wrong paths, improper access, etc..
     */
    public static void setFilePerms( final boolean _verbose,  final String _filePathStr,
            final boolean readable, final boolean writeable, final boolean executable,
            final boolean ownerOnly ) throws SecurityException
    {
        final String HDR = CLASSNAME + ": AWSCmdline(): ";
        // final String errmsg = CLASSNAME + ": deepClone(): ERROR deepCloning object of type "+ _orig.getClass().getName() +" of value=["+ ((_orig==null)?"null":_orig.toString()) +"]";

        try {
            final File file = new File ( _filePathStr );
            if ( readable ) file.setReadable( true, true /* ownerOnly */ ); // even if user forgets, make sure GROUP and OTHERS do NOT have permissions to this file.
            if ( writeable ) file.setWritable( true, true /* ownerOnly */ ); // even if user forgets, make sure GROUP and OTHERS do NOT have permissions to this file.
            if ( executable ) file.setExecutable( true, true /* ownerOnly */ ); // even if user forgets, make sure GROUP and OTHERS do NOT have permissions to this file.
            //-----------------
            // final Set<PosixFilePermission> posixperms = new HashSet<PosixFilePermission>();
            // if ( readable ) posixperms.add( PosixFilePermission.OWNER_READ );
            // if ( writeable ) posixperms.add( PosixFilePermission.OWNER_WRITE );
            // if ( executable ) posixperms.add( PosixFilePermission.OWNER_EXECUTE );
            // if ( readable && ! ownerOnly ) posixperms.add( PosixFilePermission.GROUP_READ );
            // if ( writeable && ! ownerOnly ) posixperms.add( PosixFilePermission.GROUP_WRITE );
            // if ( executable && ! ownerOnly ) posixperms.add( PosixFilePermission.GROUP_EXECUTE );
            // if ( readable && ! ownerOnly ) posixperms.add( PosixFilePermission.OTHERS_READ );
            // if ( writeable && ! ownerOnly ) posixperms.add( PosixFilePermission.OTHERS_WRITE );
            // if ( executable && ! ownerOnly ) posixperms.add( PosixFilePermission.OTHERS_EXECUTE );
            // Files.setPosixFilePermissions( _filePathStr, posixperms);

        } catch(SecurityException se) {
            if ( _verbose ) se.printStackTrace(System.err);
            System.err.println( "\n"+ se +"\n\nUnable to ensure file(path) ["+ _filePathStr +"] is NOT ACCESSIBLE to GROUP and OTHER user-groups.\n\n" );
            throw se;
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

}
