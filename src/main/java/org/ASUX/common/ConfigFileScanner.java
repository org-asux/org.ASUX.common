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

/**
 *  <p>This is part of org.ASUX.common GitHub.com project and the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>This class is a bunch of tools to help make it easy to work with the Configuration and Propertyfiles - while making it very human-friendly w.r.t .comments etc...</p>
 */
public abstract class ConfigFileScanner implements java.io.Serializable {

    private static final long serialVersionUID = 110L;
    public static final String CLASSNAME = "org.ASUX.common.ConfigFileScanner";

    //--------------------------------------------------------
    protected final boolean verbose;

    private String fileName = null;
    protected ArrayList<String> lines; // = new ArrayList<>();
    private ArrayList<Integer> origLineNumbers; //  = new ArrayList<>();

    private transient Iterator<String> iterator = null;  // <<- transient class variable.  Will not be part of deepClone() method.
    private int currentLineNum = -1;

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

    /**
     * After successfully opening a file.. you can get state-details (which point of the ConfigFile are we at currently).
     * @return something like: ConfigFile [@mapsBatch1.txt] @ line# 2 = [line contents as-is]
     */
    public String getState() {
        if ( this.fileName == null || this.currentLineNum <= 0 )
            return "ConfigFile ["+ this.fileName +"] is in invalid state";
        else
            return "ConfigFile=["+ this.fileName +"] @ line# "+ this.origLineNumbers.get(this.currentLineNum - 1) +" = ["+ this.currentLineOrNull() +"]";
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

    //===========================================================================
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine()
     *  @return either null (graceful failure) or the next string in the list of lines
     *  @throws Exception in case this class is messed up or hasNextLine() is false or has Not been invoked appropriately
     */
    public String nextLine() throws Exception {
        if ( this.hasNextLine() ) {
            this.iterator.next();
            this.currentLineNum ++;
            resetFlagsForEachLine(); // so that the isXXX() methods invoked of this class -- now that we're on NEW/NEXT line -- will NOT take a shortcut!

            if ( this.verbose ) System.out.println( CLASSNAME +": nextLine(): \t" + this.getState() );
            return this.lines.get ( this.currentLineNum - 1 );
        } else {
            this.currentLineNum = -1;
            // WARNING: Do not invoke this.rewind() or this.reset().  It will INCORRECTLY change the value of this.iterator
            throw new Exception( CLASSNAME +": nextLine(): hasNextLine() is false! CurrentLineNum=" +this.currentLineNum +".  Debug-details: state="+ this.getState() );
        }
    }

    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine() - but will return null if any errors or invalid sequence of method invocations
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    public String nextLineOrNull() {
        if ( this.hasNextLine() ) {
            this.iterator.next();
            this.currentLineNum ++;
            resetFlagsForEachLine(); // so that the isXXX() methods invoked of this class -- now that we're on NEW/NEXT line -- will NOT take a shortcut!

            if ( this.verbose ) System.out.println( CLASSNAME +": nextLine(): \t " + this.getState() );
            return this.lines.get ( this.currentLineNum - 1 );
        } else {
            this.currentLineNum = -1;
            // WARNING: Do not invoke this.rewind() or this.reset().  It will INCORRECTLY change the value of this.iterator
            return null;
        }
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
    public boolean openFile( final String _filename, final boolean _ok2TrimWhiteSpace, final boolean _bCompressWhiteSpace ) throws Exception {

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

            if ( this.verbose ) System.out.println( CLASSNAME + ": openFile(): successfully opened file [" + this.fileName +"]" );

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
                if ( this.verbose ) System.out.println( CLASSNAME + ": openFile(): AS-IS line=[" + line +"]" );

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
                    if ( this.verbose ) System.out.println( CLASSNAME +": openFile(): I found the text "+ hashMatcher.group() +" starting at index "+  hashMatcher.start()+ " and ending at index "+ hashMatcher.end() );    
                    line = line.substring( 0, hashMatcher.start() );
                    if ( _ok2TrimWhiteSpace ) line = line.trim(); // trim both leading and trailing whitespace
                }
                Matcher slashMatcher    = slashPattern.matcher( line );
                if (slashMatcher.find()) {
                    if ( this.verbose ) System.out.println( CLASSNAME +": openFile(): I found the text "+ slashMatcher.group() +" starting at index "+  slashMatcher.start() +" and ending at index "+ slashMatcher.end() );    
                    line = line.substring( 0, slashMatcher.start() );
                    if ( _ok2TrimWhiteSpace ) line = line.trim(); // trim both leading and trailing whitespace
                }

                // after all the comment pre-processing above.. check if the line has become equivalent to empty-line.. 
                emptyMatcher = emptyPattern.matcher( line ); // after all the above trimming, is the line pretty much whitespace?
                if ( emptyMatcher.matches() ) continue;

                //---------------------------
                if ( _ok2TrimWhiteSpace ) line = line.trim(); // trim both leading and trailing whitespace
                if ( this.verbose ) System.out.println( CLASSNAME + ": openFile(): TRIMMED line=[" + line +"]" );

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
			e.printStackTrace(System.err);
			System.err.println(CLASSNAME + ": openFile(): Unexpected Internal ERROR, while checking for patterns for line= [" + line +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": openFile(): \n\nFailure to read/write IO for file ["+ this.fileName +"]" );
        // } catch (Exception e) {
        //     e.printStackTrace(System.err);
        //     System.err.println( CLASSNAME + ": openFile(): Unknown Internal error:.");
        }
        return false;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This deepClone function is unnecessary, if you can invoke org.apache.commons.lang3.SerializationUtils.clone(this)
     *  @param _orig what you want to deep-clone
     *  @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     */
    public static ConfigFileScanner deepClone(ConfigFileScanner _orig) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(_orig);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            final ConfigFileScanner newobj = (ConfigFileScanner) ois.readObject();

            // because this class has at least one TRANSIENT class-variable.. ..
            // we need to 'restore' that object's transient variable to a 'replica'
            newobj.iterator = newobj.lines.iterator();
            for ( int ix = 0; ix < newobj.currentLineNum; ix++ )
                newobj.iterator.next(); // This will advance this.iterator to the right position, as java.lang.Iterator is NOT clonable/NOT SERIALIZABLE.
                // we rarely CLONE an object of this class, when it's still pointing to line #1.  So, this ABOVE for-loop is just fine.

            return newobj;

        } catch (java.io.IOException e) {
			e.printStackTrace(System.err);
            return null;
        } catch (ClassNotFoundException e) {
			e.printStackTrace(System.err);
            return null;
        }
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
			e.printStackTrace(System.err);
			System.err.println( CLASSNAME + ": main(): Unexpected Internal ERROR, while processing " + ((args==null || args.length<=0)?"[No CmdLine Args":args[0]) +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
    }

}
