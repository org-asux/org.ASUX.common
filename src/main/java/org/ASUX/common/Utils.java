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

import java.util.regex.*;
import java.util.Scanner;
import java.util.Properties;

import java.io.StringReader;
import java.io.PrintStream;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 *  Utility functions that are quite generic
 */
public class Utils {

    public static final String CLASSNAME = "org.ASUX.common.Utils";

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This deepClone function is unnecessary, if you can invoke org.apache.commons.lang3.SerializationUtils.clone(this)
     *  @param <T> any class that implements java.io.Streamable interface
     *  @param _orig what you want to deep-clone
     *  @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     *  @throws Exception like ClassNotFoundException while trying to serialize and deserialize the input-parameter
     */
    public static <T> T deepClone(T _orig) throws Exception {
        final String errmsg = CLASSNAME + ": deepClone(): ERROR deepCloning object of type "+ _orig.getClass().getName() +" of value=["+ ((_orig==null)?"null":_orig.toString()) +"]";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(_orig);
            oos.flush();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            @SuppressWarnings("unchecked")
            final T newobj = (T) ois.readObject();
            return newobj;
        } catch (ClassNotFoundException e) {
            e.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this method
            System.err.println( errmsg +"\n"+ e );
            System.exit(133);
            throw e;
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this method
            System.err.println( errmsg +"\n"+ e );
            System.exit(133);
            throw e;
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================
    public static final String REGEXP_SIMPLEWORD = "[${}@%a-zA-Z0-9\\.,:_/-]+";
    public static final String REGEXP_KVPAIR = "^\\s*("+ REGEXP_SIMPLEWORD +")=("+ REGEXP_SIMPLEWORD +")\\s*$";

    public static Properties parseProperties( final String _s ) throws Exception
    {
        final String HDR = CLASSNAME + ": parse(_s): ";
        final Pattern printPattern = Pattern.compile( REGEXP_KVPAIR ); // Note: A line like 'print -' would FAIL to match \\S.*\\S
        java.util.Scanner scanner = null;
        try {
            final Properties props = new Properties();
            scanner = new Scanner( _s );
            scanner.useDelimiter( ";|"+System.lineSeparator() );
            //---------------------------
            for ( int ix=1;   scanner.hasNext();   ix++ ) {
                final String line = scanner.next();
                // System.out.println( HDR +"line #"+ ix +"="+ line );
                final Matcher matcher = printPattern.matcher( line );
                if ( ! matcher.find() )
                    throw new Exception( HDR +"Not a valid KV-Pair @ line #"+ ix +" = '"+ line +"'" );
                props.load( new StringReader( line ) );
            } // for loop
            // System.out.println( HDR +"# of entries loaded into java.util.Properties = "+ props.size() );
            // props.list( System.out );
            return props;
        // } catch(Exception e) {
            // e.printStackTrace( System.err );
            // System.err.println( e.getMessage() );
        } finally {
            scanner.close();
        }
        // return null;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

}
