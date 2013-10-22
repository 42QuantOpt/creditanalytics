
package org.drip.math.pchip;

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
 * LocalControlRegimeBuilder exports Regime creation/calibration methods to generate customized basis splines, with
 *  customized segment behavior using the segment control.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LocalControlRegimeBuilder {

	/**
	 * Create a Regime off of Hermite Splines from the specified the Predictor Ordinates, the Response
	 *  Values, the Custom Slopes, and the Segment Builder Parameters.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param adblCustomSlope Array of Custom Slopes
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Calibration Set up Mode NATURAL | FINANCIAL | FLOATING | NOTAKNOT
	 * 
	 * @return The Instance of the Hermite Spline Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CustomSlopeHermiteSpline (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final double[] adblCustomSlope,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode)
	{
		org.drip.math.regime.MultiSegmentRegime regime =
			org.drip.math.regime.RegimeBuilder.CreateUncalibratedRegimeEstimator (strName,
				adblPredictorOrdinate, aPRBP);

		if (null == regime || null == adblResponseValue || null == adblCustomSlope) return null;

		int iNumResponseValue = adblResponseValue.length;
		org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDLeft = new
			org.drip.math.segment.PredictorOrdinateResponseDerivative[iNumResponseValue - 1];
		org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDRight = new
			org.drip.math.segment.PredictorOrdinateResponseDerivative[iNumResponseValue - 1];

		if (1 >= iNumResponseValue || adblPredictorOrdinate.length != iNumResponseValue ||
			adblCustomSlope.length != iNumResponseValue)
			return null;

		for (int i = 0; i < iNumResponseValue; ++i) {
			org.drip.math.segment.PredictorOrdinateResponseDerivative pord = null;

			try {
				pord = new org.drip.math.segment.PredictorOrdinateResponseDerivative (adblResponseValue[i],
					new double[] {adblCustomSlope[i]});
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (0 == i)
				aPORDLeft[i] = pord;
			else if (iNumResponseValue - 1 == i)
				aPORDRight[i - 1] = pord;
			else {
				aPORDLeft[i] = pord;
				aPORDRight[i - 1] = pord;
			}
		}

		return regime.setupHermite (aPORDLeft, aPORDRight, null, fwr, iSetupMode) ? regime : null;
	}

	/**
	 * Create Hermite/Bessel C1 Cubic Spline Regime
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * @param bEliminateSpuriousExtrema TRUE => Eliminate Spurious Extrema
	 * @param bApplyMonotoneFilter TRUE => Apply Monotone Filter
	 * 
	 * @return Hermite/Bessel C1 Cubic Spline Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateBesselCubicSplineRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode,
		final boolean bEliminateSpuriousExtrema,
		final boolean bApplyMonotoneFilter)
	{
		org.drip.math.pchip.LocalControlRegime lcr = org.drip.math.pchip.LocalControlRegime.Create
			(adblPredictorOrdinate, adblResponseValue, org.drip.math.pchip.LocalControlRegime.C1_BESSEL,
				bEliminateSpuriousExtrema, bApplyMonotoneFilter);

		return null == lcr ? null : CustomSlopeHermiteSpline (strName, adblPredictorOrdinate,
			adblResponseValue, lcr.C1(), aPRBP, fwr, iSetupMode);
	}

	/**
	 * Create Hyman (1983) Monotone Preserving Regime. The reference is:
	 * 
	 * 	Hyman (1983) Accurate Monotonicity Preserving Cubic Interpolation -
	 *  	SIAM J on Numerical Analysis 4 (4), 645-654.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * @param bEliminateSpuriousExtrema TRUE => Eliminate Spurious Extrema
	 * @param bApplyMonotoneFilter TRUE => Apply Monotone Filter
	 * 
	 * @return Hyman (1983) Monotone Preserving Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateHyman83MonotoneRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode,
		final boolean bEliminateSpuriousExtrema,
		final boolean bApplyMonotoneFilter)
	{
		org.drip.math.pchip.LocalControlRegime lcr = org.drip.math.pchip.LocalControlRegime.Create
			(adblPredictorOrdinate, adblResponseValue, org.drip.math.pchip.LocalControlRegime.C1_HYMAN83,
				bEliminateSpuriousExtrema, bApplyMonotoneFilter);

		return null == lcr ? null : CustomSlopeHermiteSpline (strName, adblPredictorOrdinate,
			adblResponseValue, lcr.C1(), aPRBP, fwr, iSetupMode);
	}

	/**
	 * Create Hyman (1989) enhancement to the Hyman (1983) Monotone Preserving Regime. The reference is:
	 * 
	 * 	Doherty, Edelman, and Hyman (1989) Non-negative, monotonic, or convexity preserving cubic and quintic
	 *  	Hermite interpolation - Mathematics of Computation 52 (186), 471-494.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * @param bEliminateSpuriousExtrema TRUE => Eliminate Spurious Extrema
	 * @param bApplyMonotoneFilter TRUE => Apply Monotone Filter
	 * 
	 * @return Hyman (1989) Monotone Preserving Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateHyman89MonotoneRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode,
		final boolean bEliminateSpuriousExtrema,
		final boolean bApplyMonotoneFilter)
	{
		org.drip.math.pchip.LocalControlRegime lcr = org.drip.math.pchip.LocalControlRegime.Create
			(adblPredictorOrdinate, adblResponseValue, org.drip.math.pchip.LocalControlRegime.C1_HYMAN89,
				bEliminateSpuriousExtrema, bApplyMonotoneFilter);

		return null == lcr ? null : CustomSlopeHermiteSpline (strName, adblPredictorOrdinate,
			adblResponseValue, lcr.C1(), aPRBP, fwr, iSetupMode);
	}

	/**
	 * Create the Harmonic Monotone Preserving Regime. The reference is:
	 * 
	 * 	Fritcsh and Butland (1984) A Method for constructing local monotonic piece-wise cubic interpolants -
	 *  	SIAM J on Scientific and Statistical Computing 5, 300-304.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * @param bEliminateSpuriousExtrema TRUE => Eliminate Spurious Extrema
	 * @param bApplyMonotoneFilter TRUE => Apply Monotone Filter
	 * 
	 * @return Harmonic Monotone Preserving Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateHarmonicMonotoneRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode,
		final boolean bEliminateSpuriousExtrema,
		final boolean bApplyMonotoneFilter)
	{
		org.drip.math.pchip.LocalControlRegime lcr = org.drip.math.pchip.LocalControlRegime.Create
			(adblPredictorOrdinate, adblResponseValue, org.drip.math.pchip.LocalControlRegime.C1_HARMONIC,
				bEliminateSpuriousExtrema, bApplyMonotoneFilter);

		return null == lcr ? null : CustomSlopeHermiteSpline (strName, adblPredictorOrdinate,
			adblResponseValue, lcr.C1(), aPRBP, fwr, iSetupMode);
	}

	/**
	 * Create the Van Leer Limiter Regime. The reference is:
	 * 
	 * 	Van Leer (1974) Towards the Ultimate Conservative Difference Scheme. II - Monotonicity and
	 * 		Conservation combined in a Second-Order Scheme, Journal of Computational Physics 14 (4), 361-370.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * @param bEliminateSpuriousExtrema TRUE => Eliminate Spurious Extrema
	 * @param bApplyMonotoneFilter TRUE => Apply Monotone Filter
	 * 
	 * @return The Van Leer Limiter Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateVanLeerLimiterRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode,
		final boolean bEliminateSpuriousExtrema,
		final boolean bApplyMonotoneFilter)
	{
		org.drip.math.pchip.LocalControlRegime lcr = org.drip.math.pchip.LocalControlRegime.Create
			(adblPredictorOrdinate, adblResponseValue, org.drip.math.pchip.LocalControlRegime.C1_VAN_LEER,
				bEliminateSpuriousExtrema, bApplyMonotoneFilter);

		return null == lcr ? null : CustomSlopeHermiteSpline (strName, adblPredictorOrdinate,
			adblResponseValue, lcr.C1(), aPRBP, fwr, iSetupMode);
	}

	/**
	 * Create the Kruger Regime. The reference is:
	 * 
	 * 	Kruger (2002) Constrained Cubic Spline Interpolations for Chemical Engineering Application,
	 *  	http://www.korf.co.uk/spline.pdf
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * @param bEliminateSpuriousExtrema TRUE => Eliminate Spurious Extrema
	 * @param bApplyMonotoneFilter TRUE => Apply Monotone Filter
	 * 
	 * @return The Kruger Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateKrugerRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode,
		final boolean bEliminateSpuriousExtrema,
		final boolean bApplyMonotoneFilter)
	{
		org.drip.math.pchip.LocalControlRegime lcr = org.drip.math.pchip.LocalControlRegime.Create
			(adblPredictorOrdinate, adblResponseValue, org.drip.math.pchip.LocalControlRegime.C1_KRUGER,
				bEliminateSpuriousExtrema, bApplyMonotoneFilter);

		return null == lcr ? null : CustomSlopeHermiteSpline (strName, adblPredictorOrdinate,
			adblResponseValue, lcr.C1(), aPRBP, fwr, iSetupMode);
	}

	/**
	 * Create the Huynh Le Floch Limiter Regime. The reference is:
	 * 
	 * 	Huynh (1993) Accurate Monotone Cubic Interpolation, SIAM J on Numerical Analysis 30 (1), 57-100.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * @param bEliminateSpuriousExtrema TRUE => Eliminate Spurious Extrema
	 * @param bApplyMonotoneFilter TRUE => Apply Monotone Filter
	 * 
	 * @return The Huynh Le Floch Limiter Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateHuynhLeFlochLimiterRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode,
		final boolean bEliminateSpuriousExtrema,
		final boolean bApplyMonotoneFilter)
	{
		org.drip.math.pchip.LocalControlRegime lcr = org.drip.math.pchip.LocalControlRegime.Create
			(adblPredictorOrdinate, adblResponseValue, org.drip.math.pchip.LocalControlRegime.C1_KRUGER,
				bEliminateSpuriousExtrema, bApplyMonotoneFilter);

		return null == lcr ? null : CustomSlopeHermiteSpline (strName, adblPredictorOrdinate,
			adblResponseValue, lcr.C1(), aPRBP, fwr, iSetupMode);
	}

	/**
	 * Generate the local control C1 Slope using the Akima Cubic Algorithm. The reference is:
	 * 
	 * 	Akima (1970): A New Method of Interpolation and Smooth Curve Fitting based on Local Procedures,
	 * 		Journal of the Association for the Computing Machinery 17 (4), 589-602.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * @param bEliminateSpuriousExtrema TRUE => Eliminate Spurious Extrema
	 * @param bApplyMonotoneFilter TRUE => Apply Monotone Filter
	 * 
	 * @return The Akima Local Control Regime Instance
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateAkimaRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode,
		final boolean bEliminateSpuriousExtrema,
		final boolean bApplyMonotoneFilter)
	{
		org.drip.math.pchip.LocalControlRegime lcr = org.drip.math.pchip.LocalControlRegime.Create
			(adblPredictorOrdinate, adblResponseValue, org.drip.math.pchip.LocalControlRegime.C1_AKIMA,
				bEliminateSpuriousExtrema, bApplyMonotoneFilter);

		return null == lcr ? null : CustomSlopeHermiteSpline (strName, adblPredictorOrdinate,
			adblResponseValue, lcr.C1(), aPRBP, fwr, iSetupMode);
	}
}
