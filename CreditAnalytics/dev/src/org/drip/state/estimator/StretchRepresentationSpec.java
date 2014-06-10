
package org.drip.state.estimator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * StretchRepresentationSpec carries the calibration instruments and the corresponding calibration parameter
 *  set in LSMM instances. Together, these inputs are used for constructing an entire latent state stretch.
 *  
 * StretchRepresentationSpec exports the following functionality:
 * 	- Alternate ways of constructing custom Stretch representations
 * 	- Retrieve indexed instrument/LSMM
 * 	- Retrieve the full set calibratable instrument/LSMM
 *
 * @author Lakshmi Krishnamurthy
 */

public class StretchRepresentationSpec {
	private java.lang.String _strName = "";
	private java.lang.String _strLatentStateID = "";
	private java.lang.String _strLatentStateQuantificationMetric = "";
	private org.drip.analytics.rates.TurnListDiscountFactor _tldf = null;
	private org.drip.product.definition.CalibratableFixedIncomeComponent[] _aCalibComp = null;
	private java.util.List<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
		_lsMapManifestQuote = null;

	/**
	 * Make a StretchRepresentationSpec instance from the given components, quotes, and the measure.
	 * 
	 * @param strName Stretch Name
	 * @param strLatentStateID Latest State ID
	 * @param strLatentStateQuantificationMetric Latent State Quantifier Metric
	 * @param aCalibComp Array of the Calibration Components
	 * @param strManifestMeasure Component Manifest Measure
	 * @param adblQuote Array of the Manifest Measure Calibration Quotes
	 * @param tldf Turn List Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public static final StretchRepresentationSpec CreateStretchBuilderSet (
		final java.lang.String strName,
		final java.lang.String strLatentStateID,
		final java.lang.String strLatentStateQuantificationMetric,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp,
		final java.lang.String strManifestMeasure,
		final double[] adblQuote,
		final org.drip.analytics.rates.TurnListDiscountFactor tldf)
	{
		if (null == aCalibComp) return null;

		int iNumComp = aCalibComp.length;

		if (0 == iNumComp) return null;

		java.util.List<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			lsMapManifestQuote = new
				java.util.ArrayList<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		for (int i = 0; i < iNumComp; ++i) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapManifestQuote = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

			mapManifestQuote.put (strManifestMeasure, adblQuote[i]);

			lsMapManifestQuote.add (mapManifestQuote);
		}

		try {
			return new StretchRepresentationSpec (strName, strLatentStateID,
				strLatentStateQuantificationMetric, aCalibComp, lsMapManifestQuote, tldf);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct an instance of StretchRepresentationSpec for the Construction of the Forward Curve from the
	 * 	specified Inputs
	 * 
	 * @param strName Stretch Name
	 * @param strLatentStateID Latest State ID
	 * @param strLatentStateQuantificationMetric Latent State Quantifier Metric
	 * @param aCCSP Array of Calibration Cross Currency Swap Pair Instances
	 * @param valParams The Valuation Parameters
	 * @param bmp The Basket Market Parameters to imply the Market Quote Measure
	 * @param lsMapManifestQuoteIn Map of Calibration Measure/Quote Combination
	 * @param bOnDerivedSwap TRUE => Indicates that the basis is specified on the Derived Float-Float Swap
	 * 
	 * return Instance of StretchRepresentationSpec
	 */

