
package org.drip.spline.params;

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
 * SegmentCustomBuilderControl holds the parameters the guide the creation/behavior of the segment. It holds the
 *  segment elastic/inelastic parameters and the named basis function set.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SegmentCustomBuilderControl {
	private java.lang.String _strBasisSpline = "";
	private org.drip.spline.basis.FunctionSetBuilderParams _fsbp = null;
	private org.drip.spline.params.ResponseScalingShapeControl _rssc = null;
	private org.drip.spline.params.SegmentDesignInelasticControl _sdic = null;

	/**
	 * SegmentCustomBuilderControl constructor
	 * 
	 * @param strBasisSpline Named Segment Basis Spline
	 * @param fsbp Segment Basis Set Construction Parameters
	 * @param sdic Segment Design Inelastic Parameters
	 * @param rssc Segment Shape Controller
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public SegmentCustomBuilderControl (
		final java.lang.String strBasisSpline,
		final org.drip.spline.basis.FunctionSetBuilderParams fsbp,
		final org.drip.spline.params.SegmentDesignInelasticControl sdic,
		final org.drip.spline.params.ResponseScalingShapeControl rssc)
		throws java.lang.Exception
	{
		if (null == (_strBasisSpline = strBasisSpline) || null == (_fsbp = fsbp) || null == (_sdic = sdic))
			throw new java.lang.Exception ("SegmentCustomBuilderControl ctr => Invalid Inputs");

		_rssc = rssc;
	}

	/**
	 * Retrieve the Basis Spline Name
	 * 
	 * @return The Basis Spline Name
	 */

	public java.lang.String basisSpline()
	{
		return _strBasisSpline;
	}

	/**
	 * Retrieve the Basis Set Parameters
	 * 
	 * @return The Basis Set Parameters
	 */

	public org.drip.spline.basis.FunctionSetBuilderParams basisSetParams()
	{
		return _fsbp;
	}

	/**
	 * Retrieve the Segment Inelastic Parameters
	 * 
	 * @return The Segment Inelastic Parameters
	 */

	public org.drip.spline.params.SegmentDesignInelasticControl inelasticParams()
	{
		return _sdic;
	}

	/**
	 * Retrieve the Segment Shape Controller
	 * 
	 * @return The Segment Shape Controller
	 */

	public org.drip.spline.params.ResponseScalingShapeControl shapeController()
	{
		return _rssc;
	}
}
