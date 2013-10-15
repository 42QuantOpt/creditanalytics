
package org.drip.math.segment;

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
 * DesignInelasticParams implements basis per-segment inelastic parameter set. Currently it contains Ck and
 *  roughness penalty derivative order.
 *
 * @author Lakshmi Krishnamurthy
 */

public class DesignInelasticParams {
	private int _iCk = -1;
	private org.drip.math.segment.FitnessPenaltyParams _fpp = null;
	private org.drip.math.segment.CurvaturePenaltyParams _cpp = null;

	/**
	 * Create the C2 Design Inelastic Params
	 * 
	 * @return DesignInelasticParams instance
	 */

	public static final DesignInelasticParams MakeC2DesignInelasticParams()
	{
		try {
			return new DesignInelasticParams (2, new org.drip.math.segment.CurvaturePenaltyParams (2, 1.),
				null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create the Design Inelastic Parameters for the desired Ck Criterion and the Roughness Penalty Order
	 * 
	 * @param iCk Continuity Order
	 * @param iRoughnessPenaltyDerivativeOrder Roughness Penalty Derivative Order
	 * 
	 * @return DesignInelasticParams instance
	 */

	public static final DesignInelasticParams Create (
		final int iCk,
		final int iRoughnessPenaltyDerivativeOrder)
	{
		try {
			return new DesignInelasticParams (iCk, new org.drip.math.segment.CurvaturePenaltyParams
				(iRoughnessPenaltyDerivativeOrder, 1.), null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private DesignInelasticParams (
		final int iCk,
		final org.drip.math.segment.CurvaturePenaltyParams cpp,
		final org.drip.math.segment.FitnessPenaltyParams fpp)
		throws java.lang.Exception
	{
		if (0 > (_iCk = iCk))
			throw new java.lang.Exception ("DesignInelasticParams ctr: Invalid Inputs");

		_cpp = cpp;
		_fpp = fpp;
	}

	/**
	 * Retrieve the Continuity Order
	 * 
	 * @return The Continuity Order
	 */

	public int getCk()
	{
		return _iCk;
	}

	/**
	 * Retrieve the Fitness Penalty Parameters
	 * 
	 * @return The Fitness Penalty Parameters
	 */

	public org.drip.math.segment.FitnessPenaltyParams getFPP()
	{
		return _fpp;
	}

	/**
	 * Retrieve the Curvature Penalty Parameters
	 * 
	 * @return The Curvature Penalty Parameters
	 */

	public org.drip.math.segment.CurvaturePenaltyParams getCPP()
	{
		return _cpp;
	}
}
