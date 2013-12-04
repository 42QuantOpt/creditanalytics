
package org.drip.sample.rates;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.rates.Turn;
import org.drip.analytics.rates.TurnListDiscountFactor;
import org.drip.param.creator.ComponentMarketParamsBuilder;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableComponent;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;
import org.drip.state.curve.DiscountFactorDiscountCurve;
import org.drip.state.estimator.*;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * CustomDiscountCurveReconciler contains the sample demonstrating the full functionality behind creating
 *  highly customized spline based discount curves.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CustomDiscountCurveReconciler {
	private static final CalibratableComponent[] CashInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay,
		final int iNumFutures)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[aiDay.length + iNumFutures];

		for (int i = 0; i < aiDay.length; ++i)
			aCalibComp[i] = CashBuilder.CreateCash (dtEffective, dtEffective.addBusDays (aiDay[i], "USD"), "USD");

		CalibratableComponent[] aEDF = EDFutureBuilder.GenerateEDPack (dtEffective, iNumFutures, "USD");

		for (int i = aiDay.length; i < aiDay.length + iNumFutures; ++i)
			aCalibComp[i] = aEDF[i - aiDay.length];

		return aCalibComp;
	}

	private static final CalibratableComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aCalibComp[i] = RatesStreamBuilder.CreateIRS (
				dtEffective,
				dtEffective.addTenorAndAdjust (astrTenor[i], "USD"),
				0., "USD", "USD-LIBOR-6M", "USD");

		return aCalibComp;
	}

	private static final LinearCurveCalibrator MakeCalibrator (
		SegmentCustomBuilderControl prbp)
		throws Exception
	{
		return new LinearCurveCalibrator (
			prbp,
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null);
	}

	public static final void SplineLinearDiscountCurve (
		final SegmentCustomBuilderControl prbp)
		throws Exception
	{
		JulianDate dtToday = JulianDate.Today().addTenorAndAdjust ("0D", "USD");

		LinearCurveCalibrator lcc = MakeCalibrator (prbp);

		CalibratableComponent[] aCashComp = CashInstrumentsFromMaturityDays (dtToday, new int[] {1, 2, 7, 14, 30, 60}, 8);

		double[] adblCashQuote = new double[]
			{0.0013, 0.0017, 0.0017, 0.0018, 0.0020, 0.0023, // Cash Rate
			0.0027, 0.0032, 0.0041, 0.0054, 0.0077, 0.0104, 0.0134, 0.0160}; // EDF Rate;

		StretchRepresentationSpec rbsCash = StretchRepresentationSpec.CreateStretchBuilderSet (
			"CASH",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aCashComp,
			"Rate",
			adblCashQuote,
			null);

		CalibratableComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (dtToday, new java.lang.String[]
			{"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"});

		double[] adblSwapQuote = new double[]
			{0.0166, 0.0206, 0.0241, 0.0269, 0.0292, 0.0311, 0.0326, 0.0340, 0.0351, 0.0375, 0.0393, 0.0402, 0.0407, 0.0409, 0.0409};

		StretchRepresentationSpec rbsSwap = StretchRepresentationSpec.CreateStretchBuilderSet (
			"SWAP",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aSwapComp,
			"Rate",
			adblSwapQuote,
			null);

		StretchRepresentationSpec[] aRBS = new StretchRepresentationSpec[] {rbsCash, rbsSwap};

		org.drip.spline.grid.OverlappingStretchSpan ors = lcc.calibrateSpan (aRBS, 1.,
			new ValuationParams (dtToday, dtToday, "USD"),
			null,
			null,
			null);

		MultiSegmentSequence mssCash = ors.getStretch ("CASH");

		MultiSegmentSequence mssSwap = ors.getStretch ("SWAP");

		DiscountCurve dfdc = new DiscountFactorDiscountCurve ("USD", ors.toNonOverlapping());

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     CASH DF            DFDC     STRETCH           LOCAL");

		System.out.println ("\t----------------------------------------------------------------");

		for (double dblX = mssCash.getLeftPredictorOrdinateEdge(); dblX <= mssCash.getRightPredictorOrdinateEdge();
			dblX += 0.1 * (mssCash.getRightPredictorOrdinateEdge() - mssCash.getLeftPredictorOrdinateEdge())) {
			try {
				System.out.println ("\tCash [" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (dfdc.df (dblX), 1, 8, 1.) + " || " +
						ors.getContainingStretch (dblX).name() + " || " +
							FormatUtil.FormatDouble (mssCash.responseValue (dblX), 1, 8, 1.) + " | " +
								mssCash.monotoneType (dblX));
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     SWAP DF            DFDC     STRETCH            LOCAL");

		System.out.println ("\t----------------------------------------------------------------");

		for (double dblX = mssSwap.getLeftPredictorOrdinateEdge(); dblX <= mssSwap.getRightPredictorOrdinateEdge();
			dblX += 0.05 * (mssSwap.getRightPredictorOrdinateEdge() - mssSwap.getLeftPredictorOrdinateEdge())) {
				System.out.println ("\tSwap [" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (dfdc.df (dblX), 1, 8, 1.) + " || " +
						ors.getContainingStretch (dblX).name() + " || " +
							FormatUtil.FormatDouble (mssSwap.responseValue (dblX), 1, 8, 1.) + " | " +
								mssSwap.monotoneType (dblX));
		}

		System.out.println ("\tSwap [" + dtToday.addTenor ("60Y") + "] = " +
			FormatUtil.FormatDouble (dfdc.df (dtToday.addTenor ("60Y")), 1, 8, 1.));

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     CASH INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCashComp.length; ++i)
			System.out.println ("\t[" + aCashComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aCashComp[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dfdc, null, null, null, null, null, null),
						null, "Rate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblCashQuote[i], 1, 6, 1.));

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     SWAP INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aSwapComp.length; ++i)
			System.out.println ("\t[" + aSwapComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aSwapComp[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dfdc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblSwapQuote[i], 1, 6, 1.));

		TurnListDiscountFactor tldc = new TurnListDiscountFactor();

		tldc.addTurn (new Turn (
			dtToday.addTenor ("5Y").getJulian(),
			dtToday.addTenor ("40Y").getJulian(),
			0.001));

		dfdc.setTurns (tldc);

		System.out.println ("\n\t-------------------------------");

		System.out.println ("\t     SWAP DF            DFDC");

		System.out.println ("\t-------------------------------");

		for (double dblX = mssSwap.getLeftPredictorOrdinateEdge(); dblX <= mssSwap.getRightPredictorOrdinateEdge();
			dblX += 0.05 * (mssSwap.getRightPredictorOrdinateEdge() - mssSwap.getLeftPredictorOrdinateEdge())) {
				System.out.println ("\tSwap [" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (dfdc.df (dblX), 1, 8, 1.));
		}
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		SegmentCustomBuilderControl prbpPolynomial = new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (4),
			SegmentDesignInelasticControl.Create (2, 2),
			new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)));

		SplineLinearDiscountCurve (prbpPolynomial);
	}
}
