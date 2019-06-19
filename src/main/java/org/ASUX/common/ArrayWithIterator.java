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

import java.util.stream.IntStream;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import static org.junit.Assert.*;


/** A smarter iterator class, that is 2-in-1.
 *  @param <T> the parameter of the class, typically java.lang.String or org.ASUX.Tuple
 *  It's both an array and iterator, especially an iterator that can provide the current 'index' to the underlying array.
 */
public class ArrayWithIterator<T> implements java.util.Iterator, java.io.Serializable {

	public static final String CLASSNAME = "org.ASUX.common.ArrayWithIterator";

    //------------------------------------------------------------------------------
    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    public final boolean verbose;

    private final boolean isValid;
    private final T[] arraylist;

    protected int indexPtr = -1;

    //------------------------------------------------------------------------------
    /**
     * This Exception type is thrown exclusively by ArrayWithIterator.java class.
     * That way, you can report better errors to the end-user
     */
    public static class ArrayWithIteratorException extends Exception {
        private static final long serialVersionUID = 10L;
        public ArrayWithIteratorException(String _s) { super(_s); }
    }

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================

    /** Rudimentary Constructor.  Takes an object that is compatible with Arrays
     *  @param _array a basic java immutable Array
     */
    public ArrayWithIterator( final T[] _array ) {
        this( false, _array );
    }

    /** Rudimentary Constructor.  Takes an object that is compatible with the java.util.AbstractCollection interface (example: LinkedList, ArrayList, HashSet)
     *  @param _coll an object that implements the java.util.AbstractCollection interface (example: LinkedList, ArrayList, HashSet)
     */
    public ArrayWithIterator( final java.util.AbstractCollection<T> _coll ) {
        this( false, _coll );
    }

    /** Rudimentary Constructor.  Takes an object that is compatible with Arrays
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _array a basic java immutable Array
     */
    public ArrayWithIterator( final boolean _verbose, final T[] _array )
                // throws ArrayWithIteratorException
    {
        this.verbose = _verbose;
        this.arraylist = _array;
        this.isValid = (this.arraylist.length > 0) ? true : false;
        this.indexPtr = (this.arraylist.length > 0) ? 0 : -1;
    } // Constructor

    /** Rudimentary Constructor.  Takes an object that is compatible with Arrays
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _coll an object that implements the java.util.AbstractCollection interface (example: LinkedList, ArrayList, HashSet)
     */
    public ArrayWithIterator( final boolean _verbose, final java.util.AbstractCollection<T> _coll )
                // throws ArrayWithIteratorException
    {
        this( _verbose, constructorUtil( _coll ) );
        // @SuppressWarnings("unchecked")
        // final T[] a = (T[]) _coll.toArray( this.arraylist );
        // if ( _coll.size() > 0 ) {
        //     final Iterator<T> itr = _coll.iterator();
        //     while ( itr.hasNext ) {}
        //     this.arraylist = new T[ _coll.size() ];
        //     int i = -1;
        //     for( T entry: _coll ) {
        //         i++;
        //         this.arraylist[ i ] = entry;
        //     }
        // }
        // this( _verbose, a );
    } // Constructor

    private static <T> T[] constructorUtil ( final java.util.AbstractCollection<T> _coll ) {
        @SuppressWarnings("unchecked")
        final T[] a = (T[]) _coll.toArray(); // this step is just to get a non-null object of type T[] - just to get around compiler's errors.
                    // as long we do NOT access anything within 'a' we should be without ANY runtime exceptions
        final T[] b = (T[]) _coll.toArray( a ); // this statement guarantees compiler-wise and runtime-wise, that no exception will be thrown.
        return b;
    }

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================

    /** Whether the instance of this class is valid (in case you are passed this object by some other code, this is your sanity check).. .. before you invoke any of the other functions in ths class and end up with runtime errors
     *  @return true means all the methods in this class are GUARANTEED to NOT Throw any runtime exception :-)
     */
    public boolean isValid() {
        return this.isValid;
    }

    /**
     * Ths function will ensure this object behaves as if.. you are going to call hasNext() and next() for the 1st time (on this specific instance/object).
     */
    public void rewind() {
        this.indexPtr = 0;
    }

