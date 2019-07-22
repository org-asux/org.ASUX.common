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

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import static org.junit.Assert.*;

/**
 *  <p>This is part of org.ASUX.common GitHub.com project and the <a href= "https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p><b>Strongly recommend you use  {@link PropertiesFileScanner}, {@link ScriptFileScanner} or  {@link OSScriptFileScanner}</b> instead of this class, unless you need something very specifically limited in capability.</p>
 *  <p>Compared with {@link ConfigFileScannerL3}, this class will 'fork' itself (in UNIX's Shell-script sense), whenever it encounters an 'include filename' in the batch-script.<br>
 *      This class is <b>most useful when</b> you'd like the subclasses to be aware of 'built-in' commands.  Not something I'd recommend.<br>
 *      In fact, {@link PropertiesFileScanner}, {@link ScriptFileScanner} or  {@link OSScriptFileScanner} are all sub-classes of the other {@link ConfigFileScannerL3}.</p>
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
public class ConfigFileScannerL2 extends ConfigFileScanner {

    private static final long serialVersionUID = 112L;
    public static final String CLASSNAME = ConfigFileScannerL2.class.getName();

    public static final String REGEXP_NAME_PREFIXCHARSET = "a-zA-Z0-9$_/\\.";
    public static final String REGEXP_NAMESUFFIX = "[${}@%a-zA-Z0-9\\.,:_/-]+";
    public static final String REGEXP_NAME = "["+ REGEXP_NAME_PREFIXCHARSET +"]" + REGEXP_NAMESUFFIX;
    public static final String REGEXP_OBJECT_REFERENCE = "[?]*[@!]" + REGEXP_NAME;

    public static final String REGEXP_ECHO = "^\\s*echo\\s+(\\S.*\\S)\\s*$";
    public static final String REGEXP_INCLUDE = "^\\s*include\\s+(" + REGEXP_OBJECT_REFERENCE + ")\\s*$";
    public static final String REGEXP_PRINT = "^\\s*print\\s+(\\S.*\\S|\\.)\\s*$";

    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // instance variables.

    protected boolean bLine2bEchoed = false;
    protected String currentLineAfterMacroEval = null;
    protected ConfigFileScannerL2 includedFileScanner = null;
    protected boolean printOutputCmd = false;

    /**
     * This is to be a REFERENCE to an instance of LinkedHashMap, whose
     * object-lifecycle is maintained by some other class (as in, creating new
     * LinkedHashMap&lt;&gt;(), putting content into it, updating content as File is
     * further processed, ..)
     */
    protected transient LinkedHashMap<String, Properties> propsSetRef;

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    /**
     * <p> The basic constructor - that does __NOT__ allow you to evaluate Macro-expressions like ${XYZ} </p>
     * @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public ConfigFileScannerL2(boolean _verbose) {
        super(_verbose);
        reset();
        this.propsSetRef = null;
    }

    /**
     * <p>The basic constructor - that does __NOT__ allow you to evaluate Macro-expressions like ${XYZ} </p>
     * @param _verbose  Whether you want deluge of debug-output onto System.out.
     * @param _propsSet a REFERENCE to an instance of LinkedHashMap, whose object-lifecycle is maintained by some other class (as in, creating new LinkedHashMap&lt;&gt;(), putting content into it, updating content as File is further processed, ..)
     */
    public ConfigFileScannerL2(boolean _verbose, final LinkedHashMap<String, Properties> _propsSet) {
        super(_verbose);
        reset();
        this.propsSetRef = _propsSet;
    }

    /**
     * Do NOT use. USE ONLY in emergencies and ONLY IF you know what the fuck you are doing. No questions will be answered, and NO help will be provided.
     */
    protected ConfigFileScannerL2() {
        super();
        this.propsSetRef = null;
    }

    /**
     *  Within execBuiltInCommand() - when this class encounters a 'include @filename' - needs to invoke a constructor that creates a 2nd object of this class.
     * @return an object of the subclass of this ConfigFileScannerL2.java
     */
    protected ConfigFileScannerL2 create() {
        return new ConfigFileScannerL2( this.verbose, this.propsSetRef );
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
        // super.resetFlagsForEachLine(); <-- this is: protected abstract within __superclass__.  So, nothing to invoke.
        this.bLine2bEchoed = false;
        this.currentLineAfterMacroEval = null;
        this.printOutputCmd = false;
    }

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    /**
     * New Method added to this subclass. For use by any Processor of this batch-file.. whether the user added the 'echo' prefix to a command, requesting that that specific line/command be echoed while executing
     * @return true or false, whether the user added the 'echo' prefix to a command requesting that that specific line/command be echoed while executing
     */
    public boolean isLine2bEchoed() {
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

        return this.currentLineAfterMacroEval;
    }

    //=============================================================================
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine().  But this method is a special deviation, as it allows us to get the 'current-line' over-n-over again.
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    @Override
    public String currentLineOrNull()
    {   // !!!!!!!!!!!!!!!!!!!!!! COMPLETELY OVERRIDES Parent Method !!!!!!!!!!!!!!!!!!!!!!!!
        if ( this.includedFileScanner != null )
            return this.includedFileScanner.currentLineOrNull();

        return this.currentLineAfterMacroEval;
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
    public String getState()
    {
        if ( this.includedFileScanner != null )
            return this.includedFileScanner.getState();
        else
            return super.getState();
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
    {   final String HDR = CLASSNAME +": hasNextLine(): ";
        // try {
            while ( super.hasNextLine() ||  ( this.includedFileScanner != null && this.includedFileScanner.hasNextLine() )     ) {
                // we're going to keep iterating UNTIL we find a line that is __NOT__ a 'print' or 'include' line

                if ( this.includedFileScanner != null ) {
                        final boolean hnl = this.includedFileScanner.hasNextLine();
                        if ( this.verbose ) System.out.println( HDR +": this.includedFileScanner.hasNextLine()="+ hnl );
                        if ( hnl )
                            return true; // return hnl;
                        else {
                            this.includedFileScanner = null; // we're done with the included file!
                            continue; // Attention!!! Since we are DONE with 'included' file.. we need to look at the next row in THIS file.
                        }

                } else {
                    final String nextLn = peekNextLine();
                    if ( this.verbose ) System.out.println( HDR +": PEEEKING.. '"+ nextLn +"'" );
                    assertTrue ( nextLn != null );

                    if ( this.isBuiltInCommand( nextLn ) ) {
                        if ( this.verbose ) System.out.println( HDR +"confirmed that isBuiltInCommand("+ nextLn +") is true. Quietly and transparently processing it." );
                        // we peeked (see 4 lines above).
                        // Now, we're 100% sure its a 'print' or 'include' line in the FILE.
                        this.nextLine(); // let's go to the next-line and process that 'print' and 'include' commands.
// if ( this.verbose ) System.out.println( HDR +"BEFORE execBuiltInCommand() is invoked.. this.currentLineAfterMacroEval="+ s );
// if ( this.verbose ) System.out.println( HDR +"this.currentLineAfterMacroEval="+ this.currentLineAfterMacroEval );
// if ( this.verbose ) new Debug(true).printAllProps( HDR +" >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> !! <<<<<<<<<<<<<<<<<<<<<<<<<<<< ", this.propsSetRef );
                        final boolean retB = this.execBuiltInCommand(); // this means that we hit a 'include @filename' or 'print ...' line in the batchfile.  So, we need to process-n-skip those lines :-( ugly code.
                        assertTrue( retB == true );

                    } else {
                        if ( this.verbose ) System.out.println( HDR +"NO!! NO!! NO!!  confirmed that isBuiltInCommand("+ nextLn +") is FALSE!!!" );
                        return true; //we're inside the while-loop after passing the while-loop's conditional-expression: super.hasNextLine()
                    }
                } // if-else

                if ( this.verbose ) System.out.println( HDR +": BOTTOM of WHILE-Loop.. .." );
            } // while-loop

            if ( this.verbose ) System.out.println( HDR +": returning FALSE!" );
            return false; // if we ended the above while-loop, super.hasNextLine() is FALSE!

        // } catch (Exception e) {
        //     e.printStackTrace(System.err); // too serious an internal-error.  Immediate bug-fix required.  The application/Program will exit .. in 2nd line below.
        //     System.err.println( "\n\n"+ HDR + " Unexpected Internal ERROR @ " + this.getState() +"." );
        //     System.exit(99); // This is a serious failure. Shouldn't be happening.
        //     return false;
        // }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** Thie method overrides the parent/super class method {@link ConfigFileScanner#nextLine()}
     *  @return for scripts that end in PRINT command, this returns null. Otherwise, Returns the next string in the list of lines.
     *  @throws Exception in case this class is messed up or hasNextLine() is false or has Not been invoked appropriately
     */
    @Override
    public String nextLine() throws Exception
    {   // !!!!!!!!!!!!!!!!!!!!!! OVERRIDES Parent Method !!!!!!!!!!!!!!!!!!!!!!!!
        final String HDR = CLASSNAME + ": nextLine(): ";
        if ( this.includedFileScanner != null )
            return this.includedFileScanner.nextLine();

        // this.bLine2bEchoed must be set PRIOR to calling evalMacro() 4 lines below.
        String nextLn = super.nextLine();
        this.bLine2bEchoed = ConfigFileScannerL2.checkForEchoPrefix( this.verbose, nextLn );
        nextLn = ConfigFileScannerL2.removeEchoPrefix( nextLn );

        // make sure this.bLine2bEchoed has been set before invoking evalMacro()
        this.currentLineAfterMacroEval = evalMacro( nextLn );

        if ( this.verbose ) System.out.println( HDR +" this.currentLineAfterMacroEval="+ this.currentLineAfterMacroEval );
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
        final String HDR = CLASSNAME + ": nextLineOrNull(): ";
        if ( this.includedFileScanner != null )
            return this.includedFileScanner.nextLineOrNull();

        String nextLn = super.nextLineOrNull();
        // this.bLine2bEchoed must be set PRIOR to calling evalMacro() 4 lines below.
        this.bLine2bEchoed = ConfigFileScannerL2.checkForEchoPrefix( this.verbose, nextLn );
        nextLn = ConfigFileScannerL2.removeEchoPrefix( nextLn );

        try {
            // make sure this.bLine2bEchoed has been set before invoking evalMacro()
            this.currentLineAfterMacroEval = evalMacro( nextLn );
        } catch (Exception e) {
            // since we shouldn't be getting this error, but .. as we'd like this class to be garbage-in-garbage-out flexible.. let's dump error on the user and return NULL.
            e.printStackTrace(System.err);
            System.err.println( "\n\n"+ HDR + " Unexpected Internal ERROR @ " + this.getState() +"." );
            return null;
        }

        if ( this.verbose ) System.out.println( HDR +" this.currentLineAfterMacroEval="+ this.currentLineAfterMacroEval );
        if ( this.verbose ) System.out.println( HDR +" this.currentLine()="+ super.currentLineOrNull() );
        return this.currentLineOrNull();
    }

    //=============================================================================
    private String evalMacro( final String preMacroStr ) throws Exception, MacroException
    {   final String HDR = CLASSNAME + ": evalMacro(): ";

        final String currLnNoMacro = Macros.evalThoroughly( this.verbose, preMacroStr, this.propsSetRef );
        final boolean bNoChange = ( currLnNoMacro == null ) ? (preMacroStr == null): currLnNoMacro.equals( preMacroStr );
// if ( this.verbose ) new Debug(this.verbose).printAllProps( HDR +" >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ", this.propsSetRef );
// if ( this.verbose ) System.out.println( HDR +" this.currentLineAfterMacroEval="+ this.currentLineAfterMacroEval );

        if ( this.verbose ) System.out.println( HDR + "Echo (As-Is): " + preMacroStr);
        if ( this.verbose ) System.out.println( HDR + "Echo (Macros-substituted): " +  currLnNoMacro );

        if ( this.isLine2bEchoed() ) {
            if ( bNoChange ) {
                System.out.println("\tEcho: " + preMacroStr);
            } else {
                System.out.println("\tEcho (As-Is): " + preMacroStr);
                System.out.println("\tEcho (Macros-substituted): " + ConfigFileScannerL2.removeEchoPrefix( currLnNoMacro ) );
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
        final String HDR = CLASSNAME +": isBuiltInCommand(): ";
        if ( nextLn == null ) return false;
        final String noprefix = removeEchoPrefix( nextLn );
        if ( this.verbose ) System.out.println( HDR + "noprefix="+ noprefix );
        if ( this.verbose ) System.out.println( HDR + "noprefix.matches( REGEXP_PRINT )="+ noprefix.matches( REGEXP_PRINT ) );

        return noprefix.matches( REGEXP_PRINT ) || noprefix.matches( REGEXP_INCLUDE );
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
        final String HDR = CLASSNAME +": execBuiltInCommand(): ";
        if ( this.verbose ) System.out.println( HDR + this.getState() +"\n\t\tthis.currentLineAfterMacroEval="+ this.currentLineAfterMacroEval );

        if ( this.currentLineAfterMacroEval == null || this.currentLineAfterMacroEval.trim().length() <= 0 )
            throw new Exception("Serious internal error: We have this.currentLineAfterMacroEval='"+ this.currentLineAfterMacroEval +"'.\nERROR in"+ this.getState() );
            // we should have weeded out 'whitespace-only' lines as part of openFile().
            // If user's Properties (loaded into this.allPropsRef) lead to a MACRO EXPRESSION that is null.. that is the user's problem to figure out what the heck is this exception.

        try {
            final Pattern includePattern = Pattern.compile( REGEXP_INCLUDE );
            final Matcher includeMatcher = includePattern.matcher( this.currentLineAfterMacroEval );
            if (includeMatcher.find()) {
                if ( this.verbose ) System.out.println( HDR +": I found the INCLUDE command: '"+ includeMatcher.group(1) +"' starting at index "+  includeMatcher.start() +" and ending at index "+ includeMatcher.end() );    
                String includeFileName = includeMatcher.group(1); // this.currentLineAfterMacroEval.substring( includeMatcher.start(), includeMatcher.end() );
                if ( this.verbose ) System.out.println( HDR +"includeFileName='"+ includeFileName +"' and includeFileName.startsWith(?)="+ includeFileName.startsWith("?") +" includeFileName.substring(1)='"+ includeFileName.substring(1) + "'" );
                final boolean bOkIfMissing = includeFileName.startsWith("?"); // that is, the script-file line was:- 'properties kwom=?fnwom'
                includeFileName = includeFileName.startsWith("?") ? includeFileName.substring(1) : includeFileName; // remove the '?' prefix from key/lhs string
                if ( this.verbose ) System.out.println( HDR +"includeFileName='"+ includeFileName );

                this.includedFileScanner = this.create();
                try {
                    final boolean success = this.includedFileScanner.openFile( includeFileName, this.ok2TrimWhiteSpace, this.bCompressWhiteSpace );
                    if ( ! success )
                        throw new Exception( "Unknown internal exception opening file: "+ includeFileName );
                } catch ( java.io.FileNotFoundException fnfe) {
                    if (  !   bOkIfMissing )
                        throw fnfe;
                }
                if ( this.verbose ) System.out.println( HDR +"\t INCLUDE # "+ this.includedFileScanner );
                return true; // !!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!! method returns here.
            } // if

            Pattern printPattern = Pattern.compile( REGEXP_PRINT ); // Note: A line like 'print -' would FAIL to match \\S.*\\S
            Matcher printMatcher    = printPattern.matcher( this.currentLineAfterMacroEval );
            if (printMatcher.find()) {
                if ( this.verbose ) System.out.println( CLASSNAME +": I found the text "+ printMatcher.group() +" starting at index "+  printMatcher.start() +" and ending at index "+ printMatcher.end() );    
                final String printExpression  = printMatcher.group(1); // this.currentLineAfterMacroEval.substring( printMatcher.start(), printMatcher.end() );
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

    private void debug() {
        if ( this.verbose ) new Debug(this.verbose).printAllProps( CLASSNAME +" debug() ___________ ", this.propsSetRef );
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /**
     *  <p>This method is the literal-equivalent of 'echo' of BASH /bin/sh /bin/tcsh. How is this different from 'echo'?  Well, 'echo' is more PRIMITIVE.  It shows the command __TO BE__ executed, after ALL MACRO-Replacements.</p>
     *  @param _printExpression whatever is to the RIGHT side of the 'print' command in the Config file.
     *  @throws Exception any error (potentially from sub-classes)
     */
    private final void onPrintCmd( String _printExpression ) throws Exception
    {
        final String HDR = CLASSNAME + ": onPrintCmd("+ _printExpression +"): ";
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
    public static ConfigFileScannerL2 deepClone( final ConfigFileScannerL2 _orig ) {
        assertTrue( _orig != null );
        try {
            final ConfigFileScannerL2 newobj = Utils.deepClone( _orig );
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
    protected void deepCloneFix( final ConfigFileScannerL2 _orig ) {
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
        class TestConfigFileScanner extends ConfigFileScannerL2 {
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
            final ConfigFileScannerL2 o = new TestConfigFileScanner( verbose );
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
