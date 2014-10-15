
package org.drip.analytics.cashflow;

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
 * CouponPeriod extends the period class with the cash-flow specific fields. It exposes the following
 * 	functionality:
 * 
 * 	- Frequency, reset date, and accrual day-count convention
 * 	- Static methods to construct cash-flow period sets starting backwards/forwards, generate single period
 * 	 sets, as well as merge cash-flow periods.
 *
 * @author Lakshmi Krishnamurthy
 */

public class GenericCouponPeriod implements java.lang.Comparable<GenericCouponPeriod> {
	class CalibInput {
		double _dblFloatSpread = java.lang.Double.NaN;
		double _dblFixedCoupon = java.lang.Double.NaN;
		double _dblFixedCouponBasis = java.lang.Double.NaN;

		double fullCoupon (
			final org.drip.analytics.output.GenericCouponPeriodMetrics cpm)
		{
			return null == _forwardLabel ? _dblFixedCoupon + _dblFixedCouponBasis :
				cpm.compoundedAccrualRate() + _dblFloatSpread;
		}
	}

	private CalibInput calibInput (
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		CalibInput ci = new CalibInput();

		ci._dblFixedCouponBasis = 0.;
		ci._dblFixedCoupon = _dblFixedCoupon;
		ci._dblFloatSpread = _dblFloatSpread;

		if (null == pqs) return ci;

		if (null == _forwardLabel) {
			if (pqs instanceof org.drip.product.calib.FixedStreamQuoteSet) {
				org.drip.product.calib.FixedStreamQuoteSet fsqs =
					(org.drip.product.calib.FixedStreamQuoteSet) pqs;

				try {
					if (fsqs.containsCoupon()) ci._dblFixedCoupon = fsqs.coupon();

					if (fsqs.containsCouponBasis()) ci._dblFixedCouponBasis = fsqs.couponBasis();
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}
		} else {
			if (pqs instanceof org.drip.product.calib.FloatingStreamQuoteSet) {
				org.drip.product.calib.FloatingStreamQuoteSet fsqs =
					(org.drip.product.calib.FloatingStreamQuoteSet) pqs;

				try {
					if (fsqs.containsSpread()) ci._dblFloatSpread = fsqs.spread();
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}
		}

		return ci;
	}

	/*
	 * Period Date Fields
	 */

	private double _dblEndDate = java.lang.Double.NaN;
	private double _dblPayDate = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;
	private double _dblFXFixingDate = java.lang.Double.NaN;
	private double _dblAccrualEndDate = java.lang.Double.NaN;
	private double _dblAccrualStartDate = java.lang.Double.NaN;
	private org.drip.analytics.cashflow.ResetPeriodContainer _rpc = null;

	/*
	 * Period Date Generation Fields
	 */

	private int _iFreq = 2;
	private boolean _bApplyAccEOMAdj = false;
	private boolean _bApplyCpnEOMAdj = false;
	private java.lang.String _strCalendar = "";
	private double _dblDCF = java.lang.Double.NaN;
	private java.lang.String _strCouponDC = "30/360";
	private java.lang.String _strAccrualDC = "30/360";

	/*
	 * Period Latent State Identification Support Fields
	 */

	private java.lang.String _strPayCurrency = "";
	private java.lang.String _strCouponCurrency = "";
	private org.drip.state.identifier.CreditLabel _creditLabel = null;
	private org.drip.state.identifier.ForwardLabel _forwardLabel = null;

	/*
	 * Period Cash Extensive Fields
	 */

	private double _dblFixedCoupon = java.lang.Double.NaN;
	private double _dblFloatSpread = java.lang.Double.NaN;
	private double _dblBaseNotional = java.lang.Double.NaN;
	private org.drip.product.params.FactorSchedule _notlSchedule = null;

	private double calibAccrued (
		final CalibInput ci,
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		if (!contains (dblValueDate)) return 0.;

		org.drip.analytics.output.GenericCouponAccrualMetrics cam = accrualMetrics (dblValueDate, csqs);

		if (null == cam) return 0.;

		return 10000. * cam.accrual01() * (null == _forwardLabel ? ci._dblFixedCoupon +
			ci._dblFixedCouponBasis : cam.compoundedAccrualRate() + ci._dblFloatSpread);
	}

	private double resetPeriodRate (
		final org.drip.analytics.cashflow.GenericComposablePeriod rp,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		if (null == csqs) return java.lang.Double.NaN;

		double dblFixingDate = rp.fixing();

		if (csqs.available (dblFixingDate, _forwardLabel))
			return csqs.getFixing (dblFixingDate, _forwardLabel);

		double dblResetEndDate = rp.end();

		org.drip.analytics.rates.ForwardRateEstimator fc = csqs.forwardCurve (_forwardLabel);

		if (null != fc) return fc.forward (dblResetEndDate);

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		if (null == dcFunding)
			throw new java.lang.Exception ("CouponPeriod::resetPeriodRate => Cannot locate Discount Curve");

		double dblResetStartDate = rp.start();

		double dblEpochDate = dcFunding.epoch().julian();

		if (dblEpochDate > dblResetStartDate)
			dblResetEndDate = new org.drip.analytics.date.JulianDate (dblResetStartDate =
				dblEpochDate).addTenor (_forwardLabel.tenor()).julian();

		return dcFunding.libor (dblResetStartDate, dblResetEndDate,
			org.drip.analytics.daycount.Convention.YearFraction (dblResetStartDate, dblResetEndDate,
				_strAccrualDC, _bApplyAccEOMAdj, null, _strCalendar));
	}

	private org.drip.analytics.output.ConvexityAdjustment calcConvexityAdjustment (
		final double dblValueDate,
		final double dblFixingDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		org.drip.state.identifier.CreditLabel creditLabel = creditLabel();

		org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel();

		org.drip.state.identifier.FundingLabel fundingLabel = fundingLabel();

		org.drip.state.identifier.FXLabel fxLabel = fxLabel();

		org.drip.analytics.output.ConvexityAdjustment convAdj = new
			org.drip.analytics.output.ConvexityAdjustment();

		try {
			if (!convAdj.setCreditForward (null != csqs && dblFixingDate > dblValueDate ? java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (csqs.creditCurveVolSurface
					(creditLabel), csqs.forwardCurveVolSurface (forwardLabel), csqs.creditForwardCorrSurface
						(creditLabel, forwardLabel), dblValueDate, dblFixingDate)) : 1.))
				return null;

			if (!convAdj.setCreditFunding (null != csqs ? java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (csqs.creditCurveVolSurface
					(creditLabel), csqs.fundingCurveVolSurface (fundingLabel), csqs.creditFundingCorrSurface
						(creditLabel, fundingLabel), dblValueDate, _dblPayDate)) : 1.))
				return null;

			if (!convAdj.setCreditFX (null != csqs && isFXMTM() ? java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (csqs.creditCurveVolSurface
					(creditLabel), csqs.fxCurveVolSurface (fxLabel), csqs.creditFXCorrSurface (creditLabel,
						fxLabel), dblValueDate, _dblPayDate)) : 1.))
				return null;

			if (!convAdj.setForwardFunding (null != csqs && dblFixingDate > dblValueDate ? java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
					(csqs.forwardCurveVolSurface (forwardLabel), csqs.fundingCurveVolSurface
						(fundingLabel), csqs.forwardFundingCorrSurface (forwardLabel, fundingLabel),
							dblValueDate, dblFixingDate)) : 1.))
				return null;

