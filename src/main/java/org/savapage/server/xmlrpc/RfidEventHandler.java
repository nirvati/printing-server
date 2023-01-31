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
package org.savapage.server.xmlrpc;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.savapage.core.PerformanceLogger;
import org.savapage.core.cometd.AdminPublisher;
import org.savapage.core.cometd.PubLevelEnum;
import org.savapage.core.cometd.PubTopicEnum;
import org.savapage.core.concurrent.ReadWriteLockEnum;
import org.savapage.core.config.ConfigManager;
import org.savapage.core.config.IConfigProp;
import org.savapage.core.config.IConfigProp.Key;
import org.savapage.core.dao.DaoContext;
import org.savapage.core.dao.DeviceDao;
import org.savapage.core.dao.enums.DeviceTypeEnum;
import org.savapage.core.dao.enums.ProxyPrintAuthModeEnum;
import org.savapage.core.jpa.Device;
import org.savapage.core.jpa.User;
import org.savapage.core.print.proxy.ProxyPrintAuthManager;
import org.savapage.core.print.proxy.ProxyPrintException;
import org.savapage.core.rfid.RfidEvent;
import org.savapage.core.rfid.RfidReaderManager;
import org.savapage.core.services.DeviceService;
import org.savapage.core.services.InboxService;
import org.savapage.core.services.OutboxService;
import org.savapage.core.services.ProxyPrintService;
import org.savapage.core.services.ServiceContext;
import org.savapage.core.services.ServiceEntryPoint;
import org.savapage.core.services.UserService;
import org.savapage.core.services.helpers.InboxSelectScopeEnum;
import org.savapage.core.services.helpers.ProxyPrintOutboxResult;
import org.savapage.core.util.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Rijk Ravestein
 *
 */
public class RfidEventHandler implements ServiceEntryPoint {

    /**
     * The key for the (required) return code.
     */
    private static final String KEY_RC = "rc";

    /**
     * The key for the (optional) message.
     */
    private static final String KEY_MESSAGE = "message";

    /**
     * The logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(RfidEventHandler.class);

    private static final AdminPublisher ADMIN_PUBLISHER =
            AdminPublisher.instance();

    private static final DeviceService DEVICE_SERVICE =
            ServiceContext.getServiceFactory().getDeviceService();

    private static final InboxService INBOX_SERVICE =
            ServiceContext.getServiceFactory().getInboxService();

    private static final OutboxService OUTBOX_SERVICE =
            ServiceContext.getServiceFactory().getOutboxService();

    private static final ProxyPrintAuthManager PROXYPRINT_AUTHMANAGER =
            ProxyPrintAuthManager.instance();

    private static final UserService USER_SERVICE =
            ServiceContext.getServiceFactory().getUserService();

    /**
    *
    */
    private static final ProxyPrintService PROXY_PRINT_SERVICE =
            ServiceContext.getServiceFactory().getProxyPrintService();

    /**
     *
     */
    private static final Integer RC_ACCEPT = Integer.valueOf(0);
    private static final Integer RC_DENY = Integer.valueOf(1);
    private static final Integer RC_EXCEPTION = Integer.valueOf(99);

    /**
     * Handles a card swipe from a card reader {@link Device}.
     *
     * @param apiId
     *            The API id.
     * @param apiKey
     *            The API key.
     * @param cardNumber
     *            The RFID card number.
     * @return The map with the {@link #KEY_RC} and {@link #KEY_MESSAGE} value.
     */
    public final Map<String, Object> cardSwipe(final String apiId,
            final String apiKey, final String cardNumber) {

        final Date perfStartTime = PerformanceLogger.startTime();

        final Map<String, Object> map = new HashMap<String, Object>();

        Integer rc = RC_DENY;

        ServiceContext.open();

        final DaoContext daoContext = ServiceContext.getDaoContext();
        final DeviceDao deviceDao = daoContext.getDeviceDao();

        ReadWriteLockEnum.DATABASE_READONLY.setReadLock(true);

        try {
            /*
             * NOTE: when apiId/Key is invalid an exception is thrown.
             */
            //MemberCard.instance().validateContent(apiId, apiKey);
            throw new Exception("This API is not implemented yet!");

            /*final String clientIpAddress = SpXmlRpcServlet.getClientIpAddress();

            /*
             * Find the card reader.
             */
            /*final Device cardReader = deviceDao.findByHostDeviceType(
                    clientIpAddress, DeviceTypeEnum.CARD_READER);

            if (cardReader == null) {

                onCardReaderUnknown(map, clientIpAddress, cardNumber);

            } else if (cardReader.getDisabled()) {

                onCardReaderDisabled(map, clientIpAddress, cardNumber);

            } else if (cardReader.getCardReaderTerminal() == null) {
                rc = onCardSwipePrint(map, clientIpAddress, cardNumber,
                        cardReader);
            } else {
                rc = onCardSwipeAuth(map, clientIpAddress, cardNumber);
            }*/

        } catch (ProxyPrintException ex) {

            rc = RC_DENY;

            ADMIN_PUBLISHER.publish(PubTopicEnum.PROXY_PRINT, PubLevelEnum.WARN,
                    ex.getMessage());

            map.put(KEY_MESSAGE, ex.getMessage());

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(ex.getMessage());
            }

        } catch (Exception ex) {

            rc = RC_EXCEPTION;

            map.put(KEY_MESSAGE, ex.getMessage());

            if (!ConfigManager.isShutdownInProgress()) {

                ADMIN_PUBLISHER.publish(PubTopicEnum.NFC, PubLevelEnum.ERROR,
                        ex.getMessage());
                LOGGER.error(ex.getMessage(), ex);
            }

        } finally {

            try {

                daoContext.rollback();

            } catch (Exception ex) {

                if (!ConfigManager.isShutdownInProgress()) {
                    LOGGER.error(ex.getMessage(), ex);
                }

            } finally {
                ServiceContext.close();
            }

            ReadWriteLockEnum.DATABASE_READONLY.setReadLock(false);

        }

