package net.osmand.search.example.core;

import java.util.ArrayList;
import java.util.List;

import net.osmand.CollatorStringMatcher;
import net.osmand.StringMatcher;
import net.osmand.CollatorStringMatcher.StringMatcherMode;
import net.osmand.binary.BinaryMapIndexReader;
import net.osmand.data.LatLon;

//immutable object
public class SearchPhrase {
	
	private List<SearchWord> words = new ArrayList<>();
	private String text = "";
	private String lastWord = "";
	private CollatorStringMatcher sm;
	private SearchSettings settings;
	private List<BinaryMapIndexReader> indexes;
	
	public SearchPhrase(SearchSettings settings) {
		this.settings = settings;
	}

	public List<SearchWord> getWords() {
		return words;
	}
	
	public List<BinaryMapIndexReader> getOfflineIndexes() {
		if(indexes != null) {
			return indexes; 
		}
		return settings.getOfflineIndexes();
	}
	
	public SearchSettings getSettings() {
		return settings;
	}
	
	
	public int getRadiusLevel() {
		return settings.getRadiusLevel();
	}
	
	public SearchPhrase selectWord(SearchResult res) {
		SearchPhrase sp = new SearchPhrase(this.settings);
		sp.words.addAll(this.words);
		SearchWord sw = new SearchWord(res.mainName.trim(), res);
		sp.words.add(sw);
		// sp.text = this.text + sw.getWord() + ", ";
		// TODO FIX
		sp.text = this.text + " " + sw.getWord() + ", ";
		return sp;
	}
	
	
	
	public List<SearchWord> excludefilterWords() {
		 List<SearchWord> w = new ArrayList<>();
		 for(SearchWord s : words) {
			 if(s.getResult() == null) {
				 w.add(s);
			 }
		 }
		 return w;
	}
	
	public boolean isLastWord(ObjectType p) {
		for (int i = words.size() - 1; i >= 0; i--) {
			SearchWord sw = words.get(i);
			if (sw.getType() == p) {
				return true;
			} else if (sw.getType() != ObjectType.UNKNOWN_NAME_FILTER) {
				return false;
			}
		}
		return false;
	}
	
	public StringMatcher getNameStringMatcher() {
		if(sm != null) {
			return sm;
		}
		sm = new CollatorStringMatcher(lastWord, StringMatcherMode.CHECK_STARTS_FROM_SPACE);
		return sm;
	}
	
	public boolean hasSameConstantWords(SearchPhrase p) {
		return excludefilterWords().equals(p.excludefilterWords());
	}
	
	public boolean hasObjectType(ObjectType p) {
		for(SearchWord s : words) {
			if(s.getType() == p) {
				return true;
			}
		}
		return false;
	}
	
	public String getText() {
		return text;
	}
	
	public String getStringRerpresentation() {
		StringBuilder sb = new StringBuilder();
		for(SearchWord s : words) {
			sb.append(s.getWord()).append(" [" + s.getType() + "], ");
		}
		sb.append(lastWord);
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return getStringRerpresentation();
	}

	public boolean isNoSelectedType() {
		return words.isEmpty();
	}

	public boolean isEmpty() {
		return words.isEmpty() && lastWord.isEmpty();
	}
	
	
	public String getLastWord() {
		return lastWord;
	}

	public LatLon getLastTokenLocation() {
		for(int i = words.size() - 1; i >= 0; i--) {
			SearchWord sw = words.get(i);
			if(sw.getLocation() != null) {
				return sw.getLocation();
			}
		}
		// last token or myLocationOrVisibleMap if not selected 
		return settings.getOriginalLocation();
	}

	
	public SearchPhrase generateNewPhrase(String text, SearchSettings settings) {
		SearchPhrase sp = new SearchPhrase(settings);
		String atext = text;
		List<SearchWord> leftWords = this.words;
		if (text.startsWith((this.text + this.lastWord).trim())) {
			// string is longer
			atext = text.substring(this.text.length());
			sp.text = this.text;
			sp.words = new ArrayList<>(this.words);
			leftWords = leftWords.subList(leftWords.size(), leftWords.size());
		} else {
			sp.text = "";
		}
		if (!atext.contains(",")) {
			sp.lastWord = atext;
		} else {
			String[] ws = atext.split(",");
			for (int i = 0; i < ws.length - 1; i++) {
				boolean unknown = true;
				if (ws[i].trim().length() > 0) {
					if (leftWords.size() > 0) {
						if (leftWords.get(0).getWord().equalsIgnoreCase(ws[i].trim())) {
							sp.words.add(leftWords.get(0));
							leftWords = leftWords.subList(1, leftWords.size());
							unknown = false;
						}
					}
					if(unknown) {
						sp.words.add(new SearchWord(ws[i].trim()));
					}
					sp.text += ws[i] + ", ";
				}
				
			}
		}
		sp.text = sp.text.trim();
		return sp;
	}



	public void selectFile(BinaryMapIndexReader object) {
		if(indexes == null) {
			indexes = new ArrayList<>();
		}
		this.indexes.add(object);
	}
}
