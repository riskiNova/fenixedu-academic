/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Academic.
 *
 * FenixEdu Academic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academic.domain.accounting.postingRules;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.fenixedu.academic.domain.accounting.Account;
import org.fenixedu.academic.domain.accounting.AccountingTransaction;
import org.fenixedu.academic.domain.accounting.EntryType;
import org.fenixedu.academic.domain.accounting.Event;
import org.fenixedu.academic.domain.accounting.EventType;
import org.fenixedu.academic.domain.accounting.Exemption;
import org.fenixedu.academic.domain.accounting.ServiceAgreementTemplate;
import org.fenixedu.academic.domain.accounting.events.AcademicEventExemption;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.domain.exceptions.DomainExceptionWithLabelFormatter;
import org.fenixedu.academic.dto.accounting.AccountingTransactionDetailDTO;
import org.fenixedu.academic.dto.accounting.EntryDTO;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.academic.util.Money;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.joda.time.DateTime;

public abstract class BaseAmountPlusAmountPerUnitPR extends BaseAmountPlusAmountPerUnitPR_Base {

    protected BaseAmountPlusAmountPerUnitPR() {
        super();
    }

    protected void init(EntryType entryType, EventType eventType, DateTime startDate, DateTime endDate,
            ServiceAgreementTemplate serviceAgreementTemplate, Money baseAmount, Money amountPerUnit, Money maximumAmount) {
        super.init(entryType, eventType, startDate, endDate, serviceAgreementTemplate);
        checkParameters(baseAmount, amountPerUnit, maximumAmount);
        super.setBaseAmount(baseAmount);
        super.setAmountPerUnit(amountPerUnit);
        super.setMaximumAmount(maximumAmount);
    }

    private void checkParameters(Money baseAmount, Money amountPerUnit, Money maximumAmount) {
        if (baseAmount == null) {
            throw new DomainException(
                    "error.accounting.postingRules.BaseAmountPlusAmountPerUnitGreaterThanOnePR.baseAmount.cannot.be.null");
        }
        if (amountPerUnit == null) {
            throw new DomainException(
                    "error.accounting.postingRules.BaseAmountPlusAmountPerUnitGreaterThanOnePR.amountPerUnit.cannot.be.null");
        }

        if (maximumAmount == null) {
            throw new DomainException(
                    "error.accounting.postingRules.BaseAmountPlusAmountPerUnitGreaterThanOnePR.maximumAmount.cannot.be.null");
        }
    }

    @Override
    public void setBaseAmount(Money baseAmount) {
        throw new DomainException(
                "error.accounting.postingRules.BaseAmountPlusAmountPerUnitGreaterThanOnePR.cannot.modify.baseAmount");
    }

    @Override
    public void setAmountPerUnit(Money amountPerUnit) {
        throw new DomainException(
                "error.accounting.postingRules.BaseAmountPlusAmountPerUnitGreaterThanOnePR.cannot.modify.amountPerUnit");
    }

    @Override
    public void setMaximumAmount(Money maximumAmount) {
        throw new DomainException(
                "error.accounting.postingRules.BaseAmountPlusAmountPerUnitGreaterThanOnePR.cannot.modify.maximumAmount");
    }

    public Money getAmountForUnits(Event event) {
        return getAmountForUnits(getNumberOfUnits(event));
    }

    public Money getAmountForUnits(Integer numberOfUnits) {
        return getAmountPerUnit().multiply(new BigDecimal(numberOfUnits));
    }

    @Override
    protected Money doCalculationForAmountToPay(Event event, DateTime when, boolean applyDiscount) {
        return getBaseAmount().add(getAmountForUnits(event));
    }

    @Override
    protected Money subtractFromExemptions(Event event, DateTime when, boolean applyDiscount, Money amountToPay) {
        if (!event.getExemptionsSet().isEmpty()) {
            Collection<Exemption> exemptions = event.getExemptionsSet();

            for (Exemption exemption : exemptions) {
                AcademicEventExemption academicEventExemption = (AcademicEventExemption) exemption;

                amountToPay = amountToPay.subtract(academicEventExemption.getValue());
            }
        }

        if (amountToPay.isNegative()) {
            return Money.ZERO;
        }

        return amountToPay;
    }

    @Override
    public List<EntryDTO> calculateEntries(Event event, DateTime when) {
        return Collections.singletonList(new EntryDTO(getEntryType(), event, calculateTotalAmountToPay(event, when), event
                .getPayedAmount(), event.calculateAmountToPay(when), event.getDescriptionForEntryType(getEntryType()), event
                .calculateAmountToPay(when)));
    }

    @Override
    protected Set<AccountingTransaction> internalProcess(User user, Collection<EntryDTO> entryDTOs, Event event,
            Account fromAccount, Account toAccount, AccountingTransactionDetailDTO transactionDetail) {
        if (entryDTOs.size() != 1) {
            throw new DomainException(
                    "error.accounting.postingRules.BaseAmountPlusAmountPerUnitGreaterThanOnePR.invalid.number.of.entryDTOs");
        }

        final EntryDTO entryDTO = entryDTOs.iterator().next();
        checkIfCanAddAmount(entryDTO.getAmountToPay(), event, transactionDetail.getWhenRegistered());

        return Collections.singleton(makeAccountingTransaction(user, event, fromAccount, toAccount, entryDTO.getEntryType(),
                entryDTO.getAmountToPay(), transactionDetail));
    }

    private void checkIfCanAddAmount(Money amountToPay, Event event, DateTime when) {
        if (amountToPay.compareTo(event.calculateAmountToPay(when)) < 0) {
            throw new DomainExceptionWithLabelFormatter(
                    "error.accounting.postingRules.BaseAmountPlusAmountPerUnitGreaterThanOnePR.amount.being.payed.must.match.amount.to.pay",
                    event.getDescriptionForEntryType(getEntryType()));
        }

    }

    protected abstract Integer getNumberOfUnits(Event event);

    public String getMaximumAmountDescription() {
        if (Money.ZERO.equals(this.getMaximumAmount())) {
            return BundleUtil.getString(Bundle.APPLICATION, "label.base.amount.plus.units.with.no.maximum.value");
        }

        return this.getMaximumAmount().getAmountAsString();
    }

}
