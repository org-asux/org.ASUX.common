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
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Properties;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 *  <p>This is part of org.ASUX.common GitHub.com project and the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>This class extends {@link org.ASUX.common.ConfigFileScanner}.</p>
 *  <p>These classes together offer a tool-set to help make it very easy to work with the Configuration and Propertyfiles - while making it very human-friendly w.r.t .comments etc...</p>
 *  <p>This specific class offers 'echo' prefix to a line in the FILE, as well as 'include &gt;FILE&gt;' both of which are handled transparently!</p>
 *  <p>Both 'echo' and 'print' may sound similar, but 'print' is the literal-equivalent of 'echo' of BASH /bin/sh /bin/tcsh. So.. why need 'echo'?  Well, 'echo' is more PRIMITIVE.  It shows the command __TO BE__ executed, after ALL MACRO-Replacements.  In that sense, this combination of 'echo' and 'print' is MORE SOPHISTICATED and MORE CAPABLE that 'echo' in BASH, /bin/sh, /bin/tcsh</p>
 *  <p>In addition, this class offers the ability to evaluate expressions JUST LIKE a Bash or /bin/sh or /bin/tcsh does - if you provide a java.util.Properties instance as constructor-argument.</p>
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

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // instance variables.

    protected boolean bLine2bEchoed = false;
    protected String currentLineAfterMacroEval = null;
    protected ConfigFileScannerL2 includedFile = null;
    protected boolean printOutputCmd = false;

    /** This is to be a REFERENCE to an instance of LinkedHashMap, whose object-lifecycle is maintained by some other class (as in, creating new LinkedHashMap&lt;&gt;(), putting content into it, updating content as File is further processed, ..) */
    protected final LinkedHashMap<String,Properties> propsSetRef;

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** <p>The basic constructor - that does __NOT__ allow you to evaluate Macro-expressions like ${XYZ}</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public ConfigFileScannerL2( boolean _verbose ) {
        super( _verbose );
        this.propsSetRef = null;
        reset();
    }

    /** <p>The basic constructor - that does __NOT__ allow you to evaluate Macro-expressions like ${XYZ}</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _propsSet a REFERENCE to an instance of LinkedHashMap, whose object-lifecycle is maintained by some other class (as in, creating new LinkedHashMap&lt;&gt;(), putting content into it, updating content as File is further processed, ..)
     */
    public ConfigFileScannerL2( boolean _verbose, final LinkedHashMap<String,Properties> _propsSet ) {
        super( _verbose );
        this.propsSetRef = _propsSet;
        reset();
    }

    /** Do NOT use.  USE ONLY in emergencies and ONLY IF you know what the fuck you are doing.  No questions will be answered, and NO help will be provided. */
    protected ConfigFileScannerL2() { super(); this.propsSetRef = null; }

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
        // this.resetFlagsForEachLine(); this is already invoked within super.reset()
        this.includedFile = null;
        // if ( this.propsSetRef != null ) this.propsSetRef.clear();  <---- WARNING: Since the lifecycle of the instance pointed to by 'propsSetRef' is owned by someone else!!!
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** This function is exclusively for use within the go() - the primary function within this class - to make this very efficient when responding to the many isXXX() methods in this class.
     */
    @Override
    protected void resetFlagsForEachLine() {
        // super.resetFlagsForEachLine(); <-- this is: protected abstract
        this.bLine2bEchoed = false;
        this.currentLineAfterMacroEval = null;
        this.printOutputCmd = false;
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

    /** <p>New Method added to this subclass.</p>
     *  <p>For use by any Processor of config-file.. to semantically interpret a SPECIFIC command: 'print -';  Typically, such interpretation can mean, print the _OUTPUT_ of the previous line/command in the file.</p>
     *  @return true or false, whether the current line the config/batch file is exactly 'print -'
    */
    public boolean isPrintOutputCmd() {
        return this.printOutputCmd;
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
        final String HDR = CLASSNAME +": currentLine(): ";

        if ( this.includedFile != null )
            return this.includedFile.currentLine();

        this.currentLineAfterMacroEval = super.currentLine();

        if ( this.isLine2bEchoed() ) System.err.println( "Echo (As-Is): "+ this.currentLine() );
        if ( this.verbose ) System.out.println( HDR +"Echo (As-Is): "+ this.currentLine() );

        this.currentLineAfterMacroEval = Macros.evalThoroughly( this.verbose, this.currentLine(), this.propsSetRef );

        if ( this.isLine2bEchoed() ) System.err.println( "Echo (Macros-substituted): "+  getLine_NoEchoPrefix( this.currentLineAfterMacroEval ) );
        if ( this.verbose ) System.out.println( HDR +"Echo (Macros-substituted): "+  this.currentLineAfterMacroEval );

        if ( this.isLine2bEchoed() ) {
            return getLine_NoEchoPrefix( this.currentLineAfterMacroEval );
        } else {
            return this.currentLineAfterMacroEval;
        }
    }

    //=============================================================================
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine().  But this method is a special deviation, as it allows us to get the 'current-line' over-n-over again.
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    @Override
    public String currentLineOrNull()
    {   // !!!!!!!!!!!!!!!!!!!!!! OVERRIDES Parent Method !!!!!!!!!!!!!!!!!!!!!!!!
        final String HDR = CLASSNAME +": currentLineOrNull(): ";

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

            return this.currentLine();
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

            return this.currentLineOrNull();

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
        this.currentLineAfterMacroEval = this.currentLineOrNull(); // remember the line is most likely already trimmed.  We need to chop off any 'echo' prefix

        final String HDR = CLASSNAME +": parseLine(): ";
        if ( this.verbose ) System.out.println( HDR + this.getState() );

        if ( this.currentLineAfterMacroEval == null )
            return true;

        try {
            // ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // This block of code below (echoPattern, echoMatcher, this.bLine2bEchoed) MUST be the very BEGINNNG of this function
            final Pattern echoPattern = Pattern.compile( REGEXP_ECHO );
            final Matcher echoMatcher = echoPattern.matcher( this.currentLineAfterMacroEval );
            if (echoMatcher.find()) {
                if ( this.verbose ) System.out.println( HDR +": I found the command to be ECHO-ed '"+ echoMatcher.group(1) +"' starting at index "+  echoMatcher.start() +" and ending at index "+ echoMatcher.end() );    
                this.currentLineAfterMacroEval = echoMatcher.group(1); // this.currentLineAfterMacroEval.substring( echoMatcher.start(), echoMatcher.end() );
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!! 'this.currentLineAfterMacroEval' has been altered here.
                this.bLine2bEchoed = true;
                if ( this.verbose ) System.out.println( "\t 2nd echoing Line # "+ this.getState() );
                // fall thru below.. to identify other commands
            } // if

            final Pattern includePattern = Pattern.compile( REGEXP_INCLUDE );
            final Matcher includeMatcher = includePattern.matcher( this.currentLineAfterMacroEval );
            if (includeMatcher.find()) {
                if ( this.verbose ) System.out.println( HDR +": I found the INCLUDE command: '"+ includeMatcher.group(1) +"' starting at index "+  includeMatcher.start() +" and ending at index "+ includeMatcher.end() );    
                final String includeFileName = includeMatcher.group(1); // this.currentLineAfterMacroEval.substring( includeMatcher.start(), includeMatcher.end() );
                this.includedFile = this.create();
                final boolean success = this.includedFile.openFile( includeFileName, this.ok2TrimWhiteSpace, this.bCompressWhiteSpace );
                if ( ! success )
                    throw new Exception( "Unknown internal exception opening file: "+ includeFileName );
                if ( this.verbose ) System.out.println( "\t 2nd echoing Line # "+ this.getState() );
                return false; // !!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!! method returns here.
            } // if

            Pattern printPattern = Pattern.compile( "^\\s*print\\s+(\\S.*\\S|-)\\s*$" ); // Note: A line like 'print -' would FAIL to match \\S.*\\S
            Matcher printMatcher    = printPattern.matcher( this.currentLineAfterMacroEval );
            if (printMatcher.find()) {
                if ( this.verbose ) System.out.println( CLASSNAME +": I found the text "+ printMatcher.group() +" starting at index "+  printMatcher.start() +" and ending at index "+ printMatcher.end() );    
                final String printExpression  = printMatcher.group(1); // this.currentLineAfterMacroEval.substring( printMatcher.start(), printMatcher.end() );
                onPrintCmd( printExpression );
                return false; // !!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!! method returns here.
            }

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

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /**
     *  <p>This method is the literal-equivalent of 'echo' of BASH /bin/sh /bin/tcsh. How is this different from 'echo'?  Well, 'echo' is more PRIMITIVE.  It shows the command __TO BE__ executed, after ALL MACRO-Replacements.</p>
     *  @param _printExpression whatever is to the RIGHT side of the 'print' command in the Config file.
     *  @throws Exception any error (potentially from sub-classes)
     */
    protected void onPrintCmd( final String _printExpression ) throws Exception
    {
        final String HDR = CLASSNAME + ": main("+ _printExpression +"): ";
        if ( this.verbose ) System.out.print( HDR +" >>>>>>>>>>>>> @ beginning." );

        // Note: Because of the RegExp based grammer in parseLine().. this assert should never throw.
        assert ( _printExpression != null);

        if ( _printExpression.equals("-") )
        {   // it means: the command/line is:  'print -'   (<-- which refers to printing out WHATEVER is the OUTPUT produced by previous line batchfile)
            this.printOutputCmd = true;
            // This is where we allow SUBCLASSES to make sense of __WHAT__ to output based on context.
        } else {

            String str2output = _printExpression.toString(); // clone
            if ( str2output.trim().endsWith("\\n") ) {
                str2output = str2output.substring(0, str2output.length()-2); // chop out the 2-characters '\n'
                if ( str2output.trim().length() > 0 ) { // Whether: the print command has text other than the \n character

                    // final Object o = this.memoryAndContext.getDataFromMemory( str2output.trim() );
// ??????????????? How do we allow sub-classes to invoke methods like the ABOVE STATEMENT:  Example:    print !lookupContent
// NOTE: A simple workaround:   Within Config/Batch file, we need 2 lines:  (1) use !lookupContent (2) print -
                    System.out.println( str2output ); // println (<--- end-of-line character outputted)

                } else { // if length() <= 0 .. which prints all we have is a simple 'print \n'
                    System.out.println(); // OK. just print a new line, as the print command is NOTHING-BUT-A-simple 'print \n'
                }

            } else {
                System.out.print( str2output +" " ); // print only (<--- NO EOL character outputted.)
                // Why add a ' ' at the end?
                // Because I currently do NOT support printing ANY WhiteSpace (incl. Tabs)
            }

            // ATTENTION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // DO NOT COMMENT THIS ABOVE.  Do NOT ADD AN IF CONDITION to this.  This is by design.
            System.out.flush();
        }
    }

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
                // System.out.println( o.current Line() );
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
