
package org.drip.sample.xccy;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.*;
import org.drip.param.definition.*;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.RatesStreamBuilder;
import org.drip.product.definition.RatesComponent;
import org.drip.product.fx.CrossCurrencyComponentPair;
import org.drip.product.params.FloatingRateIndex;
import org.drip.product.rates.*;
import org.drip.quant.calculus.WengertJacobian;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.FlatUnivariate;
import org.drip.spline.params.SegmentCustomBuilderControl;
import org.drip.spline.stretch.*;
import org.drip.state.estimator.*;

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
 * CCBSDiscountCurve demonstrates the setup and construction of the Forward Curve from the CCBS Quotes.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CCBSDiscountCurve {

	/*
	 * Construct an array of float-float swaps from the corresponding reference (6M) and the derived legs.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final RatesComponent[] MakexM6MBasisSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrTenor,
		final int iTenorInMonths)
		throws Exception
	{
		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

		FloatFloatComponent[] aFFC = new FloatFloatComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenorAndAdjust (astrTenor[i], strCurrency);

			/*
			 * The Reference 6M Leg
			 */

			FloatingStream fsReference = FloatingStream.Create (dtEffective.getJulian(),
				dtMaturity.getJulian(), 0., true, FloatingRateIndex.Create (strCurrency + "-LIBOR-6M"),
					2, "Act/360", false, "Act/360", false, false, null, dap, dap, dap, dap, dap, dap,
						null, null, -1., strCurrency, strCurrency);

			/*
			 * The Derived Leg
			 */

			FloatingStream fsDerived = FloatingStream.Create (dtEffective.getJulian(),
				dtMaturity.getJulian(), 0., false, FloatingRateIndex.Create (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
					12 / iTenorInMonths, "Act/360", false, "Act/360", false, false, null, dap, dap, dap, dap, dap, dap,
						null, null, 1., strCurrency, strCurrency);

			/*
			 * The float-float swap instance
			 */

			aFFC[i] = new FloatFloatComponent (fsReference, fsDerived);

			aFFC[i].setPrimaryCode (strCurrency + "_6M::" + iTenorInMonths + "M::" + astrTenor[i]);
		}

		return aFFC;
	}

	/*
	 * Construct the Array of Swap Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final RatesComponent[] MakeIRS (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrTenor)
		throws Exception
	{
		RatesComponent[] aCalibComp = new RatesComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aCalibComp[i] = RatesStreamBuilder.CreateIRS (dtEffective,
				dtEffective.addTenorAndAdjust (astrTenor[i], strCurrency), 0., strCurrency, strCurrency + "-LIBOR-3M", strCurrency);

		return aCalibComp;
	}

	private static final CrossCurrencyComponentPair[] MakeCCSP (
		final JulianDate dtValue,
		final String strReferenceCurrency,
		final String strDerivedCurrency,
		final String[] astrTenor,
		final int iTenorInMonths)
		throws Exception
	{
		RatesComponent[] aFFCReference = MakexM6MBasisSwap (
			dtValue,
			strReferenceCurrency,
			astrTenor,
			3);

		RatesComponent[] aIRS = MakeIRS (
			dtValue,
			strDerivedCurrency,
			astrTenor);

		CrossCurrencyComponentPair[] aCCSP = new CrossCurrencyComponentPair[astrTenor.length];

		for (int i = 0; i < aCCSP.length; ++i)
			aCCSP[i] = new CrossCurrencyComponentPair ("EURUSD_" + astrTenor[i], aFFCReference[i], aIRS[i]);

		return aCCSP;
	}

	private static final void TenorJack (
		final JulianDate dtStart,
		final String strTenor,
		final String strManifestMeasure,
		final DiscountCurve dc)
	{
		String strCurrency = dc.currency();

		RatesComponent irsBespoke = RatesStreamBuilder.CreateIRS (
			dtStart, dtStart.addTenorAndAdjust (strTenor, strCurrency),
			0.,
			strCurrency,
			strCurrency + "-LIBOR-6M",
			strCurrency
		);

		WengertJacobian wjDFQuoteBespokeMat = dc.jackDDFDManifestMeasure (
			irsBespoke.maturity(),
			strManifestMeasure
		);

		System.out.println ("\t" + strTenor + " => " + wjDFQuoteBespokeMat.displayString());
	}

	public static final void MakeDiscountCurve (
		final String strReferenceCurrency,
		final String strDerivedCurrency,
		final JulianDate dtValue,
		final DiscountCurve dcReference,
		final ForwardCurve fc6MReference,
		final ForwardCurve fc3MReference,
		final double dblRefDerFX,
		final SegmentCustomBuilderControl scbc,
		final String[] astrTenor,
		final double[] adblCrossCurrencyBasis,
		final double[] adblSwapRate)
		throws Exception
	{
		List<CaseInsensitiveTreeMap<Double>> lsCCBSMapManifestQuote = new ArrayList<CaseInsensitiveTreeMap<Double>>();

		List<CaseInsensitiveTreeMap<Double>> lsIRSMapManifestQuote = new ArrayList<CaseInsensitiveTreeMap<Double>>();

		for (int i = 0; i < astrTenor.length; ++i) {
			CaseInsensitiveTreeMap<Double> mapIRSManifestQuote = new CaseInsensitiveTreeMap<Double>();

			mapIRSManifestQuote.put ("Rate", adblSwapRate[i]);

			lsIRSMapManifestQuote.add (mapIRSManifestQuote);

			CaseInsensitiveTreeMap<Double> mapCCBSManifestQuote = new CaseInsensitiveTreeMap<Double>();

			mapCCBSManifestQuote.put ("DerivedParBasisSpread", adblCrossCurrencyBasis[i]);

			lsCCBSMapManifestQuote.add (mapCCBSManifestQuote);
		}

		CrossCurrencyComponentPair[] aCCSP = MakeCCSP (
			dtValue,
			strReferenceCurrency,
			strDerivedCurrency,
			astrTenor,
			3);

		BasketMarketParams bmp = BasketMarketParamsBuilder.CreateBasketMarketParams();

		bmp.addDiscountCurve (strReferenceCurrency, dcReference);

		bmp.addForwardCurve (fc3MReference.index().fullyQualifiedName(), fc3MReference);

		bmp.addForwardCurve (fc6MReference.index().fullyQualifiedName(), fc6MReference);

		bmp.setFXCurve (strDerivedCurrency + "/" + strReferenceCurrency, new FlatUnivariate (1. / dblRefDerFX));

		bmp.setFXCurve (strReferenceCurrency + "/" + strDerivedCurrency, new FlatUnivariate (dblRefDerFX));

		ValuationParams valParams = new ValuationParams (dtValue, dtValue, strReferenceCurrency);

		LinearCurveCalibrator lcc = new LinearCurveCalibrator (
			scbc,
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null);

		StretchRepresentationSpec srsIRS = StretchRepresentationSpec.DiscountFromCCBS (
			"FIXFLOAT",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aCCSP,
			valParams,
			bmp,
			lsCCBSMapManifestQuote,
			lsIRSMapManifestQuote
		);

		DiscountCurve dcDerived = ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (
			lcc,
			new StretchRepresentationSpec[] {srsIRS},
			valParams,
			null,
			null,
			null,
			1.
		);

		bmp.addDiscountCurve (strDerivedCurrency, dcDerived);

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t     IRS INSTRUMENTS QUOTE RECOVERY FROM CCBS INPUTS");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCCSP.length; ++i) {
			IRSComponent irs = (IRSComponent) aCCSP[i].derivedComponent();

			CaseInsensitiveTreeMap<Double> mapOP = aCCSP[i].value (valParams, null, bmp, null);

			double dblCalibSwapRate = mapOP.get (irs.componentName() + "[CalibSwapRate]");

			System.out.println ("\t[" + irs.effective() + " - " + irs.maturity() + "] = " +
				FormatUtil.FormatDouble (dblCalibSwapRate, 1, 3, 100.) +
					" | " + FormatUtil.FormatDouble (adblSwapRate[i], 1, 3, 100.) + " | " +
						FormatUtil.FormatDouble (adblSwapRate[i] - dblCalibSwapRate, 2, 0, 10000.) + " | " +
							FormatUtil.FormatDouble (dcDerived.df (irs.maturity()), 1, 4, 1.));
		}

		System.out.println ("\t----------------------------------------------------------------------");

		System.out.println ("\t     IRS INSTRUMENTS QUOTE RECOVERY FROM CCBS INPUTS TENOR JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------------");

		for (int i = 0; i < aCCSP.length; ++i)
			TenorJack (
				dtValue,
				astrTenor[i],
				"Rate",
				dcDerived
			);
	}
}
