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

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import static org.junit.Assert.*;

/**
 *  <p>This is part of org.ASUX.common GitHub.com project and the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.<br>
 *  This class and its subclasses ({@link ScriptFileScanner} and {@link PropertiesFileScanner}) are key to the org.ASUX projects.</p>
 *  <p><b>Strongly recommend you use  {@link PropertiesFileScanner}, {@link ScriptFileScanner} or  {@link OSScriptFileScanner}</b> instead of this class, unless you need something very specifically limited in capability.</p>
 *  <p>This class represents a bunch of tools, to help make it easy to work with the <em>Configuration</em> and <em>Property</em> files + allowing those file to be very human-friendly w.r.t .comments etc...<br>
 *  This class immediately loads the entire contents into Memory ({@link #openFile(Object, boolean, boolean)}), and offers the ability to {@link #deepClone(ConfigFileScanner)} itself (which is a fantastic feature for recursions/loops, as very well demonstrated by subclasses like BatchFileGrammer/BatchFileProcessor of org.ASUX.YAML project).</p>
 *  <p>This class implements _ONLY_ the line-based interface of java.util.Scanner ({@link #hasNextLine()}, {@link #nextLine()}, {@link #delimiter()}, {@link #useDelimiter(String)}).<br>
 *    In addition, there are important enhancements like '<em>{@link #currentLine()}, {@link #getCommandCount()}, {@link #getLineNum()}, {@link #getState()}</em>', which help with showing error-messages &amp; stats regarding the Configuration-file being processed.</p>
 *  <p>In addition, this offers the ability to 'peek ahead' {@link #peekNextLine()}, to see what's on the next line, without have to invoke hasNextLine(), nextLine() .. and the ability to {@link #rewind()} to the beginning of the file and start scanning all over again.  To efficiently re-use Java objects, you can take advantage of {@link #reset()}, which will force you to invoke {@link #openFile(Object, boolean, boolean)} again before you can use an existing object of this class.</p>
 *  <p>Finally, the class offers you, both the right-way and wrong-way :-) .. to handle errors within Config-files.  For the right-way, it will throw Exceptions (from nextLine(), currentLine()).  ALternatively, if you'd prefer NO Exceptions, and instead prefer to have 'null' returned, you can use the 'NotNull' variant methods: {@link #nextLineOrNull()} and {@link #currentLineOrNull()}</p>
 *  <p>There are many protected methods (like: ), that can be leveraged by sub-classes.  To better understand how to do that, spend time to understand {@link ConfigFileScannerL2}, {@link PropertiesFileScanner} and {@link ScriptFileScanner}</p>
 */
public abstract class ConfigFileScanner implements java.io.Serializable {

    private static final long serialVersionUID = 110L;
    public static final String CLASSNAME = ConfigFileScanner.class.getName();

    //--------------------------------------------------------
    protected boolean verbose;

    protected Object fileName = null;
    private String delimiter = System.lineSeparator(); // unlike the other instance variables, this is private, as we have delimiter() and useDelimiter() methods/getter/setter

    /** ok2TrimWhiteSpace true or false, whether to REMOVE any leading and trailing whitespace.  Example: For YAML processing, trimming is devastating. */
    protected boolean ok2TrimWhiteSpace = true; // defaults
    /** bCompressWhiteSpace whether to replace multiple successive whitespace characters with a single space. */
    protected boolean bCompressWhiteSpace = true; // defaults

    protected ArrayList<String> lines; // = new ArrayList<>();
    protected ArrayList<Integer> origLineNumbers; //  = new ArrayList<>();

    protected transient Iterator<String> iterator = null;  // <<- transient class variable.  Will not be part of deepClone() method.

    /** ATTENTION: This is supposed to indicate line-numbers as humans see it (starting from 1 onwards).  _NOT_ as C-array-index! */
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

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  This method is identical to that of java.util.Scanner's <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Scanner.html#delimiter--">delimiter()</a> method.
     *  @return what is being used.  Default is newline (OS-specific)
     */
    public String delimiter() { return this.delimiter; }
    /**
     *  This method is identical to that of java.util.Scanner's <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Scanner.html#useDelimiter-java.lang.String-">useDelimiter()</a> method.
     * @param _s a NotNull String
     */
    public void useDelimiter( final String _s ) { this.delimiter = _s; }

