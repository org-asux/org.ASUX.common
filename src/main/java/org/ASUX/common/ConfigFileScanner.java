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

import org.ASUX.common.Tuple;

import java.util.regex.*;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import static org.junit.Assert.*;

/**
 *  <p>This is part of org.ASUX.common GitHub.com project and the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>This class is a bunch of tools to help make it easy to work with the Configuration and Propertyfiles - while making it very human-friendly w.r.t .comments etc...</p>
 */
public abstract class ConfigFileScanner implements java.io.Serializable {

    private static final long serialVersionUID = 110L;
    public static final String CLASSNAME = ConfigFileScanner.class.getName();

    //--------------------------------------------------------
    protected final boolean verbose;

    protected String fileName = null;
    /** ok2TrimWhiteSpace true or false, whether to REMOVE any leading and trailing whitespace.  Example: For YAML processing, trimming is devastating. */
    protected boolean ok2TrimWhiteSpace = true; // defaults
    /** bCompressWhiteSpace whether to replace multiple successive whitespace characters with a single space. */
    protected boolean bCompressWhiteSpace = true; // defaults

    protected ArrayList<String> lines; // = new ArrayList<>();
    protected ArrayList<Integer> origLineNumbers; //  = new ArrayList<>();

    protected transient Iterator<String> iterator = null;  // <<- transient class variable.  Will not be part of deepClone() method.
    protected int currentLineNum = -1;

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** <p>The only constructor - public/private/protected</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public ConfigFileScanner(boolean _verbose) {
        this.verbose = _verbose;
        reset();
    }

