
package org.drip.spline.bspline;

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
 * BasisHatPairGenerator implements the generation functionality behind the hat basis function pair.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BasisHatPairGenerator {

	/**
	 * Tension Hyperbolic B Spline Basis Hat Phy and Psy
	 */

	public static final java.lang.String TENSION_HYPERBOLIC = "TENSION_HYPERBOLIC";

	/**
	 * Processed Tension Hyperbolic B Spline Basis Hat Phy and Psy
	 */

	public static final java.lang.String PROCESSED_TENSION_HYPERBOLIC = "PROCESSED_TENSION_HYPERBOLIC";

	/**
	 * Processed Tension Hyperbolic B Spline Basis Hat Phy and Psy
	 */

	public static final java.lang.String PROCESSED_CUBIC_RATIONAL = "PROCESSED_CUBIC_RATIONAL";

	/**
	 * Generate the array of the Hyperbolic Phy and Psy Hat Function Pair
	 * 
	 * @param dblPredictorOrdinateLeading The Leading Predictor Ordinate
	 * @param dblPredictorOrdinateFollowing The Following Predictor Ordinate
	 * @param dblPredictorOrdinateTrailing The Trailing Predictor Ordinate
	 * @param dblTension Tension
	 * 
	 * @return The array of Hyperbolic Phy and Psy Hat Function Pair
	 */

	public static final org.drip.spline.bspline.TensionBasisHat[] HyperbolicTensionHatPair (
		final double dblPredictorOrdinateLeading,
		final double dblPredictorOrdinateFollowing,
		final double dblPredictorOrdinateTrailing,
		final double dblTension)
	{
		try {
			return new org.drip.spline.bspline.TensionBasisHat[] {new
				org.drip.spline.bspline.ExponentialTensionLeftHat (dblTension, dblPredictorOrdinateLeading,
					dblPredictorOrdinateFollowing), new org.drip.spline.bspline.ExponentialTensionRightHat
						(dblTension, dblPredictorOrdinateFollowing, dblPredictorOrdinateTrailing)};
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate the array of the Hyperbolic Phy and Psy Hat Function Pair From their Raw Counterparts
	 * 
	 * @param dblPredictorOrdinateLeading The Leading Predictor Ordinate
	 * @param dblPredictorOrdinateFollowing The Following Predictor Ordinate
	 * @param dblPredictorOrdinateTrailing The Trailing Predictor Ordinate
	 * @param dblTension Tension
	 * 
	 * @return The array of Hyperbolic Phy and Psy Hat Function Pair
	 */

	public static final org.drip.spline.bspline.TensionBasisHat[] ProcessedHyperbolicTensionHatPair (
		final double dblPredictorOrdinateLeading,
		final double dblPredictorOrdinateFollowing,
		final double dblPredictorOrdinateTrailing,
		final double dblTension)
	{
		try {
			return new org.drip.spline.bspline.TensionBasisHat[] {new
				org.drip.spline.bspline.TensionProcessedBasisHat (new
					org.drip.spline.bspline.ExponentialTensionLeftRaw (dblTension,
						dblPredictorOrdinateLeading, dblPredictorOrdinateFollowing), 2), new
							org.drip.spline.bspline.TensionProcessedBasisHat (new
								org.drip.spline.bspline.ExponentialTensionRightRaw (dblTension,
									dblPredictorOrdinateFollowing, dblPredictorOrdinateTrailing), 2)};
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate the array of the Cubic Rational Phy and Psy Hat Function Pair From their Raw Counterparts
	 * 
	 * @param strShapeControlType Type of the Shape Controller to be used - NONE, LINEAR/QUADRATIC Rational
	 * @param dblPredictorOrdinateLeading The Leading Predictor Ordinate
	 * @param dblPredictorOrdinateFollowing The Following Predictor Ordinate
	 * @param dblPredictorOrdinateTrailing The Trailing Predictor Ordinate
	 * @param dblTension Tension
	 * 
	 * @return The array of Cubic Rational Phy and Psy Hat Function Pair
	 */

	public static final org.drip.spline.bspline.TensionBasisHat[] ProcessedCubicRationalHatPair (
		final java.lang.String strShapeControlType,
		final double dblPredictorOrdinateLeading,
		final double dblPredictorOrdinateFollowing,
		final double dblPredictorOrdinateTrailing,
		final double dblTension)
	{
		try {
			return new org.drip.spline.bspline.TensionBasisHat[] {new
				org.drip.spline.bspline.TensionProcessedBasisHat (new
					org.drip.spline.bspline.CubicRationalLeftRaw (strShapeControlType, dblTension,
						dblPredictorOrdinateLeading, dblPredictorOrdinateFollowing), 2), new
							org.drip.spline.bspline.TensionProcessedBasisHat (new
								org.drip.spline.bspline.CubicRationalRightRaw (strShapeControlType,
									dblTension, dblPredictorOrdinateFollowing, dblPredictorOrdinateTrailing),
										2)};
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
