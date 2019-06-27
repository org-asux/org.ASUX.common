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

import java.io.File;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import static org.junit.Assert.*;


/**
 *  <p>This is part of org.ASUX.common GitHub.com project and the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>This class extends {@link org.ASUX.common.ConfigFileScanner}.</p>
 *  <p>These classes together offer a tool-set to help make it very easy to work with the Configuration and Propertyfiles - while making it very human-friendly w.r.t .comments etc...</p>
 *  <p>This specific class offers 'setProperty' as well as 'propertiesFile label=&gt;FILE&lt;' both of which are handled transparently!</p>
 *  <p>Both 'echo' and 'print' may sound similar, but 'print' is the literal-equivalent of 'echo' of BASH /bin/sh /bin/tcsh. So.. why need 'echo'?  Well, 'echo' is more PRIMITIVE.  It shows the command __TO BE__ executed, after ALL MACRO-Replacements.  In that sense, this combination of 'echo' and 'print' is MORE SOPHISTICATED and MORE CAPABLE that 'echo' in BASH, /bin/sh, /bin/tcsh</p>
 *  <p>In addition, this class offers the ability to evaluate expressions JUST LIKE a Bash or /bin/sh or /bin/tcsh does - if you provide a java.util.Properties instance as constructor-argument.</p>
 */
public class ScriptFileScanner extends ConfigFileScannerL2 {

    private static final long serialVersionUID = 115L;
    public static final String CLASSNAME = ScriptFileScanner.class.getName();

    public static final String REGEXP_SLEEP     = "^\\s*sleep\\s+(\\d\\d*)\\s*$";
    public static final String REGEXP_SETPROP	= "^\\s*setProperty\\s+([?]*"+ REGEXP_NAME +")=("+ REGEXP_NAME +")\\s*$"; // here the RHS (REGEXP_FILENAME) is a misnomer.  It's static text.
    public static final String REGEXP_PROPSFILE	= "^\\s*properties\\s+("+ REGEXP_NAME +")=([?]*"+ REGEXP_NAME +")\\s*$";

    public static final String GLOBALVARIABLES = "GLOBAL.VARIABLES";

    // No Instance variables!

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** <p>The constructor you must use - IF YOU NEED to evaluate Macro-expressions like ${XYZ}</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public ScriptFileScanner( boolean _verbose ) {
        super( _verbose, ScriptFileScanner.initProperties() );
assertTrue( false ); // Just to find out ..which code is using this constructor?
    }

    /** <p>The constructor to use - for SHALLOW-cloning an instance of this class.</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _propsSet Not-Null.  a REFERENCE to an instance of LinkedHashMap, whose object-lifecycle is maintained by some other class (as in, creating new LinkedHashMap&lt;&gt;(), putting content into it, updating content as File is further processed, ..)
     */
    public ScriptFileScanner( boolean _verbose, final LinkedHashMap<String,Properties> _propsSet ) {
        super( _verbose, _propsSet );
        assertTrue( _propsSet != null );
    }

    /** Do NOT use.  USE ONLY in emergencies and ONLY IF you know what the fuck you are doing.  No questions will be answered, and NO help will be provided. */
    protected ScriptFileScanner() {
        super();
assertTrue( false ); // Just to find out ..which code is using this constructor?
    }

