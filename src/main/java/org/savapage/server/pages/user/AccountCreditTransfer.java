/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2020 Datraverse B.V.
 * Author: Rijk Ravestein.
 *
 * SPDX-FileCopyrightText: © 2020 Datraverse B.V. <info@datraverse.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * For more information, please contact Datraverse B.V. at this
 * address: info@datraverse.com
 */
package org.savapage.server.pages.user;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.savapage.core.config.ConfigManager;
import org.savapage.core.config.IConfigProp.Key;
import org.savapage.server.pages.MarkupHelper;
import org.savapage.server.session.SpSession;

/**
 *
 * @author Rijk Ravestein
 *
 */
public class AccountCreditTransfer extends AbstractUserPage {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public AccountCreditTransfer(final PageParameters parameters) {

        super(parameters);

        final String currencySymbol = SpSession.getAppCurrencySymbol();
        final MarkupHelper helper = new MarkupHelper(this);

        helper.addLabel("currency-symbol", currencySymbol);
        helper.addLabel("decimal-separator", SpSession.getDecimalSeparator());

        helper.encloseLabel("prompt-comment", localized("prompt-comment"),
                ConfigManager.instance().isConfigValue(
                        Key.FINANCIAL_USER_TRANSFER_ENABLE_COMMENTS));

    }
}
