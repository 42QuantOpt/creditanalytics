
package org.drip.product.params;

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
 * PeriodSet is the place-holder for the componentís period generation parameters. Contains the component's
 * 	date adjustment parameters for period start/end, period accrual start/end, effective, maturity, pay and
 * 	reset, first coupon date, and interest accrual start date. It exports serialization into and
 *  de-serialization out of byte arrays.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PeriodSet extends org.drip.service.stream.Serializer implements
	org.drip.product.params.Validatable {

	/**
	 * Coupon Frequency
	 */

	public int _iFreq = 2;

	/**
	 * Apply Coupon end-of-month adjustment
	 */

	public boolean _bApplyCpnEOMAdj = false;

	/**
	 * Coupon day count
	 */

	public java.lang.String _strCouponDC = "";

	/**
	 * Accrual day count
	 */

	public java.lang.String _strAccrualDC = "";

	/**
	 * Maturity Type
	 */

	public java.lang.String _strMaturityType = "";

	/**
	 * Maturity Date
	 */

	public double _dblMaturity = java.lang.Double.NaN;

	/**
	 * Effective Date
	 */

	public double _dblEffective = java.lang.Double.NaN;

	/**
	 * Final Maturity Date
	 */

	public double _dblFinalMaturity = java.lang.Double.NaN;

	protected java.util.List<org.drip.analytics.period.CouponPeriod> _lsCouponPeriod = null;

	/**
	 * Construct PeriodSet from the effective date, day count, frequency, and the list of coupon periods
	 * 
	 * @param dblEffective Effective Date
	 * @param strDC Day count
	 * @param iFreq Frequency
	 * @param lsCouponPeriod List of Coupon Period
	 */

	public PeriodSet (
		final double dblEffective,
		final java.lang.String strDC,
		final int iFreq,
		final java.util.List<org.drip.analytics.period.CouponPeriod> lsCouponPeriod)
	{
		_iFreq = iFreq;
		_strCouponDC = strDC;
		_strAccrualDC = strDC;
		_dblEffective = dblEffective;
		_lsCouponPeriod = lsCouponPeriod;
	}

	/**
	 * PeriodSet de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if PeriodSet cannot be properly de-serialized
	 */

	public PeriodSet (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("PeriodSet de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("PeriodSet de-serializer: Empty state");

		java.lang.String strSerializedPeriodSet = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedPeriodSet || strSerializedPeriodSet.isEmpty())
			throw new java.lang.Exception ("PeriodSet de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedPeriodSet,
			fieldDelimiter());

		if (null == astrField || 10 > astrField.length)
			throw new java.lang.Exception ("PeriodSet de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("PeriodSet de-serializer: Cannot locate frequency");

		_iFreq = new java.lang.Integer (astrField[1]).intValue();

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception
				("PeriodSet de-serializer: Cannot locate Coupon EOM Adj flag");

		_bApplyCpnEOMAdj = new java.lang.Boolean (astrField[2]).booleanValue();

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception ("PeriodSet de-serializer: Cannot locate Coupon DC");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			_strCouponDC = "";
		else
			_strCouponDC = astrField[3];

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception ("PeriodSet de-serializer: Cannot locate accrual DC");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			_strAccrualDC = "";
		else
			_strAccrualDC = astrField[4];

		if (null == astrField[5] || astrField[5].isEmpty())
			throw new java.lang.Exception
				("PeriodSet de-serializer: Cannot locate maturity type");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			_strMaturityType = "";
		else
			_strMaturityType = astrField[5];

		if (null == astrField[6] || astrField[6].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			throw new java.lang.Exception ("PeriodSet de-serializer: Cannot locate maturity");

		_dblMaturity = new java.lang.Double (astrField[6]).doubleValue();

		if (null == astrField[7] || astrField[7].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7]))
			throw new java.lang.Exception ("PeriodSet de-serializer: Cannot locate eff date");

		_dblEffective = new java.lang.Double (astrField[7]).doubleValue();

		if (null == astrField[8] || astrField[8].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[8]))
			throw new java.lang.Exception
				("PeriodSet de-serializer: Cannot locate final maturity");

		_dblFinalMaturity = new java.lang.Double (astrField[8]).doubleValue();

		if (null == astrField[9] || astrField[9].isEmpty())
			throw new java.lang.Exception ("PeriodSet de-serializer: Cannot locate the coupon periods");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[9]))
			_lsCouponPeriod = null;
		else {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[9],
				collectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty() ||
						org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrRecord[i]))
						continue;

					if (null == _lsCouponPeriod)
						_lsCouponPeriod = new java.util.ArrayList<org.drip.analytics.period.CouponPeriod>();

					_lsCouponPeriod.add (new org.drip.analytics.period.CouponPeriod
						(astrRecord[i].getBytes()));
				}
			}
		}
	}

	@Override public boolean validate()
	{
		if (null == _lsCouponPeriod || 0 == _lsCouponPeriod.size() ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEffective) || 0 == _iFreq)
			return false;

		for (org.drip.analytics.period.CouponPeriod fp : _lsCouponPeriod) {
			if (null == fp || !org.drip.quant.common.NumberUtil.IsValid (_dblMaturity = fp.endDate()))
				return false;
		}

		_dblFinalMaturity = _dblMaturity;
		return true;
	}

	/**
	 * Retrieve a list of the component's coupon periods
	 * 
	 * @return List of Coupon Period
	 */

	public java.util.List<org.drip.analytics.period.CouponPeriod> getPeriods()
	{
		return _lsCouponPeriod;
	}

	/**
	 * Return the first Coupon period
	 * 
	 * @return The first Coupon period
	 */

	public org.drip.analytics.period.CouponPeriod getFirstPeriod()
	{
		return _lsCouponPeriod.get (0);
	}

	/**
	 * Returns the final Coupon period
	 * 
	 * @return The final Coupon period
	 */

	public org.drip.analytics.period.CouponPeriod getLastPeriod()
	{
		return _lsCouponPeriod.get (_lsCouponPeriod.size() - 1);
	}

	/**
	 * Return the period index containing the specified date
	 * 
	 * @param dblDate Date input
	 * 
	 * @return Period index containing the date
	 * 
	 * @throws java.lang.Exception Thrown if the input date not in the period set range
	 */

	public int getPeriodIndex (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("PeriodSet::getPeriodIndex => Input date is NaN!");

		int i = 0;

		for (org.drip.analytics.period.CouponPeriod period : _lsCouponPeriod) {
			if (period.contains (dblDate)) return i;

			++i;
		}

		throw new java.lang.Exception
			("PeriodSet::getPeriodIndex => Input date not in the period set range!");
	}
	
	/**
	 * Retrieve the period corresponding to the given index
	 * 
	 * @param iIndex Period index
	 * 
	 * @return Period object corresponding to the input index
	 */

	public org.drip.analytics.period.CouponPeriod getPeriod (
		final int iIndex)
	{
		try {
			return _lsCouponPeriod.get (iIndex);
		} catch (java.lang.Exception e) {
		}

		return null;
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "[";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "{";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter() + _iFreq + fieldDelimiter()
			+ _bApplyCpnEOMAdj + fieldDelimiter() + _strCouponDC + fieldDelimiter() + _strAccrualDC +
				fieldDelimiter());

		if (null == _strMaturityType || _strMaturityType.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strMaturityType + fieldDelimiter());

		sb.append (_dblMaturity + fieldDelimiter() + _dblEffective + fieldDelimiter() + _dblFinalMaturity +
			fieldDelimiter());

		if (null == _lsCouponPeriod)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbPeriods = new java.lang.StringBuffer();

			for (org.drip.analytics.period.CouponPeriod p : _lsCouponPeriod) {
				if (null == p) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbPeriods.append (collectionRecordDelimiter());

				sbPeriods.append (new java.lang.String (p.serialize()));
			}

			if (sbPeriods.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
			else
				sb.append (sbPeriods.toString());
		}

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new PeriodSet (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		double dblStart = org.drip.analytics.date.JulianDate.Today().julian();

		java.util.List<org.drip.analytics.period.CouponPeriod> lsCouponPeriod = new
			java.util.ArrayList<org.drip.analytics.period.CouponPeriod>();

		int i = 5;

		while (0 != i--) {
			lsCouponPeriod.add (new org.drip.analytics.period.CouponPeriod (dblStart, dblStart + 180,
				dblStart, dblStart + 180, dblStart + 180, null, java.lang.Double.NaN, 2, 0.5, "30/360",
					"30/360", true, true, "GBP", 1., null, 0.05, "GBP", "GBP", null, null));

			dblStart += 180.;
		}

		PeriodSet bfpgp = new PeriodSet (1., "Act/360", 2, lsCouponPeriod);

		bfpgp.validate();

		byte[] abBFPGP = bfpgp.serialize();

		System.out.println (new java.lang.String (abBFPGP));

		PeriodSet bfpgpDeser = new PeriodSet (abBFPGP);

		System.out.println (new java.lang.String (bfpgpDeser.serialize()));
	}
}
