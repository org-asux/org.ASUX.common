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

}
