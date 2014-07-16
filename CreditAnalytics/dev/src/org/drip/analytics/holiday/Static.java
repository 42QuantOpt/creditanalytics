
package org.drip.analytics.holiday;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
 * 
 *  This file is part of DRIP, a free-software/open-source library for fixed income analysts and developers -
 * 		http://www.credit-trader.org/Begin.html
 * 
 *  DRIP is a free, full featured, fixed income rates, credit, and FX analytics library with a focus towards
 *  	pricing/valuation, risk, and market making.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   	you may not use this file except in compliance with the License.
 *   
 *  You may obtain a copy of the License at
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  	distributed under the License is distributed on an "AS IS" BASIS,
 *  	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 *  See the License for the specific language governing permissions and
 *  	limitations under the License.
 */

/**
 * Static implements a complete date as a specific holiday.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class Static extends Base {
	private double _dblDate = java.lang.Double.NaN;

	/**
	 * Create a static holiday from the date string and the description
	 * 
	 * @param strDate Date string
	 * @param strDescription Description
	 * 
	 * @return StaticHoliday instance
	 */

	public static final Static CreateFromDateDescription (
		final java.lang.String strDate,
		final java.lang.String strDescription)
	{
		org.drip.analytics.date.JulianDate dtHol = org.drip.analytics.date.JulianDate.CreateFromDDMMMYYYY
			(strDate);

		if (null == dtHol) return null;

		try {
			return new Static (dtHol, strDescription);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct a static holiday from the date and the description
	 * 
	 * @param dt Date
	 * @param strDescription Description
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public Static (
		final org.drip.analytics.date.JulianDate dt,
		final java.lang.String strDescription)
		throws java.lang.Exception
	{
		super (strDescription);

		if (null == dt) throw new java.lang.Exception ("Static ctr: Null date into Static Holiday");

		_dblDate = dt.julian();
	}

	/**
	 * De-serialization of StaticHoliday from byte stream
	 * 
	 * @param ab Byte stream
	 * 
	 * @throws java.lang.Exception Thrown if cannot properly de-serialize StaticHoliday
	 */

	public Static (
		final byte[] ab)
		throws java.lang.Exception
	{
		super (ab);

		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("Static de-serialize: Invalid byte stream input");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("Static de-serializer: Empty state");

		java.lang.String strFH = strRawString.substring (0, strRawString.indexOf (objectTrailer()));

		if (null == strFH || strFH.isEmpty())
			throw new java.lang.Exception ("Static de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strFH, fieldDelimiter());

		if (null == astrField || 2 > astrField.length)
			throw new java.lang.Exception ("Static de-serialize: Invalid number of fields");

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("Static de-serializer: Cannot locate static holiday date");

		_dblDate = new java.lang.Double (astrField[1]);
	}

	@Override public double getDateInYear (
		final int iYear,
		final boolean bAdjusted)
	{
		return _dblDate;
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "#";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "^";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (new java.lang.String (super.serialize()) + fieldDelimiter() + _dblDate);

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new Static (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		Static sh = Static.CreateFromDateDescription ("12-JUN-2020", "Are you kidding me?");

		byte[] abSH = sh.serialize();

		System.out.println ("Input: " + new java.lang.String (abSH));

		Static shDeser = new Static (abSH);

		System.out.println ("Output: " + new java.lang.String (shDeser.serialize()));
	}
}
