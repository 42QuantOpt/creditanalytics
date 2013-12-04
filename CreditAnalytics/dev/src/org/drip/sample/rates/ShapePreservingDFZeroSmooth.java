
package org.drip.sample.rates;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.param.creator.*;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableComponent;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.*;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;
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

public class ShapePreservingDFZeroSmooth {
	private static final CalibratableComponent[] CashInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final java.lang.String[] astrTenor)
		throws Exception
	{
		CalibratableComponent[] aCash = new CalibratableComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aCash[i] = CashBuilder.CreateCash (dtEffective, dtEffective.addTenorAndAdjust (astrTenor[i], "MXN"), "MXN");

		return aCash;
	}

	private static final CalibratableComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor)
		throws Exception
	{
		CalibratableComponent[] aSwap = new CalibratableComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aSwap[i] = RatesStreamBuilder.CreateIRS (
				dtEffective,
				dtEffective.addTenorAndAdjust (astrTenor[i], "MXN"),
				0.,
				"MXN",
				"MXN-LIBOR-6M",
				"MXN");

		return aSwap;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today().addTenorAndAdjust ("0D", "MXN");

		CalibratableComponent[] aCashComp = CashInstrumentsFromMaturityDays (
			dtToday,
			new java.lang.String[] {"1M"});

		double[] adblCashQuote = new double[] {0.0403};

		StretchRepresentationSpec rrsCash = StretchRepresentationSpec.CreateStretchBuilderSet (
			"CASH",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aCashComp,
			"Rate",
			adblCashQuote,
			null);

		CalibratableComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (dtToday, new java.lang.String[]
			{"3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y"});

		double[] adblSwapQuote = new double[]
			{0.0396, 0.0387, 0.0388, 0.0389, 0.04135, 0.04455, 0.0486, 0.0526, 0.0593, 0.0649, 0.0714596, 0.0749596, 0.0776};

		StretchRepresentationSpec rrsSwap = StretchRepresentationSpec.CreateStretchBuilderSet (
			"SWAP",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aSwapComp,
			"Rate",
			adblSwapQuote,
			null);

		StretchRepresentationSpec[] aRRS = new StretchRepresentationSpec[] {rrsCash, rrsSwap};

		LinearCurveCalibrator lcc = new LinearCurveCalibrator (
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_EXPONENTIAL_MIXTURE,
				new ExponentialMixtureSetParams (new double[] {0.01, 0.05, 0.25}),
				SegmentDesignInelasticControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.))),
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null);

		GlobalControlCurveParams gccp = new GlobalControlCurveParams (
			org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentDesignInelasticControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.))),
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null);

		LocalControlCurveParams lccp = new LocalControlCurveParams (
			org.drip.spline.pchip.LocalMonotoneCkGenerator.C1_BESSEL,
			org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentDesignInelasticControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.))),
			MultiSegmentSequence.CALIBRATE,
			null,
			false,
			false);

		DiscountCurve dcShapePreserving = RatesScenarioCurveBuilder.ShapePreservingBuild (
			lcc,
			aRRS,
			new ValuationParams (dtToday, dtToday, "MXN"),
			null,
			null,
			null);

		DiscountCurve dcGloballySmooth = RatesScenarioCurveBuilder.SmoothingGlobalControlBuild (
			dcShapePreserving,
			lcc,
			gccp,
			aRRS,
			new ValuationParams (dtToday, dtToday, "MXN"),
			null,
			null,
			null);

		DiscountCurve dcLocallySmooth = RatesScenarioCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccp,
			aRRS,
			new ValuationParams (dtToday, dtToday, "MXN"),
			null,
			null,
			null);

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t               CASH INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   | SMOOTHING #1  | SMOOTHING #2  |  INPUT QUOTE  ");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCashComp.length; ++i)
			System.out.println ("\t[" + aCashComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (
					aCashComp[i].calcMeasureValue (
						new ValuationParams (dtToday, dtToday, "MXN"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcShapePreserving, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aCashComp[i].calcMeasureValue (
						new ValuationParams (dtToday, dtToday, "MXN"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcGloballySmooth, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aCashComp[i].calcMeasureValue (
						new ValuationParams (dtToday, dtToday, "MXN"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcLocallySmooth, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (adblCashQuote[i], 1, 6, 1.)
			);

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t               SWAP INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   | SMOOTHING #1  | SMOOTHING #2  |  INPUT QUOTE  ");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aSwapComp.length; ++i)
			System.out.println ("\t[" + aSwapComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (
					aSwapComp[i].calcMeasureValue (
						new ValuationParams (dtToday, dtToday, "MXN"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcShapePreserving, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aSwapComp[i].calcMeasureValue (
						new ValuationParams (dtToday, dtToday, "MXN"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcGloballySmooth, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aSwapComp[i].calcMeasureValue (
						new ValuationParams (dtToday, dtToday, "MXN"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcLocallySmooth, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (adblSwapQuote[i], 1, 6, 1.)
			);

		CalibratableComponent[] aCC = SwapInstrumentsFromMaturityTenor (dtToday, new java.lang.String[]
			{"3Y", "6Y", "9Y", "12Y", "15Y", "18Y", "21Y", "24Y", "27Y", "30Y"});

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t           BESPOKE SWAPS PAR RATE");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   |  SMOOTHING #1 |  SMOOTHING #2");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCC.length; ++i)
			System.out.println ("\t[" + aCC[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (
					aCC[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "MXN"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dcShapePreserving, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aCC[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "MXN"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dcGloballySmooth, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aCC[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "MXN"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dcLocallySmooth, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.)
			);
	}
}
