
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
 * ComponentMarketParamSet provides implementation of the ComponentMarketParamsRef interface. It serves as a
 *  place holder for the market parameters needed to value the component object � discount curve, forward
 *  curve, treasury curve, credit curve, component quote, treasury quote map, and fixings map.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComponentMarketParamSet extends org.drip.param.definition.ComponentMarketParams {
	private org.drip.analytics.definition.CreditCurve _cc = null;
	private org.drip.analytics.rates.DiscountCurve _dcTSY = null;
	private org.drip.analytics.rates.DiscountCurve _dcFunding = null;
	private org.drip.param.definition.ComponentQuote _compQuote = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUFX = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
		_mTSYQuotes = null;
	private java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> _mmFixings = null;

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>
		_mapForward = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>();

	private
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.quant.function1D.AbstractUnivariate>> _mapLatentStateForwardVolatility = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.quant.function1D.AbstractUnivariate>>();

	private
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>>
			_mapPayCurrencyForeignCollateralDC = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>>();

	/**
	 * Empty ComponentMarketParamSet Constructor
	 */

	public ComponentMarketParamSet()
	{
	}

	/**
	 * ComponentMarketParamSet de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if CreditCurve cannot be properly de-serialized
	 */

	public ComponentMarketParamSet (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("ComponentMarketParamSet de-serializer: Empty state");

		java.lang.String strSerializedComponentMarketParams = strRawString.substring (0, strRawString.indexOf
			(getObjectTrailer()));

		if (null == strSerializedComponentMarketParams || strSerializedComponentMarketParams.isEmpty())
			throw new java.lang.Exception ("ComponentMarketParamSet de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split
			(strSerializedComponentMarketParams, getFieldDelimiter());

		if (null == astrField || 8 > astrField.length)
			throw new java.lang.Exception ("ComponentMarketParamSet de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Cannot locate credit curve");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_cc = org.drip.state.creator.CreditCurveBuilder.FromByteArray (astrField[1].getBytes());

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Cannot locate discount curve");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			_dcFunding = org.drip.state.creator.DiscountCurveBuilder.FromByteArray (astrField[2].getBytes(),
				org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Cannot locate forward curve");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			_mapForward = null;

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Cannot locate TSY discount curve");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			_dcTSY = org.drip.state.creator.DiscountCurveBuilder.FromByteArray (astrField[4].getBytes(),
				org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		if (null == astrField[5] || astrField[5].isEmpty())
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Cannot locate component quote");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			_compQuote = new org.drip.param.market.ComponentMultiMeasureQuote (astrField[5].getBytes());

		if (null == astrField[6] || astrField[6].isEmpty())
			throw new java.lang.Exception ("ComponentMarketParamSet de-serializer: Cannot locate fixings");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6])) {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[6],
				getCollectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty()) continue;

					java.lang.String[] astrKVPair = org.drip.quant.common.StringUtil.Split (astrRecord[i],
						getCollectionKeyValueDelimiter());
					
					if (null == astrKVPair || 2 != astrKVPair.length || null == astrKVPair[0] ||
						astrKVPair[0].isEmpty() ||
							org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
								(astrKVPair[0]) || null == astrKVPair[1] || astrKVPair[1].isEmpty() ||
									org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
										(astrKVPair[1]))
						continue;

					java.lang.String[] astrKeySet = org.drip.quant.common.StringUtil.Split (astrKVPair[0],
						getCollectionMultiLevelKeyDelimiter());

					if (null == astrKeySet || 2 != astrKeySet.length || null == astrKeySet[0] ||
						astrKeySet[0].isEmpty() ||
							org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
								(astrKeySet[0]) || null == astrKeySet[1] || astrKeySet[1].isEmpty() ||
									org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
										(astrKeySet[1]))
						continue;

					if (null == _mmFixings)
						_mmFixings = new java.util.HashMap<org.drip.analytics.date.JulianDate,
							org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> map2D = _mmFixings.get
						(astrKeySet[0]);

					if (null == map2D)
						map2D = new org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

					map2D.put (astrKeySet[1], new java.lang.Double (astrKVPair[1]));

					_mmFixings.put (new org.drip.analytics.date.JulianDate (new java.lang.Double
						(astrKeySet[0])), map2D);
				}
			}
		}

		if (null == astrField[7] || astrField[7].isEmpty())
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Cannot locate TSY quotes");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7])) {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[7],
				getCollectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty() ||
						org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrRecord[i]))
						continue;

					java.lang.String[] astrKVPair = org.drip.quant.common.StringUtil.Split (astrRecord[i],
						getCollectionKeyValueDelimiter());
				
					if (null == astrKVPair || 2 != astrKVPair.length || null == astrKVPair[0] ||
						astrKVPair[0].isEmpty() ||
							org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
								(astrKVPair[0]) || null == astrKVPair[1] || astrKVPair[1].isEmpty() ||
									org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
										(astrKVPair[1]))
						continue;

					if (null == _mTSYQuotes)
						_mTSYQuotes = new
							org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>();

					_mTSYQuotes.put (astrKVPair[0], new org.drip.param.market.ComponentMultiMeasureQuote
						(astrKVPair[1].getBytes()));
				}
			}
		}
	}

	@Override public java.lang.String getCollectionKeyValueDelimiter()
	{
		return "]";
	}

	@Override public java.lang.String getFieldDelimiter()
	{
		return "[";
	}

	@Override public java.lang.String getObjectTrailer()
	{
		return "~";
	}

	@Override public org.drip.analytics.rates.DiscountCurve fundingCurve()
	{
		return _dcFunding;
	}

	@Override public boolean setFundingCurve (
		final org.drip.analytics.rates.DiscountCurve dcFunding)
	{
		if (null == dcFunding) return false;

		_dcFunding = dcFunding;
		return true;
	}

	@Override public org.drip.analytics.rates.DiscountCurve payCurrencyCollateralCurrencyCurve (
		final java.lang.String strPayCurrency,
		final java.lang.String strCollateralCurrency)
	{
		if (null == strPayCurrency || !_mapPayCurrencyForeignCollateralDC.containsKey (strPayCurrency) ||
			null == strCollateralCurrency)
			return null;

		return _mapPayCurrencyForeignCollateralDC.get (strPayCurrency).get (strCollateralCurrency);
	}

	@Override public boolean setPayCurrencyCollateralCurrencyCurve (
		final java.lang.String strPayCurrency,
		final java.lang.String strCollateralCurrency,
		final org.drip.analytics.rates.DiscountCurve dcPayCurrencyCollateralCurrency)
	{
		if (null == strPayCurrency || strPayCurrency.isEmpty() || null == strCollateralCurrency ||
			strCollateralCurrency.isEmpty() || null == dcPayCurrencyCollateralCurrency)
			return false;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
			mapCollateralCurrencyDC = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

		mapCollateralCurrencyDC.put (strCollateralCurrency, dcPayCurrencyCollateralCurrency);

		_mapPayCurrencyForeignCollateralDC.put (strPayCurrency, mapCollateralCurrencyDC);

		return true;
	}

	@Override public org.drip.analytics.rates.DiscountCurve collateralChoiceDiscountCurve (
		final java.lang.String strPayCurrency)
	{
		if (null == strPayCurrency || !_mapPayCurrencyForeignCollateralDC.containsKey (strPayCurrency))
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
			mapCollateralCurrencyDC = _mapPayCurrencyForeignCollateralDC.get (strPayCurrency);

		int iNumCollateralizer = mapCollateralCurrencyDC.size();

		org.drip.state.curve.ForeignCollateralizedDiscountCurve[] aFCDC = new
			org.drip.state.curve.ForeignCollateralizedDiscountCurve[iNumCollateralizer];

		int i = 0;

		for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.DiscountCurve> me :
			mapCollateralCurrencyDC.entrySet()) {
			org.drip.analytics.rates.DiscountCurve fcdc = me.getValue();

			if (!(fcdc instanceof org.drip.state.curve.ForeignCollateralizedDiscountCurve)) return null;

			aFCDC[i++] = (org.drip.state.curve.ForeignCollateralizedDiscountCurve) fcdc;
		}

		try {
			return new org.drip.state.curve.DeterministicCollateralChoiceDiscountCurve
				(mapCollateralCurrencyDC.get (strPayCurrency), aFCDC, 30);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.definition.CreditCurve creditCurve()
	{
		return _cc;
	}

	@Override public boolean setCreditCurve (
		final org.drip.analytics.definition.CreditCurve cc)
	{
		if (null == cc) return false;

		_cc = cc;
		return true;
	}

	@Override public org.drip.analytics.rates.ForwardCurve forwardCurve (
		final org.drip.product.params.FloatingRateIndex fri)
	{
		if (null == fri) return null;

		java.lang.String strFullyQualifiedName = fri.fullyQualifiedName();

		return _mapForward.containsKey (strFullyQualifiedName) ? _mapForward.get (strFullyQualifiedName) :
			null;
	}

	@Override public boolean setForwardCurve (
		final org.drip.analytics.rates.ForwardCurve fc)
	{
		if (null == fc) return false;

		_mapForward.put (fc.index().fullyQualifiedName(), fc);

		return true;
	}

	@Override public org.drip.analytics.rates.DiscountCurve govvieFundingCurve()
	{
		return _dcTSY;
	}

	@Override public boolean setGovvieFundingCurve (
		final org.drip.analytics.rates.DiscountCurve dcTSY)
	{
		if (null == dcTSY) return false;

		_dcTSY = dcTSY;
		return true;
	}

	@Override public org.drip.param.definition.ComponentQuote componentQuote()
	{
		return _compQuote;
	}

	@Override public boolean setComponentQuote (
		final org.drip.param.definition.ComponentQuote compQuote)
	{
		_compQuote = compQuote;
		return true;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			benchmarkTSYQuotes()
	{
		return _mTSYQuotes;
	}

	@Override public boolean setBenchmarkTSYQuotes (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			mTSYQuotes)
	{
		_mTSYQuotes = mTSYQuotes;
		return true;
	}

	@Override public java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> fixings()
	{
		return _mmFixings;
	}

	@Override public boolean setFixings (
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings)
	{
		_mmFixings = mmFixings;
		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate volSurface (
		final java.lang.String strLatentState,
		final org.drip.analytics.date.JulianDate dtForward)
	{
		if (null == strLatentState || !_mapLatentStateForwardVolatility.containsKey (strLatentState))
			return null;

		java.util.Map<org.drip.analytics.date.JulianDate, org.drip.quant.function1D.AbstractUnivariate>
			mapForwardVolatility = _mapLatentStateForwardVolatility.get (strLatentState);

		if (null == mapForwardVolatility || !mapForwardVolatility.containsKey (dtForward)) return null;

		return mapForwardVolatility.get (dtForward);
	}

	@Override public boolean setVolSurface (
		final java.lang.String strLatentState,
		final org.drip.analytics.date.JulianDate dtForward,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == strLatentState || strLatentState.isEmpty() || null == dtForward || null == auVolatility)
			return false;

		java.util.Map<org.drip.analytics.date.JulianDate, org.drip.quant.function1D.AbstractUnivariate>
			mapForwardVolatility = _mapLatentStateForwardVolatility.get (strLatentState);

		if (null == mapForwardVolatility) {
			mapForwardVolatility = new java.util.HashMap<org.drip.analytics.date.JulianDate,
				org.drip.quant.function1D.AbstractUnivariate>();

			mapForwardVolatility.put (dtForward, auVolatility);

			_mapLatentStateForwardVolatility.put (strLatentState, mapForwardVolatility);
		} else
			mapForwardVolatility.put (dtForward, auVolatility);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate fxCurve (
		final java.lang.String strFXCode)
	{
		if (null == strFXCode || strFXCode.isEmpty() || null == _mapAUFX || !_mapAUFX.containsKey
			(strFXCode))
			return null;

		return _mapAUFX.get (strFXCode);
	}

	@Override public boolean setFXCurve (
		final java.lang.String strFXCode,
		final org.drip.quant.function1D.AbstractUnivariate auFX)
	{
		if (null == strFXCode || strFXCode.isEmpty() || null == auFX) return false;

		if (null == _mapAUFX)
			_mapAUFX = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

		_mapAUFX.put (strFXCode, auFX);

		return true;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter());

		if (null == _cc)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_cc.serialize()) + getFieldDelimiter());

		if (null == _dcFunding)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_dcFunding.serialize()) + getFieldDelimiter());

		sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());

		if (null == _dcTSY)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_dcTSY.serialize()) + getFieldDelimiter());

		if (null == _compQuote)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_compQuote.serialize()) + getFieldDelimiter());

		if (null == _mmFixings || null == _mmFixings.entrySet())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbFixings = new java.lang.StringBuffer();

			for (java.util.Map.Entry<org.drip.analytics.date.JulianDate,
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> meOut :
					_mmFixings.entrySet()) {
				if (null == meOut || null == meOut.getValue() || null == meOut.getValue().entrySet())
					continue;

				for (java.util.Map.Entry<java.lang.String, java.lang.Double> meIn :
					meOut.getValue().entrySet()) {
					if (null == meIn || null == meIn.getKey() || meIn.getKey().isEmpty()) continue;

					if (bFirstEntry)
						bFirstEntry = false;
					else
						sb.append (getCollectionRecordDelimiter());

					sbFixings.append (meOut.getKey().getJulian() + getCollectionMultiLevelKeyDelimiter() +
						meIn.getKey() + getCollectionKeyValueDelimiter() + meIn.getValue());
				}
			}

			if (sbFixings.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
			else
				sb.append (sbFixings.toString() + getFieldDelimiter());
		}

		if (null == _mTSYQuotes || 0 == _mTSYQuotes.size())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbMapTSYQuotes = new java.lang.StringBuffer();

			for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ComponentQuote> me :
				_mTSYQuotes.entrySet()) {
				if (null == me || null == me.getKey() || me.getKey().isEmpty()) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbMapTSYQuotes.append (getCollectionRecordDelimiter());

				sbMapTSYQuotes.append (me.getKey() + getCollectionKeyValueDelimiter() + new java.lang.String
					(me.getValue().serialize()));
			}

			if (!sbMapTSYQuotes.toString().isEmpty()) sb.append (sbMapTSYQuotes);
		}

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new ComponentMarketParamSet (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		double dblStart = org.drip.analytics.date.JulianDate.Today().getJulian();

		double[] adblDate = new double[3];
		double[] adblRate = new double[3];
		double[] adblForward = new double[3];
		double[] adblRateTSY = new double[3];
		double[] adblHazardRate = new double[3];

		for (int i = 0; i < 3; ++i) {
			adblDate[i] = dblStart + 365. * (i + 1);
			adblRate[i] = 0.015 * (i + 1);
			adblForward[i] = 0.02 * (i + 1);
			adblRateTSY[i] = 0.01 * (i + 1);
			adblHazardRate[i] = 0.01 * (i + 1);
		}

		org.drip.analytics.rates.ExplicitBootDiscountCurve dc =
			org.drip.state.creator.DiscountCurveBuilder.CreateDC
				(org.drip.analytics.date.JulianDate.Today(), "ABC", null, adblDate, adblRate,
					org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		org.drip.analytics.rates.ExplicitBootDiscountCurve dcTSY =
			org.drip.state.creator.DiscountCurveBuilder.CreateDC
				(org.drip.analytics.date.JulianDate.Today(), "ABCTSY", null, adblDate, adblRateTSY,
					org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		org.drip.analytics.definition.ExplicitBootCreditCurve cc =
			org.drip.state.creator.CreditCurveBuilder.CreateCreditCurve
				(org.drip.analytics.date.JulianDate.Today(), "ABCSOV", "USD", adblDate, adblHazardRate,
					0.40);

		org.drip.param.market.ComponentMultiMeasureQuote cq = new
			org.drip.param.market.ComponentMultiMeasureQuote();

		cq.addQuote ("Price", new org.drip.param.market.MultiSidedQuote ("ASK", 103., 100000.), false);

		cq.setMarketQuote ("SpreadToTsyBmk", new org.drip.param.market.MultiSidedQuote ("MID", 210.,
			100000.));

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote> mapTSYQuotes
			= new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>();

		mapTSYQuotes.put ("TSY2ON", cq);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mIndexFixings = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mIndexFixings.put ("USD-LIBOR-6M", 0.0042);

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings = new
				java.util.HashMap<org.drip.analytics.date.JulianDate,
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		mmFixings.put (org.drip.analytics.date.JulianDate.Today().addDays (2), mIndexFixings);

		org.drip.param.market.ComponentMarketParamSet cmp = new
			org.drip.param.market.ComponentMarketParamSet();

		cmp.setCreditCurve (cc);

		cmp.setGovvieFundingCurve (dcTSY);

		cmp.setComponentQuote (cq);

		cmp.setFundingCurve (dc);

		cmp.setFixings (mmFixings);

		cmp.setBenchmarkTSYQuotes (mapTSYQuotes);

		byte[] abCMP = cmp.serialize();

		System.out.println (new java.lang.String (abCMP));

		ComponentMarketParamSet cmpDeser = new ComponentMarketParamSet (abCMP);

		System.out.println (new java.lang.String (cmpDeser.serialize()));
	}
}
