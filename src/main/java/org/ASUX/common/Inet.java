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

import java.util.ArrayList;

import static org.junit.Assert.*;

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
/**
 * This enum class is a bit extensive, only because the ENNUMERATED VALUEs are strings.
 * For variations - see https://stackoverflow.com/questions/3978654/best-way-to-create-enum-of-strings
 */
public final class Inet
{
    public static final String CLASSNAME = Inet.class.getName();

    public static final String CIDRBLOCK_BYTE3_DELTA = "CIDRBLOCK_Byte3_Delta";

    public static final String CIDRBLOCKELEMENTpattern = "([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
    public static final String CIDRBLOCKRANGEpattern = "([0-9]|[12][0-9]|3[0-2])";
    public static final String CIDRBLOCKpattern = "^" + CIDRBLOCKELEMENTpattern + "\\." + CIDRBLOCKELEMENTpattern
            + "\\." + CIDRBLOCKELEMENTpattern + "\\." + CIDRBLOCKELEMENTpattern + "/" + CIDRBLOCKRANGEpattern + "$";

    //-----------------------------
    public boolean verbose;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public Inet( final boolean _verbose ) {
        this.verbose = _verbose;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

	public static class InetAddress {
		public final int b1;
		public final int b2;
		public final int b3;
		public final int b4;
		public final int subnetMask;
		public InetAddress( final int _b1, final int _b2, final int _b3, final int _b4, final int _subnetMask ) {
			this.b1 = _b1;
			this.b2 = _b2;
			this.b3 = _b3;
			this.b4 = _b4;
			this.subnetMask = _subnetMask;
		}
		public String toString() { return ""+ b1 +"."+ b2 +"."+ b3 +"."+ b4 +"/"+ subnetMask;  }
	}

	protected static InetAddress singleton;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

	/**
	 *  <p>By leveraging A private INSTANCE of {@link Inet.InetAddress}, this class will sequentially generate subnets separated by '_CIDRBLOCK_Byte3_Delta'.</p>
	 *  <p>Example, if '_CIDRBLOCK_Byte3_Delta' === 16*256, and you invoked {@link #genSubnetMask} with '172.10.0.0/16' then you'll receive on successive invocations of this method:- </p>
	 *  <ul><li>172.31.0.0/20</li><li>172.31.16.0/20</li><li>172.31.32.0/20</li><li>172.31.48.0/20</li><li>..</li></ul>
	 *  <p>Please do Not misuse these methods and end-up screwing yourself.  Feel free to copy this entire file</p>	 * 
     *  @param _verbose  Whether you want deluge of debug-output onto System.out.
	 *  @param _CIDRBLOCK_Byte3_Delta a number like 16 (representing 16*256 ip-addresses)
	 *  @throws Exception if the argument passed in fails the Range-check: 0 &lt; _CIDRBLOCK_Byte3_Delta &lt;= 256
	 */
	public static final void getNextSubnetRange( final boolean _verbose, final int _CIDRBLOCK_Byte3_Delta ) throws Exception
	{	final String HDR = CLASSNAME + ": getNextSubnetRange("+ _CIDRBLOCK_Byte3_Delta +"): ";
		if ( 1 > _CIDRBLOCK_Byte3_Delta && _CIDRBLOCK_Byte3_Delta > 256 )
			throw new Exception( " Failed Range-check: 0 < _CIDRBLOCK_Byte3_Delta("+ _CIDRBLOCK_Byte3_Delta +") <= 256" );
		int newb2 = Inet.singleton.b2;
		int newb3 = Inet.singleton.b3 + _CIDRBLOCK_Byte3_Delta;
		if ( newb3 > 255 ) {
			newb2 += Math.floorDiv( newb3, 256 );
			newb3  = Math.floorMod( newb3, 256 );
		}
		Inet.singleton = new InetAddress( Inet.singleton.b1, newb2, newb3, Inet.singleton.b4, Inet.singleton.subnetMask );
		if ( _verbose ) System.out.println( HDR + Inet.singleton.toString() );
	}

	//=================================================================================
	/**
	 *  Given the a VPC CIDRBlock like 172.10.0.0/16.. '16' is the argument to this function.  Assuming about 16 subnets, this method will return /20 (or as appropriate for what the _cidrBlockRange is)
     *  @param _verbose  Whether you want deluge of debug-output onto System.out.
	 *  @param _cidrBlockRange a value between 1-32 (inclusive of both limits)
	 *  @return an integer in the range 0 &lt;= .. &lt;= 32 (inclusive of both limits)
	 *  @throws Exception if the argument passed in fails the Range-check: 0 &lt; cidrBlockRange &lt;= 256
	 */
	public static final int genSubnetMask( final boolean _verbose, final int _cidrBlockRange ) throws Exception
	{	final String HDR = CLASSNAME + ": genSubnetMask("+ _cidrBlockRange +"): ";
		if ( 1 > _cidrBlockRange && _cidrBlockRange > 32 )
			throw new Exception( " Failed Range-check: 0 < cidrBlockRange("+ _cidrBlockRange +") <= 32" );

		int iy = 1;
		while( Math.pow(2,iy)< _cidrBlockRange ) {
			if ( _verbose ) System.out.println( HDR + "Math.pow(2^iy) = "+ Math.pow(2,iy)  + ", cidrBlockRange = "+ _cidrBlockRange );
			iy ++;
		}
		final int subnetMask = ( 32 - 8 - iy ); // assumption that last/4th byte of CIDRBlock (a.k.a. right-most 8 bits) Not in subnet-mask.
		if ( _verbose ) System.out.println( HDR + "subnetMask = "+ subnetMask );
		assertTrue( 0 <= subnetMask && subnetMask <= 32 );
		return subnetMask;
	}

    //=================================================================================
	/** Convert the singleton into an integer, by putting the bytes together.
	 *  @return an integer that could be a negative number.  Be careful!
	*/
	public static final int getInetAddressAsInteger() {
		final int inetInt =   Inet.singleton.b1*256*256*256
							+ Inet.singleton.b2*256*256
							+ Inet.singleton.b3*256
							+ Inet.singleton.b4;
		// if ( this.verbose ) System.out.println( HDR + "inetInt = "+ inetInt );
		return inetInt;
	}

	//=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public ArrayList<String> genSubnetRangeWithMasks( final String vpcCidrBlk, final int numOfAZs, final int _CIDRBLOCK_Byte3_Delta ) throws Exception
    {
		final String HDR = CLASSNAME + ": genSubnetRangeWithMasks("+ vpcCidrBlk +"): ";
		final ArrayList<String> retval = new ArrayList<String>();

		final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile( CIDRBLOCKpattern );
		java.util.regex.Matcher matcher    = pattern.matcher( vpcCidrBlk );
		if ( ! matcher.find()) {
			throw new Exception( "Invalid vpcCidrBlk: '"+ vpcCidrBlk + "' provided within one of the many Properties-files." );
		} else {
			if ( this.verbose ) System.out.println( ": I found the text "+ matcher.group() +" starting at index "+  matcher.start() +" and ending at index "+ matcher.end() );    
			final String s1 = matcher.group(1); // line.substring( matcher.start(), matcher.end() );
			final String s2 = matcher.group(2); // line.substring( matcher.start(), matcher.end() );
			final String s3 = matcher.group(3); // line.substring( matcher.start(), matcher.end() );
			final String s4 = matcher.group(4); // line.substring( matcher.start(), matcher.end() );
			final String s5 = matcher.group(5); // line.substring( matcher.start(), matcher.end() );
			if ( this.verbose ) System.out.println( "\t s1=[" + s1 +"]\t s2=[" + s2 +"]\t s3=[" + s3 +"]\t s4=[" + s4 +"]\t/\ts5=[" + s5 +"]" );

			try {
				// final int b1 = Integer.parseInt(s1);
				// final int b2 = Integer.parseInt(s2);
				// final int b3 = Integer.parseInt(s3);
				// final int b4 = Integer.parseInt(s4);
				final int cidrBlockRange = Integer.parseInt(s5);
				if ( this.verbose ) System.out.println( HDR + "cidrBlockRange = "+ cidrBlockRange );

				if ( Inet.singleton == null ) {
					final int subnetMask = Inet.genSubnetMask( this.verbose, cidrBlockRange );
					Inet.singleton = new InetAddress( Integer.parseInt(s1), Integer.parseInt(s2), Integer.parseInt(s3), Integer.parseInt(s4), subnetMask );
				} else {
					final int subnetMask = Inet.genSubnetMask( this.verbose, cidrBlockRange );
					if ( Inet.singleton.subnetMask != subnetMask )
						throw new Exception( "Currently unable to handle changing value of subnetmask (basically changing value of 'cidrBlockRange')" );
				}

				// int iy = 1;
				// while( Math.pow(2,iy)<cidrBlockRange ) {
				// 	if ( this.verbose ) System.out.println( HDR + "Math.pow(2^iy) = "+ Math.pow(2,iy)  + ", cidrBlockRange = "+ cidrBlockRange );
				// 	iy ++;
				// }
				// final int subnetMask = ( 32 - 8 - iy ); // assumption that last/4th byte of CIDRBlock (a.k.a. right-most 8 bits) Not in subnet-mask.
				// if ( this.verbose ) System.out.println( HDR + "iy = "+ iy + ", subnetMask = "+ subnetMask );

				// int newb2 = b2;
				// int newb3 = b3;

				for ( int ix=1; ix <= numOfAZs; ix ++ ) {
					// final String subnet = ""+ Inet.singleton.b1 +"."+ Inet.singleton.b2 +"."+ Inet.singleton.b3 +"."+ Inet.singleton.b4 +"/"+ subnetMask;
					if ( this.verbose ) System.out.println( HDR + "subnet-"+ix+" = "+ Inet.singleton.toString() );

					retval.add( Inet.singleton.toString() );

					Inet.getNextSubnetRange( this.verbose, _CIDRBLOCK_Byte3_Delta );
					// newb3 += _CIDRBLOCK_Byte3_Delta;
					// if ( newb3 > 255 ) {
					// 	newb2 += Math.floorDiv( newb3, 256 );
					// 	newb3  = Math.floorMod( newb3, 256 );
					// }
				} // for numOfAZs

				return retval;

			} catch( IllegalStateException e ) {
				e.printStackTrace( System.err );
				System.err.println("FAILURE!!! SERIOUS INTERNAL LOGIC FAILURE: Shouldn't be happening for '"+ vpcCidrBlk + "'" );
				throw e;
			} catch( IndexOutOfBoundsException e ) {
				e.printStackTrace( System.err );
				System.err.println("FAILURE!!! SERIOUS INTERNAL LOGIC FAILURE: Shouldn't be happening for '"+ vpcCidrBlk + "'" );
				throw e;
			} catch( NumberFormatException e ) {
				e.printStackTrace( System.err );
				System.err.println("FAILURE!!! SERIOUS INTERNAL LOGIC FAILURE: Shouldn't be happening for '"+ vpcCidrBlk + "'" );
				throw e;
			}
		}
	}

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

	/**
	 * 
	 * @param _portnum must be one of 22, 80, 443, 8080, 1194, 1433, 1521, 3306, 5432, 5439 (as of now)
	 * @return a NotNull __LOWERCASE__ only string
	 * @throws Exception if _portnum is NOT among the values listed above,  or .. is invalid.
	 */
	public String getNameForPortNumber( final int _portnum ) throws Exception {
		if (_portnum == 22) return "ssh";
		if (_portnum == 80) return "http";
		if (_portnum == 443) return "https";
		if (_portnum == 8080) return "https";
		if (_portnum == 1194) return "openvpn-udp";
		if (_portnum == 1433) return "oracle";
		if (_portnum == 1521) return "ms-sql";
		if (_portnum == 3306) return "mysql";
		if (_portnum == 5432) return "postgres";
		if (_portnum == 5439) return "redshift";

		final String s = "Unknown port-# '"+ _portnum +"'";
		throw new Exception( s );
	}

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

	/**
	 *  Converts '22' into 'ssh', '443' into https, etc.. for a few well known labels/names for ports
	 *  @param _portName NotNull __LOWERCASE__ only string, with values ssh, http, https, openvpn-udp , oracle, ms-sql, mysql, postgres, redshift (only these values currently).<br>WARNING!!!! If the input is ALL Numeric-Positive-Integer, it will be simply returned AS-IS!!
	 *  @return a valid integer or else throws exception if _portName is unknown (which could include "invalid")<br>WARNING!!!! If the input is ALL Numeric-Positive-Integer, it will be simply returned AS-IS!!
	 *  @throws Exception if _portName is NOT among the values listed above,  or .. is invalid.
	 */
	public int getPortFromName( final String _portName ) throws Exception {
		final String s = "Unknown port-name '"+ _portName +"'";
		if ( _portName == null ) throw new Exception( s );

		if ("ssh".equals(_portName)) return 22;
		if ("http".equals(_portName)) return 80;
		if ("https".equals(_portName)) return 443;
		if ("openvpn-udp".equals(_portName)) return 1194;
		if ("oracle".equals(_portName)) return 1433;
		if ("ms-sql".equals(_portName)) return 1521;
		if ("mysql".equals(_portName)) return 3306;
		if ("postgres".equals(_portName)) return 5432;
		if ("redshift".equals(_portName)) return 5439;

		// in case the input is numeric.. just simply return it back!
		if ( _portName.matches("^\\s*[0-9]+\\s*$") ) return Integer.parseInt( _portName ); // THis line of code .. should NOT THROW!!!

		throw new Exception( s );
	}

	//=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
