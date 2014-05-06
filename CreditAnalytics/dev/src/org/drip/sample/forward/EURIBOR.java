
package org.drip.sample.forward;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
import org.drip.analytics.rates.*;
import org.drip.param.creator.*;
import org.drip.param.definition.ComponentMarketParams;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.DepositBuilder;
import org.drip.product.definition.*;
import org.drip.product.fra.FRAStandardComponent;
import org.drip.product.params.FloatingRateIndex;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.spline.params.*;
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
 * EURIBOR illustrates the Construction and Usage of the EURIBOR Forward Curve.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class EURIBOR {

	/*
	 * Construct the Array of Deposit Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final RatesComponent[] DepositFromMaturityDays (
		final JulianDate dtEffective,
		final String[] astrMaturityTenor,
		final FloatingRateIndex fri)
		throws Exception
	{
		if (null == astrMaturityTenor || 0 == astrMaturityTenor.length) return null;

		RatesComponent[] aDeposit = new RatesComponent[astrMaturityTenor.length];

		String strCurrency = fri.currency();

		for (int i = 0; i < astrMaturityTenor.length; ++i)
			aDeposit[i] = DepositBuilder.CreateDeposit (
				dtEffective,
				dtEffective.addTenorAndAdjust (astrMaturityTenor[i], strCurrency),
				fri,
				strCurrency);

		return aDeposit;
	}

	/*
	 * Construct the Array of FRA from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final RatesComponent[] FRAFromMaturityDays (
		final JulianDate dtEffective,
		final FloatingRateIndex fri,
		final String[] astrMaturityTenor,
		final double[] adblFRAStrike)
		throws Exception
	{
		if (null == astrMaturityTenor || null == adblFRAStrike || 0 == astrMaturityTenor.length) return null;

		RatesComponent[] aFRA = new RatesComponent[astrMaturityTenor.length];

		String strCurrency = fri.currency();

		for (int i = 0; i < astrMaturityTenor.length; ++i)
			aFRA[i] = new FRAStandardComponent (
				1.,
				strCurrency,
				"FRA::" + strCurrency,
				strCurrency,
				dtEffective.addTenorAndAdjust (astrMaturityTenor[i], strCurrency).getJulian(),
				fri,
				adblFRAStrike[i],
				"Act/365");

		return aFRA;
	}

	/*
	 * Construct an array of fix-float swaps from the fixed reference and the xM floater derived legs.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FixFloatComponent[] FixFloatSwap (
		final JulianDate dtEffective,
		final FloatingRateIndex fri,
		final String[] astrMaturityTenor,
		final double[] adblCoupon)
		throws Exception
	{
		if (null == astrMaturityTenor || null == adblCoupon || 0 == astrMaturityTenor.length) return null;

		String strCurrency = fri.currency();

		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

		FixFloatComponent[] aFFC = new FixFloatComponent[astrMaturityTenor.length];

		int iForwardTenorFreq = new Integer (fri.tenor().split ("M")[0]);

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenorAndAdjust (astrMaturityTenor[i], strCurrency);

			/*
			 * The Fixed Leg
			 */

			FixedStream fixStream = new FixedStream (dtEffective.getJulian(), dtMaturity.getJulian(),
				adblCoupon[i], 2, "30/360", "30/360", false, null, dap, dap, dap, dap, dap, null, null, -1.,
					strCurrency, strCurrency);

			/*
			 * The Derived Leg
			 */

			FloatingStream fsDerived = FloatingStream.Create (dtEffective.getJulian(), dtMaturity.getJulian(), 0.,
				false, fri, 12 / iForwardTenorFreq, "Act/360", false, "Act/360", false, false, null, dap, dap, dap, dap,
					dap, dap, null, null, 1., strCurrency, strCurrency);

			/*
			 * The fix-float swap instance
			 */

			aFFC[i] = new FixFloatComponent (fixStream, fsDerived);
		}

		return aFFC;
	}

	/*
	 * Construct an array of float-float swaps from the corresponding reference (6M) and the derived legs.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FloatFloatComponent[] FloatFloatSwap (
		final JulianDate dtEffective,
		final FloatingRateIndex fri,
		final String[] astrMaturityTenor)
		throws Exception
	{
		if (null == astrMaturityTenor || 0 == astrMaturityTenor.length) return null;

		String strCurrency = fri.currency();

		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

		FloatFloatComponent[] aFFC = new FloatFloatComponent[astrMaturityTenor.length];

		int iForwardTenorFreq = new Integer (fri.tenor().split ("M")[0]);

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenorAndAdjust (astrMaturityTenor[i], strCurrency);

			/*
			 * The Reference 6M Leg
			 */

			FloatingStream fsReference = FloatingStream.Create (dtEffective.getJulian(), dtMaturity.getJulian(),
				0., true, FloatingRateIndex.Create (strCurrency + "-LIBOR-6M"), 2, "Act/360", false, "Act/360",
					false, false, null, dap, dap, dap, dap, dap, dap, null, null, -1., strCurrency, strCurrency);

			/*
			 * The Derived Leg
			 */

			FloatingStream fsDerived = FloatingStream.Create (dtEffective.getJulian(), dtMaturity.getJulian(),
				0., false, fri, 12 / iForwardTenorFreq, "Act/360", false, "Act/360", false, false, null, dap, dap,
					dap, dap, dap, dap, null, null, 1., strCurrency, strCurrency);

			/*
			 * The float-float swap instance
			 */

			aFFC[i] = new FloatFloatComponent (fsReference, fsDerived);
		}

		return aFFC;
	}

	public static final ForwardCurve CustomEURIBORBuilderSample (
		final DiscountCurve dc,
		final ForwardCurve fcReference,
		final FloatingRateIndex fri,
		final SegmentCustomBuilderControl scbc,
		final String[] astrDepositTenor,
		final double[] adblDepositQuote,
		final String strDepositCalibMeasure,
		final String[] astrFRATenor,
		final double[] adblFRAQuote,
		final String strFRACalibMeasure,
		final String[] astrFixFloatTenor,
		final double[] adblFixFloatQuote,
		final String strFixFloatCalibMeasure,
		final String[] astrFloatFloatTenor,
		final double[] adblFloatFloatQuote,
		final String strFloatFloatCalibMeasure,
		final String[] astrSyntheticFloatFloatTenor,
		final double[] adblSyntheticFloatFloatQuote,
		final String strSyntheticFloatFloatCalibMeasure,
		final String strHeaderComment)
		throws Exception
	{
		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     " + strHeaderComment);

		System.out.println ("\t----------------------------------------------------------------");

		JulianDate dtValue = dc.epoch();

		CalibratableFixedIncomeComponent[] aDeposit = DepositFromMaturityDays (
			dtValue,
			astrDepositTenor,
			fri
		);

		/*
		 * Construct the Deposit Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec srsDeposit = StretchRepresentationSpec.CreateStretchBuilderSet (
			"DEPOSIT",
			ForwardCurve.LATENT_STATE_FORWARD,
			ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
			aDeposit,
			strDepositCalibMeasure,
			adblDepositQuote,
			null);

		CalibratableFixedIncomeComponent[] aFRA = FRAFromMaturityDays (
			dtValue,
			fri,
			astrFRATenor,
			adblFRAQuote
		);

		/*
		 * Construct the FRA Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec srsFRA = StretchRepresentationSpec.CreateStretchBuilderSet (
			"FRA",
			ForwardCurve.LATENT_STATE_FORWARD,
			ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
			aFRA,
			strFRACalibMeasure,
			adblFRAQuote,
			null);

		double[] adblFixFloatDerivedParBasisSpread = null;

		if (null != adblFixFloatQuote && 0 != adblFixFloatQuote.length) {
			adblFixFloatDerivedParBasisSpread = new double[adblFixFloatQuote.length];

			for (int j = 0; j < adblFixFloatQuote.length; ++j)
				adblFixFloatDerivedParBasisSpread[j] = 0.;
		}

		FixFloatComponent[] aFixFloat = FixFloatSwap (
			dtValue,
			fri,
			astrFixFloatTenor,
			adblFixFloatQuote);

		/*
		 * Construct the Fix-Float Component Set Stretch Builder
		 */

		StretchRepresentationSpec srsFixFloat = StretchRepresentationSpec.CreateStretchBuilderSet (
			"FIXFLOAT",
			ForwardCurve.LATENT_STATE_FORWARD,
			ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
			aFixFloat,
			strFixFloatCalibMeasure,
			adblFixFloatDerivedParBasisSpread,
			null);

		FloatFloatComponent[] aFloatFloat = FloatFloatSwap (
			dtValue,
			fri,
			astrFloatFloatTenor
		);

		/*
		 * Construct the Float-Float Component Set Stretch Builder
		 */

		StretchRepresentationSpec srsFloatFloat = StretchRepresentationSpec.CreateStretchBuilderSet (
			"FLOATFLOAT",
			ForwardCurve.LATENT_STATE_FORWARD,
			ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
			aFloatFloat,
			strFloatFloatCalibMeasure,
			adblFloatFloatQuote,
			null);

		FloatFloatComponent[] aSyntheticFloatFloat = FloatFloatSwap (
			dtValue,
			fri,
			astrSyntheticFloatFloatTenor
		);

		/*
		 * Construct the Synthetic Float-Float Component Set Stretch Builder
		 */

		StretchRepresentationSpec srsSyntheticFloatFloat = StretchRepresentationSpec.CreateStretchBuilderSet (
			"SYNTHETICFLOATFLOAT",
			ForwardCurve.LATENT_STATE_FORWARD,
			ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
			aSyntheticFloatFloat,
			strSyntheticFloatFloatCalibMeasure,
			adblSyntheticFloatFloatQuote,
			null);

		StretchRepresentationSpec[] aSRS = new StretchRepresentationSpec[] {
			srsDeposit,
			srsFRA,
			srsFixFloat,
			srsFloatFloat,
			srsSyntheticFloatFloat
		};

		/*
		 * Set up the Linear Curve Calibrator using the following parameters:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LinearCurveCalibrator lcc = new LinearCurveCalibrator (
			scbc,
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null);

		ValuationParams valParams = new ValuationParams (dtValue, dtValue, fri.currency());

		/*
		 * Set the discount curve based component market parameters.
		 */

		ComponentMarketParams cmp = ComponentMarketParamsBuilder.CreateComponentMarketParams
			(dc, fcReference, null, null, null, null, null);

		/*
		 * Construct the Shape Preserving Forward Curve by applying the linear curve calibrator to the array
		 *  of Deposit and Swap Stretches.
		 */

		ForwardCurve fcDerived = ScenarioForwardCurveBuilder.ShapePreservingForwardCurve (
			lcc,
			aSRS,
			fri,
			valParams,
			null,
			cmp,
			null,
			null == adblDepositQuote || 0 == adblDepositQuote.length ? adblFRAQuote[0] : adblDepositQuote[0]);

		/*
		 * Set the discount curve + cubic polynomial forward curve based component market parameters.
		 */

		cmp.setForwardCurve (fcDerived);

		/*
		 * Cross-Comparison of the Deposit Calibration Instrument "Forward" metric.
		 */

		if (null != aDeposit && null != adblDepositQuote) {
			System.out.println ("\t----------------------------------------------------------------");

			System.out.println ("\t     DEPOSIT INSTRUMENTS QUOTE RECOVERY");

			System.out.println ("\t----------------------------------------------------------------");

			for (int i = 0; i < aDeposit.length; ++i)
				System.out.println ("\t[" + aDeposit[i].effective() + " - " + aDeposit[i].maturity() + "] = " +
					FormatUtil.FormatDouble (aDeposit[i].measureValue (valParams, null, cmp, null, strDepositCalibMeasure), 1, 6, 1.) +
						" | " + FormatUtil.FormatDouble (adblDepositQuote[i], 1, 6, 1.) + " | " +
							FormatUtil.FormatDouble (fcDerived.forward (aDeposit[i].maturity()), 1, 4, 100.) + "%");
		}

		/*
		 * Cross-Comparison of the FRA Calibration Instrument "Forward" metric.
		 */

		if (null != aFRA && null != adblFRAQuote) {
			System.out.println ("\t----------------------------------------------------------------");

			System.out.println ("\t     FRA INSTRUMENTS QUOTE RECOVERY");

			System.out.println ("\t----------------------------------------------------------------");

			for (int i = 0; i < aFRA.length; ++i)
				System.out.println ("\t[" + aFRA[i].effective() + " - " + aFRA[i].maturity() + "] = " +
					FormatUtil.FormatDouble (aFRA[i].measureValue (valParams, null, cmp, null, strFRACalibMeasure), 1, 6, 1.) +
						" | " + FormatUtil.FormatDouble (adblFRAQuote[i], 1, 6, 1.) + " | " +
							FormatUtil.FormatDouble (fcDerived.forward (aFRA[i].maturity()), 1, 4, 100.) + "%");
		}

		/*
		 * Cross-Comparison of the Fix-Float Calibration Instrument "DerivedParBasisSpread" metric.
		 */

		if (null != aFixFloat && null != adblFixFloatQuote) {
			System.out.println ("\t----------------------------------------------------------------");

			System.out.println ("\t     FIX-FLOAT INSTRUMENTS QUOTE RECOVERY");

			System.out.println ("\t----------------------------------------------------------------");

			for (int i = 0; i < aFixFloat.length; ++i)
				System.out.println ("\t[" + aFixFloat[i].effective() + " - " + aFixFloat[i].maturity() + "] = " +
					FormatUtil.FormatDouble (aFixFloat[i].measureValue (valParams, null, cmp, null, "ParSwapRate"), 1, 2, 0.01) +
						"% | " + FormatUtil.FormatDouble (adblFixFloatQuote[i], 1, 2, 100.) + "% | " +
							FormatUtil.FormatDouble (fcDerived.forward (aFixFloat[i].maturity()), 1, 4, 100.) + "%");
		}

		/*
		 * Cross-Comparison of the Float-Float Calibration Instrument "DerivedParBasisSpread" metric.
		 */

		if (null != aFloatFloat && null != adblFloatFloatQuote) {
			System.out.println ("\t----------------------------------------------------------------");

			System.out.println ("\t     FLOAT-FLOAT INSTRUMENTS QUOTE RECOVERY");

			System.out.println ("\t----------------------------------------------------------------");

			for (int i = 0; i < aFloatFloat.length; ++i)
				System.out.println ("\t[" + aFloatFloat[i].effective() + " - " + aFloatFloat[i].maturity() + "] = " +
					FormatUtil.FormatDouble (aFloatFloat[i].measureValue (valParams, null, cmp, null, strFloatFloatCalibMeasure), 1, 2, 1.) +
						" | " + FormatUtil.FormatDouble (adblFloatFloatQuote[i], 1, 2, 10000.) + " | " +
							FormatUtil.FormatDouble (fcDerived.forward (aFloatFloat[i].maturity()), 1, 4, 100.) + "%");
		}

		/*
		 * Cross-Comparison of the Synthetic Float-Float Calibration Instrument "DerivedParBasisSpread" metric.
		 */

		if (null != aSyntheticFloatFloat && null != adblSyntheticFloatFloatQuote) {
			System.out.println ("\t----------------------------------------------------------------");

			System.out.println ("\t     SYNTHETIC FLOAT-FLOAT INSTRUMENTS QUOTE RECOVERY");

			System.out.println ("\t----------------------------------------------------------------");

			for (int i = 0; i < aSyntheticFloatFloat.length; ++i)
				System.out.println ("\t[" + aSyntheticFloatFloat[i].effective() + " - " + aSyntheticFloatFloat[i].maturity() + "] = " +
					FormatUtil.FormatDouble (aSyntheticFloatFloat[i].measureValue (valParams, null, cmp, null, strSyntheticFloatFloatCalibMeasure), 1, 2, 1.) +
						" | " + FormatUtil.FormatDouble (adblSyntheticFloatFloatQuote[i], 1, 2, 10000.) + " | " +
							FormatUtil.FormatDouble (fcDerived.forward (aSyntheticFloatFloat[i].maturity()), 1, 4, 100.) + "%");
		}

		return fcDerived;
	}

	private static final void ForwardJack (
		final JulianDate dt,
		final ForwardCurve fc,
		final String strStartDateTenor)
	{
		JulianDate dtJack = dt.addTenor (strStartDateTenor);

		System.out.println ("\t" + 
			dtJack + " | " +
			strStartDateTenor + ": " +
			fc.jackDForwardDManifestMeasure (
				"DerivedParBasisSpread",
				dtJack).displayString()
			);
	}

	public static final void ForwardJack (
		final JulianDate dt,
		final String strHeaderComment,
		final ForwardCurve fc)
	{
		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t" + strHeaderComment);

		System.out.println ("\t----------------------------------------------------------------");

		ForwardJack (dt, fc, "1Y");

		ForwardJack (dt, fc, "2Y");

		ForwardJack (dt, fc, "3Y");

		ForwardJack (dt, fc, "5Y");

		ForwardJack (dt, fc, "7Y");
	}
}
