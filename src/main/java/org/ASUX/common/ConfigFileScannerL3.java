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

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import static org.junit.Assert.*;

/**
 *  <p>This is part of org.ASUX.common GitHub.com project and the <a href= "https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p><b>Strongly recommend you use  {@link PropertiesFileScanner}, {@link ScriptFileScanner} or  {@link OSScriptFileScanner}</b> instead of this class, unless you need something very specifically limited in capability.</p>
 *  <p>Compared with {@link ConfigFileScannerL2}, this class will invisibly and automatically step through any 'include sub-filename' within the batch-script.  That is, you can't tell which file has the command being executed, unless you're willing to parse the string returned by {@link #getState()} (which is used to offer you accurate details - on cmdline - in case of errors in your script.).<br>
 *      This class is <b>most useful when</b> you'd like the subclasses be completely ignorant of whether there is 'nested' included-files.<br>
 *      In fact, {@link PropertiesFileScanner}, {@link ScriptFileScanner} or  {@link OSScriptFileScanner} are all sub-classes of this class.</p>
 *  <p>This class extends {@link ConfigFileScanner}.<br>
 *  This class and its subclasses ({@link ScriptFileScanner} are key to the org.ASUX projects.</p>
 *  <p>This class represents a bunch of tools, to help make it easy to work with the <em>Configuration</em> and <em>Property</em> files + allowing those file to be very human-friendly w.r.t .comments etc...</p>
 *  <p>This class, like BASH and CSH, <b>offers 'built-in' commands</b>.  Specifically,<ul><li>'<code>echo</code>' prefix </li><li> '<code>print</code>' command </li><li> '<code>include &gt;FILE&gt;</code>' command. </li></ul>(Advanced Developers: see {@link #execBuiltInCommand()}).<br>
 *     When you loop through the contents using {@link ConfigFileScanner#hasNextLine()} and {@link ConfigFileScanner#nextLine()}, you simply <b>will Not see these built-in</b> commands.<br>
 *     Example: if you have a Config-file containing <b>just built-in</b> commands, then your invocation of {@link ConfigFileScanner#hasNextLine()} will never be true!<br>
 *  <p>The '<b>echo</b>' prefix allows you to SPECIFICALLY see when a line in the Config-file was read/processed</p>
 *  <p>The '<b><code>include &gt;FILE&gt;</code></b>' (which is allows you to transparently include another file!</p>
 *  <p>Both 'echo' and 'print' may look identical in function, but 'print' is the <em>semantic-equivalent</em> of either 'System.out.print()' or 'echo' of BASH/ CSH (It includes all '<em>variable-substitution</em>').<br>
 *     So.. why need 'echo'?<br>
 *     Well, 'echo' is <b>more PRIMITIVE</b> and very useful for debugging your config-files. 'echo' shows the command __TO BE__ executed, both <b>before</b> and <b>after</b> __ALL__ '<code>${}</code>' MACRO-Replacements (a.k.a. '<em>variable-substitution</em>').<br>
 *  In that sense, this combination of 'echo' and 'print' is MORE SOPHISTICATED and MORE CAPABLE that the 'echo' built-in command in BASH, /bin/sh, /bin/csh.</p>
 *  <p>T evaluate variable-substitution expressions <em>JUST LIKE</em> a Bash or /bin/sh or /bin/tcsh does (example: <code>${ASUX::VARIABLE}</code>) - as long as you provide a Not-Null java.util.Properties instance as constructor-argument.</p>
 *  <p><b>ATTENTION: You must use the '<code>ASUX::</code>' prefix, or the variable-substition will Not happen</b>.<br>
 *     Why this special-prefix?  Simple reason!  This org.ASUX project demonstrates the need to create Config and Output files that have ${} expressions for <b>other</b> software to use.  A great example: The org.ASUX.AWS and subjects create Confile files containing ${} expressions for AWS CFN-SDK to further parse.</p>
 */
public class ConfigFileScannerL3 extends ConfigFileScanner {

    private static final long serialVersionUID = 112L;
    public static final String CLASSNAME = ConfigFileScannerL3.class.getName();

    public static final String REGEXP_NAME_PREFIXCHARSET = "a-zA-Z0-9$_/\\.";
    public static final String REGEXP_NAMESUFFIX = "[${}@%a-zA-Z0-9\\.,:_/+-]+"; // need to support ${ASUX::foreach.index+1} in org.ASUX.YAML.BatchCmdProcessor
    public static final String REGEXP_NAME = "["+ REGEXP_NAME_PREFIXCHARSET +"]" + REGEXP_NAMESUFFIX;
    public static final String REGEXP_OBJECT_REFERENCE = "[?]*[@!]" + REGEXP_NAME;

