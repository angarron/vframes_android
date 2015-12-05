package testdata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import data.json.model.CharactersModelJsonAdapter;
import data.model.*;
import data.model.character.SFCharacter;
import testdata.testdata.CharacterFactory;

public class TestDataGenerator {

    //TODO: make this accept parameters and write to file instead of print to console
    public static void main(String[] args) {
        CharactersModel charactersModel = generateRandomData();
        JsonObject modelJson = CharactersModelJsonAdapter.CharactersModelToJson(charactersModel);

        if (args.length != 0) {
            String fileName = args[0];
            writeJsonToFile(modelJson, fileName);
        } else {
            System.out.println("No file specified in args.");
        }

        System.out.println(modelJson.toString());
    }

    private static CharactersModel generateRandomData() {
        Map<CharacterName, SFCharacter> charactersMap = new HashMap<>();

        CharacterFactory characterFactory = new CharacterFactory();
        for (CharacterName name : CharacterName.values()) {
            SFCharacter character = characterFactory.generateCharacter(5);
            charactersMap.put(name, character);
        }

        return new CharactersModel(charactersMap);
    }

    private static void writeJsonToFile(JsonObject dataJson, String fileName) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(dataJson);

        try (FileWriter file = new FileWriter(fileName)) {
            file.write(prettyJson);
            System.out.println("Successfully wrote Json object to file: " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

}
