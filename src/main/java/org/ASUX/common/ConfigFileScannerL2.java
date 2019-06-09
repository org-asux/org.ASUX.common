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
import java.util.Iterator;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 *  <p>This is part of org.ASUX.common GitHub.com project and the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>This class extends {@link org.ASUX.common.ConfigFileScanner}.  These classes together offer a tool-set to help make it very easy to work with the Configuration and Propertyfiles - while making it very human-friendly w.r.t .comments etc...</p>
 */
public abstract class ConfigFileScannerL2 extends ConfigFileScanner {

    private static final long serialVersionUID = 112L;
    public static final String CLASSNAME = ConfigFileScannerL2.class.getName();

	public static final String REGEXP_INLINEVALUE = "['\" ${}@%a-zA-Z0-9\\[\\]\\.,:_/-]+";
	public static final String REGEXP_NAMESUFFIX  =     "[${}@%a-zA-Z0-9\\.,:_/-]+";
	public static final String REGEXP_NAME = "[a-zA-Z$]" + REGEXP_NAMESUFFIX;
	public static final String REGEXP_FILENAME = "[a-zA-Z$/\\.]" + REGEXP_NAMESUFFIX;
	public static final String REGEXP_OBJECT_REFERENCE = "[@!]" + REGEXP_FILENAME;

    public static final String REGEXP_ECHO = "^\\s*echo\\s+(\\S.*\\S)\\s*$";
    public static final String REGEXP_INCLUDE = "^\\s*include\\s+("+ REGEXP_OBJECT_REFERENCE +")\\s*$";

    protected boolean bLine2bEchoed = false;
    protected ConfigFileScannerL2 includedFile = null;

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** <p>The only constructor - public/private/protected</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public ConfigFileScannerL2( boolean _verbose ) {
        super( _verbose );
        reset();
    }

    protected ConfigFileScannerL2() { super(); }

