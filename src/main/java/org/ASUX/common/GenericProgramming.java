/**
 * BSD Style license.
 * Copyright (c) 2006,Uday Bhaskar Sarma Seetamraju
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the USS Infrastructures nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.ASUX.common;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * <p>Created on Aug 26, 2005</p>
 * <p>Once you switch to JAVA1.6 or better (supporting templates/generics), switch to :-</p>
 * 		<p>http://sourceforge.net/projects/privaccessor</p>
 * <p>Look inside the SOURCE code for this project under the path :-</p>
 * 		<p>/PrivilegedAccessor/src/main/java/junit/extensions</p>
 */

public class GenericProgramming {
    public static final String CLASSNAME = GenericProgramming.class.getName();
    
    /**
     * <p>Stop using this for Java1.6 and Higher -- use http://sourceforge.net/projects/privaccessor </p>
     * 
     * <p>Invoke  "obj.getFldnm()" for field whose name is "fldnm".</p>
     * <p>If you wanna access the field directly using Reflection, use {@link #getAnyField} </p>
     * @param obj Any java object
     * @param fldnm a valid fieldname [a-zA-Z-][0-9a-zA-Z-]+  (This method does not check whether this parameter has a valid fieldname)
     * @return the return value of the getter method.
     */
    public static Object fetchFieldValueUsingGetter( final Object obj ,final String fldnm )
    {
        // final String HDR= CLASSNAME +": fetchFieldValueUsingGetter("+fldnm+", " +obj.getClass().getName()+") : ";
        
        final StringBuffer objmthdnm = new StringBuffer(fldnm);
        objmthdnm.setCharAt( 0 ,Character.toUpperCase(objmthdnm.charAt(0)) );
        objmthdnm.insert( 0, "get" );

        return invokeMethod( obj, objmthdnm.toString()  );

    } // End fetchFieldValueUsingGetter()
    
    
    /**
     * <p>Stop using this for Java1.6 and Higher -- use http://sourceforge.net/projects/privaccessor</p>
     * 
     * <p>Invoke  "obj.methodName()" which takes no parameters.</p>
     * @param obj an instance of ANY class that has a method to be invoked
     * @param methodName a valid methodName [a-zA-Z-][0-9a-zA-Z-]+  (This method does not check whether this parameter has a valid methodName)
     * @return the return value of the getter method.
     */
    public static Object invokeMethod( final Object obj ,final String methodName )
    {
        final Class<?>[] noParameterTypes = new Class[0];
        final Object[] noParameters = new Object[0];
        return invokeMethod( obj, methodName, noParameterTypes, noParameters );
    }

    /**
     * <p>Stop using this for Java1.6 and Higher -- use http://sourceforge.net/projects/privaccessor</p>
     * 
     * <p>Invoke  "obj.methodName(parameters)" where parameters have respective types listed in "parameterTypes".</p>
     * <p>This method does NOT do any checking to see if the # of parameters passed (as args 3 and 4) for "methodName"
     * 		are the right set (i.e., whether such a signature exists).</p>
     * @param obj an instance of ANY class that has a method to be invoked
     * @param methodName a valid methodName [a-zA-Z-][0-9a-zA-Z-]+  (This method does not check whether this parameter has a valid methodName)
     * @param parameterTypes - - the list of parameters TYPES.  Example: new Class[] prms = { String.class };
     * @param parameters - the list of parameters.  Example: new Class[] prms = new String[]{ "value" };
     * @return the return value of the getter method.
     */
    public static Object invokeMethod( final Object obj
            ,final String methodName
            ,final Class<?>[] parameterTypes
            ,final Object[] parameters )
    {
        if ( obj == null || methodName == null )
            return null;

        final String HDR= CLASSNAME +": invokeMethod("+obj.getClass().getName()+", "+methodName+") : ";

        Method method = null;
        // Used as the parameter list for a function with NO PARAMs.
        
        try{
            method = obj.getClass().getDeclaredMethod(
                    methodName.toString(), parameterTypes );
        }catch(NoSuchMethodException e1){
            e1.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
            System.err.println( "\n\n"+ HDR +" Internal-Error: getDeclaredMethod("+methodName+") for class '"+ obj.getClass().getName() +"' - NoSuchMethodException: "+ e1 );
            return null;
        }catch(SecurityException e2){
            e2.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
            System.err.println( "\n\n"+ HDR +" Internal-Error: getDeclaredMethod("+methodName+") for class '"+ obj.getClass().getName() +"' - SecurityException: "+ e2 );
            return null;
        }
        
        if ( method == null )
        {   System.err.println( HDR+"No method "+ obj.getClass().getName() +"."+ methodName +"() found.");
            return null;
        }
        
        //-------------------
        return invokeMethod( obj, method, parameters );
        
    } // End invokeMethod()


