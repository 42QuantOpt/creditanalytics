
package org.drip.state.curve;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * This class implements the zero rate curve. It exports the following functionality:
 *  - Calculate discount factor / discount factor Jacobian
 *  - Calculate zero/forward rate / zero/forward rate Jacobian
 *  - Construct tweaked curve instances (parallel/tenor/custom tweaks)
 *  - Optionally provide the calibration instruments, the quotes, the base discount curve, and the zero bump
 *  	used to build the curve.
 *
 * @author Lakshmi Krishnamurthy
 */

public class DerivedZeroRate extends org.drip.analytics.definition.ZeroCurve {
	private org.drip.analytics.definition.DiscountCurve _dc = null;

	private java.util.Map<org.drip.analytics.date.JulianDate, java.lang.Double> _mapDF = new
		java.util.TreeMap<org.drip.analytics.date.JulianDate, java.lang.Double>();

	private java.util.Map<org.drip.analytics.date.JulianDate, java.lang.Double> _mapZeroRate = new
		java.util.TreeMap<org.drip.analytics.date.JulianDate, java.lang.Double>();

	private java.util.Map<org.drip.analytics.date.JulianDate, java.lang.Double> _mapYearFraction = new
		java.util.TreeMap<org.drip.analytics.date.JulianDate, java.lang.Double>();

	private void updateMapEntries (
		final double dblDate,
		final int iFreq,
		final java.lang.String strDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strCalendar,
		final double dblZCBump)
		throws java.lang.Exception
	{
		double dblYearFraction = org.drip.analytics.daycount.Convention.YearFraction (epoch().getJulian(),
			dblDate, strDC, bApplyCpnEOMAdj, dblDate, null, strCalendar);

		if (!org.drip.math.common.NumberUtil.IsValid (dblYearFraction) || 0. > dblYearFraction) return;

		org.drip.analytics.date.JulianDate dt = new org.drip.analytics.date.JulianDate (dblDate);

		if (0. == dblYearFraction) {
			_mapDF.put (dt, 1.);

			_mapYearFraction.put (dt, 0.);

			_mapZeroRate.put (dt, 0.);

			return;
		}

		double dblBumpedZeroRate = org.drip.analytics.support.AnalyticsHelper.DF2Yield (iFreq, _dc.df
			(dblDate), dblYearFraction) + dblZCBump;

		_mapDF.put (dt, org.drip.analytics.support.AnalyticsHelper.Yield2DF (iFreq, dblBumpedZeroRate,
			dblYearFraction));

		_mapYearFraction.put (dt, dblYearFraction);

		_mapZeroRate.put (dt, dblBumpedZeroRate);
	}

	/**
	 * DerivedZeroRate constructor from period, work-out, settle, and quoting parameters
	 * 
	 * @param iFreqZC Zero Curve Frequency
	 * @param strDCZC Zero Curve Day Count
	 * @param strCalendarZC Zero Curve Calendar
	 * @param bApplyEOMAdjZC Zero Coupon EOM Adjustment Flag
	 * @param lsCouponPeriod List of bond coupon periods
	 * @param dblWorkoutDate Work-out date
	 * @param dblCashPayDate Cash-Pay Date
	 * @param dc Discount Curve
	 * @param quotingParams Quoting Parameters
	 * @param dblZCBump DC Bump
	 * 
	 * @throws java.lang.Exception
	 */

	public DerivedZeroRate (
		final int iFreqZC,
		final java.lang.String strDCZC,
		final java.lang.String strCalendarZC,
		final boolean bApplyEOMAdjZC,
		final java.util.List<org.drip.analytics.period.CouponPeriod> lsCouponPeriod,
		final double dblWorkoutDate,
		final double dblCashPayDate,
		final org.drip.analytics.definition.DiscountCurve dc,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final double dblZCBump)
		throws java.lang.Exception
	{
		super (dc.epoch().getJulian(), dc.currency());

		if (null == (_dc = dc) || null == lsCouponPeriod || 0 == lsCouponPeriod.size() ||
			!org.drip.math.common.NumberUtil.IsValid (dblWorkoutDate) ||
				!org.drip.math.common.NumberUtil.IsValid (dblCashPayDate) ||
					!org.drip.math.common.NumberUtil.IsValid (dblZCBump))
			throw new java.lang.Exception ("DerivedZeroRate ctr => Invalid date parameters!");

		int iFreq = 0 == iFreqZC ? 2 : iFreqZC;
		boolean bApplyCpnEOMAdj = bApplyEOMAdjZC;
		java.lang.String strCalendar = strCalendarZC;

		java.lang.String strDC = null == strDCZC || strDCZC.isEmpty() ? "30/360" : strDCZC;

		if (null != quotingParams) {
			strDC = quotingParams._strYieldDC;
			iFreq = quotingParams._iYieldFrequency;
			strCalendar = quotingParams._strYieldCalendar;
			bApplyCpnEOMAdj = quotingParams._bYieldApplyEOMAdj;
		}

		for (org.drip.analytics.period.CouponPeriod period : lsCouponPeriod)
			updateMapEntries (period.getPayDate(), iFreq, strDC, bApplyCpnEOMAdj, strCalendar, dblZCBump);

		updateMapEntries (dblWorkoutDate, iFreq, strDC, bApplyCpnEOMAdj, strCalendar, dblZCBump);

		updateMapEntries (dblCashPayDate, iFreq, strDC, bApplyCpnEOMAdj, strCalendar, dblZCBump);
	}

