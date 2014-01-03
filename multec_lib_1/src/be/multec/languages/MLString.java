package be.multec.languages;

import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents a multi-lingual string. The actual string depends on the currently set language in the
 * given MLStrings manager.
 * 
 * @author Wouter Van den Broeck
 */
public class MLString {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	private MLStrings strings;
	
	/* The id of the string. This id is used to get the actual string in the MLStrings manager. */
	private String stringId;
	
	/* The actual string. */
	private String string;
	
	/* The update-handlers to be notified when the string is updated. */
	private final CopyOnWriteArraySet<WeakReference<IMLStringUpdateHandler>> updateHandlers = new CopyOnWriteArraySet<WeakReference<IMLStringUpdateHandler>>();
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @param stringId The id of the string.
	 */
	public MLString(MLStrings strings, String stringId) {
		this.strings = strings;
		this.stringId = stringId;
		this.string = strings.getString(stringId);
		strings.addString(this);
	}
	
	// ---------------------------------------------------------------------------------------------
	
	public void dispose() {
		if (strings != null) {
			strings.removeString(this);
			strings = null;
		}
		stringId = null;
		string = null;
		updateHandlers.clear();
	}
	
	// *********************************************************************************************
	// Accessors:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * @return The actual string.
	 */
	@Override
	public String toString() {
		return string;
	}
	
	/**
	 * @return The string-id that identifies the string in the MLStrings manager.
	 */
	public String getStringId() {
		return stringId;
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * This method should only be called by the MLStrings object.
	 * 
	 * @param string The new actual string.
	 */
	synchronized public void updateString(String string) {
		if (this.string == string) return;
		this.string = string;
		for (WeakReference<IMLStringUpdateHandler> handlerRef : updateHandlers) {
			if (handlerRef.get() == null) updateHandlers.remove(handlerRef);
			else handlerRef.get().stringUpdated(this);
		}
	}
	
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Register the given update-handler. This handler will now be notified when the string is
	 * updated when the current language in the MLStrings manager was changed.
	 * 
	 * This handler is stored with a weak-reference. It will no longer be notified after is was
	 * recycled by the GC. The handler can also be explicitly unregistered by calling the
	 * <em>removeUpdateHandler</em> method.
	 * 
	 * @param handler The update-handler.
	 * 
	 * @see MLString#removeUpdateHandler(IMLStringUpdateHandler)
	 */
	synchronized public void addUpdateHandler(IMLStringUpdateHandler updateHandler) {
		for (WeakReference<IMLStringUpdateHandler> handlerRef : updateHandlers) {
			if (handlerRef.get() == null) updateHandlers.remove(handlerRef);
			else if (handlerRef.get() == updateHandler) return;
		}
		updateHandlers.add(new WeakReference<IMLStringUpdateHandler>(updateHandler));
	}
	
	/**
	 * Remove the given update-handler.
	 * 
	 * @param updateHandler
	 */
	synchronized public void removeUpdateHandler(IMLStringUpdateHandler updateHandler) {
		for (WeakReference<IMLStringUpdateHandler> handlerRef : updateHandlers) {
			if (handlerRef.get() == null || handlerRef.get() == updateHandler)
				updateHandlers.remove(handlerRef);
		}
	}
	
}