    /**
     * <p>Stop using this for Java1.6 and Higher -- use http://sourceforge.net/projects/privaccessor</p>
     * 
     * <p>Invoke  "obj.methodName(parameters)" where parameters have respective types listed in "parameterTypes".</p>
     * <p>This method DOES CHECK to see if the # of parameters passed (as args 3 and 4) for "methodName"
     * 		are the right set (i.e., whether such a signature exists).</p>
     * <p>That is the primary difference between this and the above polymorphic variation.</p>
     * <p>The suffix '_DbC' refers to Bertrand Meyer's DesignByContract.</p>
     * @param obj an instance of ANY class that has a method to be invoked
     * @param staticMethodName a valid STATIC methodName [a-zA-Z-][0-9a-zA-Z-]+  (This method does not check whether this parameter has a valid methodName)
     * @param parameterTypes - the list of parameters TYPES.  Example: new Class[] prms = { String.class };
     * @param parameters - the list of parameters.  Example: new Class[] prms = new String[]{ "value" };
     * @param verboseLevel 0 for no output at all, and all +ve numbers show output.
     * @return the return value of the getter method.
     */
    public static Object invokeMethod_DbC( final Object obj
            ,final String staticMethodName
            ,final Class<?>[] parameterTypes
            ,final Object[] parameters
            ,final int verboseLevel )
    {
        if ( obj == null || staticMethodName == null )
            return null;

        final String HDR="invokeMethodOnStub("+obj.getClass().getName()+", "+staticMethodName+", parameters[], "+verboseLevel+") : ";

        Method method = null;
        boolean found = false;
        final Method[] mthds = obj.getClass().getDeclaredMethods();
        for( int ix=0;  ix < mthds.length; ix ++ ) {
            method = mthds[ix];
            if( verboseLevel >= 1 )
                System.out.println("\t'"+staticMethodName+"' =?= '"+method.getName()+"' :: "+method);
            if ( method.getName().equals(staticMethodName)) {
                final Class<?>[] prmtrTypes = method.getParameterTypes();
                if ( prmtrTypes.length != parameterTypes.length ) {
                    if( verboseLevel >= 1 )
                        System.out.println(HDR+"\tparameterTypes.length("+parameterTypes.length+") != prmtrTypes.length("+prmtrTypes.length+")'");
                    continue;
                }
                found = true; // changed to false on first mismatch.
                for(int iy=0;  iy < prmtrTypes.length; iy++ ) {
                    final Class<?> param = prmtrTypes[iy];
                    final String pcs = parameterTypes[iy].toString();
                    final String pcs2 = param.toString();
                    if ( verboseLevel >= 1 )
                        System.out.println(HDR+"Comparing types for Parameter #"+iy+" ("+param+"):\t'"+pcs+"' =?= '"+pcs2+"'");
                    if ( ! pcs2.replaceAll("  *","").equals(pcs.replaceAll("  *",""))) {
                        found = false;
                    }
                } // inner FOR loop over all parameters
            } // if method name found
            if ( found ) break;
        } // outer FOR loop over all methods.

        if ( ! found )
        {   System.err.println(HDR+"No method "+obj.getClass().getName()+"."
                +staticMethodName+"() found.");
            return null;
        }
        
        //-------------------
        return invokeMethod( obj, method, parameters );
        
    } // End invokeMethod()

