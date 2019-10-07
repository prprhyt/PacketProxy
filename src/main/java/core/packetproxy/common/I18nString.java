package packetproxy.common;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18nString {
	
	private static ResourceBundle bundle = ResourceBundle.getBundle("strings");
	private static Locale thisLocale = Locale.getDefault();
	
	private static String normalize(String message) {
		return message.replace(' ', '_').replace("\n", "\\n");
	}
	
	public static void setLocale(Locale locale) {
		thisLocale = locale;
	}
	
	public static String get(String message) {
		if (thisLocale == Locale.JAPAN) {
			try {
				return bundle.getString(normalize(message));
			} catch (java.util.MissingResourceException e) {
				return message;
			}
		}
		return message;
	}

}
