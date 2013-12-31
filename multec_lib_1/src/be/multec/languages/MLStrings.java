package be.multec.languages;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import processing.core.PApplet;
import processing.data.XML;

/**
 * Manager of multi-language strings.
 * 
 * The string-data can be loaded from an XML-file.
 * 
 * @see MLStrings#MLStrings(PApplet, Language[], String)
 * 
 * @author Wouter Van den Broeck
 */
public class MLStrings {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	/* The currently selected language. */
	private Language currentLanguage;
	
	/* A mapping from string identifiers and the strings for one language. */
	private ConcurrentHashMap<String, String> currentStringMap;
	
	/* A mapping from language objects to maps that contain the strings for that language. */
	private final ConcurrentHashMap<Language, ConcurrentHashMap<String, String>> languageMap = new ConcurrentHashMap<Language, ConcurrentHashMap<String, String>>();
	
	/* The handlers that want to be notified when the language is changed. */
	private final CopyOnWriteArraySet<LanguageChangeHandler> handlers = new CopyOnWriteArraySet<LanguageChangeHandler>();
	
	/*
	 * The collection of MLString objects that need to be updated when the language is changed.
	 * These MLString objects are referenced with weak references. The MLString objects which are no
	 * longer in use and are recycled by the GC are gracefully removed from this collection.
	 */
	private final CopyOnWriteArraySet<WeakReference<MLString>> strings = new CopyOnWriteArraySet<WeakReference<MLString>>();
	
	// *********************************************************************************************
	// Constructor:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Basic constructor.
	 * 
	 * @param languages The languages for which strings should be provided.
	 */
	public MLStrings(Language[] languages) {
		for (Language lang : languages)
			languageMap.put(lang, new ConcurrentHashMap<String, String>());
		setCurrentLanguage(languages[0]);
	}
	
	/**
	 * This constructor populates the strings collection with data loaded from an xml-file. This
	 * file should have the following format:
	 * 
	 * <pre class="code">
	 * <texts>
	 *   <string id="epidemic_planet">
	 *     <en>EPIDEMIC PLANET</en>
	 *     <ca>PLANETA D’EPIDÈMIES</ca>
	 *     <es>PLANETA DE EPIDEMIAS</es>
	 *     <eu>EPIDEMIEN PLANETA</eu>
	 *     <gl>PLANETA DE EPIDEMIAS</gl>
	 *     <it>EPIDEMIC PLANET</it>
	 *     ...
	 *   </string>
	 *   ...
	 * </texts>
	 * </pre>
	 * 
	 * @param app A PApplet instance.
	 * @param languages The languages for which strings should be provided.
	 * @param xmlPath The path of the source-data xml-file.
	 */
	public MLStrings(PApplet app, Language[] languages, String xmlPath) {
		XML xml = app.loadXML(xmlPath);
		for (Language lang : languages) {
			ConcurrentHashMap<String, String> stringMap = new ConcurrentHashMap<String, String>();
			languageMap.put(lang, stringMap);
			for (XML stringEl : xml.getChildren("string")) {
				XML[] entries = stringEl.getChildren(lang.code.toLowerCase());
				if (entries.length == 0) { throw new Error(
						"There is no entry for the string with id " + stringEl.getString("id")
								+ " for the language " + lang.label + " [" + lang.code + "]."); }
				stringMap.put(stringEl.getString("id"), entries[0].getContent());
			}
		}
		setCurrentLanguage(languages[0]);
	}
	
	// *********************************************************************************************
	// Accessors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return The currently set language.
	 */
	public Language getCurrentLanguage() {
		return currentLanguage;
	}
	
	/**
	 * Sets the currently set language.
	 * 
	 * @param currentLanguage
	 */
	public void setCurrentLanguage(Language currentLanguage) {
		if (currentLanguage == null) { throw new Error("The given language is null."); }
		if (this.currentLanguage == currentLanguage) return;
		
		this.currentLanguage = currentLanguage;
		currentStringMap = languageMap.get(currentLanguage);
		if (currentStringMap == null) { throw new Error(
				"Could not find a strings-map for the currently selected language "
						+ currentLanguage.label + " [" + currentLanguage.code + "]."); }
		dispatchUpdate();
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Returns the string for the given string identifier and for the currently set language.
	 * 
	 * @param id The string identifier.
	 * 
	 * @return The string.
	 */
	public String getString(String id) {
		// System.out.println(">> Strings.getString(" + id + ")");
		String string = currentStringMap.get(id);
		if (string == null) { throw new Error("Could not find the string for id " + id
				+ " for the currently selected language " + currentLanguage.label + " ["
				+ currentLanguage.code + "]."); }
		return string;
	}
	
	// ---------------------------------------------------------------------------------------------
	// MLString management:
	
	/**
	 * Creates and returns a new multi-language string object.
	 * 
	 * @param stringId The string identifier.
	 * 
	 * @return The new multi-language string.
	 */
	public MLString newMLString(String stringId) {
		return new MLString(this, stringId);
	}
	
	/**
	 * The given multi-language string will be set to the appropriate string according to the
	 * currently selected language. This string will be updated and notified when a different
	 * language is set.
	 * 
	 * @param string The multi-language string to set and update.
	 */
	public void addString(MLString string) {
		for (WeakReference<MLString> stringRef : strings) {
			if (stringRef.get() == null) strings.remove(stringRef); // remove empty references
			else if (stringRef.get() == string) return;
		}
		strings.add(new WeakReference<MLString>(string));
	}
	
	/**
	 * Stops updating the given multi-language string.
	 * 
	 * @param mlString
	 */
	public void removeString(MLString mlString) {
		for (WeakReference<MLString> stringRef : strings) {
			if (stringRef.get() == null || stringRef.get() == mlString) {
				// Remove all references that contain the given mlString and all empty references:
				strings.remove(stringRef);
			}
		}
	}
	
	// ---------------------------------------------------------------------------------------------
	// Update-handlers:
	
	/**
	 * Adds an event handler.
	 * 
	 * @param handler The handler to add.
	 */
	public String addHandler(LanguageChangeHandler handler) {
		handlers.add(handler);
		return getString(handler.stringId);
	}
	
	/**
	 * Removes an event handler.
	 * 
	 * @param handler The handler to remove.
	 */
	public void removeHandler(LanguageChangeHandler handler) {
		handlers.remove(handler);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/* Notify the update-handlers that the lanugage was changed. */
	private void dispatchUpdate() {
		for (WeakReference<MLString> stringRef : strings) {
			MLString string = stringRef.get();
			string.updateString(getString(string.getStringId()));
		}
		
		for (LanguageChangeHandler handler : handlers) {
			handler.update(getString(handler.stringId));
		}
	}
	
}