    public boolean getVerbose() {   return this.verbose;    }
    public void setVerbose( final boolean _verbose ) {  this.verbose = _verbose; }

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

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine().  But this method is a special deviation, as it allows us to get the 'current-line' over-n-over again.
     *  @return the next string in the list of lines (else an exception is thrown)
     *  @throws Exception in case this class is messed up or hasNextLine() is false or has Not been invoked appropriately
     */
    public String currentLine() throws Exception {
        return ConfigFileScanner.currentLine( this );
    }

    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine().  But this method is a special deviation, as it allows us to get the 'current-line' over-n-over again.
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    public String currentLineOrNull() {
        return ConfigFileScanner.currentLineOrNull( this );
    }

    //===========================================================================
    /**
     *  <p>This method exists primarily to allow this class to invoke currentLine() (especially for getState()).. without invoking sub-class' overridden version.</p>
     *  <p>Turns out I need to explicitly call this-specifc method - from within subclasses.  So, you can do that as: ConfigFileScanner.currentLine( this );
     *  @param __this since this is a static method, pass in 'this' (within the subclasses)
     *  @throws Exception see {@link #currentLine()}
     *  @return see {@link #currentLine()}
     */
    protected static final String currentLine( final ConfigFileScanner __this ) throws Exception
    {   final String HDR = CLASSNAME +": (STATIC-METHOD)currentLine(): ";
        if ( __this.currentLineNum > 0 && __this.currentLineNum <= __this.lines.size() ) {
            return __this.lines.get ( __this.currentLineNum - 1 );
        } else {
            throw new Exception( HDR +": currentLine(): invalid currentLineNum=" +__this.currentLineNum +".  Debug details: state="+ __this.getState() );
        }
    }

