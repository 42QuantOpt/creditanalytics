
package org.drip.analytics.support;

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
 * PeriodHelper exposes several period construction functionality.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class PeriodHelper {

	/**
	 * Period Set Generation Customization - No adjustment on either end
	 */

	public static final int NO_ADJUSTMENT = 0;

	/**
	 * Period Set Generation Customization - Merge the front periods to produce a long front
	 */

	public static final int FULL_FRONT_PERIOD = 1;

	/**
	 * Period Set Generation Customization - Stub (if present) belongs to the front end
	 */

	public static final int LONG_FRONT_STUB = 2;

	/**
	 * Period Set Generation Customization - Stub (if present) belongs to the back end
	 */

	public static final int LONG_BACK_STUB = 4;

	private static final double DAPAdjust (
		final double dblDate,
		final org.drip.analytics.daycount.DateAdjustParams dap)
	{
		if (null == dap) return dblDate;

		try {
			return dap.roll (dblDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return dblDate;
	}

	/**
	 * Create a Reset Period Container that from the List of Daily Reset Periods between the specified Dates
	 * 
	 * @param dblLeft The Left Date
	 * @param dblRight The Right Date
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param strCalendar The Calendar
	 * 
	 * @return The Reset Period Container that uses the List of Daily Reset Periods between the specified
	 * 	Dates
	 */

	public static final org.drip.analytics.period.ResetPeriodContainer DailyResetPeriod (
		final double dblLeft,
		final double dblRight,
		final int iAccrualCompoundingRule,
		final java.lang.String strCalendar)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblLeft) || !org.drip.quant.common.NumberUtil.IsValid
			(dblRight) || dblLeft >= dblRight)
			return null;

		double dblStart = dblLeft;
		double dblEnd = java.lang.Double.NaN;
		org.drip.analytics.period.ResetPeriodContainer rpc = null;

		try {
			dblEnd = new org.drip.analytics.date.JulianDate (dblStart).addBusDays (1, strCalendar).julian();

			rpc = new org.drip.analytics.period.ResetPeriodContainer (iAccrualCompoundingRule);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		while (dblEnd <= dblRight) {
			try {
				if (!rpc.appendResetPeriod (new org.drip.analytics.period.ResetPeriod (dblStart, dblEnd,
					dblStart)))
					return null;

				dblStart = dblEnd;

				dblEnd = new org.drip.analytics.date.JulianDate (dblStart).addBusDays (1,
					strCalendar).julian();
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return rpc;
	}

	/**
	 * Merge the specified Reset Period Lists onto a single Composite Reset Period List
	 * 
	 * @param rpcLeft The Left Reset Period Container
	 * @param rpcRight The Right Reset Period Container
	 * 
	 * @return The Composite Reset Period Container
	 */

	public static final org.drip.analytics.period.ResetPeriodContainer MergeResetPeriods (
		final org.drip.analytics.period.ResetPeriodContainer rpcLeft,
		final org.drip.analytics.period.ResetPeriodContainer rpcRight)
	{
		if (null == rpcLeft || null == rpcRight) return null;

		int iAccrualCompoundingRule = rpcLeft.accrualCompoundingRule();

		if (iAccrualCompoundingRule != rpcRight.accrualCompoundingRule()) return null;

		java.util.List<org.drip.analytics.period.ResetPeriod> lsResetPeriodsLeft = rpcLeft.resetPeriods();

		java.util.List<org.drip.analytics.period.ResetPeriod> lsResetPeriodsRight = rpcRight.resetPeriods();

		if (null == lsResetPeriodsLeft || null == lsResetPeriodsRight) return null;

		int iNumPeriodsLeft = lsResetPeriodsLeft.size();

		int iNumPeriodsRight = lsResetPeriodsRight.size();

		if (0 == iNumPeriodsLeft || 0 == iNumPeriodsRight ||
			lsResetPeriodsLeft.get (iNumPeriodsLeft - 1).end() != lsResetPeriodsRight.get (0).start())
			return null;

		org.drip.analytics.period.ResetPeriodContainer rpc = null;

		try {
			rpc = new org.drip.analytics.period.ResetPeriodContainer (iAccrualCompoundingRule);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (org.drip.analytics.period.ResetPeriod rp : lsResetPeriodsLeft) {
			if (!rpc.appendResetPeriod (rp)) return null;
		}

		for (org.drip.analytics.period.ResetPeriod rp : lsResetPeriodsRight) {
			if (!rpc.appendResetPeriod (rp)) return null;
		}

		return rpc;
	}

	/**
	 * Merge the left and right Cash Flow periods onto a bigger Cash Flow period
	 * 
	 * @param periodLeft Left Cash Flow Period
	 * @param periodRight Right Cash Flow Period
	 * 
	 * @return Merged Cash Flow Period
	 */

	public static final org.drip.analytics.period.CouponPeriod MergeCashFlowPeriods (
		final org.drip.analytics.period.CouponPeriod periodLeft,
		final org.drip.analytics.period.CouponPeriod periodRight)
	{
		if (null == periodLeft || null == periodRight || periodLeft.endDate() != periodRight.startDate())
			return null;

		double dblAccrualStartDate = periodLeft.accrualStartDate();

		int iFreq = periodLeft.freq();

		if (iFreq != periodRight.freq()) return null;
			
		java.lang.String strCouponDC = periodLeft.couponDC();

		if (!org.drip.quant.common.StringUtil.StringMatch (strCouponDC, periodRight.couponDC())) return null;

		java.lang.String strAccrualDC = periodLeft.accrualDC();

		if (!org.drip.quant.common.StringUtil.StringMatch (strAccrualDC, periodRight.accrualDC()))
			return null;

		boolean bCouponEOMAdjustment = periodLeft.couponEODAdjustment();

		if (bCouponEOMAdjustment != periodRight.couponEODAdjustment()) return null;

		boolean bAccrualEOMAdjustment = periodLeft.accrualEODAdjustment();

		if (bAccrualEOMAdjustment != periodRight.accrualEODAdjustment()) return null;

		java.lang.String strCalendar = periodLeft.calendar();

		if (!org.drip.quant.common.StringUtil.StringMatch (strCalendar, periodRight.calendar())) return null;

		java.lang.String strPayCurrency = periodLeft.payCurrency();

		if (!org.drip.quant.common.StringUtil.StringMatch (strPayCurrency, periodRight.payCurrency()))
			return null;

		org.drip.state.identifier.ForwardLabel forwardLabel = periodLeft.forwardLabel();

		if (!org.drip.analytics.support.AnalyticsHelper.LabelMatch (forwardLabel,
			periodRight.forwardLabel()))
			return null;

		org.drip.state.identifier.CreditLabel creditLabel = periodLeft.creditLabel();

		if (!org.drip.analytics.support.AnalyticsHelper.LabelMatch (creditLabel, periodRight.creditLabel()))
			return null;

		try {
			return new org.drip.analytics.period.CouponPeriod (periodLeft.startDate(), periodRight.endDate(),
				dblAccrualStartDate, periodRight.accrualEndDate(), periodRight.payDate(), MergeResetPeriods
					(periodLeft.rpc(), periodRight.rpc()), java.lang.Double.NaN, java.lang.Double.NaN, iFreq,
						periodLeft.accrualDCF (periodRight.accrualStartDate()) + periodRight.couponDCF(),
							strCouponDC, strAccrualDC, bCouponEOMAdjustment, bAccrualEOMAdjustment,
								strCalendar, strPayCurrency, forwardLabel, creditLabel);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/** Fully Customized Generation of the period list backward starting from the end.
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturityUnadjusted Unadjusted Maturity date
	 * @param dapEffective Effective date Date Adjust Parameters
	 * @param dapMaturity Maturity date Date Adjust Parameters
	 * @param dapPeriodStart Period Start date Date Adjust Parameters
	 * @param dapPeriodEnd Period End date Date Adjust Parameters
	 * @param dapAccrualStart Accrual Start date Date Adjust Parameters
	 * @param dapAccrualEnd Accrual End date Date Adjust Parameters
	 * @param dapPay Pay date Date Adjust Parameters
	 * @param dapReset Reset date Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon day count
	 * @param bApplyCpnEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param strAccrualDC Accrual day count
	 * @param bApplyAccEOMAdj Apply end-of-month adjustment to the accrual periods
	 * @param iPSEC Period Set Edge Customizer Setting
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 		determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strPayCurrency Pay Currency
	 * @param forwardLabel The Forward Label
	 * @param creditLabel The Credit Label
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CouponPeriod> BackwardPeriodSingleReset (
		final double dblEffective,
		final double dblMaturityUnadjusted,
		final org.drip.analytics.daycount.DateAdjustParams dapEffective,
		final org.drip.analytics.daycount.DateAdjustParams dapMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodStart,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualStart,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final int iFreq,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final int iPSEC,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final java.lang.String strPayCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblMaturityUnadjusted) || dblEffective >=
				dblMaturityUnadjusted || 0 == iFreq)
			return null;

		double dblMaturity = DAPAdjust (dblMaturityUnadjusted, dapMaturity);

		boolean bFinalPeriod = true;
		boolean bGenerationDone = false;
		double dblPeriodEndDate = dblMaturity;
		java.lang.String strTenor = (12 / iFreq) + "M";
		double dblPeriodStartDate = java.lang.Double.NaN;
		org.drip.analytics.period.CouponPeriod periodFirst = null;
		org.drip.analytics.period.CouponPeriod periodSecond = null;

		try {
			dblPeriodStartDate = new org.drip.analytics.date.JulianDate (dblPeriodEndDate).subtractTenor
				(strTenor).julian();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		java.util.List<org.drip.analytics.period.CouponPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.period.CouponPeriod>();

		while (!bGenerationDone) {
			if (dblPeriodStartDate <= dblEffective) {
				if (FULL_FRONT_PERIOD == iPSEC) dblPeriodStartDate = dblEffective;

				bGenerationDone = true;
			}

			try {
				periodSecond = periodFirst;
				org.drip.analytics.period.ResetPeriodContainer rpc = null;

				double dblAdjustedStartDate = DAPAdjust (dblPeriodStartDate, dapPeriodStart);

				double dblAdjustedEndDate = DAPAdjust (dblPeriodEndDate, dapPeriodEnd);

				double dblAccrualStartDate = DAPAdjust (dblPeriodStartDate, dapAccrualStart);

				double dblAccrualEndDate = bFinalPeriod ? dblPeriodEndDate : DAPAdjust (dblPeriodEndDate,
					dapAccrualEnd);

				if (bFinalPeriod) bFinalPeriod = false;

				double dblDCF = bCouponDCFOffOfFreq ? 1. / iFreq :
					org.drip.analytics.daycount.Convention.YearFraction (dblAccrualStartDate,
						dblAccrualEndDate, strAccrualDC, bApplyAccEOMAdj, dblMaturity, new
							org.drip.analytics.daycount.ActActDCParams (iFreq, dblAccrualStartDate,
								dblAccrualEndDate), strCalendar);

				if (null != forwardLabel&& !(rpc = new org.drip.analytics.period.ResetPeriodContainer
					(org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC)).appendResetPeriod
					(new org.drip.analytics.period.ResetPeriod (dblAdjustedStartDate, dblAdjustedEndDate,
						DAPAdjust (dblPeriodStartDate, dapReset))))
					return null;

				if (dblAdjustedStartDate < dblAdjustedEndDate && dblAccrualStartDate < dblAccrualEndDate)
					lsCashflowPeriod.add (0, periodFirst = new org.drip.analytics.period.CouponPeriod
						(dblAdjustedStartDate, dblAdjustedEndDate, dblAccrualStartDate, dblAccrualEndDate,
							DAPAdjust (dblPeriodEndDate, dapPay), rpc, dblMaturity, java.lang.Double.NaN,
								iFreq, dblDCF, strCouponDC, strAccrualDC, bApplyCpnEOMAdj, bApplyAccEOMAdj,
									strCalendar, strPayCurrency, forwardLabel, creditLabel));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			dblPeriodEndDate = dblPeriodStartDate;

			try {
				dblPeriodStartDate = new org.drip.analytics.date.JulianDate (dblPeriodEndDate).subtractTenor
					(strTenor).julian();
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (LONG_FRONT_STUB != iPSEC || null == periodFirst || null == periodSecond) return lsCashflowPeriod;

		org.drip.analytics.period.CouponPeriod periodMerged = MergeCashFlowPeriods (periodFirst,
			periodSecond);

		if (null == periodMerged) return lsCashflowPeriod;

		lsCashflowPeriod.remove (0);

		lsCashflowPeriod.remove (0);

		lsCashflowPeriod.add (0, periodMerged);

		return lsCashflowPeriod;
	}

	/**
	 * Fully customized Generation of the period list forward starting from the start.
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturityUnadjusted Unadjusted Maturity date
	 * @param dapEffective Effective date Date Adjust Parameters
	 * @param dapMaturity Maturity date Date Adjust Parameters
	 * @param dapPeriodStart Period Start date Date Adjust Parameters
	 * @param dapPeriodEnd Period End date Date Adjust Parameters
	 * @param dapAccrualStart Accrual Start date Date Adjust Parameters
	 * @param dapAccrualEnd Accrual End date Date Adjust Parameters
	 * @param dapPay Pay date Date Adjust Parameters
	 * @param dapReset Reset date Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon day count
	 * @param bApplyCpnEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param strAccrualDC Accrual day count
	 * @param bApplyAccEOMAdj Apply end-of-month adjustment to the accrual periods
	 * @param iPSEC Period Set Edge Customizer Setting
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 		determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strPayCurrency Pay Currency
	 * @param forwardLabel The Forward Label
	 * @param creditLabel The Credit Label
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CouponPeriod> ForwardPeriodSingleReset (
		final double dblEffective,
		final double dblMaturityUnadjusted,
		final org.drip.analytics.daycount.DateAdjustParams dapEffective,
		final org.drip.analytics.daycount.DateAdjustParams dapMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodStart,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualStart,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final int iFreq,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final int iPSEC,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final java.lang.String strPayCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblMaturityUnadjusted) || dblEffective >=
				dblMaturityUnadjusted|| 0 == iFreq)
			return null;

		double dblMaturity = DAPAdjust (dblMaturityUnadjusted, dapMaturity);

		boolean bFinalPeriod = false;
		double dblPeriodDays = 365.25 / iFreq;
		double dblPeriodStartDate = dblEffective;
		org.drip.analytics.period.CouponPeriod periodFinal = null;
		double dblPeriodEndDate = dblPeriodStartDate + dblPeriodDays;
		org.drip.analytics.period.CouponPeriod periodPenultimate = null;

		if (dblPeriodEndDate > dblMaturity) dblPeriodEndDate = dblMaturity;

		java.util.List<org.drip.analytics.period.CouponPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.period.CouponPeriod>();

		while (!bFinalPeriod) {
			if (dblPeriodEndDate >= dblMaturity) {
				bFinalPeriod = true;
				dblPeriodEndDate = dblMaturity;
			}

			try {
				org.drip.analytics.period.ResetPeriodContainer rpc = null;

				if (!bFinalPeriod) {
					double dblAdjustedStartDate = DAPAdjust (dblPeriodStartDate, dapPeriodStart);

					double dblAdjustedEndDate = DAPAdjust (dblPeriodEndDate, dapPeriodStart);

					double dblAccrualStart = DAPAdjust (dblPeriodStartDate, dapAccrualStart);

					double dblAccrualEnd = DAPAdjust (dblPeriodEndDate, dapAccrualEnd);

					double dblDCF = bCouponDCFOffOfFreq ? 1. / iFreq :
						org.drip.analytics.daycount.Convention.YearFraction (dblAccrualStart, dblAccrualEnd,
							strAccrualDC, bApplyAccEOMAdj, dblMaturity, new
								org.drip.analytics.daycount.ActActDCParams (iFreq, dblAccrualStart,
									dblAccrualEnd), strCalendar);

					if (null != forwardLabel && !(rpc = new org.drip.analytics.period.ResetPeriodContainer
						(org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC)).appendResetPeriod
						(new org.drip.analytics.period.ResetPeriod (dblAdjustedStartDate, dblAdjustedEndDate,
							DAPAdjust (dblPeriodStartDate, dapReset))))
						return null;

					lsCashflowPeriod.add (periodPenultimate = new org.drip.analytics.period.CouponPeriod
						(dblAdjustedStartDate, dblAdjustedEndDate, dblAccrualStart, dblAccrualEnd, DAPAdjust
							(dblPeriodEndDate, dapPay), rpc, dblMaturity, java.lang.Double.NaN, iFreq,
								dblDCF, strCouponDC, strAccrualDC, bApplyCpnEOMAdj, bApplyAccEOMAdj,
									strCalendar, strPayCurrency, forwardLabel, creditLabel));
				} else {
					double dblAccrualStart = DAPAdjust (dblPeriodStartDate, dapAccrualStart);

					double dblAdjustedStartDate = DAPAdjust (dblPeriodStartDate, dapPeriodStart);

					double dblDCF = bCouponDCFOffOfFreq ? 1. / iFreq :
						org.drip.analytics.daycount.Convention.YearFraction (dblAccrualStart,
							dblPeriodEndDate, strAccrualDC, bApplyAccEOMAdj, dblMaturity, new
								org.drip.analytics.daycount.ActActDCParams (iFreq, dblAccrualStart,
									dblPeriodEndDate), strCalendar);

					if (null != forwardLabel && !(rpc = new org.drip.analytics.period.ResetPeriodContainer
						(org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC)).appendResetPeriod
						(new org.drip.analytics.period.ResetPeriod (dblAdjustedStartDate, dblPeriodEndDate,
							DAPAdjust (dblPeriodStartDate, dapReset))))
						return null;

					lsCashflowPeriod.add (periodFinal = new org.drip.analytics.period.CouponPeriod
						(dblAdjustedStartDate, dblPeriodEndDate, dblAccrualStart, dblPeriodEndDate, DAPAdjust
							(dblPeriodEndDate, dapPay), rpc, dblMaturity, java.lang.Double.NaN, iFreq,
								dblDCF, strCouponDC, strAccrualDC, bApplyCpnEOMAdj, bApplyAccEOMAdj,
									strCalendar, strPayCurrency, forwardLabel, creditLabel));
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			dblPeriodEndDate = dblPeriodStartDate;
			dblPeriodStartDate = dblPeriodEndDate - dblPeriodDays;
		}

		if (LONG_BACK_STUB != iPSEC || null == periodFinal || null == periodPenultimate)
			return lsCashflowPeriod;

		org.drip.analytics.period.CouponPeriod periodMerged = MergeCashFlowPeriods (periodFinal,
			periodPenultimate);

		if (null == periodMerged) return lsCashflowPeriod;

		lsCashflowPeriod.remove (lsCashflowPeriod.size() - 1);

		lsCashflowPeriod.remove (lsCashflowPeriod.size() - 1);

		lsCashflowPeriod.add (periodMerged);

		return lsCashflowPeriod;
	}

	/**
	 * Fully customized Regular Period List Generation
	 * 
	 * @param dblEffective Effective date
	 * @param strMaturityTenor Maturity Tenor
	 * @param dapEffective Effective date Date Adjust Parameters
	 * @param dapMaturity Maturity date Date Adjust Parameters
	 * @param dapPeriodStart Period Start date Date Adjust Parameters
	 * @param dapPeriodEnd Period End date Date Adjust Parameters
	 * @param dapAccrualStart Accrual Start date Date Adjust Parameters
	 * @param dapAccrualEnd Accrual End date Date Adjust Parameters
	 * @param dapPay Pay date Date Adjust Parameters
	 * @param dapReset Reset date Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon day count
	 * @param bApplyCpnEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param strAccrualDC Accrual day count
	 * @param bApplyAccEOMAdj Apply end-of-month adjustment to the accrual periods
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 		determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strPayCurrency Pay Currency
	 * @param forwardLabel The Forward Label
	 * @param creditlabel The Credit Label
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CouponPeriod> RegularPeriodSingleReset (
		final double dblEffective,
		final java.lang.String strMaturityTenor,
		final org.drip.analytics.daycount.DateAdjustParams dapEffective,
		final org.drip.analytics.daycount.DateAdjustParams dapMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodStart,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualStart,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final int iFreq,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final java.lang.String strPayCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) || null == strMaturityTenor)
			return null;

		boolean bLoopOn = true;
		java.lang.String strPeriodTenor = (12 / iFreq) + "M";
		org.drip.analytics.date.JulianDate dtPeriodStart = null;

		try {
			dtPeriodStart = new org.drip.analytics.date.JulianDate (dblEffective);

		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.analytics.date.JulianDate dtMaturity = dtPeriodStart.addTenor (strMaturityTenor);

		if (null == dtMaturity) return null;

		double dblMaturityDate = dtMaturity.julian();

		org.drip.analytics.date.JulianDate dtPeriodEnd = dtPeriodStart.addTenor (strPeriodTenor);

		if (null == dtPeriodEnd) return null;

		double dblPeriodEndDate = dtPeriodEnd.julian();

		if (dblPeriodEndDate > dblMaturityDate) dblPeriodEndDate = dblMaturityDate;

		java.util.List<org.drip.analytics.period.CouponPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.period.CouponPeriod>();

		while (dblPeriodEndDate <= dblMaturityDate && bLoopOn) {
			if (dblPeriodEndDate >= dblMaturityDate) {
				bLoopOn = false;
				dblPeriodEndDate = dblMaturityDate;
			}

			double dblPeriodStartDate = dtPeriodStart.julian();

			double dblAdjustedStartDate = DAPAdjust (dblPeriodStartDate, dapPeriodStart);

			double dblAdjustedEndDate = DAPAdjust (dblPeriodEndDate, dapPeriodEnd);

			double dblAccrualStart = DAPAdjust (dblPeriodStartDate, dapAccrualStart);

			double dblAccrualEnd = dblPeriodEndDate == dblMaturityDate ? dblPeriodEndDate : DAPAdjust
				(dblPeriodEndDate, dapAccrualStart);

			try {
				org.drip.analytics.period.ResetPeriodContainer rpc = null;

				if (null != forwardLabel && !(rpc = new org.drip.analytics.period.ResetPeriodContainer
					(org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC)).appendResetPeriod
					(new org.drip.analytics.period.ResetPeriod (dblAdjustedStartDate, dblAdjustedEndDate,
						DAPAdjust (dblPeriodStartDate, dapReset))))
					return null;

				double dblDCF = bCouponDCFOffOfFreq ? 1. / iFreq :
					org.drip.analytics.daycount.Convention.YearFraction (dblAccrualStart, dblAccrualEnd,
						strAccrualDC, bApplyAccEOMAdj, dblMaturityDate, new
							org.drip.analytics.daycount.ActActDCParams (iFreq, dblAccrualStart,
								dblAccrualEnd), strCalendar);

				lsCashflowPeriod.add (new org.drip.analytics.period.CouponPeriod (dblAdjustedStartDate,
					dblAdjustedEndDate, dblAccrualStart, dblAccrualEnd, DAPAdjust (dblPeriodEndDate, dapPay),
						rpc, dblMaturityDate, java.lang.Double.NaN, iFreq, dblDCF, strCouponDC, strAccrualDC,
							bApplyCpnEOMAdj, bApplyAccEOMAdj, strCalendar, strPayCurrency, forwardLabel,
								creditLabel));

				dtPeriodStart = dtPeriodEnd;

				if (null == (dtPeriodEnd = dtPeriodStart.addTenor (strPeriodTenor))) return null;

				dblPeriodEndDate = dtPeriodEnd.julian();
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return lsCashflowPeriod;
	}

	/**
	 * Fully customized Regular Period List Generation
	 * 
	 * @param dblEffective Effective date
	 * @param strMaturityTenor Maturity Tenor
	 * @param dapEffective Effective date Date Adjust Parameters
	 * @param dapMaturity Maturity date Date Adjust Parameters
	 * @param dapPeriodStart Period Start date Date Adjust Parameters
	 * @param dapPeriodEnd Period End date Date Adjust Parameters
	 * @param dapAccrualStart Accrual Start date Date Adjust Parameters
	 * @param dapAccrualEnd Accrual End date Date Adjust Parameters
	 * @param dapPay Pay date Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon day count
	 * @param bApplyCpnEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param strAccrualDC Accrual day count
	 * @param bApplyAccEOMAdj Apply end-of-month adjustment to the accrual periods
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 		determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strPayCurrency Pay Currency
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param forwardLabel The Forward Label
	 * @param creditlabel The Credit Label
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CouponPeriod> RegularPeriodDailyReset (
		final double dblEffective,
		final java.lang.String strMaturityTenor,
		final org.drip.analytics.daycount.DateAdjustParams dapEffective,
		final org.drip.analytics.daycount.DateAdjustParams dapMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodStart,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualStart,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final int iFreq,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final java.lang.String strPayCurrency,
		final int iAccrualCompoundingRule,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) || null == strMaturityTenor)
			return null;

		boolean bLoopOn = true;
		java.lang.String strPeriodTenor = (12 / iFreq) + "M";
		org.drip.analytics.date.JulianDate dtPeriodStart = null;

		try {
			dtPeriodStart = new org.drip.analytics.date.JulianDate (dblEffective);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.analytics.date.JulianDate dtMaturity = dtPeriodStart.addTenor (strMaturityTenor);

		if (null == dtMaturity) return null;

		double dblMaturityDate = dtMaturity.julian();

		org.drip.analytics.date.JulianDate dtPeriodEnd = dtPeriodStart.addTenor (strPeriodTenor);

		if (null == dtPeriodEnd) return null;

		java.util.List<org.drip.analytics.period.CouponPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.period.CouponPeriod>();

		double dblPeriodEndDate = dtPeriodEnd.julian();

		if (dblPeriodEndDate > dblMaturityDate) dblPeriodEndDate = dblMaturityDate;

		while (dblPeriodEndDate <= dblMaturityDate && bLoopOn) {
			if (dblPeriodEndDate >= dblMaturityDate) {
				bLoopOn = false;
				dblPeriodEndDate = dblMaturityDate;
			}

			double dblPeriodStartDate = dtPeriodStart.julian();

			double dblAdjustedStartDate = DAPAdjust (dblPeriodStartDate, dapPeriodStart);

			double dblAdjustedEndDate = DAPAdjust (dblPeriodEndDate, dapPeriodEnd);

			double dblAccrualStart = DAPAdjust (dblPeriodStartDate, dapAccrualStart);

			double dblAccrualEnd = dblPeriodEndDate == dblMaturityDate ? dblPeriodEndDate : DAPAdjust
				(dblPeriodEndDate, dapAccrualStart);

			try {
				double dblDCF = bCouponDCFOffOfFreq ? 1. / iFreq :
					org.drip.analytics.daycount.Convention.YearFraction (dblAccrualStart, dblAccrualEnd,
						strAccrualDC, bApplyAccEOMAdj, dblMaturityDate, new
							org.drip.analytics.daycount.ActActDCParams (iFreq, dblAccrualStart,
								dblAccrualEnd), strCalendar);

				lsCashflowPeriod.add (new org.drip.analytics.period.CouponPeriod (dblAdjustedStartDate,
					dblAdjustedEndDate, dblAccrualStart, dblAccrualEnd, DAPAdjust (dblPeriodEndDate, dapPay),
						null != forwardLabel ? DailyResetPeriod (dblAdjustedStartDate, dblAdjustedEndDate,
							iAccrualCompoundingRule, strCalendar) : null, dblMaturityDate,
								java.lang.Double.NaN, iFreq, dblDCF, strCouponDC, strAccrualDC,
									bApplyCpnEOMAdj, bApplyAccEOMAdj, strCalendar, strPayCurrency,
										forwardLabel, creditLabel));

				dtPeriodStart = dtPeriodEnd;

				if (null == (dtPeriodEnd = dtPeriodStart.addTenor (strPeriodTenor))) return null;

				dblPeriodEndDate = dtPeriodEnd.julian();
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return lsCashflowPeriod;
	}

	/** Simplified Generation of the period list backward starting from the end.
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturity Maturity date
	 * @param dap Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strDayCount Day Count
	 * @param iPSEC Period Set Edge Customizer Setting
	 * @param bMergeLeadingPeriods - TRUE - Merge the Front 2 coupon periods
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 		determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strPayCurrency Pay Currency
	 * @param forwardLabel The Forward Label
	 * @param creditLabel The Credit Label
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CouponPeriod> BackwardPeriodSingleReset (
		final double dblEffective,
		final double dblMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dap,
		final int iFreq,
		final java.lang.String strDayCount,
		final boolean bApplyEOMAdj,
		final int iPSEC,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final java.lang.String strPayCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		return BackwardPeriodSingleReset (dblEffective, dblMaturity, dap, dap, dap, dap, dap, dap, dap, dap,
			iFreq, strDayCount, bApplyEOMAdj, strDayCount, bApplyEOMAdj, iPSEC, bCouponDCFOffOfFreq,
				strCalendar, strPayCurrency, forwardLabel, creditLabel);
	}

	/**
	 * Simplified Generation of the period list forward starting from the start.
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturity Maturity date
	 * @param dap Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strDayCount Day Count Convention
	 * @param bApplyEOMAdj End-of-month adjustment
	 * @param iPSEC Period Set Edge Customizer Setting
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 		determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strPayCurrency Pay Currency
	 * @param forwardLabel The Forward Label
	 * @param creditLabel The Credit Label
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CouponPeriod> ForwardPeriodSingleReset (
		final double dblEffective,
		final double dblMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dap,
		final int iFreq,
		final java.lang.String strDayCount,
		final boolean bApplyEOMAdj,
		final int iPSEC,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final java.lang.String strPayCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		return ForwardPeriodSingleReset (dblEffective, dblMaturity, dap, dap, dap, dap, dap, dap, dap, dap,
			iFreq, strDayCount, bApplyEOMAdj, strDayCount, bApplyEOMAdj, iPSEC, bCouponDCFOffOfFreq,
				strCalendar, strPayCurrency, forwardLabel, creditLabel);
	}

	/**
	 * Simplified Generation of the regular period lists.
	 * 
	 * @param dblEffective Effective date
	 * @param strMaturityTenor Maturity Tenor
	 * @param dap Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strDayCount Day Count Convention
	 * @param bApplyEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 	determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strPayCurrency Pay Currency
	 * @param forwardLabel The Forward Label
	 * @param creditLabel The Credit Label
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CouponPeriod> RegularPeriodSingleReset (
		final double dblEffective,
		final java.lang.String strMaturityTenor,
		final org.drip.analytics.daycount.DateAdjustParams dap,
		final int iFreq,
		final java.lang.String strDayCount,
		final boolean bApplyEOMAdj,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final java.lang.String strPayCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		return RegularPeriodSingleReset (dblEffective, strMaturityTenor, dap, dap, dap, dap, dap, dap, dap,
			dap, iFreq, strDayCount, bApplyEOMAdj, strDayCount, bApplyEOMAdj, bCouponDCFOffOfFreq,
				strCalendar, strPayCurrency, forwardLabel, creditLabel);
	}

	/**
	 * Simplified Generation of the regular period lists.
	 * 
	 * @param dblEffective Effective date
	 * @param strMaturityTenor Maturity Tenor
	 * @param dap Date Adjust Parameters
	 * @param iFreq Frequency
	 * @param strDayCount Day Count Convention
	 * @param bApplyEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param bCouponDCFOffOfFreq TRUE => Full coupon DCF = 1 / Frequency; FALSE => Full Coupon DCF
	 * 	determined from Coupon DCF and the coupon accrual period
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strPayCurrency Pay Currency
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param forwardLabel The Forward Label
	 * @param creditLabel The Credit Label
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CouponPeriod> RegularPeriodDailyReset (
		final double dblEffective,
		final java.lang.String strMaturityTenor,
		final org.drip.analytics.daycount.DateAdjustParams dap,
		final int iFreq,
		final java.lang.String strDayCount,
		final boolean bApplyEOMAdj,
		final boolean bCouponDCFOffOfFreq,
		final java.lang.String strCalendar,
		final java.lang.String strPayCurrency,
		final int iAccrualCompoundingRule,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		return RegularPeriodDailyReset (dblEffective, strMaturityTenor, dap, dap, dap, dap, dap, dap, dap,
			iFreq, strDayCount, bApplyEOMAdj, strDayCount, bApplyEOMAdj, bCouponDCFOffOfFreq, strCalendar,
				strPayCurrency, iAccrualCompoundingRule, forwardLabel, creditLabel);
	}

	/**
	 * Generate a single Cash Flow period between the effective and the maturity dates
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturity Maturity date
	 * @param strDayCount Day Count
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strCouponCurrency Pay Currency
	 * @param strPayCurrency Pay Currency
	 * @param forwardLabel The Forward Label
	 * @param creditLabel The Credit Label
	 * 
	 * @return List containing the single Cash Flow period
	 */

	public static final java.util.List<org.drip.analytics.period.CouponPeriod> SinglePeriodSingleReset (
		final double dblEffective,
		final double dblMaturity,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar,
		final java.lang.String strCouponCurrency,
		final java.lang.String strPayCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblMaturity) || dblEffective >= dblMaturity)
			return null;

		java.util.List<org.drip.analytics.period.CouponPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.period.CouponPeriod>();

		try {
			org.drip.analytics.period.ResetPeriodContainer rpc = null;

			if (null != forwardLabel && !(rpc = new org.drip.analytics.period.ResetPeriodContainer
				(org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC)).appendResetPeriod
					(new org.drip.analytics.period.ResetPeriod (dblEffective, dblMaturity, dblEffective)))
				return null;

			lsCashflowPeriod.add (0, new org.drip.analytics.period.CouponPeriod (dblEffective, dblMaturity,
				dblEffective, dblMaturity, dblMaturity, rpc, dblMaturity, java.lang.Double.NaN, 1,
					org.drip.analytics.daycount.Convention.YearFraction (dblEffective, dblMaturity,
						strDayCount, false, dblMaturity, null, strCalendar), strDayCount, strDayCount, false,
							false, strCalendar, strPayCurrency, forwardLabel, creditLabel));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return lsCashflowPeriod;
	}

	/**
	 * Generate the daily period list starting from the start.
	 * 
	 * @param dblEffective Effective date
	 * @param dblMaturity Maturity date
	 * @param dapReset Reset Date Adjust Parameters
	 * @param dapPay Pay Date Adjust Parameters
	 * @param strDC Accrual/Coupon day count
	 * @param strCalendar Optional Holiday Calendar for accrual
	 * @param strCouponCurrency Pay Currency
	 * @param strPayCurrency Pay Currency
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param forwardLabel The Forward Label
	 * @param creditLabel The Credit label
	 * 
	 * @return List of coupon Periods
	 */

	public static final java.util.List<org.drip.analytics.period.CouponPeriod> DailyPeriodDailyReset (
		final double dblEffective,
		final double dblMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final java.lang.String strDC,
		final java.lang.String strCalendar,
		final java.lang.String strCouponCurrency,
		final java.lang.String strPayCurrency,
		final int iAccrualCompoundingRule,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblMaturity) || dblEffective >= dblMaturity)
			return null;

		boolean bTerminationReached = false;
		double dblPeriodStartDate = dblEffective;

		java.util.List<org.drip.analytics.period.CouponPeriod> lsCashflowPeriod = new
			java.util.ArrayList<org.drip.analytics.period.CouponPeriod>();

		while (!bTerminationReached) {
			try {
				double dblAdjustedStartDate = org.drip.analytics.daycount.Convention.RollDate
					(dblPeriodStartDate, org.drip.analytics.daycount.Convention.DR_FOLL, strCalendar);

				double dblAdjustedEndDate = org.drip.analytics.daycount.Convention.RollDate
					(dblAdjustedStartDate + 1, org.drip.analytics.daycount.Convention.DR_FOLL, strCalendar);

				if (dblAdjustedStartDate >= dblMaturity) {
					dblAdjustedStartDate = dblPeriodStartDate;
					bTerminationReached = true;
				}

				if (dblAdjustedEndDate >= dblMaturity) {
					dblAdjustedEndDate = dblMaturity;
					bTerminationReached = true;
				}

				org.drip.analytics.period.ResetPeriodContainer rpc = null;

				if (null != forwardLabel && !(rpc = new org.drip.analytics.period.ResetPeriodContainer
					(iAccrualCompoundingRule)).appendResetPeriod (new org.drip.analytics.period.ResetPeriod
						(dblAdjustedStartDate, dblAdjustedEndDate, dblAdjustedStartDate)))
					return null;

				if (dblAdjustedStartDate < dblAdjustedEndDate)
					lsCashflowPeriod.add (new org.drip.analytics.period.CouponPeriod (dblAdjustedStartDate,
						dblAdjustedEndDate, dblAdjustedStartDate, dblAdjustedEndDate, DAPAdjust
							(dblAdjustedEndDate, dapPay), rpc, dblMaturity, java.lang.Double.NaN, 360,
								org.drip.analytics.daycount.Convention.YearFraction (dblAdjustedStartDate,
									dblAdjustedEndDate, strDC, false, dblMaturity, null, strCalendar), strDC,
										strDC, false, false, strCalendar, strPayCurrency, forwardLabel,
											creditLabel));

				dblPeriodStartDate = dblAdjustedEndDate;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return lsCashflowPeriod;
	}
}