    /** For example strings like "<code>paths.*.*.responses.200</code>", this will make this object point to the last element"<code>200</code>"".
     */
    public void skip2end() {
        if ( ! this.isValid ) return;
        // if ( this.index() >= this.arraylist.length ) // perhaps we fully iterated this.hasNext() to the end .. already.
        //     this.rewind();
        // for ( int ix=index(); ix< this.arraylist.length; ix++ )
        //     ypNoMatches.next(); // if we loop all the way to 'this.arraylist.length' then we'll end up with this,index() pointing WELL beyond the 
        this.indexPtr = this.arraylist.length - 1;
        return;
    }

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================

    /** see java.util.Iterator
     *  @return true means {@link get} will return a valid string, GUARANTEED to NOT Throw any runtime exception :-)
     */
    public boolean hasNext() {
        if ( this.verbose ) System.out.println( CLASSNAME + ": hasNext(): Starting @ "+ this.indexPtr );
        if (   !   this.isValid ) return false;

        if ( this.indexPtr < this.arraylist.length )
            return true;
        else
            return false;
    }

    /** This is Not part of java.util.Iterator, but am providing it to handle boundary-scenarios much better than having NullPointerExceptions much later in running code.
     *  @return the object at the next location in the array
     *  @throws ArrayWithIteratorException if this variant of next()-method is called without checking hasNext().  No other reason to throw this specific exception.
     *  @throws IndexOutOfBoundsException shouldn't happen, unless a defect within this code base for ArrayWithIterator class.
     */
    public T next_throws() throws ArrayWithIteratorException, IndexOutOfBoundsException {
//        String retstr = null;
//        if ( this.hasNext() ) {
//            retstr = this.arraylist[this.indexPtr];
//            this.indexPtr ++;
//        }
//        return retstr;
        if ( this.hasNext() ) {
            this.indexPtr ++;
            return get();
        } else 
            throw new ArrayWithIteratorException( CLASSNAME +": next(): Array has size="+ this.size() +" but hasNext()="+ this.hasNext() +" and current index is="+ this.indexPtr );
    }

    /** see java.util.Iterator
     *  @return the object at the next location in the array
     */
    public T next() {
        if ( this.hasNext() ) {
            this.indexPtr ++;
            return getOrNull();
        } else 
            return null;
    }

    // /**
    //  * See java.util.Iterator
    //  * Performs the given action for each remaining element until all elements have been processed or the action throws an exception. Actions are performed in the order of iteration, if that order is specified. Exceptions thrown by the action are relayed to the caller.
    //  */
    // void forEachRemaining( java.util.function.Consumer<? super E> _action ) {
    //     // The default implementation behaves as if:
    //     while (hasNext())
    //         _action.accept(next());
    // }

    /** Return the element pointed to by the current index {@link #index} inside the array stored internally.
     *  @return a string that does NOT have periods/dots/delimiter in it.  The string may be (based on example above) = "*".
     *  @throws IndexOutOfBoundsException If you invoke this after hasNext() returns false, you'll get this Exception.  Also, this exception will be throws if this class has bugs.
     */
    public T get() throws IndexOutOfBoundsException {
        if ( ! this.isValid ) return null;
        if ( this.indexPtr < this.arraylist.length )
            return this.arraylist[this.indexPtr];
        else
            throw new IndexOutOfBoundsException( CLASSNAME +": get(): Array has size="+ this.size() +" but hasNext()="+ this.hasNext() +" and current index is="+ this.indexPtr );
    }

    /** Return the element pointed to by the current index {@link #index} inside the array stored internally.
     *  @return a string that does NOT have periods/dots/delimiter in it.  The string may be (based on example above) = "*".
     */
    public T getOrNull() {
        if ( ! this.isValid ) return null;
        if ( this.indexPtr < this.arraylist.length )
            return this.arraylist[this.indexPtr];
        else
            return null;
    }

    /**
     * # of elements stored inside the array
     * @return an integer &gt;= 0 or -1, if this instance is invalid.
     */
    public int size() {
        if ( ! this.isValid )
            return -1;
        else
            return this.arraylist.length;
    }

    // /**
    //  * # of elements stored inside the array
    //  * @return an integer &gt;= 0 or -1, if this instance is invalid.
    //  */
    // public int length() {
    //     return this.size();
    // }

