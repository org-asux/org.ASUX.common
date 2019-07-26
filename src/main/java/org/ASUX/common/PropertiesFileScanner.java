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

import org.ASUX.common.Macros.MacroException;

import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Properties;

import java.io.Reader;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import static org.junit.Assert.*;

/**
 * <p>This is part of org.ASUX.common GitHub.com project and the <a href=
 * "https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a>
 * GitHub.com projects.</p>
 * <p>This class extends {@link org.ASUX.common.ConfigFileScanner}.</p>
 * <p>These classes together offer a tool-set to help make it very easy to work
 * with the Configuration and Propertyfiles - while making it very
 * human-friendly w.r.t .comments etc...</p>
 * <p>This specific class offers 'echo' prefix to a line in the FILE, as well as
 * 'include &gt;FILE&gt;' both of which are handled transparently!</p>
 * <p>Both 'echo' and 'print' may sound similar, but 'print' is the literal-equivalent of 'echo' of BASH /bin/sh /bin/tcsh. So.. why need 'echo'?
 * Well, 'echo' is more PRIMITIVE. It shows the command __TO BE__ executed, after ALL MACRO-Replacements. In that sense, this combination of 'echo' and
 * 'print' is MORE SOPHISTICATED and MORE CAPABLE that 'echo' in BASH, /bin/sh, /bin/tcsh</p>
 * <p>In addition, this class offers the ability to evaluate expressions JUST LIKE a Bash or /bin/sh or /bin/tcsh does - if you provide a java.util.Properties instance as constructor-argument.</p>
 */
public class PropertiesFileScanner extends Properties  {

    private static final long serialVersionUID = 112L;
    public static final String CLASSNAME = PropertiesFileScanner.class.getName();

    public boolean verbose;

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    /**
     * <p> The basic constructor - that does __NOT__ allow you to evaluate Macro-expressions like ${XYZ} </p>
     * @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public PropertiesFileScanner(boolean _verbose) {
        this.verbose = _verbose;
    }

    /**
     * Do NOT use. USE ONLY in emergencies and ONLY IF you know what the fuck you are doing. No questions will be answered, and NO help will be provided.
     */
    protected PropertiesFileScanner() {
        this.verbose = false;
    }

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    /**
     *  <p>Fully overridden Method. For use by any Processor of this batch-file.. whether the user added the 'echo' prefix to a command, requesting that that specific line/command be echoed while executing.</p>
     *  <p>See also {@link #load(Object)} and {@link #load(Object, LinkedHashMap)}</p>
     *  @param inStream NotNull object (either FileInputStream or ByteArrayInputStream)
     *  @throws IllegalArgumentException - if the input stream contains a malformed Unicode escape sequence.
     *  @throws IOException any issues reading the contents of the Input-String
     */
    @Override
    public void	load( InputStream inStream ) throws IllegalArgumentException, IOException {
        this.load( inStream, null );
    }

