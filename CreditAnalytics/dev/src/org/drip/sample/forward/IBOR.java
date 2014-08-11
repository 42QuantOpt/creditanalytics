
package org.drip.sample.forward;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.PeriodBuilder;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.calib.*;
import org.drip.product.cashflow.*;
import org.drip.product.creator.DepositBuilder;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.fra.FRAStandardComponent;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;
import org.drip.state.identifier.ForwardLabel;
import org.drip.state.inference.*;
import org.drip.state.representation.LatentStateSpecification;

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
 * IBOR illustrates the Construction and Usage of the IBOR Forward Curve.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class IBOR {
	private static final LatentStateStretchSpec ConstructStretch (
		final String strStretchName,
		final CalibratableFixedIncomeComponent[] aCalibComp,
		final String strManifestMeasure,
		final double[] adblQuote)
		throws Exception
	{
		if (null == aCalibComp || 0 == aCalibComp.length) return null;

		LatentStateSegmentSpec[] aSegmentSpec = new LatentStateSegmentSpec[aCalibComp.length];

		for (int i = 0; i < aCalibComp.length; ++i) {
			ProductQuoteSet pqs = aCalibComp[i].calibQuoteSet (
				new LatentStateSpecification[] {
					new LatentStateSpecification (
						ForwardCurve.LATENT_STATE_FORWARD,
						ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
						aCalibComp[i] instanceof DualStreamComponent ?
							((DualStreamComponent) aCalibComp[i]).derivedStream().forwardLabel()[0] : aCalibComp[i].forwardLabel()[0]
					)
				}
			);

			pqs.set (strManifestMeasure, adblQuote[i]);

			aSegmentSpec[i] = new LatentStateSegmentSpec (
				aCalibComp[i],
				pqs
			);
		}

		return new LatentStateStretchSpec (
			strStretchName,
			aSegmentSpec
		);
	}

	/*
	 * Construct the Array of Deposit Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final DepositComponent[] DepositFromMaturityDays (
		final JulianDate dtEffective,
		final String[] astrMaturityTenor,
		final ForwardLabel fri)
		throws Exception
	{
		if (null == astrMaturityTenor || 0 == astrMaturityTenor.length) return null;

		DepositComponent[] aDeposit = new DepositComponent[astrMaturityTenor.length];

		String strCurrency = fri.currency();

		for (int i = 0; i < astrMaturityTenor.length; ++i)
			aDeposit[i] = DepositBuilder.CreateDeposit (
				dtEffective,
				dtEffective.addTenor (astrMaturityTenor[i]),
				fri,
				strCurrency
			);

		return aDeposit;
	}

	/*
	 * Construct the Array of FRA from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FRAStandardComponent[] FRAFromMaturityDays (
		final JulianDate dtEffective,
		final ForwardLabel fri,
		final String[] astrMaturityTenor,
		final double[] adblFRAStrike)
		throws Exception
	{
		if (null == astrMaturityTenor || null == adblFRAStrike || 0 == astrMaturityTenor.length) return null;

		FRAStandardComponent[] aFRA = new FRAStandardComponent[astrMaturityTenor.length];

		String strCurrency = fri.currency();

		for (int i = 0; i < astrMaturityTenor.length; ++i)
			aFRA[i] = new FRAStandardComponent (
				1.,
				strCurrency,
				"FRA::" + strCurrency,
				strCurrency,
				dtEffective.addTenor (astrMaturityTenor[i]).julian(),
				fri,
				adblFRAStrike[i],
				"Act/365"
			);

		return aFRA;
	}

	/*
	 * Construct an array of fix-float swaps from the fixed reference and the xM floater derived legs.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FixFloatComponent[] FixFloatSwap (
		final JulianDate dtEffective,
		final ForwardLabel fri,
		final String[] astrMaturityTenor)
		throws Exception
	{
		if (null == astrMaturityTenor || 0 == astrMaturityTenor.length) return null;

		String strCurrency = fri.currency();

		FixFloatComponent[] aFFC = new FixFloatComponent[astrMaturityTenor.length];

		int iForwardTenorFreq = new Integer (fri.tenor().split ("M")[0]);

		for (int i = 0; i < astrMaturityTenor.length; ++i) {

			/*
			 * The Fixed Leg
			 */

			List<CashflowPeriod> lsFixedPeriods = PeriodBuilder.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrMaturityTenor[i],
				null,
				4,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency
			);

			FixedStream fixStream = new FixedStream (
				strCurrency,
				null,
				0.,
				1.,
				null,
				lsFixedPeriods
			);

			/*
			 * The Derived Leg
			 */

			List<CashflowPeriod> lsFloatPeriods = PeriodBuilder.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrMaturityTenor[i],
				null,
				12 / iForwardTenorFreq,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency
			);

			FloatingStream fsDerived = new FloatingStream (
				strCurrency,
				null,
				0.,
				-1.,
				null,
				lsFloatPeriods,
				fri,
				false
			);

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
		final ForwardLabel fri,
		final String[] astrMaturityTenor)
		throws Exception
	{
		if (null == astrMaturityTenor || 0 == astrMaturityTenor.length) return null;

		String strCurrency = fri.currency();

		FloatFloatComponent[] aFFC = new FloatFloatComponent[astrMaturityTenor.length];

		int iForwardTenorFreq = new Integer (fri.tenor().split ("M")[0]);

		for (int i = 0; i < astrMaturityTenor.length; ++i) {

			/*
			 * The Reference 6M Leg
			 */

			List<CashflowPeriod> lsReferenceFloatPeriods = PeriodBuilder.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrMaturityTenor[i],
				null,
				2,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency
			);

			FloatingStream fsReference = new FloatingStream (
				strCurrency,
				null,
				0.,
				1.,
				null,
				lsReferenceFloatPeriods,
				ForwardLabel.Create (strCurrency + "-LIBOR-6M"),
				false
			);

			/*
			 * The Derived Leg
			 */

			List<CashflowPeriod> lsDerivedFloatPeriods = PeriodBuilder.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrMaturityTenor[i],
				null,
				12 / iForwardTenorFreq,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency
			);

			FloatingStream fsDerived = new FloatingStream (
				strCurrency,
				null,
				0.,
				-1.,
				null,
				lsDerivedFloatPeriods,
				fri,
				false
			);

			/*
			 * The float-float swap instance
			 */

			aFFC[i] = new FloatFloatComponent (fsReference, fsDerived);
		}

		return aFFC;
	}

	public static final ForwardCurve CustomIBORBuilderSample (
		final DiscountCurve dc,
		final ForwardCurve fcReference,
		final ForwardLabel fri,
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
		final String strHeaderComment,
		final boolean bPrintMetric)
		throws Exception
	{
		if (bPrintMetric) {
			System.out.println ("\n\t----------------------------------------------------------------");

			System.out.println ("\t     " + strHeaderComment);

			System.out.println ("\t----------------------------------------------------------------");
		}

		JulianDate dtValue = dc.epoch();

		DepositComponent[] aDeposit = DepositFromMaturityDays (
			dtValue,
			astrDepositTenor,
			fri
		);

		/*
		 * Construct the Deposit Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec depositStretch = ConstructStretch (
			"DEPOSIT",
			aDeposit,
			strDepositCalibMeasure,
			adblDepositQuote
		);

		FRAStandardComponent[] aFRA = FRAFromMaturityDays (
			dtValue,
			fri,
			astrFRATenor,
			adblFRAQuote
		);

		/*
		 * Construct the FRA Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec fraStretch = ConstructStretch (
			"FRA",
			aFRA,
			strFRACalibMeasure,
			adblFRAQuote
		);

		FixFloatComponent[] aFixFloat = FixFloatSwap (
			dtValue,
			fri,
			astrFixFloatTenor);

		/*
		 * Construct the Fix-Float Component Set Stretch Builder
		 */

		LatentStateStretchSpec fixFloatStretch = ConstructStretch (
			"FIXFLOAT",
			aFixFloat,
			strFixFloatCalibMeasure,
			adblFixFloatQuote
		);

		FloatFloatComponent[] aFloatFloat = FloatFloatSwap (
			dtValue,
			fri,
			astrFloatFloatTenor
		);

		/*
		 * Construct the Float-Float Component Set Stretch Builder
		 */

		LatentStateStretchSpec floatFloatStretch = ConstructStretch (
			"FLOATFLOAT",
			aFloatFloat,
			strFloatFloatCalibMeasure,
			adblFloatFloatQuote
		);

		FloatFloatComponent[] aSyntheticFloatFloat = FloatFloatSwap (
			dtValue,
			fri,
			astrSyntheticFloatFloatTenor
		);

		/*
		 * Construct the Synthetic Float-Float Component Set Stretch Builder
		 */

		LatentStateStretchSpec syntheticFloatFloatStretch = ConstructStretch (
			"SYNTHETICFLOATFLOAT",
			aSyntheticFloatFloat,
			strSyntheticFloatFloatCalibMeasure,
			adblSyntheticFloatFloatQuote
		);

		LatentStateStretchSpec[] aStretchSpec = new LatentStateStretchSpec[] {
			depositStretch,
			fraStretch,
			fixFloatStretch,
			floatFloatStretch,
			syntheticFloatFloatStretch
		};

		/*
		 * Set up the Linear Curve Calibrator using the following parameters:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LinearLatentStateCalibrator lcc = new LinearLatentStateCalibrator (
			scbc,
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null
		);

		ValuationParams valParams = new ValuationParams (dtValue, dtValue, fri.currency());

		/*
		 * Set the discount curve based component market parameters.
		 */

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create
			(dc, fcReference, null, null, null, null, null, null);

		/*
		 * Construct the Shape Preserving Forward Curve by applying the linear curve calibrator to the array
		 *  of Deposit and Swap Stretches.
		 */

		ForwardCurve fcDerived = ScenarioForwardCurveBuilder.ShapePreservingForwardCurve (
			lcc,
			aStretchSpec,
			fri,
			valParams,
			null,
			mktParams,
			null,
			null == adblDepositQuote || 0 == adblDepositQuote.length ? adblFRAQuote[0] : adblDepositQuote[0]);

		/*
		 * Set the discount curve + cubic polynomial forward curve based component market parameters.
		 */

		mktParams.setForwardCurve (fcDerived);

		if (bPrintMetric) {
			/*
			 * Cross-Comparison of the Deposit Calibration Instrument "Forward" metric.
			 */

			if (null != aDeposit && null != adblDepositQuote) {
				System.out.println ("\t----------------------------------------------------------------");

				System.out.println ("\t     DEPOSIT INSTRUMENTS QUOTE RECOVERY");

				System.out.println ("\t----------------------------------------------------------------");

				for (int i = 0; i < aDeposit.length; ++i)
					System.out.println ("\t[" + aDeposit[i].effective() + " - " + aDeposit[i].maturity() + "] = " +
						FormatUtil.FormatDouble (aDeposit[i].measureValue (valParams, null, mktParams, null, strDepositCalibMeasure), 1, 6, 1.) +
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
						FormatUtil.FormatDouble (aFRA[i].measureValue (valParams, null, mktParams, null, strFRACalibMeasure), 1, 6, 1.) +
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
						FormatUtil.FormatDouble (aFixFloat[i].measureValue (valParams, null, mktParams, null, strFixFloatCalibMeasure), 1, 2, 100.) +
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
						FormatUtil.FormatDouble (aFloatFloat[i].measureValue (valParams, null, mktParams, null, strFloatFloatCalibMeasure), 1, 2, 1.) +
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
						FormatUtil.FormatDouble (aSyntheticFloatFloat[i].measureValue (valParams, null, mktParams, null, strSyntheticFloatFloatCalibMeasure), 1, 2, 1.) +
							" | " + FormatUtil.FormatDouble (adblSyntheticFloatFloatQuote[i], 1, 2, 10000.) + " | " +
								FormatUtil.FormatDouble (fcDerived.forward (aSyntheticFloatFloat[i].maturity()), 1, 4, 100.) + "%");
			}
		}

		return fcDerived;
	}

	private static final void ForwardJack (
		final JulianDate dt,
		final ForwardCurve fc,
		final String strStartDateTenor,
		final String strManifestMeasure)
	{
		JulianDate dtJack = dt.addTenor (strStartDateTenor);

		System.out.println ("\t" + 
			dtJack + " | " +
			strStartDateTenor + ": " +
			fc.jackDForwardDManifestMeasure (
				strManifestMeasure,
				dtJack).displayString()
			);
	}

	public static final void ForwardJack (
		final JulianDate dt,
		final String strHeaderComment,
		final ForwardCurve fc,
		final String strManifestMeasure)
	{
		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t" + strHeaderComment);

		System.out.println ("\t----------------------------------------------------------------");

		ForwardJack (dt, fc, "1Y", strManifestMeasure);

		ForwardJack (dt, fc, "2Y", strManifestMeasure);

		ForwardJack (dt, fc, "3Y", strManifestMeasure);

		ForwardJack (dt, fc, "5Y", strManifestMeasure);

		ForwardJack (dt, fc, "7Y", strManifestMeasure);
	}
}