    public static final String REGEXP_ECHO = "^\\s*echo\\s+(\\S.*\\S)\\s*$";
    public static final String REGEXP_INCLUDE = "^\\s*include\\s+(" + REGEXP_OBJECT_REFERENCE + ")\\s*$";
    public static final String REGEXP_PRINT = "^\\s*print\\s+(\\S.*\\S|\\.)\\s*$";

    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // instance variables.

    protected boolean bLine2bEchoed = false;
    protected boolean printOutputCmd = false;

    /**
     * This is to be a REFERENCE to an instance of LinkedHashMap, whose
     * object-lifecycle is maintained by some other class (as in, creating new
     * LinkedHashMap&lt;&gt;(), putting content into it, updating content as File is
     * further processed, ..)
     */
    protected transient LinkedHashMap<String, Properties> propsSetRef;

    /**
     * For every 'include sub-filename' encountered in the batch-scripts/config-scripts,
     * this will point to a new instance of this class (Never that of any subclass), as implemented within {@link #execBuiltInCommand()}.
     */
    protected ConfigFileScannerL3 includedFileScanner = null;

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    /**
     * <p> The basic constructor - that does __NOT__ allow you to evaluate Macro-expressions like ${XYZ} </p>
     * @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public ConfigFileScannerL3(boolean _verbose) {
        super(_verbose);
        reset();
        this.propsSetRef = null;
    }

    /**
     * <p>The basic constructor - that does __NOT__ allow you to evaluate Macro-expressions like ${XYZ} </p>
     * @param _verbose  Whether you want deluge of debug-output onto System.out.
     * @param _propsSet a REFERENCE to an instance of LinkedHashMap, whose object-lifecycle is maintained by some other class (as in, creating new LinkedHashMap&lt;&gt;(), putting content into it, updating content as File is further processed, ..)
     */
    public ConfigFileScannerL3(boolean _verbose, final LinkedHashMap<String, Properties> _propsSet) {
        super(_verbose);
        reset();
        this.propsSetRef = _propsSet;
    }

    /**
     * Do NOT use. USE ONLY in emergencies and ONLY IF you know what the fuck you are doing. No questions will be answered, and NO help will be provided.
     */
    protected ConfigFileScannerL3() {
        super();
        this.propsSetRef = null;
    }

    /**
     *  Within execBuiltInCommand() - when this class encounters a 'include @filename' - needs to invoke a constructor that creates a 2nd object of this class.
     * @return an object of the subclass of this ConfigFileScannerL3.java
     */
    protected ConfigFileScannerL3 create() {
        return new ConfigFileScannerL3( this.verbose, this.propsSetRef );
    }

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    /**
     * This class aims to mimic java.util.Scanner's hasNextLine() and nextLine() and reset().<br>
     * reset() has draconian-implications - as if openConfigFile() was never called!
     */
    @Override
    public void reset() {
        super.reset();
        // this.resetFlagsForEachLine(); this is already invoked within super.reset()

        this.includedFileScanner = null;
        // if ( this.propsSetRef != null ) this.propsSetRef.clear(); <---- WARNING: !!!!!!!!!!!!!!!!!!!
        // The lifecycle of the instance/object pointed to by 'propsSetRef' is owned by someone else!!!
    }

    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /**
     * This function is exclusively for use within the go() - the primary function
     * within this class - to make this very efficient when responding to the many isXXX() methods in this class.
     */
    @Override
    protected void resetFlagsForEachLine() {
        final String HDR = this.getHDRPrefix() +": resetFlagsForEachLine(): ";

        // super.resetFlagsForEachLine(); <-- this is: protected abstract within __superclass__.  So, nothing to invoke.
        if (this.includedFileScanner != null) {
            this.includedFileScanner.resetFlagsForEachLine();
            return;
        }

        this.bLine2bEchoed = false;
        this.printOutputCmd = false;
        if ( this.verbose ) System.out.println( HDR + ": instance-variables are:- "+ this.dump() );
    }

    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    private String dump() {
        return "this.bLine2bEchoed="+ this.bLine2bEchoed +" this.printOutputCmd="+ this.printOutputCmd;
    }

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    /**
     * New Method added to this subclass. For use by any Processor of this batch-file.. whether the user added the 'echo' prefix to a command, requesting that that specific line/command be echoed while executing
     * @return true or false, whether the user added the 'echo' prefix to a command requesting that that specific line/command be echoed while executing
     */
    public boolean isLine2bEchoed() {
        if (this.includedFileScanner != null)
            return this.includedFileScanner.bLine2bEchoed;

        return this.bLine2bEchoed;
    }

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    /**
     * This overrides the method from parent class {@link org.ASUX.common.ConfigFileScanner} This override is reqired to "parse out" prefixes like 'ecbo'
     * 
     * @return the next string in the list of lines (else an exception is thrown)
     * @throws Exception in case this class is messed up or hasNextLine() is false
     *                   or has Not been invoked appropriately
     */
    @Override
    public String currentLine() throws Exception
    { // !!!!!!!!!!!!!!!!!!!!!! COMPLETELY OVERRIDES Parent Method  // !!!!!!!!!!!!!!!!!!!!!!!!
        if (this.includedFileScanner != null)
            return this.includedFileScanner.currentLine();

        final String currLnNoMacro = Macros.evalThoroughly( this.verbose, super.currentLine(), this.propsSetRef );
        final String nextLn = ConfigFileScannerL3.removeEchoPrefix( currLnNoMacro );
        return nextLn;
    }

