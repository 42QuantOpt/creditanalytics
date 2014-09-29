
package org.drip.analytics.cashflow;

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
 * FixedCouponPeriod implements the fixed coupon functionality.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FixedCouponPeriod {
	class CalibInput {
		double _dblFixedCoupon = java.lang.Double.NaN;
		double _dblFixedCouponBasis = java.lang.Double.NaN;

		double fullCoupon()
		{
			return _dblFixedCoupon + _dblFixedCouponBasis;
		}
	}

	private int _iFreq = 2;
	private int _iAccrualCompoundingRule = -1;
	private java.lang.String _strPayCurrency = "";
	private java.lang.String _strCouponCurrency = "";
	private double _dblPayDate = java.lang.Double.NaN;
	private double _dblBaseNotional = java.lang.Double.NaN;
	private double _dblFXFixingDate = java.lang.Double.NaN;
	private org.drip.state.identifier.CreditLabel _creditLabel = null;
	private org.drip.product.params.FactorSchedule _notlSchedule = null;
	private java.util.List<org.drip.analytics.cashflow.ComposableFixedPeriod> _lsComposableFixedPeriod =
		null;

	private CalibInput calibInput (
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		CalibInput ci = new CalibInput();

		org.drip.analytics.cashflow.ComposableFixedPeriod cfpFirst = _lsComposableFixedPeriod.get (0);

		try {
			ci._dblFixedCoupon = cfpFirst.baseRate (null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		ci._dblFixedCouponBasis = cfpFirst.basis();

		if (null != pqs && pqs instanceof org.drip.product.calib.FixedStreamQuoteSet) {
			org.drip.product.calib.FixedStreamQuoteSet fsqs = (org.drip.product.calib.FixedStreamQuoteSet)
				pqs;

			try {
				if (fsqs.containsCoupon()) ci._dblFixedCoupon = fsqs.coupon();

				if (fsqs.containsCouponBasis()) ci._dblFixedCouponBasis = fsqs.couponBasis();
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return ci;
	}

	private double calibAccrued (
		final CalibInput ci,
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		org.drip.analytics.output.FixedCouponAccrualMetrics fcam = accrualMetrics (dblValueDate, csqs);

		return null == fcam ? 0. : fcam.notional() * fcam.dcf() * ci.fullCoupon() * fcam.fx();
	}

	/**
	 * FixedCouponPeriod Constructor
	 * 
	 * @param iFreq Frequency
	 * @param dblPayDate Period Pay Date
	 * @param strPayCurrency Pay Currency
	 * @param strCouponCurrency Coupon Currency
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param dblBaseNotional Coupon Period Base Notional
	 * @param notlSchedule Coupon Period Notional Schedule
	 * @param creditLabel The Credit Label
	 * @param dblFXFixingDate The FX Fixing Date for non-MTM'ed Cash-flow
	 * 
	 * @throws java.lang.Exception Thrown if the Accrual Compounding Rule is invalid
	 */

	public FixedCouponPeriod (
		final int iFreq,
		final double dblPayDate,
		final java.lang.String strPayCurrency,
		final java.lang.String strCouponCurrency,
		final int iAccrualCompoundingRule,
		final double dblBaseNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final org.drip.state.identifier.CreditLabel creditLabel,
		final double dblFXFixingDate)
		throws java.lang.Exception
	{
		if (0 >= (_iFreq = iFreq) || !org.drip.quant.common.NumberUtil.IsValid (_dblPayDate = dblPayDate) ||
			null == (_strPayCurrency = strPayCurrency) || _strPayCurrency.isEmpty() || null ==
				(_strCouponCurrency = strCouponCurrency) || _strCouponCurrency.isEmpty() ||
					!org.drip.analytics.support.ResetUtil.ValidateCompoundingRule (_iAccrualCompoundingRule =
						iAccrualCompoundingRule) || !org.drip.quant.common.NumberUtil.IsValid
							(_dblBaseNotional = dblBaseNotional))
			throw new java.lang.Exception ("FixedCouponPeriod ctr: Invalid Inputs");

		_creditLabel = creditLabel;
		_dblFXFixingDate = dblFXFixingDate;

		if (null == (_notlSchedule = notlSchedule))
			_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();
	}

	/**
	 * Append the specified Composable Fixed Period
	 * 
	 * @param cfp The Composable Fixed Period
	 * 
	 * @return TRUE => The Composable Fixed Period Successfully Appended
	 */

	public boolean appendPeriod (
		final org.drip.analytics.cashflow.ComposableFixedPeriod cfp)
	{
		if (null == cfp) return false;

		if (null == _lsComposableFixedPeriod)
			_lsComposableFixedPeriod = new
				java.util.ArrayList<org.drip.analytics.cashflow.ComposableFixedPeriod>();

		_lsComposableFixedPeriod.add (cfp);

		return true;
	}

	/**
	 * Retrieve the Composable Fixed Periods
	 * 
	 * @return The Composable Fixed Periods
	 */

	public java.util.List<org.drip.analytics.cashflow.ComposableFixedPeriod> periods()
	{
		return _lsComposableFixedPeriod;
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
			throw new java.lang.Exception ("FixedCouponPeriod::contains => Invalid Inputs");

		double dblStartDate = _lsComposableFixedPeriod.get (0).accrualStartDate();

		if (dblStartDate > dblDate) return false;

		double dblEndDate = _lsComposableFixedPeriod.get (_lsComposableFixedPeriod.size() -
			1).accrualEndDate();

		if (dblEndDate < dblDate) return false;

		return true;
	}

	/**
	 * Retrieve the Accrual Compounding Rule
	 * 
	 * @return The Accrual Compounding Rule
	 */

	public int accrualCompoundingRule()
	{
		return _iAccrualCompoundingRule;
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
	 * Return the period FX Fixing Date
	 * 
	 * @return Period FX Fixing Date
	 */

	public double fxFixingDate()
	{
		return _dblFXFixingDate;
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

		if (null == csqs) throw new java.lang.Exception ("FixedCouponPeriod::fx => Invalid Inputs");

		if (!isFXMTM()) return csqs.getFixing (_dblFXFixingDate, fxLabel);

		org.drip.quant.function1D.AbstractUnivariate auFX = csqs.fxCurve (fxLabel);

		if (null == auFX)
			throw new java.lang.Exception ("FixedCouponPeriod::fx => No Curve for " +
				fxLabel.fullyQualifiedName());

		return auFX.evaluate (_dblPayDate);
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
			throw new java.lang.Exception ("FixedCouponPeriod::notional => Invalid Inputs");

		return _dblBaseNotional * (null == _notlSchedule ? 1. : _notlSchedule.getFactor (dblDate));
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
			throw new java.lang.Exception ("FixedCouponPeriod::notional => Invalid Dates");

		return _dblBaseNotional * (null == _notlSchedule ? 1. : _notlSchedule.getFactor (dblDate1,
			dblDate2));
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
		return _strPayCurrency.equalsIgnoreCase (_strCouponCurrency) ? null :
			org.drip.state.identifier.FXLabel.Standard (_strPayCurrency + "/" + _strCouponCurrency);
	}

	/**
	 * Compute the Convexity Adjustment at the specified value date using the market data provided
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curves/Surface
	 * 
	 * @return The Convexity Adjustment Instance
	 */

	public org.drip.analytics.output.ConvexityAdjustment convexityAdjustment (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		org.drip.state.identifier.CreditLabel creditLabel = creditLabel();

		org.drip.state.identifier.FundingLabel fundingLabel = fundingLabel();

		org.drip.state.identifier.FXLabel fxLabel = fxLabel();

		org.drip.analytics.output.ConvexityAdjustment convAdj = new
			org.drip.analytics.output.ConvexityAdjustment();

		try {
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

	/**
	 * Compute the Fixed Coupon Measures
	 * 
	 * @param dblValueDate Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * 
	 * @return The Fixed Coupon Measures
	 */

	public org.drip.analytics.output.FixedCouponPeriodMetrics couponMetrics (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValueDate)) return null;

		double dblDCF = 0.;
		double dblCouponAmount = java.lang.Double.NaN;
		double dblFullCouponRate = java.lang.Double.NaN;

		org.drip.analytics.definition.CreditCurve cc = null == csqs ? null : csqs.creditCurve (_creditLabel);

		org.drip.analytics.rates.DiscountCurve dcFunding = null == csqs ? null : csqs.fundingCurve
			(fundingLabel());

		if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
			_iAccrualCompoundingRule)
			dblCouponAmount = 0.;
		else if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblCouponAmount = 1.;

		org.drip.analytics.cashflow.ComposableFixedPeriod cfpFirst = _lsComposableFixedPeriod.get (0);

		try {
			dblFullCouponRate = cfpFirst.fullCouponRate (csqs);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (org.drip.analytics.cashflow.ComposableFixedPeriod cfp : _lsComposableFixedPeriod) {
			double dblPeriodDCF = cfp.fullCouponDCF();

			dblDCF += dblPeriodDCF;

			if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				_iAccrualCompoundingRule)
				dblCouponAmount += dblFullCouponRate * dblPeriodDCF;
			else if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
				_iAccrualCompoundingRule)
				dblCouponAmount *= (1. + dblFullCouponRate * dblPeriodDCF);
		}

		if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblCouponAmount -= 1.;

		try {
			double dblFX = fx (csqs);

			double dblNotional = _dblBaseNotional * _notlSchedule.getFactor (_dblPayDate);

			return new org.drip.analytics.output.FixedCouponPeriodMetrics (cfpFirst.accrualStartDate(),
				_lsComposableFixedPeriod.get (_lsComposableFixedPeriod.size() - 1).accrualEndDate(), dblDCF,
					dblFullCouponRate, dblNotional, dblCouponAmount * dblNotional * dblFX, null == cc ? 1. :
						cc.survival (_dblPayDate), null == dcFunding ? 1. : dcFunding.df (_dblPayDate),
							dblFX, convexityAdjustment (dblValueDate, csqs));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Compute the Fixed Coupon Accrual Measures to the specified Accrual End Date
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * 
	 * @return The Fixed Coupon Accrual Measures to the specified Accrual End Date
	 */

	public org.drip.analytics.output.FixedCouponAccrualMetrics accrualMetrics (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		double dblAccrualDCF = 0.;
		double dblAccruedAmount = java.lang.Double.NaN;
		double dblFullCouponRate = java.lang.Double.NaN;

		try {
			if (!contains (dblValueDate)) return null;

			if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				_iAccrualCompoundingRule)
				dblAccruedAmount = 0.;
			else if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
				_iAccrualCompoundingRule)
				dblAccruedAmount = 1.;

			org.drip.analytics.cashflow.ComposableFixedPeriod cfpFirst = _lsComposableFixedPeriod.get (0);

			try {
				dblFullCouponRate = cfpFirst.fullCouponRate (csqs);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			for (org.drip.analytics.cashflow.ComposableFixedPeriod cfp : _lsComposableFixedPeriod) {
				double dblPeriodAccruedDCF = java.lang.Double.NaN;

				int iDateLocation = cfp.dateLocation (dblValueDate);

				if (org.drip.analytics.cashflow.ComposableFixedPeriod.NODE_RIGHT_OF_SEGMENT == iDateLocation)
					break;

				if (org.drip.analytics.cashflow.ComposableFixedPeriod.NODE_INSIDE_SEGMENT == iDateLocation)
					dblPeriodAccruedDCF = cfp.accrualDCF (dblValueDate);
				else if (org.drip.analytics.cashflow.ComposableFixedPeriod.NODE_LEFT_OF_SEGMENT ==
					iDateLocation)
					dblPeriodAccruedDCF = cfp.fullCouponDCF();

				dblAccrualDCF += dblPeriodAccruedDCF;

				if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
					_iAccrualCompoundingRule)
					dblAccruedAmount += dblFullCouponRate * dblPeriodAccruedDCF;
				else if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
					_iAccrualCompoundingRule)
					dblAccruedAmount *= (1. + dblFullCouponRate * dblPeriodAccruedDCF);
			}

			if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
				_iAccrualCompoundingRule)
				dblAccruedAmount -= 1.;

			double dblFX = fx (csqs);

			double dblNotional = _dblBaseNotional * _notlSchedule.getFactor (_dblPayDate);

			return new org.drip.analytics.output.FixedCouponAccrualMetrics (cfpFirst.accrualStartDate(),
				dblValueDate, dblAccrualDCF, dblFullCouponRate, dblNotional, dblAccruedAmount * dblNotional *
					dblFX, dblFX);
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

		CalibInput ci = calibInput (pqs);

		if (null == ci) return null;

		org.drip.analytics.output.FixedCouponPeriodMetrics fcpm = couponMetrics (dblValueDate, csqs);

		if (null == fcpm) return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		if (!prwc.updateValue (calibAccrued (ci, dblValueDate, csqs) - fcpm.notional() * fcpm.dcf() *
			ci.fullCoupon() * fcpm.fx() * fcpm.survival() * fcpm.df() * fcpm.convAdj().cumulative()))
			return null;

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

		CalibInput ci = calibInput (pqs);

		if (null == ci) return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		org.drip.analytics.output.FixedCouponPeriodMetrics fcpm = couponMetrics (dblValueDate, csqs);

		if (null == fcpm) return null;

		if (fundingLabel().match (pqs.fundingLabel())) {
			double dblDiscountFactorLoading = fcpm.notional() * fcpm.dcf() * ci.fullCoupon() * fcpm.fx() *
				fcpm.survival() * fcpm.convAdj().cumulative();

			if (!prwc.addPredictorResponseWeight (_dblPayDate, dblDiscountFactorLoading)) return null;

			if (!prwc.addDResponseWeightDManifestMeasure ("PV", _dblPayDate, dblDiscountFactorLoading))
				return null;
		} else {
			if (!prwc.updateValue (-1. * fcpm.notional() * fcpm.dcf() * ci.fullCoupon() * fcpm.fx() *
				fcpm.survival() * fcpm.df() * fcpm.convAdj().cumulative()))
				return null;
		}

		if (!prwc.updateValue (calibAccrued (ci, dblValueDate, csqs))) return null;

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
		return fundingPRWC (dblValueDate, csqs, pqs);
	}
}