    /**
     *  Creates a Spliterator over the elements described by this Iterable.
     *  Implementation Requirements: The default implementation creates an early-binding spliterator from the iterable's Iterator. The spliterator inherits the fail-fast properties of the iterable's iterator.
     *  Implementation Note: The default implementation should usually be overridden. The spliterator returned by the default implementation has poor splitting capabilities, is unsized, and does not report any spliterator characteristics. Implementing classes can nearly always provide a better implementation.
     */
    // default Spliterator<T> spliterator() {
    // }


    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================

    /** For example strings like "<code>paths.*.*.responses.200</code>", your first call will return 0 (index numbering per C/Java array-index standard).  Every call to next() will increment the return value of this function.  When you call next() a 5th/6th/7th/../100th time for above example, this function will return the same value then onwards (= # of elements in the ArrayWithIterator-string.  In this example, that is 5)
     *  @return integer &gt;= 0 (if things are working) and -1 is things are screwed up.
     */
    public int index() {
        if ( ! this.isValid ) return -1;

        // there is No reason that this.arraylist.index will be < 0 -- if this instance has this.isValid is true, as we do Not decrement this variable
        assertTrue( this.indexPtr >= 0 );

        if ( this.indexPtr < this.arraylist.length )
            return this.indexPtr;
        else
            return this.arraylist.length; // This is to mark that we've a valid instance, and the user has iterated beyond the length of the array
    }

    //-----------------------------------------------------------------
    /** For debug purposes, this function can return all the values beyond the iterator's current position.
     *  @return a string
     */
    public String getPrefix() {
        if ( ! this.isValid ) return null;
        if ( this.indexPtr < this.arraylist.length ) {
            String retstr = "";
            // Compiler error: local variables referenced from a lambda expression must be final or effectively final
            // IntStream.range(0, this.indexPtr).forEach(i -> retstr+= this.arraylist[i]);
            final int[] range = IntStream.range(0, this.indexPtr).toArray();
            for( int ix : range )
                retstr += this.arraylist[ix].toString() + "\t";
            return retstr;
        }else{
            return null; // We've a problem if we're here
        }
    }

    /** For example strings like "<code>paths.*.*.responses.200</code>", before your 1st call to next(), this function will return "<code>paths.*.*.responses.200</code>".  After the 1st call to next(), this function will return "<code>*.*.responses.200</code>".  After the 3rd call to next(), this will return "<code>responses.200</code>".  After you call next() a 5th time(or more), this function will return null(String).
     *  @return a string that does NOT have periods/dots in it.  The string may be (based on example above) = "*".
     */
    public String getSuffix() {
        if ( ! this.isValid ) return null;
        if ( this.indexPtr < this.arraylist.length ) {
            String retstr = "";
            // Compiler error: local variables referenced from a lambda expression must be final or effectively final
            // IntStream.range(this.indexPtr, this.arraylist.length).forEach(i -> retstr+= this.arraylist[i]);
            int[] range = IntStream.range(this.indexPtr, this.arraylist.length).skip(1).toArray();
            for( int ix : range )
                retstr += "\t" + this.arraylist[ix].toString();
            return retstr;
        }else{
            return null; // We've a problem if we're here
        }
    }

    /**
     * Implements the Object.toString() operation .. in a superior manner for debugging.
     */
    public String toString() {
        if ( this.isValid )
            return this.getPrefix() +"\t@"+ this.index() +":"+ this.getOrNull() +"\t"+ this.getSuffix();
        else
            return "Invalid object of "+CLASSNAME;
    }

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================


    /** This deepClone function is unnecessary, if you can invoke org.apache.commons.lang3.SerializationUtils.clone(this)
     *  @param <E> Since this method is static, it has a separate naming convention 'E'.  But it should be the same class 'T' as used in constructor.
     *  @param _orig what you want to deep-clone
     *  @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     *  @throws Exception if there is any trouble in cloning.  Example: whether the object being cloned does NOT implement the java.io.Serializable, or CLASSPATH does Not have the class-definition, etc..
     */
    public static <E> ArrayWithIterator<E> deepClone(ArrayWithIterator<E> _orig) throws Exception {
        @SuppressWarnings("unchecked")
        final ArrayWithIterator<E> clone = (ArrayWithIterator<E>) org.ASUX.common.Utils.deepClone( _orig );
        return clone;
    }

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================