    //=============================================================================
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine().  But this method is a special deviation, as it allows us to get the 'current-line' over-n-over again.
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    @Override
    public String currentLineOrNull()
    {   // !!!!!!!!!!!!!!!!!!!!!! COMPLETELY OVERRIDES Parent Method !!!!!!!!!!!!!!!!!!!!!!!!
        final String HDR = this.getHDRPrefix() +": currentLineOrNull(): ";

        if ( this.includedFileScanner != null )
            return this.includedFileScanner.currentLineOrNull();

        try {
            final String currLnNoMacro = Macros.evalThoroughly( this.verbose, super.currentLine(), this.propsSetRef );
            final String nextLn = ConfigFileScannerL3.removeEchoPrefix( currLnNoMacro );
            return nextLn;
        } catch (Exception e) {
            if ( this.verbose ) e.printStackTrace(System.err); // Static Method. So.. can't avoid dumping this on the user.
            if ( this.verbose ) System.out.println( HDR +"Ignoring above exception and continuing by returning null" );
            return null;
        }
    }

    //===========================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //===========================================================================

    /**
     *  <p>For a string, it detects whether it starts with 'echo' after ignoring all the appropriate whitespace.. </p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param line pass in a string (Not null, else NullPointer exception)
     *  @return true if current-line starts with 'echo' after ignoring all the appropriate whitespace
     */
    public static boolean checkForEchoPrefix( final boolean _verbose, final String line )
    {   final String HDR = CLASSNAME +": checkForEchoPrefix(): ";
        final Pattern echoPattern = Pattern.compile( REGEXP_ECHO );
        final Matcher echoMatcher = echoPattern.matcher( line );
        if (echoMatcher.find()) {
            if ( _verbose ) System.out.println( HDR +": I found the command to be ECHO-ed '"+ echoMatcher.group(1) +"' starting at index "+  echoMatcher.start() +" and ending at index "+ echoMatcher.end() );    
            if ( _verbose ) System.out.println( HDR +"\t Detected 'ECHO' prefix in Line # "+ line );
            return true;
        }
        return false;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /**
     *  <p>A static utility method</p>
     *  <p>Given a string, it detects whether it starts with 'echo' after ignoring all the appropriate whitespace.. then, return without the 'echo' prefix from the string</p>
     *  @param _line can be null or any String
     *  @return the rest of _line AFTER removing any 'echo ' prefix .. including any whitespace preceding echo, using the REGEXP: \s*echo\s
     */
    protected static String removeEchoPrefix( final String _line )
    {   final String HDR = CLASSNAME +": removeEchoPrefix(): ";
        if ( _line == null ) return null;

        try {
            final Pattern echoPattern = Pattern.compile( REGEXP_ECHO );
            final Matcher echoMatcher = echoPattern.matcher( _line );
            if (echoMatcher.find()) {
                // if ( _verbose ) System.out.println( HDR +": I found the command to be ECHO-ed '"+ echoMatcher.group(1) +"' starting at index "+  echoMatcher.start() +" and ending at index "+ echoMatcher.end() );    
                return echoMatcher.group(1);
            } else {
                return _line;
            } // if-else
        } catch (PatternSyntaxException e) {
			e.printStackTrace(System.err); // too serious an internal-error.  Immediate bug-fix required.  The application/Program will exit .. in 2nd line below.
			System.err.println( HDR + ": Unexpected Internal ERROR, while checking for pattern ("+ REGEXP_ECHO +")." );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
        return null; // we shouldn't be getting here, due to 'System.exit()' within above catch()
    }

    //===========================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //===========================================================================

    /**
     * See {@link org.ASUX.common.ConfigFileScanner#getState()}.
     * @return something like: ConfigFile [@mapsBatch1.txt] @ line# 2 = [line contents as-is]
     */
    @Override
    public String getState() {
        if ( this.includedFileScanner != null )
            return this.includedFileScanner.getState();
        else
            return super.getState();
    }

    //===========================================================================

    /**
     * This is a debugging tool, to help determine the 'Russian-Doll' situation, when Config-file includes another, which include yet-another, .. .. ..
     * @return something like: ConfigFile [@mapsBatch1.txt] @ line# 2 = [line contents as-is] .. .. --&lt; .. .. &lt;Repeat&gt;
     */
    protected String getRecursiveState() {
        if ( this.includedFileScanner == null )
            return super.getState();
        else
            return ConfigFileScanner.getState( this ) + " .. --> .. "+ this.includedFileScanner.getRecursiveState();
    }

    //===========================================================================

    private String getHDRPrefix() { return CLASSNAME + "("+ super.getFileName() +")"; }

    //===========================================================================

    public void setVerbose( final boolean _verbose ) {
        super.setVerbose(_verbose);
        if ( this.includedFileScanner != null )
            this.includedFileScanner.setVerbose(_verbose);
    }

    //===========================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //===========================================================================

    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine()
     *  @return true or false
     *  @throws java.io.FileNotFoundException If we encounter a 'include' built-in command and the filename passed as '@...' does Not exist.
     *  @throws java.io.IOException If we encounter a 'include' built-in command and there is any trouble reding the included-file passed in as '@...'
     *  @throws java.lang.Exception either this function throws or will return false.
     */
    @Override
    public boolean hasNextLine() throws java.io.FileNotFoundException, java.io.IOException, Exception
    {   final String HDR = this.getHDRPrefix() +": hasNextLine(): ";
        if ( this.verbose ) System.out.println( HDR +" @ Beginning: has includedFileScanner? "+ (this.includedFileScanner!=null) + " .. "+ this.getRecursiveState() );

        final boolean bInclFileHasNext = ( this.includedFileScanner == null )
                                        ? false
                                        : this.includedFileScanner.hasNextLine();
        if ( bInclFileHasNext ) {
            return true;
        } else {
            // Attention!!! Since we are DONE with 'included' file.. we need to look at the next row in _THIS_ file.
            this.includedFileScanner = null;
            return ConfigFileScannerL3.hasNextLine( this );
        }
    }

    /** <p>NOTE: This is a STATIC method.</p>
     *  <p>NOTE: This is for internal-use only by {@link #hasNextLine()}.</p>
     *  <p>Since this class can be (at any instant of time) be stepping thru the contents of an included-file, this _REUSABLE_ code is used to 1st invoke on {@link #includedFileScanner} before AGAIN invoking this code 'this'.</p>
     *  <p>This method is made protected, in case sub-class would like to step thru the entries in {@link ConfigFileScanner#lines}.</p>
     *  @param __this since this is a static method, pass in 'this' (or ANY of the subclasses)
     *  @return true or false
     *  @throws java.io.FileNotFoundException If we encounter a 'include' built-in command and the filename passed as '@...' does Not exist.
     *  @throws java.io.IOException If we encounter a 'include' built-in command and there is any trouble reding the included-file passed in as '@...'
     *  @throws java.lang.Exception either this function throws or will return false.
     */
    protected static boolean hasNextLine( final ConfigFileScannerL3 __this ) throws java.io.FileNotFoundException, java.io.IOException, Exception
    {   final String HDR = __this.getHDRPrefix() +": (STATIC)hasNextLine(): ";
        if ( __this.verbose ) System.out.println( HDR +" has includedFileScanner? "+ (__this.includedFileScanner!=null) + " .. "+ __this.getRecursiveState() );

        while ( ConfigFileScanner.hasNextLine( __this ) ) {
            if ( __this.verbose ) System.out.println( HDR +" @ TOP OF WHILE LOOP: "+ ConfigFileScanner.getState( __this )  );
            // we're going to keep iterating UNTIL we find a line that is __NOT__ a 'print' or 'include' line

            final String nextLn = __this.peekNextLine();
            if ( __this.verbose ) System.out.println( HDR +": PEEEKING.. '"+ nextLn +"'" );
            assertTrue ( nextLn != null );

            // Now let's ask the _SUBclasses_ if they've implemented the command in this line, as a built-in-command!
            if ( __this.isBuiltInCommand( nextLn ) ) {
                if ( __this.verbose ) System.out.println( HDR +"confirmed that isBuiltInCommand("+ nextLn +") is true. Quietly and transparently processing it." );
                // we peeked (see 7 lines above).
                // Now, we're 100% sure its a 'print' or 'include' line in the FILE.
                __this.nextLine(); // let's go to the next-line and process that 'print' and 'include' commands.
                // if ( __this.verbose ) new Debug(true).printAllProps( HDR, __this.propsSetRef );

                assertTrue( __this.includedFileScanner == null );
                final boolean retB = __this.execBuiltInCommand(); // it means that we hit a 'include @filename' or 'print ...' line in the batchfile.  So, we need to process-n-skip those lines :-( ugly code.
                assertTrue( retB == true );

                if ( __this.includedFileScanner == null ) {
                    if ( __this.verbose ) System.out.println( HDR +"ABOUT to go to the next line in __this .. _ASSUMING_ _IF_ another line exists.. .." );
                    continue; // go to next-line in __this
                } else {

                    // hmmmm.  the built-in command executed, was an 'include'
                    if ( __this.verbose ) System.out.println( HDR +"Just executed an 'include' command "+ ConfigFileScanner.getState( __this ) );
                    if ( __this.includedFileScanner.hasNextLine() ) {
                        if ( __this.verbose ) System.out.println( HDR +"'included' file hasNextLine===TRUE! "+ ConfigFileScanner.getState( __this )  );
                        return true;
                    } else {
                        // hmmmm.  The 'included' file is empty :-(
                            if ( __this.verbose ) System.out.println( "Included file is empty!  Did you intend to have it be empty?  See within "+ ConfigFileScanner.getState( __this ) );
                        __this.includedFileScanner = null; // it's empty, so let's not keep it around
                        continue;
                    }
                }

            } else {
                if ( __this.verbose ) System.out.println( HDR +"NO!! NO!! NO!!  confirmed that isBuiltInCommand("+ nextLn +") is FALSE!!! "+ ConfigFileScanner.getState( __this ) );
                return true; //we're inside the while-loop after passing the while-loop's conditional-expression: super.hasNextLine()
            }

            // if ( __this.verbose ) System.out.println( HDR +": BOTTOM of WHILE-Loop.. .." );
        } // while-loop

        if ( __this.verbose ) System.out.println( HDR +": returning FALSE!" );
        return false; // if we ended the above while-loop, super.hasNextLine() is FALSE!

    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This class aims to AUGMENTS java.util.Scanner's hasNextLine() and nextLine(), but needs to be used with CAUTION.
     *  @return the next line or NULL
     *  @throws IndexOutOfBoundsException if this method is NOT-PROPERLY called within a loop() based on the conditional: hasNextLine()
     */
    @Override
    public String peekNextLine() throws IndexOutOfBoundsException
    {   final String HDR = this.getHDRPrefix() +": hasNextLine(): ";
        if ( this.verbose ) System.out.println( HDR +" @ Beginning: has includedFileScanner? "+ (this.includedFileScanner!=null) + " .. "+ this.getRecursiveState() );

        final String peekedLine = ( this.includedFileScanner == null )
                                        ? null
                                        : this.includedFileScanner.peekNextLine();
        if ( peekedLine != null ) {
            return peekedLine;
        } else {
            return super.peekNextLine();
        }
    }

    //===========================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //===========================================================================

    /** Thie method overrides the parent/super class method {@link ConfigFileScanner#nextLine()}
     *  @return for scripts that end in PRINT command, this returns null. Otherwise, Returns the next string in the list of lines.
     *  @throws Exception in case this class is messed up or hasNextLine() is false or has Not been invoked appropriately
     */
    @Override
    public String nextLine() throws Exception
    {   // !!!!!!!!!!!!!!!!!!!!!! OVERRIDES Parent Method !!!!!!!!!!!!!!!!!!!!!!!!
        final String HDR = this.getHDRPrefix() + ": nextLine(): ";
        if ( this.includedFileScanner != null ) {
            this.resetFlagsForEachLine(); // !!!!!!!!!! ATTENTION !!!!!!!!!!!! This is important even if this.includedFileScanner != null
            return this.includedFileScanner.nextLine();
        }

        // this.bLine2bEchoed must be set PRIOR to calling evalMacroAndEcho() 4 lines below.
        String nextLn = super.nextLine();
        this.bLine2bEchoed = ConfigFileScannerL3.checkForEchoPrefix( this.verbose, nextLn );
        nextLn = ConfigFileScannerL3.removeEchoPrefix( nextLn );

        // make sure this.bLine2bEchoed has been set before invoking evalMacroAndEcho()
        final String currentLineAfterMacroEval = evalMacroAndEcho( nextLn );

        if ( this.verbose ) System.out.println( HDR +" currentLineAfterMacroEval="+ currentLineAfterMacroEval );
        if ( this.verbose ) System.out.println( HDR +" this.currentLine()="+ super.currentLine() );
        return this.currentLine();
    }

    //===========================================================================
    /** Thie method overrides the parent/super class method {@link ConfigFileScanner#nextLineOrNull()}
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    @Override
    public String nextLineOrNull()
    {   // !!!!!!!!!!!!!!!!!!!!!! OVERRIDES Parent Method !!!!!!!!!!!!!!!!!!!!!!!!
        final String HDR = this.getHDRPrefix() + ": nextLineOrNull(): ";
        if ( this.includedFileScanner != null )
            return this.includedFileScanner.nextLineOrNull();

        String nextLn = super.nextLineOrNull();
        // this.bLine2bEchoed must be set PRIOR to calling evalMacroAndEcho() 4 lines below.
        this.bLine2bEchoed = ConfigFileScannerL3.checkForEchoPrefix( this.verbose, nextLn );
        nextLn = ConfigFileScannerL3.removeEchoPrefix( nextLn );

        try {
            // make sure this.bLine2bEchoed has been set before invoking evalMacroAndEcho()
            final String currentLineAfterMacroEval = evalMacroAndEcho( nextLn );

            if ( this.verbose ) System.out.println( HDR +" currentLineAfterMacroEval="+ currentLineAfterMacroEval );
            if ( this.verbose ) System.out.println( HDR +" this.currentLine()="+ super.currentLineOrNull() );

        } catch (Exception e) {
            // since we shouldn't be getting this error, but .. as we'd like this class to be garbage-in-garbage-out flexible.. let's dump error on the user and return NULL.
            e.printStackTrace(System.err);
            System.err.println( "\n\n"+ HDR + " Unexpected Internal ERROR @ " + this.getState() +"." );
            return null;
        }
        return this.currentLineOrNull();
    }

    //=============================================================================
    private String evalMacroAndEcho( final String preMacroStr ) throws Exception, MacroException
    {   final String HDR = this.getHDRPrefix() + ": evalMacroAndEcho(): ";

        final String currLnNoMacro = Macros.evalThoroughly( this.verbose, preMacroStr, this.propsSetRef );
        final boolean bNoChange = ( currLnNoMacro == null ) ? (preMacroStr == null): currLnNoMacro.equals( preMacroStr );
// if ( this.verbose ) new Debug(this.verbose).printAllProps( HDR +" >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ", this.propsSetRef );
// if ( this.verbose ) System.out.println( HDR +" currentLine After MacroEval="+ this.currentLineOrNull() );

        if ( this.verbose ) System.out.println( HDR + "Echo (As-Is): " + preMacroStr);
        if ( this.verbose ) System.out.println( HDR + "Echo (Macros-substituted): " +  currLnNoMacro );

        if ( this.isLine2bEchoed() ) {
            if ( bNoChange ) {
                System.out.println("\tEcho: " + preMacroStr);
            } else {
                System.out.println("\tEcho (As-Is): " + preMacroStr);
                System.out.println("\tEcho (Macros-substituted): " + ConfigFileScannerL3.removeEchoPrefix( currLnNoMacro ) );
            }
        }

        return currLnNoMacro;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  <p>This method is used to simply tell whether 'current-line' matches the REGEXP patterns that execBuiltInCommand() will be processing 'internally' within this class</p>
     *  <p>In this class, those would be the REGEXP for 'print ...' and 'include @...'</p>
     *  @param nextLn current line or 'peek-forward' line
     *  @return true if the line will be processed 'internally'
     */
    protected boolean isBuiltInCommand( final String nextLn ) {
        final String HDR = this.getHDRPrefix() +": isBuiltInCommand(): ";
        if ( this.includedFileScanner != null )
            return this.includedFileScanner.isBuiltInCommand( nextLn );

        if ( nextLn == null ) return false;

        final String noprefix = removeEchoPrefix( nextLn );
        if ( this.verbose ) System.out.println( HDR + "noprefix="+ noprefix );
        if ( this.verbose ) System.out.println( HDR + "noprefix.matches( REGEXP_PRINT )="+ noprefix.matches( REGEXP_PRINT ) );

        final boolean retb = noprefix.matches( REGEXP_PRINT ) || noprefix.matches( REGEXP_INCLUDE );
        if ( this.verbose ) System.out.println( HDR + "is "+ (retb ? "" : "NOT") +" a BUILT-IN COMMAND: "+ nextLn );
        return retb;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** <p>New Method added to this subclass.  Implement your command parsing and do as appropriate.</p>
     *  <p>ATTENTION: Safely assume that any 'echo' prefix parsing and any 'print' parsing has happened already in a TRANSAPARENT way.</p>
     *  <p>This method is automatically invoked _WITHIN_ nextLine().  nextLine() is inherited from the parent {@link org.ASUX.common.ConfigFileScanner}.</p>
     *  @return true if all 'normal', and false IF-AND-ONLY-IF any problems (you are advised to throw Exception instead)
     *  @throws java.io.FileNotFoundException If we encounter a 'include' built-in command and the filename passed as '@...' does Not exist.
     *  @throws java.io.IOException If we encounter a 'include' built-in command and there is any trouble reding the included-file passed in as '@...'
     *  @throws Exception This class does NOT.  But .. subclasses may have overridden this method and can throw exception(s).  Example: org.ASUX.yaml.BatchFileGrammer.java
     */
    protected boolean execBuiltInCommand() throws java.io.FileNotFoundException, java.io.IOException, Exception
    {
        final String HDR = this.getHDRPrefix() +": execBuiltInCommand(): ";
        if ( this.includedFileScanner != null )
            return this.includedFileScanner.execBuiltInCommand();

        final String line = (this.includedFileScanner != null) ?  this.includedFileScanner.currentLine() : this.currentLine();

        if ( this.verbose ) System.out.println( HDR + this.getState() +"\n\t\tline="+ line );

        if ( line == null || line.trim().length() <= 0 )
            throw new Exception("Serious internal error: We have line='"+ line +"'.\nERROR in"+ this.getState() );
            // we should have weeded out 'whitespace-only' lines as part of openFile().
            // If user's Properties (loaded into this.allPropsRef) lead to a MACRO EXPRESSION that is null.. that is the user's problem to figure out what the heck is this exception.

        try {
            final Pattern includePattern = Pattern.compile( REGEXP_INCLUDE );
            final Matcher includeMatcher = includePattern.matcher( line );
            if (includeMatcher.find()) {
                if ( this.verbose ) System.out.println( HDR +": I found the INCLUDE command: '"+ includeMatcher.group(1) +"' starting at index "+  includeMatcher.start() +" and ending at index "+ includeMatcher.end() );    
                String includeFileName = includeMatcher.group(1); // line.substring( includeMatcher.start(), includeMatcher.end() );
                if ( this.verbose ) System.out.println( HDR +"includeFileName='"+ includeFileName +"' and includeFileName.startsWith(?)="+ includeFileName.startsWith("?") +" includeFileName.substring(1)='"+ includeFileName.substring(1) + "'" );

                final boolean bOkIfMissing = includeFileName.startsWith("?"); // that is, the script-file line was:- 'properties kwom=?fnwom'
                includeFileName = includeFileName.startsWith("?") ? includeFileName.substring(1) : includeFileName; // remove the '?' prefix from key/lhs string
                if ( this.verbose ) System.out.println( HDR +"includeFileName='"+ includeFileName +"'" );

                final String filenameWWOAt = includeFileName.startsWith("?") ? includeFileName.substring(1) : includeFileName; // remove the '?' prefix from file's name/path.
                // 'WWOAt' === With-OR-Without-@-symbol ... as,  we're Not sure if there is an '@' prefix (to the file-name).

                final String filename = filenameWWOAt.startsWith("@") ? filenameWWOAt.substring(1) : filenameWWOAt;

                final File fileObj = new File ( filename );
                if ( fileObj.exists() && fileObj.canRead() ) {
                    if ( this.verbose ) System.out.println( HDR +"Filename=[" + fileObj.getAbsolutePath() +"] exists!" );
                } else {
                    if ( this.verbose ) System.out.println( HDR +"Filename=[" + fileObj.getAbsolutePath() +"] does __NOT__ exist !!!!" );
                }

                this.includedFileScanner = new ConfigFileScannerL3( this.verbose, this.propsSetRef );   // ConfigFileScannerL2 would instead invoke:- this.create()
                try {
                    if ( this.verbose ) System.out.println( HDR +"About to openFile(" + includeFileName +"] " );
                    final boolean success = this.includedFileScanner.openFile( includeFileName, this.ok2TrimWhiteSpace, this.bCompressWhiteSpace );
                    if ( ! success )
                        throw new Exception( "Unknown internal exception opening file: "+ includeFileName );
                } catch ( java.io.FileNotFoundException fnfe) {
                    if (  !   bOkIfMissing ) {
                        if ( this.verbose ) System.out.println( HDR +"Filename=[" + fileObj.getAbsolutePath() +"] does __NOT__ exist !!!!\n"+ fnfe );
                        throw fnfe;
                    }
                }
                if ( this.verbose ) System.out.println( HDR +"\t INCLUDED_File's contents:\n"+ this.includedFileScanner );
                return true; // !!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!! method returns here.
            } // if

            Pattern printPattern = Pattern.compile( REGEXP_PRINT ); // Note: A line like 'print -' would FAIL to match \\S.*\\S
            Matcher printMatcher    = printPattern.matcher( line );
            if (printMatcher.find()) {
                if ( this.verbose ) System.out.println( HDR +": I found the text "+ printMatcher.group() +" starting at index "+  printMatcher.start() +" and ending at index "+ printMatcher.end() );    
                final String printExpression  = printMatcher.group(1); // line.substring( printMatcher.start(), printMatcher.end() );
                onPrintCmd( printExpression ); // Note: We've already evaluated Macros, but the time we get to this IF-block.
                return true; // !!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!! method returns here.
            }

            // if we're here, then this class did NOT process the current line
            return false;

        } catch (PatternSyntaxException e) {
			e.printStackTrace(System.err); // too serious an internal-error.  Immediate bug-fix required.  The application/Program will exit .. in 2nd line below.
			System.err.println( HDR + ": Unexpected Internal ERROR, while checking for pattern ("+ REGEXP_ECHO +")." );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
        return false; // we shouldn't be getting here, due to 'System.exit()' within above catch()
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    // private void debug() {
    //     if ( this.verbose ) new Debug(this.verbose).printAllProps( this.getHDRPrefix() +" debug() ___________ ", this.propsSetRef );
    // }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /**
     *  <p>This method is the literal-equivalent of 'echo' of BASH /bin/sh /bin/tcsh. How is this different from 'echo'?  Well, 'echo' is more PRIMITIVE.  It shows the command __TO BE__ executed, after ALL MACRO-Replacements.</p>
     *  @param _printExpression whatever is to the RIGHT side of the 'print' command in the Config file.
     *  @throws Exception any error (potentially from sub-classes)
     */
    private final void onPrintCmd( String _printExpression ) throws Exception
    {
        final String HDR = this.getHDRPrefix() + ": onPrintCmd("+ _printExpression +"): ";
        if ( this.verbose ) System.out.println( HDR +" @ method-beginning." );

        // Note: Because of the RegExp based grammer in execBuiltInCommand().. this assertTrue should never throw.
        assertTrue ( _printExpression != null);

        String str2output = _printExpression.toString(); // clone
        if ( str2output.trim().endsWith("\\n") ) {
            str2output = str2output.substring(0, str2output.length()-2); // chop out the 2-characters '\n'
            // !!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!
            // How do we allow sub-classes to INFLUENCE what is 'printed':  Example:    print !lookupContent (within subclasses)
            // NOTE: A simple workaround:   Within subclasses.. for a Config/Batch file, we need 2 lines:  (1) use !lookupContent (2) print -
            System.out.println( str2output ); // println (<--- end-of-line EOL character is output)

        } else {
            System.out.print( str2output +" " ); // print only (<--- NO end-of-line EOL character outputted.)
            // Why add a ' ' at the end?
            // Because I currently do NOT support printing ANY WhiteSpace (incl. Tabs).. .. so successive 'print' commands have at least 1 space separating the output
        }

        System.out.flush();
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This deepClone function is VERY MUCH necessary, as No cloning-code can handle 'transient' variables in this class/superclass.
     *  @param _orig what you want to deep-clone
     *  @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     */
    public static ConfigFileScannerL3 deepClone( final ConfigFileScannerL3 _orig ) {
        assertTrue( _orig != null );
        try {
            final ConfigFileScannerL3 newobj = Utils.deepClone( _orig );
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
    protected void deepCloneFix( final ConfigFileScannerL3 _orig ) {
        super.deepCloneFix(_orig);
        // this CLASS has TRANSIENT class-variables..
        this.propsSetRef = _orig.propsSetRef;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================
    // For unit-testing purposes only
    public static void main(String[] args) {
        final String HDR = CLASSNAME + ": main(): ";
        class TestConfigFileScanner extends ConfigFileScannerL3 {
            private static final long serialVersionUID = 9990L;
            public TestConfigFileScanner(boolean _verbose) { super(_verbose); }
            protected TestConfigFileScanner create() { return new TestConfigFileScanner( this.verbose ); }
            // protected void onPrintDash() { System.out.println("----Print-DASH----Print-DASH----Print-DASH----Print-DASH----Print-DASH----Print-DASH----Print-DASH----"); }
        };
        try {
            boolean verbose = false;
            int ix = 0;
            if ( "--verbose".equals(args[0]) ) {
                ix ++;
                verbose = true;
            }
            final ConfigFileScannerL3 o = new TestConfigFileScanner( verbose );
            o.openFile( args[ix], true, true );
            while (o.hasNextLine()) {
                final String s = o.nextLine();
                // System.out.println( o.current Line() );
                System.out.println( o.getState() );
                System.out.println( s );
                System.out.println( "\n\n" );
            }
		} catch (Exception e) {
			e.printStackTrace(System.err); // main().  For Unit testing
			System.err.println( HDR + "Unexpected Internal ERROR, while processing " + ((args==null || args.length<=0)?"[No CmdLine Args":args[0]) +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
    }

}
