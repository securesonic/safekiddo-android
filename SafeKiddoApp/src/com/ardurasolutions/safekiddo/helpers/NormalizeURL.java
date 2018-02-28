package com.ardurasolutions.safekiddo.helpers;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


public class NormalizeURL {
	
	public static final String DEFAULT_SCHEME = "http://";
	
	/**
	 * 
	 * @param taintedURL
	 * @return
	 * @throws MalformedURLException
	 */
	public static String normalize(final String taintedURL) throws MalformedURLException {
		return normalize(taintedURL, null);
	}
	
	/**
	 * 
	 * @param taintedURL
	 * @param defaultScheme - if is null use http://
	 * @return
	 * @throws MalformedURLException
	 */
	public static String normalize(final String taintedURL, final String defaultScheme) throws MalformedURLException {
		if (taintedURL == null) return null;
		if (taintedURL.trim().length() == 0) return taintedURL;
        final URL url;
        try
        {
        	String scheme = new URI(taintedURL).getScheme();
        	if (scheme == null || (scheme != null && scheme.trim().length() == 0)) {
        		scheme = defaultScheme == null ? DEFAULT_SCHEME : defaultScheme;
        		url = new URI(scheme + taintedURL).normalize().toURL();
        	} else {
        		url = new URI(taintedURL).normalize().toURL();
        	}
        } catch (URISyntaxException e) {
            throw new MalformedURLException(e.getMessage());
        } catch (IllegalArgumentException e) {
        	throw new MalformedURLException(e.getMessage());
        }catch (Exception e) {
        	throw new MalformedURLException(e.getMessage());
        }


        final String path = url.getPath().replace("/$", "");
        final SortedMap<String, String> params = createParameterMap(url.getQuery());
        final int port = url.getPort();
        final String queryString;

        if (params != null) {
            // Some params are only relevant for user tracking, so remove the most commons ones.
            for (Iterator<String> i = params.keySet().iterator(); i.hasNext();) {
                final String key = i.next();
                if (key.startsWith("utm_") || key.contains("session")) {
                    i.remove();
                }
            }
            queryString = "?" + canonicalize(params);
        } else {
            queryString = "";
        }

        return url.getProtocol() + "://" + url.getHost().toLowerCase(Locale.getDefault())
            + (port != -1 && port != 80 ? ":" + port : "")
            + path + queryString + (url.getRef() != null && url.getRef().trim().length() > 0 ? "#" + url.getRef() : "");
    }

    /**
     * Takes a query string, separates the constituent name-value pairs, and
     * stores them in a SortedMap ordered by lexicographical order.
     * @return Null if there is no query string.
     */
    private static SortedMap<String, String> createParameterMap(final String queryString)
    {
        if (queryString == null || queryString.isEmpty())
        {
            return null;
        }

        final String[] pairs = queryString.split("&");
        final Map<String, String> params = new HashMap<String, String>(pairs.length);

        for (final String pair : pairs)
        {
            if (pair.length() < 1)
            {
                continue;
            }

            String[] tokens = pair.split("=", 2);
            for (int j = 0; j < tokens.length; j++)
            {
                try
                {
                    tokens[j] = URLDecoder.decode(tokens[j], "UTF-8");
                }
                catch (UnsupportedEncodingException ex)
                {
                    ex.printStackTrace();
                }
            }
            switch (tokens.length)
            {
                case 1:
                {
                    if (pair.charAt(0) == '=')
                    {
                        params.put("", tokens[0]);
                    }
                    else
                    {
                        params.put(tokens[0], "");
                    }
                    break;
                }
                case 2:
                {
                    params.put(tokens[0], tokens[1]);
                    break;
                }
            }
        }

        return new TreeMap<String, String>(params);
    }

    /**
     * Canonicalize the query string.
     *
     * @param sortedParamMap Parameter name-value pairs in lexicographical order.
     * @return Canonical form of query string.
     */
    private static String canonicalize(final SortedMap<String, String> sortedParamMap)
    {
        if (sortedParamMap == null || sortedParamMap.isEmpty())
        {
            return "";
        }

        final StringBuffer sb = new StringBuffer(350);
        final Iterator<Map.Entry<String, String>> iter = sortedParamMap.entrySet().iterator();

        while (iter.hasNext())
        {
            final Map.Entry<String, String> pair = iter.next();
            sb.append(pair.getKey());
            sb.append('=');
            sb.append(pair.getValue());
            if (iter.hasNext())
            {
                sb.append('&');
            }
        }

        return sb.toString();
    }
}