			if (!convAdj.setForwardFX (null != csqs && isFXMTM() && dblFixingDate > dblValueDate ?
				java.lang.Math.exp (org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
					(csqs.forwardCurveVolSurface (forwardLabel), csqs.fxCurveVolSurface (fxLabel),
						csqs.forwardFXCorrSurface (forwardLabel, fxLabel), dblValueDate, dblFixingDate)) :
							1.))
				return null;

			if (!convAdj.setFundingFX (null != csqs && isFXMTM() ? java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
					(csqs.fundingCurveVolSurface (fundingLabel), csqs.fxCurveVolSurface (fxLabel),
						csqs.fundingFXCorrSurface (fundingLabel, fxLabel), dblValueDate, _dblPayDate)) : 1.))
				return null;

			return convAdj;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private org.drip.analytics.output.ResetPeriodMetrics resetPeriodMetrics (
		final org.drip.analytics.cashflow.GenericComposablePeriod rp,
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		double dblResetPeriodStartDate = rp.start();

		double dblResetPeriodEndDate = rp.end();

		double dblResetPeriodFixingDate = rp.end();

		try {
			org.drip.analytics.output.ResetPeriodMetrics rpm = new
				org.drip.analytics.output.ResetPeriodMetrics (dblResetPeriodStartDate, dblResetPeriodEndDate,
					dblResetPeriodFixingDate, resetPeriodRate (rp, csqs) + _dblFloatSpread,
						org.drip.analytics.daycount.Convention.YearFraction (dblResetPeriodStartDate,
							dblResetPeriodEndDate, _strCouponDC, _bApplyAccEOMAdj, null, _strCalendar));

			if (org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				_rpc.accrualCompoundingRule() && !rpm.setConvAdj (calcConvexityAdjustment (dblValueDate,
					dblResetPeriodFixingDate, csqs)))
				return null;

			return rpm;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct a CouponPeriod instance from the specified dates
	 * 
	 * @param dblStartDate Period Start Date
	 * @param dblEndDate Period End Date
	 * @param dblAccrualStartDate Period Accrual Start Date
	 * @param dblAccrualEndDate Period Accrual End Date
	 * @param dblPayDate Period Pay Date
	 * @param rpc Reset Period Container
	 * @param dblFXFixingDate The FX Fixing Date for non-MTM'ed Cash-flow
	 * @param iFreq Frequency
	 * @param dblDCF Full Period Day Count Fraction
	 * @param strCouponDC Coupon day count
	 * @param strAccrualDC Accrual Day count
	 * @param bApplyCpnEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param bApplyAccEOMAdj Apply end-of-month adjustment to the accrual periods
	 * @param strCalendar Holiday Calendar
	 * @param dblBaseNotional Coupon Period Base Notional
	 * @param notlSchedule Coupon Period Notional Schedule
	 * @param dblFixedCouponFloatSpread Fixed Coupon/Float Spread
	 * @param strPayCurrency Pay Currency
	 * @param strCouponCurrency Coupon Currency
	 * @param forwardLabel The Forward Label
	 * @param creditLabel The Credit Label
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public GenericCouponPeriod (
		final double dblStartDate,
		final double dblEndDate,
		final double dblAccrualStartDate,
		final double dblAccrualEndDate,
		final double dblPayDate,
		final org.drip.analytics.cashflow.ResetPeriodContainer rpc,
		final double dblFXFixingDate,
		final int iFreq,
		final double dblDCF,
		final java.lang.String strCouponDC,
		final java.lang.String strAccrualDC,
		final boolean bApplyCpnEOMAdj,
		final boolean bApplyAccEOMAdj,
		final java.lang.String strCalendar,
		final double dblBaseNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final double dblFixedCouponFloatSpread,
		final java.lang.String strPayCurrency,
		final java.lang.String strCouponCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStartDate = dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEndDate = dblEndDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblAccrualStartDate = dblAccrualStartDate) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblAccrualEndDate = dblAccrualEndDate) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblPayDate = dblPayDate) ||
							!org.drip.quant.common.NumberUtil.IsValid (_dblDCF = dblDCF) || _dblStartDate >=
								_dblEndDate || _dblAccrualStartDate >= _dblAccrualEndDate ||
									!org.drip.quant.common.NumberUtil.IsValid (_dblBaseNotional =
										dblBaseNotional) || null == (_strPayCurrency = strPayCurrency) ||
											_strPayCurrency.isEmpty())
			throw new java.lang.Exception ("CouponPeriod ctr: Invalid inputs");

		_iFreq = iFreq;
		_creditLabel = creditLabel;
		_strCalendar = strCalendar;
		_strCouponDC = strCouponDC;
		_strAccrualDC = strAccrualDC;
		_bApplyAccEOMAdj = bApplyAccEOMAdj;
		_bApplyCpnEOMAdj = bApplyCpnEOMAdj;
		_dblFXFixingDate = dblFXFixingDate;

		if (null == (_notlSchedule = notlSchedule))
			_notlSchedule = org.drip.product.params.FactorSchedule.BulletSchedule();

		if (null != (_forwardLabel = forwardLabel)) {
			if (null == (_rpc = rpc) || !org.drip.quant.common.NumberUtil.IsValid (_dblFloatSpread =
				dblFixedCouponFloatSpread))
				throw new java.lang.Exception
					("CouponPeriod ctr: Invalid Forward/Reset/Float Spread Combination");

			_strCouponCurrency = _forwardLabel.currency();
		} else {
			if (!org.drip.quant.common.NumberUtil.IsValid (_dblFixedCoupon = dblFixedCouponFloatSpread) ||
				null == (_strCouponCurrency = strCouponCurrency) || _strCouponCurrency.isEmpty())
				throw new java.lang.Exception
					("CouponPeriod ctr: Invalid Fixed Coupon/Coupon Currency Combination");
		}
	}

	/**
	 * Return the period Start Date
	 * 
	 * @return Period Start Date
	 */

	public double startDate()
	{
		return _dblStartDate;
	}

	/**
	 * Return the period End Date
	 * 
	 * @return Period End Date
	 */

	public double endDate()
	{
		return _dblEndDate;
	}

	/**
	 * Return the period Accrual Start Date
	 * 
	 * @return Period Accrual Start Date
	 */

	public double accrualStartDate()
	{
		return _dblAccrualStartDate;
	}

	/**
	 * Check whether the supplied date is inside the period specified
	 * 
	 * @param dblDate Date input
	 * 
	 * @return True indicates the specified date is inside the period
	 * 
	 * @throws java.lang.Exception Thrown if input is invalid
	 */

	public boolean contains (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("CouponPeriod::contains => Invalid Inputs");

		if (_dblStartDate > dblDate || dblDate > _dblEndDate) return false;

		return true;
	}

	/**
	 * Set the period Accrual Start Date
	 * 
	 * @param dblAccrualStartDate Period Accrual Start Date
	 * 
	 * @return TRUE => Accrual Start Date Successfully Set
	 */

	public boolean setAccrualStartDate (
		final double dblAccrualStartDate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblAccrualStartDate)) return false;

		_dblAccrualStartDate = dblAccrualStartDate;
		return true;
	}

	/**
	 * Return the period Accrual End Date
	 * 
	 * @return Period Accrual End Date
	 */

	public double accrualEndDate()
	{
		return _dblAccrualEndDate;
	}

	/**
	 * Return the period Pay Date
	 * 
	 * @return Period Pay Date
	 */

	public double payDate()
	{
		return _dblPayDate;
	}

	/**
	 * Set the period Pay Date
	 * 
	 * @param dblPayDate Period Pay Date
	 * 
	 * @return TRUE => Period Pay Date Successfully set
	 */

	public boolean setPayDate (
		final double dblPayDate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPayDate)) return false;

		_dblPayDate = dblPayDate;
		return true;
	}

	/**
	 * Retrieve the Reset Period Container Instance
	 * 
	 * @return The Reset Period Container Instance
	 */

	public org.drip.analytics.cashflow.ResetPeriodContainer rpc()
	{
		return _rpc;
	}

	/**
	 * Return the period FX Fixing Date
	 * 
	 * @return Period FX Fixing Date
	 */

	public double fxFixingDate()
	{
		return _dblFXFixingDate;
	}

	/**
	 * Coupon Period FX
	 * 
	 * @param csqs Market Parameters
	 * 
	 * @return The Period FX
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double fx (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		org.drip.state.identifier.FXLabel fxLabel = fxLabel();

		if (null == fxLabel) return 1.;

		if (null == csqs) throw new java.lang.Exception ("CouponPeriod::fx => Invalid Inputs");

		if (!isFXMTM()) return csqs.getFixing (_dblFXFixingDate, fxLabel);

		org.drip.quant.function1D.AbstractUnivariate auFX = csqs.fxCurve (fxLabel);

		if (null == auFX)
			throw new java.lang.Exception ("CouponPeriod::fx => No Curve for " +
				fxLabel.fullyQualifiedName());

		return auFX.evaluate (_dblPayDate);
	}

	/**
	 * Is this Cash Flow FX MTM'ed?
	 * 
	 * @return TRUE => FX MTM is on (i.e., FX is not driven by fixing)
	 */

	public boolean isFXMTM()
	{
		return !org.drip.quant.common.NumberUtil.IsValid (_dblFXFixingDate);
	}

	/**
	 * Retrieve the Coupon Day Count
	 * 
	 * @return The Coupon Day Count
	 */

	public java.lang.String couponDC()
	{
		return _strCouponDC;
	}

	/**
	 * Retrieve the Coupon EOM Adjustment Flag
	 * 
	 * @return The Coupon EOM Adjustment Flag
	 */

	public boolean couponEODAdjustment()
	{
		return _bApplyCpnEOMAdj;
	}

	/**
	 * Retrieve the Accrual Day Count
	 * 
	 * @return The Accrual Day Count
	 */

	public java.lang.String accrualDC()
	{
		return _strAccrualDC;
	}

	/**
	 * Retrieve the Accrual EOM Adjustment Flag
	 * 
	 * @return The Accrual EOM Adjustment Flag
	 */

	public boolean accrualEODAdjustment()
	{
		return _bApplyAccEOMAdj;
	}

	/**
	 * Get the coupon DCF
	 * 
	 * @return The coupon DCF
	 */

	public double couponDCF()
	{
		return _dblDCF;
	}

	/**
	 * Get the period Accrual Day Count Fraction to an accrual end date
	 * 
	 * @param dblAccrualEnd Accrual End Date
	 * 
	 * @exception Throws if inputs are invalid, or if the date does not lie within the period
	 */

	public double accrualDCF (
		final double dblAccrualEnd)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblAccrualEnd))
			throw new java.lang.Exception ("CouponPeriod::accrualDCF => Accrual end is NaN!");

		if (_dblAccrualStartDate > dblAccrualEnd && dblAccrualEnd > _dblAccrualEndDate)
			throw new java.lang.Exception ("CouponPeriod::accrualDCF => Invalid in-period accrual date!");

		org.drip.analytics.daycount.ActActDCParams actactDCParams = new
			org.drip.analytics.daycount.ActActDCParams (_iFreq, _dblAccrualStartDate, _dblAccrualEndDate);

		return org.drip.analytics.daycount.Convention.YearFraction (_dblAccrualStartDate, dblAccrualEnd,
			_strAccrualDC, _bApplyAccEOMAdj, actactDCParams, _strCalendar) /
				org.drip.analytics.daycount.Convention.YearFraction (_dblAccrualStartDate,
					_dblAccrualEndDate, _strAccrualDC, _bApplyAccEOMAdj, actactDCParams, _strCalendar) *
						_dblDCF;
	}

	/**
	 * Retrieve the Calendar
	 * 
	 * @return The Calendar
	 */

	public java.lang.String calendar()
	{
		return _strCalendar;
	}

	/**
	 * Retrieve the Coupon Frequency
	 * 
	 * @return The Coupon Frequency
	 */

	public int freq()
	{
		return _iFreq;
	}

	/**
	 * Convert the Coupon Frequency into a Tenor
	 * 
	 * @return The Coupon Frequency converted into a Tenor
	 */

	public java.lang.String tenor()
	{
		int iTenorInMonths = 12 / _iFreq ;

		return 1 == iTenorInMonths || 2 == iTenorInMonths || 3 == iTenorInMonths || 6 == iTenorInMonths || 12
			== iTenorInMonths ? iTenorInMonths + "M" : "ON";
	}

	/**
	 * Retrieve the Pay Currency
	 * 
	 * @return The Pay Currency
	 */

	public java.lang.String payCurrency()
	{
		return _strPayCurrency;
	}

	/**
	 * Retrieve the Coupon Currency
	 * 
	 * @return The Coupon Currency
	 */

	public java.lang.String couponCurrency()
	{
		return _strCouponCurrency;
	}

	/**
	 * Get the Period Fixed Coupon Rate
	 * 
	 * @return Period Fixed Coupon Rate
	 */

	public double fixedCoupon()
	{
		return _dblFixedCoupon;
	}

	/**
	 * Get the period spread over the floating index
	 * 
	 * @return Period Float Spread
	 */

	public double floatSpread()
	{
		return _dblFloatSpread;
	}

	/**
	 * Get the Period Base Notional
	 * 
	 * @return Period Base Notional
	 */

	public double baseNotional()
	{
		return _dblBaseNotional;
	}

	/**
	 * Get the period Notional Schedule
	 * 
	 * @return Period Notional Schedule
	 */

	public org.drip.product.params.FactorSchedule notionalSchedule()
	{
		return _notlSchedule;
	}

	/**
	 * Coupon Period Notional Corresponding to the specified Date
	 * 
	 * @param dblDate The Specified Date
	 * 
	 * @return The Period Notional Corresponding to the specified Date
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate) || !contains (dblDate))
			throw new java.lang.Exception ("CouponPeriod::notional => Invalid Inputs");

		return _dblBaseNotional * (null == _notlSchedule ? 1. : _notlSchedule.factor (dblDate));
	}

	/**
	 * Coupon Period Notional Aggregated over the specified Dates
	 * 
	 * @param dblDate1 The Date #1
	 * @param dblDate2 The Date #2
	 * 
	 * @return The Period Notional Aggregated over the specified Dates
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate2) || !contains (dblDate1) || !contains (dblDate2))
			throw new java.lang.Exception ("Coupon::notional => Invalid Dates");

		return _dblBaseNotional * (null == _notlSchedule ? 1. : _notlSchedule.factor (dblDate1, dblDate2));
	}

	/**
	 * Return the Collateral Label
	 * 
	 * @return The Collateral Label
	 */

	public org.drip.state.identifier.CollateralLabel collateralLabel()
	{
		return org.drip.state.identifier.CollateralLabel.Standard (_strPayCurrency);
	}

	/**
	 * Return the Credit Label
	 * 
	 * @return The Credit Label
	 */

	public org.drip.state.identifier.CreditLabel creditLabel()
	{
		return _creditLabel;
	}

	/**
	 * Return the Forward Label
	 * 
	 * @return The Forward Label
	 */

	public org.drip.state.identifier.ForwardLabel forwardLabel()
	{
		return _forwardLabel;
	}

	/**
	 * Return the Funding Label
	 * 
	 * @return The Funding Label
	 */

	public org.drip.state.identifier.FundingLabel fundingLabel()
	{
		return org.drip.state.identifier.FundingLabel.Standard (_strPayCurrency);
	}

	/**
	 * Return the FX Label
	 * 
	 * @return The FX Label
	 */

	public org.drip.state.identifier.FXLabel fxLabel()
	{
		java.lang.String strCouponCurrency = couponCurrency();

		return _strPayCurrency.equalsIgnoreCase (strCouponCurrency) ? null :
			org.drip.state.identifier.FXLabel.Standard (_strPayCurrency + "/" + strCouponCurrency);
	}

	/**
	 * Compute the Coupon Measures at the specified Accrual End Date
	 * 
	 * @param dblValueDate Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * 
	 * @return The Coupon Measures at the specified Accrual End Date
	 */

	public org.drip.analytics.output.GenericCouponPeriodMetrics baseMetrics (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValueDate)) return null;

		double dblDF = 1.;
		double dblSurvival = 1.;

		org.drip.state.identifier.FXLabel fxLabel = fxLabel();

		org.drip.state.identifier.CreditLabel creditLabel = creditLabel();

		org.drip.state.identifier.FundingLabel fundingLabel = fundingLabel();

		org.drip.analytics.definition.CreditCurve cc = null == csqs ? null : csqs.creditCurve (creditLabel);

		org.drip.analytics.rates.DiscountCurve dcFunding = null == csqs ? null : csqs.fundingCurve
			(fundingLabel);

		int iAccrualCompoundingRule = null == _rpc ?
			org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC :
				_rpc.accrualCompoundingRule();

		java.util.List<org.drip.analytics.output.ResetPeriodMetrics> lsRPM = new
			java.util.ArrayList<org.drip.analytics.output.ResetPeriodMetrics>();

		try {
			double dblFX = fx (csqs);

			if (null != dcFunding) dblDF = dcFunding.df (_dblPayDate);

			if (null != cc) dblSurvival = cc.survival (_dblPayDate);

			if (null == _forwardLabel) {
				lsRPM.add (new org.drip.analytics.output.ResetPeriodMetrics (_dblStartDate, _dblEndDate,
					java.lang.Double.NaN, _dblFixedCoupon, _dblDCF));

				return org.drip.analytics.output.GenericCouponPeriodMetrics.Create (_dblStartDate, _dblEndDate,
					_dblPayDate, notional (_dblEndDate), iAccrualCompoundingRule, lsRPM, dblSurvival, dblDF,
						dblFX, calcConvexityAdjustment (dblValueDate, _dblStartDate, csqs), creditLabel,
							_forwardLabel, fundingLabel, fxLabel);
			}

			for (org.drip.analytics.cashflow.GenericComposablePeriod rp : _rpc.resetPeriods()) {
				org.drip.analytics.output.ResetPeriodMetrics rpm = resetPeriodMetrics (rp, dblValueDate,
					csqs);

				if (null == rpm) return null;

				lsRPM.add (rpm);
			}

			return org.drip.analytics.output.GenericCouponPeriodMetrics.Create (_dblStartDate, _dblEndDate,
				_dblPayDate, notional (_dblEndDate), iAccrualCompoundingRule, lsRPM, dblSurvival, dblDF,
					dblFX, org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
						iAccrualCompoundingRule ? calcConvexityAdjustment (dblValueDate, _dblStartDate, csqs)
							: null, creditLabel, _forwardLabel, fundingLabel, fxLabel);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Compute the Accrual Measures to the specified Accrual End Date
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * 
	 * @return The Accrual Measures to the specified Accrual End Date
	 */

	public org.drip.analytics.output.GenericCouponAccrualMetrics accrualMetrics (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValueDate) || _dblStartDate == dblValueDate)
			return null;

		int iAccrualCompoundingRule = null == _rpc ?
			org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC :
				_rpc.accrualCompoundingRule();

		java.util.List<org.drip.analytics.output.ResetPeriodMetrics> lsRPM = new
			java.util.ArrayList<org.drip.analytics.output.ResetPeriodMetrics>();

		try {
			 if (!contains (dblValueDate)) return null;

			 double dblFX = fx (csqs);

			 if (null == _forwardLabel) {
				lsRPM.add (new org.drip.analytics.output.ResetPeriodMetrics (_dblStartDate, dblValueDate,
					java.lang.Double.NaN, _dblFixedCoupon,
						org.drip.analytics.daycount.Convention.YearFraction (_dblStartDate, dblValueDate,
							_strAccrualDC, _bApplyAccEOMAdj, null, _strCalendar)));

				return new org.drip.analytics.output.GenericCouponAccrualMetrics (_dblStartDate, dblValueDate,
					dblFX, notional (dblValueDate), iAccrualCompoundingRule, lsRPM);
			}

			for (org.drip.analytics.cashflow.GenericComposablePeriod rp : _rpc.resetPeriods()) {
				double dblResetPeriodStartDate = rp.start();

				int iNodeLocationIndicator = rp.nodeLocation (dblValueDate);

				if (org.drip.analytics.cashflow.GenericComposablePeriod.NODE_LEFT_OF_SEGMENT == iNodeLocationIndicator ||
					dblValueDate == dblResetPeriodStartDate)
					break;

				org.drip.analytics.output.ResetPeriodMetrics rpm = resetPeriodMetrics (rp, dblValueDate,
					csqs);

				if (null == rpm) return null;

				if (org.drip.analytics.cashflow.GenericComposablePeriod.NODE_INSIDE_SEGMENT == iNodeLocationIndicator)
					lsRPM.add (new org.drip.analytics.output.ResetPeriodMetrics (dblResetPeriodStartDate,
						dblValueDate, dblResetPeriodStartDate, rpm.nominalRate(),
							org.drip.analytics.daycount.Convention.YearFraction (dblResetPeriodStartDate,
								dblValueDate, _strAccrualDC, _bApplyAccEOMAdj, null, _strCalendar)));
				else
					lsRPM.add (rpm);
			}

			return new org.drip.analytics.output.GenericCouponAccrualMetrics (_dblStartDate, dblValueDate, dblFX,
				notional (dblValueDate), iAccrualCompoundingRule, lsRPM);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate the Forward Predictor/Response Constraint
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * @param pqs Product Quote Set
	 * 
	 * @return The Forward Predictor/Response Constraint
	 */

	public org.drip.state.estimator.PredictorResponseWeightConstraint forwardPRWC (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == pqs) return null;

		double dblPV = 0.;
		double dblAccrued = 0.;

		CalibInput ci = calibInput (pqs);

		try {
			dblAccrued = calibAccrued (ci, dblValueDate, csqs);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		org.drip.analytics.output.GenericCouponPeriodMetrics cpm = baseMetrics (dblValueDate, csqs);

		if (null == cpm) return null;

		java.util.Map<java.lang.Double, java.lang.Double> mapForwardRateLoading =
			cpm.forwardRateForwardLoading (pqs.forwardLabel());

		if (null != mapForwardRateLoading && 0 != mapForwardRateLoading.size()) {
			dblPV -= cpm.annuity() * cpm.dcf() * ci._dblFloatSpread;

			for (java.util.Map.Entry<java.lang.Double, java.lang.Double> meForwardRateLoading :
				mapForwardRateLoading.entrySet()) {
				double dblDateAnchor = meForwardRateLoading.getKey();

				double dblForwardRateLoading = meForwardRateLoading.getValue();

				if (!prwc.addPredictorResponseWeight (dblDateAnchor, dblForwardRateLoading)) return null;

				if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblDateAnchor, dblForwardRateLoading))
					return null;
			}
		} else
			dblPV -= cpm.annuity() * cpm.dcf() * ci.fullCoupon (cpm);

		if (!prwc.updateValue (dblPV + dblAccrued)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	/**
	 * Generate the Funding Predictor/Response Constraint
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * @param pqs Product Quote Set
	 * 
	 * @return The Funding Predictor/Response Constraint
	 */

	public org.drip.state.estimator.PredictorResponseWeightConstraint fundingPRWC (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == pqs) return null;

		double dblPV = 0.;
		double dblAccrued = 0.;

		CalibInput ci = calibInput (pqs);

		try {
			dblAccrued = calibAccrued (ci, dblValueDate, csqs);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		org.drip.analytics.output.GenericCouponPeriodMetrics cpm = baseMetrics (dblValueDate, csqs);

		if (null == cpm) return null;

		java.util.Map<java.lang.Double, java.lang.Double> mapDiscountFactorLoading =
			cpm.discountFactorFundingLoading (pqs.fundingLabel());

		if (null != mapDiscountFactorLoading && 0 != mapDiscountFactorLoading.size()) {
			for (java.util.Map.Entry<java.lang.Double, java.lang.Double> meDiscountFactorLoading :
				mapDiscountFactorLoading.entrySet()) {
				double dblDateAnchor = meDiscountFactorLoading.getKey();

				double dblDiscountFactorFundingLoading = meDiscountFactorLoading.getValue() * ci.fullCoupon
					(cpm);

				if (!prwc.addPredictorResponseWeight (dblDateAnchor, dblDiscountFactorFundingLoading))
					return null;

				if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblDateAnchor,
					dblDiscountFactorFundingLoading))
					return null;
			}
		} else
			dblPV -= cpm.annuity() * cpm.dcf() * ci.fullCoupon (cpm);

		if (!prwc.updateValue (dblPV + dblAccrued)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	/**
	 * Generate the Merged Forward/Funding Predictor/Response Constraint
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * @param pqs Product Quote Set
	 * 
	 * @return The Merged Forward/Funding Predictor/Response Constraint
	 */

	public org.drip.state.estimator.PredictorResponseWeightConstraint forwardFundingPRWC (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == _forwardLabel) return fundingPRWC (dblValueDate, csqs, pqs);

		if (null == pqs) return null;

		double dblPV = 0.;
		double dblAccrued = 0.;

		CalibInput ci = calibInput (pqs);

		try {
			dblAccrued = calibAccrued (ci, dblValueDate, csqs);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		org.drip.analytics.output.GenericCouponPeriodMetrics cpm = baseMetrics (dblValueDate, csqs);

		if (null == cpm) return null;

		java.util.Map<java.lang.Double, java.lang.Double> mapMergedDiscountFactorLoading =
			cpm.discountFactorForwardFundingLoading (pqs.forwardLabel(), pqs.fundingLabel());

		if (null != mapMergedDiscountFactorLoading && 0 != mapMergedDiscountFactorLoading.size()) {
			for (java.util.Map.Entry<java.lang.Double, java.lang.Double> meMergedDiscountFactorLoading :
				mapMergedDiscountFactorLoading.entrySet()) {
				double dblMergedDateAnchor = meMergedDiscountFactorLoading.getKey();

				double dblMergedDiscountFactorLoading = meMergedDiscountFactorLoading.getValue();

				if (!prwc.addPredictorResponseWeight (dblMergedDateAnchor, dblMergedDiscountFactorLoading))
					return null;

				if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblMergedDateAnchor,
					dblMergedDiscountFactorLoading))
					return null;
			}

			if (0. != ci._dblFloatSpread) {
				java.util.Map<java.lang.Double, java.lang.Double> mapDiscountFactorLoading =
					cpm.discountFactorFundingLoading (pqs.fundingLabel());

				if (null != mapDiscountFactorLoading && 0 != mapDiscountFactorLoading.size()) {
					for (java.util.Map.Entry<java.lang.Double, java.lang.Double> meDiscountFactorLoading :
						mapDiscountFactorLoading.entrySet()) {
						double dblDateAnchor = meDiscountFactorLoading.getKey();

						double dblDiscountFactorLoading = meDiscountFactorLoading.getValue();

						if (!prwc.addPredictorResponseWeight (dblDateAnchor, dblDiscountFactorLoading))
							return null;

						if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblDateAnchor,
							dblDiscountFactorLoading))
							return null;
					}
				}
			}

			if (!prwc.addMergeLabel (_forwardLabel)) return null;
		}

		if (!prwc.updateValue (dblPV + dblAccrued)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	/**
	 * Create a set of loss period measures
	 * 
	 * @param comp Component for which the measures are to be generated
	 * @param valParams ValuationParams from which the periods are generated
	 * @param pricerParams PricerParams that control the generation characteristics
	 * @param dblWorkoutDate Double JulianDate representing the absolute end of all the generated periods
	 * @param csqs Market Parameters
	 *  
	 * @return The Generated Loss Quadrature Metrics
	 */

	public java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> lossMetrics (
		final org.drip.product.definition.CreditComponent comp,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final double dblWorkoutDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == comp || null == valParams || null == pricerParams || null == csqs || null ==
			csqs.creditCurve (comp.creditLabel()[0]) || !org.drip.quant.common.NumberUtil.IsValid
				(dblWorkoutDate) || _dblStartDate > dblWorkoutDate)
			return null;

		org.drip.analytics.rates.DiscountCurve dc = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (_strPayCurrency));

		if (null == dc) return null;

		int iDiscretizationScheme = pricerParams.discretizationScheme();

		java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> lsLQM = null;
		double dblPeriodEndDate = _dblEndDate < dblWorkoutDate ? _dblEndDate : dblWorkoutDate;

		if (org.drip.param.pricer.PricerParams.PERIOD_DISCRETIZATION_DAY_STEP == iDiscretizationScheme &&
			(null == (lsLQM = org.drip.analytics.support.LossQuadratureGenerator.GenerateDayStepLossPeriods
				(comp, valParams, this, dblPeriodEndDate, pricerParams.unitSize(), csqs)) || 0 ==
					lsLQM.size()))
				return null;

		if (org.drip.param.pricer.PricerParams.PERIOD_DISCRETIZATION_PERIOD_STEP == iDiscretizationScheme &&
			(null == (lsLQM =
				org.drip.analytics.support.LossQuadratureGenerator.GeneratePeriodUnitLossPeriods (comp,
					valParams, this, dblPeriodEndDate, pricerParams.unitSize(), csqs)) || 0 ==
						lsLQM.size()))
			return null;

		if (org.drip.param.pricer.PricerParams.PERIOD_DISCRETIZATION_FULL_COUPON == iDiscretizationScheme &&
			(null == (lsLQM = org.drip.analytics.support.LossQuadratureGenerator.GenerateWholeLossPeriods
				(comp, valParams, this, dblPeriodEndDate, csqs)) || 0 == lsLQM.size()))
			return null;

		return lsLQM;
	}

	@Override public int hashCode()
	{
		long lBits = java.lang.Double.doubleToLongBits ((int) _dblPayDate);

		return (int) (lBits ^ (lBits >>> 32));
	}

	@Override public int compareTo (
		final GenericCouponPeriod periodOther)
	{
		if ((int) _dblPayDate > (int) (periodOther._dblPayDate)) return 1;

		if ((int) _dblPayDate < (int) (periodOther._dblPayDate)) return -1;

		return 0;
	}
}