    /**
     *  <p>This is non-standard polymorphism added by org.ASUX toolset, as we allow users to provide Properties separated via semicolon inline on commandline.</p>
     *  <p>See also {@link #load(InputStream)}</p>
     *  @param _s NotNull object (either java.lang.String, FileInputStream or ByteArrayInputStream)
     *  @throws IllegalArgumentException - if the input stream contains a malformed Unicode escape sequence.
     *  @throws IOException any issues reading the contents of the Input-String
     */
    public void load( final Object _s ) throws IllegalArgumentException, IOException {
        this.load( _s, null );
    }

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    private static class PropsFileScannerIMPL extends OSScriptFileScanner {

        private static final long serialVersionUID = 190L;
        public static final String CLASSNAME = PropsFileScannerIMPL.class.getName();

        public static final String REGEXP_SIMPLEWORD = "[${}@%a-zA-Z0-9\\.,:;()%_/|+ -]+"; // NO Spaces
        public static final String REGEXP_KVPAIR = "^\\s*("+ REGEXP_SIMPLEWORD +")=\\s*['\"]?(.*)['\"]?\\s*$"; // allows for empty string ""
        // public static final String REGEXP_KVPAIR = "^\\s*("+ REGEXP_SIMPLEWORD +")=\\s*['\"]?("+ REGEXP_SIMPLEWORD +")['\"]?\\s*$";

        private final PropertiesFileScanner propsFile;
        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

        public PropsFileScannerIMPL( boolean _verbose, final PropertiesFileScanner _pfs ) {
            super( _verbose );
            this.propsFile = _pfs;
        }

        public PropsFileScannerIMPL( boolean _verbose, final PropertiesFileScanner _pfs, final LinkedHashMap<String,Properties> _propsSet ) {
            super( _verbose, _propsSet );
            assertTrue( _propsSet != null );
            this.propsFile = _pfs;
        }

        protected PropsFileScannerIMPL create() {
            return new PropsFileScannerIMPL( super.verbose, new PropertiesFileScanner(super.verbose),   super.propsSetRef );
        }

        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

        /**
         *  <p>This method is used to simply tell whether 'current-line' matches the REGEXP patterns that execBuiltInCommand() will be processing 'internally' within this class</p>
         *  <p>In this class, those would be the REGEXP for 'print ...' and 'include @...'</p>
         *  @param nextLn current line or 'peek-forward' line
         *  @return true if the line will be processed 'internally'
         */
        @Override
        protected boolean isBuiltInCommand( final String nextLn )
        {   final String HDR = CLASSNAME +": isBuiltInCommand(): ";

            if ( super.isBuiltInCommand(nextLn) )
                return true;

            if ( nextLn == null ) return false;
            final String noprefix = removeEchoPrefix( nextLn );
            if ( this.verbose ) System.out.println( HDR +"noprefix="+ noprefix );
            final boolean retb = noprefix.matches( REGEXP_KVPAIR );
            if ( this.verbose ) System.out.println( HDR +"retb="+ retb );
            return retb;
        }

        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

        /** <p>New Method added to this subclass.  Implement your command parsing and do as appropriate.</p>
         *  <p>ATTENTION: Safely assume that any 'echo' prefix parsing and any 'print' parsing has happened already in a TRANSAPARENT way.</p>
         *  <p>This method is automatically invoked _WITHIN_ nextLine().  nextLine() is inherited from the parent {@link org.ASUX.common.ConfigFileScannerL3}.</p>
         *  @return true if all 'normal', and false IF-AND-ONLY-IF we hit a 'include @filename' line in the batchfile
         *  @throws Exception This class does NOT.  But .. subclasses may have overridden this method and can throw exception(s).  Example: org.ASUX.yaml.BatchFileGrammer.java
         */
        @Override
        protected boolean execBuiltInCommand() throws Exception
        {
            if ( super.execBuiltInCommand() )
                return true; // Don't bother to do anything for lines that are auto-processed internally by super.class

            final String HDR = CLASSNAME +": execBuiltInCommand(): ";
            if ( this.verbose ) System.out.println( HDR + this.getState() );

            try {

                Pattern kvpairPattern = Pattern.compile( REGEXP_KVPAIR );
                Matcher kvpairMatcher = kvpairPattern.matcher( super.currentLine() );
                if (kvpairMatcher.find()) {
                    if ( this.verbose ) System.out.println( HDR +"I found the text "+ kvpairMatcher.group() +" starting at index "+  kvpairMatcher.start() +" and ending at index "+ kvpairMatcher.end() );
                    final String key = kvpairMatcher.group(1);
                    final String val = kvpairMatcher.group(2);
                    this.propsFile.setProperty( key, val );
                    if ( this.verbose ) System.out.println( HDR +"Added KV-Pair: "+ key +" = "+ val );
                    return true;
                }
                // else 
                //      throw new Exception( HDR +"Not a valid KV-Pair @ line #"+ ix +" = '"+ line +"'" );

            // This class did NOT process the current line
            return false; // see javadoc for this method (and for super-class), as to why 'false'

        } catch (PatternSyntaxException e) {
			e.printStackTrace(System.err); // too serious an internal-error.  Immediate bug-fix required.  The application/Program will exit .. in 2nd line below.
			System.err.println( HDR + ": Unexpected Internal ERROR, while checking for pattern ("+ REGEXP_ECHO +")." );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
        // we shouldn't be getting here, due to 'System.exit()' within above catch()
        return false; // This class did NOT process the current line
    }
    } // class

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    /**
     *  <p>This is a unique ASUX.org enhancement to java.utill.Properties.<br>
     *      By passing in a "list/set" of Properties (whose lifecycle is managed elsewhere), you can benefit from "Macro-expressions" within the Properties file.</p>
     *  <p>Example: an property/entry like: <code>fileInUsersHomeFolder=${ASUX::user.home}/filename</code></p>
     *  @param _src NotNull object (either java.lang.String, FileInputStream or ByteArrayInputStream)
     *  @param _allProps Null-OK.  a REFERENCE to an instance of LinkedHashMap, whose object-lifecycle is maintained by some other class (as in, creating new LinkedHashMap&lt;&gt;(), putting content into it, updating content as File is further processed, ..)
     *  @throws IllegalArgumentException in case the Macros within the contents of the Properties file are invalid.
     *  @throws IOException any trouble reading the "source of data" (_src)
     */
    public void load( final Object _src, final LinkedHashMap<String,Properties> _allProps ) throws IllegalArgumentException, IOException
    {
        final String HDR = CLASSNAME + ": load(_src,<inStream>): ";
        // final Pattern printPattern = Pattern.compile( REGEXP_KVPAIR ); // Note: A line like 'print -' would FAIL to match \\S.*\\S
        try {
            final PropsFileScannerIMPL scanner = new PropsFileScannerIMPL(
                            this.verbose,
                            this,
                            _allProps != null ? _allProps : OSScriptFileScanner.initProperties()
                            );

            scanner.useDelimiter( ";|"+System.lineSeparator() );
            scanner.openFile( _src, /* _ok2TrimWhiteSpace */ true, /* _bCompressWhiteSpace */ true );
            //---------------------------
            for ( int ix=1;   scanner.hasNextLine();   ix++ ) {
                final String line = scanner.nextLine();
                if ( this.verbose ) System.out.println( HDR +"line #"+ ix +"="+ line );
                throw new Exception( "Not a valid KV-Pair "+ scanner.getState() +"'" );
            } // for loop
            if ( this.verbose ) System.out.println( HDR +"# of entries loaded into java.util.Properties = "+ this.size() );
            if ( this.verbose ) super.list( System.out );
        } catch(IllegalArgumentException iae) {
            throw iae;
        } catch(IOException ie) {
            throw ie;
        } catch(Exception e) {
            if ( this.verbose ) e.printStackTrace( System.err );
            if ( this.verbose ) System.err.println( e.getMessage() );
            throw new IOException( e.getMessage() );
        } finally {
            if ( _src instanceof InputStream ) ((InputStream)_src).close();
        }
    }

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    /**
     * See https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html#load-java.io.Reader-
     * @param reader the input character stream.
     */
    @Override
    public void	load(Reader reader) throws IOException {
        throw new IOException( "load(java.io.Reader() is Not implemented in "+ CLASSNAME +"." );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This deepClone function is VERY MUCH necessary, as No cloning-code can handle 'transient' variables in this class/superclass.
     *  @param _orig what you want to deep-clone
     *  @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     */
    public static PropertiesFileScanner deepClone( final PropertiesFileScanner _orig ) {
        assertTrue( _orig != null );
        try {
            final PropertiesFileScanner newobj = Utils.deepClone( _orig );
            newobj.deepCloneFix( _orig );
            return newobj;
        } catch (Exception e) {
			e.printStackTrace(System.err); // Static Method. So.. can't avoid dumping this on the user.
            return null;
        }
    }

    /**
     * In order to allow deepClone() to work seamlessly up and down the class-hierarchy.. I should allow subclasses to EXTEND (Not semantically override) this method.
     * @param _orig the original NON-Null object
     */
    protected void deepCloneFix( final PropertiesFileScanner _orig ) {
        // this CLASS has __NO__ TRANSIENT class-variables..
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================
    // For unit-testing purposes only
    public static void main(String[] args) {
        final String HDR = CLASSNAME + ": main(): ";
        try {
            boolean verbose = false;
            int ix = 0;
            if ( "--verbose".equals(args[0]) ) {
                ix ++;
                verbose = true;
            }
            final PropertiesFileScanner o = new PropertiesFileScanner( verbose );
            o.load( args[ix] );
            o.list( System.out );

        } catch (Exception e) {
			e.printStackTrace(System.err); // main().  For Unit testing
			System.err.println( HDR + "Unexpected Internal ERROR, while processing " + ((args==null || args.length<=0)?"[No CmdLine Args":args[0]) +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
    }

}
