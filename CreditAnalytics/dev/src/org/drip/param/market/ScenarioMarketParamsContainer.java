
package org.drip.param.market;

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
 * ScenarioMarketParamsContainer extends MarketParams abstract class, and is the place holder for the
 *  comprehensive suite of the market set of curves for the given date. It exports the following
 *  functionality:
 * 	- add/remove/retrieve scenario discount curve
 * 	- add/remove/retrieve scenario Forward curve
 * 	- add/remove/retrieve scenario zero curve
 * 	- add/remove/retrieve scenario credit curve
 * 	- add/remove/retrieve scenario recovery curve
 * 	- add/remove/retrieve scenario FXForward curve
 * 	- add/remove/retrieve scenario FXBasis curve
 * 	- add/remove/retrieve scenario fixings
 * 	- add/remove/retrieve Treasury/component quotes
 * 	- retrieve scenario Market Parameters
 * 	- retrieve map of flat rates/credit/recovery Market Parameters
 * 	- retrieve double map of tenor rates/credit/recovery Market Parameters
 *  - retrieve rates/credit scenario generator
 *
 * @author Lakshmi Krishnamurthy
 */

public class ScenarioMarketParamsContainer extends org.drip.param.definition.ScenarioMarketParams {
	private static final int BASE = 0;
	private static final int BUMP_UP = 1;
	private static final int BUMP_DN = 2;
	private static final int RR_BUMP_UP = 4;
	private static final int RR_BUMP_DN = 8;

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
		_mapCQTSY = null;
	private java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> _mmFixings = null;

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioDiscountCurve>
		_mapIRCSC = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioDiscountCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioForwardCurve>
		_mapSFC = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioForwardCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioCreditCurve>
		_mapCCSC = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioCreditCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
		_mapCQComp = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
		_mapScenCSQS = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
		getDCSet (
			final int iBumpType)
	{
		if (null == _mapIRCSC.entrySet()) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve> mapDC =
			new org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioDiscountCurve> meDCSG :
			_mapIRCSC.entrySet()) {
			if (null != meDCSG.getKey() && null != meDCSG.getValue()) {
				if (BASE == iBumpType)
					mapDC.put (meDCSG.getKey(), meDCSG.getValue().getDCBase());
				else if (BUMP_UP == iBumpType)
					mapDC.put (meDCSG.getKey(), meDCSG.getValue().getDCBumpUp());
				else if (BUMP_DN == iBumpType)
					mapDC.put (meDCSG.getKey(), meDCSG.getValue().getDCBumpDn());
			}
		}

		return mapDC;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>
		getFCSet (
			final int iBumpType)
	{
		if (null == _mapSFC.entrySet()) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve> mapFC =
			new org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioForwardCurve> meSFC :
			_mapSFC.entrySet()) {
			if (null != meSFC.getKey() && null != meSFC.getValue()) {
				if (BASE == iBumpType)
					mapFC.put (meSFC.getKey(), meSFC.getValue().getFCBase());
				else if (BUMP_UP == iBumpType)
					mapFC.put (meSFC.getKey(), meSFC.getValue().getFCBumpUp());
				else if (BUMP_DN == iBumpType)
					mapFC.put (meSFC.getKey(), meSFC.getValue().getFCBumpDn());
			}
		}

		return mapFC;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve> getCCSet
		(final int iBumpType)
	{
		if (null == _mapCCSC.entrySet()) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve> mapCC = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioCreditCurve> meCCSG :
			_mapCCSC.entrySet()) {
			if (null != meCCSG.getKey() && null != meCCSG.getValue()) {
				if (BASE == iBumpType)
					mapCC.put (meCCSG.getKey(), meCCSG.getValue().getCCBase());
				else if (BUMP_UP == iBumpType)
					mapCC.put (meCCSG.getKey(), meCCSG.getValue().getCCBumpUp());
				else if (BUMP_DN == iBumpType)
					mapCC.put (meCCSG.getKey(), meCCSG.getValue().getCCBumpDn());
				else if (RR_BUMP_UP == iBumpType)
					mapCC.put (meCCSG.getKey(), meCCSG.getValue().getCCRecoveryUp());
				else if (RR_BUMP_DN == iBumpType)
					mapCC.put (meCCSG.getKey(), meCCSG.getValue().getCCRecoveryDn());
			}
		}