    //----------------------------------------------------
    /**
     * All subclasses are required to override this method, especially if they have their own instance-variables
     * @return an object of this ScriptFileScanner.java
     */
    protected ScriptFileScanner create() {
        return new ScriptFileScanner( this.verbose, this.propsSetRef );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  <p>Creates a well-initialized list of java.util.Properties objects, for use by ScriptFileScanner or it's subclasses.</p>
     *  <p>Currently, the list is just size=1, with the Properties object labelled {@link #GLOBALVARIABLES}</p>
     *  <p>If the instance passed in as argument to this method _ALREADY_ has a Property object labelled {@link #GLOBALVARIABLES}, then no action is taken.</p>
     *  @param _allProps a NotNull instance (else NullPointerException is thrown)
     *  @return a NotNull object
     */
    public static LinkedHashMap<String,Properties> initProperties( final LinkedHashMap<String,Properties> _allProps ) {
        final Properties existing = _allProps.get( GLOBALVARIABLES );
        if ( existing == null )
            _allProps.put( GLOBALVARIABLES, new Properties() );
        return _allProps;
    }

    /**
     *  <p>Creates a well-initialized list of java.util.Properties objects, for use by ScriptFileScanner or it's subclasses.</p>
     *  <p>Currently, the list is just size=1, with the Properties object labelled {@link #GLOBALVARIABLES}</p>
     *  @return a NotNull object
     */
    public static LinkedHashMap<String,Properties> initProperties() {
        LinkedHashMap<String,Properties> allProps = new LinkedHashMap<String,Properties>();
        allProps = ScriptFileScanner.initProperties( allProps );
        return allProps;
    }

    // /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine() and reset().<br>reset() has draconian-implications - as if openConfigFile() was never called!
    //  */
    // @Override
    // public void reset() {
    //     super.reset();
    //     // this.resetFlagsForEachLine(); this is already invoked within super.reset()
    //     // do NOT EVER DO THIS here in this reset():--> this.propsSetRef.clear();
    // }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

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
        final boolean retb = noprefix.matches( REGEXP_SLEEP ) || noprefix.matches( REGEXP_SETPROP ) || noprefix.matches( REGEXP_PROPSFILE );
        if ( this.verbose ) System.out.println( HDR +"retb="+ retb );
        return retb;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** <p>New Method added to this subclass.  Implement your command parsing and do as appropriate.</p>
     *  <p>ATTENTION: Safely assume that any 'echo' prefix parsing and any 'print' parsing has happened already in a TRANSAPARENT way.</p>
     *  <p>This method is automatically invoked _WITHIN_ nextLine().  nextLine() is inherited from the parent {@link org.ASUX.common.ConfigFileScannerL2}.</p>
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

            Pattern sleepPattern = Pattern.compile( REGEXP_SLEEP );
            Matcher sleepMatcher    = sleepPattern.matcher( super.currentLine() );
            if (sleepMatcher.find()) {
                if ( this.verbose ) System.out.println( HDR +"I found the text "+ sleepMatcher.group() +" starting at index "+  sleepMatcher.start() +" and ending at index "+ sleepMatcher.end() );
                final int sleepDuration = Integer.parseInt( sleepMatcher.group(1) ); // super.currentLine().sub string( sleepMatcher.start(), sleepMatcher.end() );
                if ( this.verbose ) System.out.println( "\t sleep=[" + sleepDuration +"]" );
                System.err.println("\n\tsleeping for (seconds) "+ sleepDuration );
                Thread.sleep( sleepDuration * 1000 );
                return true;
            }

            Pattern setPropPattern = Pattern.compile( REGEXP_SETPROP );
            Matcher setPropMatcher    = setPropPattern.matcher( super.currentLine() );
            if (setPropMatcher.find()) {
                if ( this.verbose ) System.out.println( HDR +"I found the text "+ setPropMatcher.group() +" starting at index "+  setPropMatcher.start() +" and ending at index "+ setPropMatcher.end() );
                final String key = setPropMatcher.group(1);
                final String val = setPropMatcher.group(2);
                if ( this.verbose ) System.out.println( "\t detected KVPair=[" + key +","+ val +"]" );
                String keywom = Macros.evalThoroughly( this.verbose, key, this.propsSetRef );
                final String valwom = Macros.evalThoroughly( this.verbose, val, this.propsSetRef );

                final boolean bOkIfAlreadyExists = keywom.startsWith("?"); // that is, the script-file line was:- 'properties kwom=?fnwom'
                keywom = keywom.startsWith("?") ? keywom.substring(1) : keywom; // remove the '?' prefix from key/lhs string
                final Properties globalVariables = this.propsSetRef.get( GLOBALVARIABLES );
                final String preexisting = globalVariables.getProperty( keywom );

                if ( preexisting != null ) {
                    if ( bOkIfAlreadyExists ) {
                        // Do Nothing, as it means:- if we've already defined this property already .. and .. the script-file line was:- 'setProperty ?key=...'
                        if ( this.verbose ) System.out.println( HDR +"ALREADY EXISTING KVPair: keywom=" + keywom +", pre-existing value="+ preexisting +", - with new-value="+ val +".   But because of '?' prefix to 'key', ignoring "+ super.getState() );
                    } else {
                        if ( this.verbose ) System.out.println( HDR +"!! WARNING !! OVERRIDING/OVERWRITING EXISTING KVPair: keywom=" + keywom +", pre-existing value="+ preexisting +", - with new-value="+ val +" .. "+ super.getState() );
                        globalVariables.setProperty( keywom, val );
                    }
                } else { // no pre-existing kvpair with 'key'
                    globalVariables.setProperty( keywom, val );
                }
                if ( this.verbose ) new Debug(this.verbose).printAllProps( HDR +" FULL DUMP of this.propsSetRef = ", this.propsSetRef );
				return true;
            }

            Pattern propsPattern = Pattern.compile( REGEXP_PROPSFILE );
            Matcher propsMatcher    = propsPattern.matcher( super.currentLine() );
            if (propsMatcher.find()) {
                if ( this.verbose ) System.out.println( HDR +"I found the text "+ propsMatcher.group() +" starting at index "+  propsMatcher.start() +" and ending at index "+ propsMatcher.end() );
                final String key = propsMatcher.group(1);
                final String val = propsMatcher.group(2);
                if ( this.verbose ) System.out.println( "\t detected PropsFile-KVPair=[" + key +","+ val +"]" );
                final String kwom = Macros.evalThoroughly( this.verbose, key, this.propsSetRef );
                final String fnwom = Macros.evalThoroughly( this.verbose, val, this.propsSetRef );

                final boolean bOkIfNotExists = fnwom.startsWith("?"); // that is, the script-file line was:- 'properties kwom=?fnwom'
                final String filename = fnwom.startsWith("?") ? fnwom.substring(1) : fnwom; // remove the '?' prefix from file's name/path

                final Properties props = new Properties();
                if ( this.verbose ) System.out.println( HDR +"Checking to see if filename=[" + filename +" exists.. .." );
                if ( this.verbose ) new Debug(this.verbose).printAllProps( HDR +" FULL DUMP of this.propsSetRef = ", this.propsSetRef );
                final File fileObj = new File ( filename );
                if ( fileObj.exists() && fileObj.canRead() ) {
                    final InputStream istrm = new java.io.FileInputStream( filename );
                    props.putAll( Utils.parseProperties( this.verbose, istrm ) );
                    // props.load( new java.io.FileInputStream( filename ) );
                } else {
                    if ( bOkIfNotExists ) {
                        // Do Nothing, as it means:- if filename does NOT exist.. no problem.
                        if ( this.verbose ) System.out.println( HDR +"DOES NOT EXIST: filename=[" + filename +" exists.   But because of '?' prefix to filename, ignoring error "+ super.getState() );
                    } else {
                        throw new FileNotFoundException("File: "+ filename +" does Not exist.  See "+ super.getState() );
                    }
                }

                final Properties existingPropsObj = this.propsSetRef.get( kwom );
                if ( this.verbose ) System.out.println( HDR +" FOUND Existing properties under the label=["+ kwom +"].");
                if ( existingPropsObj == null )
                    this.propsSetRef.put( kwom, props ); // This line is the action taken by this 'PropertyFile' line of the batchfile
                else
                    existingPropsObj.putAll( props );
                if ( this.verbose ) System.out.println( HDR +" properties label=["+ kwom +"] & file-name=["+ filename +"].");
				return true;
            }

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

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This deepClone function is VERY MUCH necessary, as No cloning-code can handle 'transient' variables in this class/superclass.
     *  @param _orig what you want to deep-clone
     *  @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     */
    public static ScriptFileScanner deepClone( final ScriptFileScanner _orig ) {
        assertTrue( _orig != null );
        try {
            final ScriptFileScanner newobj = Utils.deepClone( _orig );
            newobj.deepCloneFix( _orig );
            return newobj;
        } catch (Exception e) {
			e.printStackTrace(System.err); // Static Method. So.. can't avoid dumping this on the user.
            return null;
        }
    }

    // /**
    //  * In order to allow deepClone() to work seamlessly up and down the class-hierarchy.. I should allow subclasses to EXTEND (Not semantically override) this method.
    //  */
    // protected void deepCloneFix() {
    //         // UNLIKE SUPER-Class .. this CLASS DOES NOT __ANY__ TRANSIENT class-variable.. ..
    //         // So.. we do NOT need this method defined.
    // }

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
            final ScriptFileScanner o = new ScriptFileScanner( verbose, ScriptFileScanner.initProperties() );
            o.useDelimiter( ";|"+System.lineSeparator() );
            o.propsSetRef.put( GLOBALVARIABLES, new Properties() );
            o.openFile( args[ix], true, true );
            while (o.hasNextLine()) {
                if ( verbose ) System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                System.out.println();
                // final String s = o.nextLine();
                // System.out.println( o.current Line() );
                // System.out.println( o.getState() );
                // System.out.println( s );
                System.out.println( o.nextLine() );
                if ( verbose ) System.out.println("________________________________________________________________________________________");
            }
		} catch (Exception e) {
			e.printStackTrace(System.err); // main().  For Unit testing
			System.err.println( HDR + "Unexpected Internal ERROR, while processing " + ((args==null || args.length<=0)?"[No CmdLine Args":args[0]) +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
    }

}
