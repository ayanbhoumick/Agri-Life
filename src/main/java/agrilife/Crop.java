package agrilife;

import java.util.List;

public class Crop {

    private final String name;
    private final String emoji;
    private final List<String> pestKeys;

    public Crop(String name, String emoji, List<String> pestKeys) {
        this.name = name;
        this.emoji = emoji;
        this.pestKeys = pestKeys;
    }

    public String getName() { return name; }
    public String getEmoji() { return emoji; }
    public List<String> getPestKeys() { return pestKeys; }
}
