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
package org.savapage.server.webapp;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.StandardCopyOption;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;
import org.savapage.core.community.CommunityDictEnum;
import org.savapage.core.config.IConfigProp;
import org.savapage.core.config.WebAppTypeEnum;
import org.savapage.core.dao.DaoContext;
import org.savapage.core.services.ServiceContext;
import org.savapage.server.pages.LibreJsLicenseEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Rijk Ravestein
 *
 */
public final class WebAppAdmin extends AbstractWebAppPage {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * The logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(WebAppAdmin.class);

    /** */
    private static final long MAX_UPLOAD_KB = 10L;

    /** */
    private static final String[] CSS_REQ_FILENAMES =
            new String[] { "jquery.savapage-common-icons.css" };

    /**
     *
     * @param parameters
     *            The {@link PageParameters}.
     */
    public WebAppAdmin(final PageParameters parameters) {

        super(parameters);

        checkInternetAccess(IConfigProp.Key.WEBAPP_INTERNET_ADMIN_ENABLE);

        if (isWebAppCountExceeded(parameters)) {
            setWebAppCountExceededResponse();
            return;
        }

        final String appTitle = getWebAppTitle(
                getLocalizer().getString("webapp-title-suffix", this));

        addZeroPagePanel(WebAppTypeEnum.ADMIN);

        add(new Label("app-title", appTitle));

        addFileDownloadApiPanel();

        //
        this.addLibreJsLicensePanel("librjs-license-page");
    }

    @Override
    protected boolean isJqueryCoreRenderedByWicket() {
        return true;
    }

    @Override
    protected WebAppTypeEnum getWebAppType() {
        return WebAppTypeEnum.ADMIN;
    }

    @Override
    protected void appendWebAppTypeJsFiles(
            final List<Pair<String, LibreJsLicenseEnum>> list,
            final String nocache) {

        list.add(new ImmutablePair<>(String.format("%s%s",
                "jquery.savapage-admin-panels.js", nocache),
                SAVAPAGE_JS_LICENSE));

        list.add(new ImmutablePair<>(String.format("%s%s",
                "jquery.savapage-admin-pages.js", nocache),
                SAVAPAGE_JS_LICENSE));

        list.add(new ImmutablePair<>(
                String.format("%s%s", getSpecializedJsFileName(), nocache),
                SAVAPAGE_JS_LICENSE));
    }

    @Override
    protected String[] getSpecializedCssReqFileNames() {
        return CSS_REQ_FILENAMES;
    }

    @Override
    protected String getSpecializedCssFileName() {
        return "jquery.savapage-admin.css";
    }

    @Override
    protected String getSpecializedJsFileName() {
        return "jquery.savapage-admin.js";
    }

    @Override
    protected Set<JavaScriptLibrary> getJavaScriptToRender() {
        final EnumSet<JavaScriptLibrary> libs =
                EnumSet.allOf(JavaScriptLibrary.class);
        return libs;
    }
}