	/**
	 * DerivedZeroRate de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if DerivedZeroRate cannot be properly de-serialized
	 */

	public DerivedZeroRate (
		final byte[] ab)
		throws java.lang.Exception
	{
		super (org.drip.analytics.date.JulianDate.Today().getJulian(), "DEFINIT");

		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("DerivedZeroRate de-serializer: Invalid input Byte array");

		_dc = org.drip.state.creator.DiscountCurveBuilder.FromByteArray (ab,
			org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);
	}

	@Override public double df (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DerivedZeroCurve::df => got NaN for date");

		if (dblDate <= epoch().getJulian()) return 1.;

		java.lang.Double objDF = _mapDF.get (new org.drip.analytics.date.JulianDate (dblDate));

		if (null == objDF)
			throw new java.lang.Exception ("DerivedZeroCurve::df => No DF found for date " + new
				org.drip.analytics.date.JulianDate (dblDate));

		return objDF;
	}

	@Override public org.drip.math.calculus.WengertJacobian dfJack (
		final double dblDate)
	{
		try {
			if (!org.drip.math.common.NumberUtil.IsValid (dblDate) || null == _mapDF.get (new
				org.drip.analytics.date.JulianDate (dblDate)))
				return null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return _dc.dfJack (dblDate);
	}

	@Override public double getZeroRate (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DerivedZeroCurve::getZeroRate => Invalid Date");

		if (dblDate <= epoch().getJulian()) return 1.;

		java.lang.Double objZeroRate = _mapZeroRate.get (new org.drip.analytics.date.JulianDate (dblDate));

		if (null == objZeroRate)
			throw new java.lang.Exception ("DerivedZeroCurve::getZeroRate => No Zero Rate found for date " +
				new org.drip.analytics.date.JulianDate (dblDate));

		return objZeroRate;
	}

	@Override public org.drip.state.representation.LatentStateMetricMeasure[] lsmm()
	{
		return _dc.lsmm();
	}

	@Override public double manifestMeasure (
		final java.lang.String strInstr)
		throws java.lang.Exception {
		return _dc.manifestMeasure (strInstr);
	}

	@Override public org.drip.product.definition.CalibratableComponent[] calibComp()
	{
		return _dc.calibComp();
	}

	@Override public java.lang.String name() {
		return _dc.name();
	}

	@Override public org.drip.analytics.definition.Curve parallelShiftManifestMeasure (
		final double dblShift) {
		return null;
	}

	@Override public org.drip.analytics.definition.Curve shiftManifestMeasure (
		final int iSpanIndex,
		final double dblShift)
	{
		return null;
	}

	@Override public org.drip.analytics.definition.Curve customTweakManifestMeasure (
		final org.drip.param.definition.ResponseValueTweakParams mmtp)
	{
		return null;
	}

	@Override public org.drip.analytics.date.JulianDate epoch()
	{
		return _dc.epoch();
	}

	@Override public boolean setCCIS (
		final org.drip.analytics.definition.CurveConstructionInputSet ccis)
	{
		 return _dc.setCCIS (ccis);
	}

	@Override public org.drip.analytics.definition.DiscountCurve parallelShiftQuantificationMetric (
		final double dblShift)
	{
		return (org.drip.analytics.definition.DiscountCurve) _dc.parallelShiftQuantificationMetric
			(dblShift);
	}

	@Override public org.drip.analytics.definition.Curve customTweakQuantificationMetric (
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		return (org.drip.analytics.definition.DiscountCurve) _dc.customTweakQuantificationMetric
			(rvtp);
	}

	@Override public java.lang.String currency()
	{
		return _dc.currency();
	}

	@Override public double df (
		final org.drip.analytics.date.JulianDate dt)
		throws java.lang.Exception
	{
		return _dc.df (dt);
	}

	@Override public double df (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		return _dc.df (strTenor);
	}

	@Override public double effectiveDF (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		return _dc.effectiveDF (dblDate1, dblDate2);
	}

	@Override public double effectiveDF (
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2)
		throws java.lang.Exception
	{
		return _dc.effectiveDF (dt1, dt2);
	}

	@Override public double effectiveDF (
		final java.lang.String strTenor1,
		final java.lang.String strTenor2)
		throws java.lang.Exception
	{
		return _dc.effectiveDF (strTenor1, strTenor2);
	}

	@Override public double forward (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		return _dc.forward (dblDate1, dblDate2);
	}

	@Override public double forward (
		final java.lang.String strTenor1,
		final java.lang.String strTenor2)
		throws java.lang.Exception
	{
		return _dc.forward (strTenor1, strTenor2);
	}

	@Override public double zero (
		final double dblDate)
		throws java.lang.Exception
	{
		return _dc.zero (dblDate);
	}

	@Override public double zero (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		return _dc.zero (strTenor);
	}

	@Override public byte[] serialize()
	{
		return _dc.serialize();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		return _dc.deserialize (ab);
	}
}
