/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

public final class ComplexDataExample {

    private ComplexDataExample() throws IOException {

    }

    private static Ingredient newIngredient(final String name, final String measure, final String quantity) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(name);
        ingredient.setMeasurement(measure);
        ingredient.setQuantity(ByteBuffer.wrap(quantity.getBytes()));
        return ingredient;
    }

    public static void main(final String[] args) throws IOException {

        File file = new File("recipe.avro");

        System.out.println("Creating recipe object...");
        Recipe recipe = new Recipe();
        recipe.setName("Pizza Dough");
        recipe.setCalories(400L);
        recipe.setAdditionalInstructions("Once doubled in size, roll out into a circle ready for your toppings.");

        ArrayList<Step> steps = new ArrayList<Step>();

        Step step1 = new Step();
        step1.setInstruction("mix well with a whisk and leave until the mixture is frothy");
        ArrayList<Ingredient> ingredientsStep1 = new ArrayList<Ingredient>();
        ingredientsStep1.add(newIngredient("yeast", "grams", "6"));
        ingredientsStep1.add(newIngredient("sugar", "tsp", "0.5"));
        ingredientsStep1.add(newIngredient("water", "ml", "260"));
        step1.setIngredients(ingredientsStep1);
        steps.add(step1);

        Step step2 = new Step();
        step2.setInstruction(
                "knead for around 5 minutes until the mixture has become soft and elastic, then leave to rise.");
        ArrayList<Ingredient> ingredientsStep2 = new ArrayList<Ingredient>();
        ingredientsStep2.add(newIngredient("salt", "tsp", "1"));
        ingredientsStep2.add(newIngredient("strong flour", "grams", "200"));
        ingredientsStep2.add(newIngredient("pasta flour", "grams", "200"));
        step2.setIngredients(ingredientsStep2);
        steps.add(step2);

        recipe.setSteps(steps);
        recipe.setAdditionalInstructions("Once doubled in size, roll out into a circle ready for your toppings.");

        System.out.println("Writing recipe to file...");
        DatumWriter<Recipe> recipeDatumWriter = new SpecificDatumWriter<Recipe>(Recipe.class);
        DataFileWriter<Recipe> dataFileWriter = new DataFileWriter<Recipe>(recipeDatumWriter);
        dataFileWriter.create(Recipe.SCHEMA$, file);
        dataFileWriter.append(recipe);
        dataFileWriter.close();
        System.out.println("File Created.");

        System.out.println("Reading recipe from file...");
        DatumReader<Recipe> recipeDatumReader = new SpecificDatumReader<Recipe>(Recipe.class);
        DataFileReader<Recipe> dataFileReader = new DataFileReader<Recipe>(file, recipeDatumReader);
        Recipe output = null;
        while (dataFileReader.hasNext()) {
            output = dataFileReader.next(output);
            System.out.println(output);
        }
        dataFileReader.close();
        System.out.println("File read complete.");

        file.delete();
        System.out.println("File deleted.");

    }
}
