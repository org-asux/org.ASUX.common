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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 *  Functions to help manipulate Strings
 */
public class StringUtils {

    // see if you can have ths implement the interface BiConsumer<T,U>
    // https://docs.oracle.com/javase/8/docs/api/java/util/function/BiConsumer.html

    public static final String CLASSNAME = "org.ASUX.common.StringUtils";

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    public final boolean verbose;

    /** <p>The only constructor - public/private/protected</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public StringUtils(boolean _verbose) {
        this.verbose = _verbose;
    }

    private StringUtils() {
        this.verbose = false;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * Given a string, if it's 1st character and last character are BOTH single-quote.. remove them.  Similarly for DOUBLE-quote.
     * @param s any string
     * @return string - after removing the beginning and ending quotes
     */
    public String removeBeginEndQuotes( final String s ) {
        if ( s.startsWith("'") && s.endsWith("'") ) {
            if (this.verbose) System.out.println( CLASSNAME +": removeBeginEndQuotes(): before stripped single-quote =["+ s + "]" );
            final String ss = s.substring( 1, s.length()-1 );
            if (this.verbose) System.out.println( CLASSNAME +": removeBeginEndQuotes(): stripped of single-quote =["+ ss + "]" );
            return ss;
        } else if ( s.startsWith("\"") && s.endsWith("\"") ) {
            if (this.verbose) System.out.println( CLASSNAME +": removeBeginEndQuotes(): before stripped DOUBLE-quote =["+ s + "]" );
            final String ss = s.substring( 1, s.length()-1 );
            if (this.verbose) System.out.println( CLASSNAME +": removeBeginEndQuotes(): stripped of DOUBLE-quote =["+ ss + "]" );
            return ss;
        } else {
            return s;
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * Given aan array of string, for each element .. check if it's 1st character and last character are BOTH single-quote.. remove them.  Similarly for DOUBLE-quote.
     * If any array-element has such beginning and end matching quotes, remove those.
     * @param _basicStrArray any string
     * @return string[] with updated string values in same order.
     */
    public String[] removeBeginEndQuotes( final String[] _basicStrArray )
    {
        final ArrayList<String> arr = new ArrayList<>();
        for ( int ix=0; ix < _basicStrArray.length; ix++ ) {
            final String s = removeBeginEndQuotes ( _basicStrArray[ix] );
            arr.add( s );
        } // for
        return arr.toArray( _basicStrArray ); // (new String[]());
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  <p>Utility function allowing to convert an _INLINE_ String on the command-line, into a multi-line string using ';' delimiter</p>
     *  <p>There are lots of ways to do this.  but the ability to use 'built-in' commands (of {@link ScriptFileScanner} and {@link ConfigFileScannerL2}) is very valuable for other ASUX.org projects (like org.ASUX.AWS.CFN)</p>
     *  @param _s a NotNull String utilizing ';' for newline
     *  @return a NotNull String (at a minimum will be empty-string)
     *  @throws Exception On errors executing the 'built-in' commands of {@link ScriptFileScanner}, or.. while parsing the String (99.999% UNlikely)
     */
    public static String convertString2MultiLine( final String _s ) throws Exception {
        return convertString2MultiLine( false, _s, new LinkedHashMap<String,Properties>() ); // implemented in ConfigFileScanner.class
    }

    /**
     *  <p>Utility function allowing to convert an _INLINE_ String on the command-line, into a multi-line string using ';' delimiter</p>
     *  <p>There are lots of ways to do this.  but the ability to use 'built-in' commands (of {@link ScriptFileScanner} and {@link ConfigFileScannerL2}) is very valuable for other ASUX.org projects (like org.ASUX.AWS.CFN)</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _s a NotNull String utilizing ';' for newline
     *  @param _propsSet Not-Null.  a REFERENCE to an instance of LinkedHashMap, whose object-lifecycle is maintained by some other class (as in, creating new LinkedHashMap&lt;&gt;(), putting content into it, updating content as File is further processed, ..)
     *  @return a NotNull String (at a minimum will be empty-string)
     *  @throws Exception On errors executing the 'built-in' commands of {@link ScriptFileScanner}, or.. while parsing the String (99.999% UNlikely)
     */
    public static String convertString2MultiLine( final boolean _verbose, final String _s, LinkedHashMap<String,Properties> _propsSet  ) throws Exception {
        if ( _propsSet == null )
            _propsSet = OSScriptFileScanner.initProperties();
        final ScriptFileScanner scanner = new ScriptFileScanner( _verbose, _propsSet );
        scanner.useDelimiter( ";|"+System.lineSeparator() );
        scanner.openFile( _s , true /*_ok2TrimWhiteSpace */, true /* _bCompressWhiteSpace */ );
        return scanner.toString(); // implemented in ConfigFileScanner.class
    }

}
