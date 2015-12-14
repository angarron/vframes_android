package testdata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import data.json.model.VFramesDataJsonAdapter;
import data.model.IDataModel;

public class TestDataReader {

    //TODO: use a real parameter parser and consolidate this with TestDataGenerator
    public static void main(String[] args) {

        if (args.length != 1) {
            throw new IllegalArgumentException("No file specified in args.");
        }

        String fileName = args[0];
        String fileData = readFileToString(fileName);
        JsonParser parser = new JsonParser();
        JsonObject jsonData = parser.parse(fileData).getAsJsonObject();

        IDataModel dataModel = VFramesDataJsonAdapter.jsonToDataModel(jsonData);
        System.out.println("success loading data model version: " + dataModel.getVersion().toString());
        System.out.println("loaded " + dataModel.getCharactersModel().getCharacters().size() + " characters.");
    }

    private static String readFileToString(String fileName)
    {
        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(fileBytes);
    }
}
