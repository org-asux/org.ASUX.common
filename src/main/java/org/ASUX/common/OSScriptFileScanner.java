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

import java.util.LinkedHashMap;
import java.util.Properties;

import static org.junit.Assert.*;


/**
 *  <p>This is part of org.ASUX.common GitHub.com project and the <a href= "https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>This class is about creating 'Scripts' that cause Java-code (somewhere) to take action.<br>
 *     It extends {@link ScriptFileScanner}.<br>
 *     <b>Make sure to read about this parent-class before proceeding within this class</b>!<br>
 *     This class, it's peers ({@link PropertiesFileScanner}) are key to the org.ASUX projects.</p>
 *  <p>This class represents a bunch of tools, to help make it easy to work with the <em>Script</em> and <em>property</em> files + allowing those file to be very human-friendly w.r.t .comments, variable-substitutions, etc...</p>
 *  <p>This specific class exists solely to <b>automatically load</b> 'System.Environment' as one of the java.util.Properties object under the label="System.env"!</p>
 *  <p>That means you can have variable-substitions (a.k.a. Macro-variable evaluation) automatically work for ${ASUX::user.home} .. or other System.env entries!</p>
 *  <p><b>ATTENTION: (repeating from parent-class documentation) You must use the '<code>ASUX::</code>' prefix, or the variable-substition will Not happen</b>.<br>
 *     Why this special-prefix?  Simple reason!  This org.ASUX project demonstrates the need to create Config and Output files that have ${} expressions for <b>other</b> software to use.  A great example: The org.ASUX.AWS and subjects create Confile files containing ${} expressions for AWS CFN-SDK to further parse.</p>
 */
public class OSScriptFileScanner extends ScriptFileScanner {

    private static final long serialVersionUID = 116L;
    public static final String CLASSNAME = OSScriptFileScanner.class.getName();

    public static final String SYSTEM_ENV = "System.env";

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** <p>The constructor you must use - IF YOU NEED to evaluate Macro-expressions like ${XYZ}</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public OSScriptFileScanner( boolean _verbose ) {
        super( _verbose );
    }

    /** <p>The constructor to use - for SHALLOW-cloning an instance of this class.</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _propsSet Not-Null.  a REFERENCE to an instance of LinkedHashMap, whose object-lifecycle is maintained by some other class (as in, creating new LinkedHashMap&lt;&gt;(), putting content into it, updating content as File is further processed, ..)
     */
    public OSScriptFileScanner( boolean _verbose, final LinkedHashMap<String,Properties> _propsSet ) {
        super( _verbose, _propsSet );
        assertTrue( _propsSet != null );
    }

    //----------------------------------------------------
    /**
     * All subclasses are required to override this method, especially if they have their own instance-variables
     * @return an object of this ScriptFileScanner.java
     */
    protected OSScriptFileScanner create() {
        return new OSScriptFileScanner( super.verbose, super.propsSetRef );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  <p>Creates a well-initialized list of java.util.Properties objects, for use by Operating-System-linked OSScriptFileScanner or it's subclasses.</p>
     *  <p>Currently, the list is augmented by adding just one new Properties object labelled {@link #SYSTEM_ENV}</p>
     *  <p>If the instance passed in as argument to this method _ALREADY_ has a Property object labelled {@link #GLOBALVARIABLES}, then no action is taken.</p>
     *  @param _allProps a NotNull instance (else NullPointerException is thrown)
     *  @return a NotNull object
     */
    public static LinkedHashMap<String,Properties> initProperties( final LinkedHashMap<String,Properties> _allProps ) {
        final Properties existing = _allProps.get( OSScriptFileScanner.SYSTEM_ENV );
        if ( existing == null )
            _allProps.put( OSScriptFileScanner.SYSTEM_ENV, System.getProperties() );
        return _allProps;
    }

    /**
     *  <p>Creates a well-initialized list of java.util.Properties objects, for use by Operating-System-linked OSScriptFileScanner or it's subclasses.</p>
     *  <p>Currently, the list is just size 2, by adding 2 new Properties object labelled {@link org.ASUX.common.OSScriptFileScanner#SYSTEM_ENV} and labelled {@link org.ASUX.common.ScriptFileScanner#GLOBALVARIABLES}</p>
     *  @return a NotNull object
     */
    public static LinkedHashMap<String,Properties> initProperties() {
        LinkedHashMap<String,Properties> allProps = org.ASUX.common.ScriptFileScanner.initProperties();
        allProps = OSScriptFileScanner.initProperties( allProps );
        return allProps;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This deepClone function is VERY MUCH necessary, as No cloning-code can handle 'transient' variables in this class/superclass.
     *  @param _orig what you want to deep-clone
     *  @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     */
    public static OSScriptFileScanner deepClone( final OSScriptFileScanner _orig ) {
        assertTrue( _orig != null );
        try {
            final OSScriptFileScanner newobj = Utils.deepClone( _orig );
            newobj.deepCloneFix( _orig );
            return newobj;
        } catch (Exception e) {
			e.printStackTrace(System.err); // Static Method. So.. can't avoid dumping this on the user.
            return null;
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================


}
