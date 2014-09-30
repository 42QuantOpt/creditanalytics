
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
 * ComposedPeriod implements the composed coupon period functionality.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class ComposedPeriod {
	private int _iFreq = 2;
	private int _iAccrualCompoundingRule = -1;
	private java.lang.String _strPayCurrency = "";
	private double _dblPayDate = java.lang.Double.NaN;
	private double _dblBaseNotional = java.lang.Double.NaN;
	private double _dblFXFixingDate = java.lang.Double.NaN;
	private org.drip.state.identifier.CreditLabel _creditLabel = null;
	private org.drip.product.params.FactorSchedule _notlSchedule = null;
	private java.util.List<org.drip.analytics.cashflow.ComposablePeriod> _lsComposablePeriod = null;

	private double calibAccrued (
		final org.drip.analytics.cashflow.ComposedPeriodQuoteSet cpqs,
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		org.drip.analytics.output.ComposedPeriodMetrics cpm = accrualMetrics (dblValueDate, csqs);

		return null == cpm ? 0. : cpm.dcf() * notional (_dblPayDate) * fx (csqs) * (cpqs.baseRate() +
			cpqs.basis());
	}

	/**
	 * ComposedPeriod Constructor
	 * 
	 * @param lsComposablePeriod List of Composable Periods
	 * @param iFreq Frequency
	 * @param dblPayDate Period Pay Date
	 * @param strPayCurrency Pay Currency
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param dblBaseNotional Coupon Period Base Notional
	 * @param notlSchedule Coupon Period Notional Schedule
	 * @param creditLabel The Credit Label
	 * @param dblFXFixingDate The FX Fixing Date for non-MTM'ed Cash-flow
	 * 
	 * @throws java.lang.Exception Thrown if the Accrual Compounding Rule is invalid
	 */

	public ComposedPeriod (
		final java.util.List<org.drip.analytics.cashflow.ComposablePeriod> lsComposablePeriod,
		final int iFreq,
		final double dblPayDate,
		final java.lang.String strPayCurrency,
		final int iAccrualCompoundingRule,
		final double dblBaseNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final org.drip.state.identifier.CreditLabel creditLabel,
		final double dblFXFixingDate)
		throws java.lang.Exception
	{
		if (null == (_lsComposablePeriod = lsComposablePeriod) || 0 == _lsComposablePeriod.size() || 0 >=
			(_iFreq = iFreq) || !org.drip.quant.common.NumberUtil.IsValid (_dblPayDate = dblPayDate) || null
				== (_strPayCurrency = strPayCurrency) || _strPayCurrency.isEmpty() ||
					!org.drip.analytics.support.ResetUtil.ValidateCompoundingRule (_iAccrualCompoundingRule =
						iAccrualCompoundingRule) || !org.drip.quant.common.NumberUtil.IsValid
							(_dblBaseNotional = dblBaseNotional))
			throw new java.lang.Exception ("ComposedPeriod ctr: Invalid Inputs");

		_creditLabel = creditLabel;
		_dblFXFixingDate = dblFXFixingDate;

		if (null == (_notlSchedule = notlSchedule))
			_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();
	}

	/**
	 * Retrieve the List of Composable Periods
	 * 
	 * @return The List of Composable Periods
	 */

	public java.util.List<org.drip.analytics.cashflow.ComposablePeriod> periods()
	{
		return _lsComposablePeriod;
	}

	/**
	 * Period Start Date
	 * 
	 * @return The Period Start Date
	 */

	public double startDate()
	{
		return _lsComposablePeriod.get (0).accrualStartDate();
	}

	/**
	 * Period End Date
	 * 
	 * @return The Period End Date
	 */

	public double endDate()
	{
		return _lsComposablePeriod.get (_lsComposablePeriod.size() - 1).accrualEndDate();
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
			throw new java.lang.Exception ("ComposedPeriod::contains => Invalid Inputs");

		double dblStartDate = _lsComposablePeriod.get (0).accrualStartDate();

		if (dblStartDate > dblDate) return false;

		double dblEndDate = _lsComposablePeriod.get (_lsComposablePeriod.size() - 1).accrualEndDate();

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

		if (null == csqs) throw new java.lang.Exception ("ComposedPeriod::fx => Invalid Inputs");

		if (!isFXMTM()) return csqs.getFixing (_dblFXFixingDate, fxLabel);

		org.drip.quant.function1D.AbstractUnivariate auFX = csqs.fxCurve (fxLabel);

		if (null == auFX)
			throw new java.lang.Exception ("ComposedPeriod::fx => No Curve for " +
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
		return _lsComposablePeriod.get (0).couponCurrency();
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
			throw new java.lang.Exception ("ComposedPeriod::notional => Invalid Inputs");

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
			throw new java.lang.Exception ("ComposedPeriod::notional => Invalid Dates");

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
	 * Return the Forward Label
	 * 
	 * @return The Forward Label
	 */

	public org.drip.state.identifier.ForwardLabel forwardLabel()
	{
		org.drip.analytics.cashflow.ComposablePeriod cp = _lsComposablePeriod.get (0);

		if (cp instanceof org.drip.analytics.cashflow.ComposableFixedPeriod) return null;

		return ((org.drip.analytics.cashflow.ComposableFloatingPeriod)
			cp).referenceIndexPeriod().forwardLabel();
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
	 * Compute the Convexity Adjustments at the specified value date using the market data provided
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curves/Surface
	 * 
	 * @return The List of Convexity Adjustments
	 */

	public java.util.List<org.drip.analytics.output.ConvexityAdjustment> convexityAdjustment (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		org.drip.state.identifier.CreditLabel creditLabel = creditLabel();

		org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel();

		org.drip.state.identifier.FundingLabel fundingLabel = fundingLabel();

		org.drip.state.identifier.FXLabel fxLabel = fxLabel();

		double dblCreditFXConvexityAdjustment = 1.;
		double dblFundingFXConvexityAdjustment = 1.;
		org.drip.quant.function1D.AbstractUnivariate auFXVol = null;
		org.drip.quant.function1D.AbstractUnivariate auCreditVol = null;
		org.drip.quant.function1D.AbstractUnivariate auForwardVol = null;
		org.drip.quant.function1D.AbstractUnivariate auFundingVol = null;
		org.drip.quant.function1D.AbstractUnivariate auCreditFXCorr = null;
		org.drip.quant.function1D.AbstractUnivariate auForwardFXCorr = null;
		org.drip.quant.function1D.AbstractUnivariate auFundingFXCorr = null;
		org.drip.quant.function1D.AbstractUnivariate auCreditForwardCorr = null;
		org.drip.quant.function1D.AbstractUnivariate auCreditFundingCorr = null;
		org.drip.quant.function1D.AbstractUnivariate auForwardFundingCorr = null;

		if (null != csqs) {
			auCreditVol = csqs.creditCurveVolSurface (creditLabel);

			auForwardVol = csqs.forwardCurveVolSurface (forwardLabel);

			auFundingVol = csqs.fundingCurveVolSurface (fundingLabel);

			auFXVol = csqs.fxCurveVolSurface (fxLabel);

			auCreditForwardCorr = csqs.creditForwardCorrSurface (creditLabel, forwardLabel);

			auCreditFundingCorr = csqs.creditFundingCorrSurface (creditLabel, fundingLabel);

			auCreditFXCorr = csqs.creditFXCorrSurface (creditLabel, fxLabel);

			auForwardFundingCorr = csqs.forwardFundingCorrSurface (forwardLabel, fundingLabel);

			auForwardFXCorr = csqs.forwardFXCorrSurface (forwardLabel, fxLabel);

			auFundingFXCorr = csqs.fundingFXCorrSurface (fundingLabel, fxLabel);
		}

		try {
			double dblCreditFundingConvexityAdjustment = java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auCreditVol, auFundingVol,
					auCreditFundingCorr, dblValueDate, _dblPayDate));

			if (isFXMTM()) {
				dblCreditFXConvexityAdjustment = java.lang.Math.exp
					(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auCreditVol, auFXVol,
						auCreditFXCorr, dblValueDate, _dblPayDate));

				dblFundingFXConvexityAdjustment = java.lang.Math.exp
					(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auFundingVol, auFXVol,
						auFundingFXCorr, dblValueDate, _dblPayDate));
			}

			java.util.List<org.drip.analytics.output.ConvexityAdjustment> lsConvAdj = new
				java.util.ArrayList<org.drip.analytics.output.ConvexityAdjustment>();

			for (org.drip.analytics.cashflow.ComposablePeriod cp : _lsComposablePeriod) {
				org.drip.analytics.output.ConvexityAdjustment convAdj = new
						org.drip.analytics.output.ConvexityAdjustment();

				if (!convAdj.setCreditFunding (dblCreditFundingConvexityAdjustment)) return null;

				if (!convAdj.setCreditFX (dblCreditFXConvexityAdjustment)) return null;

				if (!convAdj.setFundingFX (dblFundingFXConvexityAdjustment)) return null;

				if (null != forwardLabel) {
					if (!(cp instanceof org.drip.analytics.cashflow.ComposableFloatingPeriod)) return null;

					double dblFixingDate = ((org.drip.analytics.cashflow.ComposableFloatingPeriod)
						cp).referenceIndexPeriod().fixingDate();

					if (null != csqs && dblValueDate > dblFixingDate) {
						if (!convAdj.setCreditForward (java.lang.Math.exp
							(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auCreditVol,
								auForwardVol, auCreditForwardCorr, dblValueDate, dblFixingDate))))
							return null;

						if (!convAdj.setForwardFunding (java.lang.Math.exp
							(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auForwardVol,
								auFundingVol, auForwardFundingCorr, dblValueDate, dblFixingDate))))
							return null;

						if (isFXMTM() && !convAdj.setForwardFX (java.lang.Math.exp
							(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auForwardVol,
								auFXVol, auForwardFXCorr, dblValueDate, dblFixingDate))))
							return null;
					}
				}

				lsConvAdj.add (convAdj);
			}

			return lsConvAdj;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Compute the Period Coupon Measures
	 * 
	 * @param dblValueDate Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * 
	 * @return The Period Coupon Measures
	 */

	public org.drip.analytics.output.ComposedPeriodMetrics couponMetrics (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValueDate)) return null;

		org.drip.analytics.definition.CreditCurve cc = null == csqs ? null : csqs.creditCurve (_creditLabel);

		org.drip.analytics.rates.DiscountCurve dcFunding = null == csqs ? null : csqs.fundingCurve
			(fundingLabel());

		double dblDF = 1.;
		double dblSurvivalProbability = 1.;
		double dblFX = java.lang.Double.NaN;
		double dblNotional = java.lang.Double.NaN;

		try {
			dblFX = fx (csqs);

			dblNotional = notional (_dblPayDate);

			dblDF = null == dcFunding ? 1. : dcFunding.df (_dblPayDate);

			dblSurvivalProbability = null == cc ? 1. : cc.survival (_dblPayDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		java.util.List<org.drip.analytics.output.ConvexityAdjustment> lsConvAdj = convexityAdjustment
			(dblValueDate, csqs);

		java.util.List<org.drip.analytics.output.ComposablePeriodMetrics> lsCPM = new
			java.util.ArrayList<org.drip.analytics.output.ComposablePeriodMetrics>();

		for (int i = 0; i < _lsComposablePeriod.size(); ++i) {
			org.drip.analytics.cashflow.ComposablePeriod cp = _lsComposablePeriod.get (i);

			try {
				lsCPM.add (new org.drip.analytics.output.ComposablePeriodMetrics (cp.fullCouponDCF(),
					cp.fullCouponRate (csqs), dblNotional, dblSurvivalProbability, dblDF, dblFX,
						lsConvAdj.get (i)));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return org.drip.analytics.output.ComposedPeriodMetrics.Create (lsCPM, _iAccrualCompoundingRule);
	}

	/**
	 * Compute the Coupon Accrual Measures to the specified Accrual End Date
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * 
	 * @return The Coupon Accrual Measures to the specified Accrual End Date
	 */

	public org.drip.analytics.output.ComposedPeriodMetrics accrualMetrics (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		java.util.List<org.drip.analytics.output.ComposablePeriodMetrics> lsCPM = new
			java.util.ArrayList<org.drip.analytics.output.ComposablePeriodMetrics>();

		try {
			if (!contains (dblValueDate)) return null;

			double dblFX = fx (csqs);

			double dblNotional = notional (_dblPayDate);

			for (org.drip.analytics.cashflow.ComposablePeriod cfp : _lsComposablePeriod) {
				int iDateLocation = cfp.dateLocation (dblValueDate);

				if (org.drip.analytics.cashflow.ComposableFixedPeriod.NODE_RIGHT_OF_SEGMENT == iDateLocation)
					break;

				if (org.drip.analytics.cashflow.ComposableFixedPeriod.NODE_INSIDE_SEGMENT == iDateLocation)
					lsCPM.add (new org.drip.analytics.output.ComposablePeriodMetrics (cfp.accrualDCF
						(dblValueDate), cfp.fullCouponRate (csqs), dblNotional, 1., 1., dblFX, new
							org.drip.analytics.output.ConvexityAdjustment()));
				else if (org.drip.analytics.cashflow.ComposableFixedPeriod.NODE_LEFT_OF_SEGMENT ==
					iDateLocation)
					lsCPM.add (new org.drip.analytics.output.ComposablePeriodMetrics (cfp.fullCouponDCF(),
						cfp.fullCouponRate (csqs), dblNotional, 1., 1., dblFX, new
							org.drip.analytics.output.ConvexityAdjustment()));
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return org.drip.analytics.output.ComposedPeriodMetrics.Create (lsCPM, _iAccrualCompoundingRule);
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

		double dblAccrued = java.lang.Double.NaN;

		org.drip.analytics.cashflow.ComposedPeriodQuoteSet cpqs = periodQuoteSet (pqs, csqs);

		try {
			dblAccrued = calibAccrued (cpqs, dblValueDate, csqs);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.analytics.output.ComposedPeriodMetrics cpm = couponMetrics (dblValueDate, csqs);

		if (null == cpm) return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		/* org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel();

		if (null == forwardLabel || !forwardLabel.match (pqs.forwardLabel())) {
			if (!prwc.updateValue (-1. * notional (_dblPayDate) * cpm.dcf() * fx (csqs) * (cpqs.baseRate() +
				cpqs.basis()) * cpm.survival() * cpm.df() *
					cpm.convAdj().cumulative()))
				return null;
		} else {
			java.util.List<org.drip.analytics.output.ComposablePeriodMetrics> lsCPM = cpm.composableMetrics();
		} */

		if (!prwc.updateValue (dblAccrued)) return null;

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

		org.drip.analytics.cashflow.ComposedPeriodQuoteSet cpqs = periodQuoteSet (pqs, csqs);

		if (null == cpqs) return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		/* org.drip.analytics.output.CouponMetrics cm = couponMetrics (dblValueDate, csqs);

		if (null == cm) return null;

		if (fundingLabel().match (pqs.fundingLabel())) {
			double dblDiscountFactorLoading = cm.notional() * cm.dcf() * (cpqs.baseRate() + cpqs.basis()) *
				cm.fx() * cm.survival() * cm.convAdj().cumulative();

			if (!prwc.addPredictorResponseWeight (_dblPayDate, dblDiscountFactorLoading)) return null;

			if (!prwc.addDResponseWeightDManifestMeasure ("PV", _dblPayDate, dblDiscountFactorLoading))
				return null;
		} else {
			if (!prwc.updateValue (-1. * cm.notional() * cm.dcf() * (cpqs.baseRate() + cpqs.basis()) *
				cm.fx() * cm.survival() * cm.df() * cm.convAdj().cumulative()))
				return null;
		}

		if (!prwc.updateValue (calibAccrued (cpqs, dblValueDate, csqs))) return null; */

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

	/**
	 * Retrieve the Period Calibration Quotes from the specified product quote set
	 * 
	 * @param pqs The Product Quote Set
	 * @param csqs The Market Curve Surface/Quote Set
	 * 
	 * @return The Composed Period Quote Set
	 */

	abstract public org.drip.analytics.cashflow.ComposedPeriodQuoteSet periodQuoteSet (
		final org.drip.product.calib.ProductQuoteSet pqs,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs);
}
