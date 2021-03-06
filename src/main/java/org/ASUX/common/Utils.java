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

import java.util.LinkedHashMap;
import java.util.Properties;

import java.io.InputStream;

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

    /**
     *  <p>Given an String, read the contents line-by-line (very important if you're aiming for 'multi-line' properties), checks if they are valid KV-pairs, and loads them into a __new__ Properties object.</p>
     *  <p>Note: you can either pass in a file-name, or an __INLINE__ string with ';' or EOLN character breaking up that INLINE-string into 'lines'</p>
     *  <p>if the argument passed to this method, starts with '@' then it's treated as a file-name, else .. it's treated as _INLINE__ Properties (example: key1=value1;key2=value2'</p>
     *  @param _s a NotNull string, if it starts with '@' then it's treated as a file-name, else it's treated as _INLINE__ Properties (example: key1=value1;key2=value2'
     *  @return a new instance of java.util.Properties() NotNull (if error, you get Exception thrown)
     *  @throws Exception if the contents fail the RegExp <br><pre>
                        REGEXP_SIMPLEWORD = "[${}@%a-zA-Z0-9\\.,:;()%_/|+ -]+"; // NO Spaces
                        REGEXP_KVPAIR = "^\\s*("+ REGEXP_SIMPLEWORD +")=\\s*['\"]?(.*)['\"]?\\s*$"; // allows for empty string ""
</pre>
     */
    public static Properties parseProperties( final String _s ) throws Exception
    {
        return parseProperties( false, _s, null );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  <p>Given an InputStream, read the contents line-by-line (very important if you're aiming for 'multi-line' properties), checks if they are valid KV-pairs, and loads them into a __new__ Properties object.</p>
     *  <p>Note: for the content read from the InputStream, any ';' or EOLN character will break-up that content into 'lines'</p>
     * @param _verbose Whether you want deluge of debug-output onto System.out.
     * @param _istrm a NotNull instance of java.io.Stream
     * @return a new instance of java.util.Properties() NotNull (if error, you get Exception thrown)
     * @throws Exception if the contents fail the RegExp <br><pre>
                        REGEXP_SIMPLEWORD = "[${}@%a-zA-Z0-9\\.,:;()%_/|+ -]+"; // NO Spaces
                        REGEXP_KVPAIR = "^\\s*("+ REGEXP_SIMPLEWORD +")=\\s*['\"]?(.*)['\"]?\\s*$"; // allows for empty string ""
</pre>
     */
    public static Properties parseProperties( final boolean _verbose, final InputStream _istrm ) throws Exception {
        return parseProperties( _verbose, _istrm, null );
    }
    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  <p>Given an InputStream, read the contents line-by-line (very important if you're aiming for 'multi-line' properties), checks if they are valid KV-pairs, and loads them into a __new__ Properties object.</p>
     *  <p>Note: for the content read from the InputStream, any ';' or EOLN character will break-up that content into 'lines'</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _src NotNull object (either java.lang.String, FileInputStream or ByteArrayInputStream)
     *  @param _allProps Null-OK.  a REFERENCE to an instance of LinkedHashMap, whose object-lifecycle is maintained by some other class (as in, creating new LinkedHashMap&lt;&gt;(), putting content into it, updating content as File is further processed, ..)
     *  @return a new instance of java.util.Properties() NotNull (if error, you get Exception thrown)
     *  @throws Exception if the contents fail the RegExp <br><pre>
                        REGEXP_SIMPLEWORD = "[${}@%a-zA-Z0-9\\.,:;()%_/|+ -]+"; // NO Spaces
                        REGEXP_KVPAIR = "^\\s*("+ REGEXP_SIMPLEWORD +")=\\s*['\"]?(.*)['\"]?\\s*$"; // allows for empty string ""
</pre>
     */
    public static Properties parseProperties( final boolean _verbose, final Object _src, final LinkedHashMap<String,Properties> _allProps ) throws Exception
    {
        final PropertiesFileScanner props = new PropertiesFileScanner( _verbose );
        props.load( _src, _allProps );
        return props;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

}