    /**
     *  <p>This method exists primarily to allow this class to invoke currentLineOrNull() (especially for getState()).. without invoking sub-class' overridden version.</p>
     *  <p>Turns out I need to explicitly call this-specifc method - from within subclasses.  So, you can do that as: ConfigFileScanner.currentLineOrNull( this );
     *  @param __this since this is a static method, pass in 'this' (within the subclasses)
     * @return see {@link #currentLineOrNull()}
     */
    protected static final String currentLineOrNull( final ConfigFileScanner __this )
    {
        if ( __this.currentLineNum > 0 && __this.currentLineNum <= __this.lines.size() ) {
            return __this.lines.get ( __this.currentLineNum - 1 );
        } else {
            return null;
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * What was the file that class has ingested for processing
     * @return the fileName exactly as passed to go() method.
     */
    public String getFileName()  {
        if ( this.fileName instanceof String )
            return this.fileName.toString();
        else
            return "@java.io.InputStream.class instance";
    }

    /**
     * <p>ATTENTION: Line numbers start with 1, as all text-editors show.<br>
     *      If there is an error in the file being processed, the error-message will note the line#.</p>
     * <p>Use this in conjunction with this.currentLine().</p>
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
        return ConfigFileScanner.getState( this );
        // if ( this.fileName == null || this.currentLineNum < 0 )
        //     return "ConfigFile ["+ this.getFileName() +"] is in invalid state";
        // else if ( this.currentLineNum == 0 )
        //     return "ConfigFile ["+ this.getFileName() +"] has _JUST_ been Opened, and nextLine() has NOT YET been invoked";
        // else {
        //     final String s = "@ line# "+ this.origLineNumbers.get( this.currentLineNum - 1) +" = ["+ ConfigFileScanner.currentLineOrNull( this ) +"]";
        //     if ( this.getFileName().startsWith("@") ) {
        //         return "File-name: '"+ this.getFileName().substring(1) +"' "+ s;
        //     } else {
        //         return "inline-content/InputStream provided: '"+ this.getFileName() +"' "+ s;
        //     }
        // }
    }

    /**
     *  In order to allow subclassses to invoke the IMPLEMENTATION of {@link #getState()}, as sub-classes like ConfigFileScannerL3 (for good reasons) override {@link #getState()}.
     *  @param __this since this is a static method, pass in 'this' (or ANY of the subclasses)
     *  @return something like: ConfigFile [@mapsBatch1.txt] @ line# 2 = [line contents as-is]
     */
    public static String getState( final ConfigFileScanner __this ) {
        if ( __this.fileName == null || __this.currentLineNum < 0 )
            return "ConfigFile ["+ __this.getFileName() +"] is in invalid state";
        else if ( __this.currentLineNum == 0 )
            return "ConfigFile ["+ __this.getFileName() +"] has _JUST_ been Opened, and nextLine() has NOT YET been invoked";
        else {
            final String s = "@ line# "+ __this.origLineNumbers.get( __this.currentLineNum - 1) +" = ["+ ConfigFileScanner.currentLineOrNull( __this ) +"]";
            if ( __this.getFileName().startsWith("@") ) {
                return "File-name: '"+ __this.getFileName().substring(1) +"' "+ s;
            } else {
                return "inline-content/InputStream provided: '"+ __this.getFileName() +"' "+ s;
            }
        }
    }


    //===========================================================================
    /**
     *  <p>Returns what it read into memory (from whatever the source-of-input provided to {@link #openFile(Object, boolean, boolean)}).. as a single string.</p>
     *  <p>What you'll find most useful is that the delimiters hae been converted into EOLN (platform-specific).<br>
     *     You can use default delimiter ';' to convert _INLINE_ strings on the command-line into a multi-line input-YAML!</p>
     *  @return a NotNull String (at a minimum you'll get an empty-string)
     */
    @Override
    public String toString() {
        if ( this.lines == null || this.lines.size() <= 0 )
            return "";
        else {
            final StringBuffer s = new StringBuffer();
            for( String line: lines ) {
                s.append( line ).append( System.lineSeparator() );
            } // for
            return s.toString();
        }
    }

    //===========================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //===========================================================================

    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine()
     *  @return true or false
     *  @throws Exception to enable subclasses to more sophisticated things (like built-in commands) and throw exceptions in that context
     */
    public boolean hasNextLine() throws Exception {
        return ConfigFileScanner.hasNextLine( this );
    }

    /**
     *  <p>This method exists primarily to allow this class to invoke hasNextLine() (especially for getState()).. without invoking sub-class' overridden version.</p>
     *  <p>Turns out I need to explicitly call this-specifc method - from within subclasses.  So, you can do that as: ConfigFileScanner.hasNextLine( this );
     *  @param __this since this is a static method, pass in 'this' (or ANY of the subclasses)
     * @return see {@link #hasNextLine()}
     */
    public static final boolean hasNextLine( final ConfigFileScanner __this )
    {   // final String HDR = CLASSNAME +": (STATIC-METHOD)hasNextLine(): ";
        if ( __this.lines == null ) return false;
        if ( __this.iterator == null )
            __this.rewind();
        return __this.iterator.hasNext();
    }

    //===========================================================================
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
     *  @throws Exception in case subclasses override.. or hasNextLine() is false or has Not been invoked appropriately
     */
    public String nextLine() throws Exception {
        return ConfigFileScanner.nextLineOrNull( this );
    }

    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine() - but will return null if any errors or invalid sequence of method invocations
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    public String nextLineOrNull() {
        return ConfigFileScanner.nextLineOrNull( this );
    }

    /** This is an enhancement to java.util.Scanner.  It is most useful, to implement FOR Loops or IF-statements (as demonstrated in org.ASUX.YAML.BatchCmdProcessor's skip2MatchingEnd())
     */
    public void skipLine() {
        ConfigFileScanner.nextLineOrNull( this );
    }

    //===========================================================================
    /**
     *  <p>This method is the common implementation body for both nextLine() and nextLineOrNull().</p>
     *  <p>Turns out I need to explicitly call this.nextLineOrNull() - from within subclasses.  So, you can do that as: ConfigFileScanner.nextLineOrNull( this );
     *  @param __this since this is a static method, pass in 'this'
     * @return see {@link #nextLineOrNull()}
     */
    protected static final String nextLineOrNull( final ConfigFileScanner __this ) {
        final String HDR = CLASSNAME +": (STATIC-METHOD)nextLineOrNull(): ";
        __this.iterator.next();
        __this.currentLineNum ++;
        __this.resetFlagsForEachLine(); // so that the isXXX() methods invoked of this class -- now that we're on NEW/NEXT line -- will NOT take a shortcut!

        if ( __this.verbose ) System.out.println( HDR +"\t" + __this.getState() );
        return __this.lines.get ( __this.currentLineNum - 1 );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** As com.esotericsoftware.yamlBeans has some magic where Keys are NOT strings! ..
     *  In order for me to add new entries to the _map created by that library, I need to go thru hoops.
     *  @param _input Either it's a java.lang.String representing the full-path to the file (don't assume relative paths will work ALL the time).. or .. an __INLINE_STRING__ content with delimiter() as newlines, or .. a NotNull reference to java.io.InputStream
     *  @param _ok2TrimWhiteSpace true or false, whether to REMOVE any leading and trailing whitespace.  Example: For YAML processing, trimming is devastating.
     *  @param _bCompressWhiteSpace whether to replace multiple successive whitespace characters with a single space.
     *  @return true (successful and NO errors) or false (any error or issue/trouble whatsoever)
     *  @throws java.io.FileNotFoundException If filename passed as '@...' does Not exist.
     *  @throws java.io.IOException any trouble reding the file passed in as '@...'
     *  @throws java.lang.Exception either this function throws or will return false.
     */
    public boolean openFile( final Object _input, final boolean _ok2TrimWhiteSpace, final boolean _bCompressWhiteSpace )
                    throws java.io.FileNotFoundException, java.io.IOException, Exception
    {
        final String HDR = CLASSNAME +": openFile("+ _input +","+ _ok2TrimWhiteSpace +","+ _bCompressWhiteSpace +"): ";

        this.ok2TrimWhiteSpace = _ok2TrimWhiteSpace;
        this.bCompressWhiteSpace = _bCompressWhiteSpace;
        this.reset(); // just in case.
        this.fileName = _input;

        String line = null;
        try {
            java.util.Scanner scanner = null;
            if ( this.fileName.toString().startsWith("@") ) {
                if ( this.verbose ) System.out.println( HDR +"this.fileName.toString()='"+ this.fileName.toString() +"'" );
                final InputStream istrm = new FileInputStream( this.fileName.toString().substring(1) );
                scanner = new java.util.Scanner( istrm );
            } else {
                if ( this.fileName instanceof String ) {
                    // what I thought was filename is __ACTUALLY__  __INLINE-CONTENT__ to be parsed as-is
                    scanner = new java.util.Scanner( this.fileName.toString() );
                    if ( this.verbose ) System.out.println( HDR +" using special delimiter <"+ scanner.delimiter() +"> for INLINE Batch-commands provided via cmdline" );
                } else if ( this.fileName instanceof InputStream ) {
                    scanner = new java.util.Scanner( (InputStream) this.fileName );
                    if ( this.verbose ) System.out.println( HDR +" content provided via java.io.InputStream" );
                } else {
                    throw new Exception();
                }
            }
            if ( this.verbose ) System.out.println( HDR +"successfully opened file [" + this.fileName +"]" );

            scanner.useDelimiter( this.delimiter );
            if ( this.verbose ) System.out.println( HDR +" using special delimiter <"+ scanner.delimiter() +"> for INLINE Batch-commands provided via cmdline" );

            // different way to detect comments, and to remove them.
			Pattern emptyPattern        = Pattern.compile( "^\\s*$" ); // empty line
			Pattern hashlinePattern     = Pattern.compile( "^#.*" ); // from start of line ONLY
			Pattern hashPattern         = Pattern.compile(  "\\s*#.*" );
			Pattern slashlinePattern    = Pattern.compile( "^//.*" ); // from start of line ONLY
			Pattern slashPattern        = Pattern.compile(  "\\s*//.*" );
			Pattern dashlinepattern = Pattern.compile( "^--.*" );

            // int nonEmptyCmdLinenum = 0;

            //---------------------------
            for ( int origLineNum=1;   scanner.hasNext();   origLineNum++ ) {
                line = scanner.next();

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
            this.rewind(); // rewind the pointer to the 1st line in the batch file.
            return true;

        // scanner.hasNext() only throws a RUNTIMEEXCEPTION: IllegalStateException - if this scanner is closed
        // scanner.next() only throws a RUNTIMEEXCEPTION: NoSuchElementException - if no more tokens are available

		} catch (PatternSyntaxException e) {
			e.printStackTrace(System.err); // PatternSyntaxException! too fatal an error, to allow program/application to continue to run.
			System.err.println( "\n\n"+ HDR +"Unexpected Internal ERROR, while checking for patterns for line= [" + line +"]. Exception Message: "+ e );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        // } catch (java.io.IOException e) {
        //     e.printStackTrace(System.err); // IOException! too fatal an error, to allow program/application to continue to run.
        //     System.err.println( "\n\n"+ HDR +"Failure to read/write IO for file ["+ this.fileName +"]. Exception Message: "+ e );
		// 	System.exit(91); // This is a serious failure. Shouldn't be happening.
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
    protected void deepCloneFix( final ConfigFileScanner _orig ) {
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
