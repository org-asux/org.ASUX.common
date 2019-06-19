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

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashMap;


import static org.junit.Assert.*;


/**
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects, would simply NOT be possible without the genius Java library <a href="https://github.com/EsotericSoftware/yamlbeans">"com.esotericsoftware.yamlbeans"</a>.</p>
 *  <p>This class is a bunch of tools to help make it easy to work with the java.util.Map objects that the EsotericSoftware library creates.</p>
 *  <p>One example is the work around required when replacing the 'Key' - within the MACRO command Processor.</p>
 *  <p>If the key is already inside single or double-quotes.. then the replacement ends up as <code>'"newkeystring"'</code></p>
 */
public class Output {

    public static final String CLASSNAME = "org.ASUX.common.Output";

    public static final String ASUXKEYWORDFORWRAPPER = "ASUX.output.";
    public static final String ARRAYWRAPPED = ASUXKEYWORDFORWRAPPER+"array";
    public static final String LISTWRAPPED = ASUXKEYWORDFORWRAPPER+"list";
    public static final String SINGLESTRINGWRAPPED = ASUXKEYWORDFORWRAPPER+"singleString";

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    public final boolean verbose;

    /** <p>The only constructor - public/private/protected</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public Output(boolean _verbose) {
        this.verbose = _verbose;
    }

    private Output() {
        this.verbose = false;
    }

    //------------------------------------------------------------------------------
    public static class ASUXException extends Exception {
        private static final long serialVersionUID = 1L;
        public ASUXException(String _s) { super(_s); }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    public static enum OutputType { Type_Unknown, Type_String, Type_ArrayList, Type_LinkedList, Type_KVPair, Type_KVPairs, Type_LinkedHashMap };
                                                            // 'KVPair' has No 's' character @ end.  While 'KVPairs' does.

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    public static class Object<T> {
        public static final String CLASSNAME = "org.ASUX.common.Output:Object<T>";

        private OutputType type = OutputType.Type_Unknown;

        private String string = null;
        private ArrayList<T> array = new ArrayList<>();
        private LinkedList<T> list = new LinkedList<>();
        private Tuple<String,String> kvPair= null;
        private LinkedHashMap<String,java.lang.Object> map = new LinkedHashMap<>();

        //------------------------------------------------------------------------------
        public OutputType getType() { return this.type; }
        public void setType( final OutputType _typ ) { this.type = _typ; }

        //------------------------------------------------------------------------------
        public String getString() throws ASUXException {
            if ( getType() == OutputType.Type_String )
                return this.string;
            else
                throw new ASUXException( CLASSNAME +": getString(): Contents of Object<T> is of type: "+ this.type.toString() );
        }

        public ArrayList<T> getArray() throws ASUXException {
            if ( getType() == OutputType.Type_ArrayList )
                return this.array;
            else
                throw new ASUXException( CLASSNAME +": getArray(): Contents of Object<T> is of type: "+ this.type.toString() );
        }

        public LinkedList<T>  getList() throws ASUXException {
            if ( getType() == OutputType.Type_LinkedList )
                return this.list;
            else
                throw new ASUXException( CLASSNAME +": getList(): Contents of Object<T> is of type: "+ this.type.toString() );
        }

        public Tuple<String,String> getKVPair() throws ASUXException {
            if ( getType() == OutputType.Type_KVPair )
                return this.kvPair;
            else
                throw new ASUXException( CLASSNAME +": getKVPair(): Contents of Object<T> is of type: "+ this.type.toString() );
        }

        public LinkedHashMap<String,java.lang.Object> getMap() throws ASUXException {
            if ( getType() == OutputType.Type_LinkedHashMap )
                return this.map;
            else
                throw new ASUXException( CLASSNAME +": getMap(): Contents of Object<T> is of type: "+ this.type.toString() );
        }

        //------------------------------------------------------------------------------
        public void setString( final String _s ) {
            setType( OutputType.Type_String );
            this.string = _s;
        }

        public void setArray( final ArrayList<T> _a ) {
            setType( OutputType.Type_ArrayList );
            this.array = _a;
        }

        public void setList( final LinkedList<T> _l ) {
            setType( OutputType.Type_LinkedList );
            this.list = _l;
        }

        public void setKVPair( final Tuple<String,String> _kv ) {
            setType( OutputType.Type_KVPair );
            this.kvPair = _kv;
        }

        public void setMap( final LinkedHashMap<String,java.lang.Object> _m ) {
            setType( OutputType.Type_LinkedHashMap );
            this.map = _m;
        }

        //------------------------------------------------------------------------------
        /**
         * Use this method when you do NOT care what is inside this org.ASUX.common.Output.Object instance.
         * Ideally suited to pass this on to YAML-Libraries - specifically for that library to then write it to a file.
         * @return java.lang.Object
         * @throws ASUXException should Not be thrown (semantically robust due to Switch-statement implementation internally), but Java compiler requires I note that this specific exception can happen 
         * @throws Exception this method will throw this, if this org.ASUX.common.Output.Object has Not been initialized yet
         */
        public java.lang.Object getJavaObject() throws ASUXException, Exception {
            // if (this.verbose) System.out.println( CLASSNAME +": getJavaObject(): type= ["+ this.type +"]" );

            // for the following SWITCH-statement, keep an eye on Output.OutputType
            switch( this.type ) {
                case Type_ArrayList:    return this.getArray();
                case Type_LinkedList:   return this.getList();
                case Type_KVPairs:      return this.getMap();
                case Type_LinkedHashMap:    return this.getMap();
                case Type_String:       return this.getString();
                case Type_KVPair:       return this.getKVPair();
                case Type_Unknown:
                default:
                    throw new Exception( CLASSNAME + ": getJavaObject(): Serious INTERNAL-ERROR .. more likely you invoked .getJavaObject() method on an UNInitialized object)!!!" );
            } // switch
        } // method: getJavaObject

    } // Output.Object class definition

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    public LinkedHashMap<String, java.lang.Object> wrapAnObject_intoLinkedHashMap( final java.lang.Object _output ) throws Exception
    {
        // do NOT MAKE the mistake of using a SWITCH statement using 'typ' below.
        // final Output.OutputType typ = this.getWrappedObjectType( _o );
        // switch( typ )  <<---- do Not do this!!!!!!!!!!!!!!!!!!!!!!!!!!!!

// UNKNOWN: What if .. the code BELOW, in this function.. unknowingly wraps an already wrapped object??????????????

        if ( _output instanceof String ) {
            @SuppressWarnings("unchecked")
            final String s = ( String ) _output;
            LinkedHashMap<String, java.lang.Object> retMap = new LinkedHashMap<String, java.lang.Object>();
            retMap.put( SINGLESTRINGWRAPPED, s );
            // retMap = Tools.lintRemover(retMap);
            return retMap;
        } else if ( _output instanceof LinkedList ) {
            @SuppressWarnings("unchecked")
            final LinkedList<java.lang.Object> list = ( LinkedList<java.lang.Object> ) _output;
            // list.forEach( s -> System.out.println( s.toString() ) );
            LinkedHashMap<String, java.lang.Object> retMap = new LinkedHashMap<String, java.lang.Object>();
            retMap.put( ARRAYWRAPPED, list );
            // retMap = Tools.lintRemover(retMap);
            return retMap;

        } else if ( _output instanceof ArrayList ) {
            @SuppressWarnings("unchecked")
            final ArrayList<String> arr = ( ArrayList<String> ) _output;
            LinkedHashMap<String, java.lang.Object> retMap = new LinkedHashMap<String, java.lang.Object>();
            retMap.put( LISTWRAPPED, arr );
            // retMap = Tools.lintRemover(retMap);
            return retMap;

        } else if ( _output instanceof LinkedHashMap) {
            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, java.lang.Object> retMap = (LinkedHashMap<String, java.lang.Object>) _output;
            // do I need to run Tools.lintRemover() as the _output of org.ASUX.yaml library stays 100% conformant withe com.esotericsoftware library usage.
            return retMap;

        } else {
            throw new Exception ( CLASSNAME +": wrapAnObject_intoLinkedHashMap(): _output is Not of type LinkedHashMap.  It's ["+ ((_output==null)?"null":_output.getClass().getName()) +"]");
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================


    public OutputType getWrappedObjectType( java.lang.Object o )
    {
        if ( o == null ) return OutputType.Type_Unknown;
        if ( o instanceof String ) return  OutputType.Type_String;
        if ( o instanceof Tuple ) return  OutputType.Type_KVPair; // singular;  No 's' character @ end.  This is Not KVPairs
        if ( o instanceof ArrayList ) return  OutputType.Type_ArrayList;
        if ( o instanceof LinkedList ) return  OutputType.Type_LinkedList;
        if ( o instanceof LinkedHashMap ) {

            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, java.lang.Object> map = (LinkedHashMap<String, java.lang.Object>) o;

            if (map.keySet().size() <= 0) return OutputType.Type_LinkedHashMap; // This is the only unclear scenario

            if ( map.keySet().size() > 1 ) {
                if ( this.verbose ) System.out.println( CLASSNAME +": getWrappedObjectType(): checking whether a Map is KVPairs/Plural.. for "+ map.toString() );
                // check to see if the LinkedHashMap is just 1-level deep with NOTHING but String:String Key-value pairs.
                // That is, the LinkedHashMap has NO NESTING.
                boolean bOnlyKVPairs = true; // guilty until proven innocent.
                for ( String k: map.keySet() ) {
                    if ( map.get(k) instanceof String ) {
                        continue;
                    } else {
                        bOnlyKVPairs = false;
                        break;
                    }
                }
                if ( this.verbose ) System.out.println( CLASSNAME +": getWrappedObjectType(): .. .. .. .. it turns out that .. bOnlyKVPairs= "+ bOnlyKVPairs );
                if ( bOnlyKVPairs )
                    return OutputType.Type_KVPairs; // PLURAL;  Note the 's' character @ end.  This is Not KVPair (singular)
                else
                    return OutputType.Type_LinkedHashMap; // Its a big goop of data at at least 1 level of NESTED Maps inside it.

            } else {
                assertTrue( map.keySet().size() == 1); // better be true - unless the above PAIR of IF-conditions above are messed-with
                final String k = map.keySet().iterator().next();
                if ( this.verbose ) System.out.println( CLASSNAME +": getWrappedObjectType(): A single-key LinkedHashMap with k=["+ k +"]" );
                if ( ( ! ARRAYWRAPPED.equals(k))  &&  (  ! LISTWRAPPED.equals(k)) )
                    return OutputType.Type_LinkedHashMap;

                final java.lang.Object o1 = map.get(k);
                if ( o1 instanceof String ) return  OutputType.Type_String;
                if ( o1 instanceof ArrayList ) return  OutputType.Type_ArrayList;
                if ( o1 instanceof LinkedList ) return  OutputType.Type_LinkedList;
                if ( o1 instanceof LinkedHashMap ) return  OutputType.Type_LinkedHashMap;
                return OutputType.Type_Unknown;
            }
        }
        return OutputType.Type_Unknown;
    }

    //--------------------------------
    /**
     * THis function exists.. as much of ths org.ASUX.yaml library requires a LinkedHashMap object.
     * So, any "sources" that bring in a "ArrayList", "LinkedList" or even a Scalar "String" object.. are put into a simple LinkedHashMap "wrapper"!
     * So.. this function exists to automatically "unwrap" that wrapper (if that's the case), or to return the object as-is (if that is Not the case)
     * @param _o the object we'd like to get the "real" value of
     * @return "unwrap" that simple LinkedHashMap wrapper (if that's the case), or to return the object as-is (if that is Not the case)
     * @throws Exception if unknown type, other than "LinkedHashMap", "ArrayList", "LinkedList" or even a Scalar "java.lang.String"
     */
    public java.lang.Object getTheActualObject( final java.lang.Object _o ) throws Exception
    {
        if ( _o == null ) return null;
        // final OutputType typ = this.getWrappedObjectType( _o );
        if ( _o instanceof String ) {
            return _o;
        } else if ( _o instanceof LinkedList ) {
            return _o;
        } else if ( _o instanceof ArrayList ) {
            return _o;

        } else if ( _o instanceof LinkedHashMap) {
// SHOULD I run Tools.lintRemover() to ensure this object stays 100% conformant withe com.esotericsoftware library usage.???

            // let's check if this LinkedHashMap is a 'wrapper' for an object that is Not a LinkedHashMap.
            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, java.lang.Object> map = (LinkedHashMap<String, java.lang.Object>) _o;

            if ( map.keySet().size() <= 0 ) {
                return _o; // This is the only unclear scenario.  Perhaps an empty result from a previous command in YAML-Batch?
            } else if ( map.keySet().size() > 1 ) {
                return _o; // return the originally passed object AS-IS.  It's a full-blown LinkedHashMap!
            } else {
                assertTrue( map.keySet().size() == 1 ); // better be true - unless the above PAIR of IF-conditions above are messed-with
                // Implies this LinkedHashMap is very likely a SIMPLE Wrapper.
                final String k = map.keySet().iterator().next();
                if ( k.startsWith( ASUXKEYWORDFORWRAPPER ) ) {
                    final java.lang.Object o1 = map.get(k);
                    return o1;
                } else {
                    return _o; // well.. .. we almost mistook it for a "wrapper"
                }
            }
        } else {
            throw new Exception( CLASSNAME +": getTheActualObject(): ERROR: unknown object of type ["+ _o.getClass().getName() +"]");
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    //------------------------------------------------------------
    public String getString( java.lang.Object _o ) throws Exception {
        if ( _o == null ) return null;
        final OutputType typ = this.getWrappedObjectType( _o );
        switch(typ) {
            case Type_String:
                @SuppressWarnings("unchecked")
                final String s = (String) this.getTheActualObject( _o );
                return s;
            case Type_LinkedHashMap:
                    final java.lang.Object o1 = this.getTheActualObject( _o );
                    if ( o1 instanceof String ) {
                        @SuppressWarnings("unchecked")
                        final String s2 = (String) o1;
                        return s2;
                    } else {
                        throw new Exception ( CLASSNAME +": getString(): LinkedHashMap with just 1 entry has object NOT of String type: "+ o1.getClass().getName() );
                    }
                    // break;
            case Type_KVPair:
            case Type_KVPairs:
            case Type_LinkedList:
            case Type_ArrayList:
            case Type_Unknown:
            default:
                throw new Exception( CLASSNAME +": getString(): ERROR: unknown object of type ["+ _o.getClass().getName() +"]" );
        } // switch
    }

    //------------------------------------------------------------
    public ArrayList< Tuple< String,String > >      getKVPairs( final java.lang.Object _o ) {
        ArrayList< Tuple< String,String > > kvpairs = new ArrayList<>();
        final OutputType typ = this.getWrappedObjectType( _o );
        if ( typ != OutputType.Type_KVPairs )
            return kvpairs; // as an empty ArrayList.

        @SuppressWarnings("unchecked")
        final LinkedHashMap<String, java.lang.Object> map = (LinkedHashMap<String, java.lang.Object>) _o;
        for ( String k: map.keySet() ) {
            kvpairs.add( new Tuple< String,String>(k, map.get(k).toString() ) );
        }
        return kvpairs; //unless o is an empty Map, this will have something in it.
    }

    //------------------------------------------------------------
    public ArrayList<String> getArrayList( final java.lang.Object _o ) throws Exception {
        if ( _o == null ) return null;
        final OutputType typ = this.getWrappedObjectType( _o );
        final java.lang.Object o1 = this.getTheActualObject( _o );
        switch(typ) {
            case Type_ArrayList:
                @SuppressWarnings("unchecked")
                final ArrayList<String> arr = (ArrayList<String>) o1;
                return arr;
            case Type_LinkedHashMap:
                    if ( o1 instanceof ArrayList ) {
                        @SuppressWarnings("unchecked")
                        final ArrayList<String> arr2 = (ArrayList<String>) o1;
                        return arr2;
                    } else {
                        throw new Exception ( CLASSNAME +": getArrayList(): LinkedHashMap with just 1 entry has object NOT of ArrayList type: "+ o1.getClass().getName() );
                    }
                    // break;
            case Type_String:
            case Type_KVPair:
            case Type_KVPairs:
            case Type_LinkedList:
            case Type_Unknown:
            default:
                throw new Exception( CLASSNAME +": getArrayList(): ERROR: unknown object of type ["+ _o.getClass().getName() +"]" );
        } // switch
    }

    //------------------------------------------------------------
    public LinkedList<String> getLinkedList( final java.lang.Object _o ) throws Exception {
        if ( _o == null ) return null;
        final OutputType typ = this.getWrappedObjectType( _o );
        switch(typ) {
            case Type_LinkedList:
                @SuppressWarnings("unchecked")
                final LinkedList<String> list = (LinkedList<String>) this.getTheActualObject( _o );
                return list;
            case Type_LinkedHashMap:
                    final java.lang.Object o1 = this.getTheActualObject( _o );
                    if ( o1 instanceof LinkedList ) {
                        @SuppressWarnings("unchecked")
                        final LinkedList<String> list2 = (LinkedList<String>) o1;
                        return list2;
                    } else {
                        throw new Exception ( CLASSNAME +": getLinkedList(): LinkedHashMap with just 1 entry has object NOT of LinkedList type: "+ o1.getClass().getName() );
                    }
                    // break;
            case Type_String:
            case Type_KVPair:
            case Type_KVPairs:
            case Type_ArrayList:
            case Type_Unknown:
            default:
                throw new Exception( CLASSNAME +": getLinkedList(): ERROR: unknown object of type ["+ _o.getClass().getName() +"]" );
        } // switch
    }

}
