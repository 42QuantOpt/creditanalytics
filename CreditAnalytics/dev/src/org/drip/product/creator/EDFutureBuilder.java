
package org.drip.product.creator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * EDFutureBuilder contains the suite of helper functions for creating the EDFuture product and product pack
 *  from the parameters/codes/byte array streams. It also contains function to construct EDF codes and the
 *  EDF product from code.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class EDFutureBuilder {

	/**
	 * Creates the EDF Code given a effective date
	 * 
	 * @param dblEffective Double representing the Effective JulianDate
	 * 
	 * @return EDF Code String
	 */

	public static java.lang.String MakeBaseEDFCode (
		final double dblEffective)
	{
		int iMonth = 0;
		java.lang.String strEDFCode = "ED";

		try {
			iMonth = org.drip.analytics.date.JulianDate.Month (dblEffective);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (org.drip.analytics.date.JulianDate.MARCH == iMonth)
			strEDFCode = strEDFCode + "H";
		else if (org.drip.analytics.date.JulianDate.JUNE == iMonth)
			strEDFCode = strEDFCode + "M";
		else if (org.drip.analytics.date.JulianDate.SEPTEMBER == iMonth)
			strEDFCode = strEDFCode + "U";
		else if (org.drip.analytics.date.JulianDate.DECEMBER == iMonth)
			strEDFCode = strEDFCode + "Z";
		else
			return null;

		try {
			return strEDFCode + (org.drip.analytics.date.JulianDate.Year (dblEffective) % 10);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate a EDF pack with the specified number of contracts
	 * 
	 * @param dt Spot date specifying the contract issue
	 * @param iNumEDF Number of contracts
	 * @param strCurrency Contract currency string
	 * 
	 * @return Array of EDF product
	 */

	public static org.drip.product.rates.EDFComponent[] GenerateEDPack (
		final org.drip.analytics.date.JulianDate dt,
		final int iNumEDF,
		final java.lang.String strCurrency)
	{
		if (0 == iNumEDF || null == dt) return null;

		org.drip.product.rates.EDFComponent[] aEDF = new org.drip.product.rates.EDFComponent[iNumEDF];

		try {
			org.drip.analytics.date.JulianDate dtEDFStart = dt.firstEDFStartDate (3);

			for (int i = 0; i < iNumEDF; ++i) {
				org.drip.analytics.date.JulianDate dtEDFMaturity = dtEDFStart.addMonths (3);

				(aEDF[i] = new org.drip.product.rates.EDFComponent (dtEDFStart, dtEDFMaturity,
					org.drip.state.identifier.ForwardLabel.Create (strCurrency, "LIBOR", "3M"), strCurrency,
						"Act/360", strCurrency)).setPrimaryCode (MakeBaseEDFCode (dtEDFStart.julian()));

				dtEDFStart = dtEDFStart.addMonths (3);
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return aEDF;
	}

	/**
	 * Create an EDF product from the effective and maturity dates, and the IR curve
	 * 
	 * @param dtEffective JulianDate effective
	 * @param dtMaturity JulianDate Maturity
	 * @param strIR IR curve name
	 * 
	 * @return EDF product
	 */

	public static final org.drip.product.definition.RatesComponent CreateEDF (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final java.lang.String strIR)
	{
		try {
			org.drip.product.definition.RatesComponent edf = new org.drip.product.rates.EDFComponent
				(dtEffective, dtMaturity, org.drip.state.identifier.ForwardLabel.Create (strIR, "LIBOR",
					"3M"), strIR, "Act/360", strIR);

			edf.setPrimaryCode (MakeBaseEDFCode (dtEffective.julian()));

			return edf;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an EDF product from the effective date, the tenor, and the IR curve
	 * 
	 * @param dtEffective JulianDate effective
	 * @param strTenor Tenor string
	 * @param strIR IR curve name
	 * 
	 * @return EDF product
	 */

	public static final org.drip.product.definition.RatesComponent CreateEDF (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String strTenor,
		final java.lang.String strIR)
	{
		try {
			org.drip.product.definition.RatesComponent edf = new org.drip.product.rates.EDFComponent
				(dtEffective, dtEffective.addTenor (strTenor), org.drip.state.identifier.ForwardLabel.Create
					(strIR, "LIBOR", strTenor), strIR, "Act/360", strIR);

			edf.setPrimaryCode (MakeBaseEDFCode (dtEffective.julian()));

			return edf;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an EDF product from the effective date, the product code, and the IR curve
	 * 
	 * @param strFullEDCode EDF product code
	 * @param dt JulianDate effective
	 * @param strIR IR curve name
	 * 
	 * @return EDF product
	 */

	public static final org.drip.product.definition.RatesComponent CreateEDF (
		final java.lang.String strFullEDCode,
		final org.drip.analytics.date.JulianDate dt,
		final java.lang.String strIR)
	{
		try {
			org.drip.product.definition.RatesComponent edf = new org.drip.product.rates.EDFComponent
				(strFullEDCode, dt, strIR);

			edf.setPrimaryCode (strFullEDCode);

			return edf;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a EDFuture Instance from the byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @return EDFuture Instance
	 */

	public static final org.drip.product.definition.RatesComponent FromByteArray (
		final byte[] ab)
	{
		if (null == ab || 0 == ab.length) return null;

		try {
			return new org.drip.product.rates.EDFComponent (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