    protected ConfigFileScanner() { this.verbose = false;}


    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine() and reset().<br>reset() has draconian-implications - as if openConfigFile() was never called!
     */
    public void reset() {
        this.fileName = null;
        this.lines = new ArrayList<>();
        this.origLineNumbers = new ArrayList<>();

        this.iterator = null;
        this.currentLineNum = -1;

        this.resetFlagsForEachLine();
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** This function is exclusively for use within the go() - the primary function within this class - to make this very efficient when responding to the many isXXX() methods in this class.
     */
    protected abstract void resetFlagsForEachLine();

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine().  Scanner has reset().  I prefer rewind().  reset() is still defined, but has draconian-implications - as if openFile() was never called!
     */
    public void rewind() {
        this.iterator = this.lines.iterator();
        this.currentLineNum = 0; // Both -1 and 0 are invalid values.  1st line # is always === '1'.  That way it helps the user to debug batch-file issues.

        this.resetFlagsForEachLine();
    }

    //===========================================================================
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine().  But this method is a special deviation, as it allows us to get the 'current-line' over-n-over again.
     *  @return the next string in the list of lines (else an exception is thrown)
     *  @throws Exception in case this class is messed up or hasNextLine() is false or has Not been invoked appropriately
     */
    public String currentLine() throws Exception {

        if ( this.currentLineNum > 0 && this.currentLineNum <= this.lines.size() ) {
            // return this.nextLine(); // this will Not be null.. just because of the call to hasNextLine() above.
            return this.lines.get ( this.currentLineNum - 1 );
        } else {
            throw new Exception( CLASSNAME +": currentLine(): invalid currentLineNum=" +this.currentLineNum +".  Debug details: state="+ this.getState() );
        }
    }

    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine().  But this method is a special deviation, as it allows us to get the 'current-line' over-n-over again.
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    public String currentLineOrNull() {
        return currentLineOrNull_Impl();
    }

    /**
     * This method exists primarily to allow this class to invoke currentLineOrNull() (especially for getState()).. without invoking sub-class' overridden version.
     * @return either null (graceful failure) or the next string in the list of lines
     */
    private final String currentLineOrNull_Impl()
    {   // final String HDR = CLASSNAME +": currentLineOrNull_Impl(): ";
        if ( this.currentLineNum > 0 && this.currentLineNum <= this.lines.size() ) {
            // return this.nextLine(); // this will Not be null.. just because of the call to hasNextLine() above.
            return this.lines.get ( this.currentLineNum - 1 );
        } else {
            return null;
        }
    }

    //===========================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //===========================================================================

    /**
     * What was the file that class has ingested for processing
     * @return the fileName exactly as passed to go() method.
     */
    public String getFileName()  {
        return this.fileName;
    }

    /**
     * Use this in conjunction with this.currentLine().    Line numbers start with 1, as all text-editors show.  If there is an error in the file being processed, the error-message will note the line#.
     * @return the line # within the process that is 'current'.  It will be -1, if anything failed in 'reading in' the fileName.
     */
    public int getLineNum()  {
        return this.currentLineNum;
    }

    /**
     * Statistics about the batch file being processed.  Currently only # of Non-Comment and Non-empty lines are counted.
     * @return a number &gt;=0 (if no errors parsing / loading batch-file).. or -1 if any trouble with ConfigFile.
     */
    public int getCommandCount()  {
        return (this.lines != null) ? this.lines.size(): -1;
    }

    //===========================================================================
    /**
     * After successfully opening a file.. you can get state-details (which point of the ConfigFile are we at currently).
     * @return something like: ConfigFile [@mapsBatch1.txt] @ line# 2 = [line contents as-is]
     */
    public String getState() {
        if ( this.fileName == null || this.currentLineNum <= 0 )
            return "ConfigFile ["+ this.fileName +"] is in invalid state";
        else {
            final String s = "@ line# "+ this.origLineNumbers.get(this.currentLineNum - 1) +" = ["+ this.currentLineOrNull_Impl() +"]";
            if ( this.fileName.startsWith("@") ) {
                return "File-name: '"+ this.fileName.substring(1) +"' "+ s;
            } else {
                return "inline-content provided: '"+ this.fileName +"' "+ s;
            }
        }
    }

    //===========================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //===========================================================================

    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine()
     *  @return true or false
     */
    public boolean hasNextLine() {
        if ( this.lines == null ) return false;
        if ( this.iterator == null ) {
            this.rewind();
        }
        return this.iterator.hasNext();
    }

    /** This class aims to AUGMENTS java.util.Scanner's hasNextLine() and nextLine(), but needs to be used with CAUTION.
     *  @return the next line or NULL
     *  @throws IndexOutOfBoundsException if this method is NOT-PROPERLY called within a loop() based on the conditional: hasNextLine()
     */
    public String peekNextLine() throws IndexOutOfBoundsException {
        if ( this.lines == null ) return null;
        if ( this.iterator == null ) return null;
        return this.lines.get ( this.getLineNum() ); // compare this return-value with that of currentLine()
    }

    //===========================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //===========================================================================

    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine()
     *  @return 0.0001% chance (a.k.a. code bugs) that this is null. Returns the next string in the list of lines
     *  @throws Exception in case this class is messed up or hasNextLine() is false or has Not been invoked appropriately
     */
    public String nextLine() throws Exception {
            return nextLineOrNull();
    }

    //===========================================================================
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine() - but will return null if any errors or invalid sequence of method invocations
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    public String nextLineOrNull() {
        final String HDR = CLASSNAME +": nextLineOrNull(): ";
        this.iterator.next();
        this.currentLineNum ++;
        resetFlagsForEachLine(); // so that the isXXX() methods invoked of this class -- now that we're on NEW/NEXT line -- will NOT take a shortcut!

        if ( this.verbose ) System.out.println( HDR +"\t" + this.getState() );
        return this.lines.get ( this.currentLineNum - 1 );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** As com.esotericsoftware.yamlBeans has some magic where Keys are NOT strings! ..
     *  In order for me to add new entries to the _map created by that library, I need to go thru hoops.
     *  @param _filename the full path to the file (don't assume relative paths will work ALL the time)
     *  @param _ok2TrimWhiteSpace true or false, whether to REMOVE any leading and trailing whitespace.  Example: For YAML processing, trimming is devastating.
     *  @param _bCompressWhiteSpace whether to replace multiple successive whitespace characters with a single space.
     *  @return true (successful and NO errors) or false (any error or issue/trouble whatsoever)
     *  @throws java.lang.Exception either this function throws or will return false.
     */
    public boolean openFile( final String _filename, final boolean _ok2TrimWhiteSpace, final boolean _bCompressWhiteSpace ) throws Exception
    {
        final String HDR = CLASSNAME +": openFile("+ _filename +","+ _ok2TrimWhiteSpace +","+ _bCompressWhiteSpace +"): ";

        this.ok2TrimWhiteSpace = _ok2TrimWhiteSpace;
        this.bCompressWhiteSpace = _bCompressWhiteSpace;
        this.reset(); // just in case.
        this.fileName = _filename;

        String line = null;
        try {
            java.util.Scanner scanner = null;
            if ( this.fileName.startsWith("@") ) {
                final java.io.InputStream istrm = new java.io.FileInputStream( this.fileName.substring(1) );
                scanner = new java.util.Scanner( istrm );
            } else {
                scanner = new java.util.Scanner( this.fileName ); // what I thought was filename is actually INLINE-CONTENT to parse
            }

            if ( this.verbose ) System.out.println( HDR +"successfully opened file [" + this.fileName +"]" );

            // different way to detect comments, and to remove them.
			Pattern emptyPattern        = Pattern.compile( "^\\s*$" ); // empty line
			Pattern hashlinePattern     = Pattern.compile( "^#.*" ); // from start of line ONLY
			Pattern hashPattern         = Pattern.compile(  "\\s*#.*" );
			Pattern slashlinePattern    = Pattern.compile( "^//.*" ); // from start of line ONLY
			Pattern slashPattern        = Pattern.compile(  "\\s*//.*" );
			Pattern dashlinepattern = Pattern.compile( "^--.*" );

            // int nonEmptyCmdLinenum = 0;

            //---------------------------
            for ( int origLineNum=1;   scanner.hasNextLine();   origLineNum++ ) {
                line = scanner.nextLine();

                //---------------------------
                if ( _bCompressWhiteSpace ) {
                    line = line.replaceAll("\\s\\s+", " ");
                }
                if ( this.verbose ) System.out.println( HDR +"AS-IS line=[" + line +"]" );

                Matcher emptyMatcher = emptyPattern.matcher( line );
                if ( emptyMatcher.matches() ) continue;
                Matcher hashlineMatcher = hashlinePattern.matcher( line );
                if ( hashlineMatcher.matches() ) continue;
                Matcher slashlineMatcher = slashlinePattern.matcher( line );
                if ( slashlineMatcher.matches() ) continue;
                Matcher dashlineMatcher = dashlinepattern.matcher( line );
                if ( dashlineMatcher.matches() ) continue;

                //---------------------------
                // if we are here, then the line does ___NOT___ start with # or.. // or --
                Matcher hashMatcher     = hashPattern.matcher( line );
                if (hashMatcher.find()) {
                    if ( this.verbose ) System.out.println( HDR +"I found the text "+ hashMatcher.group() +" starting at index "+  hashMatcher.start()+ " and ending at index "+ hashMatcher.end() );    
                    line = line.substring( 0, hashMatcher.start() );
                    if ( _ok2TrimWhiteSpace ) line = line.trim(); // trim both leading and trailing whitespace
                }
                Matcher slashMatcher    = slashPattern.matcher( line );
                if (slashMatcher.find()) {
                    if ( this.verbose ) System.out.println( HDR +"I found the text "+ slashMatcher.group() +" starting at index "+  slashMatcher.start() +" and ending at index "+ slashMatcher.end() );    
                    line = line.substring( 0, slashMatcher.start() );
                    if ( _ok2TrimWhiteSpace ) line = line.trim(); // trim both leading and trailing whitespace
                }

                // after all the comment pre-processing above.. check if the line has become equivalent to empty-line.. 
                emptyMatcher = emptyPattern.matcher( line ); // after all the above trimming, is the line pretty much whitespace?
                if ( emptyMatcher.matches() ) continue;

                //---------------------------
                if ( _ok2TrimWhiteSpace ) line = line.trim(); // trim both leading and trailing whitespace
                if ( this.verbose ) System.out.println( HDR +" TRIMMED line=[" + line +"]" );

                //---------------------------
                // nonEmptyCmdLinenum ++;
                this.lines.add( line );
                this.origLineNumbers.add( origLineNum );
                // alternatively use a Map<Integer,Integer>.put( nonEmptyCmdLinenum, origLineNum )
            }

            //---------------------------
            scanner.close();
            this.rewind(); // rewing the pointer to the 1st line in the batch file.
            return true;

        // scanner.hasNext() only throws a RUNTIMEEXCEPTION: IllegalStateException - if this scanner is closed
        // scanner.next() only throws a RUNTIMEEXCEPTION: NoSuchElementException - if no more tokens are available

		} catch (PatternSyntaxException e) {
			e.printStackTrace(System.err); // PatternSyntaxException! too fatal an error, to allow program/application to continue to run.
			System.err.println( "\n\n"+ HDR +"Unexpected Internal ERROR, while checking for patterns for line= [" + line +"]. Exception Message: "+ e );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err); // IOException! too fatal an error, to allow program/application to continue to run.
            System.err.println( "\n\n"+ HDR +"Failure to read/write IO for file ["+ this.fileName +"]. Exception Message: "+ e );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        // } catch (Exception e) {
        //     e.printStackTrace(System.err);// General Exception! too fatal an error, to allow program/application to continue to run.
        //     System.err.println( HDR +"Unknown Internal error: "+ e );
        }
        return false;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This deepClone function is VERY MUCH necessary, as No cloning-code can handle 'transient' variables in this class.
     *  @param _orig what you want to deep-clone
     *  @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     */
    public static ConfigFileScanner deepClone( ConfigFileScanner _orig ) {
        try {
            final ConfigFileScanner newobj = Utils.deepClone( _orig );
            newobj.deepCloneFix();
            return newobj;
        } catch (Exception e) {
			e.printStackTrace(System.err); // Static Method. So.. can't avoid dumping this on the user.
            return null;
        }
    }

    /**
     * In order to allow deepClone() to work seamlessly up and down the class-hierarchy.. I should allow subclasses to EXTEND (Not semantically override) this method.
     */
    protected void deepCloneFix() {
            // because this class has at least one TRANSIENT class-variable.. ..
            // we need to 'restore' that object's transient variable to a 'replica'
            this.iterator = this.lines.iterator();
            for ( int ix = 0; ix < this.currentLineNum; ix++ )
                this.iterator.next(); // This will advance this.iterator to the right position, as java.lang.Iterator is NOT clonable/NOT SERIALIZABLE.
                // we rarely CLONE an object of this class, when it's still pointing to line #1.  So, this ABOVE for-loop is just fine.
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================
    // For unit-testing purposes only
    public static void main(String[] args) {
        class TestConfigFileScanner extends ConfigFileScanner {
            private static final long serialVersionUID = 1L;
            public TestConfigFileScanner(boolean _verbose) {
                super(_verbose);
            }
            protected void resetFlagsForEachLine() {}
        };
        try {
            final ConfigFileScanner o = new TestConfigFileScanner(true);
            o.openFile( args[0], true, true );
            while (o.hasNextLine()) {
                System.out.println(o.nextLine());
                o.getState();
            }
		} catch (Exception e) {
			e.printStackTrace(System.err); // main().  For Unit testing
			System.err.println( CLASSNAME + ": main(): Unexpected Internal ERROR, while processing " + ((args==null || args.length<=0)?"[No CmdLine Args":args[0]) +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
    }

}