		return mapCC;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
		getSpecificIRFlatBumpDCSet (
			final java.lang.String strIRCurve,
			final boolean bBumpUp)
	{
		if (null == strIRCurve || strIRCurve.isEmpty() || null == _mapIRCSC.get (strIRCurve)) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve> mapDC =
			getDCSet (BASE);

		if (null == mapDC) return null;

		mapDC.put (strIRCurve, bBumpUp ? _mapIRCSC.get (strIRCurve).getDCBumpUp() : _mapIRCSC.get
			(strIRCurve).getDCBumpDn());

		return mapDC;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>
		getSpecificForwardFlatBumpFCSet (
			final java.lang.String strForwardCurve,
			final boolean bBumpUp)
	{
		if (null == strForwardCurve || strForwardCurve.isEmpty() || null == _mapSFC.get (strForwardCurve))
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve> mapFC =
			getFCSet (BASE);

		if (null == mapFC) return null;

		mapFC.put (strForwardCurve, bBumpUp ? _mapSFC.get (strForwardCurve).getFCBumpUp() : _mapSFC.get
			(strForwardCurve).getFCBumpDn());

		return mapFC;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
		getSpecificCreditFlatBumpCCSet (
			final java.lang.String strCreditCurve,
			final boolean bBumpUp)
	{
		if (null == strCreditCurve || strCreditCurve.isEmpty() || null == _mapCCSC.get (strCreditCurve))
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve> mapCC =
			getCCSet (BASE);

		if (null == mapCC || null == _mapCCSC.get (strCreditCurve)) return null;

		mapCC.put (strCreditCurve, bBumpUp ? _mapCCSC.get (strCreditCurve).getCCBumpUp() : _mapCCSC.get
			(strCreditCurve).getCCBumpDn());

		return mapCC;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
		getSpecificCreditFlatBumpRRSet (
			final java.lang.String strCreditCurve,
			final boolean bBumpUp)
	{
		if (null == strCreditCurve || strCreditCurve.isEmpty() || null == _mapCCSC.get (strCreditCurve))
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve> mapCC =
			getCCSet (BASE);

		if (null == mapCC || null == _mapCCSC.get (strCreditCurve)) return null;

		mapCC.put (strCreditCurve, bBumpUp ? _mapCCSC.get (strCreditCurve).getCCRecoveryUp() : _mapCCSC.get
			(strCreditCurve).getCCRecoveryDn());

		return mapCC;
	}

	private org.drip.param.market.CurveSurfaceQuoteSet customMarketParams (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
			mapDC,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve> mapFC,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
			mapCC)
	{
		org.drip.param.market.CurveSurfaceQuoteSet mp = null;

		try {
			mp = new org.drip.param.market.CurveSurfaceQuoteSet();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (null != mapDC && 0 != mapDC.size()) {
			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.DiscountCurve>
				meDC : mapDC.entrySet()) {
				if (null == meDC) continue;

				org.drip.analytics.rates.DiscountCurve dcFunding = meDC.getValue();

				if (null != dcFunding && !mp.setFundingCurve (dcFunding)) return null;
			}
		}

		if (null != mapFC && 0 != mapFC.size()) {
			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.ForwardCurve>
				meFC : mapFC.entrySet()) {
				if (null == meFC) continue;

				org.drip.analytics.rates.ForwardCurve fc = meFC.getValue();

				if (null != fc && !mp.setForwardCurve (fc)) return null;
			}
		}

		if (null != mapCC && 0 != mapCC.size()) {
			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.definition.CreditCurve>
				meCC : mapCC.entrySet()) {
				if (null == meCC) continue;

				org.drip.analytics.definition.CreditCurve cc = meCC.getValue();

				if (null != cc && !mp.setCreditCurve (cc)) return null;
			}
		}

		mp.setFixings (_mmFixings);

		return mp;
	}

	/**
	 * Construct an empty MarketParamsContainer instance
	 */

	public ScenarioMarketParamsContainer()
	{
	}

	@Override public boolean addScenDC (
		final java.lang.String strName,
		final org.drip.param.definition.ScenarioDiscountCurve irsg)
	{
		if (null != strName && !strName.isEmpty() && null != irsg) {
			_mapIRCSC.put (strName, irsg);

			return true;
		}

		return false;
	}

	@Override public boolean removeScenDC (
		final java.lang.String strName)
	{
		if (null != strName && !strName.isEmpty()) {
			_mapIRCSC.remove (strName);

			return true;
		}

		return false;
	}

	@Override public boolean addScenFC (
		final java.lang.String strName,
		final org.drip.param.definition.ScenarioForwardCurve sfc)
	{
		if (null != strName && !strName.isEmpty() && null != sfc) {
			_mapSFC.put (strName, sfc);

			return true;
		}

		return false;
	}

	@Override public boolean removeScenFC (
		final java.lang.String strName)
	{
		if (null != strName && !strName.isEmpty()) {
			_mapSFC.remove (strName);

			return true;
		}

		return false;
	}

	@Override public boolean addScenCC (
		final java.lang.String strName,
		final org.drip.param.definition.ScenarioCreditCurve ccsg)
	{
		if (null != strName && !strName.isEmpty() && null != ccsg) {
			_mapCCSC.put (strName, ccsg);

			return true;
		}

		return false;
	}

	@Override public boolean removeScenCC (
		final java.lang.String strName)
	{
		if (null != strName && !strName.isEmpty()) {
			_mapCCSC.remove (strName);

			return true;
		}

		return false;
	}

	@Override public boolean addTSYQuote (
		final java.lang.String strBenchmark,
		final org.drip.param.definition.ProductQuote cqTSY)
	{
		if (null == strBenchmark || strBenchmark.isEmpty() || null == cqTSY) return false;

		if (null == _mapCQTSY)
			_mapCQTSY = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>();

		_mapCQTSY.put (strBenchmark, cqTSY);

		return true;
	}

	@Override public boolean removeTSYQuote (
		final java.lang.String strBenchmark)
	{
		if (null == strBenchmark || strBenchmark.isEmpty()) return false;

		if (null == _mapCQTSY) return true;

		_mapCQTSY.remove (strBenchmark);

		return true;
	}

	@Override public boolean setTSYQuotes (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote> mapCQTSY)
	{
		_mapCQTSY = mapCQTSY;
		return true;
	}

	@Override public org.drip.param.definition.ProductQuote getTSYQuote (
		final java.lang.String strBenchmark)
	{
		if (null == _mapCQTSY || null == strBenchmark || strBenchmark.isEmpty()) return null;

		return _mapCQTSY.get (strBenchmark);
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
		getTSYQuotes()
	{
		return _mapCQTSY;
	}

	@Override public boolean addFixings (
		final org.drip.analytics.date.JulianDate dtFix,
		final java.lang.String strIndex,
		final double dblFixing)
	{
		if (null == dtFix || null == strIndex || strIndex.isEmpty() ||
			!org.drip.quant.common.NumberUtil.IsValid (dblFixing))
			return false;

		if (null == _mmFixings)
			_mmFixings = new java.util.HashMap<org.drip.analytics.date.JulianDate,
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mIndexFixings = _mmFixings.get
			(dtFix);

		if (null == mIndexFixings)
			mIndexFixings = new org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mIndexFixings.put (strIndex, dblFixing);

		_mmFixings.put (dtFix, mIndexFixings);

		return true;
	}

	@Override public boolean removeFixings (
		final org.drip.analytics.date.JulianDate dtFix,
		final java.lang.String strIndex)
	{
		if (null == dtFix || null == strIndex || strIndex.isEmpty()) return false;

		if (null == _mmFixings) return true;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mIndexFixings = _mmFixings.get
			(dtFix);

		if (null == mIndexFixings) return true;

		mIndexFixings.remove (strIndex);

		_mmFixings.put (dtFix, mIndexFixings);

		return true;
	}

	@Override public java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			getFixings()
	{
		return _mmFixings;
	}

	@Override public boolean addCompQuote (
		final java.lang.String strCompID,
		final org.drip.param.definition.ProductQuote cqComp)
	{
		if (null == strCompID || strCompID.isEmpty() || null == cqComp) return false;

		if (null == _mapCQComp)
			_mapCQComp = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>();

		_mapCQComp.put (strCompID, cqComp);

		return true;
	}

	@Override public boolean removeCompQuote (
		final java.lang.String strCompID)
	{
		if (null == strCompID || strCompID.isEmpty()) return false;

		if (null == _mapCQComp) return true;

		_mapCQComp.remove (strCompID);

		return true;
	}

	@Override public boolean addCompQuotes (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			mCompQuotes)
	{
		_mapCQComp = mCompQuotes;
		return true;
	}

	@Override public org.drip.param.definition.ProductQuote getCompQuote (
		final java.lang.String strCompID)
	{
		if (null == _mapCQComp || null == strCompID || strCompID.isEmpty()) return null;

		return _mapCQComp.get (strCompID);
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
		getCompQuotes()
	{
		return _mapCQComp;
	}

	@Override public boolean addScenMarketParams (
		final java.lang.String strScenarioName,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == strScenarioName || strScenarioName.isEmpty() || null == csqs) return false;

		_mapScenCSQS.put (strScenarioName, csqs);

		return true;
	}

	@Override public org.drip.param.market.CurveSurfaceQuoteSet getScenMarketParams (
		final java.lang.String strScenarioName)
	{
		if (null == strScenarioName || strScenarioName.isEmpty()) return null;

		return _mapScenCSQS.get (strScenarioName);
	}

	@Override public org.drip.param.market.CurveSurfaceQuoteSet getScenMarketParams (
		final org.drip.product.definition.FixedIncomeComponent comp,
		final java.lang.String strScen)
	{
		if (null == comp || null == strScen || strScen.isEmpty()) return null;

		org.drip.analytics.rates.ForwardCurve fc = null;
		org.drip.analytics.rates.DiscountCurve dc = null;
		org.drip.analytics.rates.DiscountCurve dcTSY = null;
		org.drip.analytics.definition.CreditCurve cc = null;

		if (null != comp.couponCurrency()[0] && null != _mapIRCSC.get (comp.couponCurrency()[0]))
			dc = _mapIRCSC.get (comp.couponCurrency()[0]).getDCBase();

		if (null != comp.forwardCurveName() && null != _mapSFC.get (comp.forwardCurveName()))
			fc = _mapSFC.get (comp.forwardCurveName()).getFCBase();

		if (null != comp.couponCurrency()[0] && null != _mapIRCSC.get (comp.couponCurrency()[0]))
			dcTSY = _mapIRCSC.get (comp.couponCurrency()[0]).getDCBase();

		if (null != comp.creditCurveName() && null != _mapCCSC.get (comp.creditCurveName()))
			cc = _mapCCSC.get (comp.creditCurveName()).getCCBase();

		if ("FlatIRBumpUp".equalsIgnoreCase (strScen) && null != comp.couponCurrency()[0] && null !=
			_mapIRCSC.get (comp.couponCurrency()[0]))
			dc = _mapIRCSC.get (comp.couponCurrency()[0]).getDCBumpUp();

		if ("FlatIRBumpDn".equalsIgnoreCase (strScen) && null != comp.couponCurrency()[0] && null !=
			_mapIRCSC.get (comp.couponCurrency()[0]))
			dc = _mapIRCSC.get (comp.couponCurrency()[0]).getDCBumpDn();

		if ("FlatForwardBumpUp".equalsIgnoreCase (strScen) && null != comp.forwardCurveName() && null !=
			_mapSFC.get (comp.forwardCurveName()))
			fc = _mapSFC.get (comp.forwardCurveName()).getFCBumpUp();

		if ("FlatForwardBumpDn".equalsIgnoreCase (strScen) && null != comp.forwardCurveName() && null !=
			_mapSFC.get (comp.forwardCurveName()))
			fc = _mapSFC.get (comp.forwardCurveName()).getFCBumpDn();

		if ("FlatCreditBumpUp".equalsIgnoreCase (strScen) && null != comp.creditCurveName() && null !=
			_mapCCSC.get (comp.creditCurveName()))
			cc = _mapCCSC.get (comp.creditCurveName()).getCCBumpUp();

		if ("FlatCreditBumpDn".equalsIgnoreCase (strScen) && null != comp.creditCurveName() && null !=
			_mapCCSC.get (comp.creditCurveName()))
			cc = _mapCCSC.get (comp.creditCurveName()).getCCBumpDn();

		return org.drip.param.creator.MarketParamsBuilder.Create (dc, fc,
			dcTSY, cc, comp.name(), _mapCQComp.get (comp.name()), _mapCQTSY, _mmFixings);
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			getIRTenorMarketParams (
				final org.drip.product.definition.FixedIncomeComponent comp,
				final boolean bBumpUp)
	{
		if (null == comp || null == comp.couponCurrency()[0] || null == _mapIRCSC.get
			(comp.couponCurrency()[0]))
			return null;

		if (bBumpUp && (null == _mapIRCSC.get (comp.couponCurrency()[0]).getTenorDCBumpUp() || null ==
			_mapIRCSC.get (comp.couponCurrency()[0]).getTenorDCBumpUp().entrySet()))
			return null;

		if (!bBumpUp && (null == _mapIRCSC.get (comp.couponCurrency()[0]).getTenorDCBumpDn() || null ==
			_mapIRCSC.get (comp.couponCurrency()[0]).getTenorDCBumpDn().entrySet()))
			return null;

		org.drip.analytics.rates.ForwardCurve fc = null;
		org.drip.analytics.definition.CreditCurve cc = null;
		org.drip.analytics.rates.DiscountCurve dcTSY = null;

		if (null != comp.forwardCurveName() && null != _mapSFC.get (comp.forwardCurveName()))
			fc = _mapSFC.get (comp.forwardCurveName()).getFCBase();

		if (null != comp.couponCurrency()[0] && null != _mapIRCSC.get (comp.couponCurrency()[0]))
			dcTSY = _mapIRCSC.get (comp.couponCurrency()[0]).getDCBase();

		if (null != comp.creditCurveName() && null != _mapCCSC.get (comp.creditCurveName()))
			cc = _mapCCSC.get (comp.creditCurveName()).getCCBase();

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet> mapCSQS
			= new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

		if (bBumpUp) {
			if (null == _mapIRCSC.get (comp.couponCurrency()[0]).getTenorDCBumpUp() || null == _mapIRCSC.get
				(comp.couponCurrency()[0]).getTenorDCBumpUp().entrySet())
				return null;

			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.DiscountCurve> meDC :
				_mapIRCSC.get (comp.couponCurrency()[0]).getTenorDCBumpUp().entrySet()) {
				if (null == meDC || null == meDC.getKey() || meDC.getKey().isEmpty()) continue;

				mapCSQS.put (meDC.getKey(), org.drip.param.creator.MarketParamsBuilder.Create
					(meDC.getValue(), fc, dcTSY, cc, comp.name(), _mapCQComp.get (comp.name()), _mapCQTSY,
						_mmFixings));
			}
		} else {
			if (null == _mapIRCSC.get (comp.couponCurrency()[0]).getTenorDCBumpDn() || null == _mapIRCSC.get
				(comp.couponCurrency()[0]).getTenorDCBumpDn().entrySet())
				return null;

			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.DiscountCurve> meDC :
				_mapIRCSC.get (comp.couponCurrency()[0]).getTenorDCBumpDn().entrySet()) {
				if (null == meDC || null == meDC.getKey() || meDC.getKey().isEmpty()) continue;

				mapCSQS.put (meDC.getKey(), org.drip.param.creator.MarketParamsBuilder.Create
					(meDC.getValue(), fc, dcTSY, cc, comp.name(), _mapCQComp.get (comp.name()), _mapCQTSY,
						_mmFixings));
			}
		}

		return mapCSQS;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			getForwardTenorMarketParams (
				final org.drip.product.definition.FixedIncomeComponent comp,
				final boolean bBumpUp)
	{
		if (null == comp || null == comp.couponCurrency()[0] || null == _mapIRCSC.get
			(comp.couponCurrency()[0]))
			return null;

		if (bBumpUp && (null == _mapSFC.get (comp.forwardCurveName()).getTenorFCBumpUp() || null ==
			_mapSFC.get (comp.forwardCurveName()).getTenorFCBumpUp().entrySet()))
			return null;

		if (!bBumpUp && (null == _mapSFC.get (comp.forwardCurveName()).getTenorFCBumpDn() || null ==
			_mapSFC.get (comp.forwardCurveName()).getTenorFCBumpDn().entrySet()))
			return null;

		org.drip.analytics.rates.DiscountCurve dc = null;
		org.drip.analytics.definition.CreditCurve cc = null;
		org.drip.analytics.rates.DiscountCurve dcTSY = null;

		if (null != comp.couponCurrency()[0] && null != _mapIRCSC.get (comp.couponCurrency()[0]))
			dc = _mapIRCSC.get (comp.couponCurrency()[0]).getDCBase();

		if (null != comp.couponCurrency()[0] && null != _mapIRCSC.get (comp.couponCurrency()[0]))
			dcTSY = _mapIRCSC.get (comp.couponCurrency()[0]).getDCBase();

		if (null != comp.creditCurveName() && null != _mapCCSC.get (comp.creditCurveName()))
			cc = _mapCCSC.get (comp.creditCurveName()).getCCBase();

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet> mapCSQS
			= new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

		if (bBumpUp) {
			if (null == _mapSFC.get (comp.forwardCurveName()).getTenorFCBumpUp() || null == _mapSFC.get
				(comp.forwardCurveName()).getTenorFCBumpUp().entrySet())
				return null;

			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.ForwardCurve> meFC :
				_mapSFC.get (comp.forwardCurveName()).getTenorFCBumpUp().entrySet()) {
				if (null == meFC || null == meFC.getKey() || meFC.getKey().isEmpty()) continue;

				mapCSQS.put (meFC.getKey(), org.drip.param.creator.MarketParamsBuilder.Create (dc,
					meFC.getValue(), dcTSY, cc, comp.name(), _mapCQComp.get (comp.name()), _mapCQTSY,
						_mmFixings));
			}
		} else {
			if (null == _mapSFC.get (comp.forwardCurveName()).getTenorFCBumpDn() || null == _mapSFC.get
				(comp.forwardCurveName()).getTenorFCBumpDn().entrySet())
				return null;

			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.ForwardCurve> meFC :
				_mapSFC.get (comp.forwardCurveName()).getTenorFCBumpDn().entrySet()) {
				if (null == meFC || null == meFC.getKey() || meFC.getKey().isEmpty()) continue;

				mapCSQS.put (meFC.getKey(), org.drip.param.creator.MarketParamsBuilder.Create (dc,
					meFC.getValue(), dcTSY, cc, comp.name(), _mapCQComp.get (comp.name()), _mapCQTSY,
						_mmFixings));
			}
		}

		return mapCSQS;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			getCreditTenorMarketParams (
				final org.drip.product.definition.FixedIncomeComponent comp,
				final boolean bBumpUp)
	{
		if (null == comp || null == comp.creditCurveName() || null == _mapCCSC.get (comp.creditCurveName()))
			return null;

		if (bBumpUp && (null == _mapCCSC.get (comp.creditCurveName()).getTenorCCBumpUp() || null ==
			_mapCCSC.get (comp.creditCurveName()).getTenorCCBumpUp().entrySet()))
			return null;

		if (!bBumpUp && (null == _mapCCSC.get (comp.creditCurveName()).getTenorCCBumpDn() || null ==
			_mapCCSC.get (comp.creditCurveName()).getTenorCCBumpDn().entrySet()))
			return null;

		org.drip.analytics.rates.ForwardCurve fc = null;
		org.drip.analytics.rates.DiscountCurve dc = null;
		org.drip.analytics.rates.DiscountCurve dcTSY = null;

		if (null != comp.couponCurrency()[0] && null != _mapIRCSC.get (comp.couponCurrency()[0]))
			dc = _mapIRCSC.get (comp.couponCurrency()[0]).getDCBase();

		if (null != comp.forwardCurveName() && null != _mapSFC.get (comp.forwardCurveName()))
			fc = _mapSFC.get (comp.forwardCurveName()).getFCBase();

		if (null != comp.couponCurrency()[0] && null != _mapIRCSC.get (comp.couponCurrency()[0]))
			dcTSY = _mapIRCSC.get (comp.couponCurrency()[0]).getDCBase();

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet> mapCSQS
			= new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

		if (bBumpUp) {
			if (null == _mapCCSC.get (comp.creditCurveName()).getTenorCCBumpUp() || null == _mapCCSC.get
				(comp.creditCurveName()).getTenorCCBumpUp().entrySet())
				return null;

			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.definition.CreditCurve> meCC :
				_mapCCSC.get (comp.creditCurveName()).getTenorCCBumpUp().entrySet()) {
				if (null == meCC || null == meCC.getKey() || meCC.getKey().isEmpty()) continue;

				mapCSQS.put (meCC.getKey(), org.drip.param.creator.MarketParamsBuilder.Create (dc, fc, dcTSY,
					meCC.getValue(), comp.name(), _mapCQComp.get (comp.name()), _mapCQTSY, _mmFixings));
			}
		} else {
			if (null == _mapCCSC.get (comp.creditCurveName()).getTenorCCBumpDn() || null == _mapCCSC.get
				(comp.creditCurveName()).getTenorCCBumpDn().entrySet())
				return null;

			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.definition.CreditCurve> meCC :
				_mapCCSC.get (comp.creditCurveName()).getTenorCCBumpDn().entrySet()) {
				if (null == meCC || null == meCC.getKey() || meCC.getKey().isEmpty()) continue;

				mapCSQS.put (meCC.getKey(), org.drip.param.creator.MarketParamsBuilder.Create (dc, fc, dcTSY,
					meCC.getValue(), comp.name(), _mapCQComp.get (comp.name()), _mapCQTSY, _mmFixings));
			}
		}

