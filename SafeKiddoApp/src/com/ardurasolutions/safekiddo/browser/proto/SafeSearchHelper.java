package com.ardurasolutions.safekiddo.browser.proto;

public class SafeSearchHelper {
	
	/*
	 * GOOGLE
	 */
	private static final String[] googlePatterns = {
		"\\b(https|http)://[-a-zA-Z0-9+]*.google.\\S+",
		"\\b(https|http)://google.\\S+"
	};
	//private static final String googleCheckPatternsQueryOn = "\\b(https|http)://[-a-zA-Z0-9+]*.google.\\S*\\?\\S*safe=on\\S*";
	private static final String googleCheckPatternsHashOn = "\\b(https|http)://[-a-zA-Z0-9+]*.google.\\S*#\\S*safe=\\S*";
	/*
	 * BING
	 */
	private static final String[] bingPatterns = {
		"\\b(https|http)://[-a-zA-Z0-9+]*.bing.\\S+",
		"\\b(https|http)://bing.\\S+"
	};
	
	public static enum SafeSearchSiteType {
		GOOGLE,
		BING;
	}
	
	public static class SafeSearchCheckResult {
		private String oryginalUrl = null;
		private String newUrl = null;
		
		public SafeSearchCheckResult(String url) {
			oryginalUrl = url;
		}
		public boolean isChanaged() {
			if (newUrl == null && oryginalUrl == null) return false;
			if (oryginalUrl == null) return false;
			if (newUrl != null && oryginalUrl != null && !newUrl.equals(oryginalUrl))
				return true;
			else
				return false;
		}
		public String getOryginalUrl() {
			return oryginalUrl;
		}
		public void setOryginalUrl(String oryginalUrl) {
			this.oryginalUrl = oryginalUrl;
		}
		public String getNewUrl() {
			return newUrl;
		}
		public void setNewUrl(String newUrl) {
			this.newUrl = newUrl;
		}
	}
	
	public static SafeSearchCheckResult checkSafeSearchIsOk(String url) {
		SafeSearchCheckResult res = new SafeSearchCheckResult(url);
		
		SafeSearchSiteType type = getSafeSearchSite(url);
		if (type != null) {
			switch(type) {
				case GOOGLE:
					// sprawdzenie czy wogole safe search jest aktywne, jeżeli jest to nie ma co sprawdzać
					// nie ma # i safe=on
					if (!url.contains("#") && (url.contains("safe=on") || url.contains("safe=active"))) break;
					// jest # i safe=on lub safe=active
					if (url.contains("#")) {// && (url.contains("safe=on") || url.contains("safe=active"))) break;
						String[] splitHashs = url.split("#");
					//	if (splitHashs[1].contains(cs))
						if (splitHashs[1].contains("safe=on") || splitHashs[1].contains("safe=active")) break;
					}
					
					// sprawdzenie czy jest # w adresie
					if (url.contains("#")) {
						// jeżeli jest hash to tylko tym się zajmujemy, google i tak bierze dane tylko z niego
						// jeżeli jest safe=.... w adresie to zamieniamy na safe=active
						if (url.matches(googleCheckPatternsHashOn)) {
							// jest fraza safe= więc trzeba ją ustawić na safe=active
							res.setNewUrl(url.replaceAll("safe=off", "safe=active"));
						} else {
							// nie ma frazy safe= więc doklejamy
							res.setNewUrl(url + "&safe=active");
						}
					} else {
						// nie ma # to doklejamy i już
						res.setNewUrl(url + "#safe=active");
					}
				break;
				case BING:
					// safe mode w bing to adlt=strict
					if (!url.contains("adlt=strict")) {
						String newUrl = url;
						if (newUrl.contains("?"))
							newUrl += "&adlt=strict";
						else
							newUrl += "?&adlt=strict";
						res.setNewUrl(newUrl);
					}
				break;
			}
		}
		
		return res;
	}
	
	/**
	 * 
	 * @param url
	 * @return NULL or SafeSearchSiteType
	 */
	public static SafeSearchSiteType getSafeSearchSite(String url) {
		
		if (url != null && url.length() > 0) {
			for(String p : googlePatterns) {
				if (url.matches(p)) {
					return SafeSearchSiteType.GOOGLE;
				}
			}
			for(String p : bingPatterns) {
				if (url.matches(p)) {
					return SafeSearchSiteType.BING;
				}
			}
		}
		
		return null;
	}

}
