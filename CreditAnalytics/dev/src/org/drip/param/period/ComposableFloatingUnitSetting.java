
package org.drip.param.period;

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
 * ComposableFloatingUnitSetting contains the cash flow periods' composable sub period details.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComposableFloatingUnitSetting {
	private java.lang.String _strTenor = "";
	private int _iEdgeDateSequenceScheme = -1;
	private int _iReferencePeriodArrearsType = -1;
	private double _dblSpread = java.lang.Double.NaN;
	private org.drip.state.identifier.ForwardLabel _forwardLabel = null;
	private org.drip.analytics.daycount.DateAdjustParams _dapEdge = null;
	private org.drip.analytics.daycount.DateAdjustParams _dapForwardFixing = null;

	/**
	 * ComposableFloatingUnitSetting constructor
	 * 
	 * @param strTenor Unit Tenor
	 * @param iEdgeDateSequenceScheme Edge Date Generation Scheme
	 * @param dapEdge Date Adjust Parameter Settings for the Edge Dates
	 * @param forwardLabel Forward Label
	 * @param iReferencePeriodArrearsType Reference Period Arrears Type
	 * @param dapForwardFixing Date Adjust Parameter Settings for the Forward Fixing Date
	 * @param dblSpread Floater Spread
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public ComposableFloatingUnitSetting (
		final java.lang.String strTenor,
		final int iEdgeDateSequenceScheme,
		final org.drip.analytics.daycount.DateAdjustParams dapEdge,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final int iReferencePeriodArrearsType,
		final org.drip.analytics.daycount.DateAdjustParams dapForwardFixing,
		final double dblSpread)
		throws java.lang.Exception
	{
		if (null == (_strTenor = strTenor) || _strTenor.isEmpty() || null == (_forwardLabel = forwardLabel)
			|| !org.drip.quant.common.NumberUtil.IsValid (_dblSpread = dblSpread))
			throw new java.lang.Exception ("ComposableFloatingUnitSetting ctr: Invalid Inputs");

		_dapEdge = dapEdge;
		_dapForwardFixing = dapForwardFixing;
		_iEdgeDateSequenceScheme = iEdgeDateSequenceScheme;
		_iReferencePeriodArrearsType = iReferencePeriodArrearsType;
	}

	/**
	 * Retrieve the Tenor
	 * 
	 * @return The Tenor
	 */

	public java.lang.String tenor()
	{
		return _strTenor;
	}

	/**
	 * Retrieve the Edge Date Generation Scheme
	 * 
	 * @return The Edge Date Generation Scheme
	 */

	public int edgeDateSequenceScheme()
	{
		return _iEdgeDateSequenceScheme;
	}

	/**
	 * Retrieve the Edge Date Adjust Parameters
	 * 
	 * @return The Edge Date Adjust Parameters
	 */

	public org.drip.analytics.daycount.DateAdjustParams dapEdge()
	{
		return _dapEdge;
	}

	/**
	 * Retrieve the Forward Label
	 * 
	 * @return The Forward Label
	 */

	public org.drip.state.identifier.ForwardLabel forwardLabel()
	{
		return _forwardLabel;
	}

	/**
	 * Retrieve the Reference Period Arrears Type
	 * 
	 * @return The Reference Period Arrears Type
	 */

	public int referencePeriodArrearsType()
	{
		return _iReferencePeriodArrearsType;
	}

	/**
	 * Retrieve the Forward Fixing Date Adjust Parameters
	 * 
	 * @return The Forward Fixing Date Adjust Parameters
	 */

	public org.drip.analytics.daycount.DateAdjustParams dapForwardFixing()
	{
		return _dapForwardFixing;
	}

	/**
	 * Retrieve the Floating Unit Spread
	 * 
	 * @return The Floating Unit Spread
	 */

	public double spread()
	{
		return _dblSpread;
	}
}
