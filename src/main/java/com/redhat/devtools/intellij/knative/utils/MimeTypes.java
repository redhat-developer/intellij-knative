/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.knative.utils;

import java.util.HashMap;
import java.util.Map;

public class MimeTypes {

    public final static String TEXT_PLAIN = "text/plain";
    public final static String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private final static Map<String, String> mime;
    static {
        mime = new HashMap<>();
        mime.put(".aac", "udio/aac");
        mime.put(".abw", "application/x-abiword");
        mime.put(".arc", "application/x-freearc");
        mime.put(".avif", "image/avif");
        mime.put(".avi", "video/x-msvideo");
        mime.put(".azw", "application/vnd.amazon.ebook");
        mime.put(".bin", "application/octet-stream");
        mime.put(".bmp", "image/bmp");
        mime.put(".bz", "application/x-bzip");
        mime.put(".bz2", "application/x-bzip2");
        mime.put(".cda", "application/x-cdf");
        mime.put(".csh", "application/x-csh");
        mime.put(".css", "text/css");
        mime.put(".csv", "text/csv");
        mime.put(".doc", "application/msword");
        mime.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mime.put(".eot", "application/vnd.ms-fontobject");
        mime.put(".epub", "application/epub+zip");
        mime.put(".gz", "application/gzip");
        mime.put(".gif", "image/gif");
        mime.put(".htm", "text/html");
        mime.put(".html", "text/html");
        mime.put(".ico", "image/vnd.microsoft.icon");
        mime.put(".ics", "text/calendar");
        mime.put(".jpg", "image/jpeg");
        mime.put(".jpeg", "image/jpeg");
        mime.put(".js", "text/javascript");
        mime.put(".json", "application/json");
        mime.put(".jsonld", "application/ld+json");
        mime.put(".midi", "audio/midi");
        mime.put(".mp3", "audio/mpeg");
        mime.put(".mp4", "video/mp4");
        mime.put(".mpeg", "video/mpeg");
        mime.put(".odp", "application/vnd.oasis.opendocument.presentation");
        mime.put(".ods", "application/vnd.oasis.opendocument.spreadsheet");
        mime.put(".odt", "application/vnd.oasis.opendocument.text");
        mime.put(".oga", "audio/ogg");
        mime.put(".ogv", "video/ogg");
        mime.put(".ogx", "application/ogg");
        mime.put(".opus", "audio/opus");
        mime.put(".png", "image/png");
        mime.put(".pdf", "application/pdf");
        mime.put(".php", "application/x-httpd-php");
        mime.put(".ppt", "application/vnd.ms-powerpoint");
        mime.put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        mime.put(".rar", "application/vnd.rar");
        mime.put(".rtf", "application/rtf");
        mime.put(".sh", "application/x-sh");
        mime.put(".svg", "image/svg+xml");
        mime.put(".tar", "application/x-tar");
        mime.put(".tiff", "image/tiff");
        mime.put(".txt", "text/plain");
        mime.put(".wav", "audio/wav");
        mime.put(".weba", "audio/webm");
        mime.put(".webm", "video/webm");
        mime.put(".webp", "image/webp");
        mime.put(".xhtml", "application/xhtml+xml");
        mime.put(".xls", "application/vnd.ms-excel");
        mime.put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        mime.put(".xml", "application/xml");
        mime.put(".zip", "application/zip");
        mime.put(".3gp", "video/3gpp");
        mime.put(".3g2", "video/3gpp2");
        mime.put(".7z", "application/x-7z-compressed");
    }

    public static Map<String, String> getMime() {
        return mime;
    }
}