	public static final StretchRepresentationSpec ForwardFromCCBS (
		final java.lang.String strName,
		final java.lang.String strLatentStateID,
		final java.lang.String strLatentStateQuantificationMetric,
		final org.drip.product.fx.CrossCurrencyComponentPair[] aCCSP,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.definition.BasketMarketParams bmp,
		final java.util.List<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			lsMapManifestQuoteIn,
		final boolean bOnDerivedSwap)
	{
		if (null == aCCSP || null == bmp) return null;

		int iNumCCSP = aCCSP.length;

		if (0 == iNumCCSP) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp = new
			org.drip.product.definition.CalibratableFixedIncomeComponent[iNumCCSP];

		java.util.List<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			lsMapManifestQuote = null != lsMapManifestQuoteIn && bOnDerivedSwap ? lsMapManifestQuoteIn : new
				java.util.ArrayList<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		for (int i = 0; i < iNumCCSP; ++i) {
			if (null == aCCSP[i]) return null;

			double dblFX = java.lang.Double.NaN;

			aCalibComp[i] = aCCSP[i].derivedComponent();

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapOP = aCCSP[i].value
				(valParams, null, bmp, null);

			java.lang.String strCompCalibPVMeasure = aCCSP[i].referenceComponent().componentName() + "[PV]";

			java.lang.String strCompCalibDerivedCleanDV01Measure =
				aCCSP[i].referenceComponent().componentName() + "[DerivedCleanDV01]";

			if (null == mapOP || !mapOP.containsKey (strCompCalibPVMeasure) || !mapOP.containsKey
				(strCompCalibDerivedCleanDV01Measure))
				return null;

			java.lang.String strFXCode = aCCSP[i].derivedComponent().couponCurrency()[0] + "/" +
				aCCSP[i].referenceComponent().couponCurrency()[0]; 

			org.drip.quant.function1D.AbstractUnivariate auFX = bmp.fxCurve (strFXCode);

			if (null == auFX) return null;

			try {
				dblFX = auFX.evaluate (aCCSP[i].derivedComponent().effective().getJulian());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapManifestQuote = null ==
				lsMapManifestQuote || iNumCCSP != lsMapManifestQuote.size() ? null : lsMapManifestQuote.get
					(i);

			double dblReferencePVInDerUnits = (mapOP.get (strCompCalibPVMeasure) + (bOnDerivedSwap || null ==
				mapManifestQuote ? 0. : mapOP.get (strCompCalibDerivedCleanDV01Measure) *
					mapManifestQuote.get ("DerivedParBasisSpread"))) * dblFX;

			if (null == mapManifestQuote || !bOnDerivedSwap) {
				(mapManifestQuote = new
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>()).put ("PV",
						dblReferencePVInDerUnits);

				lsMapManifestQuote.add (mapManifestQuote);
			} else
				mapManifestQuote.put ("PV", dblReferencePVInDerUnits);
		}

		try {
			return new StretchRepresentationSpec (strName, strLatentStateID,
				strLatentStateQuantificationMetric, aCalibComp, lsMapManifestQuote, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct an instance of StretchRepresentationSpec for the Construction of the Discount Curve from the
	 * 	specified Inputs
	 * 
	 * @param strName Stretch Name
	 * @param strLatentStateID Latest State ID
	 * @param strLatentStateQuantificationMetric Latent State Quantifier Metric
	 * @param aCCSP Array of Calibration Cross Currency Swap Pair Instances
	 * @param valParams The Valuation Parameters
	 * @param bmp The Basket Market Parameters to imply the Market Quote Measure
	 * @param lsCCBSMapManifestQuote Map of the CCBS Calibration Measure/Quote Combination
	 * @param lsIRSMapManifestQuote Map of the Fixed-Float Calibration Measure/Quote Combination
	 * 
	 * return Instance of StretchRepresentationSpec
	 */

	public static final StretchRepresentationSpec DiscountFromCCBS (
		final java.lang.String strName,
		final java.lang.String strLatentStateID,
		final java.lang.String strLatentStateQuantificationMetric,
		final org.drip.product.fx.CrossCurrencyComponentPair[] aCCSP,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.definition.BasketMarketParams bmp,
		final java.util.List<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			lsCCBSMapManifestQuote,
		final java.util.List<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			lsIRSMapManifestQuote)
	{
		if (null == aCCSP || null == bmp || null == lsCCBSMapManifestQuote || null == lsIRSMapManifestQuote)
			return null;

		int iNumCCSP = aCCSP.length;

		if (0 == iNumCCSP || iNumCCSP != lsCCBSMapManifestQuote.size() || iNumCCSP !=
			lsIRSMapManifestQuote.size())
			return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aDerivedComp = new
			org.drip.product.definition.CalibratableFixedIncomeComponent[iNumCCSP];

		for (int i = 0; i < iNumCCSP; ++i) {
			if (null == aCCSP[i]) return null;

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCCBSManifestQuote =
				lsCCBSMapManifestQuote.get (i);

			if (null == mapCCBSManifestQuote) return null;

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapIRSManifestQuote =
				lsIRSMapManifestQuote.get (i);

			if (null == mapIRSManifestQuote) return null;

			aDerivedComp[i] = aCCSP[i].derivedComponent();

			org.drip.product.definition.CalibratableFixedIncomeComponent refComp =
				aCCSP[i].referenceComponent();

			java.lang.String strRefCompName = refComp.componentName();

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapOP = aCCSP[i].value
				(valParams, null, bmp, null);

			java.lang.String strCompCalibPVMeasure = strRefCompName + "[Upfront]";
			java.lang.String strCompCalibDerivedCleanDV01Measure = strRefCompName + "[DerivedCleanDV01]";

			if (null == mapOP || !mapOP.containsKey (strCompCalibPVMeasure) || !mapOP.containsKey
				(strCompCalibDerivedCleanDV01Measure))
				return null;

			org.drip.quant.function1D.AbstractUnivariate auFX = bmp.fxCurve
				(aDerivedComp[i].couponCurrency()[0] + "/" + refComp.couponCurrency()[0]);

			if (null == auFX) return null;

			double dblFX = java.lang.Double.NaN;

			try {
				dblFX = auFX.evaluate (aDerivedComp[i].effective().getJulian());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			mapIRSManifestQuote.put ("Upfront", (mapOP.get (strCompCalibPVMeasure) + mapOP.get
				(strCompCalibDerivedCleanDV01Measure) * mapCCBSManifestQuote.get ("DerivedParBasisSpread")) *
					dblFX);
		}

		try {
			return new StretchRepresentationSpec (strName, strLatentStateID,
				strLatentStateQuantificationMetric, aDerivedComp, lsIRSMapManifestQuote, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * StretchRepresentationSpec constructor
	 * 
	 * @param strName Stretch Name
	 * @param strLatentStateID Latest State ID
	 * @param strLatentStateQuantificationMetric Latent State Quantifier Metric
	 * @param aCalibComp Array of the Calibration Components
	 * @param lsMapManifestQuote List of Component Manifest Measures/Quote Map
	 * @param tldf Turn List Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public StretchRepresentationSpec (
		final java.lang.String strName,
		final java.lang.String strLatentStateID,
		final java.lang.String strLatentStateQuantificationMetric,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp,
		final java.util.List<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			lsMapManifestQuote,
		final org.drip.analytics.rates.TurnListDiscountFactor tldf)
		throws java.lang.Exception
	{
		if (null == (_strName = strName) || _strName.isEmpty() || null == (_strLatentStateID =
			strLatentStateID) || _strLatentStateID.isEmpty() || null == (_strLatentStateQuantificationMetric
				= strLatentStateQuantificationMetric) || _strLatentStateQuantificationMetric.isEmpty() ||
					null == (_lsMapManifestQuote = lsMapManifestQuote) || null == (_aCalibComp = aCalibComp))
			throw new java.lang.Exception ("StretchRepresentationSpec ctr: Invalid Inputs");

		_tldf = tldf;
		int iNumComp = _aCalibComp.length;

		if (1 > iNumComp || _lsMapManifestQuote.size() != iNumComp)
			throw new java.lang.Exception ("StretchRepresentationSpec ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Stretch Name
	 * 
	 * @return The Stretch Name
	 */

	public java.lang.String getName()
	{
		return _strName;
	}

	/**
	 * Retrieve the Array of the Calibratable Components
	 * 
	 * @return The Array of the Calibratable Components
	 */

	public org.drip.product.definition.CalibratableFixedIncomeComponent[] getCalibComp()
	{
		return _aCalibComp;
	}

	/**
	 * Retrieve the Array of Latent State Metric Measures
	 * 
	 * @return The Array of Latent State Metric Measures
	 */

	public org.drip.state.representation.LatentStateMetricMeasure[] getLSMM()
	{
		int iNumQuote = _lsMapManifestQuote.size();

		org.drip.state.representation.LatentStateMetricMeasure[] aLSMM = new
			org.drip.state.representation.LatentStateMetricMeasure[iNumQuote];

		for (int i = 0; i < iNumQuote; ++i) {
			try {
				aLSMM[i] = new org.drip.analytics.rates.RatesLSMM (_strLatentStateID,
					_strLatentStateQuantificationMetric, _lsMapManifestQuote.get (i), _tldf);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return aLSMM;
	}

	/**
	 * Retrieve the LSMM corresponding to the given Instrument index
	 * 
	 * @param iIndex The Instrument index
	 * 
	 * @return The Instrument's LSMM
	 */

	public org.drip.state.representation.LatentStateMetricMeasure getLSMM (
		final int iIndex)
	{
		if (iIndex >= _aCalibComp.length) return null;

		try {
			return new org.drip.analytics.rates.RatesLSMM (_strLatentStateID,
				_strLatentStateQuantificationMetric, _lsMapManifestQuote.get (iIndex), _tldf);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the Calibration Component corresponding to the given Instrument index
	 * 
	 * @param iIndex The Instrument index
	 * 
	 * @return The Calibration Component
	 */

	public org.drip.product.definition.CalibratableFixedIncomeComponent getCalibComp (
		final int iIndex)
	{
		if (iIndex >= _aCalibComp.length) return null;

		return _aCalibComp[iIndex];
	}
}
