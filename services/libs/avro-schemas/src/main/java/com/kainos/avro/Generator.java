package com.kainos.avro;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;

import org.apache.avro.reflect.ReflectData;
import org.springframework.util.FileSystemUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kainos.avro.conversion.LocalDateTimeConversion;
import com.kainos.avro.source.ErrorEvent;
import com.kainos.pets.api.model.PetRequest;
import com.kainos.petstore.model.Pet;

public class Generator {
    private static final String AVRO_LIBS_DIR = "../avro-schemas/src/main/avro/";
    private static final String COMMON_AVRO = "com.kainos.common.avro";
    private static final String ENUM_WITH_NAMESPACE_REGEXP = "(\"type\":\"enum\",\"name\":\"[\\w\\d]+\",\"namespace\":\")([\\w\\d.]+)(\")";

    public static void main(String[] args) {
        ReflectData reflectData = getReflectData();

        Map<Class, String> sourcePojos = ofEntries(
            entry(PetRequest.class, "PetRequest.avsc"),
            entry(Pet.class, "Pet.avsc"),
            entry(ErrorEvent.class, "ErrorEvent.avsc")
        );

        clearDirectory(AVRO_LIBS_DIR);
        sourcePojos.forEach((aClass, dstAvroFileName) ->
            saveSchemaToFile(
                getSchemaForClass(reflectData, aClass),
                dstAvroFileName
            )
        );
    }

    private static void clearDirectory(String path) {
        FileSystemUtils.deleteRecursively(new File(path));
        File avroLibsDir = new File(path);
        createDirectory(avroLibsDir);
    }

    private static void createDirectory(File directory) {
        if (directory.exists()) {
            return;
        }

        directory.mkdirs();
    }

    private static ReflectData getReflectData() {
        ReflectData.AllowNull reflectData = ReflectData.AllowNull.get();
        reflectData.addLogicalTypeConversion(new LocalDateTimeConversion());
        return reflectData;
    }

    private static void saveSchemaToFile(String avroSchema, String filename) {
        File file = new File(AVRO_LIBS_DIR + filename);
        createDirectory(file.getParentFile());

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), UTF_8);
             PrintWriter out = new PrintWriter(writer)) {
            out.print(toPrettyFormat(avroSchema));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getSchemaForClass(ReflectData reflectData, Type type) {
        String schema = reflectData.getSchema(type).toString();
        return schema
            .replaceAll(ENUM_WITH_NAMESPACE_REGEXP, "$1$2Enums$3")
            .replace("com.kainos.avro.source", COMMON_AVRO)
            .replace("com.kainos.pets.api.model", "com.kainos.pets.avro")
            .replace("com.kainos.petstore.model", "com.kainos.petstore.avro");
    }

    private static String toPrettyFormat(String jsonString) {
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        return gson.toJson(json);
    }
}

