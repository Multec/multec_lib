package be.multec.languages;

/**
 * @author Wouter Van den Broeck
 */
public class Language {
	
	// *********************************************************************************************
	// Attributes:
	// ---------------------------------------------------------------------------------------------
	
	public static Language EN = new Language("EN", "English");
	public static Language CA = new Language("CA", "Catalˆ"); // Catalan - Catalˆ
	public static Language ES = new Language("ES", "Castellano"); // Castilian - Castellano
	public static Language EU = new Language("EU", "Euskara"); // Basque - Euskara
	public static Language GL = new Language("GL", "Galego"); // Galician - Galego
	//public static Language IT = new Language("IT", "Italiano"); // Italian - Italiano
	//public static Language NL = new Language("NL", "Nederlands"); // Dutch - Nederlands
	
	// ---------------------------------------------------------------------------------------------
	
	public String code;
	
	public String label;
	
	// *********************************************************************************************
	// Constructors:
	// ---------------------------------------------------------------------------------------------
	
	public Language(String code, String label) {
		this.code = code;
		this.label = label;
	}
	
	// *********************************************************************************************
	// Methods:
	// ---------------------------------------------------------------------------------------------
	
}
