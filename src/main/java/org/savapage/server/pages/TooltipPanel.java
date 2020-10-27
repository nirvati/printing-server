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
package org.savapage.server.pages;

import java.util.UUID;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 *
 * @author Rijk Ravestein
 *
 */
public class TooltipPanel extends Panel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     * @param id
     *            The non-null id of this component.
     */
    public TooltipPanel(final String id) {
        super(id);
    }

    /**
     *
     * @param htmlContent
     *            The HTMl tooltip content.
     * @param escapeMarkup
     *            {@code true} if model strings should be escaped.
     */
    public void populate(final String htmlContent, final boolean escapeMarkup) {

        final String uuid = UUID.randomUUID().toString();
        final String title = this.getString("title");

        Label labelWrk;

        //
        labelWrk = new Label("tooltip-anchor", title);
        labelWrk.add(new AttributeModifier("href", String.format("#%s", uuid)));
        labelWrk.add(new AttributeModifier("title", title));
        add(labelWrk);

        //
        labelWrk = new Label("tooltip-content", htmlContent);
        labelWrk.add(new AttributeModifier("id", uuid));
        labelWrk.setEscapeModelStrings(escapeMarkup);
        add(labelWrk);
    }

}