    /**
     * Since this is an abstract class, parse() line - when it encounters a 'include @filename' - needs to invoke a constructor that creates a 2nd object
     * @return an object of the subclass of this ConfigFileScannerL2.java
     */
    protected abstract ConfigFileScannerL2 create();

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine() and reset().<br>reset() has draconian-implications - as if openConfigFile() was never called!
     */
    @Override
    public void reset() {
        super.reset();
        this.bLine2bEchoed = false;
        this.includedFile = null;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** This function is exclusively for use within the go() - the primary function within this class - to make this very efficient when responding to the many isXXX() methods in this class.
     */
    @Override
    protected void resetFlagsForEachLine() {
        // super.resetFlagsForEachLine(); <-- this is: protected abstract
        this.bLine2bEchoed = false;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** New Method added to this subclass.
     *  For use by any Processor of this batch-file.. whether the user added the 'echo' prefix to a command, requesting that that specific line/command be echoed while executing
     *  @return true or false, whether the user added the 'echo' prefix to a command, requesting that that specific line/command be echoed while executing
    */
    public boolean isLine2bEchoed() {
        return this.bLine2bEchoed;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This overrides the method from parent class {@link org.ASUX.common.ConfigFileScanner}
     *  This override is reqired to "parse out" prefixes like 'ecbo'
     *  @return the next string in the list of lines (else an exception is thrown)
     *  @throws Exception in case this class is messed up or hasNextLine() is false or has Not been invoked appropriately
     */
    @Override
    public String currentLine() throws Exception
    {   // !!!!!!!!!!!!!!!!!!!!!! OVERRIDES Parent Method !!!!!!!!!!!!!!!!!!!!!!!!
        if ( this.includedFile != null )
            return this.includedFile.currentLine();

        if ( this.isLine2bEchoed() ) {
            // return this.nextLine(); // this will Not be null.. just because of the call to hasNextLine() above.
            return getLine_NoEchoPrefix( super.currentLine() );
        } else {
            return super.currentLine();
        }
    }

    //=============================================================================
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine().  But this method is a special deviation, as it allows us to get the 'current-line' over-n-over again.
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    @Override
    public String currentLineOrNull()
    {   // !!!!!!!!!!!!!!!!!!!!!! OVERRIDES Parent Method !!!!!!!!!!!!!!!!!!!!!!!!
        if ( this.includedFile != null )
            return this.includedFile.currentLineOrNull();

        if ( this.isLine2bEchoed() ) {
            // return this.nextLine(); // this will Not be null.. just because of the call to hasNextLine() above.
            return getLine_NoEchoPrefix( super.currentLineOrNull() );
        } else {
            return super.currentLineOrNull();
        }
    }

    //=============================================================================
    /**
     * New private STATIC method - to delete the 'echo' prefix from a string
     * @param _line can be null
     * @return the _line as-is but without the 'echo' at the beginning of the line
     */
    private static String getLine_NoEchoPrefix( String _line )
    {
        if ( _line == null )
            return null;
        Pattern echoPattern = Pattern.compile( REGEXP_ECHO );
        Matcher echoMatcher    = echoPattern.matcher( _line );
        if (echoMatcher.find()) {
            return echoMatcher.group(1); // line.substring( echoMatcher.start(), echoMatcher.end() );
        } else {
            return _line;
        }
    }

    //===========================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //===========================================================================

    /**
     * See {@link org.ASUX.common.ConfigFileScanner#getState()}.
     * @return something like: ConfigFile [@mapsBatch1.txt] @ line# 2 = [line contents as-is]
     */
    @Override
    public String getState()
    {
        if ( this.includedFile != null )
            return this.includedFile.getState();
        else
            return super.getState();
    }

    //===========================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //===========================================================================

    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine()
     *  @return true or false
     */
    @Override
    public boolean hasNextLine()
    {
        if ( this.includedFile != null ) {
                final boolean hnl = this.includedFile.hasNextLine();
                if ( hnl )
                    return true; // return hnl;
                else {
                    this.includedFile = null; // we're done with the included file!
                    return super.hasNextLine();
                }
        } else {
                return super.hasNextLine();
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** Thie method overrides the parent/super class method {@link ConfigFileScanner#nextLine()}
     *  @return 0.0001% chance (a.k.a. code bugs) that this is null. Returns the next string in the list of lines
     *  @throws Exception in case this class is messed up or hasNextLine() is false or has Not been invoked appropriately
     */
    @Override
    public String nextLine() throws Exception
    {   // !!!!!!!!!!!!!!!!!!!!!! OVERRIDES Parent Method !!!!!!!!!!!!!!!!!!!!!!!!
        while ( true ) {
             // I'm guaranteeing that we'll only iterate once - UNLESS we hit 'continue' (in the lines below.)
            if ( this.includedFile != null )
                return this.includedFile.nextLine();

            final String nl = super.nextLine();

            if (  !   parseLine() )  // this means that we hit a 'include @filename' line in the batchfile.  So, we need to REDO this method :-( ugly code.
                continue;

            if ( this.isLine2bEchoed() ) {
                return getLine_NoEchoPrefix( nl );
            } else {
                return nl;
            }
        } // while(true)
    }

    //===========================================================================
    /** Thie method overrides the parent/super class method {@link ConfigFileScanner#nextLineOrNull()}
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    @Override
    public String nextLineOrNull()
    {   // !!!!!!!!!!!!!!!!!!!!!! OVERRIDES Parent Method !!!!!!!!!!!!!!!!!!!!!!!!
        final String HDR = CLASSNAME +": nextLineOrNull(): ";
        while ( true ) {
             // I'm guaranteeing that we'll only iterate once - UNLESS we hit 'continue' (in the lines below.)
            if ( this.includedFile != null )
                return this.includedFile.nextLineOrNull();

            final String nl = super.nextLineOrNull();

            try {
                // ATTENTION !! subclasses may have overridden this method/parseLine() and can throw exception(s)
                if (  !   parseLine() )  // this means that we hit a 'include @filename' line in the batchfile.  So, we need to REDO this method :-( ugly code.
                    continue;
            } catch (Exception e) {
                e.printStackTrace(System.err); // too serious an internal-error.  Immediate bug-fix required.  The application/Program will exit .. in 2nd line below.
                System.err.println( HDR + " Unexpected Internal ERROR, when invoking parseLine(), for line= [" + nl +"]" );
                System.exit(91); // This is a serious failure. Shouldn't be happening.
            }
        
            if ( this.isLine2bEchoed() ) {
                return getLine_NoEchoPrefix( nl );
            } else {
                return nl;
            }
        } // while(true)
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** <p>New Method added to this subclass.</p>
     *  <p>ATTENTION: BE VERY CAREFUL when overriding this method, otherwise you cannot take advantage of 'echo' prefix parsing built-in.
     *  <p>This method should be called after nextLine().  nextLine() is inherited from the parent {@link org.ASUX.common.ConfigFileScanner}.</p>
     *  @return true if all 'normal', and false IF-AND-ONLY-IF we hit a 'include @filename' line in the batchfile
     *  @throws Exception This class does NOT.  But .. subclasses may have overridden this method and can throw exception(s).  Example: org.ASUX.yaml.BatchFileGrammer.java
     */
    protected boolean parseLine() throws Exception
    {
        this.resetFlagsForEachLine();
        String line = this.currentLineOrNull(); // remember the line is most likely already trimmed.  We need to chop off any 'echo' prefix

        final String HDR = CLASSNAME +": parseLine(): ";
        if ( this.verbose ) System.out.println( HDR + this.getState() );

        if ( line == null )
            return true;

        try {
            // ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // This block of code below (echoPattern, echoMatcher, this.bLine2bEchoed) MUST be the very BEGINNNG of this function
            final Pattern echoPattern = Pattern.compile( REGEXP_ECHO );
            final Matcher echoMatcher = echoPattern.matcher( line );
            if (echoMatcher.find()) {
                if ( this.verbose ) System.out.println( HDR +": I found the command to be ECHO-ed '"+ echoMatcher.group(1) +"' starting at index "+  echoMatcher.start() +" and ending at index "+ echoMatcher.end() );    
                line = echoMatcher.group(1); // line.substring( echoMatcher.start(), echoMatcher.end() );
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!! 'line' has been altered here.
                this.bLine2bEchoed = true;
                if ( this.verbose ) System.out.println( "\t 2nd echoing Line # "+ this.getState() );
                // fall thru below.. to identify other commands
            } // if

            final Pattern includePattern = Pattern.compile( REGEXP_INCLUDE );
            final Matcher includeMatcher = includePattern.matcher( line );
            if (includeMatcher.find()) {
                if ( this.verbose ) System.out.println( HDR +": I found the INCLUDE command: '"+ includeMatcher.group(1) +"' starting at index "+  includeMatcher.start() +" and ending at index "+ includeMatcher.end() );    
                final String includeFileName = includeMatcher.group(1); // line.substring( includeMatcher.start(), includeMatcher.end() );
                this.includedFile = this.create();
                final boolean success = this.includedFile.openFile( includeFileName, this.ok2TrimWhiteSpace, this.bCompressWhiteSpace );
                if ( ! success )
                    throw new Exception( "Unknown internal exception opening file: "+ includeFileName );
                if ( this.verbose ) System.out.println( "\t 2nd echoing Line # "+ this.getState() );
                return false; // !!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!! method returns here.
            } // if

            return true; // all ok.  We did NOT encounter a 'include @filename'

        } catch (PatternSyntaxException e) {
			e.printStackTrace(System.err); // too serious an internal-error.  Immediate bug-fix required.  The application/Program will exit .. in 2nd line below.
			System.err.println( HDR + ": Unexpected Internal ERROR, while checking for pattern ("+ REGEXP_ECHO +")." );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
        return true; // we shouldn't be getting here, due to 'System.exit()' within above catch()
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This deepClone function is unnecessary, as org.apache.commons.lang3.SerializationUtils.clone(this) is unable to handle 'transient' variables in this class.
     *  @param _orig what you want to deep-clone
     *  @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     */
    public static ConfigFileScannerL2 deepClone( final ConfigFileScannerL2 _orig ) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(_orig);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            final ConfigFileScannerL2 newobj = (ConfigFileScannerL2) ois.readObject();

            // // because this class has at least one TRANSIENT class-variable.. ..
            // // we need to 'restore' that object's transient variable to a 'replica'
            newobj.iterator = newobj.lines.iterator();
            for ( int ix = 0; ix < newobj.currentLineNum; ix++ )
                newobj.iterator.next(); // This will advance this.iterator to the right position, as java.lang.Iterator is NOT clonable/NOT SERIALIZABLE.
                // we rarely CLONE an object of this class, when it's still pointing to command #1 in _orig.  So, this ABOVE for-loop is just fine.

            return newobj;

        } catch (ClassNotFoundException e) {
			e.printStackTrace(System.err); // Static Method. So.. can't avoid dumping this on the user.
            return null;
        } catch (java.io.IOException e) {
			e.printStackTrace(System.err); // Static Method. So.. can't avoid dumping this on the user.
            return null;
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================
    // For unit-testing purposes only
    public static void main(String[] args) {
        final String HDR = CLASSNAME + ": main(): ";
        class TestConfigFileScanner extends ConfigFileScannerL2 {
            private static final long serialVersionUID = 1L;
            public TestConfigFileScanner(boolean _verbose) { super(_verbose); }
            protected TestConfigFileScanner create() { return new TestConfigFileScanner( this.verbose ); }
        };
        try {
            final ConfigFileScannerL2 o = new TestConfigFileScanner(false);
            o.openFile( args[0], true, true );
            while (o.hasNextLine()) {
                System.out.println( o.nextLine() );
                // System.out.println( o.currentLine() );
                System.out.println( o.getState() );
                System.out.println();
            }
		} catch (Exception e) {
			e.printStackTrace(System.err); // main().  For Unit testing
			System.err.println( HDR + "Unexpected Internal ERROR, while processing " + ((args==null || args.length<=0)?"[No CmdLine Args":args[0]) +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
    }

}
