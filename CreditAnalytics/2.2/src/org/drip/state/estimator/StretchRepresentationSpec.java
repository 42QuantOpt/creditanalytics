
package org.drip.state.estimator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
	private double[] _adblQuote = null;
	private java.lang.String _strName = "";
	private java.lang.String _strLatentStateID = "";
	private java.lang.String[] _astrManifestMeasure = null;
	private java.lang.String _strLatentStateQuantificationMetric = "";
	private org.drip.analytics.rates.TurnListDiscountFactor _tldf = null;
	private org.drip.product.definition.CalibratableComponent[] _aCalibComp = null;

	/**
	 * Make a StretchRepresentationSpec instance from the given components, quotes, and the measure.
	 * 
	 * @param strName Stretch Name
	 * @param strLatentStateID Latest State ID
	 * @param strLatentStateQuantificationMetric Latent State Quantifier Metric
	 * @param aCalibComp Array of the Calibration Components
	 * @param strManifestMeasure Product Manifest Measures
	 * @param adblQuote Array of Quotes
	 * @param tldf Turn List Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public static final StretchRepresentationSpec CreateStretchBuilderSet (
		final java.lang.String strName,
		final java.lang.String strLatentStateID,
		final java.lang.String strLatentStateQuantificationMetric,
		final org.drip.product.definition.CalibratableComponent[] aCalibComp,
		final java.lang.String strManifestMeasure,
		final double[] adblQuote,
		final org.drip.analytics.rates.TurnListDiscountFactor tldf)
	{
		if (null == aCalibComp) return null;

		int iNumComp = aCalibComp.length;
		java.lang.String[] astrManifestMeasure = new java.lang.String[iNumComp];

		if (0 == iNumComp) return null;

		for (int i = 0; i < iNumComp; ++i)
			astrManifestMeasure[i] = strManifestMeasure;

		try {
			return new StretchRepresentationSpec (strName, strLatentStateID,
				strLatentStateQuantificationMetric, aCalibComp, astrManifestMeasure, adblQuote, tldf);
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
	 * @param astrManifestMeasure Array of the Product Manifest Measures
	 * @param adblQuote Array of Quotes
	 * @param tldf Turn List Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public StretchRepresentationSpec (
		final java.lang.String strName,
		final java.lang.String strLatentStateID,
		final java.lang.String strLatentStateQuantificationMetric,
		final org.drip.product.definition.CalibratableComponent[] aCalibComp,
		final java.lang.String[] astrManifestMeasure,
		final double[] adblQuote,
		final org.drip.analytics.rates.TurnListDiscountFactor tldf)
		throws java.lang.Exception
	{
		if (null == (_strName = strName) || _strName.isEmpty() || null == (_strLatentStateID =
			strLatentStateID) || _strLatentStateID.isEmpty() || null == (_strLatentStateQuantificationMetric
				= strLatentStateQuantificationMetric) || _strLatentStateQuantificationMetric.isEmpty() ||
					null == (_aCalibComp = aCalibComp) || null == (_astrManifestMeasure =
						astrManifestMeasure) || null == (_adblQuote = adblQuote))
			throw new java.lang.Exception ("StretchRepresentationSpec ctr: Invalid Inputs");

		_tldf = tldf;
		int iNumComp = _aCalibComp.length;

		if (1 > iNumComp || astrManifestMeasure.length != iNumComp || adblQuote.length != iNumComp)
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

	public org.drip.product.definition.CalibratableComponent[] getCalibComp()
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
		int iNumQuote = _adblQuote.length;
		org.drip.state.representation.LatentStateMetricMeasure[] aLSMM = new
			org.drip.state.representation.LatentStateMetricMeasure[iNumQuote];

		for (int i = 0; i < iNumQuote; ++i) {
			try {
				aLSMM[i] = new org.drip.analytics.rates.RatesLSMM (_strLatentStateID,
					_strLatentStateQuantificationMetric, _astrManifestMeasure[i], _adblQuote[i], _tldf);
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
				_strLatentStateQuantificationMetric, _astrManifestMeasure[iIndex], _adblQuote[iIndex],
					_tldf);
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

	public org.drip.product.definition.CalibratableComponent getCalibComp (
		final int iIndex)
	{
		if (iIndex >= _aCalibComp.length) return null;

		return _aCalibComp[iIndex];
	}
}