    /**
     * Stop using this for Java1.6 and Higher -- use http://sourceforge.net/projects/privaccessor
     * 
     * Invoke  "obj.methodName(parameters)"
     * @param obj an instance of ANY class that has a method to be invoked
     * @param method a valid methodName [a-zA-Z-][0-9a-zA-Z-]+  (This method does not check whether this parameter has a valid methodName)
     * @param parameters - eg: new Class[] prms = new String[]{ "value" };
     * @return the return value of the getter method.
     */
    public static Object invokeMethod( final Object obj
            ,final Method method
            ,final Object[] parameters)
    {
        if ( obj == null || method == null )
            return null;

        final String HDR="invokeMethod("+obj.getClass().getName()+", "+method+", parameters[]) : ";
        
        Object retobj = null;
        try
        {
            // isAccessible is deprecated since Java 9
            // For Java 9 and 10.. use:-
            //      Class java.lang.reflect.AccessibleObject
            //      public final boolean canAccess​(Object obj)
			if ( ! method.isAccessible() ) {
				method.setAccessible(true); // This will throw the SecurityException if running in an appserver.
				// Since this is a generic method, we do NOT WANT to fail due to above invocation for public fields.
			}
            retobj = method.invoke( obj, parameters );

        }catch(IllegalAccessException e1){
            e1.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
            System.err.println( "\n\n"+ HDR +" method.invoke(...) IllegalAccessException: "+ e1 );
            return null;
        }catch(IllegalArgumentException e2){
            e2.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
            System.err.println( "\n\n"+ HDR +" method.invoke(...) IllegalArgumentException: "+ e2 );
            return null;
        }catch(InvocationTargetException e3){
            e3.printStackTrace(System.err);  // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
            System.err.println( "\n\n"+ HDR +" method.invoke(...) InvocationTargetException: "+ e3 );
            return null;
        }catch(SecurityException e4){
            e4.printStackTrace(System.err);  // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
            System.err.println( "\n\n"+ HDR +" method.invoke(...) SecurityException: "+ e4 );
            return null;
        }
        
        return retobj;
        
	} // End invokeMethod()

	/**
     * Stop using this for Java1.6 and Higher -- use http://sourceforge.net/projects/privaccessor
     * 
	 * Provides access to EVEN PRIVATE members in classes.
     * @param o an instance of ANY class that has a method to be invoked
     * @param fieldName a valid fieldname [a-zA-Z-][0-9a-zA-Z-]+  (This method does not check whether this parameter has a valid fieldname)
     * @return the return value of the field (whether public or private field).
	 */
	public static Object getAnyField (final Object o, final String fieldName) {
		if ( o == null || fieldName == null )
            return null;

        final String HDR="getAnyField("+o.getClass().getName()+", "+fieldName+") : ";

		try {
			final Field field = o.getClass().getDeclaredField(fieldName);
            // isAccessible is deprecated since Java 9
            // For Java 9 and 10.. use:-
            //      Class java.lang.reflect.AccessibleObject
            //      public final boolean canAccess​(Object obj)
			if ( ! field.isAccessible() ) {
				field.setAccessible(true); // This will throw the SecurityException if running in an AppServer.
				// Since this is a generic method, we do NOT WANT to fail due to above invocation for public fields.
			}
			return field.get(o);

		}catch(IllegalAccessException e1){
			e1.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
			System.err.println( "\n\n"+ HDR +" getAnyField(...) IllegalAccessException: "+ e1 );
			return null;
        }catch(IllegalArgumentException e2){
            e2.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
            System.err.println( "\n\n"+ HDR +" getAnyField(...) IllegalArgumentException: "+ e2 );
            return null;
		}catch(NoSuchFieldException e3){
			e3.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
			System.err.println( "\n\n"+ HDR +" getAnyField("+fieldName+") for class '"+ o.getClass().getName() +"' - NoSuchFieldException: "+ e3 );
			return null;
		}catch(SecurityException e4){
			e4.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
			System.err.println( "\n\n"+ HDR +" method.getAnyField(...) SecurityException: "+ e4 );
			return null;
        }catch(ExceptionInInitializerError e5){
            e5.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
            System.err.println( "\n\n"+ HDR +" getAnyField(...) ExceptionInInitializerError: "+ e5 );
            return null;
		}
    }

