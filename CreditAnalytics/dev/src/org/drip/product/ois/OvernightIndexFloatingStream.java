
package org.drip.product.ois;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * OvernightIndexFloatingStream contains an implementation of the Floating leg cash flow stream backed by an
 * 	Overnight Stream.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class OvernightIndexFloatingStream extends org.drip.product.rates.FloatingStream {

	@Override protected double getFixing (
		final double dblValueDate,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.analytics.period.CashflowPeriod currentPeriod,
		final org.drip.param.market.MarketParamSet mktParams)
		throws java.lang.Exception
	{
		try {
			return super.getFixing (dblValueDate, org.drip.product.params.FloatingRateIndex.Create
				(fri.currency(), fri.index(), "1D"), currentPeriod, mktParams);
		} catch (java.lang.Exception e) {
		}

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mapFixings =
				mktParams.fixings();

		if (null == mapFixings || 0 == mapFixings.size())
			throw new java.lang.Exception
				("OvernightIndexFloatingStream::getCompoundedOvernightFixing => Cannot get Fixing");

		double dblPrevDate = currentPeriod.getStartDate();

		java.lang.String strCalendar = currentPeriod.calendar();

		java.lang.String strAccrualDC = currentPeriod.accrualDC();

		java.lang.String strFRIFullName = fri.fullyQualifiedName();

		double dblAccruedAmount = 0.;
		double dblDate = dblPrevDate + 1;
		double dblPeriodEndDate = dblValueDate;
		double dblLastCoupon = java.lang.Double.NaN;

		while (dblDate <= dblPeriodEndDate) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFRIFixing = mapFixings.get
				(new org.drip.analytics.date.JulianDate (dblDate));

			if (null != mapFRIFixing && mapFRIFixing.containsKey (strFRIFullName)) {
				java.lang.Double dblFixing = mapFRIFixing.get (strFRIFullName);

				if (null != dblFixing && org.drip.quant.common.NumberUtil.IsValid (dblFixing)) {
					dblAccruedAmount += org.drip.analytics.daycount.Convention.YearFraction (dblPrevDate,
						dblDate, strAccrualDC, false, java.lang.Double.NaN, null, strCalendar) *
							(dblLastCoupon = dblFixing);

					dblPrevDate = dblDate;
				}
			}

			++dblDate;
		}

		if (!org.drip.quant.common.NumberUtil.IsValid (dblLastCoupon))
			throw new java.lang.Exception
				("OvernightIndexFloatingStream::getCompoundedOvernightFixing => Cannot get Fixing");

		return (dblAccruedAmount + org.drip.analytics.daycount.Convention.YearFraction (dblPrevDate,
			dblPeriodEndDate, currentPeriod.accrualDC(), false, java.lang.Double.NaN, null, strCalendar) *
				dblLastCoupon) / org.drip.analytics.daycount.Convention.YearFraction
					(currentPeriod.getStartDate(), dblValueDate, strAccrualDC, false, java.lang.Double.NaN,
						null, strCalendar);
	}

	/**
	 * OvernightIndexFloatingStream constructor
	 * 
	 * @param strCurrency Cash Flow Currency
	 * @param dblSpread Spread
	 * @param dblNotional Initial Notional Amount
	 * @param notlSchedule Notional Schedule
	 * @param lsCouponPeriod List of the Coupon Periods
	 * @param fri Floating Rate Index
	 * @param bIsReference Is this the Reference Leg in a Float-Float Swap?
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public OvernightIndexFloatingStream (
		final java.lang.String strCurrency,
		final double dblSpread,
		final double dblNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod,
		final org.drip.product.params.FloatingRateIndex fri,
		final boolean bIsReference)
		throws java.lang.Exception
	{
		super (strCurrency, dblSpread, dblNotional, notlSchedule, lsCouponPeriod, fri, bIsReference);
	}
}
