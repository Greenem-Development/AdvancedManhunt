package org.greenem.plugins.manhunt.completers.util;

import static org.greenem.plugins.manhunt.util.SimpleLogger.log;
import static org.greenem.plugins.manhunt.util.SimpleLogger.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompleterUtils {
	private static String wordsPossiblyInPrackets = "((?:\"\\w+\")|(?:\\w+))";
	private static Pattern patternFindWordAtTheEnd = Pattern.compile(".*?" + wordsPossiblyInPrackets + "$");
	private static Pattern patternWordPossiblyWithBracketsIsAtTheEnd = Pattern.compile("((?:\"\\w+\\s*\"?)|(?:\\w+))$");
	private static String textPatternLastBrackededWord = "(?:[^\"])*(\"[^\"]*\"?)$"; // "(?:[^\"])*(\"[^\"]*\"?)$"
	private static String textPatternLastBrackededWordForReplaceRemoval = "(\"[^\"]*\"?)$";
	private static Pattern lastBrackededWord = Pattern.compile(textPatternLastBrackededWord);
	
	public static String extractAfterQuotes(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        int quoteCount = 0;
        int lastQuoteIndex = -1;

        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '"') {
                quoteCount++;
                if (quoteCount % 2 == 0) {
                    lastQuoteIndex = i;
                }
            }
        }
        
//        System.out.println("Substr " + lastQuoteIndex);
        
        if (lastQuoteIndex == -1) {
            return input; // No matching quote pairs found
        }
        
        return input.substring(lastQuoteIndex + 1);
    }
	
	public static void mains(String[] args) {
		System.out.println(extractAfterQuotes("aaaa \"aaa a \" aaa \"a a"));
	}
	
    public static boolean stillAtTheWordOrBrackets(String str) { // getWordWordOrBracketsWhenStillThere
//        return !str.endsWith(" ");
        
        Matcher matcher = lastBrackededWord.matcher(extractAfterQuotes(str));
//        System.out.println(matcher.matches());
    	if(matcher.matches()) return true;
    	else return !str.endsWith(" ");
    }

//    public static String removeWordAtTheEnd(String str) {
//    	Matcher matcher = lastBrackededWord.matcher(extractAfterQuotes(str));
//    	if(matcher.matches()) {
////    		debug("removing BRACKEDED");
//    		return str.replaceAll(textPatternLastBrackededWordForReplaceRemoval, "").trim();
//    	}
//    	else return str.replaceAll("\\s*" + wordsPossiblyInPrackets + "$", "");
//    }
    
    public static String getWordAtTheEnd(String str) {
    	str = str.trim();
    	Matcher matcher1 = lastBrackededWord.matcher(extractAfterQuotes(str));
    	if(matcher1.matches()) {
//    		debug("getting BRACKEDED");
    		return matcher1.group(1);
    	}
    	Matcher matcher = patternFindWordAtTheEnd.matcher(str);
    	if(matcher.matches()) return matcher.group(1);
    	else return str;
    }
    
    public static List<String> applyFilteredSuggestions(List<String> possibleOptions, String userInput) {
		List<String> choices = new ArrayList<>();
		
		for(int i = 0; i < possibleOptions.size(); i++) {
			String text = possibleOptions.get(i);
			debug("|FilteredSuggestions, length " + text.trim().split("\\s").length + ": " + text);
			if(text.trim().split("\\s").length > 1) {
				possibleOptions.set(i, "\"" + text + "\"");
			}
		}
		
		debug("||User input: " + userInput);
		
		if(userInput != null && userInput.trim().split("\\s").length > 1) {
			userInput = "\"" + userInput + "\"";
		}
		
		for (String option : possibleOptions) {
//			option = option.replace("-", "_").replace(" ", "_");
			// null means here almost just an empty string, but no need to check anything at all
		    if (userInput == null || option.toLowerCase().startsWith(userInput.toLowerCase())) {
//		    	debug(option + " starts with " + userInput);
		        choices.add(option);
		    }
		}
		
		if(userInput != null) { // in null case the stuff is already added by startsWith case
			for (String option : possibleOptions) {
	//			option = option.replace("-", "_").replace(" ", "_");
			    if (option.toLowerCase().contains(userInput.toLowerCase()) && !option.toLowerCase().startsWith(userInput.toLowerCase())) {
	//		    	debug(option + " contains " + userInput + ", but " + option + " doesn't start with " + userInput);
			        choices.add(option);
			    }
			}
		}
		
//		for(int i = 0; i < choices.size(); i++) {
//			String theChoise = choices.get(i);
//			debug(theChoise + " " + theChoise.trim().split("\\s").length);
//			if(theChoise.trim().split("\\s").length > 1) {
//				choices.set(i, "\"" + theChoise + "\"");
//			}
//		}
		
		return choices;
	}
    
    
    /**
     * Remove the quotes if there are any.
     * Should be used right after getting a matcher.group() of a corresponding type or when parsing a name another way
     */
    public static String formatRemoveNameQuotes(String name) {
    	name = name.trim();
    	if(name.startsWith("\"")) {
    		name = name.substring(1);
    	}
    	if(name.endsWith("\"")) {
    		name = name.substring(0, name.length() - 1);
    	}
    	
    	// recursion
    	if(name.startsWith("\"") || name.endsWith("\"")) name = formatRemoveNameQuotes(name);
    	return name;
    }
    
    /**
     * Add quotes if needed.
     */
    public static String formatAddNameQuotes(String name) {
    	if(name.trim().split("\\s").length > 1 && !(name.trim().startsWith("\"") && name.trim().endsWith("\""))) {
			return "\"" + name + "\"";
		}
    	return name;
    }
}