	/**
	 * Given a specific class, and a **STATIC** method of that class, it will invoke it.
	 * @param userClass the class.getName() that has a static method to invoke
     * @param methodName a valid methodName [a-zA-Z-][0-9a-zA-Z-]+  (This method does not check whether this parameter has a valid methodName)
	 * @param parameterTypes - the list of parameters TYPES.  Example: new Class[] prms = { String.class };
	 * @param parameters - the list of parameters.  Example: new Class[] prms = new String[]{ "value" };
	 * @return The value returned by the method.  Null return value means something went wrong, even the method invocation was successful without exceptions.
	 */
	public static Object invokeStaticMethod( 
			Class<?> userClass, final String methodName, final Class<?>[] parameterTypes, final Object[] parameters)
	{
        final String HDR= CLASSNAME +": invokeStaticMethod("+userClass.getName()+", "+methodName+"): ";
		Method method2 = null;
		try {
		    method2 = userClass.getDeclaredMethod( methodName, parameterTypes );
		    // First check to see if the method is a static method of the class...
		    if ( Modifier.isStatic(method2.getModifiers()) ) {
		    	return method2.invoke(null, parameters);
		    }else{
			    System.err.println( HDR+" STATIC-METHOD getDeclaredMethod("+ methodName +") for class '"+ userClass.getName() +"' does NOT exist " );
			    return null;
		    }
		}catch(NoSuchMethodException e){
            e.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
		    System.err.println( "\n\n"+ HDR +" STATIC-METHOD getDeclaredMethod("+ methodName +") for class '" + userClass.getName() +"' - NoSuchMethodException: "+ e );
		    return null;
		}catch(SecurityException e2){
            e2.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
		    System.err.println( "\n\n"+ HDR +" STATIC-METHOD getDeclaredMethod("+ methodName +") for class '"+ userClass.getName() +"' - SecurityException: "+ e2 );
		    return null;
        }catch(IllegalAccessException e3){
            e3.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
            System.err.println( "\n\n"+ HDR +" STATIC-METHOD getDeclaredMethod("+ methodName +") for class '"+ userClass.getName() +"' - IllegalAccessException: "+ e3 );
            return null;
        }catch(IllegalArgumentException e4){
            e4.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
            System.err.println( "\n\n"+ HDR +" STATIC-METHOD getDeclaredMethod("+ methodName +") for class '"+ userClass.getName() +"' - IllegalArgumentException: "+ e4 );
            return null;
        }catch(InvocationTargetException e5){
            e5.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
            System.err.println( "\n\n"+ HDR +" STATIC-METHOD getDeclaredMethod("+ methodName +") for class '"+ userClass.getName() +"' - InvocationTargetException: "+ e5 );
            return null;
		}
	}


	/**
	 * Given a specific class, and a **STATIC** method of that class, return the method, if it exists and is static (But no accessibility checks)
	 * @param userClass the class.getName() that has a static method to invoke
     * @param methodName a valid methodName [a-zA-Z-][0-9a-zA-Z-]+  (This method does not check whether this parameter has a valid methodName)
	 * @param parameterTypes - the list of parameters TYPES.  Example: new Class[] prms = { String.class };
	 * @return The method object itself, if it exists and is static (But no accessibility checks)
	 */
	public static Method getStaticMethod(  Class<?> userClass, final String methodName, final Class<?>[] parameterTypes )
	{
        final String HDR=CLASSNAME +": getStaticMethod("+userClass.getName()+", "+methodName+") : ";
		Method method3 = null;
		try{
		    method3 = userClass.getDeclaredMethod( methodName, parameterTypes );
		    // First check to see if the method is a static method of the class...
		    if ( Modifier.isStatic(method3.getModifiers()) )
		    	return method3;
		    else
		    	return null;
		}catch(NoSuchMethodException e2){
            e2.printStackTrace(System.err); // Static Method.  Can't see an immediate option to enable levels-of-verbosity for this java file.
		    System.err.println( "\n\n"+ HDR +" STATIC-METHOD getStaticMethod("+ methodName +") for class '"+ userClass.getName() +"' - NoSuchMethodException: "+ e2 );
		    return null;
		}
	}

}
