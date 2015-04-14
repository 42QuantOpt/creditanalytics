
package org.drip.spaces.tensor;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * GeneralizedMultidimensionalVectorSpace exposes the basic Properties of the General R^d Vector Space.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class GeneralizedMultidimensionalVectorSpace implements
	org.drip.spaces.tensor.GeneralizedVectorSpace
{
	private org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace[] _aGUVS = null;

	protected GeneralizedMultidimensionalVectorSpace (
		final org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace[] aGUVS)
		throws java.lang.Exception
	{
		if (null == (_aGUVS = aGUVS))
			throw new java.lang.Exception ("GeneralizedMultidimensionalVectorSpace ctr: Invalid Inputs");

		int iDimension = _aGUVS.length;

		if (0 == iDimension)
			throw new java.lang.Exception ("GeneralizedMultidimensionalVectorSpace ctr: Invalid Inputs");

		for (int i = 0; i < iDimension; ++i) {
			if (null == _aGUVS[i])
				throw new java.lang.Exception ("GeneralizedMultidimensionalVectorSpace ctr: Invalid Inputs");
		}
	}

	/**
	 * Retrieve the Dimension of the Space
	 *  
	 * @return The Dimension of the Space
	 */

	public int dimension()
	{
		return _aGUVS.length;
	}

	/**
	 * Retrieve the Array of the Underlying Unidimensional Vector Spaces
	 * 
	 * @return The Array of the Underlying Unidimensional Vector Spaces
	 */

	public org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace[] vectorSpaces()
	{
		return _aGUVS;
	}

	/**
	 * Validate the Input Instance
	 * 
	 * @param adblInstance The Input Instance
	 * 
	 * @return TRUE => Instance is a Valid Entry in the Space
	 */

	public boolean validateInstance (
		final double[] adblInstance)
	{
		if (null == adblInstance) return false;

		int iDimension = _aGUVS.length;

		if (adblInstance.length != iDimension) return false;

		for (int i = 0; i < iDimension; ++i) {
			if (!_aGUVS[i].validateInstance (adblInstance[i])) return false;
		}

		return true;
	}

	@Override public boolean match (
		final org.drip.spaces.tensor.GeneralizedVectorSpace gvsOther)
	{
		if (null == gvsOther || !(gvsOther instanceof GeneralizedMultidimensionalVectorSpace)) return false;

		GeneralizedMultidimensionalVectorSpace gmvsOther = (GeneralizedMultidimensionalVectorSpace) gvsOther;

		int iDimensionOther = gmvsOther.dimension();

		if (iDimensionOther != dimension()) return false;

		org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace[] aGUVSOther = gmvsOther.vectorSpaces();

		for (int i = 0; i < iDimensionOther; ++i) {
			if (!aGUVSOther[i].match (_aGUVS[i])) return false;
		}

		return true;
	}

	@Override public boolean subset (
		final org.drip.spaces.tensor.GeneralizedVectorSpace gvsOther)
	{
		if (null == gvsOther || !(gvsOther instanceof GeneralizedMultidimensionalVectorSpace)) return false;

		GeneralizedMultidimensionalVectorSpace gmvsOther = (GeneralizedMultidimensionalVectorSpace) gvsOther;

		org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace[] aGUVSOther = gmvsOther.vectorSpaces();

		int iDimensionOther = _aGUVS.length;

		for (int i = 0; i < iDimensionOther; ++i) {
			if (!aGUVSOther[i].match (_aGUVS[i])) return false;
		}

		return true;
	}
}