        map.put(KEY_RC, rc);

        PerformanceLogger.log(this.getClass(), "cardSwipe", perfStartTime,
                cardNumber);

        return map;
    }

    /**
     * Handles unknown card reader.
     *
     * @param map
     *            The map with the {@link #KEY_RC} and {@link #KEY_MESSAGE}
     *            value.
     * @param clientIpAddress
     *            The client IP address.
     * @param cardNumber
     *            The card number.
     */
    private void onCardReaderUnknown(final Map<String, Object> map,
            final String clientIpAddress, final String cardNumber) {

        final String key = "rfid-card-swipe-reader-unknown";

        ADMIN_PUBLISHER.publish(PubTopicEnum.NFC, PubLevelEnum.WARN,
                Messages.getSystemMessage(this.getClass(), key, cardNumber,
                        clientIpAddress));

        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(Messages.getLogFileMessage(this.getClass(), key,
                    cardNumber, clientIpAddress));
        }

        map.put(KEY_MESSAGE, "Unknown Card Reader [" + clientIpAddress + "]");
    }

    /**
     * Handles disabled card reader.
     *
     * @param map
     *            The map with the {@link #KEY_RC} and {@link #KEY_MESSAGE}
     *            value.
     * @param clientIpAddress
     *            The client IP address.
     * @param cardNumber
     *            The card number.
     */
    private void onCardReaderDisabled(final Map<String, Object> map,
            final String clientIpAddress, final String cardNumber) {

        final String key = "rfid-card-swipe-reader-disabled";

        ADMIN_PUBLISHER.publish(PubTopicEnum.NFC, PubLevelEnum.WARN,
                Messages.getSystemMessage(this.getClass(), key, cardNumber,
                        clientIpAddress));

        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(Messages.getLogFileMessage(this.getClass(), key,
                    cardNumber, clientIpAddress));
        }

        map.put(KEY_MESSAGE, "Disabled Card Reader [" + clientIpAddress + "]");
    }

    /**
     * Checks if a direct authenticated print is pending of a single printer or
     * printer group related to a card reader.
     *
     * @param cardReader
     *            The card reader.
     * @return {@code true} if an authenticated print is pending.
     */
    private static boolean isDirectAuthReqPending(final Device cardReader) {

        final Set<String> printerNames =
                DEVICE_SERVICE.collectPrinterNames(cardReader);

        boolean isPending = false;

        final Iterator<String> iter = printerNames.iterator();

        while (iter.hasNext()) {

            final String printerName = iter.next();

            isPending =
                    PROXYPRINT_AUTHMANAGER.isAuthPendingForPrinter(printerName);

            if (isPending) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Authorisation request for printer ["
                            + printerName + "] pending ");
                }
                break;
            }
        }

        return isPending;
    }

    /**
     * Handles a card swipe on a card reader for terminal authentication.
     *
     * @param map
     *            The map with the {@link #KEY_RC} and {@link #KEY_MESSAGE}
     *            value.
     * @param clientIpAddress
     *            The client IP address.
     * @param cardNumber
     *            The card number.
     * @throws InterruptedException
     */
    private Integer onCardSwipeAuth(final Map<String, Object> map,
            final String clientIpAddress, final String cardNumber)
            throws InterruptedException {

        String key = "rfid-card-swipe";

        ADMIN_PUBLISHER.publish(PubTopicEnum.NFC, PubLevelEnum.INFO,
                Messages.getSystemMessage(this.getClass(), key, cardNumber,
                        clientIpAddress));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Messages.getLogFileMessage(this.getClass(), key,
                    cardNumber, clientIpAddress));
        }

        final User user = USER_SERVICE.findUserByCardNumber(cardNumber);

        RfidReaderManager.reportEvent(clientIpAddress,
                new RfidEvent(RfidEvent.EventEnum.CARD_SWIPE, cardNumber));

        final String userId;

        if (user == null) {
            userId = "?";
        } else {
            userId = user.getUserId();
        }

        final StringBuilder msg = new StringBuilder(96);
        msg.append("User [");
        msg.append(userId).append("] authenticated.");

        map.put(KEY_MESSAGE, msg.toString());

        return RC_ACCEPT;
    }

    /**
     * Handles a card swipe on a card reader for printing.
     *
     * @param map
     *            The map with the {@link #KEY_RC} and {@link #KEY_MESSAGE}
     *            value.
     * @param clientIpAddress
     *            The client IP address.
     * @param cardNumber
     *            The card number.
     * @param cardReader
     *            The card reader {@link Device}.
     * @return The return code for the XML-RPC request.
     * @throws ProxyPrintException
     *             When logical proxy print errors.
     * @throws InterruptedException
     *             When thread is interrupted.
     */
    private Integer onCardSwipePrint(final Map<String, Object> map,
            final String clientIpAddress, final String cardNumber,
            final Device cardReader)
            throws ProxyPrintException, InterruptedException {

        /*
         * Check if Card Reader supports Fast|Hold Print.
         */
        final ProxyPrintAuthModeEnum authMode =
                DEVICE_SERVICE.getProxyPrintAuthMode(cardReader.getId());

        final boolean isFastProxyPrintSupported =
                authMode != null && authMode.isFast();

        final boolean isHoldReleasePrintSupported =
                authMode != null && authMode.isHoldRelease();

        final boolean doHoldFastProxyPrint;

        String key = "rfid-card-swipe";

        if (isFastProxyPrintSupported || isHoldReleasePrintSupported) {

            doHoldFastProxyPrint = !isDirectAuthReqPending(cardReader);

            if (doHoldFastProxyPrint) {
                key = "rfid-card-swipe-print-release";
            }

        } else {
            doHoldFastProxyPrint = false;
        }

        ADMIN_PUBLISHER.publish(PubTopicEnum.NFC, PubLevelEnum.INFO,
                Messages.getSystemMessage(this.getClass(), key, cardNumber,
                        clientIpAddress));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Messages.getLogFileMessage(this.getClass(), key,
                    cardNumber, clientIpAddress));
        }

        /*
         * Find the user of the card.
         */
        final User user = USER_SERVICE.findUserByCardNumber(cardNumber);

        if (user == null) {
            key = "rfid-card-swipe-no-user";
            throw new ProxyPrintException(
                    Messages.getSystemMessage(this.getClass(), key, cardNumber),
                    Messages.getLogFileMessage(this.getClass(), key,
                            cardNumber));
        }

        /*
         *
         */
        if (doHoldFastProxyPrint) {

            /*
             * Hold|Fast Proxy Print.
             */
            doHoldFastProxyPrint(map, clientIpAddress, cardNumber, cardReader,
                    user, isHoldReleasePrintSupported,
                    isFastProxyPrintSupported);

        } else {

            if (!PROXYPRINT_AUTHMANAGER.isAuthPendingForUser(user.getId())) {

                key = "rfid-card-swipe-no-request";

                throw new ProxyPrintException(
                        Messages.getSystemMessage(this.getClass(), key,
                                user.getUserId(), cardNumber),
                        Messages.getLogFileMessage(this.getClass(), key,
                                user.getUserId(), cardNumber));
            }

            /*
             * WebApp Proxy Print.
             */
            RfidReaderManager.reportEvent(clientIpAddress,
                    new RfidEvent(RfidEvent.EventEnum.CARD_SWIPE, cardNumber));

            final StringBuilder builder = new StringBuilder(96);
            builder.append("User [").append(user.getUserId())
                    .append("] authenticated pending print request.");

            map.put(KEY_MESSAGE, builder.toString());

        }

        return RC_ACCEPT;
    }

    /**
     * Performs a Hold and/or Fast Proxy Print.
     *
     * @param map
     *            The map with the {@link #KEY_RC} and {@link #KEY_MESSAGE}
     *            value.
     * @param clientIpAddress
     *            The client IP address.
     * @param cardNumber
     *            The card number.
     * @param cardReader
     *            The card reader {@link Device}.
     * @param user
     *            The user of the card.
     * @param isHoldPrintSupported
     *            {@code true} if Hold Print is supported.
     * @param isFastPrintSupported
     *            {@code true} if Fast Print is supported.
     * @return The number of pages printed.
     * @throws ProxyPrintException
     *             When logical proxy print errors.
     */
    private int doHoldFastProxyPrint(final Map<String, Object> map,
            final String clientIpAddress, final String cardNumber,
            final Device cardReader, final User user,
            final boolean isHoldPrintSupported,
            final boolean isFastPrintSupported) throws ProxyPrintException {

        final ConfigManager cm = ConfigManager.instance();

        /*
         * Parameters
         */
        final int nHoldJobs =
                OUTBOX_SERVICE.getOutboxJobCount(user.getUserId());

        final boolean isProxyPrintSafePagesClear;

        if (cm.isConfigValue(
                IConfigProp.Key.WEBAPP_USER_PROXY_PRINT_CLEAR_INBOX_ENABLE)) {

            final InboxSelectScopeEnum proxyPrintClearScope =
                    cm.getConfigEnum(InboxSelectScopeEnum.class,
                            Key.WEBAPP_USER_PROXY_PRINT_CLEAR_INBOX_SCOPE);

            isProxyPrintSafePagesClear = EnumSet
                    .of(InboxSelectScopeEnum.ALL, InboxSelectScopeEnum.JOBS,
                            InboxSelectScopeEnum.PAGES)
                    .contains(proxyPrintClearScope);
        } else {
            isProxyPrintSafePagesClear = false;
        }

        /*
         * Counters
         */
        int nPagesReleased = 0;

        //
        final DaoContext daoContext = ServiceContext.getDaoContext();
        daoContext.beginTransaction();

        /*
         * Step 1: HOLD Print Release.
         */
        final int nHoldJobsReleased;

        if (isHoldPrintSupported) {

            final ProxyPrintOutboxResult result = PROXY_PRINT_SERVICE
                    .proxyPrintOutbox(cardReader, cardNumber);

            nHoldJobsReleased = result.getJobs();
            nPagesReleased += result.getPages();
        } else {
            nHoldJobsReleased = 0;
        }

        /*
         * Step 2: FAST Print Release.
         */
        final boolean doFastPrint;

        if (isFastPrintSupported) {
            /*
             * If Hold jobs exist for any printer, a Fast Print is only done
             * when Proxy Printing is configured to “Always remove SafePages
             * after printing”. This way, because all SafePages are cleared
             * after creating a Hold Job, we know for sure that present
             * SafePages are not part of any Hold Print Job.
             */
            if (nHoldJobs == 0) {
                doFastPrint = true;
            } else {
                doFastPrint = isProxyPrintSafePagesClear;
            }
        } else {
            doFastPrint = false;
        }

        if (doFastPrint) {
            /*
             * We need a new transaction, when Hold Print was executed.
             */
            if (!daoContext.isTransactionActive()) {
                daoContext.beginTransaction();
            }

            /*
             * NOTE: this will also clear ALL SafePages.
             */
            nPagesReleased += PROXY_PRINT_SERVICE
                    .proxyPrintInboxFast(cardReader, cardNumber);

        } else if (nHoldJobsReleased > 0 && !isProxyPrintSafePagesClear) {
            /*
             * When Proxy Printing is not configured to “Always remove SafePages
             * after printing”, all SafePages will be ad-hoc cleared after a
             * Hold Print release. This will prevent that, after all Hold jobs
             * are released at Hold-only printers, and no Hold jobs exist for
             * any printer, remaining SafePages will be Fast Printed anyway.
             */
            INBOX_SERVICE.deleteAllPages(user.getUserId());
        }

        daoContext.commit();

        final StringBuilder builder = new StringBuilder(96);

        builder.append("User [").append(user.getUserId()).append("] released [")
                .append(nPagesReleased).append("] proxy printed page");

        if (nPagesReleased > 1) {
            builder.append("s");
        }

        if (cardReader.getPrinter() != null) {
            builder.append(" to printer [")
                    .append(cardReader.getPrinter().getPrinterName())
                    .append("].");
        } else if (cardReader.getPrinterGroup() != null) {
            builder.append(" to printergroup [")
                    .append(cardReader.getPrinterGroup().getDisplayName())
                    .append("].");
        }

        if (nPagesReleased == 0) {
            final String key = "rfid-card-swipe-no-pages";

            throw new ProxyPrintException(
                    Messages.getSystemMessage(this.getClass(), key,
                            user.getUserId()),
                    Messages.getLogFileMessage(this.getClass(), key,
                            user.getUserId()));
        }

        map.put(KEY_MESSAGE, builder.toString());

        return nPagesReleased;
    }
}