		return mapCSQS;
	}

	@Override public org.drip.param.market.CurveSurfaceQuoteSet getScenMarketParams (
		final org.drip.product.definition.BasketProduct bp,
		final java.lang.String strScen)
	{
		if (null == strScen) return null;

		if ("Base".equalsIgnoreCase (strScen))
			return customMarketParams (getDCSet (BASE), getFCSet (BASE), getCCSet (BASE));

		if ("FlatIRBumpUp".equalsIgnoreCase (strScen))
			return customMarketParams (getDCSet (BUMP_UP), getFCSet (BASE), getCCSet (BASE));

		if ("FlatIRBumpDn".equalsIgnoreCase (strScen))
			return customMarketParams (getDCSet (BUMP_DN), getFCSet (BASE), getCCSet (BASE));

		if ("FlatForwardBumpUp".equalsIgnoreCase (strScen))
			return customMarketParams (getDCSet (BASE), getFCSet (BUMP_UP), getCCSet (BASE));

		if ("FlatForwardBumpDn".equalsIgnoreCase (strScen))
			return customMarketParams (getDCSet (BASE), getFCSet (BUMP_DN), getCCSet (BASE));

		if ("FlatCreditBumpUp".equalsIgnoreCase (strScen))
			return customMarketParams (getDCSet (BASE), getFCSet (BASE), getCCSet (BUMP_UP));

		if ("FlatCreditBumpDn".equalsIgnoreCase (strScen))
			return customMarketParams (getDCSet (BASE), getFCSet (BASE), getCCSet (BUMP_DN));

		if ("FlatRRBumpUp".equalsIgnoreCase (strScen))
			return customMarketParams (getDCSet (BASE), getFCSet (BASE), getCCSet (RR_BUMP_UP));

		if ("FlatRRBumpDn".equalsIgnoreCase (strScen))
			return customMarketParams (getDCSet (BASE), getFCSet (BASE), getCCSet (RR_BUMP_DN));

		return null;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			dcFlatBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapMP = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioDiscountCurve> meDCSG :
			_mapIRCSC.entrySet()) {
			if (null != meDCSG) {
				java.lang.String strKey = meDCSG.getKey();

				if (null != strKey && !strKey.isEmpty())
					mapMP.put (strKey, customMarketParams (getSpecificIRFlatBumpDCSet (strKey, bBump),
						getFCSet (BASE), getCCSet (BASE)));
			}
		}

		return mapMP;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			forwardFlatBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapMP = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioDiscountCurve> meDCSG :
			_mapIRCSC.entrySet()) {
			if (null != meDCSG) {
				java.lang.String strKey = meDCSG.getKey();

				if (null != strKey && !strKey.isEmpty())
					mapMP.put (strKey, customMarketParams (getDCSet (BASE), getSpecificForwardFlatBumpFCSet
						(strKey, bBump), getCCSet (BASE)));
			}
		}

		return mapMP;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			creditFlatBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapMP = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioCreditCurve> meCCSG :
			_mapCCSC.entrySet()) {
			if (null != meCCSG) {
				java.lang.String strKey = meCCSG.getKey();

				if (null != strKey && !strKey.isEmpty())
					mapMP.put (strKey, customMarketParams (getDCSet (BASE), getFCSet (BASE),
						getSpecificCreditFlatBumpCCSet (strKey, bBump)));
			}
		}

		return mapMP;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			recoveryFlatBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapMP = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioCreditCurve> meCCSG :
			_mapCCSC.entrySet()) {
			if (null != meCCSG) {
				java.lang.String strKey = meCCSG.getKey();

				if (null != strKey && !strKey.isEmpty())
					mapMP.put (strKey, customMarketParams (getDCSet (BASE), getFCSet (BASE),
						getSpecificCreditFlatBumpRRSet (strKey, bBump)));
			}
		}

		return mapMP;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>>
			dcTenorBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump)
	{
		if (null == bp) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>>
			mmIRTenorCSQS = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioDiscountCurve> meDCSG :
			_mapIRCSC.entrySet()) {
			if (null == meDCSG) continue;

			if (bBump && (null == meDCSG.getValue() || null == meDCSG.getValue().getTenorDCBumpUp() || null
				== meDCSG.getValue().getTenorDCBumpUp().entrySet()))
				return null;

			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
				mapTenorCSQS = new
					org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.DiscountCurve> meDC :
				(bBump ? meDCSG.getValue().getTenorDCBumpUp().entrySet() :
					meDCSG.getValue().getTenorDCBumpDn().entrySet())) {
				if (null == meDC || null == meDCSG.getKey() || meDCSG.getKey().isEmpty()) continue;

				org.drip.param.market.CurveSurfaceQuoteSet csqs = getScenMarketParams (bp, "Base");

				if (null != csqs) {
					csqs.setFundingCurve (meDC.getValue());

					mapTenorCSQS.put (meDC.getKey(), csqs);
				}
			}

			mmIRTenorCSQS.put (meDCSG.getKey(), mapTenorCSQS);
		}

		return mmIRTenorCSQS;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>>
			creditTenorBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump)
	{
		if (null == bp) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>>
			mmCreditTenorCSQS = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioCreditCurve> meCCSG :
			_mapCCSC.entrySet()) {
			if (null == meCCSG) continue;

			if (bBump && (null == meCCSG.getValue() || null == meCCSG.getValue().getTenorCCBumpUp() || null
				== meCCSG.getValue().getTenorCCBumpUp().entrySet()))
				return null;

			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
				mapTenorCSQS = new
					org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.definition.CreditCurve> meCC :
				(bBump ? meCCSG.getValue().getTenorCCBumpUp().entrySet() :
					meCCSG.getValue().getTenorCCBumpDn().entrySet())) {
				if (null == meCC || null == meCCSG.getKey() || meCCSG.getKey().isEmpty()) continue;

				org.drip.param.market.CurveSurfaceQuoteSet csqs = getScenMarketParams (bp, "Base");

				if (null != csqs) {
					csqs.setCreditCurve (meCC.getValue());

					mapTenorCSQS.put (meCC.getKey(), csqs);
				}
			}

			mmCreditTenorCSQS.put (meCCSG.getKey(), mapTenorCSQS);
		}

		return mmCreditTenorCSQS;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioDiscountCurve>
			irsg()
	{
		return _mapIRCSC;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioCreditCurve>
			ccsg()
	{
		return _mapCCSC;
	}
}