    /** 
     *  This function does NOT assume any common objects/strings.
     *  It does a TRUE value-based comparison - by taking advantage of the fact that this class is java.io.Streamable.
     *  By implications, if you have cloned a ArrayWithIterator instance and called next() on the clone, they are NOT equal.
     *  @param _lhs left hand side
     *  @param _rhs right hand side
     *  @return true or fale
     */
    public static boolean equals( ArrayWithIterator<?> _lhs, ArrayWithIterator<?> _rhs ) {
        try {
            ByteArrayOutputStream baosLHS = new ByteArrayOutputStream();
            ObjectOutputStream oosLHS = new ObjectOutputStream(baosLHS);
            oosLHS.writeObject(_lhs);
            ByteArrayOutputStream baosRHS = new ByteArrayOutputStream();
            ObjectOutputStream oosRHS = new ObjectOutputStream(baosRHS);
            oosRHS.writeObject(_rhs);
            return java.util.Arrays.equals( baosLHS.toByteArray(), baosRHS.toByteArray() );
        } catch (java.io.IOException e) {
            return false;
        // } catch (ClassNotFoundException e) {
        //     return null;
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    public static void main(String[] args) {
        System.out.println( CLASSNAME + ": main(): started with '"+args[0]+"'" );
        ArrayWithIterator<?> awi = new ArrayWithIterator<>( true, args );
        submain( awi );

        System.out.println("-----------------------------------------------------");
        final java.util.ArrayList<String> arraylist = new java.util.ArrayList<>( java.util.Arrays.asList(args) );
        // WARNING!!!
        // List<T> list = Arrays.asList(array);
        // This above line is a SHORTCUT that will cause trouble later..
        // Note: The list returned from asList() is a fixed-size list backed by the original array.
        // Because the size of the list returned from asList() is fixed.. and since ArrayList is essentially implemented as an array..
        //  .. later, if we want to add or remove elements from the returned list, an UnsupportedOperationException will be thrown.
        final ArrayWithIterator<?> awi2 = new ArrayWithIterator<>( true, arraylist );
        submain( awi2 );

        System.out.println("-----------------------------------------------------");
        awi2.rewind();
        submain( awi2 );
        System.out.println("-----------------------------------------------------");
        awi2.rewind();
        awi.hasNext();
        awi.skip2end();
        submain( awi2 );
    }

    //===============================================================================================
    public static void submain( final ArrayWithIterator<?> awi ) {
        System.out.println( CLASSNAME + ": constructor complete.  Size ="+ awi.size() );
        System.out.println( awi.toString() );

        // these lines below test Boundary-condition behavior, when the 1st invocation of hasNext() has NOT yet happened.
        awi.getOrNull();
        try {
            awi.next_throws();
        } catch ( ArrayWithIteratorException e ) { System.err.println( "Caught ArrayWithIteratorException: "+ e.getMessage() ); }
        try {
            awi.get();
        } catch ( IndexOutOfBoundsException e ) { System.err.println( "Caught IndexOutOfBoundsException: "+ e.getMessage() ); }

        while (awi.hasNext()) {
            System.out.println( awi.get() );
            awi.next();
        }
        awi.hasNext();
        awi.skip2end();

        // these lines below test Boundary-condition behavior, when hasNext() retuns false.
        try {
            awi.next_throws();
        } catch ( ArrayWithIteratorException e ) { System.err.println( "Caught ArrayWithIteratorException: "+ e.getMessage() );
        } catch ( IndexOutOfBoundsException e ) { System.err.println( "Caught IndexOutOfBoundsException: "+ e.getMessage() ); }
        try {
            awi.get();
        } catch ( IndexOutOfBoundsException e ) { System.err.println( "Caught IndexOutOfBoundsException: "+ e.getMessage() ); }
        awi.hasNext();
        awi.getOrNull();
        awi.hasNext();
        System.out.println( CLASSNAME + ": UNIT-TESTING complete.  Size ="+ awi.size() );
        System.out.println( awi.toString() );
    }

}
